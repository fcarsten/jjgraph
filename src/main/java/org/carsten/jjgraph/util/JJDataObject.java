/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

public class JJDataObject {
	private final double data[];
	private final double color;

	public JJDataObject(final double l[], final double c) {
		data = new double[l.length];
		System.arraycopy(l, 0, data, 0, l.length);

		color = c;
	}

	public int getDimension() {
		return data.length;
	}

	public double getAt(final int i) {
		return data[i];
	}

	public double getColor() {
		return color;
	}
} // JJDataObject
