/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

import java.awt.Color;
import java.awt.Graphics2D;

import org.carsten.jjgraph.util.JJPoint;

public class JJGraphicNodeCircle extends JJGraphicNodeAppearance {
	protected int radius;

	@Override
	public void setWidth(final int r) {
		super.setWidth(r);

		setRadius(r);
	}

	@Override
	public void setHeight(final int r) {
		super.setHeight(r);
		setRadius(r);
	}

	@Override
	public int getHeight(final boolean outline) {
		return getRadius(outline);
	}

	@Override
	public int getWidth(final boolean outline) {
		return getRadius(outline);
	}

	public int getRadius(final boolean outline) {
		return Math.max(super.getWidth(outline), super.getHeight(outline));
	}

	public void setRadius(final int r) {
		this.radius = Math.max(width, height);
	}

	@Override
	public void paint(final Graphics2D g) {
		final JJGraphWindow fenster = knoten.window();

		double zoom = 1;
		int outlineOffset = 0;

		int x = (int) getX();
		int y = (int) getY();

		if (fenster != null) {
			zoom = fenster.getZoom();
			x = (int) fenster.deviceAdjustX(getX());
			y = (int) fenster.deviceAdjustY(getY());
			if (fenster.getOutline())
				outlineOffset = 2 * knoten.outlineWidth;
		}

		final Color nodeColor = knoten.getColor();
		g.setColor(nodeColor);

		final String name = knoten.getDisplayedName();

		final int w = knoten.getWidth();

		g.fillOval(x - w / 2, y - w / 2, w, w);

		if (outlineOffset != 0) {
			if (nodeColor.getAlpha() == 255)
				g.setColor(Color.white);
			else
				g.setColor(new Color(255, 255, 255, nodeColor.getAlpha()));

			g.fillOval((x - w / 2) + knoten.outlineWidth, (y - w / 2) + knoten.outlineWidth,
					w - 2 * knoten.outlineWidth, w - 2 * knoten.outlineWidth);
		}

		if (knoten.isSelected())
			g.setColor(Color.red);
		else {
			if (nodeColor.getAlpha() == 255)
				g.setColor(Color.black);
			else
				g.setColor(new Color(0, 0, 0, nodeColor.getAlpha()));
		}

		g.drawOval(x - w / 2, y - w / 2, Math.max(1, w - 1), Math.max(1, w - 1));

		if (outlineOffset != 0) {
			g.drawOval((x - w / 2) + knoten.outlineWidth, (y - w / 2) + knoten.outlineWidth,
					(w - 1) - 2 * knoten.outlineWidth, (w - 1) - 2 * knoten.outlineWidth);
		} else if (!knoten.isSelected()) {
			final Color nc = knoten.getColor();
			if (nc.getGreen() < 125) {
				if (nodeColor.getAlpha() == 255)
					g.setColor(Color.white);
				else
					g.setColor(new Color(255, 255, 255, nodeColor.getAlpha()));
			}
		}

		if ((!name.equals("")) && (knoten.getLabelPosition() == JJGraphWindow.LABEL_INSIDE)) {
			g.drawString(name, (x - (w - JJGraphicNodeImpl.X_NODE_BORDER) / 2) + outlineOffset / 2,
					(y - (knoten.labelHeight - JJGraphicNodeImpl.Y_NODE_BORDER) / 2) + // outlineOffset/2
																						// +
							knoten.getFontMetrics().getAscent());
		}
	}

	@Override
	public JJPoint adjustLineEnd(final JJPoint B) {
		final JJPoint result = new JJPoint(getX(), getY());

		final JJGraphWindow fenster = knoten.window();

		if (fenster != null) {
			result.x = fenster.deviceAdjustX(getX());
			result.y = fenster.deviceAdjustY(getY());
		}

		final double radius = getRadius(knoten.window().getOutline());

		final JJPoint diff = JJPoint.minus(B, result);
		diff.divA(diff.abs() / (radius / 2.0));
		result.plusA(diff);

		return result;
	}

}
