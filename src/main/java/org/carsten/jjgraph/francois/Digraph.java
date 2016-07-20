/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.francois;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Digraph {
	List<Edge> edges = new ArrayList<>();
	List<Node> nodes = new ArrayList<>();
	int nb_id = 0;
	int eb_id = 0;

	void transpose() {
		for (final Object element : edges) {
			final Edge e = (Edge) element;
			final Node tmp = e.source;
			e.source = e.target;
			e.target = tmp;
		}
	}

	void shuffle() {
		Collections.shuffle(nodes);
		Collections.shuffle(edges);

	}

	/** source and target of the edge must be in the digraph already */
	Edge addEdge(final Edge e) {
		edges.add(e);
		return e;
	}

	Edge removeEdge(final Edge e) {
		edges.remove(e);
		return e;
	}

	Node addNode(final Node n) {
		nodes.add(n);
		return n;
	}

	Node removeNode(final Node n) {
		nodes.remove(n);
		return n;
	}

	int numberOfNodes() {
		return nodes.size();
	}

	int numberOfEdges() {
		return edges.size();
	}

	List<Node> inNodes(final Node n) {
		final List<Node> l = new ArrayList<>();
		for (final Edge element : edges) {
			final Edge e = element;
			if (e.target == n)
				l.add(e.source);
		}
		return l;
	}

	int inDegree(final Node n) {
		return inNodes(n).size();
	}

	List<Node> outNodes(final Node n) {
		final List<Node> l = new ArrayList<>();
		for (final Edge element : edges) {
			final Edge e = element;
			if (e.source == n)
				l.add(e.target);
		}
		return l;
	}

	int outDegree(final Node n) {
		return outNodes(n).size();
	}
}
