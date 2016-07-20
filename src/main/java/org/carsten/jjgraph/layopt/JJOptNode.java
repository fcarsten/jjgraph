/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.layopt;

/**
 * JJOptNode.java
 *
 *
 * Created: Wed Oct 11 18:06:20 2000
 *
 * @author
 * @version
 */
import org.carsten.jjgraph.graph.JJGraphicNode;
import org.carsten.jjgraph.util.JJPoint;
import org.carsten.jjgraph.util.JJRegressable;

public class JJOptNode implements JJRegressable {
	private JJPoint oldPos;
	private JJPoint newPos;
	private JJPoint optPos;
	JJGraphicNode node;

	public JJPoint getOptPos() {
		return optPos;
	}

	public void setOptPos(final JJPoint v) {
		this.optPos = v;
	}

	public JJPoint getNewPos() {
		return newPos;
	}

	public void setNewPos(final JJPoint v) {
		this.newPos = v;
	}

	public JJPoint getOldPos() {
		return oldPos;
	}

	public void setOldPos(final JJPoint v) {
		this.oldPos = v;
	}

	public JJOptNode(final JJPoint p, final JJGraphicNode n) {
		node = n;
		oldPos = p;
	}

	@Override
	public JJPoint getPos1() {
		return getNewPos();
	}

	@Override
	public JJPoint getPos2() {
		return getOldPos();
	}

} // JJOptNode
