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

package rdfavro;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.compiler.specific.SpecificCompiler;
import org.apache.jena.atlas.io.IO;

public class GenerateAvroRDF {
    public static void main(String[] args) throws IOException {
        Schema tripleSchema = tripleSchema();
//        String s = tripleSchema.toString(true);
//        System.out.println(s);

        String FN = "avro.avsc";

        OutputStream out = Files.newOutputStream(Paths.get(FN));
        try ( PrintWriter pw = IO.asPrintWriterUTF8(out) ) {
            pw.print(tripleSchema.toString(true));
        }

        SpecificCompiler.compileSchema(new File(FN), new File("src/main/avro-generated"));
        System.out.println("DONE");
    }


    public static Schema tripleSchema() {

        String NS = "org.apache.jena.riot.avro";

        // IRI or prefix name.
        Schema termIRI = SchemaBuilder.record("TermIRI")
            .namespace(NS)
            .fields()
            .name("iri")
              .type()
              .enumeration("Kind").symbols("IRI","PN")
              .noDefault()
            .requiredString("value")
            .endRecord();

        Schema termBNode = SchemaBuilder.record("TermBlankNode")
            .namespace(NS)
            .fields()
            .requiredString("label")
            .endRecord();

        Schema termLiteral = SchemaBuilder.record("TermLiteral")
            .namespace(NS)
            .fields()
            .requiredString("lex")
            // Oneof
            .optionalString("lang")
            .name("datatype").type().optional().type(termIRI)
            .endRecord();

        Schema termSubj = Schema.createUnion(termIRI, termBNode);
        Schema termObj = Schema.createUnion(termIRI, termBNode, termLiteral);

//        Schema termIRIOrBNode = SchemaBuilder.record("IRIOrBlankNode")
//            .namespace(NS)
//            .fields().name("iri_bnode").type()
//                .unionOf()
//                    .array().items().type(termIRI).and().type(termBNode).endUnion()
//                .noDefault()
//            .endRecord();

//        System.out.println("---- IRI");
//        System.out.println(termIRI.toString(true));
//        System.out.println("---- Literal");
//        System.out.println(termLiteral.toString(true));


        Schema triple = SchemaBuilder.record("ATriple")
            .namespace(NS)
            .fields()
            .name("subject")
                .type(termSubj)
//                .type().unionOf().array().items()
//                  .type(termIRI).and().type(termBNode).endUnion()
                .noDefault()
            .name("predicate")
              .type(termIRI)
              .noDefault()
            .name("object")
               .type(termObj)
//                .type().unionOf().array().items()
//                   .type(termIRI).and()
//                   .type(termBNode).and()
//                   .type(termLiteral)
//                   .endUnion()
                 .noDefault()
        .endRecord();

        //System.out.println("---- Triple");

        return triple;
    }
}

