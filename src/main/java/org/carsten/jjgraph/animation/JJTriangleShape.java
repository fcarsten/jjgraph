/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

import org.carsten.jjgraph.util.Debug;
import org.carsten.jjgraph.util.GFDecomp;
import org.carsten.jjgraph.util.JJArrayRegressCollection;
import org.carsten.jjgraph.util.JJLinAlgException;
import org.carsten.jjgraph.util.JJLinearRegress;
import org.carsten.jjgraph.util.JJPoint;
import org.carsten.jjgraph.util.JJRegressPointPair;
import org.carsten.jjgraph.util.Triangle;

public class JJTriangleShape extends JJClusterShape {
	protected Triangle triangle;
	JJPoint points[] = new JJPoint[3];
	JJPoint endPoints[] = new JJPoint[3];

	double area2(final JJPoint A, final JJPoint B, final JJPoint C) {
		return (A.x - C.x) * (B.y - C.y) - (A.y - C.y) * (B.x - C.x);
	}

	public boolean insideTriangle(final JJPoint a) // ABC is assumed to be
													// counter-clockwise
	{
		final double a1 = area2(points[0], points[1], a);
		final double a2 = area2(points[1], points[2], a);
		final double a3 = area2(points[2], points[0], a);

		return ((a1 >= 0) && (a2 >= 0) && (a3 >= 0)) || ((a1 <= 0) && (a2 <= 0) && (a3 <= 0));
	}

	public JJTriangleShape(final Triangle t, final JJPoint p[], final double weights[], final JJPoint ep[]) {
		super(p, weights);
		for (int i = 0; i < 3; i++) {
			points[i] = new JJPoint(p[i]);
			if (ep != null) {
				endPoints[i] = new JJPoint(ep[i]);
			}
		}
		triangle = t;
	}

	@Override
	public void mouseClicked(final JJPoint p) {
		if (insideTriangle(p)) {
			Debug.println("Found triangle!");
			final JJRegressPointPair array[] = new JJRegressPointPair[3];
			for (int k = 0; k < 3; k++) {
				array[k] = new JJRegressPointPair();
				array[k].p1 = points[k];
				array[k].p2 = endPoints[k];
			}
			final JJPoint center = new JJPoint(); // JJPoint.plus(points[0],
			// points[1]).plusA(points[2]).divA(3);

			try {
				final double m[] = JJLinearRegress.regress(new JJArrayRegressCollection(array), center);

				final GFDecomp d1 = new GFDecomp(m);
				Debug.println("Decompose : " + d1);
			} catch (final JJLinAlgException ex) {
			}
		}
	} // JJClusterShape
}
