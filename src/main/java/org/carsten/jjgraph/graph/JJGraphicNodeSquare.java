/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

import java.awt.Color;
import java.awt.Graphics2D;

public class JJGraphicNodeSquare extends JJGraphicNodeDefault {

	@Override
	public int getHeight() {
		return Math.max(super.getWidth(), super.getHeight());
	}

	@Override
	public int getWidth() {
		return Math.max(super.getWidth(), super.getHeight());
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

		final int w = knoten.getWidth(); // (int)n.getWidth() + outlineOffset;
		final int h = knoten.getHeight(); // (int)n.getHeight() + outlineOffset;

		g.fillRect(x - w / 2, y - h / 2, w, h);

		if (outlineOffset != 0) {
			if (nodeColor.getAlpha() == 255)
				g.setColor(Color.white);
			else
				g.setColor(new Color(255, 255, 255, nodeColor.getAlpha()));

			g.fillRect((x - w / 2) + knoten.outlineWidth, (y - h / 2) + knoten.outlineWidth,
					w - 2 * knoten.outlineWidth, h - 2 * knoten.outlineWidth);
		}

		if (knoten.isSelected())
			g.setColor(Color.red);
		else {
			if (nodeColor.getAlpha() == 255)
				g.setColor(Color.black);
			else
				g.setColor(new Color(0, 0, 0, nodeColor.getAlpha()));
		}

		g.drawRect(x - w / 2, y - h / 2, Math.max(1, w - 1), Math.max(1, h - 1));

		if (outlineOffset != 0) {
			g.drawRect((x - w / 2) + knoten.outlineWidth, (y - h / 2) + knoten.outlineWidth,
					(w - 1) - 2 * knoten.outlineWidth, (h - 1) - 2 * knoten.outlineWidth);
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
					(y - (knoten.labelHeight - JJGraphicNodeImpl.Y_NODE_BORDER) / 2) + // +
																						// outlineOffset/2
																						// +
							knoten.getFontMetrics().getAscent());
		}
	}

}
