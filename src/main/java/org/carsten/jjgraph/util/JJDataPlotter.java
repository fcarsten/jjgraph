/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
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

public class JJDataPlotter extends JPanel {

	protected JJDataPlotterFrame fenster;

	protected Image offscreen;

	protected Dimension offscreensize;
	protected Graphics2D offGraphics;

	protected java.util.List<JJDataObject> dataList;
	protected int dimensions = 0;

	// public void add(JJDataObject l)
	// {
	// dataList.add(l);
	// repaint();
	// }

	double min[];
	double max[];
	double minC = Double.NaN;
	double maxC = Double.NaN;

	static Color colors[];
	private double slideValue = 50;

	/**
	 * Get the value of slideValue.
	 *
	 * @return value of slideValue.
	 */
	public double getSlideValue() {
		return slideValue;
	}

	/**
	 * Set the value of slideValue.
	 *
	 * @param v
	 *            Value to assign to slideValue.
	 */
	public void setSlideValue(final double v) {
		this.slideValue = v;
	}

	static {
		colors = new Color[256];
		for (int i = 0; i < 256; i++) {
			colors[i] = new Color(255 - i, i, 0, 100);
			// colors[i] = new Color(0, 0, 255, i);
		}

	}

	protected Color getColor(final JJDataObject data) {
		double c = data.getColor();
		c -= minC;
		if (minC == maxC)
			c = 0.5;
		else
			c /= (maxC - minC);

		final int ind = (int) (c * 255);
		return colors[ind];

	}

	protected void initData() {
		if (dataList.isEmpty())
			return;

		JJDataObject obj = dataList.get(0);
		dimensions = obj.getDimension();
		min = new double[dimensions];
		max = new double[dimensions];

		for (int i = 0; i < dimensions; i++) {
			min[i] = Double.NaN;
			max[i] = Double.NaN;
		}

		int k = 0;

		for (final Iterator<JJDataObject> iter = dataList.iterator(); iter.hasNext();) {
			obj = iter.next();
			k++;

			for (int i = 0; i < dimensions; i++) {
				final double tmp = obj.getAt(i);
				if (Double.isNaN(min[i]) || (tmp < min[i]))
					min[i] = tmp;
				if (Double.isNaN(max[i]) || (tmp > max[i]))
					max[i] = tmp;
			}
			final double tmp = obj.getColor();

			if (Double.isNaN(minC) || (tmp < minC))
				minC = tmp;
			if (Double.isNaN(maxC) || (tmp > maxC))
				maxC = tmp;

		}
		// Debug.println("Datasize: "+k);

		// for(int i=0;i<min.length; i++){
		// Debug.println(" min " + i + " is " +min[i]);
		// Debug.println(" max " + i + " is " +max[i]);
		// }

	}

	protected double getAt(final JJDataObject obj, final int i) {
		double res = obj.getAt(i);
		res -= min[i];

		if (max[i] == min[i])
			return 0.5;

		res /= (max[i] - min[i]);

		return res;
	}

	public JJDataPlotter(final java.util.List<JJDataObject> ll) {

		dataList = ll;
		initData();

		fenster = new JJDataPlotterFrame(this);
		fenster.initComponents();

		final Drager d = new Drager();
		addMouseListener(d);
		addMouseMotionListener(d);

	}

	double offsetX = 0;
	double offsetY = 0;

	double scale = 1.0;

	@Override
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);
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
				// visiBox.setRect(bbox);
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

				// visiBox.setRect(visiBox.getX() + minX, visiBox.getY() +
				// minY,w,h);
				repaint();
			}
		}

		@Override
		public void mouseMoved(final java.awt.event.MouseEvent e) {
		}

	}

}
