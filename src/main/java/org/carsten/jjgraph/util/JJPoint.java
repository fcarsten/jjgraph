/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

/**
 * JJPoint.java
 *
 *
 * Created: Mon Apr 12 13:23:06 1999
 *
 * @author $Author: carsten $
 * @version $Revision: 1.2 $ $Date: 2002/07/31 01:40:06 $
 *
 * $Log: JJPoint.java,v $
 * Revision 1.2  2002/07/31 01:40:06  carsten
 * Added methods "boolean isValid()" and "boolean isValid(double x)"
 *
 */
import java.awt.geom.Point2D;

public class JJPoint extends Point2D.Double {
	public double z = 0.0;
	private double t = 1.0;

	final public static double JJ_EPSILON = 0.000000001;
	final public static double JJ_HUGE_VAL = (1.0 / JJ_EPSILON);

	public JJPoint() {
	}

	public boolean isValid() {
		return isValid(x) && isValid(y) && isValid(z);
	}

	public static boolean isValid(final double x) {
		return (!java.lang.Double.isNaN(x)) && (!java.lang.Double.isInfinite(x));

	}

	public JJPoint(final JJPoint p) {
		super(p.x, p.y);
		z = p.z;
		t = p.t;
	}

	public JJPoint(final double x, final double y) {
		super(x, y);
	}

	public double v(final int k) {
		if ((k > 3) || (k < 0)) {
			Debug.println("Array index violation");
			return 0.0;
		}
		if (k == 0)
			return x;
		if (k == 1)
			return y;
		if (k == 2)
			return z;
		return t;
	}

	public void setV(final int k, final double v) {
		if ((k > 3) || (k < 0)) {
			Debug.println("Array index violation");
			return;
		}
		if (k == 0)
			x = v;
		else if (k == 1)
			y = v;
		else if (k == 2)
			z = v;
	}

	public void addV(final int k, final double v) {
		if ((k > 3) || (k < 0)) {
			Debug.println("Array index violation");
			return;
		}
		if (k == 0)
			x += v;
		else if (k == 1)
			y += v;
		else if (k == 2)
			z += v;
	}

	public JJPoint(final double x, final double y, final double zp) {
		super(x, y);
		z = zp;
	}

	public JJPoint(final double x, final double y, final double zp, final double tp) {
		super(x, y);
		t = tp;
		z = zp;
	}

	/**
	 * Get the value of z.
	 *
	 * @return Value of z.
	 */
	public double getZ() {
		return z;
	}

	/**
	 * Set the value of z.
	 *
	 * @param v
	 *            Value to assign to z.
	 */
	public void setZ(final double v) {
		this.z = v;
	}

	public void setX(final double v) {
		this.x = v;
	}

	public void setY(final double v) {
		this.y = v;
	}

	public void setLocation(final JJPoint j) {
		z = j.z;
		x = j.x;
		y = j.y;
	}

	public static JJPoint mult(final JJPoint p, final double k) {
		return new JJPoint(p.x * k, p.y * k, p.z * k, p.t);
	}

	public double mult(final JJPoint p) {
		double v = x * p.getX();
		v += y * p.getY();
		v += z * p.getZ();
		return v;
	}

	public static JJPoint mult(final JJPoint p, final JJMatrix m) {
		final JJPoint tmpP = new JJPoint();
		for (int i = 0; i < JJMatrix.JJPOINT_SIZE; i++) {
			tmpP.setV(i, 0.0);
			for (int k = 0; k < JJMatrix.JJPOINT_SIZE; k++)
				tmpP.addV(i, m.row[k].v(i) * p.v(k));
		}

		return tmpP;
	}

	public static JJPoint divide(final JJPoint p, final double k) {
		return new JJPoint(p.x / k, p.y / k, p.z / k, p.t);
	}

	public static JJPoint div(final JJPoint p, final double k) {
		return divide(p, k);
	}

	public static JJPoint minus(final JJPoint p1, final JJPoint p) {
		return new JJPoint(p1.x - p.x, p1.y - p.y, p1.z - p.z, p1.t);
	}

	public static JJPoint plus(final JJPoint p1, final JJPoint p) {
		return new JJPoint(p1.x + p.x, p1.y + p.y, p1.z + p.z, p1.t);
	}

	public static JJPoint plus(final JJPoint p1, final double x, final double y, final double z) {
		return new JJPoint(p1.x + x, p1.y + y, p1.z + z, p1.t);
	}

	public JJPoint multA(final double k) {
		x *= k;
		y *= k;
		z *= k;
		return this;
		// return new JJPoint(x*k, y*k, z*k, t);
	}

	// public JJPoint multA(JJMatrix m)
	// {
	// JJPoint tmpP = new JJPoint();
	// for(int i=0; i< JJMatrix.JJPOINT_SIZE; i++)
	// {
	// tmpP.setV(i, 0.0);
	// for(int k=0; k< JJMatrix.JJPOINT_SIZE; k++)
	// tmpP.addV(i, m.row[k].v(i) * v(k));
	// }

