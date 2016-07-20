/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.francois;

class Node {

	Node(final Digraph g) {
		id = g.nb_id++;
		g.addNode(this);
	}

	Node(final int i) {
		id = i;
	}

	static int nb_id = 1;

	int xpos = 0;
	int ypos = 0;
	int layer = -1;
	int number;

	int id;
	boolean dummy = false;
}
