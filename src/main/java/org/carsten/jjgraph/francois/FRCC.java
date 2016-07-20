/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.francois;

// import java.awt.geom.*;
// import java.awt.Rectangle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJNode;
import org.carsten.jjgraph.util.JJPoint;

abstract class Cnstr {
	double edgeLength = 0.16; // 128 default (0.16 good)
	double maxRepulsion = 300; // 300 default
	double maxForce = 100.0;
	double maxForceSquared = 100.0 * 100.0;
	JJGraph _g;
	JJGraphWindow window;

	Cnstr(final JJGraphWindow g) {
		window = g;
		_g = g.getGraph();
	}

	JJGraph getGraph() {
		return _g;
	}

	abstract void apply();

	protected double calcDopt(final JJNode a, final JJNode b) {
		// Coord ca=(Coord) a.getInfos("Coord",Coord.theClass);
		// Coord cb=(Coord) b.getInfos("Coord",Coord.theClass);
		// ShapeNode sha=(ShapeNode) a.getInfos("shape", ShapeRect.theClass);
		// Rectangle ra=sha.getBounds(ca);
		// ShapeNode shb=(ShapeNode) b.getInfos("shape", ShapeRect.theClass);
		// Rectangle rb=shb.getBounds(cb);
		// return edgeLength+1.0*(Math.max(ra.width,ra.height)+
		// Math.max(rb.width,rb.height));

		// double maxWeight = 25.0;
		double maxWeight = 25.0;

		for (final Iterator<JJEdge> na = a.inIterator(); na.hasNext();) {
			final JJEdge edge = na.next();

			if (edge.getSource() == b) {
				final double weight = edge.getWeight();

				if (weight > maxWeight) {
					maxWeight = weight;
				}
			}
		}

		for (final Iterator<JJEdge> na = a.outIterator(); na.hasNext();) {
			final JJEdge edge = na.next();

			if (edge.getSource() == b) {
				final double weight = edge.getWeight();

				if (weight > maxWeight) {
					maxWeight = weight;
				}
			}
		}

		// return (double) edgeLength * Math.pow(maxWeight, 2.0);
		return edgeLength * Math.pow(maxWeight, 2.0);
	}
}

class NodeNodesRepulsionCnstr extends Cnstr {
	JJFrancoisNode a;

	NodeNodesRepulsionCnstr(final JJGraphWindow g, final JJFrancoisNode n) {
		super(g);
		a = n;
	}

	@Override
	void apply() {
		final Force fa = a.force; // (Force) a.getInfos("force");
		final JJPoint ca = a.pos; // (Coord) a.getInfos("Coord",Coord.theClass);

		for (final Iterator<JJNode> na = getGraph().nodeIterator(); na.hasNext();) {
			final JJNode b = na.next();
			final JJPoint cb = b.getGraphicNode(window).getCoords();
			final double dx = cb.x - ca.x;
			final double dy = cb.y - ca.y;
			final double d = Math.sqrt(dx * dx + dy * dy);
			if (d == 0)
				fa.addForce(2 * edgeLength * Math.random() - edgeLength, 2 * edgeLength * Math.random() - edgeLength);
			else {
				final double dopt = calcDopt(a.node.getNode(), b);

				if (d <= dopt + maxRepulsion) { // a un moment on s arrete..
					double f = -(dopt * dopt) / d;
					f /= 10.0;
					double fx = f * dx / d;
					double fy = f * dy / d;
					final double sqrlen = fx * fx + fy * fy;
					if (sqrlen > maxForceSquared) {
						final double len = Math.sqrt(sqrlen);
						fx = maxForce * fx / len;
						fy = maxForce * fy / len;
					}
					fa.addForce(fx, fy);
				}
			}
		}
	}
}

class NodeAdjacentsAttractionCnstr extends Cnstr {
	JJFrancoisNode a;

	NodeAdjacentsAttractionCnstr(final JJGraphWindow g, final JJFrancoisNode n) {
		super(g);
		a = n;
	}

	void _apply(final JJNode b, final JJFrancoisNode fa) {
		final JJPoint cb = b.getGraphicNode(window).getCoords(); // (Coord)
																	// b.getInfos("Coord",Coord.theClass);
		final double dx = cb.x - fa.pos.x;
		final double dy = cb.y - fa.pos.y;
		final double d2 = dx * dx + dy * dy;

		if (d2 == 0) {
			fa.force.addForce(2 * edgeLength * Math.random() - edgeLength, 2 * edgeLength * Math.random() - edgeLength);
		} else {
			final double dopt = calcDopt(a.node.getNode(), b);
			final double d = Math.sqrt(d2);
			double f = d2 / dopt;
			f /= 10.0;
			double fx = f * dx / d;
			double fy = f * dy / d;
			final double sqrlen = fx * fx + fy * fy;
			if (sqrlen > maxForceSquared) {
				final double len = Math.sqrt(sqrlen);
				fx = maxForce * fx / len;
				fy = maxForce * fy / len;
			}
			fa.force.addForce(fx, fy);
		}

	}

