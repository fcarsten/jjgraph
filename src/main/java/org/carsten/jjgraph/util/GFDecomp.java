/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

public class GFDecomp {
	public double xScale = 1;
	public double yScale = 1;
	public double angle1 = 0;
	public double angle2 = 0;
	public boolean flip = false;
	public double xTrans = 0;
	public double yTrans = 0;
	public double skew = 0;

	@Override
	public String toString() {
		return "";
		// "\nscale X: " + xScale +
		// ",\n scaleY: " + yScale+
		// ",\n angle 1: " + angle1 +
		// " (angle 2: " + angle2 +
		// "),\n translation : " + xTrans+ "," + yTrans +
		// ",\n skew: " + skew +
		// ",\n flip: " + flip;
	}

	public GFDecomp() {
	}

	public void decomposeTransformation(final double m[]) {
		final JJPoint p1 = GFMatrix.dtransform(1, 0, m); // ,&dx,&dy);
		final JJPoint p2 = p1.polarCoordsOf(); // dx,dy,xsc,angle);
		final JJPoint p3 = GFMatrix.dtransform(0, 1, m); // ,&dx,&dy);
		final JJPoint p4 = p3.polarCoordsOf(); // dx,dy,ysc,skew);

		xScale = p2.x;
		angle1 = p2.y;

		yScale = p4.x;
		angle2 = p4.y;

		skew = angle2 - angle1;

		// angle2 is computed with respect to the wrong axis,
		// so we have to adjust.

		if (angle2 > 0)
			angle2 -= 90;
		else
			angle2 += 270;

		while (skew < 0)
			skew += 360;
		while (skew >= 360)
			skew -= 360;
		if (skew >= 180) {
			skew -= 270;
		} else
			skew -= 90;

		flip = ((m[0] * m[3] - m[1] * m[2]) < 0);

		if (flip)
			angle2 -= 180;

		xTrans = m[4];
		yTrans = m[5];

		// Debug.println("xScale : " + xScale);
		// Debug.println("yScale : " + yScale);
		// Debug.println("xTrans : " + xTrans);
		// Debug.println("yTrans : " + yTrans);
		// Debug.println("angle1 : " + angle1);
		// Debug.println("angle2 : " + angle2);
		// Debug.println("skew : " + skew);
		// Debug.println("flip : " + flip);
	}

	public GFDecomp(final double m[]) {
		decomposeTransformation(m);
	}

	public GFDecomp(final double m1, final double m2, final double m3, final double m4, final double m5,
			final double m6) {
		final double m[] = { m1, m2, m3, m4, m5, m6 };

		decomposeTransformation(m);
	}

} // GFDecomp
