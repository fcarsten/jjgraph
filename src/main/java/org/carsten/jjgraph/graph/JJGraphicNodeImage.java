/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.net.URL;

import org.carsten.jjgraph.util.Debug;

public class JJGraphicNodeImage extends JJGraphicNodeDefault implements ImageObserver {
	private Image image = null;
	private URL imageURL = null;

	public void setImageURL(final URL f) {
		imageURL = f;
	}

	public URL getImageURL() {
		return imageURL;
	}

	@Override
	public void init(final JJGraphicNodeImpl n) {
		super.init(n);
		imageURL = n.window().getMasterImageURL();

		knoten.outlineWidth = 0;
		setImage(n.window().getMasterImage());
	}

	@Override
	synchronized public boolean imageUpdate(final Image img, final int infoflags, final int x, final int y,
			final int width, final int height) {
		// Debug.println("Got image update");
		//
		if ((infoflags & ImageObserver.WIDTH) != 0)
			super.setWidth(width);

		if ((infoflags & ImageObserver.HEIGHT) != 0)
			super.setHeight(height);

		if ((infoflags & (ImageObserver.ABORT | ImageObserver.ERROR)) != 0) {
			Debug.println("Image load problem " + imageURL);
			return true;
		}

		knoten.window().repaint(knoten);
		return ((infoflags & ImageObserver.ALLBITS) != 0);
	}

	public void setImage(final Image i) {
		image = i;

		if (image != null) {
			Toolkit.getDefaultToolkit().prepareImage(image, -1, -1, this);
			super.setWidth(image.getWidth(this));
			super.setHeight(image.getHeight(this));
		}
	}

	public Image getImage() {
		return image;
	}

	@Override
	public void paint(final Graphics2D g) {
		final JJGraphWindow fenster = knoten.window();

		if (!g.getFontMetrics().equals(knoten.getFontMetrics())) {
			knoten.setFontMetrics(g.getFontMetrics());
		}

		int x = (int) getX();
		int y = (int) getY();

		if (fenster != null) {
			x = (int) fenster.deviceAdjustX(getX());
			y = (int) fenster.deviceAdjustY(getY());
		}

		final String name = knoten.getDisplayedName();

		final int w = knoten.getWidth();
		final int h = knoten.getHeight();

		if (image != null)
			g.drawImage(image, x - w / 2, y - h / 2, fenster);// knoten.window());

		if (knoten.isSelected())
			g.setColor(Color.red);
		else {
			g.setColor(Color.black);
		}

		g.drawRect(x - w / 2, y - h / 2, Math.max(1, w - 1), Math.max(1, h - 1));

		if ((!name.equals("")) && (knoten.getLabelPosition() == JJGraphWindow.LABEL_INSIDE)) {
			g.drawString(name, (x - (w - JJGraphicNodeImpl.X_NODE_BORDER) / 2), // +
																				// outlineOffset/2,
					(y - (knoten.labelHeight - JJGraphicNodeImpl.Y_NODE_BORDER) / 2) + // outlineOffset/2
																						// +
							knoten.getFontMetrics().getAscent());
		}
	}
}
