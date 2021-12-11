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

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.RIOT;
import org.apache.jena.riot.protobuf.ProtobufRDF;
import org.apache.jena.riot.protobuf.ProtobufRDF_Blk;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.writer.WriterStreamRDFFlat;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sys.JenaSystem;

public class App {

    static {
        JenaSystem.init();
        // FusekiLogging.setLogging();
        LogCtl.setLog4j2();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    public static void main(String...args) throws IOException {
        String DIR = "/home/afs/Telicent/Projects/binary/";
        riotcmd.riot.main("--sink", DIR+"b-25m.rpb");
        //riotcmd.riot.main(DIR+"b-25m.rt");
    }

    public static void mainDelimited() throws IOException {
        Triple triple1 = SSE.parseTriple("(<_:label1> :p 123)");
        Triple triple2 = SSE.parseTriple("(<_:label1> :p 456)");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Delimited, not Blk
        StreamRDF s = ProtobufRDF.streamToOutputStream(out);
        s.start();
        s.triple(triple1);
        s.triple(triple2);
        s.finish();

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        StreamRDF recv = new WriterStreamRDFFlat(System.out, ARQ.getContext()) {
            @Override
            protected void outputNode(Node n) {
                if ( !n.isBlank() ) {
                    fmt.format(out, n);
                    return;
                }
                out.print("<");
                out.print(RiotLib.blankNodeToIriString(n));
                out.print(">");

            }
        };

        ProtobufRDF.inputStreamToStreamRDF(in, recv);
    }

    public static void mainBlk(String[] args) throws IOException {
        Triple triple1 = SSE.parseTriple("(<_:label1> :p 123)");
        Triple triple2 = SSE.parseTriple("(<_:label1> :p 456)");

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Blk
        ProtobufRDF_Blk.streamToOutputStreamBlk(out, s -> {
            s.triple(triple1);
            s.triple(triple2);
        });

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        StreamRDF recv = new WriterStreamRDFFlat(System.out, ARQ.getContext()) {
            @Override
            protected void outputNode(Node n) {
                if ( !n.isBlank() ) {
                    fmt.format(out, n);
                    return;
                }
                out.print("<");
                out.print(RiotLib.blankNodeToIriString(n));
                out.print(">");
            }
        };

        ProtobufRDF_Blk.inputStreamBlkToStreamRDF(in, recv);
    }
}
