/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.clustering;
/**
 * JJKL.java
 *
 *
 * Created: Tue May 18 20:15:52 1999
 *
 * @author Carsten Friedrich
 * @version
 */

import java.util.Iterator;
import java.util.Stack;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJNode;
import org.carsten.jjgraph.graph.JJNodePair;

public class JJKL extends JJImproveBipart {
	boolean locked[];
	int gains[];

	public JJKL(final JJBipart b, final JJGraphWindow w) {
		super(b, w);
	}

	public JJKL(final JJGraph g, final JJGraphWindow w) {
		super(g, w);
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

		return maxGain;
	}

	int doSwap(final JJNodePair paar) {
		final JJNode tmpS = paar.n1;
		final JJNode tmpT = paar.n2;
		int tmpGain = gains[tmpS.getValue()] + gains[tmpT.getValue()];

		if (paar.neighbors)
			tmpGain -= 2;

		locked[tmpS.getValue()] = true;
		locked[tmpT.getValue()] = true;

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

	// void updateNeighbors(JJNode knoten, JJNode swapPartner)
	// {
	// for(Iterator edgeIter = knoten.inEdges.listIterator();
	// edgeIter.hasNext();)
	// {
	// JJNode tmpS = ((JJEdge) edgeIter.next()).getSource();
	// updateGain(tmpS);
	// }

	// for(Iterator edgeIter = knoten.outEdges.listIterator();
	// edgeIter.hasNext();)
	// {
	// JJNode tmpT = ((JJEdge) edgeIter.next()).getTarget();
	// updateGain(tmpT);
	// }
	// }

	void updateNeighbors(final JJNode knoten, final JJNode swapPartner) {
		// Debug.println("Updating neighbors of " + knoten.getValue());

		for (final Iterator<JJEdge> edgeIter = knoten.inIterator(); edgeIter.hasNext();) {
			final JJNode tmpS = edgeIter.next().getSource();
			if (tmpS.getCluster() == knoten.getCluster()) {
				// Debug.print("Changing gain of " + tmpS.getValue() +
				// " from " + gains[tmpS.getValue()]);
				gains[tmpS.getValue()] -= 2;
				// Debug.println(" to " + gains[tmpS.getValue()]);
			} else if (tmpS != swapPartner) {
				// Debug.print("Changing gain of " + tmpS.getValue() +
				// " from " + gains[tmpS.getValue()]);
				gains[tmpS.getValue()] += 2;
				// Debug.println(" to " + gains[tmpS.getValue()]);
			}
		}

		for (final Iterator<JJEdge> edgeIter = knoten.outIterator(); edgeIter.hasNext();) {
			final JJNode tmpT = edgeIter.next().getTarget();
			if (tmpT.getCluster() == knoten.getCluster()) {
				// Debug.print("Changing gain of " + tmpT.getValue() +
				// " from " + gains[tmpT.getValue()]);
				gains[tmpT.getValue()] -= 2;
				// Debug.println(" to " + gains[tmpT.getValue()]);
			} else if (tmpT != swapPartner) {
				// Debug.print("Changing gain of " + tmpT.getValue() +
				// " from " + gains[tmpT.getValue()]);
				gains[tmpT.getValue()] += 2;
				// Debug.println(" to " + gains[tmpT.getValue()]);
			}
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
					int tmpGain = gains[tmpN1.getValue()] + gains[tmpN2.getValue()];
					final boolean neighborFlag = areNeighbors(tmpN1, tmpN2);

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

	boolean areNeighbors(final JJNode n1, final JJNode n2) {
		for (final Iterator<JJEdge> edgeIter = n1.inIterator(); edgeIter.hasNext();) {
			final JJNode tmpS = edgeIter.next().getSource();
			if (n2 == tmpS)
				return true;
		}

		for (final Iterator<JJEdge> edgeIter = n1.outIterator(); edgeIter.hasNext();) {
			final JJNode tmpT = edgeIter.next().getTarget();
			if (n2 == tmpT)
				return true;
		}
		return false;
	}

	void updateGains() {
		for (final Iterator<JJNode> nodeIter = graph.nodeIterator(); nodeIter.hasNext();) {
			updateGain(nodeIter.next());
		}
	}

	void updateGain(final JJNode knoten) {
		final int tmpCluster = knoten.getCluster();
		gains[knoten.getValue()] = 0;

		for (final Iterator<JJEdge> edgeIter = knoten.inIterator(); edgeIter.hasNext();) {
			final JJNode tmpS = edgeIter.next().getSource();
			if (tmpS.getCluster() == knoten.getCluster())
				gains[knoten.getValue()]--;
			else
				gains[knoten.getValue()]++;
		}

		for (final Iterator<JJEdge> edgeIter = knoten.outIterator(); edgeIter.hasNext();) {
			final JJNode tmpT = edgeIter.next().getTarget();
			if (tmpT.getCluster() == knoten.getCluster())
				gains[knoten.getValue()]--;
			else
				gains[knoten.getValue()]++;
		}
	}

	void resetLocked() {
		for (int i = 0; i < graph.getNumNodes(); i++) {
			locked[i] = false;
		}
	}

	@Override
	public void doClustering() {
		locked = new boolean[graph.getNumNodes()];
		gains = new int[graph.getNumNodes()];
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

	}

	@Override
	public String getName() {
		return "Kernighan-Lin";
	}

} // JJKL
