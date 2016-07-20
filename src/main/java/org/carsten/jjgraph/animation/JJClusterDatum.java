/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

/**
 * JJClusterDatum.java
 *
 *
 * Created: Wed May 09 15:34:57 2001
 *
 * @author
 * @version
 */
import org.carsten.jjgraph.util.JJRegressPointPair;

public class JJClusterDatum {
	private int currentCluster = -1;
	private final JJRegressPointPair triangle[];

	public int getCurrentCluster() {
		return currentCluster;
	}

	public void setCurrentCluster(final int v) {
		this.currentCluster = v;
	}

	public JJClusterDatum(final JJRegressPointPair m[]) {
		triangle = new JJRegressPointPair[m.length];

		for (int i = 0; i < m.length; i++) {
			triangle[i] = new JJRegressPointPair(m[i]);
		}
	}

	// private double []getMatrix()
	// {
	// double m[] = new double[6];
	// try {
	// m= JJLinearRegress.regress(new JJArrayRegressCollection(triangle),
	// new JJPoint());
	// }
	// catch(JJLinAlgException e){
	// }

	// return m;
	// }

	public JJRegressPointPair getPointAt(final int k) {
		return triangle[k];
	}

	public int getSize() {
		return triangle.length;
	}

} // JJClusterDatum
