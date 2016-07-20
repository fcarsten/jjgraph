/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.clustering;

/**
 * JJLaplaceMatrix.java
 *
 *
 * Created: Wed Nov 17 14:54:37 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import java.util.Iterator;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJNode;

import Jama.Matrix;

public class JJLaplaceMatrix extends Matrix {

	public JJLaplaceMatrix(final JJGraph g) {
		super(g.getNumNodes(), g.getNumNodes(), 0);

		final int numNodes = g.getNumNodes();

		g.numberNodes(0);

		for (final Iterator<JJNode> iter = g.nodeIterator(); iter.hasNext();) {
			final JJNode tmpN = iter.next();
			final int num = tmpN.getValue();
			set(num, num, tmpN.deg());
		}

		for (final Iterator<JJEdge> iter = g.edgeIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			final int s = tmpE.getSource().getValue();
			final int t = tmpE.getTarget().getValue();
			set(s, t, -1);
			set(t, s, -1);
		}
	}
} // JJLaplaceMatrix
