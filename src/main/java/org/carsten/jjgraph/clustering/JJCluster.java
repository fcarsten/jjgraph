/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.clustering;

import java.awt.Color;
import java.util.Iterator;

/**
 * JJCluster.java
 *
 *
 * Created: Fri Feb 26 14:56:34 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJNode;

public abstract class JJCluster {
	private int numClusters = -1;
	// private int maxDeg = 0;

	// private int [] clusterSize = null; // Contains the number of nodes of the
	// clusters
	private int[] histogram = null; // Contains a histogram of the degrees of
									// the nodes
	private Color[] colors = null;
	protected JJGraphWindow window;

	abstract public String getName();

	public Color[] getColors() {
		return colors;
	}

	protected void setColors(final Color[] k) {
		colors = k;
	}

	public int[] getHistogram() {
		return histogram;
	}

	protected void setHistogram(final int[] k) {
		histogram = k;
	}

	/**
	 * Get the value of numClusters.
	 *
	 * @return Value of numClusters.
	 */
	public int getNumClusters() {
		return numClusters;
	}

	/**
	 * Set the value of numClusters.
	 *
	 * @param v
	 *            Value to assign to numClusters.
	 */
	public void setNumClusters(final int v) {
		this.numClusters = v;
	}

	// JJNode [] nucleus = null; // contains a representative of the cluster

	JJGraph graph = null;

	// public void printClusterSizes()
	// {
	// for(int i =0; i< numClusters; i++){
	// Debug.println("Cluster " + i + " has " + clusterSize[i] + " nodes.");
	// }
	// }

	//
	// Requires histogram
	//
	//
	// Returns the limit-degree of the nuclei
	//

	// public int computeNumClusters()
	// {
	// int numNodes = graph.getNumNodes();

	// int sqrt = (int)Math.sqrt(numNodes);
	// int limit = 0;

	// {
	// int localCount =0;

	// while( (limit <= maxDeg) &&
	// ( (Math.abs( (numNodes - sqrt) - localCount) >=
	// Math.abs( (numNodes - sqrt) - (localCount + histogram[limit])))))
	// localCount += histogram[limit ++];

	// if(limit > maxDeg)
	// limit = maxDeg;
	// }

	// // We have to jump back over empty node degrees

	// while((histogram[limit] == 0) && (limit>0))
	// limit--;

	// numClusters = 0;
	// for(Iterator nodeIter = graph.nodeIterator();
	// nodeIter.hasNext (); )
	// if( ((JJNode) nodeIter.next ()).deg() >= limit)
	// numClusters ++;

	// numClusters = Math.min(numClusters, 2* sqrt );
	// return limit;

	// }

	//
	// Return max degree
	//
	private int computeMaxDeg() {
		int maxDeg = 0;

		for (final Iterator<JJNode> nodeIter = graph.nodeIterator(); nodeIter.hasNext();) {
			final JJNode tmpN = nodeIter.next();
			if (tmpN.deg() > maxDeg)
				maxDeg = tmpN.deg();
		}
		return maxDeg;
	}

	public void colorGraph() {
		if (colors == null)
			colors = new Color[getNumClusters()];

		for (int i = 0; i < getNumClusters(); i++) {
			colors[i] = new Color((float) (Math.random() / 2.0 + 0.5), (float) (Math.random() / 2.0 + 0.5),
					(float) (Math.random() / 2.0 + 0.5));
		}

		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			final JJNode tmpN = iter.next();
			if (tmpN.getCluster() != -1)
				tmpN.getGraphicNode(window).setColor(colors[tmpN.getCluster()]);
		}
	}

	public void buildHistogram() {
		final int maxDeg = computeMaxDeg();

		if ((histogram == null) || (histogram.length != maxDeg + 1))
			histogram = new int[maxDeg];

		for (int i = 0; i < maxDeg; i++)
			histogram[i] = 0;

		for (final Iterator<JJNode> nodeIter = graph.nodeIterator(); nodeIter.hasNext();) {
			histogram[nodeIter.next().deg()]++;
		}
	}

	//
	// depends on numClusters
	//

	// public void updateClusterSize()
	// {
	// if( (clusterSize == null) || (clusterSize.length != getNumClusters()))
	// clusterSize = new int[getNumClusters()];

	// for(int i=0; i<=getNumClusters(); i++)
	// clusterSize[i]=0;

	// for(Iterator nodeIter = graph.nodeIterator(); nodeIter.hasNext (); )
	// {
	// JJNode tmpN = (JJNode) nodeIter.next ();
	// if(tmpN.getCluster() != -1)
	// clusterSize[tmpN.getCluster()]++;
	// }
	// }

	abstract void doClustering();

	public void cluster() {
		doClustering();
		// updateClusterSize();
		setEdgeWeights();
		// Debug.println("Scaled cost " + scaledCost());
		// Debug.println("Cluster Ratio " + clusterRatio());
	}

	public void setEdgeWeights() {
		for (final Iterator<JJEdge> edgeIter = graph.edgeIterator(); edgeIter.hasNext();) {
			final JJEdge tmpE = edgeIter.next();
			if ((tmpE.getTarget().getCluster() != tmpE.getSource().getCluster())
					&& (tmpE.getTarget().getCluster() != -1) && (tmpE.getSource().getCluster() != -1)) {
				tmpE.setWeight(0.5);
			} else
				tmpE.setWeight(1.0);
		}
	}

	//
	// depends on getNumClusters(), nucleus
	//

	public JJGraph buildStructureGraph(final JJGraph g) {
		final JJGraph strucGraph = g;
		strucGraph.setMultipleEdges(false);
		strucGraph.setName("Cluster structure for " + graph.getName());
		int numCuts = 0;

		final JJNode clusterRep[] = new JJNode[getNumClusters()];
		// Color colors[] = new Color[getNumClusters()];

		for (int i = 0; i < getNumClusters(); i++) {
			clusterRep[i] = strucGraph.addNode();
			// [i].setName("" + clusterSize[i]);
			// Debug.println("Setting color to: " + colors[i]);

			if (colors != null) {
				for (final Object element : strucGraph.getWindows()) {
					final JJGraphWindow w = (JJGraphWindow) element;
					clusterRep[i].getGraphicNode(w).setColor(colors[i]);
				}
				// Debug.println("Setting color to: " + colors[i]);
			}

			clusterRep[i].setCluster(i);
		}

		for (final Iterator<JJEdge> edgeIter = graph.edgeIterator(); edgeIter.hasNext();) {
			final JJEdge tmpE = edgeIter.next();
			if ((tmpE.getTarget().getCluster() != tmpE.getSource().getCluster())
					&& (tmpE.getTarget().getCluster() != -1) && (tmpE.getSource().getCluster() != -1)) {
				strucGraph.addEdge(clusterRep[tmpE.getSource().getCluster()],
						clusterRep[tmpE.getTarget().getCluster()]);
				numCuts++;
			}
		}

		// Debug.println("Clustering has " + numCuts + " cuts.");
		return strucGraph;
	}

	// public JJCluster(JJGraph derGraph) {
	// graph = derGraph;
	// }

	public JJCluster(final JJGraph derGraph, final int i, final JJGraphWindow w) {
		window = w;
		graph = derGraph;
		setNumClusters(i);
	}

	double scaledCost() {
		final double numNodes = graph.getNumNodes();
		final double aktualNumClusters = getNumClusters();
		final double clusterWeight[] = new double[getNumClusters() + 1];
		final double cutWeight[] = new double[getNumClusters() + 1];
		double nowhereEdges = 0;
		double sum = 0;

		// for(int i=0;i<getNumClusters(); i++){
		// if(nucleus[i] != null)
		// aktualNumClusters++;
		// }

		for (final Iterator<JJEdge> edgeIter = graph.edgeIterator(); edgeIter.hasNext();) {
			final JJEdge tmpE = edgeIter.next();

			if ((tmpE.getTarget().getCluster() == -1) || (tmpE.getSource().getCluster() == -1)) {
				nowhereEdges++;
			} else if (tmpE.getTarget().getCluster() == tmpE.getSource().getCluster()) {
				clusterWeight[tmpE.getTarget().getCluster()] += tmpE.getWeight();
			} else {
				cutWeight[tmpE.getTarget().getCluster()] += tmpE.getWeight();
				cutWeight[tmpE.getSource().getCluster()] += tmpE.getWeight();
			}
		}

		for (int i = 0; i < getNumClusters(); i++) {
			// if (nucleus[i] != null)
			sum += Math.max(cutWeight[i], 0.01) / Math.max(clusterWeight[i], 0.01);
		}

		return (1.0 / Math.max((numNodes * (aktualNumClusters - 1)), numNodes)) * sum;
	}

	double clusterRatio() {
		double cutWeight = 0;
		final double clusterWeight[] = new double[getNumClusters() + 1];
		int nowhereEdges = 0;
		double sum = 0;

		for (final Iterator<JJEdge> edgeIter = graph.edgeIterator(); edgeIter.hasNext();) {
			final JJEdge tmpE = edgeIter.next();

			if ((tmpE.getTarget().getCluster() == -1) || (tmpE.getSource().getCluster() == -1)) {
				nowhereEdges++;
			} else if (tmpE.getTarget().getCluster() == tmpE.getSource().getCluster()) {
				clusterWeight[tmpE.getTarget().getCluster()] += tmpE.getWeight();
			} else {
				cutWeight += tmpE.getWeight();
			}
		}

		for (int i = 0; i < getNumClusters(); i++)
			// if(nucleus[i] != null)
			for (int j = i + 1; j < getNumClusters(); j++)
				// if(nucleus[j] != null)
				sum += clusterWeight[i] * clusterWeight[j];

		return Math.max(cutWeight, 0.01) / Math.max(sum, 0.01);
	}

	// int countValidClusters()
	// {
	// int result=0;

	// for(int i =0; i<getNumClusters(); i++)
	// {
	// if(nucleus[i] != null)
	// result++;
	// }
	// return result;
	// }

} // JJCluster
