/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.clustering;

/**
 * JJImproveBipart.java
 *
 *
 * Created: Tue May 18 20:02:52 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindow;

abstract public class JJImproveBipart extends JJBipart {
	boolean initialized = false;

	void init() {
		final JJRandBipart tmpC = new JJRandBipart(graph, window);
		tmpC.cluster();

		// numClusters=tmpC.numClusters;
		// maxDeg=tmpC.maxDeg;
		// clusterSize=tmpC.clusterSize;
		// histogram=tmpC.histogram;
		// nucleus=tmpC.nucleus;
		// graph=tmpC.graph;
		initialized = true;
	}

	public JJImproveBipart(final JJGraph derGraph, final JJGraphWindow w) {
		super(derGraph, w);
	}

	@Override
	public void cluster() {
		if (!initialized)
			init();
		super.cluster();
	}

	public JJImproveBipart(final JJBipart tmpC, final JJGraphWindow w) {
		super(tmpC.graph, w);

		tmpC.cluster();

		// numClusters=tmpC.numClusters;
		// maxDeg=tmpC.maxDeg;
		// clusterSize=tmpC.clusterSize;
		// histogram=tmpC.histogram;
		// nucleus=tmpC.nucleus;

		initialized = true;
	}

} // JJImproveBipart
