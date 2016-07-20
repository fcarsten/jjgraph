/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

/**
 * JJGTEdge.java
 *
 *
 * Created: Mon Dec  6 16:35:41 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import org.carsten.jjgraph.graph.JJNode;

public class JJGTEdge {
	private JJNode qNode;

	public JJNode getQNode() {
		return qNode;
	}

	public void setQNode(final JJNode n) {
		qNode = n;
	}

	public JJGTEdge(final JJNode n) {
		qNode = n;
	}

} // JJGTEdge
