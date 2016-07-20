/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

public class Triangle {
	Edge edges[] = new Edge[3];
	public int p1, p2, p3;
	JJDelaunay del;

	public int opposite(final Edge e) {
		if ((e.a != p1) && (e.b != p1))
			return p1;

		if ((e.a != p2) && (e.b != p2))
			return p2;

		return p3;
	}

	public boolean isValid() {
		if ((p1 >= 0) && (p2 >= 0) && (p3 >= 0))
			return true;
		return false;
	}

	void _normalize() {
		p1 -= 3;
		p2 -= 3;
		p3 -= 3;

		for (int i = 0; i < 3; i++)
			edges[i]._normalize();
	}

	double area2(final JJPoint A, final JJPoint B, final JJPoint C) {
		return (A.x - C.x) * (B.y - C.y) - (A.y - C.y) * (B.x - C.x);
	}

	public boolean insideTriangle(final JJPoint P) // ABC is assumed to be
													// counter-clockwise
	{
		final double a1 = area2(del.points[p1], del.points[p2], P);
		final double a2 = area2(del.points[p2], del.points[p3], P);
		final double a3 = area2(del.points[p3], del.points[p1], P);

		return ((a1 >= 0) && (a2 >= 0) && (a3 >= 0)) || ((a1 <= 0) && (a2 <= 0) && (a3 <= 0));
	}

	public int next(final int i) {
		if (i == p1)
			return p2;
		if (i == p2)
			return p3;
		return p1;
	}

	public Edge findEdge(final int a, final int b) {
		for (int i = 0; i < 3; i++)
			if (edges[i].contains(a) && edges[i].contains(b))
				return edges[i];
		throw new RuntimeException("Couldn't find edge " + a + " - " + b + " in triangle " + this + ".");
	}

	@Override
	public String toString() {
		return "" + p1 + "-" + p2 + "-" + p3;
	}

	public void fixOrder() {
		// Fix orientation
		final double x1 = del.points[p1].x;
		final double y1 = del.points[p1].y;
		final double x2 = del.points[p2].x;
		final double y2 = del.points[p2].y;
		final double x3 = del.points[p3].x;
		final double y3 = del.points[p3].y;

		final double det = x1 * y2 - x2 * y1 + x3 * y1 - x1 * y3 + x2 * y3 - x3 * y2;
		if (det > 0) {
			final int tmp = p2;
			p2 = p3;
			p3 = tmp;
			// Debug.println("Fixing orientation to " + p1 +"-"+ p2 +"-" +p3);
		}
	}

	// public Triangle(Triangle t)
	// {
	// edges = null;
	// del = null;
	// p1= -1;
	// p2= -1;
	// p3= -1;
	// }

	public Triangle() {
		edges = null;
		del = null;
		p1 = -1;
		p2 = -1;
		p3 = -1;
	}

	public Triangle(final Edge ea, final Edge eb, final Edge ec, final int a, final int b, final int c,
			final Triangle oldFace, final JJDelaunay d) {
		del = d;

		edges[0] = ea;
		edges[1] = eb;
		edges[2] = ec;
		p1 = a;
		p2 = b;
		p3 = c;

		fixOrder();

		for (int i = 0; i < 3; i++) {
			if (edges[i].t1 == oldFace)
				edges[i].t1 = this;
			else if (edges[i].t2 == oldFace)
				edges[i].t2 = this;
			else if (edges[i].t1 == null)
				edges[i].t1 = this;
			else if (edges[i].t2 == null)
				edges[i].t2 = this;
			else
				Debug.println("Error assigning triangles to edge");

		}
	}

	public Triangle(final Edge ea, final Edge eb, final Edge ec, final int a, final int b, final int c,
			final Triangle oldFace1, final Triangle oldFace2, final JJDelaunay d) {
		del = d;

		edges[0] = ea;
		edges[1] = eb;
		edges[2] = ec;
		p1 = a;
		p2 = b;
		p3 = c;

		fixOrder();

		for (int i = 0; i < 3; i++) {
			if ((edges[i].t1 == oldFace1) || (edges[i].t1 == oldFace2))
				edges[i].t1 = this;
			else if ((edges[i].t2 == oldFace1) || (edges[i].t2 == oldFace2))
				edges[i].t2 = this;
			else if (edges[i].t1 == null)
				edges[i].t1 = this;
			else if (edges[i].t2 == null)
				edges[i].t2 = this;
			else
				Debug.println("Error assigning triangles to edge");

		}
	}

	// public boolean contains(int p)
	// {
	// for(int i=0; i<3; i++)
	// if(edges[i].contains(p))
	// return true;
	// return false;
	// }

	// !!!
	// Changes semantic !!!!
	// Maybe not save. If Delaunay fails change back to above
	// !!!

	public boolean contains(final int p) {
		return ((p == p1) || (p == p2) || (p == p3));
	}

	JJTNode node;

	@Override
	public boolean equals(final Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Triangle))
			return false;

		final Triangle t = (Triangle) o;

		return ((t.contains(p1)) && (t.contains(p2)) && (t.contains(p3)));

	}

	@Override
	public int hashCode() {
		return p1 * p2 * p3;
	}

}
