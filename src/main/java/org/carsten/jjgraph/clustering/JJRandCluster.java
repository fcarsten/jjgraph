/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.clustering;

/**
 * JJRandCluster.java
 *
 *
 * Created: Fri Feb 26 14:56:34 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import java.util.Iterator;

import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJNode;

public class JJRandCluster extends JJCluster {

	@Override
	void doClustering() {
		if (getNumClusters() < 2)
			return;

		final int numNodes = graph.getNumNodes();

		int counter = 0;
		final int tmpClSize = (int) Math.ceil(((double) numNodes) / ((double) getNumClusters()));

		for (final Iterator<JJNode> nodeIter = graph.nodeIterator(); nodeIter.hasNext();) {
			final JJNode tmpN = nodeIter.next();
			final int aktuSlot = counter % getNumClusters();
			tmpN.setCluster(aktuSlot);
			counter++;
		}

		// updateClusterSize();
	}

	public JJRandCluster(final JJGraph derGraph, final int anzCluster, final JJGraphWindow w) {
		super(derGraph, anzCluster, w);
	}

	@Override
	public String getName() {
		return "Random Clustering";
	}
} // JJRandCluster
