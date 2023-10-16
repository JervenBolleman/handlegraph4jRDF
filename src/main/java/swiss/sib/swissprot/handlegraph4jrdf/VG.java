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
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * A minimal set of properties and classes that are needed to describe variation
 * graphs.
 * 
 * @see <a href="http://biohackathon.org/resource/vg">The VG Description in OWL</a>
 * @author <a href="mailto:jerven.bolleman@sib.swiss">Jerven Bolleman</a>
 */
public class VG {

	private VG() {

	}

	/**
	 * http://biohackathon.org/resource/vg#
	 */
	public static final String NAMESPACE = "http://biohackathon.org/resource/vg#";

	/**
	 * Recommended prefix for the VG namespace: "vg"
	 */
	public static final String PREFIX = "vg";

	/**
	 * An immutable {@link Namespace} constant that represents the VG namespace.
	 */
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);
	/**
	 * A node in the variant graph, representing a sequence section.
	 */
	public static final IRI Node = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "Node");
	/**
	 * A Path is a collection of steps from path to path that represent an
	 * asserdfs:labelmbled sequence integrated into the variant graph.
	 */
	public static final IRI Path = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "Path");
	/**
	 * A step along a path in the variant graph. A series of steps along a path
	 * represent an assembled sequence that was originally inserted into the the
	 * variant graph. A step points to a :Node or the reverse complement of a node
	 * and has a rank (step number).
	 */
	public static final IRI Step = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "Step");
	/**
	 * The rank records the step place along its path.
	 */
	public static final IRI rank = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "rank");
	/**
	 * This is the position on the reference path at which this step starts.
	 */
	public static final IRI position = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "position");
	/**
	 * This means that this step occurs on the path that is the object of this
	 * statment
	 */
	public static final IRI path = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "path");
	/**
	 * This links a node from the forward (5' to 3') strand on the subject node to
	 * the forward (5' to 3') strand on the predicate node.
	 */
	public static final IRI linksForwardToForward = SimpleValueFactory.getInstance().createIRI(NAMESPACE,
			"linksForwardToForward");
	/**
	 * This links a node from the forward (5' to 3') strand on the subject node to
	 * the reverse (3' to 5') strand on the predicate node.
	 */
	public static final IRI linksForwardToReverse = SimpleValueFactory.getInstance().createIRI(NAMESPACE,
			"linksForwardToReverse");
	/**
	 * This links a node from the reverse (3' to 5') strand on the subject node to
	 * the forward (5' to 3') strand on the predicate node.
	 */
	public static final IRI linksReverseToForward = SimpleValueFactory.getInstance().createIRI(NAMESPACE,
			"linksReverseToForward");
	/**
	 * This links a node from the reverse (3' to 5') strand on the subject node to
	 * the reverse (3' to 5') strand on the predicate node.
	 */
	public static final IRI linksReverseToReverse = SimpleValueFactory.getInstance().createIRI(NAMESPACE,
			"linksReverseToReverse");
	/**
	 * The super property that says two nodes are linked and does not allow one to
	 * infer which side to side it goes.
	 */
	public static final IRI links = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "links");
	/**
	 * This means this step occurs on the revese complement of the sequence attaced
	 * to the node (i.e. it is on the implicit reverse (3' to 5') strand) of the
	 * predicate node.
	 */
	public static final IRI reverseOfNode = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "reverseOfNode");
	/**
	 * This means that this step occurs on the forward strand of the sequence
	 * attaced to the node (i.e. it is on the explicit encoded forward (5' to 3')
	 * strand) of the predicate node.
	 */
	public static final IRI node = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "node");
}
