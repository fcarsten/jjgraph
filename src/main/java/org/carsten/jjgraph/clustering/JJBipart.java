/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.clustering;

/**
 * JJBipart.java
 *
 *
 * Created: Tue May 18 18:48:36 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindow;

abstract public class JJBipart extends JJCluster {

	public JJBipart(final JJGraph g, final JJGraphWindow w) {
		super(g, 2, w);
		// setNumClusters(2);
	}

	@Override
	public int getNumClusters() {
		return 2;
	}

	@Override
	public void setNumClusters(final int i) {
	}

	// public int computeNumClusters()
	// {
	// return maxDeg;
	// }

} // JJBipart
