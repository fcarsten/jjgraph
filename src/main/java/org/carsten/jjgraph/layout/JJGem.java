/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.layout;

import java.awt.Rectangle;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJGraphicNode;
import org.carsten.jjgraph.graph.JJNode;
import org.carsten.jjgraph.layopt.JJLayOpt;
import org.carsten.jjgraph.util.Debug;
import org.carsten.jjgraph.util.JJPoint;

public class JJGem implements JJLayout {

	protected JJGraphWindow fenster;
	protected JJGraph graph;

	protected JJGemNode gemNodes[];
	protected JJNode graphNodes[];

	protected int numNodes = 0;
	protected JJPoint bc;

	protected double temperature = 0.0;
	protected boolean layoutInvisible = false;

	// protected boolean suspended = false;
	protected double radius = Double.NaN;
	private Rectangle bounds = null;

	/**
	 * Get the value of bounds.
	 *
	 * @return value of bounds.
	 */
	public Rectangle getBounds() {
		return bounds;
	}

	/**
	 * Set the value of bounds.
	 *
	 * @param v
	 *            Value to assign to bounds.
	 */
	public void setBounds(final Rectangle v) {
		this.bounds = v;
	}

	/**
	 * Get the value of radius.
	 *
	 * @return value of radius.
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * Set the value of radius.
	 *
	 * @param v
	 *            Value to assign to radius.
	 */
	public void setRadius(final double v) {
		this.radius = v;
	}

	@Override
	public int allowsOptimize() {
		return JJLayOpt.TRANSLATE | JJLayOpt.SCALE_PROPORTIONAL | JJLayOpt.FLIP | JJLayOpt.ROTATE;
	}

	final static protected double STARTTEMP = 1.0;
	final static protected double MAXTEMP = 1.5;
	final static protected double OSCILLATION = 0.4;
	final static protected double ROTATION = 0.9;
	final static protected double SHAKE = 0.3;

	final static double SLEEP_TIME = 50;

	protected double finalTemp = 0.02;
	protected double maxIter = 3;
	protected double gravity = 0.15;
	protected double edgeLength = 128;

	protected double unitMass = 1;
	protected boolean useEdgeWeight = true;

	/**
	 * Get the value of useEdgeWeight.
	 *
	 * @return Value of useEdgeWeight.
	 */
	public boolean getUseEdgeWeight() {
		return useEdgeWeight;
	}

	/**
	 * Set the value of useEdgeWeight.
	 *
	 * @param v
	 *            Value to assign to useEdgeWeight.
	 */
	public void setUseEdgeWeight(final boolean v) {
		this.useEdgeWeight = v;
	}

	/**
	 * Get the value of unitMass.
	 *
	 * @return Value of unitMass.
	 */
	public double getUnitMass() {
		return unitMass;
	}

	/**
	 * Set the value of unitMass.
	 *
	 * @param v
	 *            Value to assign to unitMass.
	 */
	public void setUnitMass(final double v) {
		this.unitMass = v;
	}

	protected double elenSqr = 128 * 128;
	protected double maxtemp = MAXTEMP * 128;
	protected double maxAttract = 128 * 128 * 64; // 1048576;

	/**
	 * Get the value of gravity.
	 *
	 * @return Value of gravity.
	 */
	public double getGravity() {
		return gravity;
	}

	/**
	 * Set the value of gravity.
	 *
	 * @param v
	 *            Value to assign to gravity.
	 */
	public void setGravity(final double v) {
		this.gravity = v;
	}

	/**
	 * Get the value of maxIter.
	 *
	 * @return Value of maxIter.
	 */
	public double getMaxIter() {
		return maxIter;
	}

	/**
	 * Set the value of maxIter.
	 *
	 * @param v
	 *            Value to assign to maxIter.
	 */
	public void setMaxIter(final double v) {
		this.maxIter = v;
	}

	/**
	 * Get the value of finalTemp.
	 *
	 * @return Value of finalTemp.
	 */
	public double getFinalTemp() {
		return finalTemp;
	}

	/**
	 * Set the value of finalTemp.
	 *
	 * @param v
	 *            Value to assign to finalTemp.
	 */
	public void setFinalTemp(final double v) {
		this.finalTemp = v;
	}

	/**
	 * Get the value of edgeLength.
	 *
	 * @return Value of edgeLength.
	 */
	public double getEdgeLength() {
		return edgeLength;
	}

