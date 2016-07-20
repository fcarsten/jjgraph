/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

/**
 * JJSPQRLayout.java
 *
 *
 * Created: Wed Dec  8 13:15:30 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphImpl;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJNode;

import Jama.Matrix;

public class JJSPQRLayout {
	private final JJGraphWindow window;
	private final JJSPQR spqr;
	private final JJGraph spqrTree;
	private final HashMap<JJNode, LinkedList<JJPoint>> virtualPos = new HashMap<>();

	public final static double MINDIST = 50.0;

	public JJSPQRLayout(final JJGraphWindow w, final JJSPQR s) {
		window = w;
		spqr = s;
		spqrTree = spqr.getSpqrTree();

		initLayout();
		buildInnerSets();
	}

	public void layout() {
		computeBBoxes();
		draw();
	}

	void initLayout() {
		// Debug.println( "Doing initLayout");

		for (final Iterator<JJNode> nodeIter = spqrTree.nodeIterator(); nodeIter.hasNext();) {
			final JJNode tmpN = nodeIter.next();

			spqr.spqrInfo(tmpN).initLayout();
		}

		//
		// Remove all bends from the edges
		//

		// toDo

		virtualPos.clear();

		for (final Iterator<JJNode> nodeIter = window.getGraph().nodeIterator(); nodeIter.hasNext();) {
			final JJNode tmpN = nodeIter.next();
			virtualPos.put(tmpN, new LinkedList<>());
		}

		window.removeBends();

		// dummyPos.clear();
	}

	void buildInnerSets() {
		// Debug.println( "Doing buildInnerSets");

		for (final Iterator<JJNode> nodeIter = spqrTree.nodeIterator(); nodeIter.hasNext();) {
			final JJNode tmpN = nodeIter.next();

			switch (spqr.spqrInfo(tmpN).getSpqrType()) {
			case JJSpqrNode.V_NODE: {
				spqr.spqrInfo(spqr.spqrFather(tmpN)).addInnerNode(spqr.spqrInfo(tmpN).getRealNode());
				break;
			}
			case JJSpqrNode.E_NODE: {
				if (tmpN.firstSon() != null)
					spqr.spqrInfo(spqr.spqrFather(tmpN)).addInnerEdge(tmpN);
				break;
			}
			default:
				break;
			}
		}
	}

	// Precondition: inner sets have been built

	void computeBBoxes() {
		// Debug.println( "Doing computeBBoxes");

		getWidth(spqr.getSpqrRoot());
		getHeight(spqr.getSpqrRoot());
	}

	void computeBBox(final JJNode n) {
		switch (spqr.spqrInfo(n).getSpqrType()) {
		case JJSpqrNode.S_NODE:
		case JJSpqrNode.P_NODE: {
			computeWidth(n);
			computeHeight(n);
			break;
		}
		case JJSpqrNode.R_NODE: {
			computeBBoxRNode(n);
			break;
		}
		default: {
			return;
		}
		}
	}

	void draw() {
		// Debug.println( "Doing draw");

		final JJMatrix m = new JJMatrix(JJMatrix.einheitsMatrix);

		double scaleX = 1.0;
		double scaleY = 1.0;

		if (getWidth(spqr.getSpqrRoot()) > 1400.0)
			scaleX = 1400.0 / getWidth(spqr.getSpqrRoot());

		if (getHeight(spqr.getSpqrRoot()) > 1400.0)
			scaleY = 1400.0 / getHeight(spqr.getSpqrRoot());

		JJMatrix.scale(m, scaleX, scaleY);

		move(spqr.getSNode(), JJPoint.mult(new JJPoint(0.0, getHeight(spqr.getSpqrRoot()) / 2.0), m));
		move(spqr.getTNode(), JJPoint.mult(new JJPoint(0.0, -getHeight(spqr.getSpqrRoot()) / 2.0), m));

		// rotate(m, 45);

		drawNode(spqr.getSpqrRoot(), m);
	}

	void drawNode(final JJNode n, final JJMatrix m) {
		switch (spqr.spqrInfo(n).getSpqrType()) {
		case JJSpqrNode.P_NODE: {
			drawPNode(n, new JJMatrix(m));
			break;
		}
		case JJSpqrNode.S_NODE: {
			drawSNode(n, new JJMatrix(m));
			break;
		}
		case JJSpqrNode.Q_NODE: {
			drawQNode(n, new JJMatrix(m));
			break;
		}
		case JJSpqrNode.R_NODE: {
			drawRNode(n, new JJMatrix(m));
			break;
		}
		default: {
			// ffAssert("Called drawNode with wrong JJNode Type" == NULL);
			break;
		}
		}
	}

	JJPoint getPosVNode(final JJNode n) {
		// ffAssert(spqr.spqrInfo(n).getSpqrType() == JJSpqrNode.V_NODE);
		final JJNode tmpN = spqr.spqrInfo(n).getRealNode();
		// GT_Common_Graphics* cg = aktuGraph.gt(tmpN).graphics();
		// return JJPoint(cg.x(), cg.y());
		return new JJPoint(tmpN.getGraphicNode(window).getCoords());
	}

	JJPoint getPos(final JJNode n) {
		// GT_Common_Graphics* cg = aktuGraph.gt(n).graphics();
		// return JJPoint(cg.x(), cg.y());
		return new JJPoint(n.getGraphicNode(window).getCoords());
	}

	void moveVNode(final JJNode n, final JJPoint p) {
		// ffAssert(spqr.spqrInfo(n).getSpqrType() == JJSpqrNode.V_NODE);
		final JJNode tmpN = spqr.spqrInfo(n).getRealNode();

		// Debug.println( "Moving JJNode " + tmpN.getName() + " to " + p);

		// GT_Common_Graphics* cg = aktuGraph.gt(tmpN).graphics();
		// cg.x(p.getX());
		// cg.y(p.getY());
		window.moveNodeTo(tmpN.getGraphicNode(window), p);
	}

	void move(final JJNode n, final JJPoint p) {
		// Debug.println( "Moving JJNode " + n.getName() + " to " + p);

		// GT_Common_Graphics* cg = aktuGraph.gt(n).graphics();
		// cg.x(p.getX());
		// cg.y(p.getY());
		window.moveNodeTo(n.getGraphicNode(window), p);
	}

	void drawSNode(final JJNode n, final JJMatrix m) {
		// Debug.println( "Drawing S node");
		// Debug.println( m);
		// Debug.println( m);
		//
		// Adapt the inner nodes of the sceleton to the new origin
		//

		for (final JJNode tmpN : spqr.spqrInfo(n).getInnerNodes()) {
			JJPoint tmpP = getPos(tmpN);
			tmpP = JJPoint.mult(tmpP, m); // = new JJPoint(tmpP.mult(m));
			move(tmpN, tmpP);
		}

		//
		// compute the origin of the edges of the sceleton and draw their graphs
		//

		JJMatrix.translate(m, 0.0, getHeight(n) / 2.0);

		final JJNode pfad = spqr.spqrInfo(n).getStEdge().father().brother();

		for (final Iterator<JJEdge> iter = pfad.outIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			final JJNode tmpN = tmpE.getTarget();// spqrTree.target(tmpE);
			if (spqr.spqrInfo(tmpN).getSpqrType() == JJSpqrNode.E_NODE) {
				JJMatrix.translate(m, 0.0, -getHeight(tmpN) / 2.0);
				drawNode(tmpN.firstSon(), m);
				JJMatrix.translate(m, 0.0, -getHeight(tmpN) / 2.0);
			}
		}
	}

	void drawPNode(final JJNode n, final JJMatrix m) {
		// Debug.println( "Drawing P node");
		// Debug.println( m);
		//
		// compute the origin of the edges of the sceleton and draw their graphs
		//

		final JJNode s = spqr.getSource(n);
		final JJNode t = spqr.getSink(n);
		// JJPoint sPoint(0.0, getHeight(n)/2.0 - (MINDIST/4.0)*
		// (virtualPos[s].length() + 1));
		// JJPoint tPoint(0.0, -getHeight(n)/2.0 +
		// (MINDIST/4.0)* (virtualPos[t].length() +1));
		final JJPoint sPoint = new JJPoint(0.0, getHeight(n) / 2.0 - (MINDIST / 4.0));
		final JJPoint tPoint = new JJPoint(0.0, -getHeight(n) / 2.0 + MINDIST / 4.0);

		JJMatrix.translate(m, -getWidth(n) / 2.0, 0.0);

		for (final JJNode tmpN : spqr.spqrInfo(n).getInnerEdges()) {

			JJMatrix.translate(m, getWidth(tmpN) / 2.0, 0.0);

			// Debug.println("m:" +m);
			// Debug.println("SPoint: "+ sPoint.mult(m));

			virtualPos.get(s).addFirst(JJPoint.mult(sPoint, m));
			virtualPos.get(t).addLast(JJPoint.mult(tPoint, m));

			drawNode(tmpN.firstSon(), m);
			JJMatrix.translate(m, getWidth(tmpN) / 2.0, 0.0);

			virtualPos.get(s).removeFirst(); // (virtualPos[s].first());
			virtualPos.get(t).removeLast(); // (virtualPos[t].last());
		}
	}

	void drawQNode(final JJNode n, final JJMatrix m) {
		// Debug.println( "Drawing Q node");
		// Debug.println( m);

		final JJNode s = spqr.getSource(n);
		final JJNode t = spqr.getSink(n);

		for (final Iterator<JJPoint> iter = virtualPos.get(t).listIterator(); iter.hasNext();) {
			final JJPoint tmpP = iter.next();
			addBend(spqr.spqrInfo(n).getRealEdge(), tmpP);
		}

		for (final Iterator<JJPoint> iter = virtualPos.get(s).listIterator(); iter.hasNext();) {
			final JJPoint tmpP = iter.next();
			addBend(spqr.spqrInfo(n).getRealEdge(), tmpP);
		}
	}

	void drawRNode(final JJNode n, final JJMatrix m) {
		// Debug.println( "Drawing R node");
		// Debug.println( m);
		//
		// Compute transfer Matrices for the inner Edges
		//

		// HashMap matrices;

		for (final JJNode tmpN : spqr.spqrInfo(n).getInnerEdges()) {

			final JJMatrix tmpM = new JJMatrix(m);
			JJPoint posS = new JJPoint(getPos(spqr.getSource(tmpN)));
			JJPoint posT = new JJPoint(getPos(spqr.getSink(tmpN)));

			if (spqr.getSource(tmpN) == spqr.getSource(n))
				posS = new JJPoint(0.0, getHeight(n) / 2.0);

			if (spqr.getSink(tmpN) == spqr.getSink(n))
				posT = new JJPoint(0.0, -getHeight(n) / 2.0);

			// JJPoint tmpP2 = posS.plus(posT.minus(posS).div(2.0));
			final JJPoint tmpP2 = JJPoint.minus(posT, posS).divA(2.0).plusA(posS);
			JJMatrix.translate(tmpM, tmpP2.getX(), tmpP2.getY());

			final double alpha = JJPoint.getAngle(JJPoint.minus(posT, posS));
			JJMatrix.rotate(tmpM, -alpha);

			// JJPoint tmpP = posT - posS;

			// double minD = minDist(posS, posT,
			// spqr.spqrInfo(n).getInnerEdges());

			// if(getWidth(tmpN) > minD)
			// scale(tmpM, minD/getWidth(tmpN), 1.0);

			adjustMatrix(tmpM, tmpN);

			// matrices[tmpN] = tmpM;
			spqr.spqrInfo(tmpN).setTM(tmpM);
			// tmpN.setName(tmpN.getName() + tmpM);

		}

		//
		// Adapt the inner nodes of the sceleton to the new origin
		//

		for (final JJNode tmpN : spqr.spqrInfo(n).getInnerNodes()) {

			JJPoint tmpP = getPos(tmpN);
			tmpP = JJPoint.mult(tmpP, m); // new JJPoint(tmpP.mult(m));
			move(tmpN, tmpP);
		}

		//
		// Draw supgraphs
		//

		for (final JJNode tmpN : spqr.spqrInfo(n).getInnerEdges()) {
			drawNode(tmpN.firstSon(), spqr.spqrInfo(tmpN).getTM());
		}
	}

	double getWidth(final JJNode n) {
		// Debug.println( "Doing getWidth");
		// ffAssert(n != null);

		switch (spqr.spqrInfo(n).getSpqrType()) {
		case JJSpqrNode.Q_NODE: {
			// Debug.println(""+ MINDIST/2.0);
			return MINDIST / 2.0;
		}
		case JJSpqrNode.E_NODE: {
			JJNode tmpN = n.firstSon();
			if (tmpN == null)
				tmpN = spqr.getTwin(n).firstSon();
			// ffAssert(tmpN != null);

			return getWidth(tmpN);
		}
		case JJSpqrNode.S_NODE:
		case JJSpqrNode.P_NODE:
		case JJSpqrNode.R_NODE: {
			if (spqr.spqrInfo(n).getWidth() == -1.0)
				computeBBox(n);

			// Debug.println(""+ spqr.spqrInfo(n).getWidth());
			return spqr.spqrInfo(n).getWidth();
		}
		default: {
			// ffAssert("Called getWidth() with wrong JJNode type" == NULL);
			break;
		}
		}

		return -1.0;
	}

	double getHeight(final JJNode n) {

		// Debug.println( "Doing getHeight");

		// ffAssert(n != null);

		switch (spqr.spqrInfo(n).getSpqrType()) {
		case JJSpqrNode.Q_NODE: {
			return MINDIST;
		}
		case JJSpqrNode.E_NODE: {
			JJNode tmpN = n.firstSon();
			if (tmpN == null)
				tmpN = spqr.getTwin(n).firstSon();
			// ffAssert(tmpN != null);

			return getHeight(tmpN);
		}
		case JJSpqrNode.S_NODE:
		case JJSpqrNode.P_NODE:
		case JJSpqrNode.R_NODE: {
			if (spqr.spqrInfo(n).getHeight() == -1.0)
				computeBBox(n);
			return spqr.spqrInfo(n).getHeight();
		}
		default: {
			// ffAssert("Called getHeight() with wrong JJNode type" == NULL);
			break;
		}
		}

		return -1.0;
	}

	void computeWidth(final JJNode n) {
		// Debug.println( "Doing computeWidth");

		// ffAssert(n != null);

		switch (spqr.spqrInfo(n).getSpqrType()) {
		case JJSpqrNode.S_NODE: {
			computeWidthSNode(n);
			break;
		}
		case JJSpqrNode.P_NODE: {
			computeWidthPNode(n);
			break;
		}
		case JJSpqrNode.R_NODE:
			// {
			// computeWidthRNode(n);
			// break;
			// }
		default: {
			// ffAssert("Called computeWidth with wrong JJNode type" == NULL);
			break;
		}
		}
	}

	void computeHeight(final JJNode n) {

		// Debug.println( "Doing computeHeight");

		// ffAssert(n != null);

		switch (spqr.spqrInfo(n).getSpqrType()) {
		case JJSpqrNode.S_NODE: {
			computeHeightSNode(n);
			break;
		}
		case JJSpqrNode.P_NODE: {
			computeHeightPNode(n);
			break;
		}
		case JJSpqrNode.R_NODE:
			// {
			// computeHeightRNode(n);
			// break;
			// }
		default: {
			// ffAssert("Called computeHeight with wrong JJNode type" == NULL);
			break;
		}
		}
	}

	void computeHeightSNode(final JJNode n) {

		// Debug.println( "Doing computeHeightSNode");

		final HashSet<JJNode> innerEdges = spqr.spqrInfo(n).getInnerEdges();
		double height = 0.0;

		for (final Object element : innerEdges) {
			final JJNode tmpN = (JJNode) element;
			height += getHeight(tmpN);
		}

		spqr.spqrInfo(n).setHeight(height);

		// Set Coordinates for the inner nodes

		final JJPoint aktuCoord = new JJPoint(0.0, height / 2.0);

		final JJNode path = spqr.spqrInfo(n).getStEdge().father().brother();

		for (final Iterator<JJEdge> iter = path.outIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			final JJNode tmpN = tmpE.getTarget(); // spqrTree.target(tmpE);
			if (spqr.spqrInfo(tmpN).getSpqrType() == JJSpqrNode.E_NODE) {
				// aktuCoord = new JJPoint(aktuCoord.minus(new JJPoint(0.0,
				// getHeight(tmpN))));
				aktuCoord.minusA(new JJPoint(0.0, getHeight(tmpN)));
			} else {
				moveVNode(tmpN, aktuCoord);
			}
		}
	}

	void computeHeightPNode(final JJNode n) {
		// Debug.println( "Doing computeHeightPNode");

		final HashSet<JJNode> innerEdges = spqr.spqrInfo(n).getInnerEdges();
		double height = MINDIST * 1.5;

		for (final JJNode element : innerEdges) {
			final JJNode tmpN = element;
			height = Math.max(getHeight(tmpN) + MINDIST / 2.0, height);
		}

		spqr.spqrInfo(n).setHeight(height);
	}

	void computeWidthSNode(final JJNode n) {

		// Debug.println( "Doing computeWidthSNode");

		final HashSet<JJNode> innerEdges = spqr.spqrInfo(n).getInnerEdges();
		double width = MINDIST / 2.0;

		for (final JJNode element : innerEdges) {
			final JJNode tmpN = element;
			width = Math.max(getWidth(tmpN) + 1.0, width);
		}

		spqr.spqrInfo(n).setWidth(width);
	}

	void computeWidthPNode(final JJNode n) {
		// Debug.println( "Doing computeWidthPNode");

		final HashSet<JJNode> innerEdges = spqr.spqrInfo(n).getInnerEdges();
		double width = 0.0;

		for (final JJNode jjNode : innerEdges) {
			final JJNode tmpN = jjNode;
			width += getWidth(tmpN);
		}

		spqr.spqrInfo(n).setWidth(width);
	}

	// void computeWidthRNode(JJNode n)
	// {
	// // Debug.println( "Doing computeWidthRNode");
	// // This is wrong and must be replaced by an actual computation according
	// to
	// // a tutte layout.
	// spqr.spqrInfo(n).setWidth(MINDIST/2.0);
	// }

	void computeBBoxRNode(final JJNode knoten) {

		// Debug.println( "Doing computeHeightRNode");

		final HashMap<JJNode, JJNode> tutteToG = new HashMap<>();
		final HashMap<JJNode, JJNode> gToTutte = new HashMap<>();
		final HashMap<JJEdge, JJNode> eToG = new HashMap<>();

		final HashMap<JJNode, JJPoint> tutteToCoord = new HashMap<>();
		// list<JJPoint> fixedPos;

		//
		// Building the JJGraph which is given to the tutte layout algorithm
		//

		// GT_Graph *tutteG = master.newGraph();
		// JJGraph tutte = tutteG.leda();
		final JJGraph tutte = new JJGraphImpl();

		// JJGraphWindow tutteF = null; //tutte.createGraphic();
		final JJGraphWindow tutteF = tutte.createGraphic();

		final JJNode tmpS = tutte.addNode();
		tmpS.setName("S");
		tutteToCoord.put(tmpS, new JJPoint(0.0, 1.0));
		if (tutteF != null) {
			tutteF.moveNodeTo(tmpS.getGraphicNode(tutteF), new JJPoint(0.0, 100.0));
		}

		tutteToG.put(tmpS, spqr.getSource(knoten));
		gToTutte.put(spqr.getSource(knoten), tmpS);

		final JJNode tmpT = tutte.addNode();
		tmpT.setName("T");
		tutteToCoord.put(tmpT, new JJPoint(0.0, -1.0));
		if (tutteF != null) {
			tutteF.moveNodeTo(tmpT.getGraphicNode(tutteF), new JJPoint(0.0, -100.0));
		}
		tutteToG.put(tmpT, spqr.getSink(knoten));
		gToTutte.put(spqr.getSink(knoten), tmpT);

		// tutte.new_edge(tmpS, tmpT);

		final HashSet<JJNode> innerNodes = spqr.spqrInfo(knoten).getInnerNodes();

		for (final JJNode element : innerNodes) {
			final JJNode loopN = element;
			final JJNode tmpN = tutte.addNode();
			tmpN.setName("i");
			tutteToG.put(tmpN, loopN);
			gToTutte.put(loopN, tmpN);
			tutteToCoord.put(tmpN, new JJPoint(0.0, 1.0));
			if (tutteF != null) {
				tutteF.moveNodeTo(tmpN.getGraphicNode(tutteF), new JJPoint(0.0, 100.0));
			}
		}

		final HashSet<JJNode> innerEdges = spqr.spqrInfo(knoten).getInnerEdges();
		for (final JJNode element : innerEdges) {
			final JJNode loopN = element;
			final JJEdge tmpE = tutte.addEdge(gToTutte.get(spqr.getSource(loopN)), gToTutte.get(spqr.getSink(loopN)));
			eToG.put(tmpE, loopN);
		}

		//
		// Building the set of fixed Nodes and giving them fixed coordinates.
		//

		final LinkedList<JJNode> fixedNodes = new LinkedList<>();
		fixedNodes.addFirst(tmpS);
		fixedNodes.addFirst(tmpT);

		JJNode outerPath = spqr.spqrInfo(knoten).getStEdge().father().brother();

		// double pathLength = (spqrTree.outdeg(outerPath)-1)/2.0;
		// double deltaPhi = 180.0/ (pathLength+1);
		// double aktuPhi= deltaPhi;
		double aktuPhi = 0.0;

		double unit = 1.0;

		for (final Iterator<JJEdge> iter = outerPath.outIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			final JJNode tmpN = tmpE.getTarget(); // spqrTree.target(tmpE);
			if (spqr.spqrInfo(tmpN).getSpqrType() == JJSpqrNode.E_NODE)
				unit += getHeight(tmpN);
		}

		unit = 180.0 / unit;

		for (final Iterator<JJEdge> iter = outerPath.outIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			final JJNode tmpN = tmpE.getTarget(); // spqrTree.target(tmpE);
			if (spqr.spqrInfo(tmpN).getSpqrType() == JJSpqrNode.E_NODE) {
				aktuPhi += getHeight(tmpN) * unit;
			} else if (spqr.spqrInfo(tmpN).getSpqrType() == JJSpqrNode.V_NODE) {
				final JJNode tutteN = gToTutte.get(spqr.spqrInfo(tmpN).getRealNode());
				fixedNodes.addFirst(tutteN);
				tutteN.setName(tutteN.getName() + " f1 " + (int) aktuPhi);

				// Debug.println( " Point ist " + tutteToCoord[tutteN] +endl;
				// Debug.println("Phi: " + aktuPhi);

				tutteToCoord.put(tutteN, JJPoint.rotate(tutteToCoord.get(tutteN), aktuPhi));
				if (tutteF != null) {
					tutteF.moveNodeTo(tutteN.getGraphicNode(tutteF), JJPoint.mult(tutteToCoord.get(tutteN), 100.0));
				}

				// Debug.println( "aktuPhi = " + aktuPhi + " Point ist ("
				// + tutteToCoord[tutteN].getX() + ","
				// + tutteToCoord[tutteN].getY() + ")" +endl;

				// aktuPhi += deltaPhi;
			}
		}

		outerPath = spqr.getTwin(spqr.spqrInfo(knoten).getStEdge()).father().brother();

		// pathLength = (spqrTree.outdeg(outerPath)-1)/2.0;
		// deltaPhi = -(180.0/ (pathLength+1));

		aktuPhi = 0.0;
		for (final Iterator<JJEdge> iter = outerPath.outIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			final JJNode tmpN = tmpE.getTarget(); // spqrTree.target(tmpE);
			if (spqr.spqrInfo(tmpN).getSpqrType() == JJSpqrNode.E_NODE)
				unit += getHeight(tmpN);
		}

		unit = 180.0 / unit;

		for (final Iterator<JJEdge> iter = outerPath.outIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			final JJNode tmpN = tmpE.getTarget(); // spqrTree.target(tmpE);
			if (spqr.spqrInfo(tmpN).getSpqrType() == JJSpqrNode.E_NODE) {
				aktuPhi -= getHeight(tmpN) * unit;
			} else if (spqr.spqrInfo(tmpN).getSpqrType() == JJSpqrNode.V_NODE) {
				final JJNode tutteN = gToTutte.get(spqr.spqrInfo(tmpN).getRealNode());
				fixedNodes.addFirst(tutteN);
				tutteN.setName(tutteN.getName() + " f2 " + (int) aktuPhi);

				// Debug.println( " Point ist " + tutteToCoord[tutteN] +endl;
				// Debug.println("Phi: " + aktuPhi);
				tutteToCoord.put(tutteN, JJPoint.rotate(tutteToCoord.get(tutteN), aktuPhi));
				if (tutteF != null) {
					tutteF.moveNodeTo(tutteN.getGraphicNode(tutteF), JJPoint.mult(tutteToCoord.get(tutteN), 100.0));
				}
				// Debug.println( "aktuPhi = " + aktuPhi + " Point ist ("
				// + tutteToCoord[tutteN].getX() + ","
				// + tutteToCoord[tutteN].getY() + ")" +endl;

				// aktuPhi += deltaPhi;
			}
		}

		// HashMap dummyNodes;
		// addDummyNodes(tutte, knoten, gToTutte, dummyNodes, tutteToCoord);

		double scale = 1.0;

		do_tutte(tutte, fixedNodes, tutteToCoord, eToG);

		// Debug.println("After Tutte");

		if (tutteF != null)
			for (final Iterator<JJNode> iter = tutte.nodeIterator(); iter.hasNext();) {
				final JJNode tmpN = iter.next();
				tutteF.moveNodeTo(tmpN.getGraphicNode(tutteF), JJPoint.mult(tutteToCoord.get(tmpN), 100.0));
				// Debug.println(tmpN.getName() + " : " +
				// ((JJPoint)tutteToCoord.get(tmpN)).mult(100.0));
			}

		computeBarryCenter(tutte, knoten, gToTutte, tutteToCoord);

		//
		// Now we scale the result in so that all subgraphs fit in.
		//

		for (final Object element : innerEdges) {
			final JJNode tmpN = (JJNode) element;
			final double should = getHeight(tmpN);
			final double is = JJPoint.dist(tutteToCoord.get(gToTutte.get(spqr.getSource(tmpN))),
					tutteToCoord.get(gToTutte.get(spqr.getSink(tmpN))));
			final double tmpScale = should / is;
			if (tmpScale > scale)
				scale = tmpScale;
		}

		//
		// Write Back Koordinates
		//

		for (final Object element : innerNodes) {
			final JJNode tmpN = (JJNode) element;
			move(tmpN, JJPoint.mult(tutteToCoord.get(gToTutte.get(tmpN)), scale));

			// GT_Common_Graphics* cg = tutteG.gt(gToTutte[tmpN]).graphics();
			// cg.x(tutteToCoord[gToTutte[tmpN]].getX()*scale);
			// cg.y(tutteToCoord[gToTutte[tmpN]].getY()*scale);
		}

		//
		// We adjust the BarryCenters to the new Scalation
		//

		for (final Iterator<JJEdge> iter = knoten.outIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			final JJNode tmpN = tmpE.getTarget();
			if (spqr.spqrInfo(tmpN).getBarryCenter() != null) {
				// spqr.spqrInfo(tmpN).
				// setBarryCenter(JJPoint.mult(spqr.spqrInfo(tmpN).getBarryCenter(),scale));
				spqr.spqrInfo(tmpN).getBarryCenter().multA(scale);
				// Debug.println(" Scale " + scale);
				// Debug.println(" New Barry center: " +
				// spqr.spqrInfo(tmpN).getBarryCenter());

				// if(tutteF != null){
				// JJNode newN = tutte.addNode();
				// tutteF.moveNodeTo(newN.getGraphicNode(),
				// spqr.spqrInfo(tmpN).getBarryCenter().getX(),
				// spqr.spqrInfo(tmpN).getBarryCenter().getY());
				// }

			}

			// JJNode newN = tutte.addNode();
			// GT_Common_Graphics* cg = tutteG.gt(newN).graphics();
			// cg.x(spqr.spqrInfo(tmpN).getBarryCenter().getX());
			// cg.y(spqr.spqrInfo(tmpN).getBarryCenter().getY());
		}

		if (tutteF != null) {
			tutteF.moveNodeTo(tmpS.getGraphicNode(tutteF), tutteToCoord.get(tmpS).getX() * scale,
					tutteToCoord.get(tmpS).getY() * scale);
			tutteF.moveNodeTo(tmpT.getGraphicNode(tutteF), tutteToCoord.get(tmpT).getX() * scale,
					tutteToCoord.get(tmpT).getY() * scale);
		}

		// GT_Common_Graphics* cg = tutteG.gt(tmpS).graphics();
		// cg.x(tutteToCoord[tmpS].getX()*scale);
		// cg.y(tutteToCoord[tmpS].getY()*scale);
		// cg = tutteG.gt(tmpT).graphics();
		// cg.x(tutteToCoord[tmpT].getX()*scale);
		// cg.y(tutteToCoord[tmpT].getY()*scale);

		spqr.spqrInfo(knoten).setHeight(2.0 * scale);
		spqr.spqrInfo(knoten).setWidth(2.0 * scale + (MINDIST / 2.0));
	}

	void addBend(final JJEdge e, final JJPoint p) {
		// GT_Polyline line = aktuGraph.gt(e).graphics().line();

		// Debug.println( "Adding bend at: " + p);
		e.getGraphicEdge(window).addBendFirst(p);

		// line.insert(GT_Point(p.getX(), p.getY()), line.first());
		// aktuGraph.gt(e).graphics().line (line);
	}

	double minDist(final JJPoint s, final JJPoint t, final HashSet<JJNode> innerEdges) {
		double minD = JJPoint.JJ_HUGE_VAL;
		// ??? porting
		// JJPoint s1 = s.plus(
		// ((t.minus(s)).divide(JJPoint.dist(s,t))).mult(MINDIST/2.0));
		// JJPoint t1 = t.plus(
		// ((s.minus(t)).divide(JJPoint.dist(s,t))).mult(MINDIST/2.0));

		final JJPoint s1 = JJPoint.minus(t, s).divA(JJPoint.dist(s, t)).multA(MINDIST / 2.0).plusA(s);

		final JJPoint t1 = JJPoint.minus(s, t).divA(JJPoint.dist(s, t)).multA(MINDIST / 2.0).plusA(t);

		for (final Object element : innerEdges) {
			final JJNode n2 = (JJNode) element;
			final JJPoint tmpS = getPos(spqr.getSource(n2));
			final JJPoint tmpT = getPos(spqr.getSink(n2));
			if ((tmpS != s) || (tmpT != t)) {
				// JJPoint s2 =
				// tmpS.plus(((tmpT.minus(tmpS)).divide(JJPoint.dist(tmpS,tmpT))
				// .mult(MINDIST/2.0)));
				// JJPoint t2 =
				// tmpT.plus((tmpS.minus(tmpT)).divide(JJPoint.dist(tmpS,tmpT))
				// .mult(MINDIST/2.0));
				final JJPoint s2 = JJPoint.minus(tmpT, tmpS).divA(JJPoint.dist(tmpS, tmpT)).multA(MINDIST / 2.0)
						.plusA(tmpS);

				final JJPoint t2 = JJPoint.minus(tmpS, tmpT).divA(JJPoint.dist(tmpS, tmpT)).multA(MINDIST / 2.0)
						.plusA(tmpT);

				minD = Math.min(minD, JJPoint.dist(s1, s2));
				minD = Math.min(minD, JJPoint.dist(t1, s2));
				minD = Math.min(minD, JJPoint.dist(s2, t1));
				minD = Math.min(minD, JJPoint.dist(t2, t1));
			}
		}

		// Debug.println( "Minimal Distance: " + minD);

		return minD;
	}

	void computeBarryCenter(final JJGraph tutte, final JJNode rNode, final HashMap<JJNode, JJNode> gToTutte,
			final HashMap<JJNode, JJPoint> tutteToCoord) {
		// Debug.println("Outdeg of R-node: " + rNode.outdeg());

		for (final Iterator<JJEdge> iter = rNode.outIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			final JJNode face = tmpE.getTarget();
			int numNodes = 2;
			final JJPoint center = tutteToCoord.get(gToTutte.get(spqr.getSink(face)));
			center.plusA(tutteToCoord.get(gToTutte.get(spqr.getSource(face))));

			for (final Iterator<JJEdge> iter2 = face.firstSon().outIterator(); iter2.hasNext();) {
				final JJEdge tmpE2 = iter2.next();
				final JJNode tmpN = tmpE2.getTarget(); // spqrTree.target(tmpE2);
				if (spqr.spqrInfo(tmpN).getSpqrType() == JJSpqrNode.V_NODE) {
					numNodes++;
					center.plusA(tutteToCoord.get(gToTutte.get(spqr.spqrInfo(tmpN).getRealNode())));
				}
			}

			for (final Iterator<JJEdge> iter2 = face.lastSon().outIterator(); iter2.hasNext();) {
				final JJEdge tmpE2 = iter2.next();
				final JJNode tmpN = tmpE2.getTarget(); // spqrTree.target(tmpE2);
				if (spqr.spqrInfo(tmpN).getSpqrType() == JJSpqrNode.V_NODE) {
					numNodes++;
					center.plusA(tutteToCoord.get(gToTutte.get(spqr.spqrInfo(tmpN).getRealNode())));
				}
			}
			final JJPoint tmpP = JJPoint.div(center, numNodes);
			// Debug.println("New center: " + tmpP);
			spqr.spqrInfo(face).setBarryCenter(tmpP);
		}
	}

	//
	// Adjsut Matrix tm to fit the sekeleton of edge node in its sector
	//

	void adjustMatrix(final JJMatrix tm, final JJNode knoten) {
		//
		// First we compute the Coordinates of the relevant points
		//
		// Debug.println("adjustMatrix");

		JJPoint source = getPos(spqr.getSource(knoten));
		JJPoint sink = getPos(spqr.getSink(knoten));

		if (spqr.getSource(knoten) == spqr.getSource(spqr.spqrFather(knoten)))
			source = new JJPoint(0.0, getHeight(spqr.spqrFather(knoten)) / 2.0);

		if (spqr.getSink(knoten) == spqr.getSink(spqr.spqrFather(knoten)))
			sink = new JJPoint(0.0, -getHeight(spqr.spqrFather(knoten)) / 2.0);

		// JJPoint center = new JJPoint((source.plus(sink).divide(2.0)));
		final JJPoint center = JJPoint.plus(source, sink).divA(2.0);

		final boolean needsFlip = true;

		JJPoint barry1, barry2;

		if (!spqr.isBlocking(knoten)) {
			// needsFlip = false;
			barry1 = spqr.spqrInfo(spqr.faceOf(spqr.getTwin(knoten))).getBarryCenter();
			barry2 = JJPoint.flipAB(barry1, source, sink);
		} else {
			barry1 = spqr.spqrInfo(spqr.faceOf(knoten)).getBarryCenter();
			if (!spqr.isBlocking(spqr.getTwin(knoten))) {
				// needsFlip = false;
				barry2 = JJPoint.flipAB(barry1, source, sink);
			} else {
				barry2 = spqr.spqrInfo(spqr.faceOf(spqr.getTwin(knoten))).getBarryCenter();
			}
		}

		final double maxX = getWidth(spqr.spqrFather(knoten)) / 2.0;
		final double minX = -getWidth(spqr.spqrFather(knoten)) / 2.0;
		final double maxY = getHeight(spqr.spqrFather(knoten)) / 2.0;
		final double minY = -getHeight(spqr.spqrFather(knoten)) / 2.0;

		// Debug.println( "BBox: " + new JJPoint(minX, minY) + " -- "
		// + new JJPoint(maxX, maxY));

		barry1 = new JJPoint(Math.max(minX, (Math.min(maxX, barry1.getX()))),
				Math.max(minY, (Math.min(maxY, barry1.getY()))));

		barry2 = new JJPoint(Math.max(minX, (Math.min(maxX, barry2.getX()))),
				Math.max(minY, (Math.min(maxY, barry2.getY()))));

		// Debug.println( "Before Trans: " + source + " " + sink + " " +
		// barry1 + " " + barry2);

		//
		// Now we normalise our points
		//

		source.minusA(center);
		sink.minusA(center);
		barry1.minusA(center);
		barry2.minusA(center);

		final double alpha = JJPoint.getAngle(sink);

		// Debug.println( "After Trans: " + source + " " + sink + " " + barry1
		// + " " + barry2);

		source = JJPoint.rotate(source, alpha);
		sink = JJPoint.rotate(sink, alpha);
		barry1 = JJPoint.rotate(barry1, alpha);
		barry2 = JJPoint.rotate(barry2, alpha);

		// Debug.println( "After Rotate: " + source + " " + sink + " " + barry1
		// + " " + barry2);

		//
		// Now we compute the intersection of the triangles
		//

		JJPoint intersect = new JJPoint(barry1);

		// if(needsFlip)
		// {
		barry2.setX(-barry2.getX());

		// Debug.println( "After Flip: " + source + " " + sink + " " + barry1
		// + " " + barry2);

		JJPoint b1 = new JJPoint(barry1);
		JJPoint b2 = new JJPoint(barry2);

		if (Math.abs(JJPoint.getAngle(JJPoint.minus(barry2, sink))) > Math
				.abs(JJPoint.getAngle(JJPoint.minus(barry1, sink))))
			b1 = barry2;

		if (Math.abs(JJPoint.getAngle(JJPoint.minus(barry1, source))) < Math
				.abs(JJPoint.getAngle(JJPoint.minus(barry2, source))))
			b2 = barry1;

		final Matrix A = new Matrix(2, 2);
		// vector rhs(2);
		final Matrix rhs = new Matrix(2, 1);

		A.set(0, 0, b1.getX() - sink.getX());
		A.set(0, 1, -(b2.getX() - source.getX()));
		A.set(1, 0, b1.getY() - sink.getY());
		A.set(1, 1, -(b2.getY() - source.getY()));
		rhs.set(0, 0, source.getX() - sink.getX());
		rhs.set(1, 0, source.getY() - sink.getY());
		Matrix erg = null;
		try {
			erg = A.solve(rhs);
		} catch (final java.lang.RuntimeException e) {
			Debug.println("Matrix");
			A.print(6, 3);
			Debug.println("is singular");
			return;
		}

		// intersect = source.plus( (b2.minus(source)).mult(erg.get(1,0)));

		intersect = JJPoint.minus(b2, source).multA(erg.get(1, 0)).plusA(source);

		// }
		// Debug.println( "New Intersection: " + intersect);

		final JJPoint rect1 = JJPoint.plus(source, intersect).divA(2.0);
		final JJPoint rect2 = JJPoint.plus(sink, intersect).divA(2.0);

		final double width = Math.abs(rect1.getX() + rect2.getX());
		final double height = JJPoint.dist(rect1, rect2);

		// double transY = ((rect1.plus(rect2)).divide(2.0)).getY();
		final double transY = JJPoint.plus(rect1, rect2).divA(2.0).getY();

		// Debug.println( "rect1 " + rect1 + " rect2 " + rect2 + " width : " +
		// width
		// + " height " + height + " transY " + transY);
		// Debug.println( "Scale: " + width/getWidth(knoten) + ","
		// + height/getHeight(knoten));

		JJMatrix.translate(tm, 0.0, transY);

		// double tmpS = Math.min(width/getWidth(knoten),
		// height/getHeight(knoten));
		// scale(tm, tmpS, tmpS);

		JJMatrix.scale(tm, width / getWidth(knoten), height / getHeight(knoten));
	}

	boolean do_tutte(final JJGraph G, final LinkedList<JJNode> fixed_nodes, final HashMap<JJNode, JJPoint> positions,
			final HashMap<JJEdge, JJNode> eToG)
	// does the actual computation. fixed_nodes and fixed_positions
	// contain the nodes with fixed positions.
	{
		// Debug.println( "Doing do_tutte");

		JJNode w;
		// JJEdge e;

		if (fixed_nodes.size() == G.getNumNodes())
			return true;

		// mark fixed nodes and set their positions in a
		final HashMap<JJNode, Boolean> fixed = new HashMap<>();
		final Double zero = new Double(0.0);

		for (final Iterator<JJNode> nodeIter = G.nodeIterator(); nodeIter.hasNext();) {
			final JJNode tmpN = nodeIter.next();
			fixed.put(tmpN, Boolean.FALSE);
		}

		for (final Iterator<JJNode> iter = fixed_nodes.listIterator(); iter.hasNext();) {
			final JJNode v = iter.next();
			fixed.put(v, Boolean.TRUE);
		}

		// all nodes have fixed positions - nothing left to do

		// collect other nodes
		// and compute their wieght

		final HashMap<JJNode, Double> sumWeight = new HashMap<>();
		for (final Iterator<JJNode> nodeIter = G.nodeIterator(); nodeIter.hasNext();) {
			final JJNode tmpN = nodeIter.next();
			sumWeight.put(tmpN, zero);
		}

		final LinkedList<JJNode> other_nodes = new LinkedList<>();

		for (final Iterator<JJNode> nodeIter = G.nodeIterator(); nodeIter.hasNext();) {
			final JJNode v = nodeIter.next();

			for (final Iterator<JJEdge> iter = v.outIterator(); iter.hasNext();) {
				final JJEdge loopE = iter.next();
				sumWeight.put(v, new Double(sumWeight.get(v).doubleValue() + 1.0 / getHeight(eToG.get(loopE))));

				// sumWeight[v] += 1.0/getHeight(eToG[loopE]);
			}

			for (final Iterator<JJEdge> iter = v.inIterator(); iter.hasNext();) {
				final JJEdge loopE = iter.next();
				sumWeight.put(v, new Double(sumWeight.get(v).doubleValue() + 1.0 / getHeight(eToG.get(loopE))));
			}

			if (fixed.get(v) == Boolean.FALSE) {
				other_nodes.addLast(v);
			}
		}

		final HashMap<JJNode, Integer> ind = new HashMap<>();

		int i = 0;

		for (final Iterator<JJNode> iter = other_nodes.listIterator(); iter.hasNext();) {
			final JJNode v = iter.next();
			ind.put(v, new Integer(i++));
		}

		final int n = other_nodes.size(); // #other nodes
		Matrix coord = null; // = new Matrix(n,1); // coordinates (first x then
								// y)
		final Matrix rhs = new Matrix(n, 1); // right hand side
		final Matrix A = new Matrix(n, n); // equations

		for (final Iterator<JJNode> iter = other_nodes.listIterator(); iter.hasNext();) {
			final JJNode v = iter.next();
			// double one_over_d = 1.0/double(G.degree(v));
			final double one_over_d = 1.0 / sumWeight.get(v).doubleValue();

			for (final Iterator<JJEdge> iter2 = v.outIterator(); iter2.hasNext();) {
				final JJEdge e = iter2.next();
				w = e.getTarget(); // (v == source(e)) ? target(e) : source(e);

				if (fixed.get(w) == Boolean.FALSE) {
					A.set(ind.get(v).intValue(), ind.get(w).intValue(), (1.0 / getHeight(eToG.get(e))) * one_over_d);
				}
			}

			for (final Iterator<JJEdge> iter2 = v.inIterator(); iter2.hasNext();) {
				final JJEdge e = iter2.next();
				w = e.getSource(); // (v == source(e)) ? target(e) : source(e);

				if (fixed.get(w) == Boolean.FALSE) {
					A.set(ind.get(v).intValue(), ind.get(w).intValue(), (1.0 / getHeight(eToG.get(e))) * one_over_d);
				}
			}

			A.set(ind.get(v).intValue(), ind.get(v).intValue(), -1);
		}

		if (A.det() == 0)
			return false;

		// compute right hand side for x coordinates
		for (final Iterator<JJNode> iter = other_nodes.listIterator(); iter.hasNext();) {
			final JJNode v = iter.next();
			final int i1 = ind.get(v).intValue();
			rhs.set(i1, 0, 0.0);
			// double one_over_d = 1.0/double(G.degree(v));
			final double one_over_d = 1.0 / sumWeight.get(v).doubleValue(); // sumWeight[v];

			for (final Iterator<JJEdge> iter2 = v.outIterator(); iter2.hasNext();) {
				final JJEdge e = iter2.next();
				w = e.getTarget(); // (v == source(e)) ? target(e) : source(e);

				if (fixed.get(w) == Boolean.TRUE) {
					// rhs[ind[v]] -= (1.0/getHeight(eToG[e])) * (one_over_d*
					// positions[w].getX());

					rhs.set(i1, 0,
							rhs.get(i1, 0) - (1.0 / getHeight(eToG.get(e))) * (one_over_d * positions.get(w).getX()));
				}
			}

			for (final Iterator<JJEdge> iter2 = v.inIterator(); iter2.hasNext();) {
				final JJEdge e = iter2.next();
				w = e.getSource(); // (v == source(e)) ? target(e) : source(e);

				if (fixed.get(w) == Boolean.TRUE) {
					// int i1 = ((Integer)ind.get(v)).intValue();

					rhs.set(i1, 0,
							rhs.get(i1, 0) - (1.0 / getHeight(eToG.get(e))) * (one_over_d * positions.get(w).getX()));
				}
			}
		}

		// compute x coordinates
		coord = A.solve(rhs);

		for (final Iterator<JJNode> iter = other_nodes.listIterator(); iter.hasNext();) {
			final JJNode v = iter.next();
			final JJPoint tmpP = positions.get(v);
			tmpP.setX(coord.get(ind.get(v).intValue(), 0));
			// positions[v]= tmpP;
		}

		// compute right hand side for y coordinates
		for (final Iterator<JJNode> iter = other_nodes.listIterator(); iter.hasNext();) {
			final JJNode v = iter.next();
			rhs.set(ind.get(v).intValue(), 0, 0);
			// double one_over_d = 1.0/double(G.degree(v));
			final double one_over_d = 1.0 / sumWeight.get(v).doubleValue(); // sumWeight[v];

			for (final Iterator<JJEdge> iter2 = v.outIterator(); iter2.hasNext();) {
				final JJEdge e = iter2.next();
				w = e.getTarget(); // (v == source(e)) ? target(e) : source(e);

				if (fixed.get(w) == Boolean.TRUE) {
					// rhs[ind[v]] -= (1.0/getHeight(eToG[e])) * (one_over_d*
					// positions[w].getY());
					final int i1 = ind.get(v).intValue();

					rhs.set(i1, 0,
							rhs.get(i1, 0) - (1.0 / getHeight(eToG.get(e))) * (one_over_d * positions.get(w).getY()));
				}
			}

			for (final Iterator<JJEdge> iter2 = v.inIterator(); iter2.hasNext();) {
				final JJEdge e = iter2.next();
				w = e.getSource(); // (v == source(e)) ? target(e) : source(e);

				if (fixed.get(w) == Boolean.TRUE) {
					final int i1 = ind.get(v).intValue();

					rhs.set(i1, 0,
							rhs.get(i1, 0) - (1.0 / getHeight(eToG.get(e))) * (one_over_d * positions.get(w).getY()));
				}
			}
		}

		// compute x coordinates
		coord = A.solve(rhs);

		for (final Iterator<JJNode> iter = other_nodes.listIterator(); iter.hasNext();) {
			final JJNode v = iter.next();
			final JJPoint tmpP = positions.get(v);
			tmpP.setY(coord.get(ind.get(v).intValue(), 0));
			// Debug.println(v.getName() + " " + tmpP.mult(100));
			// Debug.println(v.getName() + " " +
			// ((JJPoint)positions.get(v)).mult(100.0));

			// positions[v]= tmpP;
		}

		return true;
	}

} // JJSPQRLayout
