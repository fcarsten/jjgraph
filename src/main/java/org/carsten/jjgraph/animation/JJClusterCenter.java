/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

import java.util.List;

/**
 * JJClusterCenter.java
 *
 *
 * Created: Wed May 09 15:34:57 2001
 *
 * @author
 * @version
 */
import org.carsten.jjgraph.util.GFDecomp;
import org.carsten.jjgraph.util.GFTransformMatrix;
import org.carsten.jjgraph.util.JJArrayRegressCollection;
import org.carsten.jjgraph.util.JJLinAlgException;
import org.carsten.jjgraph.util.JJLinearRegress;
import org.carsten.jjgraph.util.JJPoint;
import org.carsten.jjgraph.util.JJRegressPointPair;

public class JJClusterCenter {
	private GFTransformMatrix matrix;
	private GFDecomp decomp = new GFDecomp();

	@Override
	public String toString() {
		return " " + matrix;// + ",\n" + decomp;
	}

	public JJClusterCenter(final GFTransformMatrix m) {
		matrix = m;
		decomp = new GFDecomp(m.m);
	}

	public JJClusterCenter() {
		matrix = new GFTransformMatrix(GFTransformMatrix.unitMatrix);
	}

	public JJPoint transform(final JJPoint p) {
		return matrix.transform(p);
	}

	public double getMatrixEntryAt(final int k) {
		return matrix.m[k];
	}

	public double getAngle() {
		return decomp.angle1;
	}

	// public void plusA(JJClusterCenter m)
	// {
	// matrix.plusA(m.matrix);
	// }

	// public void divA(double m)
	// {
	// matrix.divA(m);
	// decomp = new GFDecomp(matrix.m);
	// }

	public double dist(final JJClusterDatum m) {
		double dist = 0;
		for (int i = 0; i < m.getSize(); i++) {
			final JJRegressPointPair p = m.getPointAt(i);

			dist += JJPoint.sqrDist(p.p2, matrix.transform(p.p1));
		}

		return dist / m.getSize();
	}

	public void recompute(final List<JJClusterDatum> l) throws JJLinAlgException {
		int i = 0;
		int num = 0;

		for (final JJClusterDatum jjClusterDatum : l) {
			final JJClusterDatum d = jjClusterDatum;
			num += d.getSize();
		}
		final JJRegressPointPair array[] = new JJRegressPointPair[num];

		for (final JJClusterDatum jjClusterDatum : l) {
			final JJClusterDatum d = jjClusterDatum;
			for (int k = 0; k < d.getSize(); k++) {
				array[i++] = d.getPointAt(k);
			}
		}
		double m[] = null;
		m = JJLinearRegress.regress(new JJArrayRegressCollection(array), new JJPoint());
		matrix = new GFTransformMatrix(m);
		decomp = new GFDecomp(m);
	}

} // JJClusterCenter
