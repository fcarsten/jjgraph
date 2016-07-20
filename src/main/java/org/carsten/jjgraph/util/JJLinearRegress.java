/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

/**
 * JJLinearRegress.java
 *
 *
 * Created: Mon Nov 06 17:16:32 2000
 *
 * @author
 * @version
 */

public class JJLinearRegress {

	public static void computeR2(final JJRegressableCollection r, final double[] m, final JJPoint centerOld) {
		double ssReg = 0;
		double ssTot = 0;
		double avgX = 0;
		double avgY = 0;
		// centerOld = new JJPoint();

		for (int i = 0; i < r.size(); i++) {
			// JJPoint x = new JJPoint(r.getPos1(i));
			final JJPoint y = new JJPoint(r.getPos2(i));
			y.minusA(centerOld);

			avgX += y.x;
			avgY += y.y;
		}
		final JJPoint avg = new JJPoint(avgX /= r.size(), avgY /= r.size());

		for (int i = 0; i < r.size(); i++) {
			final JJPoint x = new JJPoint(r.getPos1(i));
			final JJPoint y = new JJPoint(r.getPos2(i));
			x.minusA(centerOld);
			y.minusA(centerOld);

			final JJPoint p = GFMatrix.transform(x, m);
			ssTot += JJPoint.sqrDist(avg, y);
			ssReg += JJPoint.sqrDist(p, y);
		}
		if (ssTot != 0) {
			// Debug.println("R-squared: " + (1.0- ssReg/ssTot));
		} else {
			// Debug.println("R-squared undefined (ssTot = 0), ssReg: " +
			// ssReg);
		}

	}

	public static double[] regress(final JJRegressableCollection r, final JJPoint centerOld) throws JJLinAlgException {
		final double m[] = new double[6];
		// centerOld = new JJPoint();

		m[0] = m[3] = 1;
		m[2] = m[4] = m[1] = m[5] = 0;

		if (r == null)
			return m;

		double sx1 = 0;
		double sx1q = 0;
		double sx2 = 0;
		double sx2q = 0;
		double sx1x2 = 0;

		double sy1 = 0;
		double sx2y1 = 0;
		double sx1y1 = 0;
		double sy2 = 0;
		double sx2y2 = 0;
		double sx1y2 = 0;
		double sy1q = 0;
		double sy2q = 0;

		for (int i = 0; i < r.size(); i++) {
			final JJPoint x = new JJPoint(r.getPos1(i));
			x.minusA(centerOld);
			sx1 += x.x;
			sx2 += x.y;
			sx1q += x.x * x.x;
			sx2q += x.y * x.y;
			sx1x2 += x.x * x.y;

			final JJPoint y = new JJPoint(r.getPos2(i));
			y.minusA(centerOld);
			sy1 += y.x;
			sy2 += y.y;
			sy1q += y.x * y.x;
			sy2q += y.y * y.y;
			sx2y1 += x.y * y.x;
			sx2y2 += x.y * y.y;
			sx1y1 += x.x * y.x;
			sx1y2 += x.x * y.y;
		}

		final double n = r.size();

		// Debug.println("Computing correlation: ");

		// double cor = (sx1y1 + sx2y2) - (1/n)* ( sx1* sy1 + sx2*sy2);
		// cor /= Math.sqrt( ((sx1q + sx2q) - (1/n)* ((sx1*sx1) + (sx2*sx2))) *
		// ((sy1q * sy2q) - (1/n)* ((sy1*sy1) + (sy2*sy2))));

		// Debug.println(" " + cor);

		if ((n * sx1x2 * sx1x2 - 2.0 * sx1 * sx2 * sx1x2 + sx1 * sx1 * sx2q + sx2 * sx2 * sx1q
				- n * sx1q * sx2q) == 0) {
			m[0] = m[3] = 1;
			m[2] = m[4] = m[1] = m[5] = 0;
			throw (new JJLinAlgException("No linear transformation computable!"));
		}

		m[0] = (n * sx1x2 * sx2y1 + sx2 * sx2 * sx1y1 - n * sx1y1 * sx2q - sx1 * sx2 * sx2y1 - sx2 * sy1 * sx1x2
				+ sx1 * sy1 * sx2q)
				/ (n * sx1x2 * sx1x2 - 2.0 * sx1 * sx2 * sx1x2 + sx1 * sx1 * sx2q + sx2 * sx2 * sx1q - n * sx1q * sx2q);

		m[2] = (n * sx1x2 * sx1y1 + sx1 * sx1 * sx2y1 - n * sx2y1 * sx1q - sx1 * sx2 * sx1y1 - sx1 * sy1 * sx1x2
				+ sx2 * sy1 * sx1q)
				/ (n * sx1x2 * sx1x2 - 2.0 * sx1 * sx2 * sx1x2 + sx1 * sx1 * sx2q + sx2 * sx2 * sx1q - n * sx1q * sx2q);

		m[4] = (sy1 * sx1x2 * sx1x2 - sx1 * sx1x2 * sx2y1 - sx2 * sx1x2 * sx1y1 + sx1 * sx1y1 * sx2q
				+ sx2 * sx2y1 * sx1q - sy1 * sx1q * sx2q)
				/ (n * sx1x2 * sx1x2 - 2.0 * sx1 * sx2 * sx1x2 + sx1 * sx1 * sx2q + sx2 * sx2 * sx1q - n * sx1q * sx2q);

		m[1] = (n * sx1x2 * sx2y2 + sx2 * sx2 * sx1y2 - n * sx1y2 * sx2q - sx1 * sx2 * sx2y2 - sx2 * sy2 * sx1x2
				+ sx1 * sy2 * sx2q)
				/ (n * sx1x2 * sx1x2 - 2.0 * sx1 * sx2 * sx1x2 + sx1 * sx1 * sx2q + sx2 * sx2 * sx1q - n * sx1q * sx2q);

		m[3] = (n * sx1x2 * sx1y2 + sx1 * sx1 * sx2y2 - n * sx2y2 * sx1q - sx1 * sx2 * sx1y2 - sx1 * sy2 * sx1x2
				+ sx2 * sy2 * sx1q)
				/ (n * sx1x2 * sx1x2 - 2.0 * sx1 * sx2 * sx1x2 + sx1 * sx1 * sx2q + sx2 * sx2 * sx1q - n * sx1q * sx2q);

		m[5] = (sy2 * sx1x2 * sx1x2 - sx1 * sx1x2 * sx2y2 - sx2 * sx1x2 * sx1y2 + sx1 * sx1y2 * sx2q
				+ sx2 * sx2y2 * sx1q - sy2 * sx1q * sx2q)
				/ (n * sx1x2 * sx1x2 - 2.0 * sx1 * sx2 * sx1x2 + sx1 * sx1 * sx2q + sx2 * sx2 * sx1q - n * sx1q * sx2q);

		return m;
	}

} // JJLinearRegress
