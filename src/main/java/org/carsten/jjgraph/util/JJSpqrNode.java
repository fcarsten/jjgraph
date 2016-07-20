/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

/**
 * JJSpqrNode.java
 *
 *
 * Created: Mon Dec  6 15:09:50 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import java.util.HashSet;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphImpl;
import org.carsten.jjgraph.graph.JJNode;

public class JJSpqrNode {
	final static int NONE = 0;
	final static int S_NODE = 1;
	final static int P_NODE = 2;
	final static int Q_NODE = 3;
	final static int R_NODE = 4;
	final static int E_NODE = 5;
	final static int F_NODE = 6;
	final static int D_NODE = 7; // Pfade der F-Knoten
	final static int V_NODE = 8;

	private JJNode source;
	private JJNode sink;
	private int spqrType;

	// Fuer F_NODE

	private boolean blocking;
	private JJPoint barryCenter = new JJPoint(0.0, 0.0);

	// FUER V_NODE

	private JJNode realNode;

	// Fuer Q_NODE

	private JJEdge realEdge;

	// Fuer D_NODE, V_NODE, E_NODE

	private JJNode twin;

	// Fuer E_NODE

	private boolean stEdge;
	JJMatrix tm;

	// Fuer R_NODE und S_NODE

	private JJNode theStEdge;

	// Layoutinfo

	private double width;
	private double height;
	private final JJGraph sceleton;
	private final HashSet<JJNode> innerNodes;
	private final HashSet<JJNode> innerEdges;

	public JJSpqrNode() {
		innerNodes = new HashSet<>();
		innerEdges = new HashSet<>();
		sceleton = new JJGraphImpl();
		// spqrType = new JJSpqrType();

		source = null;
		sink = null;
		spqrType = NONE;
		blocking = false;
		realNode = null;
		realEdge = null;
		twin = null;
		stEdge = false;
		theStEdge = null;
		// barrryCenter = new JJPoint(0.0,0.0);
		tm = new JJMatrix(JJMatrix.einheitsMatrix);
		width = -1;
		height = -1;
	}

	public JJSpqrNode(final int t, final JJNode so, final JJNode si) {
		innerNodes = new HashSet<>();
		innerEdges = new HashSet<>();
		sceleton = new JJGraphImpl();
		// spqrType = new JJSpqrType();

		source = so;
		sink = si;
		spqrType = t;
		blocking = false;
		realNode = null;
		realEdge = null;
		twin = null;
		stEdge = false;
		theStEdge = null;
	}

	public void initLayout() {
		width = -1;
		height = -1;

		sceleton.clear();
		innerNodes.clear();
		innerEdges.clear();
		// barryCenter = new JJPoint(0,0);
		tm = new JJMatrix(JJMatrix.einheitsMatrix);
	}

	public void addInnerNode(final JJNode n) {
		innerNodes.add(n);
	}

	public void addInnerEdge(final JJNode n) {
		innerEdges.add(n);
	}

	public JJGraph getSceleton() {
		return sceleton;
	}

	public void setWidth(final double w) {
		width = w;
	}

	public double getWidth() {
		return width;
	}

	public void setBarryCenter(final JJPoint w) {
		barryCenter = new JJPoint(w);
	}

	public JJPoint getBarryCenter() {
		return new JJPoint(barryCenter);
	}

	public void setTM(final JJMatrix w) {
		tm = w;
	}

	public JJMatrix getTM() {
		return tm;
	}

	public void setHeight(final double h) {
		height = h;
	}

	public double getHeight() {
		return height;
	}

	public HashSet<JJNode> getInnerNodes() {
		return innerNodes;
	}

	public HashSet<JJNode> getInnerEdges() {
		return innerEdges;
	}

	public JJNode getSource() {
		return source;
	}

	public void setSource(final JJNode n) {
		source = n;
	}

	public JJNode getSink() {
		return sink;
	}

	public void setSink(final JJNode n) {
		sink = n;
	}

	public boolean isStEdge() {
		return stEdge;
	}

	public void setStEdge(final boolean n) {
		stEdge = n;
	}

	public JJNode getStEdge() {
		return theStEdge;
	}

	public void setStEdge(final JJNode n) {
		theStEdge = n;
	}

	public JJNode getTwin() {
		return twin;
	}

	public void setTwin(final JJNode n) {
		twin = n;
	}

	public int getSpqrType() {
		return spqrType;
	}

	public void setSpqrType(final int t) {
		spqrType = t;
	}

	public boolean isBlocking() {
		return blocking;
	}

	public void setBlocking(final boolean n) {
		blocking = n;
	}

	public JJNode getRealNode() {
		return realNode;
	}

	public void setRealNode(final JJNode n) {
		realNode = n;
	}

	public JJEdge getRealEdge() {
		return realEdge;
	}

	public void setRealEdge(final JJEdge n) {
		realEdge = n;
	}

	public void revert() {
		final JJNode tmpN = sink;
		sink = source;
		source = tmpN;
		if (realEdge != null)
			realEdge.revert();
		// graph_of(realEdge).rev_edge(realEdge);
	}

} // JJSpqrNode
