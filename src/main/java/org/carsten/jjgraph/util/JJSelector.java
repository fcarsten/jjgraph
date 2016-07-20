/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * JJSelector.java
 *
 *
 * Created: Mon Nov 15 11:12:12 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindowImpl;
import org.carsten.jjgraph.graph.JJGraphicNode;
import org.carsten.jjgraph.graph.JJInspectable;
import org.carsten.jjgraph.graph.JJNode;

public class JJSelector implements JJInspectable, ActionListener {
	private final JJGraph graph;
	private final JJGraphWindowImpl fenster;
	private JComboBox<String> actionBox;
	private JComboBox<String> targetBox;
	private JTextField tf;
	private JButton select;

	public final static int SELECT = 0;
	public final static int HIDE = 1;
	public final static int SHOW = 2;
	public final static int SHOW_ONLY = 3;
	public final static int DELETE = 4;

	public final static int NODES = 0;
	public final static int EDGES = 1;

	@Override
	public void actionPerformed(final java.awt.event.ActionEvent e) {
		if (e.getSource() == actionBox) {
			if (actionBox.getSelectedIndex() == SELECT) {
				targetBox.setSelectedIndex(NODES);
				targetBox.setEnabled(false);
			} else
				targetBox.setEnabled(true);
		} else if (e.getSource() == select) {
			final String exp = tf.getText();

			Pattern reg = null;
			try {
				reg = Pattern.compile(exp);
			} catch (final PatternSyntaxException err) {
				// Debug.println(err.getMessage());
				return;
			}
			final boolean tmpRedraw = fenster.setRedraw(false);

			switch (targetBox.getSelectedIndex()) {
			case NODES: {
				switch (actionBox.getSelectedIndex()) {
				case SELECT: {
					selectNodes(reg);
					break;
				}
				case HIDE: {
					hideNodes(reg);
					break;
				}
				case SHOW: {
					showNodes(reg);
					break;
				}
				case SHOW_ONLY: {
					showOnlyNodes(reg);
					break;
				}
				case DELETE: {
					deleteNodes(reg);
					break;
				}
				default: {
				}
				}
				break;
			}
			case EDGES: {
				switch (actionBox.getSelectedIndex()) {
				case HIDE: {
					hideEdges(reg);
					break;
				}
				case SHOW: {
					showEdges(reg);
					break;
				}
				case SHOW_ONLY: {
					showOnlyEdges(reg);
					break;
				}
				case DELETE: {
					deleteEdges(reg);
					break;
				}
				default: {
				}
				}
				break;
			}
			default: {
			}
			}
			fenster.setRedraw(tmpRedraw);
		}
		return;
	}

	public void selectNodes(final Pattern reg) {
		final java.util.List<JJGraphicNode> toDel = new LinkedList<>();
		// Debug.println("Deleting nodes");

		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			final JJNode tmpE = iter.next();
			if (reg.matcher(tmpE.getName()).matches()) {
				// Debug.println("Selecting " + tmpE.getName());
				toDel.add(tmpE.getGraphicNode(fenster));
			}

		}
		fenster.selectNodes(toDel);
	}

	public void hideNodes(final Pattern reg) {
		// Debug.println("Hiding nodes");

		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			final JJNode tmpE = iter.next();
			if (reg.matcher(tmpE.getName()).matches())
				tmpE.getGraphicNode(fenster).hide();
		}
	}

	public void showNodes(final Pattern reg) {
		// Debug.println("Showing nodes");

		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			final JJNode tmpE = iter.next();
			if (reg.matcher(tmpE.getName()).matches())
				tmpE.getGraphicNode(fenster).unhide();
		}
	}

	public void showOnlyNodes(final Pattern reg) {
		// Debug.println("Showing nodes");

		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			final JJNode tmpE = iter.next();
			if (reg.matcher(tmpE.getName()).matches())
				tmpE.getGraphicNode(fenster).unhide();
			else
				tmpE.getGraphicNode(fenster).hide();
		}
	}

	public void deleteNodes(final Pattern reg) {
		final java.util.List<JJNode> toDel = new LinkedList<>();
		// Debug.println("Deleting nodes");

		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			final JJNode tmpE = iter.next();
			if (reg.matcher(tmpE.getName()).matches()) {
				// Debug.println("Deleting " + tmpE.getName());
				toDel.add(tmpE);
			}

		}
		for (final Object element : toDel) {
			graph.deleteNode((JJNode) element);
		}
	}

	public void hideEdges(final Pattern reg) {
		// Debug.println("Hiding edges");

		for (final Iterator<JJEdge> iter = graph.edgeIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			if (reg.matcher(tmpE.getName()).matches())
				tmpE.getGraphicEdge(fenster).hide();
		}
	}

	public void showEdges(final Pattern reg) {
		// Debug.println("Showing edges");

		for (final Iterator<JJEdge> iter = graph.edgeIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			if (reg.matcher(tmpE.getName()).matches())
				tmpE.getGraphicEdge(fenster).unhide();
		}
	}

	public void showOnlyEdges(final Pattern reg) {
		// Debug.println("Showing edges");

		for (final Iterator<JJEdge> iter = graph.edgeIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			if (reg.matcher(tmpE.getName()).matches())
				tmpE.getGraphicEdge(fenster).unhide();
			else
				tmpE.getGraphicEdge(fenster).hide();
		}
	}

	public void deleteEdges(final Pattern reg) {
		final java.util.List<JJEdge> toDel = new LinkedList<>();
		// Debug.println("Deleting edges");

		for (final Iterator<JJEdge> iter = graph.edgeIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			if (reg.matcher(tmpE.getName()).matches()) {
				// Debug.println("Deleting " + tmpE.getName());
				toDel.add(tmpE);
			}

		}
		for (final Object element : toDel) {
			graph.deleteEdge((JJEdge) element);
		}
	}

	@Override
	public JPanel createTab() {
		final JPanel arg = new JPanel();
		arg.setLayout(new BorderLayout());
		arg.add(new JLabel("Select nodes/edges"), BorderLayout.NORTH);

		final String actionList[] = { "Select", "Hide", "Show", "Show only", "Delete" };
		actionBox = new JComboBox<>(actionList);
		actionBox.setSelectedIndex(0);
		actionBox.addActionListener(this);

		final String targetList[] = { "Nodes", "Edges" };
		targetBox = new JComboBox<>(targetList);
		targetBox.setSelectedIndex(0);
		targetBox.addActionListener(this);
		targetBox.setEnabled(false);

		tf = new JTextField(".*", 16);

		final JPanel controls = new JPanel();
		controls.add(actionBox);
		controls.add(targetBox);
		controls.add(new JLabel("matching"));
		controls.add(tf);
		arg.add(controls, BorderLayout.CENTER);

		select = new JButton("Select");
		select.addActionListener(this);

		arg.add(select, BorderLayout.SOUTH);
		final JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(arg, BorderLayout.NORTH);

		return p;
	}

	public JJSelector(final JJGraphWindowImpl f) {
		fenster = f;
		graph = fenster.getGraph();
	}

	@Override
	public String getTabName() {
		return "Selector";
	}

} // JJSelector
