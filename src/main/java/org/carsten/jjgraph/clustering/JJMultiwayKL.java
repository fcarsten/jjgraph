/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.clustering;

/**
 * JJMultiwayKL.java
 *
 *
 * Created: Tue May 18 20:15:52 1999
 *
 * @author Carsten Friedrich
 * @version
 */

import java.awt.Color;
import java.util.Iterator;
import java.util.Stack;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJGraphicNode;
import org.carsten.jjgraph.graph.JJNode;
import org.carsten.jjgraph.graph.JJNodePair;

public class JJMultiwayKL extends JJImproveCluster {
	boolean locked[];
	int gains[][];
	// JJGraphWindow window = null;

	public JJMultiwayKL(final JJGraph g, final int i, final JJGraphWindow w) {
		super(g, i, w);
		// window =w;
	}

	int doPass() {
		int maxGain = 0;
		final Stack<JJNodePair> undo = new Stack<>();
		int currentGain = 0;

		JJNodePair bestSwap = findBestSwap();

		while (bestSwap.isValid()) {
			final int localGain = doSwap(bestSwap);
			currentGain += localGain;

			if (currentGain > maxGain) {
				undo.clear();
				maxGain = currentGain;
			} else {
				undo.push(bestSwap);
			}

			// Debug.println("Current gain: " + currentGain + " Max gain: " +
			// maxGain
			// + " Undo length: " + undo.size());

			bestSwap = findBestSwap();
		}

		while (!undo.empty()) {
			doSwap(undo.pop());
		}

		// Debug.println("Gain " + maxGain);
		return maxGain;
	}

	int doSwap(final JJNodePair paar) {
		final JJNode tmpS = paar.n1;
		final JJNode tmpT = paar.n2;

		int tmpGain = gains[tmpS.getValue()][tmpT.getCluster()] + gains[tmpT.getValue()][tmpS.getCluster()];

		tmpGain -= gains[tmpS.getValue()][tmpS.getCluster()] + gains[tmpT.getValue()][tmpT.getCluster()];

		// int tmpGain = gains[tmpS.getValue()] + gains[tmpT.getValue()];

		if (paar.neighbors)
			tmpGain -= 2;

		locked[tmpS.getValue()] = true;
		locked[tmpT.getValue()] = true;

		if (window != null) {
			final JJGraphicNode sn = tmpS.getGraphicNode(window);
			final JJGraphicNode tn = tmpT.getGraphicNode(window);
			final Color tmpCol = sn.getColor();
			sn.setColor(tn.getColor());
			tn.setColor(tmpCol);
		}

		final int tmpC = tmpS.getCluster();
		tmpS.setCluster(tmpT.getCluster());
		tmpT.setCluster(tmpC);

		updateNeighbors(tmpS, tmpT);
		updateNeighbors(tmpT, tmpS);

		// gains[tmpS.getValue()] = -gains[tmpS.getValue()];
		// gains[tmpT.getValue()] = -gains[tmpT.getValue()];
		// if(paar.neighbors){
		// gains[tmpS.getValue()] -= 1;
		// gains[tmpT.getValue()] -= 1;
		// }

		updateGain(tmpS);
		updateGain(tmpT);

		// Debug.println("Swap " + tmpS.getValue() + " and " + tmpT.getValue() +
		// " with gain: " + tmpGain);
		return tmpGain;
	}

	void updateNeighbors(final JJNode knoten, final JJNode swapPartner) {
		for (final Iterator<JJEdge> edgeIter = knoten.inIterator(); edgeIter.hasNext();) {
			final JJNode tmpS = edgeIter.next().getSource();
			updateGain(tmpS);
		}

		for (final Iterator<JJEdge> edgeIter = knoten.outIterator(); edgeIter.hasNext();) {
			final JJNode tmpT = edgeIter.next().getTarget();
			updateGain(tmpT);
		}
	}

	// JJEdge findBestSwap()
	// {
	// int maxGain =0;
	// JJEdge maxEdge = null;

	// for(Iterator edgeIter = graph.edgeSet.listIterator();
	// edgeIter.hasNext();)
	// {
	// JJEdge tmpE = (JJEdge) edgeIter.next();
	// JJNode tmpS = tmpE.getSource();
	// JJNode tmpT = tmpE.getTarget();

