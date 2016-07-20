/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.clustering;
/**
 * JJCzekanovskiMatrix.java
 *
 *
 * Created: Wed Nov 17 14:54:37 1999
 *
 * @author Carsten Friedrich
 * @version
 */

import java.util.HashSet;
import java.util.Iterator;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJNode;

import Jama.Matrix;

public class JJCzekanovskiMatrix extends Matrix {

	// returns the distance based on the size of the symmetric set difference

	double dist(final JJNode n1, final JJNode n2) {
		final double q = n1.deg() + n2.deg();
		double neighbors = 0;

		double diff = 0;

		final HashSet<JJNode> n1a = new HashSet<>();
		final HashSet<JJNode> n2a = new HashSet<>();
		// Debug.println(n1.getName() + "," + n2.getName());

		for (final Iterator<JJEdge> iter = n1.outIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			if (tmpE.getTarget() != n2)
				n1a.add(tmpE.getTarget());
			else
				neighbors = 1;
		}

		for (final Iterator<JJEdge> iter = n1.inIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			if (tmpE.getSource() != n2)
				n1a.add(tmpE.getSource());
			else
				neighbors = 1;
		}

		for (final Iterator<JJEdge> iter = n2.outIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			if (tmpE.getTarget() != n1)
				n2a.add(tmpE.getTarget());
			else
				neighbors = 1;
		}

		for (final Iterator<JJEdge> iter = n2.inIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			if (tmpE.getSource() != n1)
				n2a.add(tmpE.getSource());
			else
				neighbors = 1;
		}

		// diff = q - 2.0*(n2a.intersection(n1a).size()+ neighbors);

		for (final Object element : n1a) {
			if (!n2a.contains(element)) {
				diff++;
			}
		}

		for (final Object element : n2a) {
			if (!n1a.contains(element)) {
				diff++;
			}
		}

		// Debug.println("q: " +q);
		// Debug.println("diff: " + diff);
		return diff / q;

	}

	public JJCzekanovskiMatrix(final JJGraph g) {
		super(g.getNumNodes(), g.getNumNodes(), 0);

		final int numNodes = g.getNumNodes();

		final double dp[] = new double[numNodes];
		final double d[][] = new double[numNodes][numNodes];
		double dpp = 0;

		final JJNode knoten[] = new JJNode[numNodes];

		g.numberNodes(0);

		int i = 0;

		for (final Iterator<JJNode> iter = g.nodeIterator(); iter.hasNext();) {
			final JJNode tmpN = iter.next();
			knoten[i++] = tmpN;
		}

		for (i = 0; i < numNodes; i++) {
			for (int k = i; k < numNodes; k++) {
				if (i == k) {
					d[i][k] = 0;
				} else {
					final double tmp = dist(knoten[i], knoten[k]);
					// Debug.println("symmetric set difference: " + tmp);

					d[i][k] = tmp;
					d[k][i] = tmp;
				}
			}
		}

		for (i = 0; i < numNodes; i++) {
			for (int k = 0; k < numNodes; k++) {
				dp[i] += d[i][k];
			}
			dp[i] /= numNodes;
			// Debug.println("dp[" + i + "] = " + dp[i]);

			dpp += dp[i];
		}

		dpp /= numNodes;
		// Debug.println("d(.,.) = " +dpp);

		for (i = 0; i < numNodes; i++) {
			for (int k = i; k < numNodes; k++) {
				set(i, k, 0.5 * (dp[i] + dp[k] - dpp));
				set(k, i, 0.5 * (dp[i] + dp[k] - dpp));
			}
		}
	}

} // JJCzekanovskiMatrix
