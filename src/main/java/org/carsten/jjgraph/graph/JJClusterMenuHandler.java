/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

/**
 * JJClusterMenuHandler.java
 *
 *
 * Created: Mon Apr 17 17:01:49 2000
 *
 * @author Carsten Friedrich
 * @version
 */
import javax.swing.JCheckBoxMenuItem;

import org.carsten.jjgraph.clustering.JJCluster;
import org.carsten.jjgraph.clustering.JJImproveCluster;
import org.carsten.jjgraph.clustering.JJRandCluster;
import org.carsten.jjgraph.util.Debug;

public class JJClusterMenuHandler implements ActionListener {

	private final LinkedList<JJCluster> clusterAlgos = new LinkedList<>();
	private final JJGraphWindow window;
	private final JJGraph graph;
	private JJCluster nc = null;
	private boolean buildStructureGraph = false;
	private boolean colorGraph = false;

	public JJClusterMenuHandler(final JJGraphWindow g) {
		window = g;
		graph = window.getGraph();

	}

	public void add(final JJCluster l) {
		clusterAlgos.add(l);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getActionCommand().equals("structureGraph")) {
			buildStructureGraph = ((JCheckBoxMenuItem) e.getSource()).getState();
		}
		if (e.getActionCommand().equals("colorGraph")) {
			colorGraph = ((JCheckBoxMenuItem) e.getSource()).getState();
		} else {
			for (final JJCluster l : clusterAlgos) {
				if (l.getName().equals(e.getActionCommand())) {
					// Debug.println("Using: " + l.getName());
					graph.getUndoManager().openSubtask(l.getName());
					graph.getWindows().setBusy(true);
					if (l instanceof JJImproveCluster) {
						// Debug.println("Improving given clustering");
						if (nc == null) {
							graph.getWindows().printWarning("Using random clustering to initialize");
							nc = new JJRandCluster(graph, 4, window);
							nc.cluster();
						}
						((JJImproveCluster) l).cluster(nc);
						nc = l;

					} else {
						// Debug.println("computing new clustering");
						l.cluster();
						nc = l;
					}

					if (colorGraph) {
						nc.colorGraph();
					}

					graph.getUndoManager().closeSubtask(l.getName());
					graph.getWindows().setBusy(false);
					updateStructureGraph(nc);
					return;
				}
			}
		}
	}

	private JJGraphWindow otherWindow;

	public void setOtherWindow(final JJGraphWindow w) {
		otherWindow = w;
	}

	public JJGraphWindow getOtherWindow() {
		return otherWindow;
	}

	public void updateStructureGraph(final JJCluster tmpC) {
		if (otherWindow != null) {
			otherWindow.getFrame().dispose(); // fenster.dispose();
			otherWindow = null;
		}

		if (buildStructureGraph) {
			try {
				final JJGraph tmpG = graph.getClass().newInstance();
				final JJGraphWindow fenster2 = tmpG.createGraphic();
				tmpC.buildStructureGraph(tmpG);
				otherWindow = fenster2;
			} catch (final InstantiationException e) {
				Debug.println("Could not create new graph: " + e.getMessage());
			} catch (final IllegalAccessException e) {
				Debug.println("Could not create new graph: " + e.getMessage());
			}

			// fenster2.setOtherWindow(this); // otherWindow = this;
		}
	}

} // JJClusterMenuHandler
