/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.layout;

/**
 * JJTree.java
 *
 *
 * Created: Wed Dec  8 11:15:52 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import java.util.Collection;
import java.util.Iterator;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJNode;
import org.carsten.jjgraph.layopt.JJLayOpt;

public class JJTree implements JJLayout {
	private final JJGraphWindow fenster;
	private final JJGraph graph;
	private int OPT_DIST = 50;
	private int depth = 0;

	public JJTree(final JJGraphWindow f) {
		fenster = f;
		graph = f.getGraph();
	}

	@Override
	public int allowsOptimize() {
		return JJLayOpt.TRANSLATE | JJLayOpt.SCALE | JJLayOpt.FLIP;
	}

	public int getOptDist() {
		return OPT_DIST;
	}

	public void setOptDist(final int newValue) {
		OPT_DIST = newValue;
	}

	private JJNode findRoot() {
		for (final Iterator<JJNode> nodeIter = graph.nodeIterator(); nodeIter.hasNext();) {
			final JJNode tmpN = nodeIter.next();
			if (tmpN.indeg() == 0)
				return tmpN;
		}
		return null;
	}

	@Override
	public void layout() {
		fenster.removeBends();

		final JJNode root = findRoot();

		if (root != null) {
			depth = 0;
			graph.setNodeValue(-1);
			treeLayout(root);
			setXCoord(root, 0);
		} else
			throw new Error("Could not find root node for tree");
	}

	@Override
	public void layout(final Collection<JJNode> c) {
		layout();
	}

	private void setXCoord(final JJNode n, int x) {
		// Debug.println(n.getName() + "(" + n.getValue() + "): " +x);

		fenster.rmoveNodeTo(n.getGraphicNode(fenster), OPT_DIST * (x + n.getValue() / 2), 0);

		if (n.getValue() == -1) {
			fenster.printError("Graph is not a tree!");
			return;
		}

		if (n.outdeg() == 0) {
			return;
		}

		for (final Iterator<JJEdge> iter = n.outIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			final JJNode tmpN = tmpE.getTarget();

			setXCoord(tmpN, x);
			x += tmpN.getValue();
			tmpN.setValue(-1);
		}
	}

	public void treeLayout(final JJNode n) {
		depth++;
		int width = 0;

		fenster.moveNodeTo(n.getGraphicNode(fenster), 100, 100 + OPT_DIST * depth);

		if (n.getValue() != -1) {
			fenster.printError("Graph is not a tree!");
			return;
		}

		if (n.outdeg() == 0) {
			n.setValue(2);
			depth--;
			return;
		}

		for (final Iterator<JJEdge> iter = n.outIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			final JJNode tmpN = tmpE.getTarget();
			treeLayout(tmpN);
			width += tmpN.getValue();
			// tmpN.setValue(width);
		}

		n.setValue(width);

		depth--;
	}

	@Override
	public String getName() {
		return "Tree";

	}

	@Override
	public void apply(final String s) {
		layout();
	}

	@Override
	public boolean canDo(final String s) {
		return s.equals(getName());

	}

} // JJTree
