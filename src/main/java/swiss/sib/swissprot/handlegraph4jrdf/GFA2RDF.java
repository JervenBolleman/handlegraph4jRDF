/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swiss.sib.swissprot.handlegraph4jrdf;

import io.github.vgteam.handlegraph4j.gfa1.GFA1Reader;
import io.github.vgteam.handlegraph4j.gfa1.line.Line;
import io.github.vgteam.handlegraph4j.gfa1.line.LinkLine;
import io.github.vgteam.handlegraph4j.gfa1.line.PathLine;
import io.github.vgteam.handlegraph4j.gfa1.line.PathLine.Step;
import io.github.vgteam.handlegraph4j.gfa1.line.SegmentLine;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import org.eclipse.rdf4j.common.net.ParsedIRI;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.datatypes.XMLDatatypeUtil;
import org.eclipse.rdf4j.model.impl.NumericLiteral;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;

/**
 *
 * @author Jerven Bolleman <jerven.bolleman@sib.swiss>
 */
@Command(name = "gfa1toRdf", mixinStandardHelpOptions = true, version = "gfa1tot2rdf 0.0.1",
        description = "Prints the equivalent RDF for a GFA1 file")
public class GFA2RDF implements Callable<Integer> {

    private static final ValueFactory VF = SimpleValueFactory.getInstance();
    private final IntIntHashMap nodeLengthMapByIntId = new IntIntHashMap();
    private final ObjectIntHashMap<String> nodeLengthMapByByteArrayId = new ObjectIntHashMap<>();

    @Parameters(index = "0", description = "The GFA file to translate to RDF")
    private File inputFile;

    @Parameters(index = "1", description = "The output file to write RDF too")
    private File outputFile;

    @Option(names = {"-b", "--base"}, description = "Base IRI of this graph")
    private String base = "http://example.org/vg/";

    @Option(names = {"-s", "--short"}, description = "try to generate shorter text, and leave out inferable triples")
    boolean preCompress;

    @Option(names = {"-e", "--extra"}, description = "try to all triples possible")
    boolean extra;

    @Override
    public Integer call() throws Exception { // your business logic goes here...
        ParsedIRI baseIRI = new ParsedIRI(base);
        try ( OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            try ( Stream<String> s = Files.lines(inputFile.toPath(), StandardCharsets.US_ASCII)) {
                writeConvertedToOutputStream(out, baseIRI, s);
            }
        }
        return 0;
    }

    void writeConvertedToOutputStream(final OutputStream out, ParsedIRI baseIRI, Stream<String> s) throws RDFHandlerException {
        TurtleWriter tw = new PrefixedURITurtleWriter(out, baseIRI);
        tw.set(BasicWriterSettings.PRETTY_PRINT, false);
        tw.startRDF();
        String nodePrefix;
        if (preCompress) {
            tw.handleNamespace("r", RDF.NAMESPACE);
            tw.handleNamespace("", VG.NAMESPACE);
            nodePrefix = "n";
            tw.handleNamespace("S", VG.Step.stringValue());
            tw.handleNamespace("N", VG.Node.stringValue());
            tw.handleNamespace("rv", RDF.VALUE.stringValue());
            tw.handleNamespace("ff", VG.linksForwardToForward.stringValue());
            tw.handleNamespace("fr", VG.linksForwardToReverse.stringValue());
            tw.handleNamespace("rf", VG.linksReverseToForward.stringValue());
            tw.handleNamespace("rr", VG.linksReverseToReverse.stringValue());
            tw.handleNamespace("l", VG.links.stringValue());
            tw.handleNamespace("sp", VG.path.stringValue());
            tw.handleNamespace("sr", VG.rank.stringValue());
            tw.handleNamespace("sn", VG.node.stringValue());
            tw.handleNamespace("f", FALDO.NAMESPACE);
            tw.handleNamespace("fep", FALDO.ExactPosition.stringValue());
            tw.handleNamespace("p", FALDO.position.stringValue());
        } else {
            tw.handleNamespace(RDF.PREFIX, RDF.NAMESPACE);
            tw.handleNamespace(VG.PREFIX, VG.NAMESPACE);
            nodePrefix = "node";
            tw.handleNamespace(FALDO.PREFIX, FALDO.NAMESPACE);
        }
        tw.handleNamespace(nodePrefix, base + "node/");
        Iterator<String> si = s.iterator();
        convert(si, tw, nodePrefix);

        tw.endRDF();
    }

    public void convert(Iterator<String> si, TurtleWriter tw, String nodePrefix) {
        int pathCounter = 0;
        GFA1Reader gfA1Reader = new GFA1Reader(si);
        while (gfA1Reader.hasNext()) {
            Line line = gfA1Reader.next();
            pathCounter = convertLineToRdf(line, tw, pathCounter, nodePrefix);
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new GFA2RDF()).execute(args);
        System.exit(exitCode);

    }

