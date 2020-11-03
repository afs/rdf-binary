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

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.protobuf.PB_RDF.RDF_Triple;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.FmtUtils;
import rdfprotobuf.ProtobufConvert;

public class App {

    public static void main(String[] args) throws IOException {
        Triple triple = SSE.parseTriple("(_:b :p 123)");
        RDF_Triple rdfTriple = ProtobufConvert.convert(triple, false);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CodedOutputStream cout = CodedOutputStream.newInstance(out);
        rdfTriple.writeTo(cout);
        cout.flush();

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

        CodedInputStream cin = CodedInputStream.newInstance(in);


        RDF_Triple rdfTriple2 = RDF_Triple.parseFrom(cin);
        Triple triple2 = ProtobufConvert.convert(rdfTriple2);

        System.out.println(FmtUtils.stringForTriple(triple2));

    }

}

