/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sib.swiss.swissprot.handlegraph4jrdf;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

/**
 *
 * @author Jerven Bolleman <jerven.bolleman@sib.swiss>
 */
public class VG {

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

    public static final IRI Node = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "Node");
    public static final IRI Path = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "Path");
    public static final IRI Step = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "Step");
    public static final IRI rank = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "rank");
    public static final IRI position = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "position");
    public static final IRI path = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "path");
    public static final IRI linksForwardToForward = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "linksForwardToForward");
    public static final IRI linksForwardToReverse = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "linksForwardToReverse");
    public static final IRI linksReverseToForward = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "linksReverseToForward");
    public static final IRI linksReverseToReverse = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "linksReverseToReverse");
    public static final IRI links = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "links");
    public static final IRI reverseOfNode = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "reverseOfNode");
    public static final IRI node = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "node");
}
