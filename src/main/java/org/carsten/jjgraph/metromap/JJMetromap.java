/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
/*
 * Created on 14/08/2003
 */
package org.carsten.jjgraph.metromap;

import java.awt.FileDialog;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJGraphWindowImpl;
import org.carsten.jjgraph.graph.JJGraphicNode;
import org.carsten.jjgraph.graph.JJNode;
import org.carsten.jjgraph.layout.JJLayout;
import org.carsten.jjgraph.util.JJPoint;

/**
 * @author Damian Merrick
 */
public class JJMetromap implements JJLayout {

	private final JJGraphWindow graphWindow;
	private final JJGraph graph;

	public JJMetromap(final JJGraphWindow graphWindow) {
		this.graphWindow = graphWindow;
		this.graph = graphWindow.getGraph();
	}

	/**
	 * @see org.carsten.jjgraph.layout.JJLayout#apply(java.lang.String)
	 */
	@Override
	public void apply(final String s) {
		layout();
	}

	/**
	 * @see org.carsten.jjgraph.layout.JJLayout#canDo(java.lang.String)
	 */
	@Override
	public boolean canDo(final String s) {
		return s.equals("Metromap");
	}

	/**
	 * @see org.carsten.jjgraph.layout.JJLayout#layout(java.util.Collection)
	 */
	@Override
	public void layout(final Collection<JJNode> c) {
		layout();
	}

	/**
	 * @see org.carsten.jjgraph.layout.JJLayout#getName()
	 */
	@Override
	public String getName() {
		return "Metromap";
	}

	/**
	 * @see org.carsten.jjgraph.layout.JJLayout#allowsOptimize()
	 */
	@Override
	public int allowsOptimize() {
		return 0;
	}

	/**
	 * @see org.carsten.jjgraph.layout.JJLayout#layout()
	 */
	@Override
	public void layout() {
		final MetroMapGraph mmGraph = new MetroMapGraph();

		FileDialog fileDialog = new FileDialog(graphWindow.getFrame(), "Load metromap file", FileDialog.LOAD);
		fileDialog.setFilenameFilter(new FilenameFilter() {
			@Override
			public boolean accept(final File dir, final String name) {
				return name.toLowerCase().endsWith(".txt");
			}
		});
		fileDialog.setVisible(true);

		if (fileDialog.getFile() != null) {
			final String filename = fileDialog.getDirectory() + fileDialog.getFile();
			final String metFilename = filename.substring(0, filename.lastIndexOf(".")) + ".MET";

			// Read text file containing metromap description
			if (!mmGraph.readTextFile(filename)) {
				return;
			}

			boolean presetGeometry = false;
			String presetFilename = "";
			if (javax.swing.JOptionPane.showConfirmDialog(null, "Do you wish to use a predefined initial layout?",
					"Metromap", javax.swing.JOptionPane.YES_NO_OPTION,
					javax.swing.JOptionPane.QUESTION_MESSAGE) == javax.swing.JOptionPane.YES_OPTION) {
				fileDialog = new FileDialog(graphWindow.getFrame(), "Load initial layout file", FileDialog.LOAD);
				fileDialog.setFilenameFilter(new FilenameFilter() {
					@Override
					public boolean accept(final File dir, final String name) {
						return name.toLowerCase().endsWith(".txt");
					}
				});
				fileDialog.setVisible(true);

				if (fileDialog.getFile() != null) {
					presetGeometry = true;
					presetFilename = fileDialog.getDirectory() + fileDialog.getFile();
				}
			}

			// Record start time
			final long startTime = System.currentTimeMillis();

			// Remove link nodes
			mmGraph.removeDegree2Nodes();

			if (presetGeometry) {
				mmGraph.presetGeometry(presetFilename);
			}

			// Convert graph to jjGraph format
			convertMMtoJJ(mmGraph);

			if (presetGeometry) {
				// Copy positions to jjGraph
				copyMMPositions(mmGraph);
			}
			/*
			 * if(!presetGeometry) { // Layout graph (without edge weights)
			 * JJGem gem = new JJGem(graphWindow); gem.setFinalTemp(1.0 /
			 * 4000.0); // gem.setFinalTemp(1.0 / 1000.0); gem.setMaxIter(1000);
			 * gem.setUseEdgeWeight(false); gem.layout(); }
			 *
			 * // Layout graph (with edge weights) FRCC frcc = new
			 * FRCC(graphWindow); frcc.layout();
			 *
			 * // Copy positions to mmGraph copyJJPositions(mmGraph);
			 *
			 * // Reinstitute link nodes mmGraph.reinstituteDegree2Nodes();
			 */
			// Convert graph back to jjGraph format
			convertMMtoJJ(mmGraph);

			// Repaint graph display
			graphWindow.repaint();

			// Write .MET file
			mmGraph.writeMETFile(metFilename);

			// Copy positions to jjGraph
			copyMMPositions(mmGraph);

			// Graph is undirected
			graph.setDirected(false);

			// Report time taken
			final long endTime = System.currentTimeMillis();
			final long msTaken = endTime - startTime;
			System.out.println("Time taken: " + msTaken + " ms");

			// Fit graph to display
			if (graphWindow instanceof JJGraphWindowImpl) {
				((JJGraphWindowImpl) graphWindow).centerGraph();
				((JJGraphWindowImpl) graphWindow).zoomToFit();
			}
		}
	}

