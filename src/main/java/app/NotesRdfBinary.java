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

//import org.apache.jena.riot.system.IRIResolver;

public class NotesRdfBinary {
    // Graph as list triples (block, delimited)
    // Graph as StreamRDF (items delimited)
    // StreamRDF as block

    // ****
    // [x] ?? Refactor PBinRDF into two : blk and delimited.
    // [ ] Own "deblocker" (lib has two objects, LimitedInputStream and CodedInputStream per row)
    //     Avoid LimitedInputStream
    // One CodedInputStream over BlockInputStream which has .move() which reads on
    // varint and sets the limit detection.

    // [x] Setup, Lang and registration.
    // [x] TestProtobufSetup
    //     Update io/rdf-binary.html and javadoc
    // [x] License for PB_RDF
    // [x] Tests of readers and writers.

    // [x] Check buffering

    // Licenses:
    // apache-jena
    // apache-jena-fuseki
    // Combined jars: jena-fuseki-server, jena-fuseki-fulljar, jena-fuseki-war, jena-fuseki-docker
}
