/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * JJSPQRWindow.java
 *
 *
 * Created: Mon Nov 15 11:12:12 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphEvent;
import org.carsten.jjgraph.graph.JJGraphImpl;
import org.carsten.jjgraph.graph.JJGraphListener;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJGraphicNode;
import org.carsten.jjgraph.graph.JJInspectable;
import org.carsten.jjgraph.graph.JJNode;

public class JJSPQRWindow extends JPanel implements ActionListener, ChangeListener, JJInspectable, JJGraphListener {
	private JButton initB;
	private JButton addEdgeB;
	private JButton splitB;
	private JButton layoutB;
	private JButton compressB;

	private JCheckBox showTreeB;

	private final JJGraph graph;
	private final JJGraphWindow fenster;
	private final JJSPQR spqr;

	private final Color blue = new Color(100, 100, 255);
	private final Color darkGreen = new Color(0, 100, 0);

	@Override
	public void graphStructureChanged(final JJGraphEvent e) {
		disableButtons();
		spqr.setNeedsInit(true);
	}

	public void graphAppearanceChanged(final JJGraphEvent e) {
	}

	@Override
	public String getTabName() {
		return ("SPQR");
	}

	public JJSPQRWindow(final JJGraphWindow g) {
		fenster = g;
		graph = g.getGraph();
		graph.addStructureListener(this);

		spqr = new JJSPQR(fenster, this);
	}

	public void disableButtons() {
		addEdgeB.setEnabled(false);
		splitB.setEnabled(false);
		layoutB.setEnabled(false);
	}

	public void enableButtons() {
		addEdgeB.setEnabled(true);
		splitB.setEnabled(true);
		layoutB.setEnabled(true);
	}

	@Override
	public JPanel createTab() {
		final JPanel panel = new JPanel();

		final JLabel titleLabel = new JLabel("SPQR Control");

		initB = new JButton("Init");
		initB.addActionListener(this);

		addEdgeB = new JButton("Add Edge");
		addEdgeB.addActionListener(this);

		splitB = new JButton("Split");
		splitB.addActionListener(this);

		layoutB = new JButton("Layout");
		layoutB.addActionListener(this);

		compressB = new JButton("Compress");
		compressB.addActionListener(this);

		showTreeB = new JCheckBox("Show Tree");
		showTreeB.addChangeListener(this);

		final BorderLayout b1 = new BorderLayout();
		panel.setLayout(b1);
		panel.add(titleLabel, BorderLayout.NORTH);

		// adding the entry Panel
		final JPanel entryPanel = new JPanel();
		panel.add(entryPanel, BorderLayout.CENTER);

		final GridLayout g1 = new GridLayout(3, 2);
		entryPanel.setLayout(g1);

		entryPanel.add(initB);
		entryPanel.add(addEdgeB);

		entryPanel.add(splitB);
		entryPanel.add(layoutB);

		entryPanel.add(compressB);
		entryPanel.add(showTreeB);
		disableButtons();

		final JPanel arg = new JPanel();
		arg.setLayout(new BorderLayout());
		arg.add(panel, BorderLayout.NORTH);

		return arg;
	}

	@Override
	public void actionPerformed(final java.awt.event.ActionEvent e) {
		graph.getUndoManager().openSubtask(e.getActionCommand());

		if (e.getActionCommand().equals("Init")) {
			// Debug.println("Init");
			if (spqr.init())
				enableButtons();
			// Debug.println("Init finished");

		} else if (e.getActionCommand().equals("Layout")) {
			// Debug.println("Layout not implemented");
			spqr.layout();
		} else if (e.getActionCommand().equals("Compress")) {
			// Debug.println("Compress");
			spqr.compress();
		} else if (e.getActionCommand().equals("Split")) {
			// Debug.println("Split");
			if (fenster.getSelectedEdge() != null)
				spqr.split(fenster.getSelectedEdge().getEdge());
			else
				fenster.printError("Select an edge");
		} else if (e.getActionCommand().equals("Add Edge")) {
			// Debug.println("Add Edge");
			if (fenster.getSelectedNodes().size() != 2)
				fenster.printError("Select exactly two nodes");
			else {
				final Iterator<JJGraphicNode> i = fenster.getSelectedNodes().iterator();
				final JJGraphicNode n1 = i.next();
				final JJGraphicNode n2 = i.next();
				spqr.addEdge(n1.getNode(), n2.getNode());

				// spqr.addEdge(((JJGraphicNode)w.getSelectedNodes().get
				// (0)).getNode(),
				// ((JJGraphicNode)w.getSelectedNodes().get (1)).getNode());
			}

		} else {
			Debug.println("Unknown command " + e.getActionCommand());
		}
		graph.getUndoManager().closeSubtask(e.getActionCommand());
	}

	@Override
	public void stateChanged(final javax.swing.event.ChangeEvent e) {
		spqr.showTree();
	}

	public void message(final String txt) {
		fenster.printError(txt);
	}

	public void alertBox(final String txt) {
		JOptionPane.showMessageDialog(null, txt);
	}

	public void confirmStep(final String txt, final HashSet<JJNode> toSel) {
		for (final JJNode jjNode : toSel) {
			final JJNode tmpN = jjNode;
			// tmpN.setColor(darkGreen); !!! not implemented
		}

		alertBox(txt);

		for (final Object element : toSel) {
			final JJNode tmpN = (JJNode) element;
			// tmpN.setColor(blue); !!! not implemented
		}

	}

	public void showPath(final LinkedList<JJEdge> pfad) {
		// for(Iterator iter = pfad.listIterator(); iter.hasNext();){
		// JJEdge e = (JJEdge) iter.next();
		// e.setColor(darkGreen);
		// }
		Debug.println("Show path not implemented");

	}

	public JJGraph newGraph(final boolean visible) {
		final JJGraph tmpG = new JJGraphImpl();
		if (visible)
			tmpG.createGraphic();

		return tmpG;
	}

} // JJSPQRWindow
