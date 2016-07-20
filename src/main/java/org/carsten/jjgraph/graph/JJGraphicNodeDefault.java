/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import org.carsten.jjgraph.util.JJPoint;

public class JJGraphicNodeDefault extends JJGraphicNodeAppearance {
	BasicStroke thickStroke = new BasicStroke(2f);
	BasicStroke thinStroke = new BasicStroke();

	public JJGraphicNodeDefault(final JJGraphicNodeImpl n) {
		knoten = n;
	}

	public JJGraphicNodeDefault() {
	}

	@Override
	public void paint(final Graphics2D g) {
		// double zoom = 1;
		int outlineOffset = 0;

		int x = (int) knoten.getX();
		int y = (int) knoten.getY();
		final JJGraphWindow fenster = knoten.window();

		if (fenster != null) {
			// zoom = fenster.getZoom();
			x = (int) fenster.deviceAdjustX(knoten.getX());
			y = (int) fenster.deviceAdjustY(knoten.getY());
			if (fenster.getOutline())
				outlineOffset = 2 * knoten.outlineWidth;
		}

		final Color nodeColor = knoten.getColor();
		g.setColor(nodeColor);

		final String name = knoten.getDisplayedName();

		final int w = knoten.getWidth(); // (int)n.getWidth() + outlineOffset;
		final int h = knoten.getHeight(); // (int)n.getHeight() + outlineOffset;

		// Debug.println("x : " + (x-w/2) +
		// "x : " + (y-h/2) +
		// "x : " + w +
		// "x : " + h);

		// if(x < -2.0e6 || x > 2.0e6 || y < -2.0e6 || y > 2.0e6) {
		// return;
		// }

		if (((x + w / 2) > JJGraphFrame.canvasWidth) || ((y + h / 2) > JJGraphFrame.canvasHeight)) {
			if (fenster != null)
				fenster.printError("Illegal coordiantes");
			return;
		}

		g.fillRect(x - w / 2, y - h / 2, w, h);

		if (outlineOffset != 0) {
			if (nodeColor.getAlpha() == 255)
				g.setColor(Color.white);
			else
				g.setColor(new Color(255, 255, 255, nodeColor.getAlpha()));

			g.fillRect((x - w / 2) + knoten.outlineWidth, (y - h / 2) + knoten.outlineWidth,
					w - 2 * knoten.outlineWidth, h - 2 * knoten.outlineWidth);
		}

		if (knoten.isSelected()) {
			g.setColor(Color.red);
		} else {
			if (nodeColor.getAlpha() == 255)
				g.setColor(Color.black);
			else
				g.setColor(new Color(0, 0, 0, nodeColor.getAlpha()));
		}

		g.setStroke(thickStroke);
		g.drawRect(x - w / 2, y - h / 2, Math.max(1, w - 1), Math.max(1, h - 1));

		if (outlineOffset != 0) {
			g.drawRect((x - w / 2) + knoten.outlineWidth, (y - h / 2) + knoten.outlineWidth,
					(w - 1) - 2 * knoten.outlineWidth, (h - 1) - 2 * knoten.outlineWidth);
		} else {
			final Color nc = knoten.getColor();
			if (nc.getGreen() < 125) {
				if (nodeColor.getAlpha() == 255)
					g.setColor(Color.white);
				else
					g.setColor(new Color(255, 255, 255, nodeColor.getAlpha()));
			} else {
				if (nodeColor.getAlpha() == 255)
					g.setColor(Color.black);
				else
					g.setColor(new Color(0, 0, 0, nodeColor.getAlpha()));
			}

		}

		if ((!name.equals("")) && (knoten.getLabelPosition() == JJGraphWindow.LABEL_INSIDE)) {
			g.drawString(name, (x - (w - JJGraphicNodeImpl.X_NODE_BORDER) / 2) + outlineOffset / 2,
					(y - (h - JJGraphicNodeImpl.Y_NODE_BORDER) / 2) + outlineOffset / 2
							+ knoten.getFontMetrics().getAscent());
		}
		g.setStroke(thinStroke);
	}

	@Override
	public JJPoint adjustLineEnd(final JJPoint B) {
		final JJPoint result = new JJPoint(0, 0);

		double aa, bb;

		final JJPoint A = new JJPoint(knoten.getX(), knoten.getY());
		double width = getWidth() / 2.0;
		double height = getHeight() / 2.0;

		final JJGraphWindow fenster = knoten.window();
		if (fenster != null) {
			final Rectangle r = fenster.getBounds(knoten);
			if (r == null) {
				result.x = fenster.deviceAdjustX(knoten.getX());
				result.y = fenster.deviceAdjustY(knoten.getY());
				return result;
			}

			width = r.width / 2.0;
			height = r.height / 2.0;
			A.x = r.x + width;
			A.y = r.y + height;
		}

		if (A.y == B.y) {
			aa = width;
			bb = 0;
		} else if (A.x == B.x) {
			aa = 0;
			bb = height;
		} else {
			aa = (Math.abs(A.x - B.x) * height) / Math.abs(A.y - B.y);
			bb = (Math.abs(A.y - B.y) * width) / Math.abs(A.x - B.x);
			if (aa > width)
				aa = width;
			else
				bb = height;
		}

		if (A.x > B.x)
			aa = -aa;
		if (A.y > B.y)
			bb = -bb;

		result.x = A.x + aa;
		result.y = A.y + bb;

		return result;
	}
}
