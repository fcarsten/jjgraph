/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.clustering;
/**
 * JJEigenBipart.java
 *
 *
 * Created: Tue May 18 18:54:13 1999
 *
 * @author Carsten Friedrich
 * @version
 */

import java.util.Iterator;

import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJNode;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class JJEigenBipart extends JJBipart {
	private Matrix em;
	// private int numValues;

	// public int getNumValues()
	// {
	// return numValues;
	// }

	public Matrix getMatrix() {
		return em;
	}

	public JJEigenBipart(final JJGraph g, final int mm, final JJGraphWindow w) {
		super(g, w);
		matrixMode = mm;
	}

	public JJEigenBipart(final JJGraph g, final JJGraphWindow w) {
		super(g, w);
	}

	final public static int LAPLACE = 0;
	final public static int ADJACENCE = 1;

	private int matrixMode = LAPLACE;

	/**
	 * Get the value of matrixMode.
	 *
	 * @return Value of matrixMode.
	 */
	public int getMatrixMode() {
		return matrixMode;
	}

	/**
	 * Set the value of matrixMode.
	 *
	 * @param v
	 *            Value to assign to matrixMode.
	 */
	public void setMatrixMode(final int v) {
		this.matrixMode = v;
	}

	@Override
	public void doClustering() {
		Matrix lp;
		if (matrixMode == LAPLACE)
			lp = new JJLaplaceMatrix(graph);
		else
			lp = new JJAdjMatrix(graph);

		// Matrix lp = new JJCzekanovskiMatrix(graph);
		final EigenvalueDecomposition ev = new EigenvalueDecomposition(lp);
		// `lp.print(6,3);

		final double[] rev = ev.getRealEigenvalues();
		// for(int k=0;k<rev.length; k++){
		// Debug.println("Eigenvalue: " + rev[k]);
		// }

		em = ev.getV();
		// em.print(6,3);

		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			final JJNode tmpN = iter.next();
			final int num = tmpN.getValue();

			if (em.get(num, 1) < 0) {
				tmpN.setCluster(0);
			} else {
				tmpN.setCluster(1);
			}
		}

		// numValues = ev.getRealEigenvalues().length;

		// new JJEigenWindow(this, ev.getRealEigenvalues().length-1);
	}

	@Override
	public String getName() {
		return "Sprectral bipartitioning";
	}

} // JJEigenBipart
