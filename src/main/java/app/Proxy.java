/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app;

import static java.lang.String.format;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.ByteString.Output;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.JettyServer;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.web.HttpSC;
import org.eclipse.jetty.util.IO;
import org.seaborne.protobuf.HttpProxyGrpc;
import org.seaborne.protobuf.HttpProxyGrpc.HttpProxyBlockingStub;
import org.seaborne.protobuf.PB_HTTP.GrpcHttpRequest;
import org.seaborne.protobuf.PB_HTTP.GrpcHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Proxy {
    public static void main(String...a) {

        try {
            main$();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally { System.exit(0); }
    }

    public static int portProxy   = 2020;
    public static int portGRPC    = 28181;
    public static int portFuseki  = 3003;
    public static String PROXY    = "proxy";
    public static String SERVICE  = "fuseki";

    // Dataset name , with initial "/"
    public static String DS_NAME   = "/ds";

    public static String PROXY_URL = endpoint("localhost", portProxy, PROXY);
    public static String FUSEKI_URL = endpoint("localhost", portFuseki, "");
    public static String FUSEKI_DS = FUSEKI_URL+DS_NAME;

    // Header names used for GRPC. These are chosen to be distinct from all possible HTTP header names.
    public static String G_SERVICE      = "$servuice";
    public static String G_REQUEST      = "$request";
    public static String G_METHOD       = "$method";
    public static String G_QUERYSTRING  = "$querystring" ;

    // Map service name to proxied endpoint.
    public static Map<String, String> serviceRegistry = Map.of("fuseki", "localhost:"+portFuseki);

    // HTTP headers not to copy
    static Set<String> excludeRequestHeaders  = Set.of("Host", "Connection", "Content-Length");
    // Content-Length is "system only".
    static Set<String> excludeResponseHeaders = Set.of("Vary", "Cache-Control", "Pragma", "Content-Length");

    // The client sends:
    // URL:     http://localhost:2020/proxy/fuseki/ds?query=
    // Proxy    http://localhost:2020/proxy/
    // Service                               fuseki
    // Request                                     /ds?query=

    public static void main$() throws IOException {
        setupServers();
        dwim();
    }

    public static void setupServers() throws IOException {
        // -- Jetty server as HTTP proxy relay
        String pathSpec = PROXY.isEmpty() ? "/*" : "/"+PROXY+"/*";
        JettyServer jettyServer = JettyServer.create()
            .port(portProxy)
            .addServlet(pathSpec, new ProxyServlet())
            .build();
        jettyServer.start();

        // -- GRPC Server : provides a GRPC call to make a HTTP request and return the response
        io.grpc.Server grpcServer = ServerBuilder.forPort(portGRPC).addService(new ProxyService()).build();
        grpcServer.start();

        // -- Fuseki database server
        FusekiLogging.setLogging();
        FusekiServer fusekiServer =  FusekiServer.create()
            .port(portFuseki)
            //.verbose(true)
            .add(DS_NAME, DatasetGraphFactory.createTxnMem())
            .build();
        fusekiServer.start();
        // --
    }

    public static void dwim() {
        // Use the setup.
        // SPARQL update;  SPARQL query then SPARQL GSP get()
        try ( RDFConnection conn = RDFConnectionFactory.connect(FUSEKI_DS) ) {
            // SPARQL update
            conn.update("INSERT DATA { <x:s> <x:p> 123 }");

            // SPARQL query
            conn.queryResultSet("SELECT * { ?s ?p ?o }", rs->ResultSetFormatter.out(rs));

            // SPARQL GSP
            Model model = conn.fetch();
            RDFDataMgr.write(System.out, model,  Lang.NT);
        }
        // ----
        System.out.println();
        // Bad things.
        try ( RDFConnection conn = RDFConnectionFactory.connect(FUSEKI_DS+"X") ) {
            conn.queryAsk("ASK{}");
        } catch (QueryExceptionHTTP ex) {
            Log.info(Proxy.class, "Got: "+ex.getStatusCode());
        }

        // Bad things.
        try ( RDFConnection conn = RDFConnectionFactory.connect("http://localhost:2020/proxy/joseki/ds") ) {
            conn.queryAsk("ASK{}");
        } catch (QueryExceptionHTTP ex) {
            Log.info(Proxy.class, "Got: "+ex.getStatusCode());
        }
    }

    // ----------

    /**
     *  Servlet for the Jetty server acting as the front-end proxy.
     *  <p>
     *  HTTP -> GRPC request
     *  <br/>
     *  GRPC response -> HTTP
     */
    static class ProxyServlet extends HttpServlet {
        // Regex for proxy+service+request
        private static Pattern serviceRegex = Pattern.compile("/([^/]*)/([^/])/(.*)");

        // Map GET and POST
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException { proxy(req, res); }
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException { proxy(req, res); }

        private void proxy(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
            // Extract HTTP reuest details.
            String host = httpRequest.getHeader("Host");
            String method = httpRequest.getMethod();
            // getRequestURI - the HTTP request line path. (c.f. getRequestURL)
            String requestURI = httpRequest.getRequestURI().toString();
            String queryString = httpRequest.getQueryString();

            // Decode URL
            Matcher matcher = serviceRegex.matcher(requestURI);
            if ( ! matcher.matches() ) {
                httpResponse.sendError(HttpSC.BAD_REQUEST_400);
                return ;
            }
            String serviceName = matcher.group(2);
            String serviceRequest = matcher.group(3);

            // ** Do any checking here **

            // Build GRPC request message
            Map<String, String> httpHeaders = new HashMap<>();
            copyHeaders(httpRequest, httpHeaders);
            if ( queryString != null )
                httpHeaders.put(G_QUERYSTRING, queryString);
            httpHeaders.put(G_METHOD, method);
            httpHeaders.put(G_SERVICE, serviceName);
            httpHeaders.put(G_REQUEST, serviceRequest);
            Output output = ByteString.newOutput();
            try {
                byte[] reqBody = IO.readBytes(httpRequest.getInputStream());
                output.write(reqBody);
            } catch (IOException e) {
                e.printStackTrace();
                httpResponse.sendError(HttpSC.BAD_REQUEST_400, "IOException");
                return ;
            }
            // Build GRPC request
            ByteString bStr = output.toByteString();
            Any body = Any.newBuilder()
                //.setTypeUrl("") // Don't care
                .setValue(bStr)
                .build();

            // GRPC
            GrpcHttpRequest grpcRequest = GrpcHttpRequest.newBuilder()
                    .putAllHeaders(httpHeaders)
                    .setBody(body)
                    .build();

            ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost",28181).usePlaintext().build();
            HttpProxyBlockingStub blockingStub = HttpProxyGrpc.newBlockingStub(channel);
            //asyncStub = RouteGuideGrpc.newStub(channel);

            // Make call.
            GrpcHttpResponse grpcResponse = blockingStub.http(grpcRequest);

            // Unpack GRPC response, convert to HTTP response, return response.
            grpcResponse.getHeadersMap().forEach((k,v)->httpResponse.setHeader(k,v));
            httpResponse.setStatus(grpcResponse.getStatusCode());
            try ( OutputStream reponseBody = httpResponse.getOutputStream() ) {
                reponseBody.write(grpcResponse.getBody().getValue().toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * GRPC service that calls the backend service endpoint
     * <p>
     * GRPC -> HTTP request
     * <br/>
     * HTTP response -> GRPC
     */
    static class ProxyService extends HttpProxyGrpc.HttpProxyImplBase {
        private static Logger LOG = LoggerFactory.getLogger("GRPC Proxy");
        private static HttpClient httpClient = HttpClient.newBuilder().build();

        @Override
        public void http(GrpcHttpRequest grpcRequest, StreamObserver<GrpcHttpResponse> responseObserver) {
            // Extract details.
            Map<String, String> map = grpcRequest.getHeadersMap();
            String method = map.get(G_METHOD);
            String serviceName = map.get(G_SERVICE);
            String queryString = map.get(G_QUERYSTRING);
            String serviceRequest = map.get(G_REQUEST);
            // check.
            if ( serviceName == null || ! serviceRegistry.containsKey(serviceName) ) {
                sendError(responseObserver, "No service registered");
                return;
            }
            // Service to HTTP endpoint
            String url = serviceRegistry.get(serviceName)+serviceRequest;
            if ( queryString != null && ! queryString.isEmpty() )
                url = url+"?"+queryString;
            LOG.info(format("==> %s %s", method, url));
            byte[] body = grpcRequest.getBody().getValue().toByteArray();

            // HTTP request
            HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));
            grpcRequest.getHeadersMap().forEach((k,v)->{
                if ( excludeResponseHeaders.contains(k) ) return;
                // Content-Length is restricted.
                builder.setHeader(k,v);
            });

            switch(method) {
                case "GET":builder.GET();break;
                case "POST":builder.POST(BodyPublishers.ofByteArray(body));break;
                default:
                    sendError(responseObserver, "Unknown method: "+method);
                    return;
            }

            HttpRequest httpRequest = builder.build();
            try {
                HttpResponse<byte[]> httpResponse = httpClient.send(httpRequest, BodyHandlers.ofByteArray());
                byte[] httpResponseBody = httpResponse.body();
                Output out = ByteString.newOutput();
                out.write(httpResponseBody);
                Any anyBody = Any.newBuilder().setValue(out.toByteString()).build();

                // HTTP headers
                Map<String, String> httpHeaders = new HashMap<>();
                copyHeaders(httpResponse, httpHeaders);

                GrpcHttpResponse response = GrpcHttpResponse.newBuilder()
                    .putAllHeaders(httpHeaders)
                    .setBody(anyBody)
                    .setStatusCode(httpResponse.statusCode())
                    .build();
                // Send response.
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        // GRPC says "no".
        private void sendError(StreamObserver<GrpcHttpResponse> responseObserver, String string) {
            GrpcHttpResponse response = GrpcHttpResponse.newBuilder()
                .setStatusCode(HttpSC.BAD_REQUEST_400)
                .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    // Utility : Make an endpoint URL
    private static String endpoint(String hostname, int port, String path) {
        if (path.startsWith("/") )
            path = path.substring(1);
        return format("http://%s:%d/%s", hostname, port, path);
    }

    // Utility : Filter and copy headers from an HTTP request into a GRPC headers map.
    static void copyHeaders(HttpServletRequest request, Map<String, String> httpHeaders) {
        Iter.iter(request.getHeaderNames().asIterator())
            .filter(h->excludeRequestHeaders.contains(h))
            .forEach(h->httpHeaders.put(h, request.getHeader(h)));
    }

    // Utility : Filter and copy headers from an HTTP response into a GRPC headers map.
    static <X> void copyHeaders(HttpResponse<X> response, Map<String, String> httpHeaders) {
        // Copy headers from an HTTP response into GRPC headers map.
        Map<String, List<String>> map = response.headers().map();
        map.keySet().stream()
            .filter(h->excludeResponseHeaders.contains(h))
            .forEach(h->httpHeaders.put(h, map.get(h).get(0)));
    }
}
