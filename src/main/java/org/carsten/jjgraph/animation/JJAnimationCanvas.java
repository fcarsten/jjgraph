/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

import java.awt.Color;

import org.carsten.jjgraph.util.JJPoint;

public interface JJAnimationCanvas {
	// cause the canvas to display any changes
	void refresh();

	// returns the current zoom factor. Used for animation speed control
	double getZoom();

	Color getBackground();

	// Moves AnimationShape shape to the position newPosition
	void setPosition(JJAnimatedShape shape, JJPoint newPosition);
	// Rectangle getVisibleRect();
	// Rectangle boundingBox();
	// void setPredrawer(JJPreDrawer j);

	void unselectAll();

	void select(JJAnimatedShape shape);

}
