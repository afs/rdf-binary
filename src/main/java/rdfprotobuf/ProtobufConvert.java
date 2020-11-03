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


import java.math.BigInteger ;

import org.apache.jena.JenaRuntime ;
import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.datatypes.xsd.impl.RDFLangString ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.Node_Triple;
import org.apache.jena.graph.Triple ;
import org.apache.jena.riot.protobuf.PB_RDF.*;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.PrefixMapFactory ;
//import org.apache.jena.riot.thrift.StreamRDF2Thrift;
//import org.apache.jena.riot.thrift.Thrift2StreamRDF;
//import org.apache.jena.riot.thrift.RiotThriftException;
//import org.apache.jena.riot.thrift.StreamRDF2Thrift;
//import org.apache.jena.riot.thrift.TRDF;
//import org.apache.jena.riot.thrift.Thrift2StreamRDF;
//import org.apache.jena.riot.thrift.wire.* ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.core.Var ;

/** Convert to and from Protobuf wire objects.
 * See {@link StreamRDF2Protobuf} and {@link Protobuf2StreamRDF}
 * for ways to convert as streams (they recycle intermediate objects).
 * @see StreamRDF2Protobuf
 * @see Protobuf2StreamRDF
 */
public class ProtobufConvert
{
    static final BigInteger MAX_I = BigInteger.valueOf(Long.MAX_VALUE) ;
    static final BigInteger MIN_I = BigInteger.valueOf(Long.MIN_VALUE) ;

    /** Attempt to encode a node by value (integer, decimal, double) into an RDF_term.
     * @param node
     * @param term
     * @return RDF_Term or null if not a value.
     */
    private static RDF_Term toProtobufValue(Node node, RDF_Term.Builder term) {
        if ( ! node.isLiteral() )
            return null ;

        // Value cases : Integer, Double, Decimal
        String lex = node.getLiteralLexicalForm() ;
        RDFDatatype rdt = node.getLiteralDatatype() ;

        if ( rdt == null )
            return null ;

        return null;

//        if ( rdt.equals(XSDDatatype.XSDdecimal) ) {
//            if ( rdt.isValid(lex)) {
//                BigDecimal decimal = new BigDecimal(lex.trim()) ;
//                int scale = decimal.scale() ;
//                BigInteger bigInt = decimal.unscaledValue() ;
//                if ( bigInt.compareTo(MAX_I) <= 0 && bigInt.compareTo(MIN_I) >= 0 ) {
//                    // This check makes sure that bigInt.longValue() is safe
//                    RDF_Decimal d = RDF_Decimal.newBuilder().setValue(bigInt.longValue()).setScale(scale).build();
//                    RDF_Literal = RDF_Literal.newBuilder().setDecimal(d);
//                    term.setValDecimal(d) ;
//                    return term.build();
//                }
//            }
//        } else if (
//            rdt.equals(XSDDatatype.XSDinteger) ||
//            rdt.equals(XSDDatatype.XSDlong) ||
//            rdt.equals(XSDDatatype.XSDint) ||
//            rdt.equals(XSDDatatype.XSDshort) ||
//            rdt.equals(XSDDatatype.XSDbyte)
//            ) {
//            // and 4 unsigned equivalents
//            // and positive, negative, nonPostive nonNegativeInteger
//
//            // Conservative - no derived types.
//            if ( rdt.isValid(lex)) {
//                try {
//                    long v = ((Number)node.getLiteralValue()).longValue() ;
//                    term.setValInteger(v) ;
//                    return true ;
//                }
//                // Out of range for the type, not a long etc etc.
//                catch (Throwable ex) { }
//            }
//        } else if ( rdt.equals(XSDDatatype.XSDdouble) ) {
//            // XSDfloat??
//            if ( rdt.isValid(lex)) {
//                try {
//                    double v = ((Double)node.getLiteralValue()).doubleValue() ;
//                    term.setValDouble(v) ;
//                    return true ;
//                }
//                // Out of range for the type, ...
//                catch (Throwable ex) { }
//            }
//        }
//        return false ;
    }

    /**
     * Encode a {@link Node} into an {@link RDF_Term},
     * using values (integer, decimal, double) if possible.
     */
    public static RDF_Term toProtobuf(Node node, RDF_Term.Builder term) {
        return toThrift(node, emptyPrefixMap, term, true);
    }

    /**
     * Encode a {@link Node} into an {@link RDF_Term}. Control whether to use values
     * (integer, decimal, double) if possible.
     */
    public static RDF_Term toThrift(Node node, RDF_Term.Builder term, boolean allowValues) {
        return toThrift(node, emptyPrefixMap, term, allowValues);
    }

