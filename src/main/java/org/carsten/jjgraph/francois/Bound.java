/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.francois;

/**
 * Bound.java
 *
 *
 * Created: Fri Apr 14 16:51:05 2000
 *
 * @author Carsten Friedrich
 * @version
 */
import org.carsten.jjgraph.graph.JJNode;
import org.carsten.jjgraph.util.JJPoint;

/** restricted mouvement for nodes. */
class Bound {
	static int maxMove = 10; // 000; // instead of 10

	static int nbCadrant = 8; // instead of =4

	static double pi4 = 0.78539816;

	static int numCadrant(double dx, double dy) {
		if (dy >= 0) {
			if (dx == 0)
				return 1;
			else if (dx > 0) {
				final double a = Math.atan(dy / dx);
				if (a < pi4)
					return 0;
				return 1;
			} else {
				dx = -dx;
				final double a = Math.atan(dy / dx);
				if (a < pi4)
					return 3;
				return 2;
			}
		} else {
			dy = -dy;
			if (dx == 0)
				return 5;
			else if (dx > 0) {
				final double a = Math.atan(dy / dx);
				if (a < pi4)
					return 7;
				return 6;
			} else {
				dx = -dx;
				final double a = Math.atan(dy / dx);
				if (a < pi4)
					return 4;
				return 5;
			}
		}

	}

	void addBound(final double dx, final double dy) {
		final double norme = Math.sqrt(dx * dx + dy * dy);

		final int i = numCadrant(dx, dy);

		final int im2 = (i + 8 - 2) % 8;
		final int im1 = (i + 8 - 1) % 8;
		final int ip1 = (i + 1) % 8;
		final int ip2 = (i + 2) % 8;

		r[i] = Math.min(r[i], norme);
		r[ip1] = Math.min(r[ip1], norme);
		r[im1] = Math.min(r[im1], norme);
		r[ip2] = Math.min(r[ip2], norme);
		r[im2] = Math.min(r[im2], norme);

	}

	void addNonEdgeBound(final double dist) {
		r[0] = Math.min(r[0], dist);
		r[1] = Math.min(r[1], dist);
		r[2] = Math.min(r[2], dist);
		r[3] = Math.min(r[3], dist);
		r[4] = Math.min(r[4], dist);
		r[5] = Math.min(r[5], dist);
		r[6] = Math.min(r[6], dist);
		r[7] = Math.min(r[7], dist);
	}

	Force _f = null;
	JJPoint _c = null;
	JJFrancoisNode _fn = null;

	double r[] = new double[nbCadrant];

	Bound(final JJNode n, final JJFrancoisNode fn) {
		_fn = fn;
		init(n);
	}

	void init(final JJNode n) {
		_f = _fn.force;
		_c = _fn.pos;

		for (int i = 0; i < nbCadrant; i++)
			r[i] = maxMove;
	}

	/** reduce force of node such that it remains into the bound */
	void apply() {
		// JJPoint c=new JJPoint(_c.x+_f.dx, _c.y+_f.dy);
		final int i = numCadrant(_c.x + _f.dx, _c.y + _f.dy);

		final double norme = Math.sqrt(_f.dx * _f.dx + _f.dy * _f.dy);
		if (norme > r[i]) {
			final double alpha = r[i] / norme;
			// _f.dx=(int) Math.floor(alpha*_f.dx);
			// _f.dy=(int) Math.floor(alpha*_f.dy);
			_f.dx = alpha * _f.dx;
			_f.dy = alpha * _f.dy;
		}
	}
}
