/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

import java.util.Iterator;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.util.JJPoint;

public interface JJMovingShape extends JJAnimatedShape {
	// the current position of the shape
	JJPoint getCoords();

	void createUndoMoveEventTo(JJPoint p);

	Iterator<JJEdge> edgeIterator();

	Iterator<JJEdge> outIterator();

	JJAnimatedShape opposite(JJEdge e);

	void setMNI(int i);

	int getMNI();
}