	private void convertMMtoJJ(final MetroMapGraph mmGraph) {
		graph.clear();

		final Vector<Edge> mmEdges = mmGraph.getEdges();
		for (int i = 0; i < mmEdges.size(); i++) {
			final Edge edge = mmEdges.elementAt(i);

			if (!edge.isInUse()) {
				continue;
			}

			final Node a = edge.getA();
			final Node b = edge.getB();

			JJNode jjA = graph.findNodeWithName(a.getLabel());
			JJNode jjB = graph.findNodeWithName(b.getLabel());

			if (jjA == null) {
				jjA = graph.addNode();
				jjA.setName(a.getLabel());
			}
			if (jjB == null) {
				jjB = graph.addNode();
				jjB.setName(b.getLabel());
			}

			final JJEdge jjEdge = graph.addEdge(jjA, jjB);
			jjEdge.setWeight(edge.getMinimumLength());
		}
	}

	private void removeEdgeWeights() {
		final Collection<JJEdge> edges = graph.getEdges();
		final Iterator<JJEdge> it = edges.iterator();

		while (it.hasNext()) {
			final JJEdge edge = it.next();

			edge.setWeight(1.0);
		}
	}

	private void copyJJPositions(final MetroMapGraph mmGraph) {
		final Collection<JJNode> nodes = graph.getNodes();
		final Iterator<JJNode> it = nodes.iterator();
		final Hashtable<String, JJPoint> nodeHash = new Hashtable<>();

		while (it.hasNext()) {
			final JJNode node = it.next();
			final JJGraphicNode graphicNode = node.getGraphicNode(graphWindow);

			if (graphicNode != null) {
				final JJPoint coords = graphicNode.getCoords();
				nodeHash.put(node.getName(), coords);
			}
		}

		final Vector<Node> mmNodes = mmGraph.getNodes();
		for (int i = 0; i < mmNodes.size(); i++) {
			final Node mmNode = mmNodes.elementAt(i);

			final JJPoint coords = nodeHash.get(mmNode.getLabel());
			if (coords != null) {
				mmNode.setX(coords.getX());
				mmNode.setY(coords.getY());
			}
		}
	}

	private void copyMMPositions(final MetroMapGraph mmGraph) {
		final Collection<JJNode> nodes = graph.getNodes();
		final Iterator<JJNode> it = nodes.iterator();
		final Hashtable<String, JJPoint> nodeHash = new Hashtable<>();

		final Vector<Node> mmNodes = mmGraph.getNodes();
		for (int i = 0; i < mmNodes.size(); i++) {
			final Node mmNode = mmNodes.elementAt(i);

			final JJPoint coords = new JJPoint(mmNode.getX(), mmNode.getY());
			nodeHash.put(mmNode.getLabel(), coords);
		}

		while (it.hasNext()) {
			final JJNode node = it.next();
			final JJGraphicNode graphicNode = node.getGraphicNode(graphWindow);

			final JJPoint coords = nodeHash.get(node.getName());

			if (coords != null && graphicNode != null) {
				graphWindow.moveNodeTo(graphicNode, coords);
			}
		}
	}

}
