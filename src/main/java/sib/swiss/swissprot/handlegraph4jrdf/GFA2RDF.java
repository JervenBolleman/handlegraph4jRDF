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
@Command(name = "gfa2rdf", mixinStandardHelpOptions = true, version = "gfa2rdf 0.0.1",
        description = "Prints the checksum (MD5 by default) of a file to STDOUT.")
public class GFA2RDF implements Callable<Integer> {

    private static final ValueFactory VF = SimpleValueFactory.getInstance();

    @Parameters(index = "0", description = "The GFA file to translate to RDF")
    private File inputFile;

    @Parameters(index = "1", description = "The output file to write RDF too")
    private File outputFile;

    @Option(names = {"-b", "--base"}, description = "Base IRI of this graph")
    private String base = "http://example.org/vg/";

    @Option(names = "-s", description = "try to generate shorter text, and leave out inferable triples")
    boolean preCompress;

    @Option(names = "-e", description = "try to all triples possible")
    boolean extra;

    @Override
    public Integer call() throws Exception { // your business logic goes here...
        ParsedIRI baseIRI = new ParsedIRI(base);
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            try (Stream<String> s = Files.lines(inputFile.toPath(), StandardCharsets.US_ASCII)) {
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
        } else {
            tw.handleNamespace(RDF.PREFIX, RDF.NAMESPACE);
            tw.handleNamespace(VG.PREFIX, VG.NAMESPACE);
            tw.handleNamespace("node", base + "node/");
            tw.handleNamespace(FALDO.PREFIX, FALDO.NAMESPACE);
        }
        int pathCounter = 0;
        Iterator<String> si = s.iterator();
        convert(si, pathCounter, tw);

        tw.endRDF();
    }

    public void convert(Iterator<String> si, int pathCounter, TurtleWriter tw) {
        GFA1Reader gfA1Reader = new GFA1Reader(si);
        while (gfA1Reader.hasNext()) {
            Line line = gfA1Reader.next();
            pathCounter = convertLineToRdf(line, tw, pathCounter);
        }
    }

    public static void main(String args) {
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

    private int convertPathLineToRdf(PathLine pathLine, RDFWriter tw, int pathCounter, boolean preCompress) {
        String nameAsString = pathLine.getNameAsString();
        IRI pathIRI;
        if (nameAsString.startsWith("http://") || nameAsString.startsWith("ftp://") || nameAsString.startsWith("https://")) {
            pathIRI = VF.createIRI(nameAsString);
        } else {
            pathIRI = VF.createIRI(base + "path/", nameAsString);
        }
        tw.handleStatement(VF.createStatement(pathIRI, RDF.TYPE, VG.Path));
        String pathBase = pathIRI.stringValue() + "/step/";
        if (preCompress) {
            tw.handleNamespace("p" + pathCounter, pathBase);
            tw.handleNamespace("pn" + pathCounter, pathIRI.stringValue());
        } else {
            tw.handleNamespace("path" + pathCounter, pathBase);
        }
        Iterator<Step> steps = pathLine.steps();
        while (steps.hasNext()) {
            Step step = steps.next();
            long rank = step.rank();
            IRI stepIRI = VF.createIRI(pathBase, Long.toString(rank));
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
            tw.handleStatement(VF.createStatement(stepIRI, VG.node, createNodeId(new String(step.nodeId(), US_ASCII))));
        }
        return pathCounter++;
    }

    private void convertSegmentLineToRdf(SegmentLine segmentLine, RDFWriter tw) {
        IRI nodeIRI = VF.createIRI(base + "node/", segmentLine.getNameAsString());
        tw.handleStatement(VF.createStatement(nodeIRI, RDF.TYPE, VG.Node));
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
                if (!normalized.equals(XMLDatatypeUtil.POSITIVE_INFINITY)
                        && !normalized.equals(XMLDatatypeUtil.NEGATIVE_INFINITY)
                        && !normalized.equals(XMLDatatypeUtil.NaN)) {
                    writer.write(normalized);
                    return; // done
                }
            } else {
                super.writeLiteral(res);
            }
        }
    }
}
