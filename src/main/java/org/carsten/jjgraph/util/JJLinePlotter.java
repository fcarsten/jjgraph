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
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

/**
 * JJGraphWindowImpl.java
 *
 *
 * Created: Thu May 04 09:46:51 2000
 *
 * @author
 * @version
 */
import javax.swing.JPanel;

public class JJLinePlotter extends JPanel {

	private JJLinePlotterFrame fenster;

	private Image offscreen;

	private Dimension offscreensize;
	private Graphics2D offGraphics;

	private java.util.List<JJLineObject> lineList;

	Rectangle2D.Double bbox;
	Rectangle2D.Double visiBox;

	public void add(final JJLineObject l) {
		lineList.add(l);
		repaint();
	}

	public JJLinePlotter(final java.util.List<JJLineObject> ll) {

		lineList = ll;
		if (ll.isEmpty())
			return;

		JJLineObject l = ll.get(0);
		Rectangle2D r = l.getBounds2D();
		bbox = new Rectangle2D.Double(r.getX(), r.getY(), Math.max(r.getWidth(), 1), Math.max(r.getHeight(), 1));

		for (final Iterator<JJLineObject> iter = ll.iterator(); iter.hasNext();) {
			l = iter.next();
			r = l.getBounds2D();

			bbox.add(new Rectangle2D.Double(r.getX(), r.getY(), Math.max(r.getWidth(), 1), Math.max(r.getHeight(), 1)));
		}

		visiBox = new Rectangle2D.Double();
		visiBox.setRect(bbox);

		Debug.println("BBox: " + bbox);

		fenster = new JJLinePlotterFrame(this);
		fenster.initComponents();

		final Drager d = new Drager();
		addMouseListener(d);
		addMouseMotionListener(d);

	}

	double offsetX;
	double offsetY;

	double scale;

	@Override
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);

		final Rectangle visiRect = getVisibleRect();

		// Debug.println("Visible Rect: " + visiRect);

		final int opacity = fenster.opacitySlider.getValue();

		double scaleX = 1;
		if (visiRect.getWidth() * visiBox.getWidth() != 0)
			scaleX = visiRect.getWidth() / visiBox.getWidth();
		double scaleY = 1;

		if (visiRect.getHeight() * visiBox.getHeight() != 0)
			scaleY = visiRect.getHeight() / visiBox.getHeight();

		offsetX = -visiBox.getX() + visiRect.getX() / 2.0;
		offsetY = -visiBox.getY() + visiRect.getY() / 2.0;
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

		for (final Object element : lineList) {
			final JJLineObject line = (JJLineObject) element;
			line.draw(offGraphics, opacity);
		}
		g.drawImage(offscreen, rect.x, rect.y, null);
	}

	class Drager extends MouseAdapter implements MouseMotionListener {
		double dragStartX;
		double dragStartY;
		boolean dragging = false;

		@Override
		public void mouseDragged(final MouseEvent e) {
			dragging = true;
		}

		@Override
		public void mousePressed(final MouseEvent e) {
			if (e.getClickCount() == 2) {
				visiBox.setRect(bbox);
				repaint();
			} else {
				dragStartX = e.getX();
				dragStartY = e.getY();
			}
		}

		@Override
		public void mouseReleased(final MouseEvent e) {
			if (dragging == true) {
				Debug.println("Draged from " + dragStartX + "," + dragStartY + " to " + e.getX() + "," + e.getY());
			}
			dragging = false;
			double minX = Math.min(dragStartX, e.getX());
			double minY = Math.min(dragStartY, e.getY());

			if ((Math.abs(dragStartX - e.getX()) > 10) && (Math.abs(dragStartY - e.getY()) > 10)) {
				minX = minX / scale;// - offsetX;
				minY = minY / scale;// - offsetY;
				final double w = Math.abs(dragStartX - e.getX()) / scale;
				final double h = Math.abs(dragStartY - e.getY()) / scale;

				visiBox.setRect(visiBox.getX() + minX, visiBox.getY() + minY, w, h);
				repaint();
			}
		}

		@Override
		public void mouseMoved(final java.awt.event.MouseEvent e) {
		}

	}

}
