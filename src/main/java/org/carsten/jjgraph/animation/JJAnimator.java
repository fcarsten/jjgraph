/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

import org.carsten.jjgraph.util.JJPoint;

public interface JJAnimator {
	// set the canvas that the shapes in the animator lie in
	void setCanvas(JJAnimationCanvas canvas);

	// add shapes to be animated
	JJSceneContainer initStartPositions(JJAnimatedShape[] shapes, JJPoint centerOld);

	// true if animation is complete
	boolean hasNext();

	// animate by one frame
	boolean next();

	// rewinds the animation so it can be shown again
	void rewind();
}
