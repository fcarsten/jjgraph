/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.lang.reflect.Method;
/**
 * JJGraphicEdgeImpl.java
 *
 *
 * Created: Wed Feb 16 16:32:29 2000
 *
 * @author $Author: carsten $
 * @version $Revision: 1.7 $ $Date: 2003/02/09 05:59:49 $
 *
 * $Log: JJGraphicEdgeImpl.java,v $
 * Revision 1.7  2003/02/09 05:59:49  carsten
 * Visibility change is an appearnce event now
 *
 * Revision 1.6  2003/02/04 00:22:33  carsten
 * Changed _isVisible to isVisible
 *
 * Revision 1.5  2002/09/20 03:45:46  carsten
 * A JJ3DGraph can now be attached to a JJGraphWindow and update
 * when nodes/edges change. Can't delete as yet and is very slow
 *
 * Revision 1.4  2002/09/06 04:47:59  carsten
 * changed permissions from private to protected
 *
 * Revision 1.3  2002/08/02 07:33:08  carsten
 * getForeground() used when drawing edge labels
 *
 * Revision 1.2  2002/07/31 01:29:39  carsten
 * Added CVS Header
 *
 */
import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import org.carsten.jjgraph.animation.JJAnimatedShape;
import org.carsten.jjgraph.util.Debug;
import org.carsten.jjgraph.util.JJPoint;
import org.carsten.jjgraph.util.JJUndo;

public class JJGraphicEdgeImpl implements JJGraphicEdge {
	protected JJEdge edge = null;
	protected JJGraphWindow window = null;

	protected BitSet visible = new BitSet();

	final static double DEG_TO_RAD = 0.017453293;// (2.0 * Math.PI) / 360.0;
	protected java.util.List<JJPoint> bends = null;

	protected boolean selected;
	protected double labelWidth = -1;
	protected double labelHeight = -1;
	protected Color color = Color.black;
	protected Rectangle bounds;
	protected boolean boundsInvalid = true;

	protected int fadePol = JJAnimatedShape.DURING;

	@Override
	public JJGraphWindow getWindow() {
		return window;
	}

	@Override
	public void repaint() {
		if (window != null)
			window.repaint(this);
	}

	/**
	 * Get the value of fadePol.
	 *
	 * @return value of fadePol.
	 */
	@Override
	public int getFadePolicy() {
		return fadePol;
	}

	/**
	 * Set the value of fadePol.
	 *
	 * @param v
	 *            Value to assign to fadePol.
	 */
	@Override
	public void setFadePolicy(final int v) {
		this.fadePol = v;
	}

	protected JJGraph graph() {
		return edge.getGraph();
	}

	@Override
	public void revertBends() {
		if (bends != null) {
			Collections.reverse(bends);
		}
	}

	@Override
	public int getNumBends() {
		if (bends == null)
			return 0;
		return bends.size();

	}

	@Override
	public void bendsToNodes() {
		final JJGraph graph = graph();

		if ((bends == null) || (bends.size() == 0))
			return;

		graph.getUndoManager().openSubtask("Bends to Nodes");

		final JJNode tmpNodes[] = new JJNode[bends.size() + 2];
		tmpNodes[0] = edge.getSource();
		tmpNodes[bends.size() + 1] = edge.getTarget();
		for (int i = 1; i < tmpNodes.length; i++) {
			if (i < tmpNodes.length - 1) {
				tmpNodes[i] = graph.addNode();
				final JJGraphicNode gn = tmpNodes[i].getGraphicNode(window);
				window.moveNodeTo(gn, bends.get(i - 1));
			}

			final JJEdge tmpE = graph.addEdge(tmpNodes[i - 1], tmpNodes[i]);

			final JJGraphicEdge ge = tmpE.getGraphicEdge(window);
			ge.setColor(getColor());

			tmpE.setWeight(getWeight());
			tmpE.setName(edge.getName());
			// tmpE.setTransparency(edge.getTransparency());
		}
		graph.deleteEdge(edge);
		graph.getUndoManager().closeSubtask("Bends to Nodes");
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

			undo.add("Change edge color", this, set_color, par);
		}

