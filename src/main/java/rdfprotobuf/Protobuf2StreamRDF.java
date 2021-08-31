/**
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

import com.google.protobuf.CodedOutputStream;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.graph.Triple ;
import org.apache.jena.riot.protobuf.PB_RDF.*;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.sparql.core.Quad ;

/** Protobuf RDF (wire format) to RDF terms (Jena java objects)
 *
 * @see StreamRDF2Protobuf for the reverse process.
 */

public class Protobuf2StreamRDF implements VisitorStreamRowPRDF {

    /**
     * Parse from a delimited stream (see {@linkplain CodedOutputStream})
     */
    public static void process(InputStream input, StreamRDF output) {
        Protobuf2StreamRDF visitor = new Protobuf2StreamRDF(PrefixMapFactory.create(), output);
        input = PB.ensureBuffered(input);
        RDF_StreamRow.Builder row = RDF_StreamRow.newBuilder();
        output.start();
        try {
            while(true) {
                RDF_StreamRow x = RDF_StreamRow.parseDelimitedFrom(input);
                boolean b = visitor.visit(x);
                if ( !b )
                    return;
            }
        } catch(IOException ex) { IO.exception(ex); }
        finally { output.finish(); }
    }

    public boolean visit(RDF_StreamRow x) {
        if ( x == null )
            return false;
        switch(x.getRowCase()) {
            case BASE :
                this.visit(x.getBase());
                return true;
            case PREFIXDECL :
                this.visit(x.getPrefixDecl());
                return true;
            case QUAD :
                this.visit(x.getQuad());
                return true;
            case TRIPLE :
                this.visit(x.getTriple());
                return true;
            case ROW_NOT_SET :
                throw new InternalErrorException();
        }
        return false;
    }

    // TODO Reuse builders.
    private final StreamRDF dest ;
    private final PrefixMap pmap ;

    public Protobuf2StreamRDF(PrefixMap pmap, StreamRDF stream) {
        this.pmap = pmap ;
        this.dest = stream ;
    }

    @Override
    public void visit(RDF_Triple rt) {
        Triple t = ProtobufConvert.convert(rt, pmap) ;
        dest.triple(t) ;
    }

    @Override
    public void visit(RDF_Quad rq) {
        Quad q = ProtobufConvert.convert(rq, pmap) ;
        dest.quad(q) ;
    }

    @Override
    public void visit(RDF_PrefixDecl prefixDecl) {
        String prefix = prefixDecl.getPrefix() ;
        String iriStr = prefixDecl.getUri() ;
        pmap.add(prefix, iriStr) ;
        dest.prefix(prefix, iriStr) ;
    }

    @Override
    public void visit(RDF_IRI baseDecl) {
        String iriStr = baseDecl.getIri();
        dest.base(iriStr) ;
    }

}