	// return tmpP;
	// }

	public JJPoint divideA(final double k) {
		x /= k;
		y /= k;
		z /= k;
		return this;
		// return new JJPoint(x/k, y/k, z/k, t);
	}

	public JJPoint divA(final double k) {
		return divideA(k);
	}

	public JJPoint minusA(final JJPoint p) {
		x -= p.x;
		y -= p.y;
		z -= p.z;
		return this;

		// return new JJPoint(x-p.x, y-p.y, z-p.z, t);
	}

	public JJPoint plusA(final JJPoint p) {
		x += p.x;
		y += p.y;
		z += p.z;
		return this;

		// return new JJPoint(x+p.x, y+p.y, z+p.z, t);
	}

	public JJPoint plusA(final double tx, final double ty) {
		return plusA(tx, ty, 0);
	}

	public JJPoint plusA(final double tx, final double ty, final double tz) {
		x += tx;
		y += ty;
		z += tz;
		return this;
	}

	public double abs() {
		if (x * x + y * y + z * z == 0)
			return 0;
		return Math.sqrt(x * x + y * y + z * z);
	}

	public JJPoint negate() {
		return new JJPoint(-x, -y, -z, t);
	}

	public boolean equals(final JJPoint p) {
		return (super.equals(p) && (p.getZ() == z));
	}

	static public double dist(final JJPoint p1, final JJPoint p2) {
		return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y) + (p1.z - p2.z) * (p1.z - p2.z));

		// JJPoint tmpP = minus(p1, p2);

		// return tmpP.abs();
	}

	static public double sqrDist(final JJPoint p1, final JJPoint p2) {
		return ((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y) + (p1.z - p2.z) * (p1.z - p2.z));
	}

	static public double dist(final JJPoint P, final JJPoint A, final JJPoint B) {
		final JJPoint R = minus(B, A);
		final double x = -(R.mult(minus(A, P))) / (R.mult(R));

		if (x < 0.0 || x > 1.0) {
			return JJ_HUGE_VAL;
		}

		return dist(mult(plus(A, R), x), P);
	}

	static public JJPoint rotate(final JJPoint p, final double w) {
		final JJMatrix tmpM = new JJMatrix(new JJPoint(Math.cos(Math.toRadians(w)), Math.sin(Math.toRadians(w)), 0),
				new JJPoint(-Math.sin(Math.toRadians(w)), Math.cos(Math.toRadians(w)), 0), new JJPoint(0, 0, 1),
				new JJPoint());

		return mult(p, tmpM);
	}

	public void rotateA(double a) {
		a = Math.toRadians(a);

		// JJPoint tmpP = rotate(this, a);
		final double x2 = x * Math.cos(a) - y * Math.sin(a); // tmpP.x;
		y = x * Math.sin(a) + y * Math.cos(a); // tmpP.y;
		x = x2;

		// z = tmpP.z;
	}

	static public JJPoint scale(final JJPoint p, final double s) {
		final JJMatrix tmpM = new JJMatrix(new JJPoint(s, 0, 0), new JJPoint(0, s, 0), new JJPoint(0, 0, 1),
				new JJPoint());
		return mult(p, tmpM);
	}

	public void scaleA(final double s) {
		x *= s;
		y *= s;
		z *= s;
	}

	static public JJPoint translate(final JJPoint p, final double x, final double y) {
		final JJMatrix tmpM = new JJMatrix(new JJPoint(1, 0, 0), new JJPoint(0, 1, 0), new JJPoint(0, 0, 1),
				new JJPoint(x, y, 0));
		return mult(p, tmpM);
	}

	static public JJPoint flipAB(final JJPoint p, final JJPoint a, final JJPoint b) {
		// Debug.println("Flip AB");

		JJPoint p2 = minus(p, a);
		final JJPoint b2 = minus(b, a);
		final double alpha = getAngle(b2);
		p2 = rotate(p2, alpha);
		JJPoint p3 = new JJPoint(-p2.getX(), p2.getY());
		p3 = rotate(p3, -alpha);
		return plus(p3, a);
	}

	static public double getAngle(final JJPoint tmpP) {
		if (tmpP.getX() > 0)
			return -Math.toDegrees(Math.acos(-tmpP.getY() / tmpP.abs()));

		return Math.toDegrees(Math.acos(-tmpP.getY() / tmpP.abs()));
	}

	// static public double rad(double x)
	// {
	// return(x*Math.PI/180.0);
	// }

	// static public double deg(double x)
	// {
	// return(x*180.0/Math.PI);
	// }

	@Override
	public String toString() {
		return "(" + x + " " + y + " " + z + " (" + t + "))";

	}

	double angle() {
		return Math.toDegrees(Math.atan2(y, x));

	}

	public JJPoint polarCoordsOf() {
		return new JJPoint(abs(), angle());
	}

	public void copyFrom(final JJPoint p) {
		x = p.x;
		y = p.y;
		z = p.z;

	}

} // JJPoint
