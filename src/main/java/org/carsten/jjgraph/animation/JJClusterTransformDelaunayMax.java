/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

import org.carsten.jjgraph.util.Edge;
import org.carsten.jjgraph.util.JJArrayRegressCollection;
import org.carsten.jjgraph.util.JJLinAlgException;
import org.carsten.jjgraph.util.JJLinearRegress;
import org.carsten.jjgraph.util.JJPoint;
import org.carsten.jjgraph.util.JJRegressPointPair;

public class JJClusterTransformDelaunayMax extends JJClusterTransformDelaunay {
	public JJClusterTransformDelaunayMax(final JJGraphAnimator b) {
		super(b);
	}

	@Override
	protected double computeEdgeWeight(final Edge e) {
		if (e.t1 == null || e.t2 == null)
			return -1;

		final int p[] = new int[3];

		p[0] = e.a;
		p[1] = e.b;
		p[2] = e.t1.opposite(e);

		final JJRegressPointPair array[] = new JJRegressPointPair[3];

		for (int k = 0; k < 3; k++) {
			array[k] = new JJRegressPointPair();

			if (p[k] < 0)
				return -1;

			array[k].p1 = startFramePoints[p[k]];
			array[k].p2 = endFramePoints[p[k]];
		}
		try {

			final double m[] = JJLinearRegress.regress(new JJArrayRegressCollection(array), new JJPoint());

			final int point = e.t2.opposite(e);
			if (point < 0)
				return -1;

			// double weight1 = JJPoint.dist(endFramePoints[point],
			// GFMatrix.transform(startFramePoints[point],
			// m));

			// double area1 = triangleArea(array[0].p1, array[1].p1,
			// array[2].p1);

			array[2].p1 = startFramePoints[point];
			array[2].p2 = endFramePoints[point];
			final double m2[] = JJLinearRegress.regress(new JJArrayRegressCollection(array), new JJPoint());

			// point = e.t1.opposite(e);
			// if(point <0)
			// return -1;
			// double weight2 = JJPoint.dist(endFramePoints[point],
			// GFMatrix.transform(startFramePoints[point],
			// m));

			// double area2 = triangleArea(array[0].p1, array[1].p1,
			// array[2].p1);

			// return Math.max(weight1, weight2);
			double res = 0;
			for (int i = 0; i < m.length; i++) {
				res += Math.abs(m[i] - m2[i]);
			}

			return res * 1000;
		} catch (final JJLinAlgException ex) {
		}

		return -1;
	}

}
