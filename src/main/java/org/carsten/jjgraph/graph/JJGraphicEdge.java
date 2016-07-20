/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import org.carsten.jjgraph.animation.JJAnimatedShape;
import org.carsten.jjgraph.util.JJPoint;

public interface JJGraphicEdge extends JJAnimatedShape {
	JJEdge getEdge();

	void setEdge(JJEdge v);

	// int getVisible();
	// void setVisible(int v);
	// boolean isDrawn();
	void hide();

	void unhide();

	/*
	 * void setVisible(int v, boolean b); BitSet getVisible(); boolean
	 * isVisible();
	 */
	boolean isSelected();

	void setSelected(boolean v);

	void rmoveTo(double x, double y);

	void moveBendTo(int i, double x, double y);

	void addBendFirst(JJPoint p);

	void addBendLast(JJPoint p);

	void addBendAt(int i, JJPoint p);

	void removeBendFirst();

	void removeBendLast();

	void removeBendAt(int i);

	void revertBends();

	void removeBends();

	int getNumBends();

	java.util.List<JJPoint> getBends();

	void setBends(java.util.List<JJPoint> l);

	void createUndoMoveEventR(double x, double y);

	double getLabelX();

	double getLabelY();

	Rectangle getBounds();

	void paint(Graphics2D g);

	void recomputeEdgeSize();

	void init(JJGraphWindow w, JJEdge e, Color c);

	JJGraphWindow getWindow();

	@Override
	Color getColor();

	@Override
	void setColor(Color v);

	void bendsToNodes();

	void repaint();
}
