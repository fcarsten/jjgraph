/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.francois;

/**
 * JJGemNode.java
 *
 *
 * Created: Wed Feb 16 16:32:29 2000
 *
 * @author Carsten Friedrich
 * @version
 */
import org.carsten.jjgraph.graph.JJGraphicNode;
import org.carsten.jjgraph.util.JJPoint;

public class JJFRNode {
	JJPoint pos = null;
	JJGraphicNode node = null;
	Force force = null;

	public JJFRNode(final JJGraphicNode n) {
		node = n;
		pos = new JJPoint(n.getCoords());
		force = new Force();
	}
}
