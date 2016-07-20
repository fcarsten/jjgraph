/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.layout;

/**
 * JJTutteNode.java
 *
 *
 * Created: Wed Feb 16 16:32:29 2000
 *
 * @author Carsten Friedrich
 * @version
 */
import org.carsten.jjgraph.graph.JJNode;
import org.carsten.jjgraph.util.JJPoint;

public class JJTutteNode {
	JJPoint newPos = null;
	JJNode node = null;
	boolean fixed;

	public JJTutteNode(final JJNode n) {
		node = n;
		newPos = new JJPoint();

		fixed = false;
	}
}
