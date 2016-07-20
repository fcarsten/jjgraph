/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.layout;

import java.awt.Rectangle;

/**
 * JJGemNode.java
 *
 *
 * Created: Wed Feb 16 16:32:29 2000
 *
 * @author Carsten Friedrich
 * @version
 */
import org.carsten.jjgraph.graph.JJGraphicNode;
import org.carsten.jjgraph.util.JJPoint;

public class JJGemNode {
	private JJPoint pos = null;
	JJPoint imp = null; // last impusle
	double heat = 0; // local temperature
	double dir; // skew gauge
	int deg;
	public JJGraphicNode node = null;

	// JJPoint oldPos;
	private Rectangle nodeBounds = null;

	/**
	 * Get the value of nodeBounds.
	 * 
	 * @return value of nodeBounds.
	 */
	public Rectangle getNodeBounds() {
		if (nodeBounds == null)
			nodeBounds = node.getWindow().getBounds(node);

		nodeBounds.x = (int) pos.x;
		nodeBounds.y = (int) pos.y;

		return nodeBounds;
	}

	public double dist(final JJGemNode n) {
		return node.dist(n.node);

	}
	// /**
	// * Set the value of nodeBounds.
	// * @param v Value to assign to nodeBounds.
	// */
	// public void setNodeBounds(Rectangle v) {
	// this.nodeBounds = v;
	// }

	private double mass;

	/**
	 * Get the value of mass.
	 * 
	 * @return value of mass.
	 */
	public double getMass() {
		return mass;
	}

	/**
	 * Set the value of mass.
	 * 
	 * @param v
	 *            Value to assign to mass.
	 */
	public void setMass(final double v) {
		this.mass = v;
	}

	public JJPoint getPos() {
		return pos;
	}

	public void setPos(final JJPoint v) {
		pos = new JJPoint(v);
	}

	public boolean addPos(final JJPoint p, final double radius) {
		if (p.isValid() && (!node.isFixed())) {
			pos.plusA(p);
			if (!Double.isNaN(radius)) {
				if (pos.abs() > radius) {
					pos.divA(pos.abs()).multA(radius);
					imp = JJPoint.div(pos, -pos.abs()).multA(imp.abs() / 2);
				}
			}

			return true;
		}
		return false;
	}

	public boolean addPos(final JJPoint p, final Rectangle bounds) {
		if (p.isValid() && (!node.isFixed())) {

			pos.plusA(p);
			if (bounds != null) {
				final Rectangle nb = getNodeBounds();
				if (!bounds.contains(nb)) {
					if (nb.x + nb.width > bounds.x + bounds.width) {
						pos.x = bounds.x + bounds.width - nb.width;
						imp.x *= -0.5;
					}
					if (nb.y + nb.height > bounds.y + bounds.height) {
						pos.y = bounds.y + bounds.height - nb.height;
						imp.y *= -0.5;
					}
					if (nb.x < bounds.x) {
						pos.x = bounds.x + nb.width / 2;
						imp.x *= -0.5;
					}
					if (nb.y < bounds.y) {
						pos.y = bounds.y + nb.height / 2;
						imp.y *= -0.5;
					}
				}
			}
			return true;
		}
		return false;
	}

	public JJGemNode(final JJGraphicNode n, final double edgeLength) {
		node = n;
		imp = new JJPoint();
		pos = new JJPoint(n.getCoords());
		if (!pos.isValid())
			pos = new JJPoint(Math.random() * 100, Math.random() * 100);

		// oldPos = new JJPoint(pos);
		if (!node.isFixed()) {
			heat = JJGem.STARTTEMP * edgeLength;
		}

		deg = n.getNode().deg();
		mass = 1 + deg / 3.0;
		dir = 0;
	}
}
