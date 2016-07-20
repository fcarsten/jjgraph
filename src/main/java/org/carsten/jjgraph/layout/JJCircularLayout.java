/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.layout;

import java.awt.Rectangle;
/**
 * JJCircularLayout.java
 *
 *
 * Created: Wed Dec  8 11:15:52 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import java.util.Collection;

import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJNode;
import org.carsten.jjgraph.util.GFMatrix;
import org.carsten.jjgraph.util.JJPoint;

public class JJCircularLayout implements JJLayout {
	private final JJGraphWindow fenster;
	private final JJGraph graph;

	double xOffset;
	double yOffset;
	double width;
	double height;

	@Override
	public int allowsOptimize() {
		return 0;// JJLayOpt.TRANSLATE | JJLayOpt.SCALE;
	}

	/**
	 * Get the value of xOffset.
	 *
	 * @return Value of xOffset.
	 */
	public double getXOffset() {
		return xOffset;
	}

	/**
	 * Set the value of xOffset.
	 *
	 * @param v
	 *            Value to assign to xOffset.
	 */
	public void setXOffset(final double v) {
		this.xOffset = v;
	}

	/**
	 * Get the value of yOffset.
	 *
	 * @return Value of yOffset.
	 */
	public double getYOffset() {
		return yOffset;
	}

	/**
	 * Set the value of yOffset.
	 *
	 * @param v
	 *            Value to assign to yOffset.
	 */
	public void setYOffset(final double v) {
		this.yOffset = v;
	}

	/**
	 * Get the value of width.
	 *
	 * @return Value of width.
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * Set the value of width.
	 *
	 * @param v
	 *            Value to assign to width.
	 */
	public void setWidth(final double v) {
		this.width = v;
	}

	/**
	 * Get the value of height.
	 *
	 * @return Value of height.
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * Set the value of height.
	 *
	 * @param v
	 *            Value to assign to height.
	 */
	public void setHeight(final double v) {
		this.height = v;
	}

	public JJCircularLayout(final JJGraphWindow f) {
		fenster = f;
		graph = f.getGraph();
	}

	@Override
	public void layout() {
		layout(graph.getNodes());
	}

	@Override
	public void layout(final Collection<JJNode> c) {
		fenster.removeBends();

		final Rectangle r = fenster.getVisibleRect();
		setXOffset(r.getX());
		setYOffset(r.getY());
		setWidth(r.getWidth() / fenster.getZoom());
		setHeight(r.getHeight() / fenster.getZoom());

		final double radius = Math.min(width, height) / 2.0;

		final double deltaA = 360.0 / c.size();
		double alpha = 0;
		final double m[] = new double[6];

		for (final JJNode element : c) {
			final JJNode tmpN = element;
			GFMatrix.rotatematrix(alpha, m);
			// m[4]= xOffset;
			// m[5]= yOffset;

			alpha += deltaA;
			final JJPoint dest = GFMatrix.transform(radius, 0, m);

			fenster.moveNodeTo(tmpN.getGraphicNode(fenster), dest);
		}
	}

	@Override
	public String getName() {
		return "Circular layout";

	}

	@Override
	public void apply(final String s) {
		layout();
	}

	@Override
	public boolean canDo(final String s) {
		return s.equals(getName());

	}

} // JJCircularLayout