	// if( (! locked[tmpS.getValue()]) &&
	// (! locked[tmpT.getValue()]) &&
	// (tmpS.getCluster() != tmpT.getCluster()))
	// {
	// int tmpGain = gains[tmpS.getValue()] + gains[tmpT.getValue()];

	// if( (maxEdge == null) ||
	// (tmpGain > maxGain)){
	// maxEdge = tmpE;
	// maxGain = tmpGain;
	// }
	// }
	// }

	// Debug.println("tmpGain " + maxGain);

	// return maxEdge;
	// }

	JJNodePair findBestSwap() {
		int maxGain = 0;
		final JJNodePair tmpPair = new JJNodePair(null, null);

		for (final Iterator<JJNode> nodeIter = graph.nodeIterator(); nodeIter.hasNext();) {
			final JJNode tmpN1 = nodeIter.next();
			for (final Iterator<JJNode> nodeIter2 = graph.nodeIterator(); nodeIter2.hasNext();) {
				final JJNode tmpN2 = nodeIter2.next();

				if ((!locked[tmpN1.getValue()]) && (!locked[tmpN2.getValue()])
						&& (tmpN1.getCluster() != tmpN2.getCluster())) {
					int tmpGain = gains[tmpN1.getValue()][tmpN2.getCluster()]
							+ gains[tmpN2.getValue()][tmpN1.getCluster()];

					tmpGain -= gains[tmpN1.getValue()][tmpN1.getCluster()]
							+ gains[tmpN2.getValue()][tmpN2.getCluster()];

					final boolean neighborFlag = graph.areNeighbors(tmpN1, tmpN2);

					if (neighborFlag) {
						tmpGain -= 2;
					}

					if ((!tmpPair.isValid()) || (tmpGain > maxGain)) {
						tmpPair.n1 = tmpN1;
						tmpPair.n2 = tmpN2;
						tmpPair.neighbors = neighborFlag;
						maxGain = tmpGain;
					}
				}

			}
		}

		// Debug.println("tmpGain " + maxGain);
		return tmpPair;
	}

	void updateGains() {
		for (final Iterator<JJNode> nodeIter = graph.nodeIterator(); nodeIter.hasNext();) {
			updateGain(nodeIter.next());
		}
	}

	void updateGain(final JJNode knoten) {
		if (knoten.getCluster() == -1)
			return;

		final int tmpCluster = knoten.getCluster();
		for (int i = 0; i < getNumClusters(); i++)
			gains[knoten.getValue()][i] = 0;

		for (final Iterator<JJEdge> edgeIter = knoten.inIterator(); edgeIter.hasNext();) {
			final JJNode tmpS = edgeIter.next().getSource();
			if (tmpS.getCluster() != -1)
				gains[knoten.getValue()][tmpS.getCluster()]++;
		}

		for (final Iterator<JJEdge> edgeIter = knoten.outIterator(); edgeIter.hasNext();) {
			final JJNode tmpT = edgeIter.next().getTarget();
			if (tmpT.getCluster() != -1)
				gains[knoten.getValue()][tmpT.getCluster()]++;
		}
	}

	void resetLocked() {
		for (final Iterator<JJNode> nodeIter = graph.nodeIterator(); nodeIter.hasNext();) {
			final JJNode tmpN = nodeIter.next();

			if (tmpN.getCluster() != -1)
				locked[tmpN.getValue()] = false;
			else
				locked[tmpN.getValue()] = true;
		}
	}

	@Override
	public void doClustering() {
		locked = new boolean[graph.getNumNodes()];
		gains = new int[graph.getNumNodes()][getNumClusters()];
		int i = 0;

		for (final Iterator<JJNode> nodeIter = graph.nodeIterator(); nodeIter.hasNext();) {
			final JJNode tmpN = nodeIter.next();
			tmpN.setValue(i++);
		}

		resetLocked();
		updateGains();

		while (doPass() > 0) {
			// Debug.println("Scaled cost " + scaledCost());
			// Debug.println("Cluster Ratio " + clusterRatio());

			resetLocked();
		}

		// updateNuclei();

	}

	// public void updateNuclei()
	// {
	// for(Iterator nodeIter = graph.nodeIterator();
	// nodeIter.hasNext (); ) {
	// JJNode tmpN1 = (JJNode) nodeIter.next ();
	// nucleus[tmpN1.getCluster()] = tmpN1;
	// }

	// }

	@Override
	public String getName() {
		return "Multiway Kernighan-Lin";
	}

} // JJMultiwayKL