    private static RDF_UNDEF rdfUNDEF = RDF_UNDEF.newBuilder().build();
    private static RDF_Term UNDEF = RDF_Term.newBuilder().setUndefined(rdfUNDEF).build();

    private static RDF_ANY rdfANY = RDF_ANY.newBuilder().build();
    private static RDF_Term ANY = RDF_Term.newBuilder().setAny(rdfANY).build();

    /** Encode a {@link Node} into an {@link RDF_Term} */
    public static RDF_Term toThrift(Node node, PrefixMap pmap, RDF_Term.Builder termBuilder, boolean allowValues) {
        if ( node == null)
            return UNDEF;

        if ( node.isURI() ) {
            RDF_PrefixName prefixName = abbrev(node.getURI(), pmap) ;
            if ( prefixName != null ) {
                termBuilder.setPrefixName(prefixName) ;
                return termBuilder.build();
            }
            RDF_IRI iri = RDF_IRI.newBuilder().setIri(node.getURI()).build() ;
            return termBuilder.setIri(iri).build();
        }

        if ( node.isBlank() ) {
            RDF_BNode b = RDF_BNode.newBuilder().setLabel(node.getBlankNodeLabel()).build();
            return termBuilder.setBnode(b).build();
        }

        if ( node.isLiteral() ) {
            // Value cases : Integer, Double, Decimal
            if ( allowValues) {
                RDF_Term term = toProtobufValue(node, termBuilder) ;
                if ( term != null )
                    return termBuilder.build();
            }

            String lex = node.getLiteralLexicalForm() ;
            String dt = node.getLiteralDatatypeURI() ;
            String lang = node.getLiteralLanguage() ;

            // General encoding.

            RDF_Literal.Builder literal = RDF_Literal.newBuilder();
            literal.setLex(lex);

            if ( JenaRuntime.isRDF11 ) {
                if ( node.getLiteralDatatype().equals(XSDDatatype.XSDstring) ||
                     node.getLiteralDatatype().equals(RDFLangString.rdfLangString) )
                    dt = null ;
            }

            if ( dt != null ) {
                RDF_PrefixName dtPrefixName = abbrev(dt, pmap) ;
                if ( dtPrefixName != null )
                    literal.setDtPrefix(dtPrefixName) ;
                else
                    literal.setDatatype(dt) ;
            }
            if ( lang != null && ! lang.isEmpty() )
                literal.setLangtag(lang) ;
            termBuilder.setLiteral(literal) ;
            return termBuilder.build();
        }

        if ( node.isVariable() ) {
            RDF_VAR var = RDF_VAR.newBuilder().setName(node.getName()).build();
            return termBuilder.setVariable(var).build();
        }

        if ( node.isNodeTriple() ) {
            Triple triple = Node_Triple.triple(node);

            RDF_Term sTerm = toThrift(triple.getSubject(), pmap, termBuilder, allowValues);
            termBuilder.clear();

            RDF_Term pTerm = toThrift(triple.getPredicate(), pmap, termBuilder, allowValues);
            termBuilder.clear();

            RDF_Term oTerm = toThrift(triple.getObject(), pmap, termBuilder, allowValues);
            termBuilder.clear();

            RDF_Triple tripleTerm = RDF_Triple.newBuilder().setS(sTerm).setP(pTerm).setO(oTerm).build();
            termBuilder.setTripleTerm(tripleTerm);
            return termBuilder.build();
        }

        if ( Node.ANY.equals(node))
            return ANY;

        throw new RiotProtobufException("Node conversion not supported: "+node) ;
    }

    private static final PrefixMap emptyPrefixMap = PrefixMapFactory.emptyPrefixMap() ;

    /** Build a {@link Node} from an {@link RDF_Term}. */
    public static Node convert(RDF_Term term) {
        return convert(term, null) ;
    }

