/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

/**
 * JJMatrix.java
 *
 *
 * Created: Thu Dec 9 13:35:08 1999
 *
 * @author Carsten Friedrich
 * @version
 */

public class JJMatrix {

	static public JJMatrix einheitsMatrix = new JJMatrix(new JJPoint(1, 0, 0, 0), new JJPoint(0, 1, 0, 0),
			new JJPoint(0, 0, 1, 0), new JJPoint(0, 0, 0, 1));

	final static int JJPOINT_SIZE = 4;

	JJPoint row[] = new JJPoint[JJPOINT_SIZE];

	@Override
	public String toString() {
		return "" + row[0] + "\n" + row[1] + "\n" + row[2] + "\n" + row[3] + "\n";
	}

	public JJMatrix() {
		for (int i = 0; i < JJPOINT_SIZE; i++)
			row[i] = null; // new JJPoint(0,0,0,0);
	}

	public JJMatrix(final JJMatrix m) {
		for (int i = 0; i < JJPOINT_SIZE; i++)
			row[i] = new JJPoint(m.row[i]);
	}

	public JJMatrix(final JJPoint a, final JJPoint b, final JJPoint c, final JJPoint d) {
		row[0] = new JJPoint(a);
		row[1] = new JJPoint(b);
		row[2] = new JJPoint(c);
		row[3] = new JJPoint(d);
	}

	public void set(final JJMatrix m) {
		for (int i = 0; i < JJPOINT_SIZE; i++)
			row[i] = new JJPoint(m.row[i]);
	}

	public boolean equals(final JJMatrix m) {
		boolean tmpB = true;
		for (int i = 0; i < JJPOINT_SIZE; i++)
			tmpB = tmpB && (row[i].equals(m.row[i]));

		return tmpB;
	}

	public JJMatrix negate() {
		final JJMatrix tmpM = new JJMatrix();
		for (int i = 0; i < JJPOINT_SIZE; i++)
			tmpM.row[i] = row[i].negate();

		return tmpM;
	}

	static public JJMatrix minus(final JJMatrix m1, final JJMatrix m2) {
		final JJMatrix tmpM = new JJMatrix();
		for (int i = 0; i < JJPOINT_SIZE; i++)
			tmpM.row[i] = JJPoint.minus(m1.row[i], m2.row[i]);
		return tmpM;
	}

	public JJMatrix plus(final JJMatrix m) {
		final JJMatrix tmpM = new JJMatrix();
		for (int i = 0; i < JJPOINT_SIZE; i++)
			tmpM.row[i] = JJPoint.plus(row[i], m.row[i]);

		return tmpM;
	}

	public JJMatrix mult(final double x) {
		final JJMatrix tmpM = new JJMatrix();
		for (int i = 0; i < JJPOINT_SIZE; i++)
			tmpM.row[i] = JJPoint.mult(row[i], x);

		return tmpM;
	}

	public JJMatrix mult(final JJMatrix m) {
		final JJMatrix tmpM = new JJMatrix(einheitsMatrix);

		for (int i = 0; i < JJPOINT_SIZE; i++) {
			for (int k = 0; k < JJPOINT_SIZE; k++) {
				tmpM.row[i].setV(k, 0.0);
				for (int j = 0; j < JJPOINT_SIZE; j++) {
					tmpM.row[i].addV(k, row[i].v(j) * m.row[j].v(k));
				}
			}
		}
		return tmpM;
	}

	public JJPoint mult(final JJPoint p) {
		final JJPoint tmpP = new JJPoint();

		for (int i = 0; i < JJPOINT_SIZE; i++) {
			tmpP.setV(i, 0.0);
			for (int k = 0; k < JJPOINT_SIZE; k++)
				tmpP.addV(i, row[i].v(k) * p.v(k));
		}
		return tmpP;
	}

	static public JJPoint mult(final JJPoint p, final JJMatrix m) {
		final JJPoint tmpP = new JJPoint();
		for (int i = 0; i < JJPOINT_SIZE; i++) {
			tmpP.setV(i, 0.0);
			for (int k = 0; k < JJPOINT_SIZE; k++)
				tmpP.addV(i, m.row[k].v(i) * p.v(k));
		}

		return tmpP;
	}

	public JJMatrix divide(final double x) {
		final JJMatrix tmpM = new JJMatrix();
		for (int i = 0; i < JJPOINT_SIZE; i++)
			tmpM.row[i] = JJPoint.divide(row[i], x);

		return tmpM;
	}

	static public double rad(final double x) {
		return (x * Math.PI / 180.0);
	}

	static public double deg(final double x) {
		return (x * 180.0 / Math.PI);
	}

	static public void rotate(final JJMatrix m, final double w) {
		final JJPoint tmpP = new JJPoint(Math.cos(rad(w)), Math.sin(rad(w)), 0, 0);
		final JJMatrix tmpM = new JJMatrix(tmpP, new JJPoint(-Math.sin(rad(w)), Math.cos(rad(w)), 0, 0),
				new JJPoint(0, 0, 1, 0), new JJPoint());

		m.set(tmpM.mult(m));
		// m = m * tmpM;
	}

	static public void scale(final JJMatrix m, final double s) {
		final JJMatrix tmpM = new JJMatrix(new JJPoint(s, 0, 0, 0), new JJPoint(0, s, 0, 0), new JJPoint(0, 0, 1, 0),
				new JJPoint());
		m.set(tmpM.mult(m));
		// m = m * tmpM;
	}

	static public void scale(final JJMatrix m, final double s, final double s2) {
		final JJMatrix tmpM = new JJMatrix(new JJPoint(s, 0, 0, 0), new JJPoint(0, s2, 0, 0), new JJPoint(0, 0, 1, 0),
				new JJPoint());
		m.set(tmpM.mult(m));
		// m = m * tmpM;
	}

	static public void translate(final JJMatrix m, final double x, final double y) {
		final JJMatrix tmpM = new JJMatrix(new JJPoint(1, 0, 0, 0), new JJPoint(0, 1, 0, 0), new JJPoint(0, 0, 1, 0),
				new JJPoint(x, y, 0));
		m.set(tmpM.mult(m));
		// m = m * tmpM;
	}

	static public void skew(final JJMatrix m, final double w) {
		final JJMatrix tmpM = new JJMatrix(new JJPoint(1, 0, 0, 0),
				new JJPoint(-Math.sin(rad(w)), Math.cos(rad(w)), 0, 0), new JJPoint(0, 0, 1, 0), new JJPoint());

		m.set(tmpM.mult(m));
	}

	static public void italic(final JJMatrix m, final double w) {
		final JJMatrix tmpM = new JJMatrix(new JJPoint(1, 0, 0, 0), new JJPoint(-Math.tan(rad(w)), 1, 0, 0),
				new JJPoint(0, 0, 1, 0), new JJPoint());

		m.set(tmpM.mult(m));
	}

	static public void flipX(final JJMatrix m) {
		final JJMatrix tmpM = new JJMatrix(new JJPoint(1, 0, 0, 0), new JJPoint(0, -1, 0, 0), new JJPoint(0, 0, 1, 0),
				new JJPoint());
		m.set(tmpM.mult(m));
	}

	static public void flipY(final JJMatrix m) {
		final JJMatrix tmpM = new JJMatrix(new JJPoint(-1, 0, 0, 0), new JJPoint(0, 1, 0, 0), new JJPoint(0, 0, 1, 0),
				new JJPoint());

		m.set(tmpM.mult(m));
	}

} // JJMatrix
