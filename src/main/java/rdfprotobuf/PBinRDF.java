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

package rdfprotobuf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Consumer;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.query.ResultSet;
import org.apache.jena.riot.protobuf.PB_RDF.RDF_Stream;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ResultSetStream;
import org.apache.jena.sparql.engine.binding.Binding;

/** Operations for protobuf-encoded RDF */
public class PBinRDF {

    private static int BUFSIZE_IN   = 64*1024 ;

    // ==== Delimited

    public static void inputStreamToStreamRDF(InputStream input, StreamRDF stream) {
        Protobuf2StreamRDF.process(input, stream);
    }

    public static StreamRDF streamToOutputStream(OutputStream output) {
        return StreamRDF2Protobuf.createDelimited(output);
    }

    // ==== Block

    /** RDF_Stream - a block of RDF_StreamRow. */
    public static void inputStreamBlkToStreamRDF(InputStream input, StreamRDF stream) {
        Protobuf2StreamRDF visitor = new Protobuf2StreamRDF(PrefixMapFactory.create(), stream);
        try {
            RDF_Stream rdfStream2 = RDF_Stream.parseFrom(input);
            stream.start();
            rdfStream2.getRowList().forEach( sr -> visitor.visit(sr) );
            stream.finish();
        } catch (IOException ex) { IO.exception(ex); }
    }

    public static void streamToOutputStreamBlk(OutputStream outputStream, Consumer<StreamRDF> streamDest) {
        StreamRDF2Protobuf.writeBlk(outputStream, streamDest);
    }

    // ==== ResultSet

    public static ResultSet readResultSet(InputStream input) {
        Protobuf2Binding p2b = new Protobuf2Binding(input);
        var resultVars = p2b.getVarNames();
        return new ResultSetStream(resultVars, null, p2b);
    }

    public static void writeResultSet(OutputStream out, ResultSet resultSet) {
        List<Var> vars = Var.varList(resultSet.getResultVars()) ;
       try ( Binding2Protobuf b2p = new Binding2Protobuf(out, vars, false) ) {
           for ( ; resultSet.hasNext() ; ) {
               Binding b = resultSet.nextBinding();
               b2p.output(b);
           }
       }
    }

}

