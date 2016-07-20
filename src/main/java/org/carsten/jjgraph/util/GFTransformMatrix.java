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

public class GFTransformMatrix extends GFMatrix implements Comparable<GFTransformMatrix> {
	public double m[];
	public final static GFTransformMatrix unitMatrix = new GFTransformMatrix(1, 0, 0, 1, 0, 0);

	public JJPoint dtransform(final double x, final double y) {
		return new JJPoint(m[0] * x + m[2] * y, m[1] * x + m[3] * y);
	}

	public JJPoint transform(final double x, final double y) {
		return new JJPoint(m[0] * x + m[2] * y + m[4], m[1] * x + m[3] * y + m[5]);
	}

	public JJPoint transform(final JJPoint p) {
		return transform(p.x, p.y);
	}

	public GFTransformMatrix transpose() {
		final double mt[] = { m[0], m[2], m[1], m[3], m[4], m[5] };
		return new GFTransformMatrix(mt);
	}

	public GFTransformMatrix(final GFTransformMatrix m1) {
		this(m1.m);
	}

	public GFTransformMatrix(final double m1[]) {
		m = new double[6];
		for (int i = 0; i < 6; i++) {
			m[i] = m1[i];
		}
	}

	public GFTransformMatrix() {
		m = new double[6];
	}

	public GFTransformMatrix(final double a, final double b, final double c, final double d, final double e,
			final double f) {
		m = new double[6];
		m[0] = a;
		m[1] = b;
		m[2] = c;
		m[3] = d;
		m[4] = e;
		m[5] = f;
	}

	public void plusA(final double a, final double b, final double c, final double d, final double e, final double f) {
		m[0] += a;
		m[1] += b;
		m[2] += c;
		m[3] += d;
		m[4] += e;
		m[5] += f;
	}

	public void plusA(final GFTransformMatrix tm) {
		for (int i = 0; i < 6; i++) {
			m[i] += tm.m[i];
		}
	}

	public double dist(final GFTransformMatrix tm) {
		double dist = 0;

		for (int i = 0; i < 6; i++) {
			dist += (m[i] - tm.m[i]) * (m[i] - tm.m[i]);
		}
		return dist;
	}

	public void divA(final double d) {
		if (d == 0)
			return;

		for (int i = 0; i < 6; i++) {
			m[i] /= d;
		}
	}

	public void concatmatrix(final double m2[]) {
		final double mt[] = new double[6];

		mt[0] = m[0] * m2[0] + m[1] * m2[2];
		mt[1] = m[0] * m2[1] + m[1] * m2[3];
		mt[2] = m[2] * m2[0] + m[3] * m2[2];
		mt[3] = m[2] * m2[1] + m[3] * m2[3];
		mt[4] = m[4] * m2[0] + m[5] * m2[2] + m2[4];
		mt[5] = m[4] * m2[1] + m[5] * m2[3] + m2[5];
		m[0] = mt[0];
		m[1] = mt[1];
		m[2] = mt[2];
		m[3] = mt[3];
		m[4] = mt[4];
		m[5] = mt[5];
	}

	public void precatmatrix(final double m1[]) {
		final double mt[] = new double[6];

		mt[0] = m1[0] * m[0] + m1[1] * m[2];
		mt[1] = m1[0] * m[1] + m1[1] * m[3];
		mt[2] = m1[2] * m[0] + m1[3] * m[2];
		mt[3] = m1[2] * m[1] + m1[3] * m[3];
		mt[4] = m1[4] * m[0] + m1[5] * m[2] + m[4];
		mt[5] = m1[4] * m[1] + m1[5] * m[3] + m[5];
		m[0] = mt[0];
		m[1] = mt[1];
		m[2] = mt[2];
		m[3] = mt[3];
		m[4] = mt[4];
		m[5] = mt[5];
	}

	public void polarDecompose(final double q[], final double s[]) {
		super.polarDecompose(m, q, s, true);
	}

	@Override
	public int hashCode() {
		double res = 0;
		for (int i = 0; i < 6; i++) {
			res += Math.floor(m[i] * 10000.0 + 0.5) / 10000.0;
		}
		return (int) res;
	}

	@Override
	public boolean equals(final Object o) {
		if ((o == null) || !(o instanceof GFTransformMatrix)) {
			return false;
		}
		if (o == this)
			return true;

		final GFTransformMatrix e = (GFTransformMatrix) o;

		for (int i = 0; i < 6; i++) {
			if ((Math.floor(m[i] * 10000.0 + 0.5) / 10000.0) != (Math.floor(e.m[i] * 10000.0 + 0.5) / 10000.0))
				return false;
		}
		return true;
	}

	@Override
	public int compareTo(final GFTransformMatrix o) // Lexicographic order
	{
		if (o == this)
			return 0;

		final GFTransformMatrix e = o;

		for (int i = 0; i < 6; i++) {
			if ((Math.floor(m[i] * 10000.0 + 0.5) / 10000.0) < (Math.floor(e.m[i] * 10000.0 + 0.5) / 10000.0))
				return 1;
			else if ((Math.floor(m[i] * 10000.0 + 0.5) / 10000.0) > (Math.floor(e.m[i] * 10000.0 + 0.5) / 10000.0))
				return -1;
		}
		return 0;
	}

	@Override
	public String toString() {
		String result = "";

		result += "[" + (Math.floor(m[0] * 10000 + 0.5) / 10000.0) + "," + (Math.floor(m[1] * 10000.0 + 0.5) / 10000.0)
				+ "," + (Math.floor(m[2] * 10000.0 + 0.5) / 10000.0) + ","
				+ (Math.floor(m[3] * 10000.0 + 0.5) / 10000.0) + "," + (Math.floor(m[4] * 10000.0 + 0.5) / 10000.0)
				+ "," + (Math.floor(m[5] * 10000.0 + 0.5) / 10000.0) + "]\n";

		return result;
	}

} // GFTransformMatrix