    /**
     * Build a {@link Node} from an {@link RDF_Term} using a prefix map which must agree
     * with the map used to create the {@code RDF_Term} in the first place.
     */
    public static Node convert(RDF_Term term, PrefixMap pmap) {
        if ( term.hasPrefixName() ) {
            String x = expand(term.getPrefixName(), pmap) ;
            if ( x != null )
                return NodeFactory.createURI(x) ;
            throw new RiotProtobufException("Failed to expand "+term) ;
            //Log.warn(BinRDF.class, "Failed to expand "+term) ;
            //return NodeFactory.createURI(prefix+":"+localname) ;
        }

        if ( term.hasIri() )
            return NodeFactory.createURI(term.getIri().getIri()) ;

        if ( term.hasBnode() )
            return NodeFactory.createBlankNode(term.getBnode().getLabel()) ;

        if ( term.hasLiteral() ) {
            RDF_Literal lit = term.getLiteral() ;
            String lex = lit.getLex() ;
            String dtString = null ;
            String lang = null;
            switch ( lit.getLangOrDatatypeCase() ) {
                case DATATYPE :
                    dtString = lit.getDatatype();
                    break;
                case DTPREFIX : {
                    String x = expand(lit.getDtPrefix(), pmap);
                    if ( x == null )
                        throw new RiotProtobufException("Failed to expand datatype prefix name: "+lit.getDtPrefix()) ;
                    dtString = x;
                    break;
                }
                case LANGORDATATYPE_NOT_SET :
                    throw new RiotProtobufException("Missing datatype/lang : "+lit) ;
                case LANGTAG :
                    lang = lit.getLangtag();
                    break;
                default :
                    throw new RiotProtobufException("Unrecognized literal kind: "+lit) ;
            }

            if ( lang != null )
                return NodeFactory.createLiteral(lex, lang) ;
            RDFDatatype dt = NodeFactory.getType(dtString) ;
            return NodeFactory.createLiteral(lex, dt) ;
        }

//        if ( term.hasValInteger() ) {
//            long x = term.getValInteger() ;
//            String lex = Long.toString(x, 10) ;
//            RDFDatatype dt = XSDDatatype.XSDinteger ;
//            return NodeFactory.createLiteral(lex, dt) ;
//        }
//
//        if ( term.hasValDouble() ) {
//            double x = term.getValDouble() ;
//            String lex = Double.toString(x) ;
//            RDFDatatype dt = XSDDatatype.XSDdouble ;
//            return NodeFactory.createLiteral(lex, dt) ;
//        }
//
//        if ( term.hasValDecimal() ) {
//            long value = term.getValDecimal().getValue() ;
//            int scale =  term.getValDecimal().getScale() ;
//            BigDecimal d =  BigDecimal.valueOf(value, scale) ;
//            String lex = d.toPlainString() ;
//            RDFDatatype dt = XSDDatatype.XSDdecimal ;
//            return NodeFactory.createLiteral(lex, dt) ;
//        }

        if ( term.hasTripleTerm() ) {
            RDF_Triple rt = term.getTripleTerm();
            Triple t = convert(rt, pmap);
            return NodeFactory.createTripleNode(t);
        }

        if ( term.hasVariable() )
            return Var.alloc(term.getVariable().getName()) ;

        if ( term.hasAny() )
            return Node.ANY ;

        if ( term.hasUndefined() )
            return null;

        throw new RiotProtobufException("No conversion to a Node: "+term.toString()) ;
    }

    private static String expand(RDF_PrefixName prefixName, PrefixMap pmap) {
        if ( pmap == null )
            return null ;

        String prefix = prefixName.getPrefix() ;
        String localname  = prefixName.getLocalName() ;
        String x = pmap.expand(prefix, localname) ;
        if ( x == null )
            throw new RiotProtobufException("Failed to expand "+prefixName) ;
        return x ;
    }


    public static RDF_Term convert(Node node, boolean allowValues) {
        return convert(node, null, allowValues) ;
    }

    public static RDF_Term convert(Node node, PrefixMap pmap, boolean allowValues) {
        RDF_Term.Builder n = RDF_Term.newBuilder();
        return toThrift(node, pmap, n, allowValues) ;
    }

    /** Produce a {@link RDF_PrefixName} is possible. */
    private static RDF_PrefixName abbrev(String uriStr, PrefixMap pmap) {
        if ( pmap == null )
            return null ;
        Pair<String, String> p = pmap.abbrev(uriStr) ;
        if ( p == null )
            return null ;
        return RDF_PrefixName.newBuilder().setPrefix(p.getLeft()).setLocalName(p.getRight()).build();
    }

    public static Triple convert(RDF_Triple triple) {
        return convert(triple, null) ;
    }

    public static Triple convert(RDF_Triple rt, PrefixMap pmap) {
        Node s = convert(rt.getS(), pmap) ;
        Node p = convert(rt.getP(), pmap) ;
        Node o = convert(rt.getO(), pmap) ;
        return Triple.create(s, p, o) ;
    }

