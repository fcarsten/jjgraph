/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

/**
 * JJGEMTranslatorNode.java
 *
 *
 * Created: Wed Feb 16 16:32:29 2000
 *
 * @author Carsten Friedrich
 * @version
 */

import org.carsten.jjgraph.util.JJPoint;

public class JJGEMTranslatorNode {
	JJPoint pos = null;
	// JJPoint imp = null; // last impusle
	JJAnimationNode bNode = null;

	// double heat; // local temperature
	// double dir; // skew gauge

	// JJPoint oldPos;

	// double mass;

	// JJGraphicNode node =null;

	public JJGEMTranslatorNode(final JJAnimationNode d) {
		// node = n;
		// imp = new JJPoint();
		pos = new JJPoint(d.getPosition());
		bNode = d;
		;

		// oldPos = new JJPoint(pos);

		// heat = JJGem.STARTTEMP; // * edgeLength;
		// mass = 1 + (double)deg/3.0;
		// dir=0;
	}
}
