/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

import org.carsten.jjgraph.util.JJPoint;

public class JJGEMTranslator {
	protected JJGEMTranslatorNode gemNodes[];
	private final JJAnimationNode animationNodes[];
	private final JJGraphAnimator graphAnimator;

	private double temperature = 1.0;

	// final static double STARTTEMP = 1.0;
	// final static double MAXTEMP = 1.5;
	// final static double OSCILLATION = 0.4;
	// final static double ROTATION = 0.9;
	// final static double SHAKE = 0.3;

	// private double finalTemp = 0.02;
	// private double maxIter = 3;
	// private double gravity = 0.15;

	private int numNodes;

	private final double MIN_DIST = 60;
	double minDistSqr = MIN_DIST * MIN_DIST;
	// private double maxtemp = MAXTEMP * 128;

	// public double getGravity() {return gravity;}
	// public void setGravity(double v) {this.gravity = v;}

	// public double getMaxIter() {return maxIter;}
	// public void setMaxIter(double v) {this.maxIter = v;}

	// public double getFinalTemp() {return finalTemp;}
	// public void setFinalTemp(double v) {this.finalTemp = v;}

	public JJGEMTranslator(final JJAnimationNode bn[], final JJGraphAnimator b) {
		animationNodes = bn;
		graphAnimator = b;

	}

	void initNodes() {
		// temperature = 0;
		// graph.setNodeValue(-1);

		gemNodes = new JJGEMTranslatorNode[animationNodes.length];
		// graphNodes = new JJNode[locNodes.size()];

		for (int i = 0; i < animationNodes.length; i++) {
			// JJGraphicNode tmpGN = animationNodes[i].knoten;
			// JJNode tmpN = tmpGN.getNode();
			final JJGEMTranslatorNode gemN = new JJGEMTranslatorNode(animationNodes[i]);

			gemNodes[i] = gemN;
			// graphNodes[i] = tmpN;

		}
	}

	void init() {
		// maxtemp = MAXTEMP * minDist; // * edgeLength;

		initNodes();
		numNodes = animationNodes.length;
		temperature = 1.0; // += gemN.heat * gemN.heat;

		// int numRounds = 0;
		// int maxRounds = (int)(maxIter * numNodes * numNodes);
		// double stopTemp = finalTemp * finalTemp * numNodes;

		// double tMin = 3;
	}

	boolean next() {
		boolean didChange = false;
		minDistSqr = MIN_DIST * (1 / graphAnimator.getZoom());
		minDistSqr *= minDistSqr;

		for (int k = 0; k < numNodes; k++) { // deterministic schedule
			final double d = JJPoint.dist(gemNodes[k].pos, gemNodes[k].bNode.getEndPosition());

			if (d < 5.0)
				gemNodes[k].pos = gemNodes[k].bNode.getEndPosition();
			else {
				// Debug.println("Dist: " + d);

				updatePos(gemNodes[k], updateImpulse(gemNodes[k]));
				didChange = true;
			}
		}

		if (temperature >= 0.01)
			temperature -= 0.01;
		else
			temperature = 0;

		writePos();

		return didChange;
	}

	void writePos() {
		for (int k = 0; k < numNodes; k++) {
			gemNodes[k].bNode.setPosition(new JJPoint(gemNodes[k].pos));
		}
	}

	void updatePos(final JJGEMTranslatorNode v, final JJPoint i) {
		v.pos.plusA(i);
	}

	JJPoint updateImpulse(final JJGEMTranslatorNode v) {
		final JJPoint i = JJPoint.minus(v.bNode.getEndPosition(), v.pos);

		// repulsive forces
		for (int k = 0; k < numNodes; k++) {
			if (gemNodes[k] != v) {
				final JJPoint d = JJPoint.minus(v.pos, gemNodes[k].pos);
				final double n = d.getX() * d.getX() + d.getY() * d.getY();
				if ((n > 0) && (n < minDistSqr))
					i.plusA(d.multA((minDistSqr * temperature) / n));
			}
		}

		i.divA(i.abs() / 5.0);
		return i;
	}

	private double norm2(final double x, final double y) {
		return Math.sqrt((x) * (x) + (y) * (y));
	}

}