    public static RDF_Triple convert(Triple triple, boolean allowValues) {
        return convert(triple, null, allowValues) ;
    }

    public static RDF_Triple convert(Triple triple, PrefixMap pmap, boolean allowValues) {
        RDF_Triple.Builder t = RDF_Triple.newBuilder();
        RDF_Term s = convert(triple.getSubject(), pmap, allowValues) ;
        RDF_Term p = convert(triple.getPredicate(), pmap, allowValues) ;
        RDF_Term o = convert(triple.getObject(), pmap, allowValues) ;
        t.setS(s) ;
        t.setP(p) ;
        t.setO(o) ;
        return t.build() ;
    }

    public static Quad convert(RDF_Quad quad) {
        return convert(quad, null) ;
    }

    public static Quad convert(RDF_Quad rq, PrefixMap pmap) {
        Node g = (rq.hasG() ? convert(rq.getG(), pmap) : null ) ;
        Node s = convert(rq.getS(), pmap) ;
        Node p = convert(rq.getP(), pmap) ;
        Node o = convert(rq.getO(), pmap) ;
        return Quad.create(g, s, p, o) ;
    }

    public static RDF_Quad convert(Quad quad, boolean allowValues) {
        return convert(quad, null, allowValues) ;
    }

    public static RDF_Quad convert(Quad quad, PrefixMap pmap, boolean allowValues) {
        RDF_Quad.Builder q = RDF_Quad.newBuilder();
        RDF_Term g = null ;
        if ( quad.getGraph() != null )
            g = convert(quad.getGraph(), pmap, allowValues) ;
        RDF_Term s = convert(quad.getSubject(), pmap, allowValues) ;
        RDF_Term p = convert(quad.getPredicate(), pmap, allowValues) ;
        RDF_Term o = convert(quad.getObject(), pmap, allowValues) ;
        if ( g != null )
            q.setG(g) ;
        q.setS(s) ;
        q.setP(p) ;
        q.setO(o) ;
        return q.build() ;
    }

//    /**
//     * Serialize the {@link RDF_Term} into a byte array.
//     * <p>
//     * Where possible, to is better to serialize into a stream, directly using {@code term.write(TProtocol)}.
//     */
//    public static byte[] termToBytes(RDF_Term term) {
//        TSerializer serializer = new TSerializer(new TCompactProtocol.Factory());
//        try {
//            return serializer.serialize(term);
//        }
//        catch (TException e) {
//            throw new RiotProtobufException(e);
//        }
//    }
//
//    /**
//     * Deserialize from a byte array into an {@link RDF_Term}.
//     * <p>
//     * Where possible, to is better to deserialize from a stream, directly using {@code term.read(TProtocol)}.
//     */
//    public static RDF_Term termFromBytes(byte[] bytes) {
//        RDF_Term term = new RDF_Term();
//        termFromBytes(term, bytes);
//        return term;
//    }
//
//    /**
//     * Deserialize from a byte array into an {@link RDF_Term}.
//     * <p>
//     * Where possible, to is better to deserialize from a stream, directly using {@code term.read(TProtocol)}.
//     */
//    public static void termFromBytes(RDF_Term term, byte[] bytes) {
//        TDeserializer deserializer = new TDeserializer(new TCompactProtocol.Factory());
//        try {
//            deserializer.deserialize(term, bytes);
//        }
//        catch (TException e) { throw new RiotProtobufException(e); }
//    }

    // RDF_Tuple => RDF_row (for result sets) or List<RDFTerm>

//    public static Tuple<Node> convert(RDF_Tuple row) {
//        return convert(row, null) ;
//    }
//
//    public static Tuple<Node> convert(RDF_Tuple row, PrefixMap pmap) {
//        List<RDF_Term> terms = row.getTerms() ;
//        Node[] tuple = new Node[terms.size()] ;
//        int idx = 0 ;
//        for ( RDF_Term rt : terms ) {
//            tuple[idx] = convert(rt, pmap) ;
//            idx ++ ;
//        }

//        return Tuple.create(tuple) ;
//    }
//
//    public static RDF_Tuple convert(Tuple<Node> tuple) {
//        return convert(tuple, null) ;
//    }
//
//    public static RDF_Tuple convert(Tuple<Node> tuple, PrefixMap pmap) {
//        RDF_Tuple rTuple = new RDF_Tuple() ;
//        for ( Node n : tuple ) {
//            RDF_Term rt = convert(n, pmap) ;
//            rTuple.addToTerms(rt) ;
//        }
//        return rTuple ;
//    }
}
