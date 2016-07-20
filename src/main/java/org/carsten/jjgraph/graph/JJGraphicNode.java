/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

import java.awt.Color;
import java.awt.Graphics2D;

import org.carsten.jjgraph.animation.JJMovingShape;
import org.carsten.jjgraph.util.JJPoint;

public interface JJGraphicNode extends JJMovingShape {
	boolean isFixed();

	void setFixed(boolean b);

	// int getVisible();
	// void setVisible(int v);
	void hide();

	void unhide();

	double dist(JJGraphicNode n);

	@Override
	Color getColor();

	@Override
	void setColor(Color v);

	boolean getSelected();

	boolean isSelected();

	void setSelected(boolean v);

	int getWidth();

	void setWidth(int x);

	int getHeight();

	void setHeight(int x);

	double getX();

	double getY();

	double getZ();

	// Rectangle getBounds();
	JJNode getNode();

	void moveTo(JJPoint p);

	void moveTo(double x, double y);

	void moveTo(double x, double y, double pz);

	void rmoveTo(double x, double y);

	void rmoveTo(JJPoint p);

	@Override
	JJPoint getCoords();

	// void updateWidth();
	@Override
	void createUndoMoveEventTo(JJPoint tmpP);

	// FontMetrics getFontMetrics();
	// void setFontMetrics(FontMetrics m);
	void recomputeNodeSize();

	void paint(Graphics2D g);

	void init(JJGraphWindow w, JJNode n, Color c);

	void repaint();

	JJPoint adjustLineEnd(JJPoint p);

	JJGraphWindow getWindow();
}
