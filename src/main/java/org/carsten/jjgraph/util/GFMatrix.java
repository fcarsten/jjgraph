/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

/**
 * GFMatrix.java
 *
 *
 * Created: Mon Nov 06 15:56:00 2000
 *
 * @author
 * @version
 */

public class GFMatrix {
	public static JJPoint dtransform(final double x, final double y, final double m[]) {
		return new JJPoint(m[0] * x + m[2] * y, m[1] * x + m[3] * y);
	}

	public static JJPoint transform(final double x, final double y, final double m[]) {
		return new JJPoint(m[0] * x + m[2] * y + m[4], m[1] * x + m[3] * y + m[5]);
	}

	public static JJPoint transform(final JJPoint p, final double m[]) {
		return transform(p.x, p.y, m);
	}

	public static void concatmatrix(final double m1[], final double m2[], final double m3[]) {
		final double mt[] = new double[6];

		mt[0] = m1[0] * m2[0] + m1[1] * m2[2];
		mt[1] = m1[0] * m2[1] + m1[1] * m2[3];
		mt[2] = m1[2] * m2[0] + m1[3] * m2[2];
		mt[3] = m1[2] * m2[1] + m1[3] * m2[3];
		mt[4] = m1[4] * m2[0] + m1[5] * m2[2] + m2[4];
		mt[5] = m1[4] * m2[1] + m1[5] * m2[3] + m2[5];
		m3[0] = mt[0];
		m3[1] = mt[1];
		m3[2] = mt[2];
		m3[3] = mt[3];
		m3[4] = mt[4];
		m3[5] = mt[5];
	}

	public static double[] transposematrix(final double m[]) {
		final double mt[] = { m[0], m[2], m[1], m[3], m[4], m[5] };
		return mt;
	}

	public static void scalematrix(final double xsc, final double ysc, final double mat[]) {
		mat[0] = xsc;
		mat[1] = 0;
		mat[2] = 0;
		mat[3] = ysc;
		mat[4] = 0;
		mat[5] = 0;
	}

	public static void rotatematrix(final double angle, final double mat[]) {
		mat[0] = Math.cos(angle * Math.atan2(0, -1) / 180.0);
		mat[1] = Math.sin(angle * Math.atan2(0, -1) / 180.0);
		mat[2] = -mat[1];
		mat[3] = mat[0];
		mat[4] = 0;
		mat[5] = 0;
	}

	public static void skewmatrix(final double skew, final double m[]) {
		m[0] = 1;
		m[1] = 0;
		m[2] = -Math.sin(skew * Math.atan2(0, -1) / 180);
		m[3] = Math.cos(skew * Math.atan2(0, -1) / 180);
		m[4] = 0;
		m[5] = 0;
	}

	public GFMatrix() {

	}

	public static void polarDecompose(final double m[], final double q[], final double s[], final boolean dummy) {
		final double a = m[0];
		final double b = m[2];
		final double c = m[1];
		final double d = m[3];

		final double det = (m[0] * m[3] - m[2] * m[1]);
		double sigDet = 1.0;

		if (det < 0)
			sigDet = -1;

		q[0] = a + sigDet * d;
		q[1] = c - sigDet * b;
		q[2] = b - sigDet * c;
		q[3] = d + sigDet * a;

		double nenner = 2 * a * d - 2 * b * c + a * a + b * b + c * c + d * d;

		s[0] = a * d - b * c + a * a + c * c;
		s[1] = a * b + c * d;
		s[2] = a * b + c * d;
		s[3] = a * d - b * c + b * b + d * d;

		if (sigDet < 0) {
			nenner = -2 * a * d + 2 * b * c + a * a + b * b + c * c + d * d;

			s[0] = -a * d + b * c + a * a + c * c;
			s[3] = -a * d + b * c + b * b + d * d;

		}

		final double col = Math.sqrt(q[0] * q[0] + q[1] * q[1]);

		// Debug.println("Column 1: " + (q[0]*q[0] + q[1] * q[1]));
		// Debug.println("Column 2: " + (q[2]*q[2] + q[3] * q[3]));

		for (int i = 0; i < 4; i++) {
			s[i] *= col / nenner;
			q[i] /= col;
		}

		// Do we still have a flip in the rotation???

		final double qdet = q[0] * q[3] - q[1] * q[2];

		if (qdet < 0) {
			final double mm[] = { 1, 0, 0, -1, 0, 0 };

			concatmatrix(s, mm, s);
			concatmatrix(mm, q, q);
		}

		concatmatrix(transposematrix(q), s, s);
		concatmatrix(s, q, s);

		// Debug.println("M:");
		// Debug.println("["+ m[0] + " " + m[2] + "]");
		// Debug.println("["+ m[1] + " " + m[3] + "]");
		// Debug.println("Determinant: " + det);

		// Debug.println("Q:");
		// Debug.println("["+ q[0] + " " + q[2] + "]");
		// Debug.println("["+ q[1] + " " + q[3] + "]");

		// Debug.println("S:");
		// Debug.println("["+ s[0] + " " + s[2] + "]");
		// Debug.println("["+ s[1] + " " + s[3] + "]");
		// Debug.println("Nenner: "+ nenner);

		// if(Debug.DEBUG){
		// double tm[]= {1,0,0,1,0,0};
		// GFMatrix.concatmatrix(tm,q,tm);
		// GFMatrix.concatmatrix(tm,s,tm);
		// Debug.println("TM:");
		// Debug.println("["+ tm[0] + " " + tm[2] + "]");
		// Debug.println("["+ tm[1] + " " + tm[3] + "]");
		// }

	}

	public static double[] composeTrans(final double alpha, final double skew, final double xScale, final double yScale,
			final double transX, final double transY, final boolean flip, final JJPoint centerNew) {
		final double m[] = { 1, 0, 0, 1, -centerNew.x, -centerNew.y };
		final double m2[] = { 1, 0, 0, 1, 0, 0 };

		if (flip) {
			scalematrix(1, -1, m2);
			concatmatrix(m, m2, m);
		}

		scalematrix((xScale != 0) ? xScale : 1, (yScale != 0) ? yScale : 1, m2);
		concatmatrix(m, m2, m);

		skewmatrix(skew, m2);
		concatmatrix(m, m2, m);

		rotatematrix(alpha, m2);
		concatmatrix(m, m2, m);

		m[4] += transX;
		m[5] += transY;
		m[4] += centerNew.x;
		m[5] += centerNew.y;
		return m;
	}

} // GFMatrix
