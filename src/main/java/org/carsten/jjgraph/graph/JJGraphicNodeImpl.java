/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.lang.reflect.Method;
import java.util.BitSet;
import java.util.Iterator;

import org.carsten.jjgraph.animation.JJAnimatedShape;
import org.carsten.jjgraph.util.Debug;
import org.carsten.jjgraph.util.JJPoint;
import org.carsten.jjgraph.util.JJUndo;

public class JJGraphicNodeImpl implements JJGraphicNode {
	protected double x;
	protected double y;
	protected double z;
	public static final int DEFAULT_NODE_WIDTH = 10;
	public static final int DEFAULT_NODE_HEIGHT = 10;

	protected Color color = new Color(200, 200, 200);
	protected JJGraphicNodeAppearance app;

	protected JJNode node;

	protected BitSet visible = new BitSet();
	protected boolean selected = false;

	protected int outlineWidth = 5;
	public final static int X_NODE_BORDER = 10;
	public final static int Y_NODE_BORDER = 4;

	protected int labelWidth = -1;
	protected int labelHeight = -1;

	private int mni = -1;
	private JJGraphWindowImpl window;

	public JJGraphicNodeImpl() {
		app = new JJGraphicNodeDefault(this);
	}

	public int getOutlineWidth() {
		return outlineWidth;
	}

	@Override
	public int getMNI() {
		return mni;
	}

	@Override
	public void setMNI(final int i) {
		mni = i;
	}

	public void setAppearance(final JJGraphicNodeAppearance a) {
		app = a;
	}

	public JJGraphicNodeAppearance getAppearance() {
		return app;
	}

	@Override
	public Iterator<JJEdge> edgeIterator() {
		return node.edgeIterator();
	}

	public int getLabelPosition() {
		if (window() != null)
			return window().getNodeLabelPosition();

		return JJGraphWindow.LABEL_NONE;
	}

	@Override
	public Iterator<JJEdge> outIterator() {
		return node.outIterator();
	}

	public Iterator<JJEdge> inIterator() {
		return node.inIterator();
	}

	@Override
	public JJAnimatedShape opposite(final JJEdge e) {
		if (e.getSource() == node) {
			return e.getTarget().getGraphicNode(window);
		}
		return e.getTarget().getGraphicNode(window);
	}

	/**
	 * Get the value of height.
	 *
	 * @return Value of height.
	 */
	@Override
	public int getHeight() {
		return app.getHeight((window() != null) && window().getOutline());
	}

	/**
	 * Set the value of height.
	 *
	 * @param v
	 *            Value to assign to height.
	 */
	@Override
	public void setHeight(final int v) {
		app.setHeight(v);
	}

	/**
	 * Get the value of width.
	 *
	 * @return Value of width.
	 */
	@Override
	public int getWidth() {
		return app.getWidth((window() != null) && window().getOutline());
	}

	/**
	 * Set the value of width.
	 *
	 * @param v
	 *            Value to assign to width.
	 */
	@Override
	public void setWidth(final int v) {
		app.setWidth(v);
	}

	protected FontMetrics fontMetrics = null;

	/**
	 * Get the value of fontMetrics.
	 *
	 * @return Value of fontMetrics.
	 */
	public FontMetrics getFontMetrics() {
		return fontMetrics;
	}

	// /**
	// * Set the value of fontMetrics.
	// * @param v Value to assign to fontMetrics.
	// */
	public void setFontMetrics(final FontMetrics v) {
		this.fontMetrics = v;
		// Debug.println("Font: "+ v.getFont().getFontName());

		recomputeNodeSize();
	}

	@Override
	public void recomputeNodeSize() {
		if (fontMetrics != null) {
			labelWidth = fontMetrics.stringWidth(getDisplayedName()) + X_NODE_BORDER;
			labelHeight = fontMetrics.getHeight() + Y_NODE_BORDER;
		}
	}

	public String getDisplayedName() {
		if (window() != null)
			return window().adjustLabel(getNode().getName());
		return getNode().getName();
	}

	protected JJGraph graph() {
		return node.getGraph();
	}

