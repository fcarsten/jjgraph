/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.francois;

/**
 * JJSugiEdge.java
 *
 *
 * Created: Thu Apr 20 15:51:47 2000
 *
 * @author Carsten Friedrich
 * @version
 */
import org.carsten.jjgraph.graph.JJEdge;

public class JJSugiEdge {
	JJEdge jjedge;
	Edge fredge;
	int id = -1;
	boolean reverted = false;

	public JJSugiEdge(final JJEdge n, final Edge n2) {
		jjedge = n;
		fredge = n2;
	}

	public JJSugiEdge(final Digraph g, final JJEdge n, final Edge n2) {
		jjedge = n;
		fredge = n2;
		id = g.eb_id++;
	}

} // JJSugiEdge
