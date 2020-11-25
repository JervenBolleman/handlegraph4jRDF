/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sib.swiss.swissprot.handlegraph4jrdf;

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
        if (preCompress) {
            tw.handleNamespace("r", RDF.NAMESPACE);
            tw.handleNamespace("", VG.NAMESPACE);
            tw.handleNamespace("n", base + "node/");
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
            tw.handleNamespace("node", base + "node/");
            tw.handleNamespace(FALDO.PREFIX, FALDO.NAMESPACE);
        }
        Iterator<String> si = s.iterator();
        convert(si, tw);

        tw.endRDF();
    }

    public void convert(Iterator<String> si, TurtleWriter tw) {
        int pathCounter = 0;
        GFA1Reader gfA1Reader = new GFA1Reader(si);
        while (gfA1Reader.hasNext()) {
            Line line = gfA1Reader.next();
            pathCounter = convertLineToRdf(line, tw, pathCounter);
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new GFA2RDF()).execute(args);
        System.exit(exitCode);

    }

    private int convertLineToRdf(Line line, RDFWriter tw, int pathCounter) {
        switch (line.getCode()) {
            case PathLine.CODE:
                return convertPathLineToRdf((PathLine) line, tw, pathCounter, preCompress);
            case SegmentLine.CODE:
                convertSegmentLineToRdf((SegmentLine) line, tw);
                return pathCounter;
            case LinkLine.CODE:
                convertLinkLineToRdf((LinkLine) line, tw);
                return pathCounter;
            default:
                return pathCounter;
        }
    }

    private int convertPathLineToRdf(PathLine pathLine, RDFWriter writer, int pathCounter, boolean preCompress) {
        String nameAsString = pathLine.getNameAsString();
        IRI pathIRI;
        String pathName = Integer.toString(pathCounter);
        if (nameAsString.startsWith("http://") || nameAsString.startsWith("ftp://") || nameAsString.startsWith("https://")) {
            pathIRI = VF.createIRI(nameAsString);
        } else {
            pathIRI = VF.createIRI(base + "path/", nameAsString);
            pathName = nameAsString;
        }

        String pathStepBase = pathIRI.stringValue() + "/step/";
        String pathPositionBase = pathIRI.stringValue() + "/position/";
        setPathNamespaces(preCompress, writer, pathName, pathStepBase, pathIRI, pathPositionBase);
        writer.handleStatement(VF.createStatement(pathIRI, RDF.TYPE, VG.Path));
        Iterator<Step> steps = pathLine.steps();
        int begin = 1; // We start at position 1.
        while (steps.hasNext()) {
            Step step = steps.next();
            writeStep(step, pathStepBase, preCompress, writer, pathIRI, begin, pathPositionBase);
        }
        if (writer instanceof PrefixedURITurtleWriter) {
            PrefixedURITurtleWriter tw = (PrefixedURITurtleWriter) writer;
            tw.unsetNamespace(pathIRI.stringValue());
            tw.unsetNamespace(pathStepBase);
            tw.unsetNamespace(pathPositionBase);
        }
        return pathCounter++;
    }

    void setPathNamespaces(boolean preCompress1, RDFWriter tw, String pathName, String pathStepBase, IRI pathIRI, String pathPositionBase) throws RDFHandlerException {
        if (preCompress1) {
            tw.handleNamespace("pn" + pathName, pathIRI.stringValue());
            tw.handleNamespace("ps" + pathName, pathStepBase);
            if (extra) {
                tw.handleNamespace("pp" + pathName, pathPositionBase);
            }
        } else {
            tw.handleNamespace("path" + pathName, pathStepBase);
            if (extra) {
                tw.handleNamespace("pathposition" + pathName, pathPositionBase);
            }
        }
    }

    void writeStep(Step step, String pathStepBase, boolean preCompress1, RDFWriter tw, IRI pathIRI, int begin, String pathPositionBase) throws RDFHandlerException {
        long rank = step.rank();
        IRI stepIRI = VF.createIRI(pathStepBase, Long.toString(rank));
        if (!preCompress1) {
            tw.handleStatement(VF.createStatement(stepIRI, RDF.TYPE, VG.Step));
            tw.handleStatement(VF.createStatement(stepIRI, RDF.TYPE, FALDO.Region));
        }
        tw.handleStatement(VF.createStatement(stepIRI, VG.path, pathIRI));
        if (rank < Integer.MAX_VALUE) {
            tw.handleStatement(VF.createStatement(stepIRI, VG.rank, VF.createLiteral((int) rank)));
        } else {
            tw.handleStatement(VF.createStatement(stepIRI, VG.rank, VF.createLiteral(rank)));
        }
        tw.handleStatement(VF.createStatement(stepIRI, VG.node, createNodeId(new String(step.nodeId(), US_ASCII))));
        if (extra) {
            int end = writePositions(begin, step, pathPositionBase, tw, stepIRI, preCompress1);
            begin = end;
        }
    }

    int writePositions(int begin, Step step, String pathPositionBase, RDFWriter tw, IRI stepIRI, boolean preCompress1) throws RDFHandlerException {
        int end = begin + getNodeLengthOfStep(step);
        IRI beginIri = VF.createIRI(pathPositionBase, String.valueOf(begin));
        IRI endIri = VF.createIRI(pathPositionBase, String.valueOf(end));
        tw.handleStatement(VF.createStatement(stepIRI, FALDO.begin, beginIri));
        tw.handleStatement(VF.createStatement(stepIRI, FALDO.end, endIri));
        if (!preCompress1) {
            tw.handleStatement(VF.createStatement(beginIri, RDF.TYPE, FALDO.Position));
        }
        tw.handleStatement(VF.createStatement(beginIri, RDF.TYPE, FALDO.ExactPosition));
        tw.handleStatement(VF.createStatement(beginIri, FALDO.position, VF.createLiteral(begin)));
        if (!preCompress1) {
            tw.handleStatement(VF.createStatement(endIri, RDF.TYPE, FALDO.Position));
        }
        tw.handleStatement(VF.createStatement(endIri, RDF.TYPE, FALDO.ExactPosition));
        tw.handleStatement(VF.createStatement(endIri, FALDO.position, VF.createLiteral(end)));
        return end;
    }

    private void convertSegmentLineToRdf(SegmentLine segmentLine, RDFWriter tw) {
        String name = segmentLine.getNameAsString();
        IRI nodeIRI = VF.createIRI(base + "node/", name);
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

    private void convertLinkLineToRdf(LinkLine linkLine, RDFWriter tw) {
        IRI fromNodeIRI = createNodeId(linkLine.getFromNameAsString());
        IRI toNodeIRI = createNodeId(linkLine.getToNameAsString());
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

    IRI createNodeId(String nodeId) {
        return VF.createIRI(base + "node/", nodeId);
    }

    private int getNodeLengthOfStep(Step step) {
        if (step.nodeHasIntId()) {
            return nodeLengthMapByIntId.get(step.nodeIntId());
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

            String prefix = namespaceTable.get(res.stringValue());
            if (prefix != null) {
                writer.write(prefix);
                writer.write(":");
                return;
            }
            super.writeURI(res);
        }

        @Override
        protected void writeLiteral(Literal res) throws IOException {
            if (res instanceof NumericLiteral) {
                String normalized = XMLDatatypeUtil.normalize(res.getLabel(), res.getDatatype());
                switch (normalized)
                {
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
}