	@Override
	void apply() {
		final Force fa = a.force; // (Force) a.getInfos("force");

		for (final Iterator<JJEdge> na = a.node.getNode().inIterator(); na.hasNext();) {
			final JJEdge b = na.next();
			_apply(b.getSource(), a);
		}

		for (final Iterator<JJEdge> na = a.node.getNode().outIterator(); na.hasNext();) {
			final JJEdge b = na.next();
			_apply(b.getTarget(), a);
		}

	}
}

class MagneticFieldCnstr extends Cnstr {
	private final JJFrancoisNode a;
	private final JJFrancoisNode b;

	private final double forceVectors[][];
	private static final int NUM_FORCE_VECTORS = 8;

	MagneticFieldCnstr(final JJGraphWindow g, final JJFrancoisNode a, final JJFrancoisNode b) {
		super(g);
		this.a = a;
		this.b = b;

		forceVectors = new double[NUM_FORCE_VECTORS][2];

		forceVectors[0][0] = 0.0;
		forceVectors[0][1] = 1.0;
		forceVectors[1][0] = 0.0;
		forceVectors[1][1] = -1.0;
		forceVectors[2][0] = 1.0;
		forceVectors[2][1] = 0.0;
		forceVectors[3][0] = -1.0;
		forceVectors[3][1] = 0.0;
		forceVectors[4][0] = 0.707;
		forceVectors[4][1] = 0.707;
		forceVectors[5][0] = -0.707;
		forceVectors[5][1] = 0.707;
		forceVectors[6][0] = -0.707;
		forceVectors[6][1] = -0.707;
		forceVectors[7][0] = 0.707;
		forceVectors[7][1] = -0.707;
	}

	@Override
	void apply() {
		final double dx = b.pos.x - a.pos.x;
		final double dy = b.pos.y - a.pos.y;
		final double d2 = dx * dx + dy * dy;
		final double d = Math.sqrt(d2);

		if (d != 0) {
			final double cm = 0.1; // Weight of magnetic field constraint (1.0)
			final double fieldStrength = 30.0; // Strength of magnetic field
												// (30.0)
			final double alpha = 1.0; // Edge length power (1.0)
			final double beta = 0.5; // Angle power (0.5)

			// Normalise y component of edge vector
			double uynorm = dy / d;
			if (uynorm < -1.0) {
				uynorm = -1.0;
			}
			if (uynorm > 1.0) {
				uynorm = 1.0;
			}

			// Normalise x component of edge vector
			double uxnorm = dx / d;
			if (uxnorm < -1.0) {
				uxnorm = -1.0;
			}
			if (uxnorm > 1.0) {
				uxnorm = 1.0;
			}

			final double forcesApplied[][] = new double[NUM_FORCE_VECTORS][2];
			final double theta[] = new double[NUM_FORCE_VECTORS];
			double lowTheta = -1.0;
			int lowThetaForce = -1;

			for (int i = 0; i < NUM_FORCE_VECTORS; i++) {
				// Calculate cos(theta) from dot product of edge and force
				// vectors
				double cosTheta = uxnorm * forceVectors[i][0] + uynorm * forceVectors[i][1];

				if (cosTheta < 0.0) {
					// Ignore angles > 90 degrees
					forcesApplied[i][0] = 0.0;
					forcesApplied[i][1] = 0.0;
					continue;
				}
				if (cosTheta > 1.0) {
					// Ensure any roundoff error doesn't break Math.acos()
					cosTheta = 1.0;
				}

				// Calculate acute angle
				theta[i] = Math.acos(cosTheta);

				// Evaluate Sugiyama magnetic force equation to determine force
				// magnitude
				double fm = cm * fieldStrength * Math.pow(d, alpha) * Math.pow(theta[i], beta);

				if (fm > maxForce) {
					fm = maxForce;
				}
				// System.out.println(fm);

				// Determine the side of the force vector on which the edge
				// vector lies
				final double cross = forceVectors[i][0] * uynorm - forceVectors[i][1] * uxnorm;

				// Calculate direction of force
				if (cross < 0) {
					forcesApplied[i][0] = -uynorm * fm;
					forcesApplied[i][1] = uxnorm * fm;
				} else {
					forcesApplied[i][0] = uynorm * fm;
					forcesApplied[i][1] = -uxnorm * fm;
				}

				// Apply forces of equal magnitude and opposite direction to
				// edge endpoints
				// (note that this could be moved outside loop and performed
				// only for force vector with lowest theta)
				// a.force.addForce(-forcesApplied[i][0], -forcesApplied[i][1]);
				// b.force.addForce(forcesApplied[i][0], forcesApplied[i][1]);

				if (lowThetaForce < 0 || theta[i] < lowTheta) {
					lowTheta = theta[i];
					lowThetaForce = i;
				}
			}

			if (lowThetaForce >= 0) {
				a.force.addForce(-forcesApplied[lowThetaForce][0], -forcesApplied[lowThetaForce][1]);
				b.force.addForce(forcesApplied[lowThetaForce][0], forcesApplied[lowThetaForce][1]);
			}
		}
	}
}

