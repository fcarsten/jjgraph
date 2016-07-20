/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.clustering;
/**
 * JJImproveluster.java
 *
 *
 * Created: Fri Feb 26 14:56:34 1999
 *
 * @author Carsten Friedrich
 * @version
 */

import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindow;

abstract public class JJImproveCluster extends JJCluster {
	boolean initialized = false;

	private void init() {
		final JJRandCluster tmpC = new JJRandCluster(graph, getNumClusters(), window);
		tmpC.cluster();

		setHistogram(tmpC.getHistogram());
		setColors(tmpC.getColors());

		initialized = true;
	}

	public JJImproveCluster(final JJGraph derGraph, final int i, final JJGraphWindow w) {
		super(derGraph, i, w);
	}

	@Override
	public void cluster() {
		if (!initialized)
			init();

		super.cluster();
	}

	public void cluster(final JJCluster tmpC) {
		graph = tmpC.graph;

		setNumClusters(tmpC.getNumClusters());
		setHistogram(tmpC.getHistogram());
		setColors(tmpC.getColors());

		initialized = true;

		super.cluster();
	}

} // JJImproveCluster
