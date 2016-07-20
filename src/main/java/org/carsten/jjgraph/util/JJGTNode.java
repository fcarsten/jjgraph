/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

/**
 * JJGTNode.java
 *
 *
 * Created: Mon Dec  6 15:06:53 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import org.carsten.jjgraph.graph.JJNode;

public class JJGTNode {
	private JJNode v1;
	private JJNode v2;

	private JJPoint pos;

	public JJGTNode() {
		v1 = null;
		v2 = null;
	}

	public JJPoint getPos() {
		return pos;
	}

	public void setPos(final JJPoint n) {
		pos = n;
	}

	public double getX() {
		return pos.getX();
	}

	public void setX(final double n) {
		pos.setX(n);
	}

	public double getY() {
		return pos.getY();
	}

	public void setY(final double n) {
		pos.setY(n);
	}

	public JJNode getV1() {
		return v1;
	}

	public void setV1(final JJNode n) {
		v1 = n;
	}

	public JJNode getV2() {
		return v2;
	}

	public void setV2(final JJNode n) {
		v2 = n;
	}

	public JJGTNode(final JJNode eins, final JJNode zwei) {
		v1 = eins;
		v2 = zwei;
		/* propAllocNode=prop; */
	}

} // JJGTNode