		this.color = v;
		window.sendEdgeColourChangeEvent(this);

		_repaint();
	}

	public void _repaint() {
		if (window() != null)
			window().repaint(this);
	}

	protected FontMetrics fontMetrics = null;

	/**
	 * Get the value of fontMetrics.
	 *
	 * @return Value of fontMetrics.
	 */
	// public FontMetrics getFontMetrics() {return fontMetrics;}

	// /**
	// * Set the value of fontMetrics.
	// * @param v Value to assign to fontMetrics.
	// */
	public void setFontMetrics(final FontMetrics v) {
		if (this.fontMetrics != v) {
			this.fontMetrics = v;
			recomputeEdgeSize();
		}
	}

	@Override
	public void recomputeEdgeSize() {
		boundsInvalid = true;
	}

	protected JJGraphWindow window() {
		return window;
	}

	/**
	 * Get the value of edge.
	 *
	 * @return Value of edge.
	 */
	@Override
	public JJEdge getEdge() {
		return edge;
	}

	/**
	 * Set the value of edge.
	 *
	 * @param v
	 *            Value to assign to edge.
	 */
	@Override
	public void setEdge(final JJEdge v) {
		this.edge = v;
	}

	/**
	 * Get the value of visible.
	 *
	 * @return Value of visible.
	 */
	@Override
	public BitSet getVisible() {
		// BitSet bs= new BitSet();
		// bs.or(visible);
		// bs.or(edge.getSource().getGraphicNode().getVisible());
		// bs.or(edge.getTarget().getGraphicNode().getVisible());

		// return bs;
		return visible;
	}

	@Override
	public boolean isVisible() {
		return visible.isEmpty();
	}

	@Override
	public void hide() {
		final JJGraphWindow w = window();
		if (w instanceof JJGraphWindowImpl) {
			setVisible(((JJGraphWindowImpl) w).HIDDEN, true);
		}
	}

	@Override
	public void unhide() {
		final JJGraphWindow w = window();
		if (w instanceof JJGraphWindowImpl) {
			setVisible(((JJGraphWindowImpl) w).HIDDEN, false);
		}
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
			if (window() != null)
				window().repaint(this);
			this.visible.set(v, b);
			if (window() != null) {
				window().repaint(this);
				window.sendEdgeVisibilityChangeEvent(this);
			}
		}
	}

	/**
	 * Get the value of selected.
	 *
	 * @return Value of selected.
	 */
	@Override
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Set the value of selected.
	 *
	 * @param v
	 *            Value to assign to selected.
	 */
	@Override
	public void setSelected(final boolean v) {
		this.selected = v;
	}

	// double len;
	public double getWeight() {
		return edge.getWeight();
	}

	@Override
	public void addBendAt(final int i, final JJPoint p) {
		if (i == 0) {
			addBendFirst(p);
		}

		if (bends == null) {
			throw new IndexOutOfBoundsException("Index " + i);
		}

		if (edge.getGraph().getUndoRecording()) {
			final Object par[] = { new Integer(i) };
			undo().add("Add bend at " + i, this, remove_bend_at, par);
		}

		bends.add(i, p);
		recomputeEdgeSize();
	}

	@Override
	public void moveBendTo(final int i, final double x, final double y) {
		final JJPoint p = bends.get(i);

		if (edge.getGraph().getUndoRecording()) {
			final Object par[] = { new Integer(i), new Double(p.x), new Double(p.y) };
			undo().add("Move bend ", this, move_bend_to, par);
		}

		p.x = x;
		p.y = y;
		recomputeEdgeSize();
	}

	@Override
	public void addBendFirst(final JJPoint p) {
		if (bends == null) {
			bends = new LinkedList<>();
		}

		if (edge.getGraph().getUndoRecording()) {
			undo().add("Add bend first", this, remove_bend_first, null);
		}
		bends.add(0, p);
		recomputeEdgeSize();
	}

	@Override
	public void rmoveTo(final double x, final double y) {
		if (bends == null)
			return;

		for (final Iterator<JJPoint> iter2 = getBends().listIterator(); iter2.hasNext();) {
			final JJPoint p = iter2.next();
			p.setX(p.getX() + x);
			p.setY(p.getY() + y);
		}

		if (edge.getGraph().getUndoRecording()) {
			createUndoMoveEventR(-x, -y);
		}
		recomputeEdgeSize();
	}

	@Override
	public void createUndoMoveEventR(final double x, final double y) {
		if ((bends == null) || (bends.size() == 0)) {
			return;
		}

		final Object par[] = { this, new JJPoint(x, y) };
		undo().add("Move Edge", window, rmove_edge_to, par);
	}

	@Override
	public void addBendLast(final JJPoint p) {
		if (bends == null) {
			bends = new LinkedList<>();
		}

		bends.add(bends.size(), p);

		if (edge.getGraph().getUndoRecording()) {
			undo().add("Add bend last", this, remove_bend_last, null);
		}
		recomputeEdgeSize();
	}

	protected JJUndo undo() {
		return edge.getGraph().getUndoManager();
	}

	@Override
	public void removeBends() {

		if ((bends != null) && edge.getGraph().getUndoRecording()) {
			final Object par[] = { bends };
			undo().add("Remove bends", this, set_bends, par);
		}

		bends = null;
		recomputeEdgeSize();
	}

	@Override
	public java.util.List<JJPoint> getBends() {
		return bends;
	}

	@Override
	public void init(final JJGraphWindow w, final JJEdge e, final Color c) {
		window = w;
		edge = e;
		edge.setGraphicEdge(this, w);
		color = c;

		// setFontMetrics(fm);
	}

	@Override
	public double getLabelX() {
		if (bends != null) {
			return bends.get(0).x;
		}

		final double x1 = edge.getSource().getGraphicNode(window).getX();
		final double x2 = edge.getTarget().getGraphicNode(window).getX();

		return (x2 + x1) / 2;
	}

	@Override
	public double getLabelY() {
		if (bends != null) {
			return bends.get(0).y;
		}

		final double y1 = edge.getSource().getGraphicNode(window).getY();
		final double y2 = edge.getTarget().getGraphicNode(window).getY();

		return (y2 + y1) / 2;
	}

	// public void setLabelWidth(double x)
	// {
	// width = x;
	// }

	// public void setLabelHeight(double x)
	// {
	// height = x;
	// }

	@Override
	public void removeBendFirst() {
		if (bends.size() == 1)
			bends = null;
		else
			bends.remove(0);

		recomputeEdgeSize();
	}

	@Override
	public void removeBendLast() {
		if (edge.getGraph().getUndoRecording()) {
			final Object par[] = { bends.get(bends.size() - 1) };
			undo().add("Remove last bend", this, add_bend_last, par);
		}

		if (bends.size() == 1)
			bends = null;
		else
			bends.remove(bends.size() - 1);
		recomputeEdgeSize();
	}

	@Override
	public void removeBendAt(final int i) {
		if (edge.getGraph().getUndoRecording()) {
			final Object par[] = { new Integer(i), bends.get(i) };
			undo().add("Remove bend " + i, this, add_bend_at, par);
		}

		bends.remove(i);

		if (bends.size() == 0)
			bends = null;
		recomputeEdgeSize();
	}

	@Override
	public void setBends(final java.util.List<JJPoint> l) {
		if ((bends == null) || (bends.size() < 2))
			return;

		if (edge.getGraph().getUndoRecording()) {
			final Object par[] = { bends };
			undo().add("Set bends", this, add_bend_at, par);
		}

		bends = l;
		recomputeEdgeSize();
	}

	@Override
	public Rectangle getBounds() {
		// if(boundsInvalid){
		final int x1 = (int) edge.getSource().getGraphicNode(window).getX();
		final int y1 = (int) edge.getSource().getGraphicNode(window).getY();
		final int x2 = (int) edge.getTarget().getGraphicNode(window).getX();
		final int y2 = (int) edge.getTarget().getGraphicNode(window).getY();

		bounds = new Rectangle(x1, y1, 0, 0);
		bounds.add(x2, y2);

		if (bends != null) {
			for (final Object element : bends) {
				final JJPoint x = (JJPoint) element;
				bounds.add((int) x.x, (int) x.y);
			}
		}

		bounds.x = (int) window.deviceAdjustX(bounds.x);
		bounds.y = (int) window.deviceAdjustY(bounds.y);
		bounds.width = (int) window.deviceAdjustWidth(bounds.width);
		bounds.height = (int) window.deviceAdjustHeight(bounds.height);

		if (fontMetrics != null) {
			labelWidth = fontMetrics.stringWidth(getDisplayedName());
			labelHeight = fontMetrics.getHeight();
			bounds.add(new Rectangle((int) window.deviceAdjustX(getLabelX() - 5),
					(int) window.deviceAdjustY(getLabelY() - 5), (int) labelWidth + 10, (int) labelHeight + 10));
		}

		// }

		return bounds;
	}

	static Method rmove_edge_to;
	static Method add_bend_first;
	static Method add_bend_last;
	static Method add_bend_at;
	static Method remove_bend_at;
	static Method remove_bend_first;
	static Method remove_bend_last;
	static Method remove_bends;
	static Method set_bends;
	static Method move_bend_to;
	static protected Method set_color;

	static {
		final Class<JJGraphWindow> jjgraphwindow = org.carsten.jjgraph.graph.JJGraphWindow.class;
		final Class<JJGraphicEdge> jjgraphicedge = org.carsten.jjgraph.graph.JJGraphicEdge.class;
		final Class<JJPoint> jjpoint = org.carsten.jjgraph.util.JJPoint.class;
		try {
			{
				final Class<?> parT[] = new Class[2];
				parT[0] = jjgraphicedge;
				parT[1] = jjpoint;
				rmove_edge_to = jjgraphwindow.getMethod("rmoveEdgeTo", parT);
			}
			{
				remove_bend_first = jjgraphicedge.getMethod("removeBendFirst", (Class<?>[]) null);
				remove_bend_last = jjgraphicedge.getMethod("removeBendLast", (Class<?>[]) null);
				remove_bends = jjgraphicedge.getMethod("removeBends", (Class<?>[]) null);
			}
			{
				final Class<?> parT[] = new Class[2];
				parT[0] = int.class;
				parT[1] = jjpoint;
				add_bend_at = jjgraphicedge.getMethod("addBendAt", parT);
			}
			{
				final Class<?> parT[] = new Class[1];
				parT[0] = jjpoint;
				add_bend_first = jjgraphicedge.getMethod("addBendFirst", parT);
				add_bend_last = jjgraphicedge.getMethod("addBendLast", parT);
			}
			{
				final Class<?> parT[] = new Class[1];
				parT[0] = int.class;
				remove_bend_at = jjgraphicedge.getMethod("removeBendAt", parT);
			}
			{
				final Class<?> parT[] = new Class[1];
				parT[0] = java.util.List.class;
				set_bends = jjgraphicedge.getMethod("setBends", parT);
			}
			{
				final Class<?> parT[] = new Class[3];
				parT[0] = int.class;
				parT[1] = double.class;
				parT[2] = double.class;
				move_bend_to = jjgraphicedge.getMethod("moveBendTo", parT);
			}
			{
				final Class<?> parT[] = new Class[1];
				parT[0] = Color.class;
				set_color = jjgraphicedge.getMethod("setColor", parT);
			}

		} catch (final java.lang.NoSuchMethodException e) {
			Debug.println("Method not found: " + e.getMessage() + " :");
		}
	}

	@Override
	public void paint(final Graphics2D g) {
		final JJGraphicNode graphicSource = getEdge().getSource().getGraphicNode(window);
		final JJGraphicNode graphicTarget = getEdge().getTarget().getGraphicNode(window);

		if (!g.getFontMetrics().equals(fontMetrics)) {
			// Debug.println("Font metrics change");
			setFontMetrics(g.getFontMetrics());
		}

		boolean drawSplines = false;

		if (window instanceof JJGraphWindowImpl) {
			drawSplines = window.isDrawSplines();
		}

		double x1 = graphicSource.getX();
		double y1 = graphicSource.getY();
		double x2 = graphicTarget.getX();
		double y2 = graphicTarget.getY();

		final String name = getDisplayedName();

		if (window != null) {
			x1 = window.deviceAdjustX(x1);
			y1 = window.deviceAdjustY(y1);
			x2 = window.deviceAdjustX(x2);
			y2 = window.deviceAdjustY(y2);

			// name = window.adjustLabel(name);
		}

		// JJPoint newEnd = new JJPoint(x2,y2);

		g.setColor(getColor());

		final Stroke tmpS = g.getStroke();

		if (getEdge().getWeight() > 1.0) {
			g.setStroke(new BasicStroke((float) Math.min(getEdge().getWeight(), 10)));
		}

		if (isSelected())
			g.setColor(Color.red);

		final int numPoints = getNumBends() + 2;

		double pos[][] = new double[numPoints][2];
		pos[0][0] = x1;
		pos[0][1] = y1;
		pos[numPoints - 1][0] = x2;
		pos[numPoints - 1][1] = y2;

		if (getBends() != null) {
			int k = 1;

			for (final Iterator<JJPoint> iter2 = getBends().listIterator(); iter2.hasNext();) {
				final JJPoint p = iter2.next();

				double tmpX = p.getX();
				double tmpY = p.getY();
				if (window != null) {
					tmpX = window.deviceAdjustX(p.getX());
					tmpY = window.deviceAdjustY(p.getY());
				}

				pos[k][0] = tmpX;
				pos[k][1] = tmpY;
				k++;
			}
		}

		final JJPoint newStart = graphicSource.adjustLineEnd(new JJPoint(pos[1][0], pos[1][1]));
		pos[0][0] = newStart.x;
		pos[0][1] = newStart.y;

		final JJPoint tmpP = new JJPoint(pos[pos.length - 2][0], pos[pos.length - 2][1]);
		final JJPoint newEnd = graphicTarget.adjustLineEnd(tmpP);
		pos[pos.length - 1][0] = newEnd.x;
		pos[pos.length - 1][1] = newEnd.y;

		if ((pos.length > 2) && drawSplines)
			pos = calculateSpline(pos);

		for (int k = 1; k < pos.length; k++) {
			if ((pos[k - 1][0] - pos[k - 1][1]) * (pos[k - 1][0] - pos[k - 1][1])
					+ (pos[k][0] - pos[k][1]) * (pos[k][0] - pos[k][1]) > 30000 * 30000) {
				window.printWarning("Some edges not drawn to workaround Java bug");
			} else {
				// System.out.println("********** " + pos[k][0] + ", " +
				// pos[k][1]);
				g.drawLine((int) (pos[k - 1][0]), (int) (pos[k - 1][1]), (int) (pos[k][0]), (int) (pos[k][1]));
			}
		}

		if (graph().isDirected())
			drawArrowHead(g, (int) (pos[pos.length - 2][0]), (int) (pos[pos.length - 2][1]),
					(int) (pos[pos.length - 1][0]), (int) (pos[pos.length - 1][1]));

		if (getEdge().getWeight() > 1.0) {
			g.setStroke(tmpS);
		}

		if ((!name.equals("")) && (fontMetrics != null)) {
			if (getColor().getAlpha() == 255)
				g.setColor(window.getForeground());
			else {
				final Color tmpC = window.getForeground();

				g.setColor(new Color(tmpC.getRed(), tmpC.getGreen(), tmpC.getBlue(), getColor().getAlpha()));
			}

			if ((window != null) && (window.getEdgeLabelPosition() != JJGraphWindow.LABEL_NONE)) {
				g.drawString(name, (int) (window.deviceAdjustX(getLabelX())),
						(int) (window.deviceAdjustY(getLabelY()) + fontMetrics.getAscent()));
			} else {
				g.drawString(name, (int) getLabelX(), (int) (getLabelY() + fontMetrics.getAscent()));
			}
		}
	}

	public double[][] calculateSpline(final double oldPos[][]) {
		double a, b, c, d, e, f, g, h;
		double x0, x1, x2, x3, y0, y1, y2, y3;
		final double thisX, thisY;
		final int steps = 10;
		final int numCurves = oldPos.length / 3;
		if ((oldPos.length <= 3) || (oldPos.length % 3 != 1)) {
			Debug.println("Wrong number of points: " + oldPos.length);
			return oldPos;

		}

		final double points[][] = new double[numCurves * steps + 1][2];
		int i = 0;

		points[i][0] = oldPos[0][0];
		points[i++][1] = oldPos[0][1];

		for (int n = 0; n < numCurves; n++) {
			x0 = oldPos[n * 3][0];
			y0 = oldPos[n * 3][1];
			x1 = oldPos[n * 3 + 1][0];
			y1 = oldPos[n * 3 + 1][1];
			x2 = oldPos[n * 3 + 2][0];
			y2 = oldPos[n * 3 + 2][1];
			x3 = oldPos[n * 3 + 3][0];
			y3 = oldPos[n * 3 + 3][1];
			a = -x0 + 3 * x1 - 3 * x2 + x3;
			b = 3 * x0 - 6 * x1 + 3 * x2;
			c = -3 * x0 + 3 * x1;
			d = x0;
			e = -y0 + 3 * y1 - 3 * y2 + y3;
			f = 3 * y0 - 6 * y1 + 3 * y2;
			g = -3 * y0 + 3 * y1;
			h = y0;
			final double step1 = 1.0f / (double) steps;

			for (double u = step1; u <= 1.001; u += step1) {
				points[i][0] = ((a * u + b) * u + c) * u + d;
				points[i][1] = ((e * u + f) * u + g) * u + h;
				i++;
			}
		}
		return points;
	}

	static public double getAngel(final JJPoint tmpP) {
		final double l = Math.sqrt(tmpP.x * tmpP.x + tmpP.y * tmpP.y);

		if (tmpP.x > 0)
			return -Math.acos(-tmpP.y / l) / DEG_TO_RAD;

		return Math.acos(-tmpP.y / l) / DEG_TO_RAD;
	}

	public void drawArrowHead(final Graphics2D g, final int x1, final int y1, final int x2, final int y2) {
		// Debug.println("Drawing arrow head");

		final JJPoint refP = new JJPoint(x2 - x1, y2 - y1);

		final double fLength = Math.sqrt((refP.x * refP.x + refP.y * refP.y));
		final double fDirection = getAngel(refP);

		final JJPoint xLeft = new JJPoint(0, 0);
		final JJPoint xRight = new JJPoint(0, 0);

		final double fHeadAngle = 25.0;
		final double fLeftAngle = fDirection - fHeadAngle;
		final double fRightAngle = fDirection + fHeadAngle;

		final double iHeadSize = 5.0;

		xLeft.x = x2 + (int) (Math.sin(fLeftAngle * DEG_TO_RAD) * iHeadSize);
		xLeft.y = y2 + (int) (Math.cos(fLeftAngle * DEG_TO_RAD) * iHeadSize);
		xRight.x = x2 + (int) (Math.sin(fRightAngle * DEG_TO_RAD) * iHeadSize);
		xRight.y = y2 + (int) (Math.cos(fRightAngle * DEG_TO_RAD) * iHeadSize);

		g.drawLine(x2, y2, (int) xLeft.x, (int) xLeft.y);
		g.drawLine(x2, y2, (int) xRight.x, (int) xRight.y);
	}

	public String getDisplayedName() {
		if (window() != null)
			return window().adjustLabel(getEdge().getName());
		return getEdge().getName();
	}

	@Override
	public String getName() {
		return getDisplayedName();
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
}