	public JJGraphWindowImpl window() {
		return window;
	}

	@Override
	public JJGraphWindow getWindow() {
		return window;
	}

	protected void _repaint() {
		if (window() != null)
			window().repaint(this);
	}

	@Override
	public void hide() {
		setVisible(window().HIDDEN, true);
	}

	@Override
	public void unhide() {
		setVisible(window().HIDDEN, false);
	}

	/**
	 * Get the value of visible.
	 *
	 * @return Value of visible.
	 */
	@Override
	public BitSet getVisible() {
		return visible;
	}

	@Override
	public boolean isVisible() {
		return visible.isEmpty();
	}

	/**
	 * Set the value of visible.
	 *
	 * @param v
	 *            Value to assign to visible.
	 */
	@Override
	public void setVisible(final int v, final boolean b) {
		if (b != this.visible.get(v)) {
			final boolean tmpRedraw = window.setRedraw(false);
			repaint();

			for (final Iterator<JJEdge> iter = getNode().edgeIterator(); iter.hasNext();) {
				final JJEdge tmpE = iter.next();
				final JJGraphicEdge ge = tmpE.getGraphicEdge(window);
				if (ge.isVisible())
					ge.repaint();
			}
			this.visible.set(v, b);
			repaint();
			window.sendNodeVisibilityChangeEvent(this);
			window.setRedraw(tmpRedraw);
		}
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setColor(final Color v) {
		if (v == null)
			return;

		if (graph().getUndoRecording()) {
			final JJUndo undo = graph().getUndoManager();
			final Object par[] = new Object[1];
			par[0] = this.color;

			undo.add("Change node color", this, set_color, par);
		}
		this.color = v;
		window.sendNodeColourChangeEvent(this);

		_repaint();
	}

	@Override
	public boolean getSelected() {
		return selected;
	}

	@Override
	public boolean isSelected() {
		return selected;
	}

	private boolean fixed = false;

	/**
	 * Set the value of fixed.
	 *
	 * @param v
	 *            Value to assign to fixed.
	 */
	@Override
	public void setFixed(final boolean v) {
		this.fixed = v;
	}

	@Override
	public boolean isFixed() {
		return isSelected() || fixed;
	}

	@Override
	public void setSelected(final boolean v) {
		this.selected = v;
	}

	@Override
	public void init(final JJGraphWindow w, final JJNode n, final Color c) // ,
																			// FontMetrics
																			// fm)
	{
		node = n;
		window = (JJGraphWindowImpl) w;

		x = (-200.0 + 400.0 * Math.random());
		y = (-200.0 + 400.0 * Math.random());
		// setFontMetrics(fm);
		this.color = c;

		node.setGraphicNode(this, w);
	}

	@Override
	public JJNode getNode() {
		return node;
	}

	// public void setHeight(double px)
	// {
	// app.setHeight(px);
	// }

	@Override
	public double getX() {
		return x;
	}

	@Override
	public double getY() {
		return y;
	}

	@Override
	public double getZ() {
		return z;
	}

	@Override
	public void moveTo(final JJPoint p) {
		moveTo(p.x, p.y, p.z);
	}

	@Override
	public void moveTo(final double px, final double py) {
		moveTo(px, py, 0.0);
	}

	@Override
	public void moveTo(final double px, final double py, final double pz) {
		if (Double.isNaN(px) || Double.isNaN(py) || Double.isNaN(pz))
			return;

		if ((px == x) && (py == y) && (pz == z))
			return;

		final JJPoint tmpP = null;

		if (graph().getUndoRecording()) {
			createUndoMoveEventTo(new JJPoint(x, y, z));
		}

		x = px;
		y = py;
		z = pz;
		window.sendNodeMoveEvent(this);
	}

	@Override
	public void createUndoMoveEventTo(final JJPoint tmpP) {
		final JJUndo undo = graph().getUndoManager();
		final Object par[] = new Object[2];
		par[0] = this;
		par[1] = tmpP;

		// Debug.println("Adding " + move_node_to );

		undo.add("Move Node", window, move_node_to, par);
	}

	@Override
	public void rmoveTo(final double px, final double py) {
		moveTo(x + px, y + py);
	}

	@Override
	public void rmoveTo(final JJPoint p) {
		moveTo(x + p.x, y + p.y);
	}

	@Override
	public JJPoint getCoords() {
		return new JJPoint(x, y, z);
	}

	static protected Method move_node_to;
	static protected Method set_color;

	static {
		final Class<? extends JJGraphWindow> jjgraphwindow = org.carsten.jjgraph.graph.JJGraphWindow.class;
		final Class<? extends JJGraphicNode> jjgraphicnode = org.carsten.jjgraph.graph.JJGraphicNode.class;
		// Debug.println("Static init");

		try {
			{
				final Class<?> parT[] = new Class[2];
				parT[0] = org.carsten.jjgraph.graph.JJGraphicNode.class;
				parT[1] = org.carsten.jjgraph.util.JJPoint.class;
				move_node_to = jjgraphwindow.getMethod("moveNodeTo", parT);
			}
			{
				final Class<?> parT[] = new Class[1];
				parT[0] = Color.class;
				set_color = jjgraphicnode.getMethod("setColor", parT);
			}
		} catch (final java.lang.NoSuchMethodException e) {
			Debug.println("Method not found: " + e.getMessage() + " :");
		}
	}

	@Override
	public String toString() {
		return node.getName();
	}

	@Override
	public void paint(final Graphics2D g) {
		final JJGraphWindow fenster = window();
		if (!g.getFontMetrics().equals(fontMetrics)) {
			// Debug.println("Font metrics change");

			setFontMetrics(g.getFontMetrics());
		}

		app.paint(g);

	}

	@Override
	public JJPoint adjustLineEnd(final JJPoint B) {
		return app.adjustLineEnd(B);
	}

	@Override
	public int getFadePolicy() {
		return JJAnimatedShape.SEPERATE;
	}

	@Override
	public void setFadePolicy(final int d) {
	}

	@Override
	public String getName() {
		return getDisplayedName();
	}

	@Override
	public void repaint() {
		window.repaint();
	}

	@Override
	public Object getAnimationData() {
		return null;
	}

	@Override
	public void setAnimationStartData(final Object o) {
	}

	@Override
	public void setAnimationEndData(final Object o) {
	}

	@Override
	public void frame(final Object o, final double d) {
	}

	@Override
	public double dist(final JJGraphicNode n) {
		boolean leftRight = false;
		boolean topBottom = false;
		final Rectangle r1 = window().getBounds(this);
		final Rectangle r2 = window().getBounds(n);
		if (r1 == null || r2 == null)
			return Double.POSITIVE_INFINITY;

		final Rectangle unionRect = new Rectangle(r1);
		unionRect.union(r2); // r1|r2;

		if (unionRect.width > r1.width + r2.width)
			leftRight = true;

		final double hu = unionRect.height;
		final double h1 = r1.height;
		final double h2 = r2.height;
		final double r1right = r1.x + r1.width;
		final double r1top = r1.y + r1.height;
		final double r2right = r2.x + r2.width;
		final double r2top = r2.y + r2.height;

		if (hu > h1 + h2)
			topBottom = true;

		if (leftRight && topBottom) {
			double distSqr = (r1.x - r2right) * (r1.x - r2right) + (r1top - r2.y) * (r1top - r2.y);
			distSqr = Math.min(distSqr, (r1right - r2.x) * (r1right - r2.x) + (r1top - r2.y) * (r1top - r2.y));
			distSqr = Math.min(distSqr, (r1right - r2.x) * (r1right - r2.x) + (r1.y - r2top) * (r1.y - r2top));
			distSqr = Math.min(distSqr, (r1.x - r2right) * (r1.x - r2right) + (r1.y - r2top) * (r1.y - r2top));
			return Math.sqrt(distSqr);
		} else if (leftRight) {
			return Math.min(Math.abs(r1.x - r2right), Math.abs(r1right - r2.x));
		} else if (topBottom) {
			return Math.min(Math.abs(r1top - r2.y), Math.abs(r1.y - r2top));
		}

		return -1;
	}

}