class NodeEdgeRepulsionCnstr extends Cnstr {
	JJFrancoisNode n;
	JJFrancoisNode source;
	JJFrancoisNode target;
	JJEdge e;

	NodeEdgeRepulsionCnstr(final JJGraphWindow g, final JJFrancoisNode a, final JJFrancoisNode s,
			final JJFrancoisNode t) {

		super(g);
		n = a;
		source = s;
		target = t;
	}

	@Override
	void apply() {
		final JJFrancoisNode a = source; // e.getSource();
		final JJFrancoisNode b = target; // e.getTarget();

		final Force fa = a.force; // (Force) a.getInfos("force");
		final JJPoint ca = a.pos; // (JJPoint)
									// a.getInfos("Coord",JJPoint.theClass);
		final Force fb = b.force; // (Force) b.getInfos("force");
		final JJPoint cb = b.pos; // (JJPoint)
									// b.getInfos("JJPoint",JJPoint.theClass);
		final Force fn = n.force; // (Force) n.getInfos("force");
		final JJPoint cn = n.pos; // (JJPoint)
									// n.getInfos("JJPoint",JJPoint.theClass);

		final double dabx = cb.x - ca.x;
		final double daby = cb.y - ca.y;
		final double danx = cn.x - ca.x;
		final double dany = cn.y - ca.y;
		final double dbnx = cn.x - cb.x;
		final double dbny = cn.y - cb.y;
		final double deno = dabx * dabx + daby * daby;

		// System.out.println("n = '" + n.node.getName() + "', ('" +
		// a.node.getName() + "', '" + b.node.getName() + "')");
		/*
		 * if(n.node.getName().equals("Sefton") &&
		 * (a.node.getName().equals("Birrong") &&
		 * b.node.getName().equals("Regents Park"))) {
		 *
		 * double cross = dabx * dbny - dbnx * daby; if(cross > 0.0) {
		 * System.out.println("LHS"); } if(cross == 0.0) {
		 * System.out.println("ON LINE"); } if(cross < 0.0) {
		 * System.out.println("RHS"); } }
		 */

		if (deno != 0) {
			final double nume = (dabx * danx + daby * dany);
			final double alpha = nume / deno;
			if (alpha > 0.0 && alpha < 1.0) {
				final double ix = ca.x + alpha * dabx;
				final double iy = ca.y + alpha * daby;
				final double dinx = cn.x - ix;
				final double diny = cn.y - iy;

				final double d = Math.sqrt(dinx * dinx + diny * diny);

				final double vx = dinx / 3; // (int) Math.rint(dinx / 3);
				final double vy = diny / 3; // (int) Math.rint(diny / 3);

				final Bound ba = a.bound; // (Bound) a.getInfos("bound");
				ba.addBound(vx, vy);
				final Bound bb = b.bound; // ( Bound) b.getInfos("bound");
				bb.addBound(vx, vy);
				final Bound bn = n.bound; // (Bound) n.getInfos("bound");
				bn.addBound(-vx, -vy);

				final double maxNodeEdgeRepulsion = maxForce;

				if (d == 0) {
					fn.addForce(2 * edgeLength * Math.random() - edgeLength,
							2 * edgeLength * Math.random() - edgeLength);
				} else if (d <= maxNodeEdgeRepulsion) {
					final double f = -(maxNodeEdgeRepulsion - d) * (maxNodeEdgeRepulsion - d);
					// f /= 10.0;
					// -(100*edgeLength*edgeLength)/d;
					int fx = (int) (f * dinx / d);
					int fy = (int) (f * diny / d);
					final double sqrlen = fx * fx + fy * fy;
					if (sqrlen > maxForceSquared) {
						final double len = Math.sqrt(sqrlen);
						fx = (int) (maxForce * fx / len);
						fy = (int) (maxForce * fy / len);
					}
					fa.addForce(fx, fy);
					fb.addForce(fx, fy);
					fn.addForce(-fx, -fy);
				}
			} else {
				final double dan3 = Math.sqrt(danx * danx + dany * dany) / 3;
				final double dbn3 = Math.sqrt(dbnx * dbnx + dbny * dbny) / 3;

				a.bound.addNonEdgeBound(dan3);
				b.bound.addNonEdgeBound(dbn3);
				n.bound.addNonEdgeBound(Math.min(dan3, dbn3));
			}
		}
	}
}

