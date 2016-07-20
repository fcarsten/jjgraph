/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

public class Edge {
	public int a;
	public int b;
	public Triangle t1;
	public Triangle t2;
	JJDelaunay del;

	boolean normalized = false;

	@Override
	public int hashCode() {
		return (b * 1000) + a;
	}

	public Triangle opposite(final Triangle t) {
		if (t.equals(t1))
			return t2;
		return t1;
	}

	void _normalize() {
		if (!normalized) {
			normalized = true;

			a -= 3;
			b -= 3;
		}
	}

	@Override
	public boolean equals(final Object o) {
		if ((o == null) || !(o instanceof Edge)) {
			return false;
		}
		final Edge e = (Edge) o;

		return (a == e.a) && (b == e.b);

	}

	public Edge(final int p1, final int p2, final JJDelaunay d) {
		del = d;
		a = Math.min(p1, p2);
		b = Math.max(p1, p2);
		// Debug.println("New Edge: " + this);
	}

	public boolean contains(final int i) {
		return (i == a) || (i == b);
	}

	public boolean isLegal() {
		final int i = a;
		final int j = b;

		if ((i < 3) && (j < 3))
			return true;

		final int k = t1.opposite(this);
		final int l = t2.opposite(this);

		// Debug.println("isLegal with i: "+ i + ", j: "+ j + ", k: "+ k +
		// ", l: "+ l + ".");

		if ((k >= 3) && (l >= 3)) // All positive
		{

			// In circle test

			final JJPoint p = new JJPoint();
			final JJPoint pi = del.points[i];
			final JJPoint pj = del.points[j];
			final JJPoint pk = del.points[k];
			final JJPoint pl = del.points[l];

			final double A = pj.x - pi.x;
			final double B = pj.y - pi.y;
			final double C = pk.x - pi.x;
			final double D = pk.y - pi.y;

			final double E = A * (pi.x + pj.x) + B * (pi.y + pj.y);
			final double F = C * (pi.x + pk.x) + D * (pi.y + pk.y);

			final double G = 2.0 * (A * (pk.y - pj.y) - B * (pk.x - pj.x));

			p.x = (D * E - B * F) / G;
			p.y = (A * F - C * E) / G;

			final double radius = Math.sqrt((pi.x - p.x) * (pi.x - p.x) + (pi.y - p.y) * (pi.y - p.y));

			// Debug.println("Centre: " + p);
			// Debug.println("Radius: " + radius);
			// Debug.println("Distance of l: " + Math.sqrt((pl.x - p.x) * (pl.x
			// - p.x) +
			// (pl.y - p.y) * (pl.y - p.y)));

			if (radius > Math.sqrt((pl.x - p.x) * (pl.x - p.x) + (pl.y - p.y) * (pl.y - p.y)))
				return false;
			else
				return true;
		}

		int negCounter = 0;
		if (i < 3)
			negCounter++;
		if (j < 3)
			negCounter++;
		if (k < 3)
			negCounter++;
		if (l < 3)
			negCounter++;
		if (negCounter == 1) {
			if ((i < 3) || (j < 3))
				return false;
			else
				return true;
		} else if (negCounter == 2) {
			// if(Math.min(i,j) < Math.min(k,l))
			return true;
			// else
			// return false;
		} else {
			Debug.println("Impossible case in isLegal");
		}

		return true;
	}

	@Override
	public String toString() {
		return "" + a + "-" + b;
	}
}
