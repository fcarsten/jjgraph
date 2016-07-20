/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

public class JJProjectionPlotter extends JJDataPlotter {

	public JJProjectionPlotter(final java.util.List<JJDataObject> ll) {
		super(ll);
	}

	@Override
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);

		final Rectangle visiRect = getVisibleRect();
		final double visiWidth = visiRect.getWidth();
		final double visiHeight = visiRect.getHeight();

		// Debug.println("Visible Rect: " + visiRect);

		final int opacity = fenster.opacitySlider.getValue();

		final double scaleX = 1;
		// if( visiRect.getWidth() * visiBox.getWidth() != 0)
		// scaleX =visiRect.getWidth() / visiBox.getWidth();
		final double scaleY = 1;

		// if( visiRect.getHeight() * visiBox.getHeight() != 0)
		// scaleY = visiRect.getHeight() / visiBox.getHeight();

		// offsetX = -visiBox.getX() + visiRect.getX()/2.0;
		// offsetY = -visiBox.getY() + visiRect.getY()/2.0;
		scale = Math.min(scaleX, scaleY);

		final Rectangle rect = g.getClipBounds();
		final Dimension d = new Dimension(rect.width, rect.height);

		if ((offscreen == null) || (d.width != offscreensize.width) || (d.height != offscreensize.height)) {
			if (d.height * d.width == 0)
				return;
			offscreen = createImage(d.width, d.height);
			offscreensize = d;
			offGraphics = (Graphics2D) offscreen.getGraphics();
			offGraphics.setFont(getFont());
			offGraphics.setStroke(new BasicStroke(0));
		} else
			offGraphics.setTransform(new AffineTransform());

		offGraphics.setColor(new Color(250, 250, 250));
		offGraphics.fillRect(0, 0, d.width, d.height);

		offGraphics.translate(-rect.x, -rect.y);
		// offGraphics.setColor(new Color(0,0,0,50));
		offGraphics.scale(scale, scale);
		offGraphics.translate(offsetX, offsetY);

		offGraphics.setColor(Color.black);
		final double boxWidth = visiWidth / dimensions;
		final double boxHeight = visiHeight / dimensions;

		for (double i = 0; i <= dimensions; i++) {
			offGraphics.drawLine(0, (int) (boxHeight * i), (int) visiWidth, (int) (boxHeight * i));
			offGraphics.drawLine((int) (boxWidth * i), 0, (int) (boxWidth * i), (int) (visiHeight));
		}

		for (final JJDataObject jjDataObject : dataList) {
			final JJDataObject data = jjDataObject;

			final Color tmpC = getColor(data);

			if (getSlideValue() < 50) {
				final double tmpV = getSlideValue() * 2 * 2.55;

				if (tmpC.getGreen() < (255 - tmpV))
					continue;
			} else {
				final double tmpV = (getSlideValue() - 50) * 2 * 2.55;

				if (tmpC.getRed() < tmpV)
					continue;
			}

			offGraphics.setColor(tmpC);
			for (int i = 0; i < dimensions; i++) {
				for (int k = 0; k < dimensions; k++) {
					offGraphics.fillRect((int) (boxWidth * i + (boxWidth - 6) * getAt(data, i) + 1),
							(int) (boxHeight * k + (boxHeight - 6) * getAt(data, k) + 1), 4, 4);

				}
			}
		}
		g.drawImage(offscreen, rect.x, rect.y, null);
	}

}
