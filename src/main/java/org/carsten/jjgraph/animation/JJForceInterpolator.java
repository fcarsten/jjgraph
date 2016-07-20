/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

/**
 * JJForceInterpolator.java
 *
 *
 * Created: Wed Feb 16 16:32:29 2000
 *
 * @author Carsten Friedrich
 * @version
 *
 */
import org.carsten.jjgraph.util.JJPoint;

public class JJForceInterpolator {
	private JJAnimationNode animationNodes[];
	private final JJGraphAnimator graphAnimator;

	private int images;
	private double imageCounter;
	private JJPoint oldPos[];

	public JJForceInterpolator(final JJGraphAnimator b) {
		graphAnimator = b;
	}

	void init(final JJAnimationNode bn[]) {
		animationNodes = bn;
		if (bn == null)
			return;

		oldPos = new JJPoint[animationNodes.length];

		for (int i = 0; i < animationNodes.length; i++) {
			if (animationNodes[i].isVisible()) {
				oldPos[i] = new JJPoint(animationNodes[i].getPosition());
			}
		}
	}

	void setNumImages(final int ni) {
		images = ni;
		imageCounter = 0;
	}

	boolean next() {
		boolean didChange = false;
		final double t = graphAnimator.speedControl(Math.min(imageCounter / images, 1));
		final int numNodes = animationNodes.length;

		final JJPoint interNodeForce = new JJPoint();
		final JJPoint forceVector = new JJPoint();
		final JJPoint targetAttractionForce = new JJPoint();

		for (int k = 0; k < numNodes; k++) { // deterministic schedule
			if (!animationNodes[k].isVisible())
				continue;

			interNodeForce.x = 0;
			interNodeForce.y = 0;
			interNodeForce.z = 0;

			final JJPoint ftv = animationNodes[k].getPosition();
			final JJPoint f0v = oldPos[k];
			final JJPoint f1v = animationNodes[k].getEndPosition();
			int numF = 0;

			for (int i = 0; i < numNodes; i++) {
				if ((i == k) || (!animationNodes[i].isVisible()))
					continue;

				final JJPoint ftu = animationNodes[i].getPosition();
				final JJPoint f0u = oldPos[i];
				final JJPoint f1u = animationNodes[i].getEndPosition();

				final double distT = dist(ftu, ftv);
				final double dist0 = dist(f0u, f0v);
				final double dist1 = dist(f1u, f1v);

				forceVector.copyFrom(ftu);
				forceVector.minusA(ftv);

				// JJPoint force = JJPoint.minus(ftu,ftv);

				if (distT != 0) {
					numF++;
					forceVector.divA(distT);
					forceVector.multA(distT - ((1 - t) * dist0 + t * dist1));
					interNodeForce.plusA(forceVector);
				}

			}
			if (numF > 0)
				interNodeForce.divA(numF);

			targetAttractionForce.copyFrom(f1v);
			targetAttractionForce.minusA(ftv);

			// JJPoint targetAttractionForceAttractionForce =
			// JJPoint.minus(f1v,ftv);

			final JJPoint force = interNodeForce.multA(1 - t);
			force.plusA(targetAttractionForce.multA(t));

			if (t < 1) {
				animationNodes[k].setPosition(ftv.plusA(force));
				if (!ftv.equals(f1v))
					didChange = true;
			} else
				animationNodes[k].setPosition(f1v);
		}
		imageCounter++;

		return t >= 1;
	}

	double dist(final JJPoint p1, final JJPoint p2) {
		return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
	}
}