    private int convertLineToRdf(Line line, RDFWriter tw, int pathCounter, String nodePrefix) {
        switch (line.getCode()) {
            case PathLine.CODE:
                return convertPathLineToRdf((PathLine) line, tw, pathCounter, nodePrefix);
            case SegmentLine.CODE:
                convertSegmentLineToRdf((SegmentLine) line, tw, nodePrefix);
                return pathCounter;
            case LinkLine.CODE:
                convertLinkLineToRdf((LinkLine) line, tw, nodePrefix);
                return pathCounter;
            default:
                return pathCounter;
        }
    }

    private int convertPathLineToRdf(PathLine pathLine, RDFWriter writer, int pathCounter, String nodePrefix) {
        String nameAsString = pathLine.getNameAsString();
        IRI pathIRI;
        String pathPrefix;
        String pathStepPrefix;
        String pathPositionPrefix;
        if (preCompress) {
            pathPrefix = "pn";
            pathStepPrefix = "ps";
            pathPositionPrefix = "pp";
        } else {
            pathPrefix = "path";
            pathStepPrefix = "pathstep";
            pathPositionPrefix = "pathposition" ;
        }
        if (nameAsString.startsWith("http://") || nameAsString.startsWith("ftp://") || nameAsString.startsWith("https://")) {
            pathIRI = new PrefixedIRI(pathPrefix, nameAsString, "");
        } else {
            pathIRI = new PrefixedIRI(pathPrefix, base + "path/", nameAsString);
        }

        String pathStepBase = pathIRI.stringValue() + "/step/";
        String pathPositionBase = pathIRI.stringValue() + "/position/";

        writer.handleNamespace(pathPrefix, pathIRI.stringValue());
        writer.handleNamespace(pathStepPrefix, pathStepBase);
        if (extra) {
            writer.handleNamespace(pathPositionPrefix, pathPositionBase);
        }
        writer.handleStatement(VF.createStatement(pathIRI, RDF.TYPE, VG.Path));
        Iterator<Step> steps = pathLine.steps();
        int begin = 1; // We start at position 1.
        while (steps.hasNext()) {
            Step step = steps.next();
            writeStep(step, pathStepBase, writer, pathIRI, begin, pathPositionBase, nodePrefix, pathPrefix, pathStepPrefix, pathPositionPrefix);
        }
        if (writer instanceof PrefixedURITurtleWriter) {
            PrefixedURITurtleWriter tw = (PrefixedURITurtleWriter) writer;
            tw.unsetNamespace(pathIRI.stringValue());
            tw.unsetNamespace(pathStepBase);
            tw.unsetNamespace(pathPositionBase);
        }
        return pathCounter++;
    }

    void writeStep(Step step, String pathStepBase, RDFWriter tw, IRI pathIRI, int begin, String pathPositionBase, String nodePrefix, String pathPrefix, String pathStepPrefix, String pathPositionPrefix) throws RDFHandlerException {
        long rank = step.rank();
        IRI stepIRI = new PrefixedIRI(pathStepPrefix, pathStepBase, Long.toString(rank));
        if (!preCompress) {
            tw.handleStatement(VF.createStatement(stepIRI, RDF.TYPE, VG.Step));
            tw.handleStatement(VF.createStatement(stepIRI, RDF.TYPE, FALDO.Region));
        }
        tw.handleStatement(VF.createStatement(stepIRI, VG.path, pathIRI));
        if (rank < Integer.MAX_VALUE) {
            tw.handleStatement(VF.createStatement(stepIRI, VG.rank, VF.createLiteral((int) rank)));
        } else {
            tw.handleStatement(VF.createStatement(stepIRI, VG.rank, VF.createLiteral(rank)));
        }
        tw.handleStatement(VF.createStatement(stepIRI, VG.node, createNodeId(nodePrefix, new String(step.nodeId(), US_ASCII))));
        if (extra) {
            int end = writePositions(begin, step, pathPositionBase, tw, stepIRI, pathPositionPrefix);
            begin = end;
        }
    }

    int writePositions(int begin, Step step, String pathPositionBase, RDFWriter tw, IRI stepIRI, String pathPositionPrefix) throws RDFHandlerException {
        int end = begin + getNodeLengthOfStep(step);
        IRI beginIri = new PrefixedIRI(pathPositionPrefix, pathPositionBase, String.valueOf(begin));
        IRI endIri = new PrefixedIRI(pathPositionPrefix, pathPositionBase, String.valueOf(end));
        tw.handleStatement(VF.createStatement(stepIRI, FALDO.begin, beginIri));
        tw.handleStatement(VF.createStatement(stepIRI, FALDO.end, endIri));
        if (!preCompress) {
            tw.handleStatement(VF.createStatement(beginIri, RDF.TYPE, FALDO.Position));
        }
        tw.handleStatement(VF.createStatement(beginIri, RDF.TYPE, FALDO.ExactPosition));
        tw.handleStatement(VF.createStatement(beginIri, FALDO.position, VF.createLiteral(begin)));
        if (!preCompress) {
            tw.handleStatement(VF.createStatement(endIri, RDF.TYPE, FALDO.Position));
        }
        tw.handleStatement(VF.createStatement(endIri, RDF.TYPE, FALDO.ExactPosition));
        tw.handleStatement(VF.createStatement(endIri, FALDO.position, VF.createLiteral(end)));
        return end;
    }

