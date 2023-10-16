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

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * FALDO is a set of properties and classes to describe positions along a linear
 * sequence.
 *
 * @see <a href="http://biohackathon.org/resource/faldo">The FALDO Description
 *      in OWL</a>
 * @author <a href="mailto:jerven.bolleman@sib.swiss">Jerven Bolleman</a>
 */
public class FALDO {

	private FALDO() {

	}

	/**
	 * The FALDO namespace
	 */
	public static final String NAMESPACE = "http://biohackathon.org/resource/faldo#";
	/**
	 * Prefered prefix for FALDO "faldo"
	 */
	public static final String PREFIX = "faldo";
	private static final SimpleValueFactory VF = SimpleValueFactory.getInstance();

	/**
	 * Used to describe a location that consists of a number of Regions but where
	 * the order is not known. e.g. the oddly named order() keyword in a INSDC file.
	 * *
	 */
	public static final IRI BagOfRegions = VF.createIRI(NAMESPACE, "BagOfRegions");

	/**
	 * The 'both strands position' indicates a region that is best described as
	 * being on 'both' strands of a double-stranded sequence, rather than one or the
	 * other. *
	 */
	public static final IRI BothStrandsPosition = VF.createIRI(NAMESPACE, "BothStrandsPosition");

	/**
	 * Sometimes a location of a feature is defined by a collection of regions e.g.
	 * join() and order() in INSDC records. One should always try to model the
	 * semantics more accurately than this, these are fallback options to encode
	 * legacy data. *
	 */
	public static final IRI CollectionOfRegions = VF.createIRI(NAMESPACE, "CollectionOfRegions");

	/**
	 * A position that is exactly known. *
	 */
	public static final IRI ExactPosition = VF.createIRI(NAMESPACE, "ExactPosition");

	/**
	 * The position of the starting amino-acid a protein or polypeptide terminated
	 * by an amino acid with a free amine group (-NH2). The convention for writing
	 * peptide sequences is to put the N-terminus on the left and write the sequence
	 * from N- to C-terminus. Instances of this class are often used when the
	 * reference sequence is not complete
	 */
	public static final IRI N = VF.createIRI(NAMESPACE, "N-TerminalPosition");

	/**
	 * The C-terminus is the end of an amino acid chain (protein or polypeptide),
	 * terminated by a free carboxyl group (-COOH).
	 */
	public static final IRI C = VF.createIRI(NAMESPACE, "C-TerminalPosition");

	/**
	 * The position is on the forward (positive, 5' to 3') strand. Shown as a '+' in
	 * GFF3 and GTF. *
	 */
	public static final IRI ForwardStrandPosition = VF.createIRI(NAMESPACE, "ForwardStrandPosition");

	/**
	 * A position that lacks exact data. *
	 */
	public static final IRI FuzzyPosition = VF.createIRI(NAMESPACE, "FuzzyPosition");

	/**
	 * This indicates that a feature is between two other positions that are both
	 * known exactly and that are next to each other. An example is a restriction
	 * enzyme cutting site. The cut is after one position and before the other
	 * position (hence, in between). *
	 */
	public static final IRI InBetweenPosition = VF.createIRI(NAMESPACE, "InBetweenPosition");

	/**
	 * Use when you have an idea of the range in which you can find the position,
	 * but you cannot be sure about the exact position. *
	 */
	public static final IRI InRangePosition = VF.createIRI(NAMESPACE, "InRangePosition");

	/**
	 * As an ordered list of regions (but the list might not be
	 * complete)."^^xsd:string , "Should be used when the location of a region is
	 * defined by an ordered list of Regions. However, try to avoid using these
	 * types in favor of using more explicit semantics about why the order is
	 * important. *
	 */
	public static final IRI ListOfRegions = VF.createIRI(NAMESPACE, "ListOfRegions");

