/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.clustering;
/**
 * JJSA.java
 *
 *
 * Created: Mon May 24 15:44:24 1999
 *
 * @author Carsten Friedrich
 * @version
 */

import java.awt.Color;
import java.util.Iterator;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJGraphicNode;
import org.carsten.jjgraph.graph.JJNode;
import org.carsten.jjgraph.graph.JJNodePair;

public class JJSA extends JJImproveCluster {
	// boolean frozen = false;
	private final int intervalLength = 16;
	private final double coolingFactor = 0.95;
	private final double initTemperatur = 0.06;
	private final double alpha = 0.05;
	private final double minAcceptance = 0.00025;

	// double currentTemperatur = 0.0;
	private int numCuts = -1;
	// private JJGraphWindow window;

	JJNode[] nodeArray;

	boolean frozen(final double temp) {
		if (temp < 0.0025)
			return true;
		return false;
	}

	int getGain(final JJNode knoten, final int newCluster) {
		if (knoten.getCluster() == -1)
			return 0;

		int gain = 0;

		final int tmpCluster = knoten.getCluster();

		for (final Iterator<JJEdge> edgeIter = knoten.inIterator(); edgeIter.hasNext();) {
			final JJNode tmpS = edgeIter.next().getSource();
			if (tmpS.getCluster() == tmpCluster)
				gain--;
			else if (tmpS.getCluster() == newCluster)
				gain++;

		}

		for (final Iterator<JJEdge> edgeIter = knoten.outIterator(); edgeIter.hasNext();) {
			final JJNode tmpT = edgeIter.next().getTarget();

			if (tmpT.getCluster() == tmpCluster)
				gain--;
			else if (tmpT.getCluster() == newCluster)
				gain++;
		}
		return gain;
	}

	JJNodePair swapRandomPair() {
		int index1 = (int) (Math.random() * graph.getNumNodes());
		int index2 = (int) (Math.random() * graph.getNumNodes());

		JJNode tmpN1 = nodeArray[index1];
		JJNode tmpN2 = nodeArray[index2];

		while (tmpN1.getCluster() == -1) {
			index1 = (index1 + 1) % graph.getNumNodes();
			tmpN1 = nodeArray[index1];
		}

		while ((tmpN2.getCluster() == tmpN1.getCluster()) || (tmpN2.getCluster() == -1)) {
			index2 = (index2 + 1) % graph.getNumNodes();
			tmpN2 = nodeArray[index2];
		}

		final JJNodePair tmpP = new JJNodePair(tmpN1, tmpN2);
		tmpP.neighbors = graph.areNeighbors(tmpN1, tmpN2);

		swapPair(tmpP);
		return tmpP;
	}

	void swapPair(final JJNodePair tmpP) {
		numCuts -= (getGain(tmpP.n1, tmpP.n2.getCluster()) + getGain(tmpP.n2, tmpP.n1.getCluster()));

		if (tmpP.neighbors)
			numCuts += 2;

		final int tmpC = tmpP.n1.getCluster();
		tmpP.n1.setCluster(tmpP.n2.getCluster());
		tmpP.n2.setCluster(tmpC);

		if (window != null) {
			final JJGraphicNode g1 = tmpP.n1.getGraphicNode(window);
			final JJGraphicNode g2 = tmpP.n2.getGraphicNode(window);

			final Color tmpCol = g1.getColor();
			g1.setColor(g2.getColor());
			g2.setColor(tmpCol);
		}
	}

	@Override
	public void doClustering() {
		initNodeArray();

		double currentTemperatur = initTemperatur;
		numCuts = graph.getNumCuts();
		double numSwaps = 0;
		double numUndos = 0;

		while (!frozen(currentTemperatur)) {

			for (int i = 0; i < graph.getNumNodes() * intervalLength; i++) {
				final int oldNumCuts = numCuts;
				final JJNodePair tmpP = swapRandomPair();
				numSwaps++;

				if (numCuts > oldNumCuts) {
					// Debug.println("Negative gain");
					// Debug.print("\rBeat " +
					// Math.exp( -(numCuts -
					// oldNumCuts)/(currentTemperatur*20)));

					if (Math.exp(-(numCuts - oldNumCuts) / (currentTemperatur * 10)) < Math.random()) {
						// Debug.println(" Failed random");
						swapPair(tmpP);
						numUndos--;
					} else {
						// Debug.println("\n Successful random");
					}

				} else {
					// Debug.println("Positive gain");
				}

			}
			currentTemperatur *= coolingFactor;
			if (((numSwaps - numUndos) / numSwaps) < minAcceptance)
				break;
		}
	}

	void initNodeArray() {
		nodeArray = new JJNode[graph.getNumNodes()];
		int tmpI = 0;

		for (final Iterator<JJNode> nodeIter = graph.nodeIterator(); nodeIter.hasNext();) {
			final JJNode tmpN1 = nodeIter.next();
			tmpN1.setValue(tmpI);
			nodeArray[tmpI++] = tmpN1;
		}
	}

	@Override
	public String getName() {
		return "Simulated annealing";
	}

	public JJSA(final JJGraph derGraph, final int i, final JJGraphWindow w) {
		super(derGraph, i, w);
	}

} // JJSA
