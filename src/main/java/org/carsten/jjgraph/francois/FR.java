/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.francois;

import java.util.Iterator;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.util.JJPoint;

public class FR extends Spring {
	public final static int edgeLengthDefault = 100;
	public final static int nbStepsDefault = 100;
	public final static int maxRepulsionDefault = 200;
	public final static int maxMoveDefault = 10;
	public final static boolean boRepulsionDefault = true;
	public final static boolean boAttractionDefault = true;

	int edgeLength = edgeLengthDefault;
	int nbSteps = nbStepsDefault;
	int maxRepulsion = maxRepulsionDefault;
	int maxMove = maxMoveDefault;
	boolean boRepulsion = boRepulsionDefault;
	boolean boAttraction = boAttractionDefault;

	public FR(final JJGraphWindow g) {
		super(g);
	}

	private void Node_Node_repulsion(final JJFRNode a, final JJFRNode b) {
		final Force fa = a.force; // (Force) a.getInfos("force");
		final Force fb = b.force; // (Force) b.getInfos("force");

		final JJPoint ca = a.pos; // (JJPoint)
									// a.getInfos("JJPoint",JJPoint.theClass);
		final JJPoint cb = b.pos; // (JJPoint)
									// b.getInfos("JJPoint",JJPoint.theClass);
		final double dx = cb.x - ca.x;
		final double dy = cb.y - ca.y;

		final double d = Math.sqrt(dx * dx + dy * dy);
		if (d == 0) {
			fa.addForce(2 * edgeLength * Math.random() - edgeLength, 2 * edgeLength * Math.random() - edgeLength);
			fb.addForce(2 * edgeLength * Math.random() - edgeLength, 2 * edgeLength * Math.random() - edgeLength);
		} else {
			final double dopt = calcDopt(a, b);
			if (d <= dopt + maxRepulsion) { // a un moment on s arrete..
				final double f = -(dopt * dopt) / d;
				final double fx = f * dx / d;
				final double fy = f * dy / d;
				fa.addForce(fx, fy);
				fb.addForce(-fx, -fy);
			}
		}
	}

	private void clearForces() {
		for (int i = 0; i < nodeInfo.length; i++) {
			nodeInfo[i].force.dx = 0;
			nodeInfo[i].force.dy = 0;
		}

	}

	protected double calcDopt(final JJFRNode a, final JJFRNode b) {
		final JJPoint ca = a.pos; // (JJPoint)
									// a.getInfos("JJPoint",JJPoint.theClass);
		final JJPoint cb = b.pos; // (JJPoint)
									// b.getInfos("JJPoint",JJPoint.theClass);
		// ShapeNode sha=(ShapeNode) a.getInfos("shape", ShapeRect.theClass);
		// Rectangle ra=sha.getBounds(ca);
		// ShapeNode shb=(ShapeNode) b.getInfos("shape", ShapeRect.theClass);
		// Rectangle rb=shb.getBounds(cb);

		// return edgeLength+0.8*(Math.max(ra.width,ra.height)+
		// Math.max(rb.width,rb.height));
		return edgeLength;
	}

	protected void Node_Node_attraction(final JJFRNode a, final JJFRNode b) {
		final Force fa = a.force; // (Force) a.getInfos("force");
		final JJPoint ca = a.pos; // (JJPoint)
									// a.getInfos("JJPoint",JJPoint.theClass);
		final Force fb = b.force; // (Force) b.getInfos("force");
		final JJPoint cb = b.pos; // (JJPoint)
									// b.getInfos("JJPoint",JJPoint.theClass);

		final double dx = cb.x - ca.x;
		final double dy = cb.y - ca.y;

		final double d2 = dx * dx + dy * dy;

		if (d2 == 0) {
			fa.addForce(2 * edgeLength * Math.random() - edgeLength, 2 * edgeLength * Math.random() - edgeLength);
			fb.addForce(2 * edgeLength * Math.random() - edgeLength, 2 * edgeLength * Math.random() - edgeLength);
		} else {
			final double dopt = calcDopt(a, b);
			final double d = Math.sqrt(d2);
			final double f = d2 / dopt;
			final double fx = f * dx / d;
			final double fy = f * dy / d;
			fa.addForce(fx, fy);
			fb.addForce(-fx, -fy);
		}
	}

	private int currentStep;

	@Override
	protected void initStep() {
		currentStep = 0;
	}

	@Override
	protected void nextStep() {
		currentStep++;
	}

	@Override
	protected boolean hasNextStep() {
		return currentStep < nbSteps;
	}

	@Override
	public void step() {
		initNodes();

		initForces();

		// les noeuds s ecartent
		if (boRepulsion) {
			// List ls=getGraph().listNodes();
			// Node[] Tn=(Node[]) ls.toArray(new Node[0]);
			clearForces();
			for (int i = 0; i < nodeInfo.length; i++) {
				for (int j = i + 1; j < nodeInfo.length; j++) {
					Node_Node_repulsion(nodeInfo[i], nodeInfo[j]);
				}
			}
		}

		// les noeuds relies s attirent
		if (boAttraction) {
			for (final Iterator<JJEdge> ea = getGraph().edgeIterator(); ea.hasNext();) {
				final JJEdge e = ea.next();
				Node_Node_attraction(nodeInfo[e.getSource().getValue()], nodeInfo[e.getTarget().getValue()]);
			}
		}

		// bound forces
		// for (Iterator na=getGraph().allNodes(); na.hasNext();) {
		// Node a=(Node) na.next();
		for (final JJFRNode element : nodeInfo) {
			final Force fa = element.force; // (Force) a.getInfos("force");
			fa.dx = Math.max(-maxMove, Math.min(maxMove, fa.dx));
			fa.dy = Math.max(-maxMove, Math.min(maxMove, fa.dy));
		}

		commitForces();
	}

	@Override
	public String getName() {
		return "Spring FR";
	}

}