	/**
	 * Set the value of edgeLength.
	 *
	 * @param v
	 *            Value to assign to edgeLength.
	 */
	public void setEdgeLength(final double v) {
		this.edgeLength = v;
		elenSqr = edgeLength * edgeLength;
		maxtemp = MAXTEMP * edgeLength;
		maxAttract = elenSqr * 64;
	}

	public boolean getLayoutInvisible() {
		return layoutInvisible;
	}

	public void setLayoutInvisible(final boolean v) {
		this.layoutInvisible = v;
	}

	public JJGem(final JJGraphWindow f) {
		fenster = f;
		graph = fenster.getGraph();
	}

	@Override
	public void layout() {
		gemInit(graph.getNodes());
		gemDo();
	}

	@Override
	public void layout(final Collection<JJNode> locNodes) {
		gemInit(locNodes);
		gemDo();
	}

	// public void run()
	// {
	// gem(graph.getNodes());
	// }

	void initNodes(final Collection<JJNode> locNodes) {
		int i = 0;

		final Rectangle r = getBounds();
		if (r == null) {
			bc = new JJPoint();
		} else {
			bc = new JJPoint(r.x + r.width / 2, r.y + r.height / 2);
		}
		temperature = 0;
		// Debug.println("Temperature: " + temperature);

		graph.setNodeValue(-1);

		gemNodes = new JJGemNode[locNodes.size()];
		graphNodes = new JJNode[locNodes.size()];

		for (final JJNode tmpN : locNodes) {
			final JJGraphicNode tmpGN = tmpN.getGraphicNode(fenster);
			final JJGemNode gemN = new JJGemNode(tmpGN, edgeLength);

			gemNodes[i] = gemN;
			graphNodes[i] = tmpN;

			// bc = bc.plus(gemN.pos);

			// 2D only
			gemN.setPos(new JJPoint(gemN.getPos().x, gemN.getPos().y, 0));

			if (!gemN.getPos().isValid())
				gemN.setPos(new JJPoint(Math.random() * 100, Math.random() * 100, 0));

			if (Double.isNaN(radius) && (r == null))
				bc.plusA(gemN.getPos());

			temperature += gemN.heat * gemN.heat;
			// Debug.println("Temperature: " + temperature);

			tmpN.setValue(i++);
		}

		// Debug.println("Init over");

	}

	protected int maxRounds;
	protected int numRounds;
	protected double stopTemp;
	protected long startTime;

	protected long maxRealTime = 120000;

	/**
	 * Get the value of maxRealTime.
	 *
	 * @return Value of maxRealTime.
	 */
	public long getMaxRealTime() {
		return maxRealTime;
	}

	/**
	 * Set the value of maxRealTime.
	 *
	 * @param v
	 *            Value to assign to maxRealTime.
	 */
	public void setMaxRealTime(final long v) {
		this.maxRealTime = v;
	}

	void init(final Collection<JJNode> locNodes) {
		elenSqr = edgeLength * edgeLength;
		maxtemp = MAXTEMP * edgeLength;
		maxAttract = elenSqr * 64;

		initNodes(locNodes);

		numRounds = 0;
		numNodes = locNodes.size();

		maxRounds = (int) (maxIter * numNodes * numNodes);
		stopTemp = finalTemp * finalTemp * elenSqr * 70 * 70 * numNodes;
		startTime = System.currentTimeMillis();
	}

	void gemDo() {
		while (next())
			;
	}

	public boolean next() {
		if ((temperature <= stopTemp) || (numRounds++ >= maxRounds)
				|| ((maxRealTime > 0) && (System.currentTimeMillis() - startTime > maxRealTime))) {
			// Debug.println("Temperature: " + temperature);

			writePos();
			gemNodes = null;
			graphNodes = null; // help the gc
			return false;
		}

		for (int k = 0; k < numNodes; k++) { // deterministic schedule
			updatePos(gemNodes[k], updateImpulse(gemNodes[k]));
		}

		// Debug.print("\rRound " + numRounds + " of " + maxRounds +
		// ", temperature " + temperature + " of " + stopTemp +
		// " ");

		return true;
	}

	protected void writePos() {
		for (int k = 0; k < numNodes; k++) {
			if (gemNodes[k].getPos().isValid()) {
				Debug.println("Moving node " + graphNodes[k].getName() + " to " + gemNodes[k].getPos());

				fenster.moveNodeTo(graphNodes[k].getGraphicNode(fenster), gemNodes[k].getPos());
			} else {
				// Debug.println("Baaaah");
				fenster.moveNodeTo(graphNodes[k].getGraphicNode(fenster),
						new JJPoint(Math.random() * 100, Math.random() * 100, 0));
			}

		}
	}

