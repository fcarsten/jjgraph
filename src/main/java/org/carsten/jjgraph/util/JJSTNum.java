/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

/**
 * JJSTNum.java
 *
 *
 * Created: Tue Dec  7 10:37:38 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJNode;

public class JJSTNum {

	static void _pushPath(final JJDfs dfs, final HashMap<JJNode, Boolean> stFlagN,
			final HashMap<JJEdge, Boolean> stFlagE, final Stack<JJNode> zuTun, final JJNode knoten,
			final JJEdge kante) {
		// Debug.println("_pushPath");

		final Stack<JJNode> revStack = new Stack<>();

		final int goal = dfs.getLowNum(knoten);
		// JJGraph derGraph = graph_of(knoten);
		JJNode loopN = knoten;
		JJEdge loopE = kante;

		if ((dfs.getEdgeType(kante) == JJDfs.TI_BACKWARD) && (kante.getTarget() == knoten)) {
			stFlagE.put(kante, Boolean.TRUE);
			return;
		} else if ((dfs.getEdgeType(kante) == JJDfs.TI_TREE) && (kante.getTarget() == knoten)) {
			while (loopN != null) {
				stFlagN.put(loopN, Boolean.TRUE);
				stFlagE.put(loopE, Boolean.TRUE);
				revStack.push(loopN);

				for (final Iterator<JJEdge> iter = loopN.outIterator(); iter.hasNext();) {
					loopE = iter.next();

					loopN = null;
					if (dfs.getEdgeType(loopE) == JJDfs.TI_TREE) {
						loopN = loopE.getTarget(); // derGraph.target(loopE);
						if (dfs.getLowNum(loopN) == goal)
							break;
						else
							loopN = null;
					}
				}
			}
			loopN = revStack.peek();

			for (final Iterator<JJEdge> iter = loopN.outIterator(); iter.hasNext();) {
				loopE = iter.next();

				if (dfs.getEdgeType(loopE) == JJDfs.TI_BACKWARD) {
					loopN = loopE.getTarget(); // derGraph.target(loopE);
					if (dfs.getLowNum(loopN) == goal)
						break;
					else
						loopN = null;
				}
			}
			if (loopN != null) {
				stFlagE.put(loopE, Boolean.TRUE);
				stFlagN.put(loopN, Boolean.TRUE);
			}
		} else if ((dfs.getEdgeType(kante) == JJDfs.TI_BACKWARD) && (kante.getSource() == knoten)) {
			loopN = knoten;
			loopE = kante;

			stFlagE.put(loopE, Boolean.TRUE);

			while ((loopN != null) && (stFlagN.get(loopN).booleanValue() == false)) {
				stFlagE.put(loopE, Boolean.TRUE);
				stFlagN.put(loopN, Boolean.TRUE);
				revStack.push(loopN);
				final JJNode tmpN = loopN;
				loopN = null;

				for (final Iterator<JJEdge> iter = tmpN.inIterator(); iter.hasNext();) {
					loopE = iter.next();

					if (dfs.getEdgeType(loopE) == JJDfs.TI_TREE) {
						loopN = loopE.getSource(); // derGraph.source(loopE);
						stFlagE.put(loopE, Boolean.TRUE);
						break;
					}
				}
			}
		} else {
			// Debug.println( "Case shouldn't happen!");
			stFlagE.put(kante, Boolean.TRUE);
		}

		while (!revStack.isEmpty()) {
			// Debug.println("Pushing :" + ((JJNode)revStack.peek()).getName());
			zuTun.push(revStack.pop());
		}
	}

	static void _adjEdgeDirSt(final JJGraph derGraph, final HashMap<JJNode, Integer> stNum) {
		// Debug.println("_adjEdgeDirSt");
		final Stack<JJEdge> toRevert = new Stack<>();

		for (final Iterator<JJEdge> edgeIter = derGraph.edgeIterator(); edgeIter.hasNext();) {

			final JJEdge tmpE = edgeIter.next();

			if (stNum.get(tmpE.getSource()).intValue() > stNum.get(tmpE.getTarget()).intValue())
				toRevert.push(tmpE);
		}

		while (!toRevert.isEmpty())
			toRevert.pop().revert(); // derGraph.rev_edge(toRevert.pop());
	}

	static public Map<JJNode, Integer> compSTNum(final JJGraph derGraph, HashMap<JJNode, Integer> stNum, JJDfs dfs) {
		// Debug.println("compSTNum");

		// assert(Is_Biconnected(derGraph));

		if (stNum == null)
			stNum = new HashMap<>();

		if (dfs == null)
			dfs = new JJDfs(derGraph);

		final HashMap<JJNode, Boolean> stFlagN = new HashMap<>(); // h_array<node,
																	// bool>
																	// stFlagN;
		final HashMap<JJEdge, Boolean> stFlagE = new HashMap<>(); // h_array<edge,
																	// bool>
																	// stFlagE;
		// JJEdge tmpE;
		// JJNode tmpN;
		JJNode sNode = null;
		JJNode tNode = null;
		final Stack<JJNode> zuTun = new Stack<>(); // stack<node> zuTun;
		int aktuST = 0;

		for (final Iterator<JJNode> nodeIter = derGraph.nodeIterator(); nodeIter.hasNext();) {
			final JJNode tmpN = nodeIter.next();
			// Debug.println("" + tmpN + ":" + dfs.getDfsNum(tmpN));

			switch (dfs.getDfsNum(tmpN)) {
			case 0: {
				tNode = tmpN;
				stFlagN.put(tmpN, Boolean.TRUE);
				break;
			}
			case 1: {
				sNode = tmpN;
				stFlagN.put(tmpN, Boolean.TRUE);
				break;
			}
			default: {
				stFlagN.put(tmpN, Boolean.FALSE);
			}
			}
		}

		if (sNode == null)
			throw new RuntimeException("Could not find sNode");

		if (tNode == null)
			throw new RuntimeException("Could not find tNode");

		for (final Iterator<JJEdge> edgeIter = derGraph.edgeIterator(); edgeIter.hasNext();) {
			final JJEdge tmpE = edgeIter.next();

			if ((tmpE.getSource() == tNode) && (tmpE.getTarget() == sNode)) {
				Debug.println("Found s-t edge");
				// tmpE.setColor(new Color(200,0,0));

				stFlagE.put(tmpE, Boolean.TRUE);
			} else
				stFlagE.put(tmpE, Boolean.FALSE);
		}
		// assert(sNode != null);
		// assert(tNode != null);

		// Debug.println("Pushing :" + sNode.getName());

		zuTun.push(sNode);
		while (!zuTun.isEmpty()) {
			// Debug.println("Popping :" + ((JJNode)zuTun.peek()).getName());

			final JJNode loopN = zuTun.pop();
			boolean allDone = true;

			for (final Iterator<JJEdge> iter = loopN.outIterator(); iter.hasNext();) {
				final JJEdge tmpE = iter.next();

				if (stFlagE.get(tmpE).booleanValue() == false) {
					allDone = false;
					_pushPath(dfs, stFlagN, stFlagE, zuTun, tmpE.getTarget(), tmpE);
					// Debug.println("Pushing :" + loopN.getName());
					zuTun.push(loopN);
					break;
				}
			}
			if (allDone == true) {
				for (final Iterator<JJEdge> iter = loopN.inIterator(); iter.hasNext();) {
					final JJEdge tmpE = iter.next();
					if (stFlagE.get(tmpE).booleanValue() == false) {
						allDone = false;
						_pushPath(dfs, stFlagN, stFlagE, zuTun, tmpE.getSource(), tmpE);
						// Debug.println("Pushing :" + loopN.getName());
						zuTun.push(loopN);
						break;
					}
				}
			}
			if (allDone == true) {

				if (aktuST == 0)
					loopN.setName(loopN.getName() + " S");
				else
					loopN.setName(loopN.getName() + " St: " + aktuST);

				stNum.put(loopN, new Integer(aktuST++));
			}
		}
		tNode.setName(tNode.getName() + " T");
		stNum.put(tNode, new Integer(aktuST++));

		_adjEdgeDirSt(derGraph, stNum);
		return stNum;
	}

	static public int getSTNumber(final Map<JJNode, Integer> stMap, final JJNode n) {
		final Integer i = stMap.get(n);
		if (i == null) {
			Debug.println("Node not in ST map");
			return -1;
		}
		return i.intValue();
	}

	static void verifySTNum(final JJGraph tmpG, final HashMap<JJNode, Integer> nodeToSt) {
		// Debug.println("verifySTNum");
		int maxST = 0;
		// JJNode tmpN;

		for (final Iterator<JJNode> nodeIter = tmpG.nodeIterator(); nodeIter.hasNext();) {
			final JJNode tmpN = nodeIter.next();

			maxST = Math.max(maxST, nodeToSt.get(tmpN).intValue());
		}

		for (final Iterator<JJNode> nodeIter = tmpG.nodeIterator(); nodeIter.hasNext();) {
			final JJNode tmpN = nodeIter.next();

			final int stNum = nodeToSt.get(tmpN).intValue();
			boolean lowPre = false;
			boolean highSucc = false;
			if (stNum == 0)
				lowPre = true;
			else {
				for (final Iterator<JJEdge> iter = tmpN.inIterator(); iter.hasNext();) {
					final JJEdge tmpE = iter.next();

					if (nodeToSt.get(tmpE.getSource()).intValue() < stNum) {
						lowPre = true;
						break;
					}
				}
			}
			if (stNum == maxST)
				highSucc = true;
			else {
				for (final Iterator<JJEdge> iter = tmpN.outIterator(); iter.hasNext();) {
					final JJEdge tmpE = iter.next();

					if (nodeToSt.get(tmpE.getTarget()).intValue() > stNum) {
						highSucc = true;
						break;
					}
				}

			}
			// assert(lowPre && highSucc);
		}
	}
} // JJSTNum
