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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.file.SeekableByteArrayInput;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.jena.riot.avro.*;

public class Avro {
    public static void main(String ...a) throws IOException {
        //Codegen:: Schema tripleSchema = GenerateAvroRDF.tripleSchema();

        ATriple atriple1 = new ATriple();
        TermIRI iri = TermIRI.newBuilder()
            .setIri(Kind.IRI).setValue("http://example/s").build();
        atriple1.setSubject(iri);

        var x = TermIRI.newBuilder().setIri(Kind.PN).setValue("ex:p").build();
        atriple1.setPredicate(x);

        TermLiteral termObjLiteral1 = TermLiteral.newBuilder().setLex("ABC").setLang("en").build();

        TermIRI termObjIRI = TermIRI.newBuilder().setIri(Kind.PN).setValue("ex:z").build();

        atriple1.setObject(termObjIRI);

//        JsonValue jv = JSON.parse(atriple1.toString());
//        System.out.println(jv);

        ATriple atriple2 = new ATriple();
        TermBlankNode s2 = TermBlankNode.newBuilder()
            .setLabel("ABCD").build();
        atriple2.setSubject(s2);

        TermIRI p2 = TermIRI.newBuilder().setIri(Kind.PN).setValue("ex:p").build();
        atriple2.setPredicate(p2);

        TermLiteral termObjLiteral2 = TermLiteral.newBuilder().setLex("ABC").setLang("en").build();

        atriple2.setObject(termObjLiteral2);

        //if ( false )
        {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DatumWriter<ATriple> userDatumWriter = new SpecificDatumWriter<ATriple>(atriple1.getSchema());
            try ( DataFileWriter<ATriple> dataFileWriter = new DataFileWriter<ATriple>(userDatumWriter) ) {

                dataFileWriter.create(atriple1.getSchema(), bout);
                dataFileWriter.append(atriple1);
                dataFileWriter.append(atriple2);
            }

            // Without schema.
            ByteArrayInputStream bytes = new ByteArrayInputStream(bout.toByteArray());


            // With schema.
            SeekableByteArrayInput seekByteIn = new SeekableByteArrayInput(bout.toByteArray());

            DatumReader<ATriple> userDatumReader = new SpecificDatumReader<ATriple>(ATriple.class);
            try ( DataFileReader<ATriple> dataFileReader = new DataFileReader<ATriple>(seekByteIn, userDatumReader) ) {
                ATriple inTriple = null;
                while (dataFileReader.hasNext()) {
                    // Reuse user object by passing it to next(). This saves us from
                    // allocating and garbage collecting many objects for files with
                    // many items.
                    inTriple = dataFileReader.next(inTriple);
                    System.out.println(inTriple);
                    //System.out.println(inTriple.getSubject());
                }
            }
        }

        System.out.println("DONE");
        System.exit(0);

        if ( false )
        {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DatumWriter<ATriple> userDatumWriter = new SpecificDatumWriter<ATriple>(atriple1.getSchema());
            try ( DataFileWriter<ATriple> dataFileWriter = new DataFileWriter<ATriple>(userDatumWriter) ) {

                dataFileWriter.create(atriple1.getSchema(), bout);
                dataFileWriter.append(atriple1);
                dataFileWriter.append(atriple2);
            }

            // Generic.
            SeekableByteArrayInput seekByteIn = new SeekableByteArrayInput(bout.toByteArray());
            DatumReader<GenericRecord> datumReader = new GenericDatumReader<>();
            try ( DataFileReader<GenericRecord> dataFileReader = new DataFileReader<>(seekByteIn, datumReader) ) {
                Schema schema = dataFileReader.getSchema();
                String str = schema.toString(true);
                System.out.println(str);
                GenericRecord gRecord = null;

                while (dataFileReader.hasNext()) {
                    // Reuse user object by passing it to next(). This saves us from
                    // allocating and garbage collecting many objects for files with
                    // many items.
                    gRecord = dataFileReader.next(gRecord);
                    System.out.println(gRecord);
                    //System.out.println(inTriple.getSubject());
                }
            }
        }

    }

    private static void other() {
//        if ( false )
//        {
//            System.out.println("TermIRI");
//            var bout = new ByteArrayOutputStream();
//
//            DatumWriter<TermIRI> userDatumWriter = new SpecificDatumWriter<>(TermIRI.class);
//            try ( DataFileWriter<TermIRI> dataFileWriter = new DataFileWriter<>(userDatumWriter) ) {
//                dataFileWriter.create(iri.getSchema(), bout);
//                dataFileWriter.append(iri);
//            }
//
//            var seekByteIn = new SeekableByteArrayInput(bout.toByteArray());
//
//            DatumReader<TermIRI> userDatumReader = new SpecificDatumReader<>(TermIRI.class);
//            try ( DataFileReader<TermIRI> dataFileReader = new DataFileReader<>(seekByteIn, userDatumReader) ) {
//                TermIRI inTerm = null;
//                while (dataFileReader.hasNext()) {
//                    // Reuse user object by passing it to next(). This saves us from
//                    // allocating and garbage collecting many objects for files with
//                    // many items.
//                    inTerm = dataFileReader.next();
//
//                    System.out.println(inTerm.getIri() + " :: "+ inTerm.getValue());
//                }
//            }
//            System.out.println();
//        }
//
//        if ( false )
//        {
//            System.out.println("TermLiteral");
//            var bout = new ByteArrayOutputStream();
//
//            DatumWriter<TermLiteral> userDatumWriter = new SpecificDatumWriter<>(TermLiteral.class);
//            try ( DataFileWriter<TermLiteral> dataFileWriter = new DataFileWriter<>(userDatumWriter) ) {
//                dataFileWriter.create(termObjLiteral1.getSchema(), bout);
//                dataFileWriter.append(termObjLiteral1);
//            }
//
//            var seekByteIn = new SeekableByteArrayInput(bout.toByteArray());
//
//            DatumReader<TermLiteral> userDatumReader = new SpecificDatumReader<>(TermLiteral.getClassSchema());
//            try ( DataFileReader<TermLiteral> dataFileReader = new DataFileReader<>(seekByteIn, userDatumReader) ) {
//                TermLiteral inTerm = null;
//                while (dataFileReader.hasNext()) {
//                    // Reuse user object by passing it to next(). This saves us from
//                    // allocating and garbage collecting many objects for files with
//                    // many items.
//                    inTerm = dataFileReader.next();
//
//                    System.out.println(inTerm);
//                }
//            }
//            System.out.println();
//        }
    }

}

