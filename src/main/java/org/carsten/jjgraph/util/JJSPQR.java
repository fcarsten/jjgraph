/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

/**
 * JJSPQR.java
 *
 *
 * Created: Mon Dec  6 13:57:05 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphImpl;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJMisc;
import org.carsten.jjgraph.graph.JJNode;
import org.carsten.jjgraph.graph.JJNodePair;

public class JJSPQR {
	private final JJGraphWindow window;
	private final JJSPQRWindow fenster;

	final static double MINDIST = 100.0;

	private boolean showTreeFlag;
	private final boolean singleStepFlag = false;
	private boolean needsInit;

	private final JJGraph spqrTree;

	public JJGraph getSpqrTree() {
		return spqrTree;
	}

	private final JJAuxStruct aux;

	private final HashMap<JJNode, JJSpqrNode> nodeToSpqrNode;
	private final HashMap<JJNode, JJGTNode> nodeToGTNode;
	private final HashMap<JJEdge, JJGTEdge> edgeToGTEdge;

	// private final HashMap virtualPos;

	private int nodeCount;

	private JJNode spqrRoot;

	public JJNode getSpqrRoot() {
		return spqrRoot;
	}

	private JJNode theSNode;

	public JJNode getSNode() {
		return theSNode;
	}

	public JJNode getTNode() {
		return theTNode;
	}

	private JJNode theTNode;
	private JJEdge theSTEdge;

	public void setNeedsInit(final boolean f) {
		needsInit = f;
	}

	public JJGraph getGraph() {
		return window.getGraph();
	}

	public JJSPQR(final JJGraphWindow g, final JJSPQRWindow f) {
		window = g;
		fenster = f;

		nodeCount = 0;
		showTreeFlag = false;

		spqrTree = new JJGraphImpl();
		if (showTreeFlag)
			spqrTree.createGraphic();

		aux = new JJAuxStruct();

		nodeToSpqrNode = new HashMap<>();
		nodeToGTNode = new HashMap<>();
		edgeToGTEdge = new HashMap<>();
		// virtualPos = new HashMap<>();

		spqrRoot = null;
		theSNode = null;
		theTNode = null;
		theSTEdge = null;

		needsInit = true;
	}

	/*
	 *
	 * public void testSect(JJPoint source, JJPoint sink, JJPoint barry1,
	 * JJPoint barry2) { JJPoint b1= barry1; JJPoint b2= barry2;
	 *
	 * if( Math.abs(getAngel(barry2 - sink)) > Math.abs(getAngel(barry1 -
	 * sink))) b1 = barry2;
	 *
	 * if( Math.abs(getAngel(barry1 - source)) < Math.abs(getAngel(barry2 -
	 * source))) b2 = barry1;
	 *
	 * // Debug.println("B1: " << b1 << " B2: " << b2);
	 *
	 *
	 * matrix A(2,2); vector rhs(2); A[0][0] = b1.getX() - sink.getX(); A[0][1]
	 * = -(b2.getX() - source.getX()); A[1][0] = b1.getY() - sink.getY();
	 * A[1][1] = -(b2.getY() - source.getY()); rhs[0] = source.getX() -
	 * sink.getX(); ` rhs[1] = source.getY() - sink.getY(); vector erg =
	 * A.solve(rhs);
	 *
	 * JJPoint intersect = source + (b2 - source) * erg[1];
	 *
	 * // Debug.println(intersect);
	 *
	 * JJPoint rect1 = (source+intersect)/2.0; JJPoint rect2 =
	 * (sink+intersect)/2.0;
	 *
	 * double width = (rect1.getX()+rect2.getX())/2.0; double height =
	 * dist(rect1, rect2);
	 *
	 * double transY = ((rect1+rect2)/2.0).getY(); // Debug.println("rect1 " +
	 * rect1 + " rect2 " + rect2 + " width : " + width + "height " + height +
	 * " transY " + transY); }
	 */

	public JJGTNode gtInfo(final JJNode v) {
		return nodeToGTNode.get(v);
	}

	public void showTree() {
		if (showTreeFlag == true) {
			showTreeFlag = false;
		} else {
			showTreeFlag = true;
		}
	}

	public void compress() {
		final LinkedList<JJNode> weg = new LinkedList<>();
		needsInit = true;

		for (final Iterator<JJNode> nodeIter = spqrTree.nodeIterator(); nodeIter.hasNext();) {

			final JJNode tmpN = nodeIter.next();
			switch (spqrInfo(tmpN).getSpqrType()) {
			case JJSpqrNode.S_NODE:
			case JJSpqrNode.P_NODE:
			case JJSpqrNode.Q_NODE:
			case JJSpqrNode.R_NODE: {
				final JJNode tmpN2 = spqrFather(tmpN);
				if (tmpN2 != null)
					spqrTree.addEdge(tmpN2, tmpN);
				break;
			}
			default: {
				weg.add(tmpN);
				break;
			}
			}
		}

		while (!weg.isEmpty())
			spqrTree.deleteNode(weg.removeFirst());
	}

	public void showRanges() {
		_showRanges();
		final JJDfs dfs = new JJDfs(getGraph());
		dfs.doDfs();

		for (final Iterator<JJNode> nodeIter = spqrTree.nodeIterator(); nodeIter.hasNext();) {

			final JJNode tmpN = nodeIter.next();
			tmpN.setName("" + dfs.getDfsNum(tmpN) + " " + dfs.getLowNum(tmpN) + " " + dfs.getCompNum(tmpN));
		}

		for (final Iterator<JJEdge> edgeIter = getGraph().edgeIterator(); edgeIter.hasNext();) {

			final JJEdge tmpE = edgeIter.next();

			if (dfs.getEdgeType(tmpE) == JJDfs.TI_TREE)
				tmpE.setName("t");
			else if (dfs.getEdgeType(tmpE) == JJDfs.TI_BACKWARD)
				tmpE.setName("b");
			else
				tmpE.setName("?");
		}
	}

	public void split(final JJEdge kante) {

		if (needsInit == true)
			if (!init())
				return;

		if (kante == theSTEdge) {
			fenster.message("Can't split s-t edge");
			return;
		}

		// Debug.println( "Split " + kante.getSource().getName() +
		// "-" + kante.getTarget().getName());
		final JJNode oldSource = kante.getSource();
		final JJNode oldTarget = kante.getTarget();
		final JJNode newNode = getGraph().addNode();

		newNode.setName("" + ++nodeCount);

		// JJGraphicNode cg1 = oldSource.getGraphicNode();
		// JJGraphicNode cg2 = oldTarget.getGraphicNode();
		// JJGraphicNode cg = newNode.getGraphicNode();

		// if((cg != null) && (cg1 != null) && (cg2 != null))
		// {
		// double x1 = cg1.getX();
		// double y1 = cg1.getY();
		// double x2 = cg2.getX();
		// double y2 = cg2.getY();
		// cg.moveTo(x1+ ((x2-x1)/2),y1+ ((y2-y1)/2));
		// }

		final JJEdge gte1 = getGraph().addEdge(oldSource, newNode);
		final JJEdge gte2 = getGraph().addEdge(newNode, oldTarget);

		final JJNode newQ = newQNode(newNode, oldTarget, gte2, null);

		final JJNode oldQ = edgeToGTEdge.get(kante).getQNode();

		setSink(oldQ, newNode);
		spqrInfo(oldQ).setRealEdge(gte1);

		oldQ.setName(oldSource.getName() + "/" + newNode.getName());

		switch (spqrInfo(spqrFather(oldQ)).getSpqrType()) {
		case JJSpqrNode.S_NODE: {
			final JJNode eFather = oldQ.father();
			setSink(eFather, newNode);

			final JJNode eFather2 = getTwin(eFather);
			setSink(eFather2, newNode);

			final JJNode newV1 = newVNodes(newNode, null);
			final JJNode newV2 = getTwin(newV1);

			final JJEdge newE1 = spqrTree.addEdge(eFather.inEdge(), newV1, JJGraph.after);
			final JJEdge newE2 = spqrTree.addEdge(eFather2.inEdge(), newV2, JJGraph.after);

			final JJNode e1 = newENodes(newNode, oldTarget, null);
			final JJNode e2 = getTwin(e1);

			spqrTree.addEdge(newE1, e1, JJGraph.after);
			spqrTree.addEdge(newE2, e2, JJGraph.after);

			spqrTree.addEdge(e1, newQ);
			break;
		}
		default: {
			final JJNode eFather = oldQ.father();
			// spqrTree.deleteEdge(spqrTree.first_in_edge(oldQ));
			spqrTree.deleteEdge(oldQ.firstInEdge()); // (JJEdge)oldQ.getInEdges().getFirst());
			final JJNode newD2 = newSTree(oldSource, oldTarget, eFather);

			final JJNode newE2 = newENodes(oldSource, newNode, newD2);
			spqrTree.addEdge(newE2, oldQ);

			newVNodes(newNode, newD2);

			final JJNode newE3 = newENodes(newNode, oldTarget, newD2);
			spqrTree.addEdge(newE3, newQ);
		}
		}

		edgeToGTEdge.put(gte1, new JJGTEdge(oldQ));
		edgeToGTEdge.put(gte2, new JJGTEdge(newQ));

		getGraph().deleteEdge(kante);
		edgeToGTEdge.remove(kante);
	}

	public boolean addPath(final LinkedList<JJEdge> liste) {

		final JJNode n1 = liste.getFirst().getSource();
		final JJNode n2 = liste.getLast().getTarget();

		if (testEdge(n1, n2) == false)
			return false;

		JJNode sq = null;

		if (liste.size() == 1) {
			final JJEdge tmpE = liste.removeFirst();
			final JJNode newQ = newQNode(n1, n2, tmpE, null);
			edgeToGTEdge.put(tmpE, new JJGTEdge(newQ));
			sq = newQ;
		} else {
			final JJNode newD = newSTree(n1, n2, null);

			while (liste.size() != 0) {
				final JJEdge tmpE = liste.removeFirst();
				final JJNode tmpN1 = tmpE.getSource(); // graph.source(tmpE);
				final JJNode tmpN2 = tmpE.getTarget(); // graph.target(tmpE);
				final JJNode newE = newENodes(tmpN1, tmpN2, newD);
				final JJNode newQ = newQNode(tmpN1, tmpN2, tmpE, newE);
				edgeToGTEdge.put(tmpE, new JJGTEdge(newQ));

				if (liste.size() != 0)
					newVNodes(tmpN2, newD);
			}
			sq = spqrFather(newD);
		}

		addSpqrObject(sq);
		return true;
	}

	public boolean addEdge(final JJNode n1, final JJNode n2) {
		if (needsInit == true)
			if (!init())
				return false;

		// Debug.println("Edge " + n1.getName() + " : " + n2.getName());

		if (testEdge(n1, n2) == false)
			return false;

		final JJEdge newE = getGraph().addEdge(n1, n2);
		final JJNode newQ = newQNode(n1, n2, newE, null);
		edgeToGTEdge.put(newE, new JJGTEdge(newQ));
		addSpqrObject(newQ);
		return true;
	}

	boolean init() {
		needsInit = false;
		clearAllStructs();

		if (getGraph().getNumNodes() < 3) {
			getGraph().clear();
			spqrTree.clear();
			initAfterClear();
			return true;
		}

		spqrTree.clear();
		if (showTreeFlag && (!spqrTree.getWindows().isEmpty()))
			spqrTree.createGraphic();

		return initOpenEar();
	}

	private void _showRanges() {
		for (final Iterator<JJNode> nodeIter = spqrTree.nodeIterator(); nodeIter.hasNext();) {
			final JJNode tmpN = nodeIter.next();

			final JJNode eins = getSource(tmpN);
			final JJNode zwei = getSink(tmpN);
			if ((eins.getGraph() == getGraph()) && (zwei.getGraph() == getGraph()))
				tmpN.setName(tmpN.getName() + ":" + eins.getName() + "-" + zwei.getName());
			else
				tmpN.setName(tmpN.getName() + " *");
		}
	}

	public JJSpqrNode spqrInfo(final JJNode v) {
		if (v == null) {
			Debug.println("spqrInfo of null node");
			(new Exception()).printStackTrace();
		}

		return nodeToSpqrNode.get(v);
	}

	public JJNode spqrFather(final JJNode knoten) {
		JJNode tmpN = knoten.father();
		while (true) {
			if (tmpN == null)
				return null;

			switch (spqrInfo(tmpN).getSpqrType()) {
			case JJSpqrNode.S_NODE:
			case JJSpqrNode.P_NODE:
			case JJSpqrNode.Q_NODE:
			case JJSpqrNode.R_NODE:
				return tmpN;
			default:
				tmpN = tmpN.father();
			}
		}
	}

	public void revert(final JJNode v) {
		// Debug.println("Revert(v)");

		spqrInfo(v).revert();

		final JJNode n1 = getSource(v);
		final JJNode n2 = getSink(v);

		v.setName(n1.getName() + "/" + n2.getName());
	}

	public JJNode newQNode(final JJNode n1, final JJNode n2, final JJEdge theEdge, final JJNode optFather) {
		final JJNode newQ = spqrTree.addNode();

		final JJSpqrNode qInfo = new JJSpqrNode(JJSpqrNode.Q_NODE, n1, n2);
		qInfo.setRealEdge(theEdge);
		nodeToSpqrNode.put(newQ, qInfo);

		newQ.setName(n1.getName() + "/" + n2.getName());

		if (optFather != null)
			spqrTree.addEdge(optFather, newQ);

		return newQ;
	}

	public JJNode newVNodes(final JJNode knoten, final JJNode optFather) {
		final JJNode newV1 = spqrTree.addNode();
		nodeToSpqrNode.put(newV1, new JJSpqrNode(JJSpqrNode.V_NODE, null, null));
		spqrInfo(newV1).setRealNode(knoten);

		newV1.setName("V" + knoten.getName());

		final JJNode newV2 = spqrTree.addNode();
		nodeToSpqrNode.put(newV2, new JJSpqrNode(JJSpqrNode.V_NODE, null, null));

		spqrInfo(newV2).setRealNode(knoten);
		newV2.setName("V" + knoten.getName());

		spqrInfo(newV1).setTwin(newV2);
		spqrInfo(newV2).setTwin(newV1);
		nodeToGTNode.put(knoten, new JJGTNode(newV1, newV2));

		if (optFather != null) {
			final JJNode optFather2 = getTwin(optFather);
			spqrTree.addEdge(optFather, newV1);
			spqrTree.addEdge(optFather2, newV2);
		}

		return newV1;
	}

	public void setSink(final JJNode v, final JJNode s) {
		// ffAssert(graph_of(v) == spqrTree);
		// ffAssert(graph_of(s) == derGraph);

		switch (spqrInfo(v).getSpqrType()) {
		case JJSpqrNode.S_NODE:
		case JJSpqrNode.P_NODE:
		case JJSpqrNode.Q_NODE:
		case JJSpqrNode.R_NODE:
		case JJSpqrNode.E_NODE: {
			break;
		}
		case JJSpqrNode.F_NODE: {
			if (!isBlocking(v))
				break;
		}
		default: {
			// ffAssert("SetSink not applicable");
		}
		}

		spqrInfo(v).setSink(s);
	}

	public JJNode getTwin(final JJNode knoten) {
		// ffAssert(graph_of(knoten) == spqrTree);
		final JJSpqrNode kInfo = spqrInfo(knoten);

		switch (kInfo.getSpqrType()) {
		case JJSpqrNode.E_NODE:
		case JJSpqrNode.V_NODE:
		case JJSpqrNode.D_NODE: {
			return kInfo.getTwin();
		}
		default: {
			// ffAssert("getTwin with wrong Spqr type" == NULL);
			break;
		}
		}
		return null;
	}

	public JJNode newENodes(final JJNode n1, final JJNode n2, final JJNode optFather) {
		// static int edgeCount= 0;

		final JJNode e1 = spqrTree.addNode();
		nodeToSpqrNode.put(e1, new JJSpqrNode(JJSpqrNode.E_NODE, n1, n2));

		e1.setName("E");

		final JJNode e2 = spqrTree.addNode();
		nodeToSpqrNode.put(e2, new JJSpqrNode(JJSpqrNode.E_NODE, n1, n2));
		e2.setName("E");

		spqrInfo(e1).setTwin(e2);
		spqrInfo(e2).setTwin(e1);

		if (optFather != null) {
			final JJNode optFather2 = getTwin(optFather);
			// ffAssert(optFather2 != null);
			spqrTree.addEdge(optFather, e1);
			spqrTree.addEdge(optFather2, e2);
		}

		return e1;
	}

	public JJNode newSTree(final JJNode n1, final JJNode n2, final JJNode optFather) {
		// static int stCount=0;
		final JJNode s = spqrTree.addNode();
		nodeToSpqrNode.put(s, new JJSpqrNode(JJSpqrNode.S_NODE, n1, n2));
		s.setName("S");

		final JJNode newF1 = newFNode(s);
		final JJNode newF2 = newFNode(s);

		final JJNode newD1 = newDNodes(newF1, newF2);
		final JJNode newD2 = newDNodes(newF1, newF2);

		final JJNode newE1 = newENodes(n1, n2, newD1);
		final JJNode newE2 = getTwin(newE1);

		spqrInfo(newE1).setStEdge(true);
		spqrInfo(newE2).setStEdge(true);
		spqrInfo(s).setStEdge(newE1);

		newE1.setName("e");
		newE2.setName("e");

		if (optFather != null)
			spqrTree.addEdge(optFather, s);

		return newD2;
	}

	public boolean testEdge(final JJNode n1, final JJNode n2) {
		if (n1 == n2) {
			fenster.message("No self-loops allowed");
			return false;
		}

		if (!test(n1, n2)) {
			fenster.message("Graph would not be planar with this edge!");
			return false;
		}

		return true;
	}

	public void addSpqrObject(final JJNode newSQ) {
		final JJNode n1 = getSource(newSQ);
		final JJNode n2 = getSink(newSQ);

		final JJNode propAllocN1 = propAllocNode(n1);
		final JJNode propAllocN2 = propAllocNode(n2);

		final JJNode my = spqrAdjust(leastCommonAncestor(propAllocN1, propAllocN2));

		if (propAllocN1 == propAllocN2) {
			// ffAssert(my == propAllocN1);
			// cerr << "Simple my is propAlloc 1 and 2" << endl;
			finalTransformation(my, newSQ);
		} else if (my == propAllocN1) {
			// cerr << "My is propAllocN1" << endl;
			final JJNode chi = findLowestOnPath(n1, propAllocN2, my);
			if (chi == propAllocN2) {
				// #ifdef DEBUG
				// cerr << " Lucky, only finalTransformation" << endl;
				// #endif
				finalTransformation(chi, newSQ);
			} else {
				// cerr << " Urgs, pathCondensation" << endl;
				final JJEdge l = pathCondensation(propAllocN2, chi, n2, newSQ);
				finalTransformation(chi, l, newSQ);
			}
		} else if (my == propAllocN2) {
			// cerr << "My is propAllocN2" << endl;
			final JJNode chi = findLowestOnPath(n2, propAllocN1, my);
			if (chi == propAllocN1) {
				// cerr << " Lucky, only finalTransformation" << endl;
				finalTransformation(chi, newSQ);
			} else {
				// cerr << " Urgs, pathCondensation" << endl;
				final JJEdge l = pathCondensation(propAllocN1, chi, n1, newSQ);
				finalTransformation(chi, l, newSQ);
			}
		} else // propAllocN1 != my and propAllocN2 != my
		{
			// cerr << "My is anything but propAlloc" << endl;
			final JJEdge l1 = pathCondensation(propAllocN1, my, n1, newSQ);
			final JJEdge l2 = pathCondensation(propAllocN2, my, n2, newSQ);
			finalTransformation(my, l1, l2, newSQ);
		}
	}

	void clearAllStructs() {
		nodeToSpqrNode.clear();
		nodeToGTNode.clear();
		edgeToGTEdge.clear();

		aux.clear();
	}

	void initAfterClear() {
		final JJNode s = getGraph().addNode();
		final JJNode t = getGraph().addNode();
		final JJNode dummy = getGraph().addNode();

		// JJGraphicNode cgS = s.getGraphicNode(); // graph.gt(s).graphics();
		// JJGraphicNode cgT = t.getGraphicNode(); // graph.gt(t).graphics();
		// JJGraphicNode cgD = dummy.getGraphicNode();
		// //graph.gt(dummy).graphics();

		// if((cgS != null) && (cgT != null) && (cgD != null))
		// {
		// cgS.moveTo(20,220);
		// cgT.moveTo(20,20);
		// cgD.moveTo(350,110);
		// }

		s.setName("S");
		t.setName("T");
		dummy.setName("1");

		final JJEdge gte1 = getGraph().addEdge(s, t);
		final JJEdge gte2 = getGraph().addEdge(s, dummy);
		final JJEdge gte3 = getGraph().addEdge(dummy, t);

		spqrRoot = newPNode(s, t, null);

		// F1
		// F2

		final JJNode f1 = newFNode(spqrRoot);
		final JJNode f2 = newFNode(spqrRoot);

		final JJNode d1 = newDNodes(f1, f2);
		final JJNode d2 = newDNodes(f1, f2);

		final JJNode e1 = newENodes(s, t, d1);
		final JJNode e2 = newENodes(s, t, d2);
		final JJNode qst = newQNode(s, t, gte1, e1);

		// F4
		// F5

		final JJNode d8 = newSTree(s, t, e2);
		final JJNode e8 = newENodes(s, dummy, d8);
		newVNodes(dummy, d8);
		final JJNode e9 = newENodes(dummy, t, d8);

		final JJNode qs1 = newQNode(s, dummy, gte2, e8);
		final JJNode q1t = newQNode(dummy, t, gte3, e9);

		// Init GT Structures

		nodeToGTNode.put(s, new JJGTNode(null, null));
		nodeToGTNode.put(t, new JJGTNode(null, null));

		edgeToGTEdge.put(gte1, new JJGTEdge(qst));
		edgeToGTEdge.put(gte2, new JJGTEdge(qs1));
		edgeToGTEdge.put(gte3, new JJGTEdge(q1t));

		nodeCount = 1;
		theSNode = s;
		theTNode = t;
		theSTEdge = gte1;
	}

	JJNode getSource(final JJNode v) {
		final JJSpqrNode vInfo = spqrInfo(v);
		switch (vInfo.getSpqrType()) {
		case JJSpqrNode.S_NODE:
		case JJSpqrNode.P_NODE:
		case JJSpqrNode.Q_NODE:
		case JJSpqrNode.R_NODE:
		case JJSpqrNode.E_NODE: {
			return vInfo.getSource();
		}
		case JJSpqrNode.F_NODE: {
			if (vInfo.getSource() != null)
				return vInfo.getSource();
			// Fall through
		}
		case JJSpqrNode.D_NODE: {
			return getSource(v.firstSon());
		}
		case JJSpqrNode.V_NODE: {
			return vInfo.getRealNode();
		}
		default: {
			// ffAssert("Get Source not applicable" != NULL);
		}
		}
		return null;
	}

	JJNode getSink(final JJNode v) {
		final JJSpqrNode vInfo = spqrInfo(v);
		switch (vInfo.getSpqrType()) {
		case JJSpqrNode.S_NODE:
		case JJSpqrNode.P_NODE:
		case JJSpqrNode.Q_NODE:
		case JJSpqrNode.R_NODE:
		case JJSpqrNode.E_NODE: {
			return vInfo.getSink();
		}
		case JJSpqrNode.F_NODE: {
			if (vInfo.getSink() != null)
				return vInfo.getSink();
			// Fall through
		}
		case JJSpqrNode.D_NODE: {
			return getSink(v.lastSon());
		}
		case JJSpqrNode.V_NODE: {
			return vInfo.getRealNode();
		}
		default: {
			// ffAssert("Get Sink not applicable" != NULL);
		}
		}
		return null;
	}

	boolean isBlocking(final JJNode v) {
		if (spqrInfo(v).getSpqrType() == JJSpqrNode.F_NODE)
			return spqrInfo(v).isBlocking();
		// ffAssert(father(v) != null);
		return isBlocking(v.father());
	}

	JJNode newFNode(final JJNode optFather) {
		final JJNode f1 = spqrTree.addNode();
		nodeToSpqrNode.put(f1, new JJSpqrNode(JJSpqrNode.F_NODE, null, null));
		f1.setName("F");

		if (optFather != null)
			spqrTree.addEdge(optFather, f1);

		return f1;
	}

	JJNode newFNode(final JJNode n1, final JJNode n2, final JJNode optFather) {
		final JJNode f1 = spqrTree.addNode();
		nodeToSpqrNode.put(f1, new JJSpqrNode(JJSpqrNode.F_NODE, n1, n2));
		f1.setName("F");

		if (optFather != null)
			spqrTree.addEdge(optFather, f1);

		return f1;
	}

	JJNode newDNodes(final JJNode optFather, final JJNode optFather2) {
		// static int dCount=0;

		final JJNode d1 = spqrTree.addNode();
		nodeToSpqrNode.put(d1, new JJSpqrNode(JJSpqrNode.D_NODE, null, null));

		// d1.setName("D" + ++dCount);

		final JJNode d2 = spqrTree.addNode();
		nodeToSpqrNode.put(d2, new JJSpqrNode(JJSpqrNode.D_NODE, null, null));

		// d2.setName("D" + dCount);

		spqrInfo(d1).setTwin(d2);
		spqrInfo(d2).setTwin(d1);

		if (optFather != null) {
			// ffAssert(optFather2 != null);
			spqrTree.addEdge(optFather, d1);
			spqrTree.addEdge(optFather2, d2);
		}

		return d1;
	}

	boolean test(final JJNode v1, final JJNode v2) {
		if (aux.defined(new JJNodePair(v1, v2)))
			return true;

		if (aux.defined(new JJNodePair(v2, v1)))
			return true;

		final JJNode my1 = propAllocNode(v1);
		final JJNode my2 = propAllocNode(v2);

		final JJNode my = leastCommonAncestor(my1, my2);

		if ((my == my1) && (my == my2)) {
			return testCaseA(v1, v2, my);
		} else if (my == my1) {
			return testCaseC(v1, v2, my2, my);
		} else if (my == my2) {
			return testCaseC(v2, v1, my1, my);
		} else {
			return testCaseB(v1, v2, my1, my2, my);
		}

	}

	JJNode propAllocNode(final JJNode v) {
		// ffAssert(spqrRoot != null);

		final JJNode v1 = gtInfo(v).getV1();
		if (v1 == null) // v is s or t
			return spqrRoot;

		return spqrAdjust(v1);
	}

	boolean initOpenEar() {
		final HashMap<JJNode, Integer> nodeToSt = new HashMap<>();

		// Debug.println("Computing an st numeration");

		JJSTNum.compSTNum(getGraph(), nodeToSt, null);

		// Debug.println("Done");

		for (final Iterator<JJNode> nodeIter = getGraph().nodeIterator(); nodeIter.hasNext();) {

			final JJNode tmpN = nodeIter.next();
			final int i = nodeToSt.get(tmpN).intValue();

			tmpN.setName("" + i);
			// graph.gt(tmpN).label(string("%d", nodeToSt[tmpN]));
		}

		// verifySTNum(*graph, nodeToSt);

		final JJNode stToNode[] = new JJNode[getGraph().getNumNodes()];
		final boolean done[] = new boolean[getGraph().getNumNodes()];

		int maxST = 0;

		for (final Iterator<JJNode> nodeIter = getGraph().nodeIterator(); nodeIter.hasNext();) {

			final JJNode tmpN = nodeIter.next();
			final int stNum = nodeToSt.get(tmpN).intValue();
			maxST = Math.max(maxST, stNum);

			stToNode[stNum] = tmpN;
			done[stNum] = false;
			if (stNum == 0)
				theSNode = tmpN;
			else if (stNum == maxST)
				theTNode = tmpN;
		}

		final HashSet<JJEdge> alleKanten = new HashSet<>();

		for (final Iterator<JJEdge> edgeIter = getGraph().edgeIterator(); edgeIter.hasNext();) {

			final JJEdge tmpE = edgeIter.next();
			alleKanten.add(tmpE);
		}

		for (final Iterator<JJEdge> iter = theSNode.outIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();

			if (tmpE.getTarget() == theTNode) {
				theSTEdge = tmpE;
				alleKanten.remove(tmpE);
				break;
			}
		}

		// ffAssert(theSNode);
		// ffAssert(theTNode);
		// ffAssert(theSTEdge);

		spqrRoot = newPNode(theSNode, theTNode, null);
		final JJNode f1 = newFNode(spqrRoot);
		final JJNode d1 = newDNodes(f1, f1);
		final JJNode e1 = newENodes(theSNode, theTNode, d1);
		final JJNode qst = newQNode(theSNode, theTNode, theSTEdge, e1);
		nodeCount = 1;
		int is = nodeToSt.get(theSNode).intValue();
		done[is] = true;
		is = nodeToSt.get(theTNode).intValue();
		done[is] = true;

		int lowNotDone = 1;

		nodeToGTNode.put(theSNode, new JJGTNode(null, null));
		nodeToGTNode.put(theTNode, new JJGTNode(null, null));
		edgeToGTEdge.put(theSTEdge, new JJGTEdge(qst));

		while (true) {
			while ((done[lowNotDone] == true) && (lowNotDone < maxST))
				lowNotDone++;
			if (lowNotDone >= maxST)
				break;

			final JJNode aktuN = stToNode[lowNotDone];
			final JJEdge fromFather = aktuN.firstInEdge(); // (JJEdge)aktuN.getInEdges().getFirst();
			// JJEdge fromFather = graph.first_in_edge(aktuN);

			final LinkedList<JJEdge> pfad = new LinkedList<>();

			pfad.addLast(fromFather);
			alleKanten.remove(fromFather);
			_markPath(done, alleKanten, aktuN, pfad, nodeToSt);

			if (!addPath(pfad)) {
				fenster.message("Graph is not planar");
				needsInit = true;
				_markPath(done, alleKanten, aktuN, pfad, nodeToSt);
				fenster.showPath(pfad);
				return false;
			}
		}

		while (!alleKanten.isEmpty()) {
			final JJEdge tmpE = alleKanten.iterator().next();
			final LinkedList<JJEdge> pfad = new LinkedList<>();
			pfad.addLast(tmpE);
			alleKanten.remove(tmpE);
			if (!addPath(pfad)) {
				pfad.addLast(tmpE);
				fenster.showPath(pfad);
				fenster.message("Graph is not planar");
				needsInit = true;
				return false;
			}
		}

		return true;
	}

	JJNode leastCommonAncestor(final JJNode n1, final JJNode n2) {
		// ffAssert(n1);
		// ffAssert(n2);

		final Stack<JJNode> pathN1 = new Stack<>();
		final Stack<JJNode> pathN2 = new Stack<>();

		JJNode tmpN = n1;
		while (tmpN != null) {
			pathN1.push(tmpN);
			tmpN = spqrFather(tmpN);
		}

		tmpN = n2;
		while (tmpN != null) {
			pathN2.push(tmpN);
			tmpN = spqrFather(tmpN);
		}

		while (true) {
			final JJNode tmpN1 = pathN1.peek();
			final JJNode tmpN2 = pathN2.peek();

			if (tmpN1 != tmpN2) {
				break;
			}
			tmpN = tmpN1;
			pathN1.pop();
			pathN2.pop();
			if (pathN1.isEmpty())
				break;
			if (pathN2.isEmpty())
				break;
		}

		// ffAssert(tmpN != null);
		return tmpN;
	}

	JJNode spqrAdjust(final JJNode knoten) {
		// ffAssert(knoten);
		switch (spqrInfo(knoten).getSpqrType()) {
		case JJSpqrNode.S_NODE:
		case JJSpqrNode.P_NODE:
		case JJSpqrNode.Q_NODE:
		case JJSpqrNode.R_NODE: {
			return knoten;
		}
		default:
			return spqrFather(knoten);
		}
	}

	void finalTransformation(final JJNode chi, final JJNode newQ) {
		// Debug.println( "Final transformation");

		final JJSpqrNode chiInfo = spqrInfo(chi);
		// JJSpqrNode *qInfo= spqrInfo(newQ);
		switch (chiInfo.getSpqrType()) {
		case JJSpqrNode.S_NODE: {
			finalTransformationS(chi, newQ);
			break;
		}
		case JJSpqrNode.R_NODE: {
			finalTransformationR(chi, newQ);
			break;
		}
		case JJSpqrNode.P_NODE: {
			finalTransformationP(chi, newQ);
			break;
		}
		default: {
			// Debug.println( "Final Transformation(chi) with bad SPQR_TYPE!");
		}
		}
	}

	// Returns the lowest JJNode on the path from start to
	// goal that contains target as pole. If none does,
	// goal is returned.

	JJNode findLowestOnPath(final JJNode target, final JJNode start, final JJNode goal) {
		// ffAssert(graph_of(start)==spqrTree);
		// ffAssert(graph_of(goal)==spqrTree);

		JJNode tmpN = spqrAdjust(start);
		JJSpqrNode spqrN = spqrInfo(tmpN);

		while ((spqrN.getSink() != target) && (spqrN.getSource() != target)) {
			tmpN = spqrFather(tmpN);
			// ffAssert(tmpN);

			if (tmpN == goal)
				return goal;

			spqrN = spqrInfo(tmpN);
		}

		return tmpN;
	}

	JJEdge pathCondensation(final JJNode my1, final JJNode chi, final JJNode v, final JJNode newQ) {
		initialTransformation(my1, v);
		JJNode p = my1;

		while (spqrFather(p) != chi) {
			p = elementaryTransformation(p, spqrFather(p), newQ);
			if (p == null)
				return null;

		}

		// return spqrTree.first_in_edge(p);
		return p.firstInEdge(); // (JJEdge) p.getInEdges().getFirst();
	}

	void finalTransformation(final JJNode chi, final JJEdge lambda1, final JJEdge lambda2, final JJNode newQ) {
		// ffAssert(chi);

		switch (spqrInfo(chi).getSpqrType()) {
		case JJSpqrNode.P_NODE: {
			finalTransformationP(chi, lambda1, lambda2, newQ);
			break;
		}
		case JJSpqrNode.S_NODE: {
			finalTransformationS(chi, lambda1, lambda2, newQ);
			break;
		}
		case JJSpqrNode.R_NODE: {
			finalTransformationR(chi, lambda1, lambda2, newQ);
			break;
		}
		default:
			break;
		}
	}

	void finalTransformation(final JJNode chi, final JJEdge lambda, final JJNode newQ) {
		// ffAssert(lambda);
		// ffAssert(chi);
		switch (spqrInfo(chi).getSpqrType()) {
		case JJSpqrNode.S_NODE: {
			finalTransformationS(chi, lambda, newQ);
			break;
		}
		case JJSpqrNode.R_NODE: {
			finalTransformationR(chi, lambda, newQ);
			break;
		}
		default:
			break;
		}
	}

	JJNode newPNode(final JJNode n1, final JJNode n2, final JJNode optFather) {
		final JJNode pNode = spqrTree.addNode();
		nodeToSpqrNode.put(pNode, new JJSpqrNode(JJSpqrNode.P_NODE, n1, n2));

		pNode.setName("P");

		if (optFather != null)
			spqrTree.addEdge(optFather, pNode);

		return pNode;
	}

	JJNode newSNode(final JJNode n1, final JJNode n2, final JJNode optFather) {
		final JJNode s = spqrTree.addNode();
		nodeToSpqrNode.put(s, new JJSpqrNode(JJSpqrNode.S_NODE, n1, n2));

		s.setName("S");

		if (optFather != null)
			spqrTree.addEdge(optFather, s);

		return s;
	}

	boolean testCaseA(final JJNode v1, final JJNode v2, final JJNode my) {
		// Debug.println( "Testing case A" );

		if (spqrInfo(my).getSpqrType() != JJSpqrNode.R_NODE)
			return true;

		// Debug.println( "Looking for face with both nodes" );

		if (findFaceWith(my, v1, v2) != null)
			return true;

		return false;
	}

	boolean testCaseC(final JJNode v1, final JJNode v2, final JJNode my2, final JJNode my) {
		// Debug.println( "Testing case C" );
		// Debug.println( "Testing whether v2 is blocked" );

		JJNode chi = null;

		// Debug.println( "Testing whether path is blocked" );

		if (isBlocking(gtInfo(v2).getV1()) && isBlocking(gtInfo(v2).getV2()))
			chi = my2;
		else {
			chi = firstBlockingUpTo(my, my2);
			if (chi != my)
				chi = spqrFather(chi);
		}

		final JJNode chiSink = getSink(chi);
		final JJNode chiSource = getSource(chi);

		if ((chiSink != v1) && (chiSource != v1) && (chi != my))
			return false;

		// Debug.println( "Testing for face with both candidates" );

		JJNode eNode2 = null;
		if (chi == my2)
			eNode2 = gtInfo(v2).getV1();
		else
			eNode2 = findRepInSceleton(chi, my2);

		// Debug.println( "Enode is " + gtSpqrTree.gt(eNode2).label() );

		final JJNode face1 = faceOf(eNode2);
		final JJNode face2 = faceOf(getTwin(eNode2));
		if (faceContainsGt(face1, v1) || faceContainsGt(face2, v1))
			return true;

		return false;
	}

	boolean testCaseB(final JJNode v1, final JJNode v2, final JJNode my1, final JJNode my2, final JJNode my) {
		// Debug.println( "Testing case B" );

		final JJNode eNode1 = findRepInSceleton(my, my1);
		final JJNode eNode2 = findRepInSceleton(my, my2);

		// Debug.println( "Testing whether v1 is blocked" );

		if (isBlocking(gtInfo(v1).getV1()) && isBlocking(gtInfo(v1).getV2()))
			return false;

		// Debug.println( "Testing whether v2 is blocked" );

		if (isBlocking(gtInfo(v2).getV1()) && isBlocking(gtInfo(v2).getV2()))
			return false;

		// Debug.println( "Testing whether pathes are blocked" );

		if ((nonBlockingUpTo(eNode1.firstSon(), my1) && nonBlockingUpTo(eNode2.firstSon(), my2)) == false)
			return false;

		// Debug.println( "Testing for face with both nodes" );

		if (spqrInfo(my).getSpqrType() == JJSpqrNode.R_NODE) {
			if ((faceOf(eNode1) == faceOf(eNode2)) || (faceOf(getTwin(eNode1)) == faceOf(eNode2))
					|| (faceOf(eNode1) == faceOf(getTwin(eNode2)))
					|| (faceOf(getTwin(eNode1)) == faceOf(getTwin(eNode2))))
				return true;
			else
				return false;
		}

		return true;
	}

	JJNode faceOf(final JJNode v) {
		if (v == null)
			return null;

		if (spqrInfo(v).getSpqrType() == JJSpqrNode.F_NODE)
			return v;

		return faceOf(v.father());
	}

	JJNode _markPath(final boolean done[], final HashSet<JJEdge> allE, final JJNode startN,
			final LinkedList<JJEdge> pfad, final HashMap<JJNode, Integer> nodeToSt) {
		JJNode aktuN = startN;

		// Debug.println( "Marking Path" );

		while (done[nodeToSt.get(aktuN).intValue()] != true) {
			done[nodeToSt.get(aktuN).intValue()] = true;
			final JJEdge tmpE = aktuN.firstOutEdge(); // (JJEdge)
														// aktuN.getOutEdges().getFirst();
														// //
														// graph.first_adj_edge(aktuN);
			pfad.addLast(tmpE);
			allE.remove(tmpE);
			aktuN = tmpE.getTarget(); // graph.target(tmpE);
		}
		return aktuN;
	}

	void finalTransformationS(final JJNode chi, final JJNode newQ) {
		// Debug.println( "Final transformation S" );

		final HashSet<JJNode> tmpS = new HashSet<>(); // set<node> tmpS;
		tmpS.add(chi);

		if (singleStepFlag)
			fenster.confirmStep("Final Transformation S", tmpS);

		final JJSpqrNode chiInfo = spqrInfo(chi);
		final JJSpqrNode qInfo = spqrInfo(newQ);
		boolean needS = true;
		boolean needP = true;
		boolean doRevert = false;

		JJNode sink = qInfo.getSink();
		JJNode source = qInfo.getSource();

		JJNode sinkV1 = gtInfo(sink).getV1();
		JJNode sourceV1 = gtInfo(source).getV1();

		if (chiInfo.getSource() == sink) {
			doRevert = true;
		} else if (chiInfo.getSink() == source) {
			doRevert = true;
		} else if (chiInfo.getSource() == source) {
			doRevert = false;
		} else if (chiInfo.getSink() == sink) {
			doRevert = false;
		} else if (sinkV1 == JJMisc.firstSuccessor(sinkV1.father(), sinkV1, sourceV1)) {
			doRevert = true;
		}

		if (doRevert == true) {
			revert(newQ);
			sink = qInfo.getSink();
			source = qInfo.getSource();
			sinkV1 = gtInfo(sink).getV1();
			sourceV1 = gtInfo(source).getV1();
		}

		final JJNode sinkV2 = gtInfo(sink).getV2();
		final JJNode sourceV2 = gtInfo(source).getV2();

		// Trying to determine which pathes we must split
		// and where to start

		JJEdge startPoint1 = null;
		JJEdge startPoint2 = null;
		JJEdge endPoint1 = null;
		JJEdge endPoint2 = null;
		JJNode path1 = null;
		JJNode path2 = null;

		if ((sinkV1 != null) && (spqrAdjust(sinkV1) == chi)) {
			path1 = sinkV1.father();
			path2 = sinkV2.father();
			endPoint1 = sinkV1.inEdge().adjPred();
			endPoint2 = sinkV2.inEdge().adjPred();
		}

		if ((sourceV1 != null) && (spqrAdjust(sourceV1) == chi)) {
			path1 = sourceV1.father();
			path2 = sourceV2.father();
			startPoint1 = sourceV1.inEdge().adjSucc();
			startPoint2 = sourceV2.inEdge().adjSucc();
		}

		if (path1 == null || path2 == null) {
			throw new RuntimeException("Path1 or Path2 is null. I didn't expect this");
		}
		// ffAssert(path1);
		// ffAssert(path2);

		if (startPoint1 == null) {
			startPoint1 = path1.firstOutEdge(); // (JJEdge)
												// path1.getOutEdges().getFirst();
												// //
												// spqrTree.first_adj_edge(path1);
			startPoint2 = path2.firstOutEdge(); // (JJEdge)
												// path2.getOutEdges().getFirst();
												// //
												// spqrTree.first_adj_edge(path2);
		}
		if (endPoint1 == null) {
			endPoint1 = path1.lastOutEdge(); // (JJEdge)
												// path1.getOutEdges().getLast();
												// //
												// spqrTree.last_adj_edge(path1);
			endPoint2 = path2.lastOutEdge(); // (JJEdge)
												// path2.getOutEdges().getLast();
												// //
												// spqrTree.last_adj_edge(path2);
		}

		if (startPoint1 == endPoint1) {
			// Debug.println( "We don't need an S-Node" );

			needS = false;
			final JJNode tmpE = startPoint1.getTarget();
			// JJNode tmpE= target(startPoint1); ?????
			// ffAssert(spqrTree.first_adj_edge(tmpE) != null);
			final JJNode tmpN = tmpE.firstOutEdge().getTarget();
			// JJNode tmpN=spqrTree.target(spqrTree.first_adj_edge(tmpE));

			if (spqrInfo(tmpN).getSpqrType() == JJSpqrNode.P_NODE) {
				// Debug.println( "We also don't need an P-Node" );
				needP = false;
			}
		}

		// Lets build the new sceleton

		if (needP) {
			final JJNode newE1 = newENodes(source, sink, null);
			final JJNode newE2 = getTwin(newE1);

			final JJNode newP = newPNode(source, sink, newE1);
			final JJNode f1 = newFNode(newP);
			final JJNode f2 = newFNode(newP);
			final JJNode d1 = newDNodes(f1, f2);
			JJNode newPath1 = newDNodes(f1, f2);
			JJNode newPath2 = getTwin(newPath1);
			final JJNode e1 = newENodes(source, sink, d1);
			spqrTree.addEdge(e1, newQ);

			if (needS) {
				final JJNode e2 = newENodes(source, sink, newPath1);
				newPath1 = newSTree(source, sink, e2);
				newPath2 = getTwin(newPath1);
			}

			// Moving

			spqrTree.addEdge(startPoint1, newE1, JJGraph.before);
			spqrTree.addEdge(startPoint2, newE2, JJGraph.before);

			JJMisc.moveEdges(startPoint1, endPoint1, newPath1);
			JJMisc.moveEdges(startPoint2, endPoint2, newPath2);
		} else // We don't need a P-Node
		{
			final JJNode tmpN = startPoint1.getTarget(); // spqrTree.target(startPoint1);
			// ffAssert(spqrTree.first_adj_edge(tmpN) != null);
			// JJNode oldPNode= spqrTree.target(spqrTree.first_adj_edge(tmpN));
			final JJNode oldPNode = tmpN.firstOutEdge().getTarget();
			final JJNode fOld = oldPNode.firstSon();
			final JJNode dOld = fOld.firstSon();
			spqrTree.deleteEdge(dOld.inEdge());

			final JJNode f1 = newFNode(oldPNode);
			spqrTree.addEdge(f1, dOld);

			final JJNode d1 = newDNodes(f1, fOld);
			final JJNode e1 = newENodes(source, sink, d1);
			spqrTree.addEdge(e1, newQ);
		}
	}

	void finalTransformationR(final JJNode chi, final JJNode newQ) {

		// Debug.println( "finalTransformation R" );

		final HashSet<JJNode> tmpS = new HashSet<>(); // set<node> tmpS;
		tmpS.add(chi);

		if (singleStepFlag)
			fenster.confirmStep("Final Transformation R", tmpS);

		// JJSpqrNode *qInfo= spqrInfo(newQ);
		final JJNode parallelEdge = findEdgeWithAndRevert(chi, newQ);

		if (parallelEdge != null) {
			finalTransformationRParallel(newQ, parallelEdge);
			return;
		}
		addEdgeToRNode(chi, newQ, null);
	}

	void finalTransformationP(final JJNode chi, final JJNode newQ) {

		if (getSource(newQ) != getSource(chi))
			revert(newQ);

		final JJNode splitFace = chi.firstSon();
		final JJNode swapPath = splitFace.firstSon();

		final JJNode newF = newFNode(chi);
		final JJNode newD1 = newDNodes(newF, newF);
		final JJNode newE = newENodes(getSource(newQ), getSink(newQ), newD1);
		spqrTree.addEdge(newE, newQ);
		spqrTree.deleteEdge(newD1.inEdge());
		spqrTree.deleteEdge(swapPath.inEdge());
		spqrTree.addEdge(splitFace, newD1);
		spqrTree.addEdge(newF, swapPath);
	}

	void finalTransformationP(final JJNode chi, final JJEdge lambda1, final JJEdge lambda2, final JJNode newQ) {
		// Debug.println( "Final transformation R R P" );

		final HashSet<JJNode> tmpS = new HashSet<>(); // set<node> tmpS;
		tmpS.add(chi);
		tmpS.add(lambda1.getTarget());
		tmpS.add(lambda2.getTarget());

		if (singleStepFlag)
			fenster.confirmStep("Final Transformation R R P", tmpS);

		JJNode eNode1 = lambda1.getSource(); // spqrTree.source(lambda1);
		JJNode eNode2 = lambda2.getSource(); // spqrTree.source(lambda2);
		final JJNode rNode1 = lambda1.getTarget(); // spqrTree.target(lambda1);
		final JJNode rNode2 = lambda2.getTarget(); // spqrTree.target(lambda2);

		// Do we have to move them to a common face?

		if (faceOf(eNode1) != faceOf(eNode2)) {
			if (faceOf(eNode1) == faceOf(getTwin(eNode2))) {
				eNode2 = getTwin(eNode2);
				spqrTree.deleteEdge(lambda2);
				spqrTree.addEdge(eNode2, rNode2);
			} else if (faceOf(eNode2) == faceOf(getTwin(eNode1))) {
				eNode1 = getTwin(eNode1);
				spqrTree.deleteEdge(lambda1);
				spqrTree.addEdge(eNode1, rNode1);
			} else if (faceOf(getTwin(eNode2)) == faceOf(getTwin(eNode1))) {
				eNode2 = getTwin(eNode2);
				spqrTree.deleteEdge(lambda2);
				spqrTree.addEdge(eNode2, rNode2);
				eNode1 = getTwin(eNode1);
				spqrTree.deleteEdge(lambda1);
				spqrTree.addEdge(eNode1, rNode1);
			} else // We have to swap
			{
				final JJNode path1 = eNode1.father();
				final JJNode face1 = path1.father();
				final JJNode swapPath = eNode2.uncle();
				final JJNode swapFace = swapPath.father();
				spqrTree.deleteEdge(path1.inEdge());
				spqrTree.deleteEdge(swapPath.inEdge());
				spqrTree.addEdge(swapFace, path1);
				spqrTree.addEdge(face1, swapPath);
			}
		}

		// ffAssert(faceOf(eNode1) == faceOf(eNode2));

		// Ok now they are on the same face
		// We now look for the pathes containing qSink and qSource

		final JJNode qSource = getSource(newQ);
		final JJNode qSink = getSink(newQ);

		JJNode stEdge1 = spqrInfo(rNode1).getStEdge();

		JJNode path1 = stEdge1.uncle();
		if (!(pathContainsGt(path1, qSource) || pathContainsGt(path1, qSink))) {
			stEdge1 = getTwin(stEdge1);
			path1 = stEdge1.uncle();
		}

		JJNode stEdge2 = spqrInfo(rNode2).getStEdge();

		JJNode path2 = stEdge2.uncle();
		if (!(pathContainsGt(path2, qSource) || pathContainsGt(path2, qSink))) {
			stEdge2 = getTwin(stEdge2);
			path2 = stEdge2.uncle();
		}

		final JJNode face1 = path1.father();
		final JJNode face2 = path2.father();

		// We now merge the 2 R nodes

		final JJNode tmpPath = getTwin(stEdge2).father();

		delSpqrNode(getTwin(stEdge2));
		delSpqrNode(stEdge2);
		spqrTree.deleteEdge(stEdge1.inEdge());
		spqrTree.addEdge(tmpPath, stEdge1);
		delSpqrNode(path1.brother());

		spqrTree.deleteEdge(path2.inEdge());
		spqrTree.addEdge(face1, path2);
		delSubTree(face2);

		JJMisc.moveEdges(rNode2.firstSon(), rNode2.lastSon(), rNode1);

		// We have to remove on face from the P Father

		final JJNode eNodeR2 = rNode2.father();
		final JJNode eNodeR2Twin = getTwin(eNodeR2);

		final JJNode pPathR2 = eNodeR2.father();
		final JJNode pPathr2Twin = eNodeR2Twin.father();

		final JJNode brotherENodeR2 = pPathR2.brother().firstSon();

		delSpqrNode(eNodeR2Twin);
		spqrTree.deleteEdge(brotherENodeR2.inEdge());
		spqrTree.addEdge(pPathr2Twin, brotherENodeR2);
		delSubTree(faceOf(eNodeR2));

		// Now we add newQ to the merged R Node

		addEdgeToRNode(rNode1, newQ, face1);

		// If necessary we compress the P node

		final JJNode chiFather = chi.father();
		if (chiFather != null) {
			spqrTree.deleteEdge(chi.inEdge());
			spqrTree.addEdge(chiFather, condenseNode(chi));
		}
	}

	void initialTransformation(final JJNode my, final JJNode v) {
		// Debug.println( "initialTransformation" );

		final HashSet<JJNode> tmpS = new HashSet<>(); // set<node> tmpS;
		tmpS.add(my);

		if (singleStepFlag)
			fenster.confirmStep("Initial Transformation", tmpS);

		final JJSpqrNode spqrInfoMy = spqrInfo(my);

		if (spqrInfoMy.getSpqrType() != JJSpqrNode.S_NODE) {
			// ffAssert(spqrInfoMy.getSpqrType() == JJSpqrNode.R_NODE);
			return;
		}

		spqrInfoMy.setSpqrType(JJSpqrNode.R_NODE);

		my.setName("R");

		final JJNode v1 = gtInfo(v).getV1();
		final JJNode v2 = gtInfo(v).getV2();
		final JJNode pfad1 = v1.father();
		final JJNode pfad2 = v2.father();

		if (pfad1.firstSon() != v1.leftBrother()) {
			final JJNode newE1 = newENodes(spqrInfoMy.getSource(), v, null);
			final JJNode newE2 = getTwin(newE1);

			final JJNode d2 = newSTree(spqrInfoMy.getSource(), v, newE1);
			final JJNode d4 = getTwin(d2);
			final JJEdge startPoint1 = pfad1.firstOutEdge();
			final JJEdge endPoint1 = v1.leftBrother().inEdge();
			final JJEdge startPoint2 = pfad2.firstOutEdge();
			final JJEdge endPoint2 = v2.leftBrother().inEdge();
			spqrTree.addEdge(startPoint1, newE1, JJGraph.before);
			spqrTree.addEdge(startPoint2, newE2, JJGraph.before);
			JJMisc.moveEdges(startPoint1, endPoint1, d2);
			JJMisc.moveEdges(startPoint2, endPoint2, d4);
		}

		if (pfad1.lastSon() != v1.rightBrother()) {
			final JJNode newE1 = newENodes(v, getSink(my), null);
			final JJNode newE2 = getTwin(newE1);

			final JJNode d2 = newSTree(v, getSink(my), newE1);
			final JJNode d4 = getTwin(d2);

			final JJEdge startPoint1 = v1.rightBrother().inEdge();
			final JJEdge endPoint1 = pfad1.lastOutEdge();
			final JJEdge startPoint2 = v2.rightBrother().inEdge();
			final JJEdge endPoint2 = pfad2.lastOutEdge();
			spqrTree.addEdge(startPoint1, newE1, JJGraph.before);
			spqrTree.addEdge(startPoint2, newE2, JJGraph.before);
			JJMisc.moveEdges(startPoint1, endPoint1, d2);
			JJMisc.moveEdges(startPoint2, endPoint2, d4);
		}
	}

	JJNode condenseNode(final JJNode knoten) {
		// ffAssert(knoten);
		switch (spqrInfo(knoten).getSpqrType()) {
		case JJSpqrNode.S_NODE: {
			return condenseSNode(knoten);
		}
		case JJSpqrNode.P_NODE: {
			return condensePNode(knoten);
		}
		default:
			break;
		}
		return knoten;
	}

	JJNode condenseSNode(final JJNode knoten) {
		// ffAssert(knoten);

		return knoten;
	}

	JJNode condensePNode(final JJNode knoten) {
		// ffAssert(knoten);
		// ffAssert(graph_of(knoten) == spqrTree);

		JJNode result = knoten;

		if (knoten.outdeg() == 1) {
			// JJNode vater= knoten.father();
			final JJNode f = knoten.firstSon();
			final JJNode d1 = f.firstSon();
			final JJNode d2 = f.lastSon();
			// ffAssert(d1 != d2);
			final JJNode e1 = d1.firstSon();
			final JJNode e2 = d2.firstSon();
			result = e1.firstSon();
			if (result == null)
				result = e2.firstSon();
			else {
				// ffAssert(e2.firstSon() == null);
			}

			// ffAssert(result != null);
			delSpqrNode(knoten);
			delSpqrNode(f);
			delSpqrNode(d1);
			delSpqrNode(d2);
			delSpqrNode(e1);
			delSpqrNode(e2);
		}

		return result;
	}

	void delSpqrNode(final JJNode knoten) {
		// ffAssert(graph_of(knoten) == spqrTree);

		final JJSpqrNode info = spqrInfo(knoten);

		if ((info.getSpqrType() == JJSpqrNode.F_NODE) && isBlocking(knoten)) {
			// Debug.println( "Deleting blocked F Node" );

			final JJNodePair tmpP = new JJNodePair(getSource(knoten), getSink(knoten));

			aux.undefine(tmpP, knoten);
		}

		nodeToSpqrNode.remove(knoten);
		// delete info;
		spqrTree.deleteNode(knoten);
	}

	JJNode findRepInSceleton(final JJNode scel, final JJNode v) {
		final JJNode tmpN = spqrFather(v);
		// ffAssert(tmpN != null);

		if (tmpN == scel)
			return v.father();

		return findRepInSceleton(scel, tmpN);
	}

	boolean nonBlockingUpTo(final JJNode my, final JJNode v) {
		if (v == my)
			return true;

		if (isBlocking(v))
			return false;

		return nonBlockingUpTo(my, spqrFather(v));
	}

	JJNode firstBlockingUpTo(final JJNode my, final JJNode v) {
		if (v == my)
			return my;

		if (isBlocking(v))
			return v;

		return firstBlockingUpTo(my, spqrFather(v));
	}

	//
	// Looks whether JJNode v contains an JJEdge from source to sink or vice
	// versa
	//

	JJNode findEdgeWithAndRevert(final JJNode v, final JJNode newQ) {
		// JJEdge tmpE;

		for (final Iterator<JJEdge> iter = v.outIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();

			final JJNode tmpN = _findEdgeWithAndRevert(tmpE.getTarget(), newQ);
			if (tmpN != null)
				return tmpN;
		}
		return null;
	}

	JJNode _findEdgeWithAndRevert(final JJNode v, final JJNode newQ) {
		final JJSpqrNode vInfo = spqrInfo(v);
		final JJNode source = getSource(newQ);
		final JJNode sink = getSink(newQ);

		if (vInfo.getSpqrType() == JJSpqrNode.E_NODE) {
			if ((getSink(v) == sink) && (getSource(v) == source))
				return v;
			else if ((getSink(v) == source) && (getSource(v) == sink)) {
				revert(newQ);
				return v;
			}
			return null;
		}

		for (final Iterator<JJEdge> iter = v.outIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			final JJNode tmpN = _findEdgeWithAndRevert(tmpE.getTarget(), newQ);
			if (tmpN != null)
				return tmpN;
		}
		return null;
	}

	boolean isExtrem(final JJNode face, final JJNode probant) {
		if ((getSource(face) == probant) || (getSink(face) == probant))
			return true;
		return false;
	}

	void addEdgeToRNodeBothExtreme(final JJNode rNode, final JJNode newQ, final JJNode flaeche) {
		// Debug.println( "Adding JJEdge to R JJNode with both extreme" );

		final JJNode newF = newFNode(rNode);
		final JJNode newD1 = newDNodes(newF, newF);

		if (getSource(newQ) != getSource(flaeche))
			revert(newQ);

		final JJNode newE = newENodes(getSource(newQ), getSink(newQ), newD1);

		final JJNode swapPath = flaeche.firstSon();
		spqrTree.deleteEdge(newD1.inEdge());
		spqrTree.deleteEdge(swapPath.inEdge());
		spqrTree.addEdge(newF, swapPath);
		spqrTree.addEdge(flaeche, newD1);
		spqrTree.addEdge(newE, newQ);

		updateBlocking(rNode);
	}

	void addEdgeToRNodeOnPath(final JJNode path, final JJNode newQ) {
		// Debug.println( "Adding JJEdge to R-node on path" );

		final JJNode qSource = getSource(newQ);
		final JJNode qSink = getSink(newQ);

		final JJNode pSource = getSource(path);
		final JJNode pSink = getSink(path);

		JJNode uno = null;
		JJNode due = null;

		JJNode sinkV = gtInfo(qSink).getV1();
		if ((sinkV != null) && (sinkV.father() != path)) {
			sinkV = gtInfo(qSink).getV2();
		}
		JJNode sourceV = gtInfo(qSource).getV1();
		if ((sourceV != null) && (sourceV.father() != path)) {
			sourceV = gtInfo(qSource).getV2();
		}

		if (isExtrem(path, qSource)) {
			if (sinkV == null || sinkV.father() != path) {
				throw new RuntimeException("SinkV is null or its father is not equal to path. I didn't expect this.");
			}
			// ffAssert(sinkV.father() == path);
			if (qSource == pSource) {
				uno = path.firstSon();
				due = sinkV.leftBrother();
			} else // qSource == pSink
			{
				revert(newQ);
				uno = sinkV.rightBrother();
				due = path.lastSon();
			}
		} else if (isExtrem(path, qSink)) {
			if (sourceV == null || sourceV.father() != path) {
				throw new RuntimeException("SinkV is null or its father is not equal to path. I didn't expect this.");
			}
			// ffAssert(sourceV.father() == path);
			if (qSink == pSink) {
				due = path.lastSon();
				uno = sourceV.rightBrother();
			} else // qSink == pSource
			{
				revert(newQ);
				uno = path.firstSon();
				due = sourceV.leftBrother();
			}
		} else {
			if (sourceV == null || sourceV.father() != path) {
				throw new RuntimeException("SinkV is null or its father is not equal to path. I didn't expect this.");
			}
			if (sinkV == null || sinkV.father() != path) {
				throw new RuntimeException("SinkV is null or its father is not equal to path. I didn't expect this.");
			}
			// ffAssert(sourceV.father() == path);
			// ffAssert(sinkV.father() == path);
			uno = JJMisc.firstSuccessor(path, sourceV, sinkV);
			if (uno != sourceV) {
				due = sourceV.leftBrother();
				revert(newQ); // ???? I'm not really shure
			} else
				due = sinkV.leftBrother();
			uno = uno.rightBrother();
		}

		// ffAssert(uno);
		// ffAssert(due);

		// ok we have uno and due now build new face and start moving

		final JJNode rNode = spqrFather(path);
		final JJNode newF = newFNode(rNode);
		final JJNode newD = newDNodes(newF, newF);
		final JJNode newE = newENodes(getSource(newQ), getSink(newQ), newD);
		spqrTree.deleteEdge(newE.inEdge());
		spqrTree.addEdge(uno.inEdge(), newE, JJGraph.before);
		spqrTree.addEdge(newE, newQ);
		JJMisc.moveEdges(uno, due, newD);

		updateBlocking(rNode);
	}

	JJNode findRepOnPath(final JJNode path, final JJNode v) {
		final JJNode v1 = gtInfo(v).getV1();
		if (v1.father() == path) {
			return v1;
		}
		// ffAssert( gtInfo(v).getV2().father() == path);
		return gtInfo(v).getV2();
	}

	void addEdgeToRNodeDifferentPathes(final JJNode face, final JJNode newQ) {
		// Debug.println( "Adding JJEdge to R JJNode with different pathes" );

		setBlocking(face, false); // This face will change source and sink
		// and cannot be longer found in auxStruct !!!

		final JJNode rNode = face.father();

		final JJNode qSource = getSource(newQ);
		final JJNode qSink = getSink(newQ);

		final JJNode path1 = face.firstSon();
		final JJNode path2 = face.lastSon();

		JJNode uno = null;
		JJNode due = null;

		if (pathContainsGt(path1, qSource)) {
			uno = findRepOnPath(path1, qSource);
			due = findRepOnPath(path2, qSink);
		} else {
			uno = findRepOnPath(path2, qSource);
			due = findRepOnPath(path1, qSink);
		}

		final JJNode newF = newFNode(rNode);
		final JJNode newD1 = newDNodes(newF, newF);
		final JJNode newD2 = getTwin(newD1);
		final JJNode newE1 = newENodes(qSource, qSink, null);
		final JJNode newE2 = getTwin(newE1);

		final JJNode pathUno = uno.father();
		final JJNode pathDue = due.father();

		JJMisc.moveEdges(uno.rightBrother(), pathUno.lastSon(), newD1);
		JJMisc.moveEdges(due, pathDue.lastSon(), newD2);

		spqrTree.addEdge(uno.inEdge(), newE1, JJGraph.after);
		spqrTree.addEdge(due.inEdge(), newE2, JJGraph.before);
		spqrTree.addEdge(newE1, newQ);

		updateBlocking(rNode);
	}

	JJNode findFaceWith(final JJNode x, final JJNode qSource, final JJNode qSink) {
		// JJEdge tmpE;

		final JJNode f11 = faceOf(gtInfo(qSource).getV1());
		final JJNode f12 = faceOf(gtInfo(qSource).getV2());
		final JJNode f21 = faceOf(gtInfo(qSink).getV1());
		final JJNode f22 = faceOf(gtInfo(qSink).getV2());

		if (f11 == f21)
			return f11;
		if (f11 == f22)
			return f11;
		if (f22 == f12)
			return f22;
		if (f21 == f12)
			return f21;
		if ((f11 != null) && ((getSource(f11) == qSink) || (getSink(f11) == qSink)))
			return f11;
		if ((f12 != null) && ((getSource(f12) == qSink) || (getSink(f12) == qSink)))
			return f12;
		if ((f21 != null) && ((getSource(f21) == qSource) || (getSink(f21) == qSource)))
			return f21;
		if ((f22 != null) && ((getSource(f22) == qSource) || (getSink(f22) == qSource)))
			return f22;

		if (aux.defined(new JJNodePair(qSource, qSink)))
			return aux.get(new JJNodePair(qSource, qSink)); // aux[JJNodePair(qSource,
															// qSink)];

		if (aux.defined(new JJNodePair(qSink, qSource)))
			return aux.get(new JJNodePair(qSink, qSource)); // aux[JJNodePair(qSink,
															// qSource)];

		// ffAssert("Couldn't find face with source and sink");
		return null;
	}

	void addEdgeToRNode(final JJNode rNode, final JJNode newQ, final JJNode flaeche) {
		final HashSet<JJNode> tmpS = new HashSet<>(); // set<node> tmpS;
		tmpS.add(rNode);
		if (flaeche != null)
			tmpS.add(flaeche);

		// if(singleStep)
		// fenster.confirmStep("Add JJEdge to R Node", tmpS, gtSpqrTree);

		JJNode theFace = flaeche;
		final JJNode qSource = getSource(newQ);
		final JJNode qSink = getSink(newQ);

		if (theFace == null) {
			theFace = findFaceWith(rNode, qSource, qSink);
			// ffAssert(theFace);
		}

		// Debug.println( "Adding JJEdge (" + qSource.getName() + "-"
		// + qSink.getName() + ") to R-Node in face ("
		// + getSource(theFace).getName() + "-"
		// + getSink(theFace).getName() + ")");

		// JJNode fSource = getSource(theFace);
		// JJNode fSink = getSink(theFace);

		final JJNode path1 = theFace.firstSon();
		final JJNode path2 = theFace.lastSon();

		// Are we on the same path?
		if (isExtrem(theFace, qSink)) {
			if (isExtrem(theFace, qSource)) {
				addEdgeToRNodeBothExtreme(rNode, newQ, theFace);
				return;
			}
			if (pathContainsGt(path1, qSource)) {
				addEdgeToRNodeOnPath(path1, newQ);
				return;
			}
			// ffAssert(pathContainsGt(path2, qSource));
			addEdgeToRNodeOnPath(path2, newQ);
			return;
		} else if (isExtrem(theFace, qSource)) {
			if (pathContainsGt(path1, qSink)) {
				addEdgeToRNodeOnPath(path1, newQ);
				return;
			}
			// ffAssert(pathContainsGt(path2, qSink));
			addEdgeToRNodeOnPath(path2, newQ);
			return;
		} else if (pathContainsGt(path1, qSink) && pathContainsGt(path1, qSource)) {
			addEdgeToRNodeOnPath(path1, newQ);
			return;
		} else if (pathContainsGt(path2, qSink) && pathContainsGt(path2, qSource)) {
			addEdgeToRNodeOnPath(path2, newQ);
			return;
		}

		addEdgeToRNodeDifferentPathes(theFace, newQ);

	}

	//
	// Finds the missing JJEdge of newQ and reverts newQ if it is wrongly
	// directed
	//

	JJEdge findOtherEdgeAndRevert(final JJNode chi, final JJEdge lambda, final JJNode newQ) {
		final JJSpqrNode qInfo = spqrInfo(newQ);
		final JJSpqrNode chiInfo = spqrInfo(chi);

		final JJNode qSink = qInfo.getSink();
		final JJNode qSource = qInfo.getSource();
		final JJNode chiSink = chiInfo.getSink();
		final JJNode chiSource = chiInfo.getSource();
		final JJNode rNode = lambda.getTarget(); // spqrTree.target(lambda);

		final JJNode tmpFather = lambda.getSource().father();

		// Test if source or sink of newQ is also extreme point of chi

		if (chiSource == qSource)
			return tmpFather.firstOutEdge();
		else if (chiSource == qSink) {
			revert(newQ);
			return tmpFather.firstOutEdge();
		}

		if (chiSink == qSink)
			return tmpFather.lastOutEdge();
		else if (chiSink == qSource) {
			revert(newQ);
			return tmpFather.lastOutEdge();
		}

		// chi must be the prop alloc JJNode of qSink or qSource

		JJNode qSinkV1 = gtInfo(qSink).getV1();
		JJNode qSourceV1 = gtInfo(qSource).getV1();
		final JJNode lambdaSource = lambda.getSource(); // spqrTree.source(lambda);

		if (spqrFather(qSinkV1) == chi) {
			// ffAssert(spqrFather(qSourceV1) == rNode);
			if (qSinkV1.father() != lambdaSource.father())
				qSinkV1 = getTwin(qSinkV1);

			if (qSinkV1 == JJMisc.firstSuccessor(qSinkV1.father(), qSinkV1, lambdaSource)) {
				revert(newQ);
				qSinkV1 = qSinkV1.rightBrother();
			} else
				qSinkV1 = qSinkV1.leftBrother();

			return qSinkV1.inEdge();
		} else // spqrFather(qSourceV1 == chi)
		{
			// ffAssert(spqrFather(qSinkV1) == rNode);
			if (qSourceV1.father() != lambdaSource.father())
				qSourceV1 = getTwin(qSourceV1);

			if (lambdaSource == JJMisc.firstSuccessor(qSourceV1.father(), qSourceV1, lambdaSource)) {
				revert(newQ);
				qSourceV1 = qSourceV1.leftBrother();
			} else
				qSourceV1 = qSourceV1.rightBrother();

			return qSourceV1.inEdge();
		}
		// ffAssert("Couldn't find other edge" == NULL);
	}

	void updateBlocking(final JJNode v) {
		// ffAssert(spqrInfo(v).getSpqrType() == JJSpqrNode.R_NODE);
		JJEdge tmpE;

		final JJNode stEdge = spqrInfo(v).getStEdge();
		final JJNode face1 = stEdge.grandFather();
		final JJNode face2 = getTwin(stEdge).grandFather();

		for (final Iterator<JJEdge> iter = v.outIterator(); iter.hasNext();) {
			tmpE = iter.next();

			final JJNode tmpF = tmpE.getTarget(); // spqrTree.target(tmpE);

			if ((tmpF != face1) && (tmpF != face2)) {
				setBlocking(tmpF, true);
			}
		}

		setBlocking(face1, false);
		setBlocking(face2, false);

		reDistributeChildren(v);
	}

	void reDistributeChildren(final JJNode v) {
		final Stack<JJNode> kids = new Stack<>(); // slist<node> kids;
		collectSpqrChildren(v, kids);
		while (kids.isEmpty() == false) {
			final JJNode tmpN = kids.pop();
			final JJNode e1 = tmpN.father();
			if (isBlocking(e1)) {
				final JJNode e2 = getTwin(e1);
				if (isBlocking(e2) == false) {
					spqrTree.deleteEdge(tmpN.inEdge());
					spqrTree.addEdge(e2, tmpN);
				}
			}
		}
	}

	void collectSpqrChildren(final JJNode v, final Stack<JJNode> kids) {
		JJEdge tmpE;

		for (final Iterator<JJEdge> iter = v.outIterator(); iter.hasNext();) {
			tmpE = iter.next();
			_collectSpqrChildren(tmpE.getTarget(), kids);
		}
	}

	void collectChildren(final JJNode v, final Stack<JJNode> kids) {
		JJEdge tmpE;

		for (final Iterator<JJEdge> iter = v.outIterator(); iter.hasNext();) {
			tmpE = iter.next();
			_collectChildren(tmpE.getTarget(), kids);
		}
	}

	void _collectChildren(final JJNode v, final Stack<JJNode> kids) {
		switch (spqrInfo(v).getSpqrType()) {
		case JJSpqrNode.S_NODE:
		case JJSpqrNode.P_NODE:
		case JJSpqrNode.Q_NODE:
		case JJSpqrNode.R_NODE: {
			// Debug.println( "Warning collect children found SPQR Node" );

			// fall through
		}
		default:
			JJEdge tmpE;

			kids.push(v);

			for (final Iterator<JJEdge> iter = v.outIterator(); iter.hasNext();) {
				tmpE = iter.next();
				_collectChildren(tmpE.getTarget(), kids);
			}
			break;
		}
	}

	void _collectSpqrChildren(final JJNode v, final Stack<JJNode> kids) {
		switch (spqrInfo(v).getSpqrType()) {
		case JJSpqrNode.S_NODE:
		case JJSpqrNode.P_NODE:
		case JJSpqrNode.Q_NODE:
		case JJSpqrNode.R_NODE: {
			kids.push(v); // push(v);
			break;
		}
		default:
			JJEdge tmpE;

			for (final Iterator<JJEdge> iter = v.outIterator(); iter.hasNext();) {
				tmpE = iter.next();
				_collectSpqrChildren(tmpE.getTarget(), kids);
			}
			break;
		}
	}

	void setBlocking(final JJNode v, final boolean flag) {
		if (spqrInfo(v).getSpqrType() == JJSpqrNode.F_NODE) {
			if (flag == true) {
				if (!isBlocking(v)) {
					v.setName("f"); // gtSpqrTree.gt(v).label("f");

					spqrInfo(v).setSource(getSource(v));
					spqrInfo(v).setSink(getSink(v));
					final JJNodePair tmpP = new JJNodePair(spqrInfo(v).getSource(), spqrInfo(v).getSink());

					// Debug.println( "Adding to Auxstruct: " + "(" +
					// tmpP.getNodeOne().getName()
					// + "," + tmpP.getNodeTwo().getName() + ")" );

					aux.define(tmpP, v);
				}
			} else {
				if (isBlocking(v)) {
					final JJNodePair tmpP = new JJNodePair(spqrInfo(v).getSource(), spqrInfo(v).getSink());

					v.setName("F"); // gtSpqrTree.gt(v).label("F");

					spqrInfo(v).setSource(null);
					spqrInfo(v).setSink(null);
					// ffAssert(tmpP.getNodeOne() != null);
					// ffAssert(tmpP.getNodeTwo() != null);
					// ffAssert(aux.defined(tmpP, v));
					// if(!auxStruct.defined(tmpP))
					// {
					// forall_defined(tmpP, auxStruct)
					// {
					// Debug.println( "Defined: " + tmpP + "(" +
					// graph.gt(tmpP.getNodeOne()).label()
					// + "," + graph.gt(tmpP.getNodeTwo()).label() + ")" );
					// }
					// }
					// else
					// {
					//
					// Debug.println( "Removing from Auxstruct: " + "(" +
					// graph.gt(tmpP.getNodeOne()).label()
					// + "," + graph.gt(tmpP.getNodeTwo()).label() + ")" );
					//
					aux.undefine(tmpP, v);
					// }
				}
			}
			spqrInfo(v).setBlocking(flag);
			return;
		}

		// ffAssert(v.father() != null);
		setBlocking(v.father(), flag);
	}

	//
	// Looks for a face in which v1 and v2 are on the same path.
	// It returns the path
	//

	JJNode findSamePathOnFace(final JJNode v1, final JJNode v2) {
		final JJNode v1v1 = gtInfo(v1).getV1();
		final JJNode v1v2 = gtInfo(v1).getV2();
		final JJNode v2v1 = gtInfo(v2).getV1();
		final JJNode v2v2 = gtInfo(v2).getV2();

		JJNode v1d1 = null;
		JJNode v1d2 = null;
		JJNode v2d1 = null;
		JJNode v2d2 = null;

		if (v1v1 != null) // v1 is S or T
		{
			v1d1 = v1v1.father();
			v1d2 = v1v2.father();
			// Debug.println( "v1d1 : " + gtSpqrTree.gt(v1d1).label() );
			// Debug.println( "v1d2 : " + gtSpqrTree.gt(v1d2).label() );
		}

		if (v2v1 != null) // v2 is S or T
		{
			v2d1 = v2v1.father();
			v2d2 = v2v2.father();
			// Debug.println( "v2d1 : " + gtSpqrTree.gt(v2d1).label() );
			// Debug.println( "v2d2 : " + gtSpqrTree.gt(v2d2).label() );
		}

		if ((v1d1 == v2d1) || (v1d1 == v2d2)) {
			return v1d1;
		} else if ((v1d2 == v2d1) || (v1d2 == v2d2)) {
			return v1d2;
		} else {
			if (v1d1 != null) {
				if ((getSource(v1d1) == v2) || (getSink(v1d1) == v2)) {
					return v1d1;
				} else if ((getSource(v1d2) == v2) || (getSink(v1d2) == v2)) {
					return v1d2;
				}
			}

			if (v2d1 != null) {
				if ((getSource(v2d1) == v1) || (getSink(v2d1) == v1)) {
					return v2d1;
				} else if ((getSource(v2d2) == v1) || (getSink(v2d2) == v1)) {
					return v2d2;
				}
			}
		}
		// Debug.println( "Couldn't find the same path" );

		return null;
	}

	//
	// Merges two D nodes into one.
	// One D JJNode must be completely covered by an E JJNode of the other D
	// Node
	//

	void mergeDNodes(final JJNode d1, final JJNode d2) {
		final JJNode d1Source = getSource(d1);
		final JJNode d1Sink = getSink(d1);
		final JJNode d2Source = getSource(d2);
		final JJNode d2Sink = getSink(d2);
		JJEdge tmpE;

		for (final Iterator<JJEdge> iter = d1.outIterator(); iter.hasNext();) {
			tmpE = iter.next();

			final JJNode tmpN = tmpE.getTarget(); // spqrTree.target(tmpE);
			if ((getSource(tmpN) == d2Source) && (getSink(tmpN) == d2Sink)) {
				replaceENodeWithD(tmpN, d2);

				if (d2.father() != null)
					spqrTree.addEdge(d2.father(), d1);

				if (getTwin(d2) != null)
					spqrInfo(getTwin(d2)).setTwin(null);

				delSpqrNode(d2);

				return;
			}
		}

		for (final Iterator<JJEdge> iter = d2.outIterator(); iter.hasNext();) {
			tmpE = iter.next();
			final JJNode tmpN = tmpE.getTarget(); // spqrTree.target(tmpE);
			if ((getSource(tmpN) == d1Source) && (getSink(tmpN) == d1Sink)) {
				replaceENodeWithD(tmpN, d1);

				if (d1.father() != null)
					spqrTree.addEdge(d1.father(), d2);

				if (getTwin(d1) != null)
					spqrInfo(getTwin(d1)).setTwin(null);

				delSpqrNode(d1);
				return;
			}
		}
	}

	//
	// Replaces eNode bei the successors of dNode
	//
	// Should test sink and source compatibility first !!!
	//

	void replaceENodeWithD(final JJNode eNode, final JJNode dNode) {
		JJEdge loopE = dNode.firstOutEdge();
		JJEdge insPoint = eNode.inEdge();
		final JJEdge end = dNode.lastOutEdge();

		while (true) {
			final JJEdge newLoopE = loopE.adjSucc();
			final JJNode tmpTarget = loopE.getTarget(); // spqrTree.target(loopE);
			insPoint = spqrTree.addEdge(insPoint, tmpTarget, JJGraph.after);
			spqrTree.deleteEdge(loopE);
			if (loopE == end)
				break;
			loopE = newLoopE;
			// ffAssert(loopE);
		}
		delSpqrNode(eNode);
	}

	void setSource(final JJNode v, final JJNode s) {
		// ffAssert(graph_of(v) == spqrTree);
		// ffAssert(graph_of(s) == graph);
		switch (spqrInfo(v).getSpqrType()) {
		case JJSpqrNode.S_NODE:
		case JJSpqrNode.P_NODE:
		case JJSpqrNode.Q_NODE:
		case JJSpqrNode.R_NODE:
		case JJSpqrNode.E_NODE: {
			break;
		}
		case JJSpqrNode.F_NODE: {
			if (!isBlocking(v))
				break;
		}
		default: {
			// ffAssert("SetSource not applicable");
		}
		}

		spqrInfo(v).setSource(s);
	}

	void delSubTree(final JJNode v) {
		final Stack<JJNode> adios = new Stack<>();
		adios.push(v);
		collectChildren(v, adios);
		while (adios.isEmpty() == false) {
			delSpqrNode(adios.pop());
		}
	}

	boolean pathContains(final JJNode pfad, final JJNode probant) {
		// ffAssert(spqrInfo(pfad).getSpqrType() == JJSpqrNode.D_NODE);
		// ffAssert(graph_of(probant) == spqrTree);

		if (probant.father() == pfad)
			return true;

		return false;
	}

	boolean pathContainsGt(final JJNode pfad, final JJNode probant) {
		// ffAssert(graph_of(probant) == graph);

		// Debug.println( "Testing path " + pfad.getName() + " for JJNode "
		// + probant.getName() );

		if ((probant == theSNode) || (probant == theTNode))
			return false;

		// ffAssert(spqrInfo(pfad).getSpqrType() == JJSpqrNode.D_NODE);

		if ((gtInfo(probant).getV1().father() == pfad) || (gtInfo(probant).getV2().father() == pfad))
			return true;

		return false;
	}

	boolean faceContainsGt(final JJNode face, final JJNode probant) {
		// ffAssert(spqrInfo(face).getSpqrType() == JJSpqrNode.F_NODE);
		// ffAssert(graph_of(probant) == graph);

		// Debug.println( "Testing face ("
		// + getSource(face).getName() + "-"
		// + getSink(face).getName() + ") for "
		// + probant.getName() );

		if ((getSource(face) == probant) || (getSink(face) == probant))
			return true;

		if (pathContainsGt(face.firstSon(), probant) || pathContainsGt(face.lastSon(), probant))
			return true;
		return false;
	}

	JJNode findStPathWith(final JJNode knoten, final JJNode probant) {
		final JJSpqrNode kInfo = spqrInfo(knoten);
		// ffAssert( (kInfo.getSpqrType() == JJSpqrNode.S_NODE) ||
		// (kInfo.getSpqrType() == JJSpqrNode.R_NODE));

		final JJNode stEdge1 = kInfo.getStEdge();
		final JJNode stEdge2 = getTwin(stEdge1);
		final JJNode stPfad1 = stEdge1.uncle();
		final JJNode stPfad2 = stEdge2.uncle();

		if (pathContainsGt(stPfad1, probant))
			return stPfad1;
		else if (pathContainsGt(stPfad2, probant))
			return stPfad2;

		return null;
	}

	//
	// Replaces the E JJNode eNode with the R JJNode of the path thePath
	// thePath is on the same face as eNode afterwards
	// thePath must be an st-path
	//

	void replaceENodeByRNode(final JJNode eNode, final JJNode path1) {
		// confirmStep("Replace E JJNode by R Node");

		final JJNode eNode2 = getTwin(eNode);
		final JJNode path2 = getTwin(path1.brother().firstSon()).uncle();

		final JJNode face1 = path1.father();
		final JJNode face2 = path2.father();

		final JJNode oldR = face1.father();

		JJMisc.moveEdges(path1.firstOutEdge(), path1.lastOutEdge(), eNode.inEdge(), JJGraph.after);

		JJMisc.moveEdges(path2.firstOutEdge(), path2.lastOutEdge(), eNode2.inEdge(), JJGraph.after);

		final JJNode newR = spqrFather(eNode);

		delSubTree(face1);
		delSubTree(face2);
		delSpqrNode(eNode);
		delSpqrNode(eNode2);

		while (oldR.outdeg() != 0) {
			final JJNode tmpN = oldR.firstSon();
			spqrTree.deleteEdge(tmpN.inEdge());
			spqrTree.addEdge(newR, tmpN);
		}

		delSpqrNode(oldR);
	}

	//
	// Replaces the E JJNode eNode with the R JJNode rNode
	// If eNode is on a st-Face and stPfad != null,
	// stPfad is on an stFace afterwards
	// stPfad is the path of rNode which should be on the st-face of
	// eNode.
	//

	void replaceENodeByRNodeST(final JJNode eNode, final JJNode rNode, final JJNode stPfad) {
		final JJSpqrNode rInfo = spqrInfo(rNode);
		// ffAssert(spqrInfo(eNode).getSpqrType() == JJSpqrNode.E_NODE);
		// ffAssert(rInfo.getSpqrType() == JJSpqrNode.R_NODE);

		final JJNode eNode2 = getTwin(eNode);
		final JJNode stEdge1 = rInfo.getStEdge();
		final JJNode stEdge2 = getTwin(stEdge1);
		JJNode stPfad1 = stEdge1.uncle();
		final JJNode stPfad2 = stEdge2.uncle();

		// We have to take care that the new Q JJNode is on an external face
		if (stPfad != null) {
			if (stPfad1 == stPfad) {
				if (isBlocking(eNode)) {
					// Debug.println( "Swapping" );

					stPfad1 = stPfad2;
					// ffAssert(isBlocking(eNode2) == false);
				}
			} else // stPfad2 == stPfad
			{
				if (isBlocking(eNode2)) {
					// Debug.println( "Swapping" );

					stPfad1 = stPfad2;
					// ffAssert(isBlocking(eNode) == false);
				}
			}
		}

		replaceENodeByRNode(eNode, stPfad1);
	}

	void finalTransformationRParallel(final JJNode newQ, final JJNode parallelEdge) {
		final JJSpqrNode qInfo = spqrInfo(newQ);

		// Debug.println( "Parallel JJEdge is: " + parallelEdge.getName());

		final JJNode pTwin = getTwin(parallelEdge);
		JJNode pSon = parallelEdge.firstSon();

		if (pSon == null)
			pSon = pTwin.firstSon();

		// ffAssert(pSon != null);

		// Do we need a new P-Node ??

		if (spqrInfo(pSon).getSpqrType() != JJSpqrNode.P_NODE) {
			spqrTree.deleteEdge(pSon.inEdge());

			final JJNode newP = newPNode(qInfo.getSource(), qInfo.getSink(), parallelEdge);
			final JJNode f1 = newFNode(newP);
			final JJNode f2 = newFNode(newP);
			final JJNode d1 = newDNodes(f1, f2);
			final JJNode d2 = newDNodes(f1, f2);
			final JJNode e1 = newENodes(qInfo.getSource(), qInfo.getSink(), d1);
			final JJNode e2 = newENodes(qInfo.getSource(), qInfo.getSink(), d2);
			spqrTree.addEdge(e1, pSon);
			spqrTree.addEdge(e2, newQ);
		} else // We already have a P JJNode as son
		{
			final JJNode oldFace = pSon.firstSon();
			final JJNode oldPath = oldFace.firstSon();

			final JJNode newFace = newFNode(pSon);
			final JJNode newPath = newDNodes(newFace, oldFace);

			final JJNode newEdge = newENodes(qInfo.getSource(), qInfo.getSink(), newPath);

			spqrTree.deleteEdge(oldPath.inEdge());
			spqrTree.addEdge(newFace, oldPath);
			spqrTree.addEdge(newEdge, newQ);
		}
		updateBlocking(spqrFather(parallelEdge));
	}

	void finalTransformationS(final JJNode chi, final JJEdge lambda1, final JJEdge lambda2, final JJNode newQ) {
		// Debug.println( "Final transformation R R S" );

		final HashSet<JJNode> tmpS = new HashSet<>(); // set<node> tmpS;
		tmpS.add(chi);
		tmpS.add(lambda1.getTarget());
		tmpS.add(lambda2.getTarget());

		if (singleStepFlag)
			fenster.confirmStep("Final Transformation R R S", tmpS);

		JJNode eNode1 = lambda1.getSource();
		JJNode eNode2 = lambda2.getSource();

		// ffAssert(eNode1.father() == eNode2.father());

		eNode1 = JJMisc.firstSuccessor(eNode1.father(), eNode1, eNode2);
		if (eNode1 == eNode2) {
			eNode2 = lambda1.getSource();
		}

		final JJNode rNode1 = eNode1.firstSon();
		final JJNode rNode2 = eNode2.firstSon();

		// We now look for the pathes containing qSink and qSource

		final JJNode qSource = getSource(newQ);
		final JJNode qSink = getSink(newQ);

		JJNode stEdge1 = spqrInfo(rNode1).getStEdge();

		JJNode path1 = stEdge1.uncle();
		if (!(pathContainsGt(path1, qSource) || pathContainsGt(path1, qSink))) {
			stEdge1 = getTwin(stEdge1);
			path1 = stEdge1.uncle();
		}

		JJNode stEdge2 = spqrInfo(rNode2).getStEdge();

		JJNode path2 = stEdge2.uncle();
		if (!(pathContainsGt(path2, qSource) || pathContainsGt(path2, qSink))) {
			stEdge2 = getTwin(stEdge2);
			path2 = stEdge2.uncle();
		}

		// JJNode face1= path1.father();
		// JJNode face2= path2.father();

		// We now build the beta S Node
		final JJNode vRight = eNode1.rightBrother();
		final JJNode vLeft = eNode2.leftBrother();

		if (vRight != vLeft) {
			if (vRight.rightBrother() != vLeft.leftBrother()) {
				final JJNode newE = newENodes(spqrInfo(vRight).getRealNode(), spqrInfo(vLeft).getRealNode(), null);
				final JJNode newD = newSTree(spqrInfo(vRight).getRealNode(), spqrInfo(vLeft).getRealNode(), newE);
				JJMisc.moveEdges(getTwin(vRight.rightBrother()), getTwin(vLeft.leftBrother()), getTwin(newD));
				JJMisc.moveEdges(vRight.rightBrother(), vLeft.leftBrother(), newD);
				spqrTree.addEdge(vRight.inEdge(), newE, JJGraph.after);
				spqrTree.addEdge(getTwin(vRight).inEdge(), getTwin(newE), JJGraph.after);
			}
		}

		// Adjust the new st-edge

		setSink(stEdge1, getSink(eNode2));
		setSink(getTwin(stEdge1), getSink(eNode2));

		// Now merge the two R-Nodes and beta

		JJMisc.moveEdges(vRight, vLeft, path1.lastOutEdge(), JJGraph.after);
		JJMisc.moveEdges(path2.firstSon(), path2.lastSon(), path1.lastOutEdge(), JJGraph.after);

		final JJNode path12 = getTwin(stEdge1).uncle();
		final JJNode path22 = getTwin(stEdge2).uncle();

		JJMisc.moveEdges(getTwin(vRight), getTwin(vLeft), path12.lastOutEdge(), JJGraph.after);
		JJMisc.moveEdges(path22.firstSon(), path22.lastSon(), path12.lastOutEdge(), JJGraph.after);

		delSubTree(faceOf(path22));
		delSubTree(faceOf(path2));

		JJMisc.moveEdges(rNode2.firstSon(), rNode2.lastSon(), rNode1);

		setSink(rNode1, getSink(eNode2));
		setSink(rNode1.father(), getSink(eNode2));
		setSink(getTwin(rNode1.father()), getSink(eNode2));

		delSubTree(getTwin(eNode2));
		delSubTree(eNode2);

		addEdgeToRNodeOnPath(path1, newQ);

		// Do we have to remove the S Node

		if (eNode1.outdeg() < 3) {
			final JJNode tmpNN = spqrFather(eNode1);
			final JJNode sFather = tmpNN.father();

			spqrTree.deleteEdge(tmpNN.inEdge());
			spqrTree.deleteEdge(rNode1.inEdge());
			spqrTree.addEdge(sFather, rNode1);
			delSubTree(tmpNN);
		}
	}

	void finalTransformationR(final JJNode chi, final JJEdge lambda1, final JJEdge lambda2, final JJNode newQ) {
		// Debug.println( "Final transformation R R R" );

		final HashSet<JJNode> tmpS = new HashSet<>(); // set<node> tmpS;
		tmpS.add(chi);
		tmpS.add(lambda1.getTarget());
		tmpS.add(lambda2.getTarget());

		if (singleStepFlag)
			fenster.confirmStep("Final Transformation R R R", tmpS);

		JJNode eNode1 = lambda1.getSource();
		JJNode eNode2 = lambda2.getSource();
		final JJNode rNode1 = lambda1.getTarget();
		final JJNode rNode2 = lambda2.getTarget();

		// We new look for the face with eNode1 and eNode2
		// we adjust eNode1 and eNode2 and move the R-Node if necessary

		if (faceOf(eNode1) != faceOf(eNode2)) {
			if (faceOf(eNode1) == faceOf(getTwin(eNode2))) {
				eNode2 = getTwin(eNode2);
				spqrTree.deleteEdge(rNode2.inEdge());
				spqrTree.addEdge(eNode2, rNode2);
			} else if (faceOf(getTwin(eNode1)) == faceOf(eNode2)) {
				eNode1 = getTwin(eNode1);
				spqrTree.deleteEdge(rNode1.inEdge());
				spqrTree.addEdge(eNode1, rNode1);
			} else if (faceOf(getTwin(eNode1)) == faceOf(getTwin(eNode2))) {
				eNode2 = getTwin(eNode2);
				spqrTree.deleteEdge(rNode2.inEdge());
				spqrTree.addEdge(eNode2, rNode2);
				eNode1 = getTwin(eNode1);
				spqrTree.deleteEdge(rNode1.inEdge());
				spqrTree.addEdge(eNode1, rNode1);
			} else {
				// ffAssert("Couldn't find the right face" == NULL);
			}
		}

		// We now look for the pathes containing qSink and qSource

		final JJNode qSource = getSource(newQ);
		final JJNode qSink = getSink(newQ);

		JJNode stEdge1 = spqrInfo(rNode1).getStEdge();

		JJNode path1 = stEdge1.uncle();
		if (!(pathContainsGt(path1, qSource) || pathContainsGt(path1, qSink))) {
			stEdge1 = getTwin(stEdge1);
			path1 = stEdge1.uncle();
		}

		JJNode stEdge2 = spqrInfo(rNode2).getStEdge();

		JJNode path2 = stEdge2.uncle();
		if (!(pathContainsGt(path2, qSource) || pathContainsGt(path2, qSink))) {
			stEdge2 = getTwin(stEdge2);
			path2 = stEdge2.uncle();
		}

		// JJNode face1= path1.father();
		// JJNode face2= path2.father();

		final JJNode newFace = faceOf(eNode1);

		replaceENodeByRNode(eNode1, path1);
		replaceENodeByRNode(eNode2, path2);

		addEdgeToRNode(chi, newQ, newFace);
	}

	void finalTransformationR(final JJNode chi, final JJEdge lambda, final JJNode newQ) {
		// Debug.println( "Final transformation R R" );

		final HashSet<JJNode> tmpS = new HashSet<>();
		tmpS.add(chi);
		tmpS.add(lambda.getTarget());

		if (singleStepFlag)
			fenster.confirmStep("Final Transformation R R", tmpS);

		final JJSpqrNode qInfo = spqrInfo(newQ);

		JJNode eNode = lambda.getSource();
		final JJNode eNodeTwin = getTwin(eNode);

		final JJNode face1 = eNode.grandFather();
		final JJNode face2 = eNodeTwin.grandFather();

		final JJNode source = qInfo.getSource();
		final JJNode sink = qInfo.getSink();

		JJNode theFace = face1;

		if (faceContainsGt(face2, source) || faceContainsGt(face2, sink)) {
			theFace = face2;
		} else {
			// ffAssert(faceContainsGt(face1, source) || faceContainsGt(face1,
			// sink));
		}

		final JJNode rSon = lambda.getTarget();
		final JJNode stSon = spqrInfo(rSon).getStEdge();
		final JJNode stSon2 = getTwin(stSon);

		JJNode thePath = stSon.uncle();
		if ((pathContainsGt(thePath, source) || pathContainsGt(thePath, sink)) == false) {
			thePath = stSon2.uncle();
			// ffAssert((pathContainsGt(thePath, source) ||
			// pathContainsGt(thePath, sink)) == true);
		}

		if (eNode.grandFather() != theFace) {
			eNode = eNodeTwin;
			// ffAssert(eNode.grandFather() == theFace);
		}

		replaceENodeByRNode(eNode, thePath);

		addEdgeToRNode(chi, newQ, theFace);
	}

	void finalTransformationS(final JJNode chi, final JJEdge lambda, final JJNode newQ) {
		// Debug.println( "Final transformation R S" );

		final HashSet<JJNode> tmpS = new HashSet<>();
		tmpS.add(chi);
		tmpS.add(lambda.getTarget());

		if (singleStepFlag)
			fenster.confirmStep("Final Transformation R S", tmpS);

		final JJNode rNode = lambda.getTarget();

		// JJSpqrNode chiInfo= spqrInfo(chi);
		final JJSpqrNode rInfo = spqrInfo(rNode);
		final JJSpqrNode qInfo = spqrInfo(newQ);

		// JJNode chiFather = chi.father();
		final JJEdge otherE = findOtherEdgeAndRevert(chi, lambda, newQ);

		final JJNode rRep = lambda.getSource();
		final JJNode sRep = otherE.getTarget();
		final JJNode uno = JJMisc.firstSuccessor(otherE.getSource(), rRep, sRep);

		JJNode due = null;
		if (uno == sRep)
			due = rRep;
		else
			due = sRep;

		final JJNode rRepFather = rRep.father();

		// ffAssert(uno.father() == due.father());

		spqrTree.deleteEdge(lambda);

		final JJNode oldRSource = getSource(rNode);
		final JJNode oldRSink = getSink(rNode);
		final JJNode newRSource = getSource(uno);
		final JJNode newRSink = getSink(due);
		final JJNode qSink = qInfo.getSink();
		final JJNode qSource = qInfo.getSource();

		setSource(rNode, newRSource);
		setSink(rNode, newRSink);

		final JJNode tmpE = newENodes(newRSource, newRSink, null);
		spqrTree.addEdge(uno.inEdge(), tmpE, JJGraph.before);
		spqrTree.addEdge(getTwin(uno).inEdge(), getTwin(tmpE), JJGraph.before);
		spqrTree.addEdge(tmpE, rNode);

		final JJNode d1 = newDNodes(null, null); // ??? Carsten porting
		final JJNode d1Twin = getTwin(d1);
		JJMisc.moveEdges(uno, due, d1);
		JJMisc.moveEdges(getTwin(uno), getTwin(due), d1Twin);

		// Do we need a new S JJNode ?

		if (d1.outdeg() > 3) {
			// Debug.println( "We need a new S-Node" );

			if (newRSink == oldRSink) // R-Knoten ist due
			{
				// Debug.println( "R-Node is due" );

				final JJNode newE1 = newENodes(newRSource, oldRSource, null);
				final JJEdge start1 = d1.firstOutEdge();
				spqrTree.addEdge(start1, newE1, JJGraph.before);

				final JJNode newE2 = getTwin(newE1);
				final JJEdge start2 = d1Twin.firstOutEdge();
				spqrTree.addEdge(start2, newE2, JJGraph.before);

				final JJNode newD = newSTree(newRSource, oldRSource, newE1);
				JJMisc.moveEdges(start1, d1.lastSon().leftBrother().leftBrother().inEdge(), newD);
				JJMisc.moveEdges(start2, d1Twin.lastSon().leftBrother().leftBrother().inEdge(), getTwin(newD));
			} else if (newRSource == oldRSource) // R-Knoten ist uno
			{
				// Debug.println( "R-Node is uno" );

				final JJNode newE1 = newENodes(oldRSink, newRSink, null);
				final JJEdge end1 = d1.lastOutEdge();
				spqrTree.addEdge(end1, newE1, JJGraph.after);

				final JJNode newE2 = getTwin(newE1);
				final JJEdge end2 = d1Twin.lastOutEdge();
				spqrTree.addEdge(end2, newE2, JJGraph.after);

				final JJNode newD = newSTree(oldRSink, newRSink, newE1);
				JJMisc.moveEdges(d1.firstSon().rightBrother().rightBrother().inEdge(), end1, newD);
				JJMisc.moveEdges(d1Twin.firstSon().rightBrother().rightBrother().inEdge(), end2, getTwin(newD));
			}
		}

		final JJNode stEdgeR1 = rInfo.getStEdge();
		setSource(stEdgeR1, newRSource);
		setSink(stEdgeR1, newRSink);

		final JJNode stEdgeR2 = getTwin(stEdgeR1);
		setSource(stEdgeR2, newRSource);
		setSink(stEdgeR2, newRSink);

		mergeDNodes(stEdgeR1.uncle(), d1);
		mergeDNodes(stEdgeR2.uncle(), d1Twin);

		// Ok now we can add the new edge

		final JJNode newF = newFNode(rNode);
		final JJNode newD = newDNodes(newF, newF);
		final JJNode newE = newENodes(qSource, qSink, newD);
		final JJNode newETwin = getTwin(newE);

		spqrTree.addEdge(newE, newQ);

		// Debug.println( "V1: " + qSource.getName()
		// + " V2: " + qSink.getName() );

		// confirmStep("Middle of Final Transformatio R S");

		final JJNode tmpD = findSamePathOnFace(qSource, qSink);
		// ffAssert(tmpD != null);

		JJNode start = tmpD.firstSon();
		while (getSource(start) != qSource) {
			start = start.rightBrother();
		}
		if (spqrInfo(start).getSpqrType() == JJSpqrNode.V_NODE)
			start = start.rightBrother();

		JJNode end = tmpD.lastSon();
		while (getSink(end) != qSink) {
			end = end.leftBrother();
		}
		if (spqrInfo(end).getSpqrType() == JJSpqrNode.V_NODE)
			end = end.leftBrother();

		spqrTree.deleteEdge(newETwin.inEdge());
		spqrTree.addEdge(start.inEdge(), newETwin, JJGraph.before);
		JJMisc.moveEdges(start.inEdge(), end.inEdge(), getTwin(newD));

		updateBlocking(rNode);

		// Is the old S JJNode still necessary

		if (rRepFather.outdeg() < 3) {
			// Debug.println( "We don't need the S JJNode any longer" );

			final JJNode sFather = chi.father();

			spqrTree.deleteEdge(chi.inEdge());
			spqrTree.deleteEdge(rNode.inEdge());
			spqrTree.addEdge(sFather, rNode);
			delSubTree(chi);
		}
	}

	JJNode elementaryTransformation(final JJNode knoten, final JJNode vater, final JJNode newQ) {
		// Debug.println( "elementaryTransformation" );

		// ffAssert(graph_of(vater) == spqrTree);
		final JJSpqrNode spqrVaterInfo = spqrInfo(vater);

		switch (spqrVaterInfo.getSpqrType()) {
		case JJSpqrNode.S_NODE: {
			return elementaryRSTransformation(knoten, vater);
		}
		case JJSpqrNode.P_NODE: {
			return elementaryRPTransformation(knoten, vater, newQ);
		}
		case JJSpqrNode.R_NODE: {
			return elementaryRRTransformation(knoten, vater, newQ);
		}
		default: {
			// ffAssert("ElementaryTransformation with wrong SPQR Type" ==
			// NULL);
			break;
		}
		}

		return null;
	}

	JJNode elementaryRSTransformation(final JJNode knoten, final JJNode vater) {
		// Debug.println( "Doing elementary RS Transformation" );

		final HashSet<JJNode> tmpS = new HashSet<>();
		tmpS.add(knoten);
		tmpS.add(vater);

		if (singleStepFlag)
			fenster.confirmStep("Elementary Transformation R S", tmpS);

		final JJNode newSource = getSource(vater);
		final JJNode newSink = getSink(vater);
		final JJNode rSource = getSource(knoten);
		final JJNode rSink = getSink(knoten);
		final JJNode eFather = knoten.father();

		final JJNode st1 = spqrInfo(knoten).getStEdge();
		final JJNode st2 = getTwin(st1);

		setSource(st1, newSource);
		setSink(st1, newSink);

		setSource(st2, newSource);
		setSink(st2, newSink);

		setSource(knoten, newSource);
		setSink(knoten, newSink);

		if (rSource != newSource) {
			final JJEdge start = eFather.father().firstOutEdge();
			final JJNode end = eFather.leftBrother().leftBrother();
			final JJEdge start2 = getTwin(eFather.father().firstSon()).inEdge();
			final JJEdge end2 = getTwin(end).inEdge();

			final JJEdge tmpE1 = st1.uncle().firstOutEdge();
			final JJEdge tmpE2 = st2.uncle().firstOutEdge();
			final JJNode newE = newENodes(newSource, rSource, null);

			if (end.inEdge() != start) {
				final JJNode newD = newSTree(newSource, rSource, newE);
				JJMisc.moveEdges(start, end.inEdge(), newD);
				JJMisc.moveEdges(start2, end2, getTwin(newD));
			} else {
				JJNode tmpN = start.getTarget().firstSon();
				if (tmpN == null)
					tmpN = start2.getTarget().firstSon();

				// ffAssert(tmpN != null);
				spqrTree.deleteEdge(tmpN.inEdge());
				spqrTree.addEdge(newE, tmpN);
			}

			spqrTree.addEdge(tmpE1, newE, JJGraph.before);
			spqrTree.addEdge(tmpE2, getTwin(newE), JJGraph.before);

			final JJNode tmpV1 = eFather.leftBrother();
			final JJNode tmpV2 = getTwin(eFather).leftBrother();
			spqrTree.deleteEdge(tmpV1.inEdge());
			spqrTree.deleteEdge(tmpV2.inEdge());
			spqrTree.addEdge(tmpE1, tmpV1, JJGraph.before);
			spqrTree.addEdge(tmpE2, tmpV2, JJGraph.before);
		}

		if (rSink != newSink) {
			final JJEdge end = eFather.father().lastOutEdge();
			final JJNode start = eFather.rightBrother().rightBrother();
			final JJEdge end2 = getTwin(eFather.father().lastSon()).inEdge();
			final JJEdge start2 = getTwin(start).inEdge();

			final JJEdge tmpE1 = st1.uncle().lastOutEdge();
			final JJEdge tmpE2 = st2.uncle().lastOutEdge();
			final JJNode newE = newENodes(rSink, newSink, null);

			if (start.inEdge() != end) {
				final JJNode newD = newSTree(rSink, newSink, newE);
				JJMisc.moveEdges(start.inEdge(), end, newD);
				JJMisc.moveEdges(start2, end2, getTwin(newD));
			} else {
				JJNode tmpN = end.getTarget().firstSon();
				if (tmpN == null)
					tmpN = end2.getTarget().firstSon();

				// ffAssert(tmpN != null);
				spqrTree.deleteEdge(tmpN.inEdge());
				spqrTree.addEdge(newE, tmpN);
			}

			spqrTree.addEdge(tmpE1, newE, JJGraph.after);
			spqrTree.addEdge(tmpE2, getTwin(newE), JJGraph.after);

			final JJNode tmpV1 = eFather.rightBrother();
			final JJNode tmpV2 = getTwin(eFather).rightBrother();
			spqrTree.deleteEdge(tmpV1.inEdge());
			spqrTree.deleteEdge(tmpV2.inEdge());
			spqrTree.addEdge(tmpE1, tmpV1, JJGraph.after);
			spqrTree.addEdge(tmpE2, tmpV2, JJGraph.after);
		}

		final JJNode grandFather = vater.father();
		spqrTree.deleteEdge(knoten.inEdge());
		spqrTree.deleteEdge(vater.inEdge());
		spqrTree.addEdge(grandFather, knoten);

		delSubTree(vater);

		return knoten;
	}

	JJNode elementaryRPTransformation(final JJNode knoten, final JJNode vater, final JJNode newQ) {
		// Debug.println( "Doing elementary RP Transformation" );

		final HashSet<JJNode> tmpS = new HashSet<>();
		tmpS.add(knoten);
		tmpS.add(vater);

		if (singleStepFlag)
			fenster.confirmStep("Elementary Transformation R P", tmpS);

		final JJNode eKnoten = knoten.father();
		final JJNode eKnotenTwin = getTwin(eKnoten);
		final JJNode dKnoten = eKnoten.father();
		final JJNode dKnotenTwin = eKnotenTwin.father();
		final JJNode dKnotenBrother = dKnoten.brother();
		final JJNode fKnoten = dKnoten.father();
		final JJNode fKnotenTwin = dKnotenTwin.father();

		// Eliminate one face of the P Node
		// ffAssert(dKnotenBrother != null);

		delSpqrNode(fKnoten);
		delSpqrNode(dKnoten);
		delSpqrNode(dKnotenTwin);
		delSpqrNode(eKnoten);
		delSpqrNode(eKnotenTwin);

		spqrTree.addEdge(fKnotenTwin, dKnotenBrother);

		// cut vater and attach knoten to grandFather

		final JJNode grandFather = vater.father();
		spqrTree.deleteEdge(vater.inEdge());
		spqrTree.addEdge(grandFather, knoten);

		// insert old JJMisc.father into knoten

		final JJNode newF = newFNode(knoten);

		JJNode stEdge = spqrInfo(knoten).getStEdge();
		// ffAssert(stEdge != null);

		JJNode stPath = stEdge.father();

		// We have to take care that the join vertex is still on an st-face

		if ((pathContainsGt(stPath.brother(), getSource(newQ)) == true)
				|| (pathContainsGt(stPath.brother(), getSink(newQ)) == true)) {

			// Debug.println( "Flipping R-Node" );

			stEdge = getTwin(stEdge);
			stPath = stEdge.father();
		}

		final JJNode stFace = stPath.father();

		spqrTree.deleteEdge(stPath.inEdge());
		spqrTree.addEdge(newF, stPath);
		final JJNode newD = newDNodes(newF, stFace);
		final JJNode newE = newENodes(getSource(stEdge), getSink(stEdge), newD);

		spqrTree.addEdge(newE, condenseNode(vater));

		// the old st-face is now blocking!

		// setBlocking(stFace, true);

		return knoten;
	}

	JJNode elementaryRRTransformation(final JJNode knoten, final JJNode vater, final JJNode newQ) {
		// Debug.println( "Doing elementary RR Transformation" );

		final HashSet<JJNode> tmpS = new HashSet<>();
		tmpS.add(knoten);
		tmpS.add(vater);

		if (singleStepFlag)
			fenster.confirmStep("Elementary Transformation R R", tmpS);

		final JJNode eFather = knoten.father();
		JJNode tmpD = findStPathWith(knoten, getSource(newQ));
		if (tmpD == null)
			tmpD = findStPathWith(knoten, getSink(newQ));

		// ffAssert(tmpD != null);

		replaceENodeByRNodeST(eFather, knoten, tmpD);
		return vater;
	}

	public void layout() {
		if (needsInit == true)
			if (!init())
				return;

		final JJSPQRLayout l = new JJSPQRLayout(window, this);
		l.layout();
	}

} // JJSPQR
