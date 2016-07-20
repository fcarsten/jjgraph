/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

import org.carsten.jjgraph.util.GFMatrix;
import org.carsten.jjgraph.util.JJPoint;

public class JJTwoWayPolarInterpol extends JJPolarInterpol {
	protected double targetAngleBack;
	protected double sourceAngleBack;
	protected double currentAngleBack;
	double ctmBack[] = { 1, 0, 0, 1, 0, 0 };
	double currentFrame = 0;
	double maxFrames = 0;

	JJTwoWayPolarInterpol(final JJGraphAnimator b) {
		super(b);
	}

	@Override
	public void resetCTM() {
		super.resetCTM();

		ctmBack[0] = ctmBack[3] = 1;
		ctmBack[1] = ctmBack[2] = ctmBack[4] = ctmBack[5] = 0;
	}

	@Override
	public void setTargetMat(final double m[][]) {
		if (m.length != 4)
			throw new IllegalArgumentException("Array size must be 4");

		final double m1[][] = { m[0], m[1] };

		super.setTargetMat(m1);

		final double q[] = m[2];
		targetAngleBack = Math.acos(q[0]);
		if (Math.abs(Math.sin(targetAngleBack) - q[1]) > EPSILON)
			targetAngleBack = -targetAngleBack;

		// Debug.println("Target Angle back: " +
		// Math.toDegrees(targetAngleBack));

		targetAngleBack = Math.toDegrees(targetAngleBack);
		targetAngle = Math.toDegrees(targetAngle);

		if (Math.abs(targetAngle + targetAngleBack) > 180) {
			if (targetAngleBack >= 90)
				targetAngleBack -= 360;
			else
				targetAngleBack += 360;
		}

		targetAngleBack = Math.toRadians(targetAngleBack);
		targetAngle = Math.toRadians(targetAngle);

		// Debug.println(" corrected: " +
		// Math.toDegrees(targetAngleBack));

		// while(targetAngle < -180)
		// targetAngle += 360;

		// we have to adjust targetMat by hand

		targetMat = new double[2][6];

		final double m2[][] = { m[1], m[3] };

		for (int k = 0; k < 2; k++) {
			for (int i = 0; i < 6; i++) {
				targetMat[k][i] = m2[k][i];
			}
		}
	}

	@Override
	public void setSourceMat(final double m[][]) {
		if (m.length != 4)
			throw new IllegalArgumentException("Array size must be 4");

		final double m1[][] = { m[0], m[1] };
		super.setSourceMat(m1);

		final double q[] = m[2];
		sourceAngleBack = Math.acos(q[0]);
		if (Math.abs(Math.sin(sourceAngleBack) - q[1]) > EPSILON)
			sourceAngleBack = -sourceAngleBack;
		// Debug.println("Source Angle back: " +
		// Math.toDegrees(sourceAngleBack));

		// while( sourceAngleBack >= 180)
		// sourceAngleBack -= 360;
		// while(sourceAngle < -180)
		// sourceAngle += 360;

		// we have to adjust sourceMat by hand

		sourceMat = new double[2][6];

		final double m2[][] = { m[1], m[3] };

		for (int k = 0; k < 2; k++) {
			for (int i = 0; i < 6; i++) {
				sourceMat[k][i] = m2[k][i];
			}
		}
	}

	@Override
	public void reset() {
		super.reset();
		currentFrame = 0;
		maxFrames = (int) (((2.0 * frames) / graphAnimator.getSpeed()) + 0.5);
		currentAngleBack = sourceAngleBack;
	}

	@Override
	public void frame(final double t) {
		double dist = targetAngle - sourceAngle;
		double distBack = sourceAngleBack - targetAngleBack;

		currentAngle = sourceAngle + (dist * t);
		currentAngleBack = targetAngleBack + (distBack * t);

		for (int i = 0; i < 6; i++) {
			dist = targetMat[0][i] - sourceMat[0][i];
			distBack = sourceMat[1][i] - targetMat[1][i];

			mat[0][i] = sourceMat[0][i] + (dist * t);
			mat[1][i] = targetMat[1][i] + (distBack * t);
		}

		resetCTM();
		ctm[4] = -center.x;
		ctm[5] = -center.y;

		ctmBack[4] = -center.x;
		ctmBack[5] = -center.y;

		// double ctm[]= {1,0,0,1,-center.x,-center.y};

		final double rm[] = new double[6];
		final double rmBack[] = new double[6];

		GFMatrix.rotatematrix(Math.toDegrees(currentAngle), rm);
		GFMatrix.concatmatrix(ctm, rm, ctm);
		GFMatrix.concatmatrix(ctm, mat[0], ctm);

		GFMatrix.rotatematrix(Math.toDegrees(currentAngleBack), rmBack);
		GFMatrix.concatmatrix(ctmBack, rmBack, ctmBack);
		GFMatrix.concatmatrix(ctmBack, mat[1], ctmBack);

		ctm[4] += center.x;
		ctm[5] += center.y;

		ctmBack[4] += center.x;
		ctmBack[5] += center.y;

		for (final JJAnimationNode animationNode : animationNodes) {
			final JJPoint sp = animationNode.getStartPosition();
			final JJPoint ep = animationNode.getEndPosition();

			final JJPoint nsp = new JJPoint(ctm[4] + sp.x * ctm[0] + sp.y * ctm[2],
					ctm[5] + sp.x * ctm[1] + sp.y * ctm[3]);
			nsp.multA(1 - t);

			final JJPoint nep = new JJPoint(ctmBack[4] + ep.x * ctmBack[0] + ep.y * ctmBack[2],
					ctmBack[5] + ep.x * ctmBack[1] + ep.y * ctmBack[3]);
			nep.multA(t);

			nsp.plusA(nep);

			animationNode.setPosition(nsp);
		}
	}

	@Override
	public JJPoint transform(final JJPoint p) {
		final JJPoint np1 = new JJPoint(ctm[4] + p.x * ctm[0] + p.y * ctm[2], ctm[5] + p.x * ctm[1] + p.y * ctm[3]);
		np1.multA((maxFrames - currentFrame) / maxFrames);
		final JJPoint np2 = new JJPoint(ctmBack[4] + p.x * ctmBack[0] + p.y * ctmBack[2],
				ctmBack[5] + p.x * ctmBack[1] + p.y * ctmBack[3]);
		np2.multA(currentFrame / maxFrames);
		return np1.plusA(np2);
	}

} // JJPolarInterpol
