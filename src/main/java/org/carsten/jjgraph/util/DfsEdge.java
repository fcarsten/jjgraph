/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

import org.carsten.jjgraph.graph.JJEdge;

public class DfsEdge {
	JJEdge edge = null;
	public int edgeType = JJDfs.TI_NO;

	public DfsEdge(final JJEdge e) {
		edge = e;
	}

}