	/**
	 * The position is known to be one of the more detailed positions listed by the
	 * location predicate. *
	 */
	public static final IRI OneOfPosition = VF.createIRI(NAMESPACE, "OneOfPosition");

	/**
	 * Superclass for the general concept of a position on a sequence. The sequence
	 * is designated with the reference predicate. *
	 */
	public static final IRI Position = VF.createIRI(NAMESPACE, "Position");

	/**
	 * A region describes a length of sequence with a start position and end
	 * position that represents a feature on a sequence, e.g. a gene. *
	 */
	public static final IRI Region = VF.createIRI(NAMESPACE, "Region");

	/**
	 * The position is on the reverse (complement, 3' to 5') strand of the sequence.
	 * Shown as '-' in GTF and GFF3. *
	 */
	public static final IRI ReverseStrandPosition = VF.createIRI(NAMESPACE, "ReverseStrandPosition");

	/**
	 * Part of the coordinate system denoting on which strand the feature can be
	 * found. If you do not yet know which stand the feature is on, you should tag
	 * the position with just this class. If you know more you should use one of the
	 * subclasses. This means a region described with a '.' in GFF3. A GFF3
	 * unstranded position does not have this type in FALDO -- those are just a
	 * 'position'. *
	 */
	public static final IRI StrandedPosition = VF.createIRI(NAMESPACE, "StrandedPosition");

	/**
	 * This predicate is used when you want to describe a non-inclusive range. Only
	 * used in the InBetweenPosition to say it is after a nucleotide, but before the
	 * next one. *
	 */
	public static final IRI after = VF.createIRI(NAMESPACE, "after");

	/**
	 * This predicate is used to indicate that the feature is found before the exact
	 * position. Use to indicate, for example, a cleavage site. The cleavage happens
	 * between two amino acids before one and after the other. *
	 */
	public static final IRI before = VF.createIRI(NAMESPACE, "before");

	/**
	 * The inclusive beginning of a position. Also known as start. *
	 */
	public static final IRI begin = VF.createIRI(NAMESPACE, "begin");

	/**
	 * This is the inverse of the begin:property. It is included to make it easier
	 * to write a number of OWL axioms. You should rarely use this in your raw data.
	 * *
	 */
	public static final IRI beginOf = VF.createIRI(NAMESPACE, "beginOf");

	/**
	 * The inclusive end of the position. *
	 */
	public static final IRI end = VF.createIRI(NAMESPACE, "end");

	/**
	 * This is the inverse of the begin:end. It is included to make it easier to
	 * write a number of OWL axioms. You should rarely use this in your raw data. *
	 */
	public static final IRI endOf = VF.createIRI(NAMESPACE, "endOf");
	/**
	 * This is the link between the concept whose location you are annotating and
	 * its range or position. For example, when annotating the region that describes
	 * an exon, the exon would be the subject and the region would be the object of
	 * the triple or: 'active site' 'location' [is] 'position 3'. *
	 */
	public static final IRI location = VF.createIRI(NAMESPACE, "location");

	/**
	 * Denoted in 1-based closed coordinates, i.e. the position on the first amino
	 * acid or nucleotide of a sequence has the value 1. For nucleotide sequences we
	 * count from the 5'end of the sequence, while for Aminoacid sequences we start
	 * counting from the N-Terminus."^^xsd:string , "The position value is the
	 * offset along the reference where this position is found. Thus the only the
	 * position value in combination with the reference determines where a position
	 * is. *
	 */
	public static final IRI position = VF.createIRI(NAMESPACE, "position");

	/**
	 * One of the possible positions listed for a OneOfPosition element. *
	 */
	public static final IRI possiblePosition = VF.createIRI(NAMESPACE, "possiblePosition");

	/**
	 * The reference is the resource that the position value is anchored to. For
	 * example, a contig or chromosome in a genome assembly.
	 */
	public static final IRI reference = VF.createIRI(NAMESPACE, "reference");
}
