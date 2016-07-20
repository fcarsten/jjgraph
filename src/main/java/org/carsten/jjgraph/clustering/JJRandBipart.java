/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.clustering;
/**
 * JJRandBipart.java
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

public class JJRandBipart extends JJBipart {

	public JJRandBipart(final JJGraph g, final JJGraphWindow w) {
		super(g, w);
	}

	@Override
	void doClustering() {
		final int numNodes = graph.getNumNodes();

		int counter = 0;
		final int tmpClSize = (int) Math.ceil((numNodes) / 2.0);

		for (final Iterator<JJNode> nodeIter = graph.nodeIterator(); nodeIter.hasNext();) {
			final JJNode tmpN = nodeIter.next();

			if (counter < tmpClSize)
				tmpN.setCluster(0);
			else
				tmpN.setCluster(1);
			counter++;

		}
	}

	@Override
	public String getName() {
		return "Random Bipartitioning";
	}
} // JJRandBipart
