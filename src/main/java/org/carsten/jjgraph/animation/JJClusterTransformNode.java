/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

/**
 * JJClusterTransformNode.java
 *
 *
 * Created: Wed Feb 16 16:32:29 2000
 *
 * @author Carsten Friedrich
 * @version
 *
 */
import java.util.HashSet;
import java.util.Set;

import org.carsten.jjgraph.util.GFMatrix;
import org.carsten.jjgraph.util.GFTransformMatrix;
import org.carsten.jjgraph.util.JJPoint;

public class JJClusterTransformNode {
	private final Set<GFTransformMatrix> matrices;
	JJPoint startPoint;
	JJPoint endPoint;
	GFTransformMatrix transformMatrix;
	JJClusterTransform clusterTrans;
	JJAnimationNode animationNode;

	private JJPoint center = new JJPoint();

	public JJPoint getCenter() {
		return center;
	}

	public void setCenter(final JJPoint v) {
		this.center = v;
	}

	protected double ctm[] = { 1, 0, 0, 1, 0, 0 };

	protected double targetAngle;
	protected double sourceAngle = 0;

	GFTransformMatrix sourceMat = new GFTransformMatrix(GFTransformMatrix.unitMatrix);
	GFTransformMatrix targetMat;

	public void resetCTM() {
		ctm[0] = ctm[3] = 1;
		ctm[1] = ctm[2] = ctm[4] = ctm[5] = 0;
	}

	public JJClusterTransformNode(final JJPoint sp, final JJPoint ep, final JJClusterTransform ct,
			final JJAnimationNode an) {
		clusterTrans = ct;
		animationNode = an;

		matrices = new HashSet<>();
		startPoint = sp;
		endPoint = ep;
	}

	public void addMatrix(final GFTransformMatrix b) {
		matrices.add(b);
	}

	@Override
	public String toString() {
		String result = "";

		if (transformMatrix != null)
			result += "Best matrix: " + transformMatrix;
		else
			for (final Object element : matrices) {
				final GFTransformMatrix b = (GFTransformMatrix) element;
				result += b;
			}
		return result;
	}

	// public JJClusterTransformMatrix selectBestTransformation(Set matrixMap)
	// {
	// double d[]= {1,0,0,1, endPoint.x-startPoint.x,
	// endPoint.y-startPoint.y};
	// JJClusterTransformMatrix bestMatrix = new JJClusterTransformMatrix(d,
	// null);
	// int quality =0;

	// for(Iterator iter= matrices.iterator(); iter.hasNext();){
	// JJClusterTransformMatrix tmpM = (JJClusterTransformMatrix) iter.next();
	// int m = tmpM.value; //(JJClusterTransform.MyInt) matrixMap.get(tmpM);
	// if(m > quality){
	// quality = m;
	// bestMatrix = tmpM;
	// }
	// else if(bestMatrix == null){
	// quality = 0;
	// bestMatrix = tmpM;
	// }

	// }

	// center = clusterTrans.graphAnimator.avgCenter;
	// setTransformMatrix(bestMatrix);

	// return bestMatrix;
	// }

	public void setTransformMatrix(final GFTransformMatrix m) {
		// double d[]= {1,0,0,1, endPoint.x-startPoint.x,
		// endPoint.y-startPoint.y};

		final boolean mergeWithBlending = true;

		if (m != null)
			transformMatrix = new GFTransformMatrix(m);
		else
			transformMatrix = new GFTransformMatrix(GFTransformMatrix.unitMatrix);

		if (mergeWithBlending) {
			final JJPoint p = transformMatrix.transform(startPoint.x - center.x, startPoint.y - center.y);
			final double d[] = { 1, 0, 0, 1, endPoint.x - (p.x + center.x), endPoint.y - (p.y + center.y) };
			transformMatrix.concatmatrix(d);
		}

		final double q[] = new double[6];
		final double s[] = new double[6];

		transformMatrix.polarDecompose(q, s);
		s[4] = transformMatrix.m[4];
		s[5] = transformMatrix.m[5];

		targetMat = new GFTransformMatrix(s);

		targetAngle = Math.acos(q[0]);
		if (Math.abs(Math.sin(targetAngle) - q[1]) > JJMatrixInterpol.EPSILON)
			targetAngle = -targetAngle;

		final double t = Math.toDegrees(targetAngle);
		if (t < -140) {
			targetAngle = Math.toRadians(t + 360);
		}
	}

	// private JJPoint center;

	// /**
	// * Get the value of center.
	// * @return Value of center.
	// */
	// public JJPoint getCenter() {return center;}

	// /**
	// * Set the value of center.
	// * @param v Value to assign to center.
	// */
	// public void setCenter(JJPoint v) {this.center = v;}

	double rm[] = new double[6];
	double currentMat[] = new double[6];

	// public void next()
	// {
	// double dist = targetAngle - sourceAngle;

	// double currentAngle = sourceAngle + (dist * clusterTrans.currentFrame /
	// clusterTrans.maxFrames);

	// for(int i=0; i<6; i++){
	// dist = targetMat.m[i] - sourceMat.m[i];

	// currentMat[i] = sourceMat.m[i] + (dist * clusterTrans.currentFrame /
	// clusterTrans.maxFrames);
	// }

	// resetCTM();
	// ctm[4]= - getCenter().x;
	// ctm[5]= - getCenter().y;

	// GFMatrix.rotatematrix(Math.toDegrees(currentAngle), rm);
	// GFMatrix.concatmatrix(ctm, rm, ctm);
	// GFMatrix.concatmatrix(ctm, currentMat, ctm);

	// ctm[4]+= getCenter().x;
	// ctm[5]+= getCenter().y;

	// JJPoint np = new JJPoint(ctm[4] + startPoint.x*ctm[0] +
	// startPoint.y*ctm[2],
	// ctm[5] + startPoint.x*ctm[1] + startPoint.y*ctm[3]);
	// animationNode.setPosition(np);
	// }

	public void frame(final double t) {
		double dist = targetAngle - sourceAngle;

		final double currentAngle = sourceAngle + (t * dist);

		for (int i = 0; i < 6; i++) {
			dist = targetMat.m[i] - sourceMat.m[i];

			currentMat[i] = sourceMat.m[i] + (t * dist);
		}

		resetCTM();
		ctm[4] = -getCenter().x;
		ctm[5] = -getCenter().y;

		GFMatrix.rotatematrix(Math.toDegrees(currentAngle), rm);
		GFMatrix.concatmatrix(ctm, rm, ctm);
		GFMatrix.concatmatrix(ctm, currentMat, ctm);

		ctm[4] += getCenter().x;
		ctm[5] += getCenter().y;

		final JJPoint np = new JJPoint(ctm[4] + startPoint.x * ctm[0] + startPoint.y * ctm[2],
				ctm[5] + startPoint.x * ctm[1] + startPoint.y * ctm[3]);
		animationNode.setPosition(np);
	}

}
