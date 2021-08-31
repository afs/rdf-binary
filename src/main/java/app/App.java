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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.RIOT;
import org.apache.jena.riot.protobuf.PB_RDF.*;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.writer.WriterStreamRDFFlat;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.FmtUtils;
import org.apache.jena.sys.JenaSystem;
import rdfprotobuf.*;

public class App {

    static {
        JenaSystem.init();
        //FusekiLogging.setLogging();
        LogCtl.setLog4j2();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    // [ ] Protobuf2StreamRDF - reusae builders.

    // Lists and repeated fields? Chunks.

    private static RDF_StreamRow.Builder streamRowBuilder = RDF_StreamRow.newBuilder();
    private static RDF_Triple.Builder tripleBuilder = RDF_Triple.newBuilder();
    private static RDF_Quad.Builder quadBuilder = RDF_Quad.newBuilder();
    private static RDF_Term.Builder termBuilder = RDF_Term.newBuilder();
//    private static RDF_PrefixDecl.Builder prefixBuilder = RDF_PrefixDecl.newBuilder();
//    private static RDF_IRI.Builder baseBuilder = RDF_IRI.newBuilder();

    public static void main(String[] args) throws IOException {
        Triple triple1 = SSE.parseTriple("(<_:label1> :p 123)");
        Triple triple2 = SSE.parseTriple("(<_:label1> :p 456)");


//        RDF_Stream.Builder rdfStream = RDF_Stream.newBuilder();
//        streamRowBuilder.clear();
//
//        rdfStream.addRow(
//            streamRowBuilder.setTriple(PB.rdfTriple(triple1, tripleBuilder, termBuilder)).build()
//        );
//        rdfStream.addRow(
//            streamRowBuilder.setTriple(PB.rdfTriple(triple2, tripleBuilder, termBuilder)).build()
//        );

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PBinRDF.streamToOutputStreamBlk(out, s->{
            s.triple(triple1);
            s.triple(triple2);
        });

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        StreamRDF recv = new WriterStreamRDFFlat(System.out, ARQ.getContext());

        PBinRDF.inputStreamBlkToStreamRDF(in, recv);
    }

    public static void main3(String[] args) throws IOException {

        Triple triple1 = SSE.parseTriple("(<_:label1> :p 123)");
        Triple triple2 = SSE.parseTriple("(<_:label1> :p 456)");

        ByteArrayOutputStream out = new ByteArrayOutputStream();


        StreamRDF stream = PBinRDF.streamToOutputStream(out);
        stream.start();
        stream.triple(triple1);
        stream.triple(triple2);
        stream.prefix("x", "http:/example/");
        stream.finish();

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

        StreamRDF recv = new WriterStreamRDFFlat(System.out, ARQ.getContext());
        //StreamRDF recv = StreamRDFLib.print(System.out);
        PBinRDF.inputStreamBlkToStreamRDF(in, recv);

    }




    // Delimited works - at what cost?
    public static void main2(String[] args) throws IOException {

        Triple triple1 = SSE.parseTriple("(_:b :p 123)");
        Triple triple2 = SSE.parseTriple("(_:b :p 456)");

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        RDF_Triple rdfTriple1 = ProtobufConvert.convert(triple1, false);
        rdfTriple1.writeDelimitedTo(out);

        RDF_Triple rdfTriple2 = ProtobufConvert.convert(triple2, false);
        rdfTriple2.writeDelimitedTo(out);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

        while(true) {
            RDF_Triple rdfTriplex = RDF_Triple.parseDelimitedFrom(in);
            if ( rdfTriplex == null )
                // EOF signal.
                break;
            Triple tx = ProtobufConvert.convert(rdfTriplex);
            System.out.println(FmtUtils.stringForTriple(tx));
        }
    }

    public static void main1(String[] args) throws IOException {

        Triple triple1 = SSE.parseTriple("(_:b :p 123)");
        Triple triple2 = SSE.parseTriple("(_:b :p 456)");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CodedOutputStream cout = CodedOutputStream.newInstance(out);


        RDF_Triple rdfTriple1 = ProtobufConvert.convert(triple1, false);
        rdfTriple1.writeTo(cout);
        int x1 = cout.getTotalBytesWritten();
        System.out.println("len1 = "+x1);

        RDF_Triple rdfTriple2 = ProtobufConvert.convert(triple2, false);
        rdfTriple2.writeTo(cout);
        int x2 = cout.getTotalBytesWritten();
        System.out.println("len1 = "+x2);

        cout.flush();

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        CodedInputStream cin = CodedInputStream.newInstance(in);


        RDF_Triple rdfTriplex1 = RDF_Triple.parseFrom(cin);
        int a1 = cin.getTotalBytesRead();
        System.out.println("in len1 = "+a1);
        Triple tx1 = ProtobufConvert.convert(rdfTriplex1);

        RDF_Triple rdfTriplex2 = RDF_Triple.parseFrom(cin);
        int a2 = cin.getTotalBytesRead();
        System.out.println("in len1 = "+a2);


        Triple tx2 = ProtobufConvert.convert(rdfTriplex2);

        System.out.println(FmtUtils.stringForTriple(tx1));
        System.out.println(FmtUtils.stringForTriple(tx2));

    }

}