    private void convertSegmentLineToRdf(SegmentLine segmentLine, RDFWriter tw, String nodePrefix) {
        String name = segmentLine.getNameAsString();
        IRI nodeIRI = createNodeId(nodePrefix, name);
        tw.handleStatement(VF.createStatement(nodeIRI, RDF.TYPE, VG.Node));
        if (extra) {
            try {
                int id = Integer.parseInt(name);
                nodeLengthMapByIntId.put(id, segmentLine.getSequence().length());
            } catch (NumberFormatException e) {
                nodeLengthMapByByteArrayId.put(name, segmentLine.getSequence().length());
            }
        }
        tw.handleStatement(VF.createStatement(nodeIRI, RDF.VALUE, VF.createLiteral(segmentLine.getSequence().asString())));
    }

    private void convertLinkLineToRdf(LinkLine linkLine, RDFWriter tw, String nodePrefix) {
        IRI fromNodeIRI = createNodeId(nodePrefix, linkLine.getFromNameAsString());
        IRI toNodeIRI = createNodeId(nodePrefix, linkLine.getToNameAsString());
        if (linkLine.isReverseComplimentOfFrom()) {
            if (linkLine.isReverseComplimentOfTo()) {
                tw.handleStatement(VF.createStatement(fromNodeIRI, VG.linksReverseToReverse, toNodeIRI));
            } else {
                tw.handleStatement(VF.createStatement(fromNodeIRI, VG.linksReverseToForward, toNodeIRI));
            }
        } else {
            if (linkLine.isReverseComplimentOfTo()) {
                tw.handleStatement(VF.createStatement(fromNodeIRI, VG.linksForwardToReverse, toNodeIRI));
            } else {
                tw.handleStatement(VF.createStatement(fromNodeIRI, VG.linksForwardToForward, toNodeIRI));
            }
        }
    }

    IRI createNodeId(String prefix, String nodeId) {
        return new PrefixedIRI(prefix, base + "node/", nodeId);
    }

    private int getNodeLengthOfStep(Step step) {
        if (step.nodeHasLongId()) {
            return nodeLengthMapByIntId.get((int) step.nodeLongId());
        } else {
            return nodeLengthMapByByteArrayId.get(step.nodeId());
        }

    }

    private class PrefixedURITurtleWriter extends TurtleWriter {

        public PrefixedURITurtleWriter(OutputStream out, ParsedIRI piri) {
            super(out, piri);
        }

        @Override
        protected void writeURI(IRI res) throws IOException {

            if (res instanceof PrefixedIRI) {
                PrefixedIRI pi = (PrefixedIRI) res;
                writer.write(pi.prefix + ':' + pi.localName);
            } else {
                String prefix = namespaceTable.get(res.stringValue());
                if (prefix != null) {
                    writer.write(prefix);
                    writer.write(":");
                    return;
                }
                super.writeURI(res);
            }
        }

        @Override
        protected void writeLiteral(Literal res) throws IOException {
            if (res instanceof NumericLiteral) {
                String normalized = XMLDatatypeUtil.normalize(res.getLabel(), res.getDatatype());
                switch (normalized) {
                    case XMLDatatypeUtil.POSITIVE_INFINITY:
                    case XMLDatatypeUtil.NEGATIVE_INFINITY:
                    case XMLDatatypeUtil.NaN:
                        super.writeLiteral(res);
                        break;
                    default:
                        writer.write(normalized);
                        return;
                }
            }
            super.writeLiteral(res);

        }

        private void unsetNamespace(String iri) {
            namespaceTable.remove(iri);
        }
    }

    private class PrefixedIRI extends SimpleIRI {

        private final String prefix;
        private final String namespace;
        private final String localName;

        public PrefixedIRI(String prefix, String namespace, String localName) {
            this.prefix = prefix;
            this.namespace = namespace;
            this.localName = localName;
        }

        @Override
        public String toString() {
            return stringValue();
        }

        @Override
        public String stringValue() {
            return namespace + localName;
        }

        @Override
        public String getNamespace() {
            return namespace;
        }

        @Override
        public String getLocalName() {
            return localName;
        }

        // Implements IRI.equals(Object)
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o instanceof IRI) {

                String a = toString();
                String b = o.toString();

                return a.equals(b);
            }

            return false;
        }

        // Implements IRI.hashCode()
        @Override
        public int hashCode() {
            return stringValue().hashCode();
        }
    }
}
