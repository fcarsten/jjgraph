/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

import org.carsten.jjgraph.graph.JJNode;

public class DfsNode {
	JJNode node = null;
	public int dfsNum = -1;
	public int compNum = -1;
	public int lowNum = 0;

	public DfsNode(final JJNode n) {
		node = n;
	}

}