	void updatePos(final JJGemNode v, JJPoint i) {
		double t, n;
		JJPoint imp;

		if (!i.isValid())
			i = new JJPoint(0, 0, 0);

		if ((i.getX() != 0) || (i.getY() != 0)) {
			n = Math.max(Math.abs(i.getX()), Math.abs(i.getY())) / elenSqr; // 16384;
			if (n > 1) {
				// i = i.div(n);
				i.divA(n);
			}

			t = v.heat;
			n = norm2(i.getX(), i.getY());
			// i = i.mult(t/n);
			// v.getPos() = v.pos.plus(i);
			// bc = bc.plus(i);
			i.multA(t / n);
			boolean tmpB = false;
			if (!Double.isNaN(radius))
				tmpB = v.addPos(i, radius);
			else
				tmpB = v.addPos(i, bounds);
			if (tmpB) {
				if (Double.isNaN(radius) && (bounds == null))
					bc.plusA(i);

				imp = v.imp;
				n = t * norm2(imp.getX(), imp.getY());
				if (n != 0) {
					temperature -= t * t;
					// Debug.println("Temperature: " + temperature);
					t += t * OSCILLATION * (i.getX() * imp.getX() + i.getY() * imp.getY()) / n;
					t = Math.min(t, maxtemp);
					v.dir += ROTATION * (i.getX() * imp.getY() - i.getY() * imp.getX()) / n;
					t -= t * Math.abs(v.dir) / numNodes;
					t = Math.max(t, 2);
					temperature += t * t;
					// Debug.println("Temperature: " + temperature);
					v.heat = t;
				}

				// v.imp = new JJPoint(i);
				v.imp = i;
			}
		}
		if (!v.imp.isValid())
			v.imp = new JJPoint(0, 0, 0);
		if (!v.getPos().isValid())
			v.setPos(new JJPoint(Math.random() * 100, Math.random() * 100, 0));

	}

	protected JJPoint updateImpulse(final JJGemNode v) {
		double n = SHAKE * edgeLength;
		final JJPoint i = new JJPoint(Math.random() * (2 * n + 1) - n, Math.random() * (2 * n + 1) - n);

		i.x += ((bc.x / numNodes) - v.getPos().x) * (v.getMass() * unitMass) * gravity;
		i.y += ((bc.y / numNodes) - v.getPos().y) * (v.getMass() * unitMass) * gravity;

		// repulsive forces

		for (int k = 0; k < numNodes; k++) {
			final JJPoint d = JJPoint.minus(v.getPos(), gemNodes[k].getPos());

			n = d.getX() * d.getX() + d.getY() * d.getY();
			if (n != 0) {
				i.plusA(d.multA(elenSqr / n));
			}
		}

		// attractive forces
		for (final Iterator<JJEdge> iter = v.node.getNode().edgeIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			final JJNode tmpN = tmpE.opposite(v.node.getNode());

			/// ??? Inefficient getGraphicNode ???
			if (layoutInvisible
					|| (tmpN.getGraphicNode(fenster).isVisible() && tmpE.getGraphicEdge(fenster).isVisible())) {
				final int k = tmpN.getValue();

				final JJPoint d = JJPoint.minus(v.getPos(), gemNodes[k].getPos());
				n = (d.getX() * d.getX() + d.getY() * d.getY()) / (v.getMass() * unitMass);
				n = Math.min(n, maxAttract);
				if (useEdgeWeight) {
					i.minusA(d.multA(n / (elenSqr * tmpE.getWeight() * tmpE.getWeight())));
				} else {
					i.minusA(d.multA(n / elenSqr));
				}
			}
		}

		// 2D
		i.z = 0;

		return i;
	}

	void gemInit(final Collection<JJNode> locNodes) {
		// graph.numberNodes (0);
		if (!layoutInvisible) {
			final java.util.List<JJNode> p = new LinkedList<>();
			for (final Object element : locNodes) {
				final JJNode tmpN = (JJNode) element;
				if (tmpN.getGraphicNode(fenster).isVisible()) {
					p.add(tmpN);
				}
			}
			init(p);
		} else
			init(locNodes);
	}

	protected double norm2(final double x, final double y) {
		return Math.sqrt((x) * (x) + (y) * (y));
	}

	@Override
	public String getName() {
		return "GEM";
	}

	@Override
	public void apply(final String s) {
		layout();
	}

	@Override
	public boolean canDo(final String s) {
		return s.equals(getName());

	}

}
