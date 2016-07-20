/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.francois;

/**
 * JJSugi.java
 *
 *
 * Created: Thu Apr 20 15:40:32 2000
 *
 * @author Carsten Friedrich
 * @version
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJGraphicEdge;
import org.carsten.jjgraph.graph.JJNode;
import org.carsten.jjgraph.layopt.JJLayOpt;
import org.carsten.jjgraph.layout.JJLayout;
import org.carsten.jjgraph.util.JJDfs;
import org.carsten.jjgraph.util.JJPoint;

class RemoveCycles {
	Digraph graph;
	List<Node> nodes = null;
	JJSugiNode sugiNodes[];
	JJSugiEdge sugiEdges[];

	public Node removeLeaves() {
		boolean didChange = true;
		Node result = null;
		int minDeg = -1;

		while (didChange) {
			didChange = false;
			result = null;
			minDeg = -1;

			for (final Iterator<Node> iter = nodes.iterator(); iter.hasNext();) {
				final Node tmpN = iter.next();
				final int outDeg = outdeg(tmpN);

				if (outDeg == 0) {
					didChange = true;
					removeNode(tmpN, iter);
				} else if ((result == null) || (outDeg < minDeg)) {
					result = tmpN;
					minDeg = outDeg;
				}
			}
		}
		return result;
	}

	void removeNode(final Node n, final Iterator<Node> iter) {
		iter.remove();
		removeNodeEdges(n);
	}

	void removeNode(final Node n) {
		nodes.remove(n);
		removeNodeEdges(n);
	}

	void removeNodeEdges(final Node n) {
		// Debug.println("Removing " + sugiNodes[n.id].jjnode.getName());

		for (final Iterator<JJEdge> edgeIter = sugiNodes[n.id].jjnode.inIterator(); edgeIter.hasNext();) {
			final Edge tmpE = sugiEdges[edgeIter.next().getValue()].fredge;
			tmpE.crv = true;
		}
	}

	void revertOutEdges(final Node n) {
		for (final Iterator<JJEdge> edgeIter = sugiNodes[n.id].jjnode.outIterator(); edgeIter.hasNext();) {

			final JJSugiEdge e = sugiEdges[edgeIter.next().getValue()];
			final Edge tmpE = e.fredge;

			if (!tmpE.crv) {
				// Debug.println("Reverting " +
				// sugiNodes[tmpE.source.id].jjnode.getName() +
				// " -> " + sugiNodes[tmpE.target.id].jjnode.getName());

				tmpE.revert();
				e.reverted = true;
				tmpE.crv = true;
			}
		}

		removeNode(n);
	}

	int outdeg(final Node n) {
		int res = 0;

		for (final Iterator<JJEdge> edgeIter = sugiNodes[n.id].jjnode.outIterator(); edgeIter.hasNext();) {
			final Edge tmpE = sugiEdges[edgeIter.next().getValue()].fredge;
			if (!tmpE.crv)
				res++;
		}
		return res;
	}

	RemoveCycles(final Digraph g, final JJSugiNode sn[], final JJSugiEdge se[]) {
		graph = g;
		nodes = new ArrayList<>();
		nodes.addAll(g.nodes);

		sugiNodes = sn;
		sugiEdges = se;
		Node tmpN = null;

		while ((tmpN = removeLeaves()) != null) {
			revertOutEdges(tmpN);
		}
	}

}

public class JJSugi implements JJLayout {
	private final JJGraphWindow fenster;
	private final JJGraph graph;
	private JJSugiNode sugiNodes[];
	private JJSugiEdge sugiEdges[];
	private Digraph digraph;

	public JJSugi(final JJGraphWindow f) {
		fenster = f;
		graph = f.getGraph();
	}

	@Override
	public int allowsOptimize() {
		if (!graph.isDirected()) {
			return JJLayOpt.TRANSLATE | JJLayOpt.SCALE_PROPORTIONAL | JJLayOpt.FLIP | JJLayOpt.ROTATE_ORTHOGONAL;
		}
		return JJLayOpt.TRANSLATE | JJLayOpt.SCALE_PROPORTIONAL | JJLayOpt.FLIP;

	}

	@Override
	public String getName() {
		return "Sugiyama";
	}

	public void createDigraph() {
		sugiNodes = new JJSugiNode[graph.getNumNodes()];
		sugiEdges = new JJSugiEdge[graph.getNumEdges()];
		digraph = new Digraph();

		int i = 0;

		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			final JJNode tmpN = iter.next();
			final Node n = new Node(digraph);

			tmpN.setValue(i);
			sugiNodes[i++] = new JJSugiNode(tmpN, n);
		}

		i = 0;
		for (final Iterator<JJEdge> iter = graph.edgeIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			final Edge e = new Edge(sugiNodes[tmpE.getSource().getValue()].frnode,
					sugiNodes[tmpE.getTarget().getValue()].frnode);
			tmpE.setValue(i);
			digraph.addEdge(e);
			sugiEdges[i++] = new JJSugiEdge(tmpE, e);
		}
	}

	public void writeNodePos() {
		int i;

		for (i = 0; i < sugiNodes.length; i++) {
			fenster.moveNodeTo(sugiNodes[i].jjnode.getGraphicNode(fenster), sugiNodes[i].frnode.xpos,
					-sugiNodes[i].frnode.ypos);
		}
		for (i = 0; i < sugiEdges.length; i++) {
			final JJGraphicEdge tmpGE = sugiEdges[i].jjedge.getGraphicEdge(fenster);
			for (final Object element : sugiEdges[i].fredge.dummies) {
				final Node n = (Node) element;
				if (sugiEdges[i].reverted)
					tmpGE.addBendFirst(new JJPoint(n.xpos, -n.ypos));
				else
					tmpGE.addBendLast(new JJPoint(n.xpos, -n.ypos));
			}
		}
	}

	@Override
	public void layout() {
		if (!graph.isDirected()) {
			final JJDfs dfs = new JJDfs(graph);
			dfs.doDfs();
			dfs.removeCycles();
		}

		createDigraph();

		if (graph.isDirected()) {
			new RemoveCycles(digraph, sugiNodes, sugiEdges);
		}

		final STT stt = new STT(digraph);
		stt.layout();

		writeNodePos();
	}

	@Override
	public void layout(final Collection<JJNode> c) {
		layout();
	}

	@Override
	public void apply(final String s) {
		layout();
	}

	@Override
	public boolean canDo(final String s) {
		return s.equals(getName());
	}

} // JJSugi