public class FRCC extends Spring {
	List<Cnstr> Lcnstr = new ArrayList<>();

	public FRCC(final JJGraphWindow g) {
		super(g);
	}

	public void defaultCnstr() {
		clearCnstr();

		for (int i = 0; i < nodeInfo.length; i++) {
			addCnstr(new NodeNodesRepulsionCnstr(window, (JJFrancoisNode) getNodeInfo(i)));
			addCnstr(new NodeAdjacentsAttractionCnstr(window, (JJFrancoisNode) getNodeInfo(i)));
		}

		for (final Iterator<JJEdge> ne = getGraph().edgeIterator(); ne.hasNext();) {
			final JJEdge e = ne.next();

			addCnstr(new MagneticFieldCnstr(window, (JJFrancoisNode) nodeInfo[e.getSource().getValue()],
					(JJFrancoisNode) nodeInfo[e.getTarget().getValue()]));
		}

		for (int i = 0; i < nodeInfo.length; i++) {
			final JJFrancoisNode a = (JJFrancoisNode) getNodeInfo(i);
			for (final Iterator<JJEdge> ne = getGraph().edgeIterator(); ne.hasNext();) {
				final JJEdge e = ne.next();
				if (e.getSource() != a.node.getNode() && e.getTarget() != a.node.getNode())
					addCnstr(new NodeEdgeRepulsionCnstr(window, a, (JJFrancoisNode) nodeInfo[e.getSource().getValue()],
							(JJFrancoisNode) nodeInfo[e.getTarget().getValue()]));
			}
		}
	}

	public void clearCnstr() {
		Lcnstr.clear();
	}

	public void addCnstr(final Cnstr c) {
		Lcnstr.add(c);
	}

	private int currentStep;
	int nbSteps = 500;

	@Override
	protected void initStep() {
		dampening = 1.0;
		currentStep = 0;
		defaultCnstr();
	}

	@Override
	protected void nextStep() {
		currentStep++;
	}

	@Override
	protected boolean hasNextStep() {
		return currentStep < nbSteps;
	}

	void initBounds() {
		for (int i = 0; i < nodeInfo.length; i++) {
			((JJFrancoisNode) getNodeInfo(i)).bound = new Bound(getNodeInfo(i).node.getNode(),
					(JJFrancoisNode) getNodeInfo(i));
		}
	}

	void commitBounds() {
		for (int i = 0; i < nodeInfo.length; i++) {
			((JJFrancoisNode) getNodeInfo(i)).bound.apply();
		}
	}

	@Override
	public void step() {
		if (currentStep == 11) {
			final int xyz = 0;
		}
		// System.out.println("Iteration #" + currentStep);

		initForces();
		initBounds();

		for (final Object element : Lcnstr) {
			final Cnstr c = (Cnstr) element;
			c.apply();
		}

		commitBounds();
		commitForces();

		// dampening *= 0.9;
		// System.out.println("dampening = " + dampening);
	}

	@Override
	void initNodes() {
		// Debug.println("Init Francois nodes");

		numNodes = graph.getNumNodes();
		nodeInfo = new JJFrancoisNode[numNodes];
		int i = 0;

		for (final Iterator<JJNode> ne = getGraph().nodeIterator(); ne.hasNext();) {
			final JJNode n = ne.next();
			nodeInfo[i] = new JJFrancoisNode(n.getGraphicNode(window));
			n.setValue(i++);
		}
	}

	// public JJFrancoisNode getNodeInfo(int l)
	// {
	// return (JJFrancoisNode) nodeInfo[l];
	// }

	@Override
	public String getName() {
		return "Fran�ois";
	}

}
