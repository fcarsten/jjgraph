/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.francois;

public class Force {
	public double dx = 0;
	public double dy = 0;

	public void addForce(final double fx, final double fy) {
		// dx=dx+(int) Math.floor(fx);
		// dy=dy+(int) Math.floor(fy);
		dx = dx + fx;
		dy = dy + fy;
	}

	@Override
	public String toString() {
		return "(" + dx + "," + dy + ")";
	}
}
