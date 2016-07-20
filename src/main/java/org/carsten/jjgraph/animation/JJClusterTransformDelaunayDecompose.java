/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

import org.carsten.jjgraph.util.Debug;
import org.carsten.jjgraph.util.Edge;
import org.carsten.jjgraph.util.GFDecomp;
import org.carsten.jjgraph.util.JJArrayRegressCollection;
import org.carsten.jjgraph.util.JJLinAlgException;
import org.carsten.jjgraph.util.JJLinearRegress;
import org.carsten.jjgraph.util.JJPoint;
import org.carsten.jjgraph.util.JJRegressPointPair;

public class JJClusterTransformDelaunayDecompose extends JJClusterTransformDelaunay {
	public JJClusterTransformDelaunayDecompose(final JJGraphAnimator b) {
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
			final JJPoint center = new JJPoint(); // JJPoint.plus(array[0].p1,
													// array[1].p1);
			// center.plusA(array[2].p1);
			// center.divA(3);

			final double m[] = JJLinearRegress.regress(new JJArrayRegressCollection(array), center);

			final int point = e.t2.opposite(e);
			if (point < 0)
				return -1;

			array[2].p1 = startFramePoints[point];
			array[2].p2 = endFramePoints[point];

			// center = JJPoint.plus(array[0].p1, array[1].p1);
			// center.plusA(array[2].p1);
			// center.divA(3);

			final double m2[] = JJLinearRegress.regress(new JJArrayRegressCollection(array), center);

			final GFDecomp d1 = new GFDecomp(m);
			final GFDecomp d2 = new GFDecomp(m2);

			double result = 0;

			{
				double a = d1.angle1;
				double b = d2.angle1;

				while (a > 360)
					a -= 360;
				while (a < 0)
					a += 360;

				while (b > 360)
					b -= 360;
				while (b < 0)
					b += 360;

				double c = Math.abs(a - b);
				if (c > 180)
					c = 360 - c;

				Debug.println("Winkel: " + c);

				result = Math.min(c, 360);
			}

			{
				double x1 = d1.xScale / d2.xScale;
				double y1 = d1.yScale / d2.yScale;
				if (x1 < 1)
					x1 = 1.0 / x1;
				if (y1 < 1)
					y1 = 1.0 / y1;

				// if(x1 > 5)
				// x1 = 5;
				// if(y1 > 5)
				// y1 = 5;

				// result += 36 * (y1 + x1);
				final double a = Math.min(36 * (y1 + x1 - 2), 360);
				if (a > result)
					Debug.println("Scale : " + a);

				result = Math.max(result, a);

			}

			{
				final double a = Math.min(Math.abs(d1.skew - d2.skew) * 4, 360);
				if (a > result)
					Debug.println("Skew : " + a);
				result = Math.max(result, a);
				// result += a;
			}
			// {
			// double dis1 = d1.xTrans - d2.xTrans;
			// double dis2 = d1.yTrans - d2.yTrans;
			// double a = Math.min(Math.sqrt(dis1*dis1 + dis2*dis2), 360);
			// // result += a
			// if(a > result)
			// Debug.println("Trans : " + a);
			// result = Math.max(result, a);
			// }
			{
				if (d1.flip != d2.flip)
					result = 360;
			}
			return result;
		} catch (final JJLinAlgException ex) {
		}

		return -1;
	}

}
