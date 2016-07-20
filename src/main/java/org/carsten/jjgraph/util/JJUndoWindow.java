/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;

/**
 * JJUndoWindow.java
 *
 *
 * Created: Mon Nov 15 11:12:12 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.carsten.jjgraph.graph.JJInspectable;

public class JJUndoWindow extends JPanel implements ActionListener, JJInspectable {
	private JButton undoButton;
	private JTree undoTree;
	private DefaultTreeModel treeModel;

	private final JJUndo undo;

	private DefaultMutableTreeNode root;
	private DefaultMutableTreeNode currentParent;

	@Override
	public String getTabName() {
		return "Undo";
	}

	public JJUndoWindow(final JJUndo u) {
		undo = u;
	}

	@Override
	public JPanel createTab() {

		final JPanel panel = new JPanel();

		final JLabel titleLabel = new JLabel("Undo Control");

		undoButton = new JButton("Undo");
		undoButton.addActionListener(this);

		root = new DefaultMutableTreeNode("Undo operations");
		currentParent = root;

		treeModel = new DefaultTreeModel(root, true);

		undoTree = new JTree(treeModel);
		undoTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		final JScrollPane treeView = new JScrollPane(undoTree);

		final BorderLayout b1 = new BorderLayout();
		panel.setLayout(b1);
		panel.add(titleLabel, BorderLayout.NORTH);

		// adding the entry Panel
		final JPanel entryPanel = new JPanel();
		panel.add(treeView, BorderLayout.CENTER);

		panel.add(undoButton, BorderLayout.SOUTH);

		// getContentPane().add(panel);
		// pack();
		// show();

		undo.setWindow(this);
		return panel;
	}

	public void undo() {

		final TreePath tp = undoTree.getPathForRow(undoTree.getRowCount() - 1);
		// Debug.println(tp);

		final DefaultMutableTreeNode last = (DefaultMutableTreeNode) tp.getLastPathComponent();
		DefaultMutableTreeNode tmpL = last.getPreviousNode();

		undo(last);

		if (last != root) {
			while (tmpL.getAllowsChildren() && (tmpL != root)) {
				final DefaultMutableTreeNode tmpN = tmpL;
				tmpL = tmpL.getPreviousNode();
				treeModel.removeNodeFromParent(tmpN);

				if (tmpL == root) {
					break;
				}
			}
		}
		if (undoTree.getSelectionCount() != 0)
			undo();

		currentParent = root;
	}

	public void undo(final DefaultMutableTreeNode node) {
		// Debug.println("Trying undo");

		if (!node.getAllowsChildren()) {
			// Debug.println("Found leaf");
			try {
				undo.undo();
			} catch (final JJUndoOnUnstableGraphException e) {
				Debug.println(e.getMessage());
			}

			treeModel.removeNodeFromParent(node);
			return;
		}
		while (node.getChildCount() > 0)
			undo((DefaultMutableTreeNode) node.getLastChild());

		if (node != root)
			treeModel.removeNodeFromParent(node);
	}

	@Override
	public void actionPerformed(final java.awt.event.ActionEvent e) {

		// Debug.println("Should undo now");
		if ((e.getSource() == undoButton) || e.getActionCommand().equals("undo")) {
			undo();
		} else {
			Debug.println("Unexpected action event");
		}
	}

	public void addLeaf(final String n) {
		final DefaultMutableTreeNode tmpN = new DefaultMutableTreeNode(n);
		tmpN.setAllowsChildren(false);

		treeModel.insertNodeInto(tmpN, currentParent, currentParent.getChildCount());

		// Make sure the user can see the lovely new node.
		// undoTree.scrollPathToVisible(new TreePath(tmpN.getPath()));
	}

	public void addFolder(final String n) {
		final DefaultMutableTreeNode tmpN = new DefaultMutableTreeNode(n);

		treeModel.insertNodeInto(tmpN, currentParent, currentParent.getChildCount());
		currentParent = tmpN;

		// Make sure the user can see the lovely new node.
		// undoTree.scrollPathToVisible(new TreePath(tmpN.getPath()));
	}

	public void closeFolder() {
		currentParent = (DefaultMutableTreeNode) currentParent.getParent();
	}

} // JJUndoWindow
