/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

import org.carsten.jjgraph.util.GFMatrix;
import org.carsten.jjgraph.util.JJPoint;

public class JJMatrixInterpol {
	protected JJAnimationNode animationNodes[];
	protected JJGraphAnimator graphAnimator;
	protected double mat[][];// = new double[6];
	protected double sourceMat[][] = { { 1, 0, 0, 1, 0, 0 } };
	protected double targetMat[][] = { { 1, 0, 0, 1, 0, 0 } };
	protected double ctm[] = { 1, 0, 0, 1, 0, 0 };

	public final static double EPSILON = 0.0001;
	protected double frames = 100;
	protected JJPoint center;

	/**
	 * Get the value of center.
	 *
	 * @return Value of center.
	 */
	public JJPoint getCenter() {
		return center;
	}

	/**
	 * Set the value of center.
	 *
	 * @param v
	 *            Value to assign to center.
	 */
	public void setCenter(final JJPoint v) {
		this.center = v;
	}

	JJMatrixInterpol(final JJGraphAnimator b) {
		graphAnimator = b;
	}

	public void init(final JJAnimationNode[] bn) {
		animationNodes = bn;
		reset();
		if (bn != null)
			for (final JJAnimationNode animationNode : animationNodes) {
				final JJPoint p = animationNode.getPosition();
				animationNode.setCustomPos(new JJPoint(p));
			}
	}

	public void setSourceMat(final double m[][]) {
		sourceMat = new double[m.length][6];

		for (int k = 0; k < m.length; k++) {
			for (int i = 0; i < 6; i++) {
				sourceMat[k][i] = m[k][i];
			}
		}

	}

	public void setTargetMat(final double m[][]) {
		targetMat = new double[m.length][6];
		for (int k = 0; k < m.length; k++) {
			for (int i = 0; i < 6; i++) {
				targetMat[k][i] = m[k][i];
			}
		}
	}

	// public boolean hasNext()
	// {
	// for(int k = 0;k<sourceMat.length;k++){
	// for(int i=0; i<6; i++){
	// if( Math.abs(mat[k][i]- targetMat[k][i]) > EPSILON)
	// return true;
	// }
	// }

	// return false;
	// }

	public void resetCTM() {
		ctm[0] = ctm[3] = 1;
		ctm[1] = ctm[2] = ctm[4] = ctm[5] = 0;
	}

	public void frame(final double d) {
		for (int k = 0; k < mat.length; k++) {
			for (int i = 0; i < 6; i++) {
				final double dist = targetMat[k][i] - sourceMat[k][i];
				mat[k][i] = sourceMat[k][i] + d * dist;
			}
		}

		resetCTM();

		for (final double[] element : mat) {
			GFMatrix.concatmatrix(ctm, element, ctm);
		}

		for (final JJAnimationNode animationNode : animationNodes) {
			final JJPoint p = animationNode.getCustomPos();
			final JJPoint np = new JJPoint(ctm[4] + p.x * ctm[0] + p.y * ctm[2], ctm[5] + p.x * ctm[1] + p.y * ctm[3]);
			animationNode.setPosition(np);
		}
	}

	// public boolean next()
	// {
	// if(!hasNext())
	// return false;

	// for(int k=0; k<mat.length; k++){
	// for(int i=0; i<6; i++){
	// double dist = targetMat[k][i] - sourceMat[k][i];
	// double step = dist * graphAnimator.getSpeed()/frames;
	// if( Math.abs(mat[k][i] - targetMat[k][i]) < step){
	// mat[k][i] = targetMat[k][i];
	// }
	// else
	// mat[k][i] += step;
	// }
	// }

	// resetCTM();

	// for(int k=0;k<mat.length;k++){
	// GFMatrix.concatmatrix(ctm, mat[k], ctm);
	// }

	// for(int i=0; i< animationNodes.length; i++){
	// JJPoint p = animationNodes[i].getCustomPos();
	// JJPoint np = new JJPoint(ctm[4] + p.x*ctm[0] + p.y*ctm[2],
	// ctm[5] + p.x*ctm[1] + p.y*ctm[3]);
	// animationNodes[i].setPosition(np);
	// }

	// return hasNext();
	// }

	public JJPoint transform(final JJPoint p) {
		return new JJPoint(ctm[4] + p.x * ctm[0] + p.y * ctm[2], ctm[5] + p.x * ctm[1] + p.y * ctm[3]);
	}

	public void reset() {
		mat = new double[sourceMat.length][6];
		for (int k = 0; k < sourceMat.length; k++) {
			for (int i = 0; i < 6; i++) {
				mat[k][i] = sourceMat[k][i];
			}
		}

	}

} // JJMatrixInterpol
