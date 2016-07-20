/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

/**
 * JJClusterTransformDelaunay.java
 *
 *
 * Created: Wed Feb 16 16:32:29 2000
 *
 * @author Carsten Friedrich
 * @version
 *
 */
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.carsten.jjgraph.util.Debug;
import org.carsten.jjgraph.util.Edge;
import org.carsten.jjgraph.util.GFMatrix;
import org.carsten.jjgraph.util.JJArrayRegressCollection;
import org.carsten.jjgraph.util.JJBlueLine;
import org.carsten.jjgraph.util.JJDelaunay;
import org.carsten.jjgraph.util.JJGraphicObject;
import org.carsten.jjgraph.util.JJHistogramWindow;
import org.carsten.jjgraph.util.JJLinAlgException;
import org.carsten.jjgraph.util.JJLinearRegress;
import org.carsten.jjgraph.util.JJPlotter;
import org.carsten.jjgraph.util.JJPoint;
import org.carsten.jjgraph.util.JJRegressPointPair;
import org.carsten.jjgraph.util.Triangle;

public class JJClusterTransformDelaunay extends JJClusterTransform {
	public JJClusterTransformDelaunay(final JJGraphAnimator b) {
		super(b);
	}

	protected double computeEdgeWeight(final Edge e) {
		if (e.t1 == null || e.t2 == null)
			return -1;

		final int p[] = new int[4];

		p[0] = e.a;
		p[1] = e.b;
		p[2] = e.t1.opposite(e);
		p[3] = e.t2.opposite(e);

		final JJRegressPointPair array[] = new JJRegressPointPair[4];

		for (int k = 0; k < 4; k++) {
			array[k] = new JJRegressPointPair();

			if (p[k] < 0)
				return -1;

			array[k].p1 = startFramePoints[p[k]];
			array[k].p2 = endFramePoints[p[k]];
		}
		try {

			final double m[] = JJLinearRegress.regress(new JJArrayRegressCollection(array), new JJPoint());

			double weight = 0;
			for (int k = 0; k < 4; k++) {
				weight += JJPoint.dist(endFramePoints[p[k]], GFMatrix.transform(startFramePoints[p[k]], m));
			}
			Debug.println("Weight: " + weight);

			return weight;
		} catch (final JJLinAlgException ex) {
		}

		return -1;
	}

	@Override
	void computeDataSet(final Set<JJClusterDatum> datumSet) {
		final JJDelaunay dStart = new JJDelaunay(startFramePoints);
		dStart.doDelaunay();

		double minWeight = -1;
		double maxWeight = -1;

		final Set<Triangle> delTriangleSet = dStart.getTriangles();
		final List<JJGraphicObject> l = new LinkedList<>();
		final List<JJGraphicObject> l2 = new LinkedList<>();
		final JJPoint p[] = new JJPoint[3];
		final double weights[] = new double[3];

		final double allWeights[] = new double[delTriangleSet.size() * 3];
		int counter = 0;

		for (final Triangle t : delTriangleSet) {
			p[0] = startFramePoints[t.p1];
			p[1] = startFramePoints[t.p2];
			p[2] = startFramePoints[t.p3];

			weights[0] = computeEdgeWeight(t.findEdge(t.p1, t.p2));
			allWeights[counter++] = weights[0];
			weights[1] = computeEdgeWeight(t.findEdge(t.p2, t.p3));
			allWeights[counter++] = weights[1];
			weights[2] = computeEdgeWeight(t.findEdge(t.p3, t.p1));
			allWeights[counter++] = weights[2];

			for (int i = 0; i < 3; i++) {
				if ((minWeight == -1) || (minWeight > weights[i]))
					minWeight = weights[i];
				if ((maxWeight == -1) || (maxWeight < weights[i]))
					maxWeight = weights[i];

			}

			final JJPoint ep[] = new JJPoint[3];
			ep[0] = endFramePoints[t.p1];
			ep[1] = endFramePoints[t.p2];
			ep[2] = endFramePoints[t.p3];

			l.add(new JJTriangleShape(t, p, weights, ep));
		}

		Arrays.sort(allWeights);
		for (int i = 1; i < allWeights.length; i++) {
			l2.add(new JJBlueLine(new JJPoint(allWeights[i - 1], -(i - 1)), new JJPoint(allWeights[i], -i)));
		}

		new JJPlotter(l2);

		final JJHistogramWindow w = new JJHistogramWindow(allWeights);
		final JJDelaunayPlotter lplotter = new JJDelaunayPlotter(l, minWeight, maxWeight);
		w.addHistogramListener(lplotter);

	}

	@Override
	JJAnimationCluster[] clusterDataSet(final Set<JJClusterDatum> dataSet) {
		return null;
	}

	@Override
	int findBestCluster(final JJClusterDatum m, final JJAnimationCluster clusters[]) {
		return -1;

	}

	@Override
	int findBestCluster(final JJClusterTransformNode m, final JJAnimationCluster clusters[]) {
		return -1;

	}

	@Override
	void initMatrices() {
		final Set<JJClusterDatum> dataSet = new HashSet<>();

		for (int i = 0; i < animationNodes.length; i++) {
			startFramePoints[i] = animationNodes[i].getStartPosition();
			endFramePoints[i] = animationNodes[i].getEndPosition();
			transformNodes[i] = new JJClusterTransformNode(startFramePoints[i], endFramePoints[i], this,
					animationNodes[i]);
		}

		computeDataSet(dataSet);
		// displayMatrixSet(matrixSet);

		// JJAnimationCluster clusters[] = clusterDataSet(dataSet);

		// stripMatrixSet(matrixSet);

		// Now we find the best matrix for each point
		//
		// We also make sure that each valid cluster has at least 3 nodes
		// associated.

		// List clusterNodes[] = assignNodesToClusters(clusters);

		// computeMatrixForNewCentersAndAssignMatrix(clusterNodes, clusters);
	}

	protected double triangleArea(final JJPoint p1, final JJPoint p2, final JJPoint p3) {
		final double a = JJPoint.dist(p1, p2);
		final double b = JJPoint.dist(p2, p3);
		final double c = JJPoint.dist(p3, p1);
		final double s = (a + b + c) / 2.0;
		return Math.sqrt(s * (s - a) * (s - b) * (s - c));
	}

}
