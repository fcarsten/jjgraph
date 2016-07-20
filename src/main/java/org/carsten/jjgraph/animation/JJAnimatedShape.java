package org.carsten.jjgraph.animation;
/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */

import java.awt.Color;
import java.util.BitSet;

public interface JJAnimatedShape {
	// Valid values for fade policy:
	public final static int DURING = 0;
	public final static int SEPERATE = 1;

	// public static final int VISIBLE = 0;
	// public static final int HIDDEN = 1;

	// returns the Color of the shape
	Color getColor();

	// sets the color of the shape
	void setColor(Color c);

	// // allows the shape to adjust to the current frame
	// void setFrame(double f);

	// returns whether the shape is currently visible
	BitSet getVisible();

	boolean isVisible();

	// sets visibility of the shape
	void setVisible(int v, boolean b);

	void setFadePolicy(int i);

	int getFadePolicy();

	String getName();

	Object getAnimationData();

	void setAnimationStartData(Object o);

	void setAnimationEndData(Object o);

	void frame(Object o, double d);

}
