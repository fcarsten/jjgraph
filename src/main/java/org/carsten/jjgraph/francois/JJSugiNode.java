/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.francois;

/**
 * JJSugiNode.java
 *
 *
 * Created: Thu Apr 20 15:51:47 2000
 *
 * @author Carsten Friedrich
 * @version
 */
import org.carsten.jjgraph.graph.JJNode;

public class JJSugiNode {
	JJNode jjnode;
	Node frnode;

	public JJSugiNode(final JJNode n, final Node n2) {
		jjnode = n;
		frnode = n2;
	}

} // JJSugiNode
