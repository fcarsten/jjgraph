/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

import java.awt.Color;
/**
 * JJDfs.java
 *
 *
 * Created: Mon Dec  6 15:19:01 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJGraphicEdge;
import org.carsten.jjgraph.graph.JJNode;

public class JJDfs {
	public final static int TI_NO = 0;
	public final static int TI_TREE = 1;
	public final static int TI_FORWARD = 2;
	public final static int TI_BACKWARD = 3;
	public final static int TI_CROSS = 4;

	protected int aktuDfs;
	protected int aktuComp;
	protected int aktuLow;

	protected HashSet<JJNode> todo;

	protected DfsMap<JJNode, DfsNode> dfsNodes = new DfsMap<>();
	protected DfsMap<JJEdge, DfsEdge> dfsEdges = new DfsMap<>();

	protected JJGraph graph;
	private JJGraphWindow window;

	/**
	 * Get the value of window.
	 *
	 * @return value of window.
	 */
	public JJGraphWindow getWindow() {
		return window;
	}

	/**
	 * Set the value of window.
	 *
	 * @param v
	 *            Value to assign to window.
	 */
	public void setWindow(final JJGraphWindow v) {
		this.window = v;
	}

	public JJDfs(final JJGraph g, final JJGraphWindow f) {
		window = f;
		graph = g;
	}

	public JJDfs(final JJGraph g) {
		graph = g;
	}

	public void doDfs() {
		todo = new HashSet<>();

		aktuDfs = 0;
		aktuComp = 0;
		aktuLow = 0;

		for (final Iterator<JJEdge> edgeIter = graph.edgeIterator(); edgeIter.hasNext();) {
			final JJEdge tmpE = edgeIter.next();
			dfsEdges.put(tmpE, new DfsEdge(tmpE));
		}

		for (final Iterator<JJNode> nodeIter = graph.nodeIterator(); nodeIter.hasNext();) {
			final JJNode tmpN = nodeIter.next();
			dfsNodes.put(tmpN, new DfsNode(tmpN));

			todo.add(tmpN);
		}

		while (!todo.isEmpty()) {
			// Debug.println("Computing dfs");
			if (graph.isDirected()) {
				_dfsDir((todo.iterator().next()));
			} else {
				_dfsUndir((todo.iterator().next()));
			}

		}

		if (!graph.isDirected())
			_adjEdgeDir();

		if (window != null)
			labelNodes();
	}

	protected void labelNodes() {
		for (final Iterator<JJNode> nodeIter = graph.nodeIterator(); nodeIter.hasNext();) {
			final JJNode tmpN = nodeIter.next();
			tmpN.setName("" + getDfsNum(tmpN) + ":" + getLowNum(tmpN));
		}
	}

	protected void _adjEdgeDir() {
		final Stack<JJEdge> toRevert = new Stack<>();

		for (final Iterator<JJEdge> edgeIter = graph.edgeIterator(); edgeIter.hasNext();) {

			final JJEdge tmpE = edgeIter.next();
			final int et = dfsEdges.getEdge(tmpE).edgeType;
			final int ds = dfsNodes.getNode(tmpE.getSource()).dfsNum;
			final int dt = dfsNodes.getNode(tmpE.getTarget()).dfsNum;

			if (et == TI_TREE) {
				if (ds > dt)
					toRevert.push(tmpE);
			} else if (ds < dt)
				toRevert.push(tmpE);
		}

		while (!toRevert.isEmpty()) {
			final JJEdge tmpE = toRevert.pop();
			tmpE.revert();
		}
	}

	public void removeCycles() {
		final Stack<JJEdge> toRevert = new Stack<>();

		for (final Iterator<JJEdge> edgeIter = graph.edgeIterator(); edgeIter.hasNext();) {

			final JJEdge tmpE = edgeIter.next();
			final int et = dfsEdges.getEdge(tmpE).edgeType;
			final int ds = dfsNodes.getNode(tmpE.getSource()).dfsNum;
			final int dt = dfsNodes.getNode(tmpE.getTarget()).dfsNum;

			if (et == TI_BACKWARD) {
				toRevert.push(tmpE);
			}
		}

		while (!toRevert.isEmpty()) {
			final JJEdge tmpE = toRevert.pop();
			tmpE.revert();
		}
	}

	protected void _dfsUndir(final JJNode knoten) {
		todo.remove(knoten);
		// Debug.println("Adding " + knoten + ":" + aktuDfs);

		dfsNodes.getNode(knoten).dfsNum = aktuDfs;
		dfsNodes.getNode(knoten).lowNum = aktuDfs++;

		for (final Iterator<JJEdge> iter = knoten.edgeIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			final int et = dfsEdges.getEdge(tmpE).edgeType;

			if (et == TI_NO) {
				final JJNode tmpN = tmpE.opposite(knoten);

				int l1 = dfsNodes.getNode(knoten).lowNum;
				final int d1 = dfsNodes.getNode(tmpN).dfsNum;
				JJGraphicEdge ge = null;

				if (window != null)
					ge = tmpE.getGraphicEdge(window);

				if (todo.contains(tmpN)) {
					if (ge != null)
						ge.setColor(new Color(0, 200, 0));
					dfsEdges.getEdge(tmpE).edgeType = TI_TREE;

					_dfsUndir(tmpN);

					final int l2 = dfsNodes.getNode(tmpN).lowNum;
					l1 = dfsNodes.getNode(knoten).lowNum;
					dfsNodes.getNode(knoten).lowNum = Math.min(l1, l2);
				} else {
					if (ge != null)
						ge.setColor(new Color(0, 0, 200));
					dfsEdges.getEdge(tmpE).edgeType = TI_BACKWARD;
					dfsNodes.getNode(knoten).lowNum = Math.min(l1, d1);
				}
			}
		}

		dfsNodes.getNode(knoten).compNum = aktuComp++;
	}

	protected void _dfsDir(final JJNode knoten) {
		todo.remove(knoten);
		// Debug.println("Adding " + knoten + ":" + aktuDfs);

		dfsNodes.getNode(knoten).dfsNum = aktuDfs;
		dfsNodes.getNode(knoten).lowNum = aktuDfs++;

		for (final Iterator<JJEdge> iter = knoten.inIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			JJGraphicEdge ge = null;

			if (window != null)
				ge = tmpE.getGraphicEdge(window);

			final int et = dfsEdges.getEdge(tmpE).edgeType;

			if (et == TI_NO) {
				final JJNode tmpN = tmpE.getSource();

				int l1 = dfsNodes.getNode(knoten).lowNum;
				final int d1 = dfsNodes.getNode(tmpN).dfsNum;

				if (todo.contains(tmpN)) {
					if (ge != null)
						ge.setColor(new Color(0, 200, 0));
					dfsEdges.getEdge(tmpE).edgeType = TI_TREE;

					_dfsDir(tmpN);

					final int l2 = dfsNodes.getNode(tmpN).lowNum;
					l1 = dfsNodes.getNode(knoten).lowNum;
					dfsNodes.getNode(knoten).lowNum = Math.min(l1, l2);
				} else {
					if (ge != null)
						ge.setColor(new Color(0, 0, 200));
					dfsEdges.getEdge(tmpE).edgeType = TI_BACKWARD;
					dfsNodes.getNode(knoten).lowNum = Math.min(l1, d1);
				}
			}
		}

		for (final Iterator<JJEdge> iter = knoten.outIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			JJGraphicEdge ge = null;

			if (window != null)
				ge = tmpE.getGraphicEdge(window);
			final int et = dfsEdges.getEdge(tmpE).edgeType;

			if (et == TI_NO) {
				final JJNode tmpN = tmpE.getTarget();
				int l1 = dfsNodes.getNode(knoten).lowNum;
				final int d1 = dfsNodes.getNode(tmpN).dfsNum;

				if (todo.contains(tmpN)) {
					if (ge != null)
						ge.setColor(new Color(0, 200, 0));
					dfsEdges.getEdge(tmpE).edgeType = TI_TREE;

					_dfsDir(tmpN);

					final int l2 = dfsNodes.getNode(tmpN).lowNum;
					l1 = dfsNodes.getNode(knoten).lowNum;
					dfsNodes.getNode(knoten).lowNum = Math.min(l1, l2);
				} else {
					if (ge != null)
						ge.setColor(new Color(0, 0, 200));
					dfsEdges.getEdge(tmpE).edgeType = TI_BACKWARD;
					dfsNodes.getNode(knoten).lowNum = Math.min(l1, d1);
				}
			}
		}

		dfsNodes.getNode(knoten).compNum = aktuComp++;
	}

	public int getDfsNum(final JJNode n) {
		return dfsNodes.getNode(n).dfsNum;
	}

	public int getEdgeType(final JJEdge n) {
		return dfsEdges.getEdge(n).edgeType;
	}

	public int getCompNum(final JJNode n) {
		return dfsNodes.getNode(n).compNum;
	}

	public int getLowNum(final JJNode n) {
		return dfsNodes.getNode(n).lowNum;
	}

} // JJDfs
