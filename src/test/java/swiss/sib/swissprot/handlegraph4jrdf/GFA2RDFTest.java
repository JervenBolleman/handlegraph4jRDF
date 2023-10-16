/*
 * The MIT License
 *
 * Copyright 2020 Jerven Bolleman <jerven.bolleman@sib.swiss>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package swiss.sib.swissprot.handlegraph4jrdf;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.stream.Stream;

import org.eclipse.rdf4j.common.net.ParsedIRI;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.jupiter.api.Test;

/**
 *
 * @author <a href="mailto:jerven.bolleman@sib.swiss">Jerven Bolleman</a>
 */
public class GFA2RDFTest {

    private static final String TEST_DATA = "H\tVN:Z:1.0\n"
            + "S\t1\tCAAATAAG\n"
            + "S\t2\tA\n"
            + "S\t3\tG\n"
            + "S\t4\tT\n"
            + "S\t5\tC\n"
            + "S\t6\tTTG\n"
            + "S\t7\tA\n"
            + "S\t8\tG\n"
            + "S\t9\tAAATTTTCTGGAGTTCTAT\n"
            + "S\t10\tA\n"
            + "S\t11\tT\n"
            + "S\t12\tATAT\n"
            + "S\t13\tA\n"
            + "S\t14\tT\n"
            + "S\t15\tCCAACTCTCTG\n"
            + "P\tx\t1+,3+,5+,6+,8+,9+,11+,12+,14+,15+\t8M,1M,1M,3M,1M,19M,1M,4M,1M,11M\n"
            + "L\t1\t+\t2\t+\t0M\n"
            + "L\t1\t+\t3\t+\t0M\n"
            + "L\t2\t+\t4\t+\t0M\n"
            + "L\t2\t+\t5\t+\t0M\n"
            + "L\t3\t+\t4\t+\t0M\n"
            + "L\t3\t+\t5\t+\t0M\n"
            + "L\t4\t+\t6\t+\t0M\n"
            + "L\t5\t+\t6\t+\t0M\n"
            + "L\t6\t+\t7\t+\t0M\n"
            + "L\t6\t+\t8\t+\t0M\n"
            + "L\t7\t+\t9\t+\t0M\n"
            + "L\t8\t+\t9\t+\t0M\n"
            + "L\t9\t+\t10\t+\t0M\n"
            + "L\t9\t+\t11\t+\t0M\n"
            + "L\t10\t+\t12\t+\t0M\n"
            + "L\t11\t+\t12\t+\t0M\n"
            + "L\t12\t+\t13\t+\t0M\n"
            + "L\t12\t+\t14\t+\t0M\n"
            + "L\t13\t+\t15\t+\t0M\n"
            + "L\t14\t+\t15\t+\t0M";

    public GFA2RDFTest() {
    }

    /**
     * Test of convert method, of class GFA2RDF.
     */
    @Test
    public void testConvertPreCompressed() throws IOException, URISyntaxException {
        Stream<String> si = Arrays.asList(TEST_DATA.split("\n")).stream();
        String httpexampleorgvg = "http://example.org/vg/";
        ParsedIRI baseIRI = new ParsedIRI(httpexampleorgvg);
        String toString;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            GFA2RDF instance = new GFA2RDF();
            instance.preCompress = true;
            instance.writeConvertedToOutputStream(baos, baseIRI, si);
            baos.flush();
            toString = baos.toString();
            System.out.print(toString);
            assertTrue(toString.length() > 100);
        }
        RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
        parser.parse(new StringReader(toString), httpexampleorgvg);
    }

    @Test
    public void testConvertExtra() throws IOException, URISyntaxException {
        Stream<String> si = Arrays.asList(TEST_DATA.split("\n")).stream();
        String httpexampleorgvg = "http://example.org/vg/";
        ParsedIRI baseIRI = new ParsedIRI(httpexampleorgvg);
        String toString;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            GFA2RDF instance = new GFA2RDF();
            instance.extra = true;
            instance.writeConvertedToOutputStream(baos, baseIRI, si);
            baos.flush();
            toString = baos.toString();
            System.out.print(toString);
            assertTrue(toString.length() > 100);
        }
        RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
        parser.parse(new StringReader(toString), httpexampleorgvg);
    }
}
