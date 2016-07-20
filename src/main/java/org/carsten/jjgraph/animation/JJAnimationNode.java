/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

import java.awt.Color;
import java.util.BitSet;

/**
 * JJGraphAnimationNode.java
 *
 *
 * Created: Thu Mar 16 15:19:26 2000
 *
 * @author Carsten Friedrich
 * @version
 */

// import org.carsten.jjgraph.graph.*;
import org.carsten.jjgraph.util.JJPoint;

public interface JJAnimationNode {
	BitSet getStartVisible();

	BitSet getEndVisible();

	boolean isVisible();

	JJAnimatedShape getAnimatedShape();

	JJMovingShape getMovingShape();

	int getFadePolicy();

	void setFadePolicy(int d);

	JJPoint getEndPosition();

	JJPoint getStartPosition();

	// JJPoint getTargetPosition();
	JJPoint getPosition();

	double getX();

	double getY();

	void setPosition(JJPoint p);

	void setCustomPos(JJPoint p);

	JJPoint getCustomPos();

	Color getStartColor();

	Color getEndColor();

	Color getColor();

	void frame(double d);

	/**
	 * Get the value of userData.
	 *
	 * @return value of userData.
	 */
	Object getUserData();

	/**
	 * Set the value of userData.
	 *
	 * @param v
	 *            Value to assign to userData.
	 */
	void setUserData(Object v);

} // JJAnimationNode
