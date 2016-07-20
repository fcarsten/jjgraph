/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

import org.carsten.jjgraph.util.Debug;
import org.carsten.jjgraph.util.Edge;
import org.carsten.jjgraph.util.GFMatrix;
import org.carsten.jjgraph.util.JJArrayRegressCollection;
import org.carsten.jjgraph.util.JJLinAlgException;
import org.carsten.jjgraph.util.JJLinearRegress;
import org.carsten.jjgraph.util.JJPoint;
import org.carsten.jjgraph.util.JJRegressPointPair;

public class JJClusterTransformDelaunayAvg extends JJClusterTransformDelaunay {
	public JJClusterTransformDelaunayAvg(final JJGraphAnimator b) {
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
		// p[3] = e.t2.opposite(e);

		final JJRegressPointPair array[] = new JJRegressPointPair[3];

		for (int k = 0; k < 3; k++) {
			array[k] = new JJRegressPointPair();

			if (p[k] < 0)
				return -1;

			array[k].p1 = startFramePoints[p[k]];
			array[k].p2 = endFramePoints[p[k]];
		}
		try {

			double m[] = JJLinearRegress.regress(new JJArrayRegressCollection(array), new JJPoint());

			int point = e.t2.opposite(e);
			if (point < 0)
				return -1;

			final double weight1 = JJPoint.dist(endFramePoints[point], GFMatrix.transform(startFramePoints[point], m));

			array[2].p1 = startFramePoints[point];
			array[2].p2 = endFramePoints[point];
			m = JJLinearRegress.regress(new JJArrayRegressCollection(array), new JJPoint());

			point = e.t1.opposite(e);
			if (point < 0)
				return -1;
			final double weight2 = JJPoint.dist(endFramePoints[point], GFMatrix.transform(startFramePoints[point], m));

			Debug.println("Weight1: " + weight1);
			Debug.println("Weight2: " + weight2);

			return weight1 + weight2;
		} catch (final JJLinAlgException ex) {
		}

		return -1;
	}

}
