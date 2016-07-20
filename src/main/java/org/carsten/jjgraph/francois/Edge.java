/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.francois;

import java.util.ArrayList;
import java.util.List;

class Edge {
	List<Node> dummies = new ArrayList<>();

	Edge(final Node s, final Node t) {
		source = s;
		target = t;
	}

	public void revert() {
		final Node tmp = source;
		source = target;
		target = tmp;
	}

	Node source;
	Node target;
	boolean reverse = false;
	boolean crv = false; // cycle remove visited
}
