/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

import org.carsten.jjgraph.util.GFMatrix;
import org.carsten.jjgraph.util.JJPoint;

public class JJPolarInterpol extends JJMatrixInterpol {
	protected double targetAngle;
	protected double sourceAngle;
	protected double currentAngle;

	JJPolarInterpol(final JJGraphAnimator b) {
		super(b);
	}

	@Override
	public void setTargetMat(final double m[][]) {
		if (m.length != 2)
			throw new IllegalArgumentException("Array size must be 2");

		final double m1[][] = { m[1] };

		super.setTargetMat(m1);
		final double q[] = m[0];
		targetAngle = Math.acos(q[0]);
		if (Math.abs(Math.sin(targetAngle) - q[1]) > EPSILON)
			targetAngle = -targetAngle;
		// Debug.println("Target Angle: " + Math.toDegrees(targetAngle));

	}

	@Override
	public void setSourceMat(final double m[][]) {
		if (m.length != 2)
			throw new IllegalArgumentException("Array size must be 2");

		final double m1[][] = { m[1] };
		super.setSourceMat(m1);
		final double q[] = m[0];
		sourceAngle = Math.acos(q[0]);
		if (Math.abs(Math.sin(sourceAngle) - q[1]) > EPSILON)
			sourceAngle = -sourceAngle;
		// Debug.println("Source Angle: " + Math.toDegrees(sourceAngle));
	}

	@Override
	public void reset() {
		super.reset();

		currentAngle = sourceAngle;
	}

	@Override
	public void frame(final double t) {
		double dist = targetAngle - sourceAngle;

		currentAngle = sourceAngle + (t * dist);

		for (int i = 0; i < 6; i++) {
			dist = targetMat[0][i] - sourceMat[0][i];
			mat[0][i] = sourceMat[0][i] + t * dist;
		}

		resetCTM();
		ctm[4] = -center.x;
		ctm[5] = -center.y;

		// double ctm[]= {1,0,0,1,-center.x,-center.y};

		final double rm[] = new double[6];

		GFMatrix.rotatematrix(Math.toDegrees(currentAngle), rm);
		GFMatrix.concatmatrix(ctm, rm, ctm);
		GFMatrix.concatmatrix(ctm, mat[0], ctm);

		ctm[4] += center.x;
		ctm[5] += center.y;

		for (final JJAnimationNode animationNode : animationNodes) {
			final JJPoint p = animationNode.getCustomPos();
			final JJPoint np = new JJPoint(ctm[4] + p.x * ctm[0] + p.y * ctm[2], ctm[5] + p.x * ctm[1] + p.y * ctm[3]);
			animationNode.setPosition(np);
		}
	}
} // JJPolarInterpol
