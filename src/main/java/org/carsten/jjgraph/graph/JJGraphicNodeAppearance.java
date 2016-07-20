/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

import java.awt.Graphics2D;

import org.carsten.jjgraph.util.JJPoint;

public abstract class JJGraphicNodeAppearance {
	protected JJGraphicNodeImpl knoten;

	public void init(final JJGraphicNodeImpl n) {
		knoten = n;
	}

	abstract public void paint(Graphics2D g);

	public double getX() {
		return knoten.getX();
	}

	public double getY() {
		return knoten.getY();
	}

	protected int width = JJGraphicNodeImpl.DEFAULT_NODE_WIDTH;
	protected int height = JJGraphicNodeImpl.DEFAULT_NODE_HEIGHT;

	public void setWidth(final int v) {
		width = v;
	}

	public void setHeight(final int v) {
		height = v;
	}

	public int getHeight() {
		return getHeight(knoten.window().getOutline());
	}

	public int getHeight(final boolean outline) {
		int oo = 0;
		if (outline)
			oo = 2 * knoten.outlineWidth;
		switch (knoten.getLabelPosition()) {
		case JJGraphWindow.LABEL_NONE:
			return height + oo;
		case JJGraphWindow.LABEL_TOP:
		case JJGraphWindow.LABEL_BOTTOM:
			return knoten.labelHeight + height + oo;
		case JJGraphWindow.LABEL_LEFT:
		case JJGraphWindow.LABEL_RIGHT:
		case JJGraphWindow.LABEL_INSIDE:
			return Math.max(knoten.labelHeight, height) + oo;
		default:
			return height + oo;
		}
	}

	public int getWidth() {
		return getWidth(knoten.window().getOutline());

	}

	public int getWidth(final boolean outline) {
		int oo = 0;
		if (outline)
			oo = 2 * knoten.outlineWidth;

		switch (knoten.getLabelPosition()) {
		case JJGraphWindow.LABEL_NONE:
			return width + oo;
		case JJGraphWindow.LABEL_INSIDE:
		case JJGraphWindow.LABEL_TOP:
		case JJGraphWindow.LABEL_BOTTOM:
			return Math.max(knoten.labelWidth, width) + oo;
		case JJGraphWindow.LABEL_LEFT:
		case JJGraphWindow.LABEL_RIGHT:
			return knoten.labelWidth + width + oo;
		default:
			return width + oo;
		}
	}

	abstract public JJPoint adjustLineEnd(JJPoint B);
}
