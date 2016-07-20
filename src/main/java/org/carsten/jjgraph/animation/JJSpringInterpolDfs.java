/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

/**
 * JJGemInterpolDfs.java
 *
 *
 * Created: Mon Feb 19 14:39:09 2001
 *
 * @author
 * @version
 */
import java.util.HashSet;
import java.util.Iterator;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJNode;
import org.carsten.jjgraph.util.DfsEdge;
import org.carsten.jjgraph.util.DfsNode;
import org.carsten.jjgraph.util.JJDfs;

public class JJSpringInterpolDfs extends JJDfs {
	private final JJNode sNode;
	private final JJNode tNode;

	public JJSpringInterpolDfs(final JJGraph g, final JJGraphWindow f, final JJNode s, final JJNode t) {
		super(g, f);
		sNode = s;
		tNode = t;
	}

	public JJSpringInterpolDfs(final JJGraph g, final JJNode s, final JJNode t) {
		super(g);
		sNode = s;
		tNode = t;
	}

	@Override
	public void doDfs() {
		todo = new HashSet<>();

		aktuDfs = 1;
		aktuComp = 0;
		aktuLow = 0;

		for (final Iterator<JJEdge> edgeIter = graph.edgeIterator(); edgeIter.hasNext();) {

			final JJEdge tmpE = edgeIter.next();
			final DfsEdge dfsEdge = new DfsEdge(tmpE);

			if (tmpE.contains(sNode) && tmpE.contains(tNode)) {
				dfsEdge.edgeType = TI_TREE;
			}

			dfsEdges.put(tmpE, dfsEdge);
		}

		for (final Iterator<JJNode> nodeIter = graph.nodeIterator(); nodeIter.hasNext();) {
			final JJNode tmpN = nodeIter.next();
			final DfsNode dfsNode = new DfsNode(tmpN);
			dfsNodes.put(tmpN, dfsNode);

			if (tmpN == sNode) {
				dfsNode.dfsNum = 0;
				dfsNode.lowNum = 0;
				dfsNode.compNum = graph.getNumNodes() - 1;
			} else if (tmpN == tNode) {
				// dfsNode.dfsNum = 1;
				// dfsNode.lowNum = 0;
				// dfsNode.compNum = graph.getNumNodes()-2;
			} else
				todo.add(tmpN);
		}

		_dfsUndir(tNode);

		// while(!todo.isEmpty())
		// {
		// _dfsUndir((JJNode) (todo.iterator().next()));
		// }

		// We have to find and adjust the edge between node 1 and 2

		// for(Iterator iter = sNode.edgeIterator(); iter.hasNext();){
		// JJEdge tmpE = (JJEdge) iter.next();
		// JJNode tmpN = tmpE.opposite(sNode);
		// if(getDfsNum(tmpN) == 2){
		// dfsEdges.getEdge(tmpE).edgeType = TI_TREE;
		// Debug.println("Corrected edge 1-2");

		// break;
		// }
		// }

		_adjEdgeDir();
		if (getWindow() != null)
			labelNodes();

	}

} // JJGemInterpolDfs
