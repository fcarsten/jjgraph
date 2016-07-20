/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

import java.net.URL;
import java.util.Iterator;

public interface JJNode {

	JJGraph getGraph();

	Iterator<JJEdge> outIterator();

	Iterator<JJEdge> inIterator();

	Iterator<JJEdge> edgeIterator();

	JJEdge outEdge(int i);

	int outIndex(JJEdge e);

	JJEdge inEdge(int i);

	int inIndex(JJEdge e);

	// Tree operations

	JJEdge inEdge();

	JJNode firstSon();

	JJNode lastSon();

	JJNode father();

	JJNode uncle();

	JJNode grandFather();

	JJNode brother();

	JJNode leftBrother();

	JJNode rightBrother();

	// End Tree operations

	JJEdge firstInEdge();

	JJEdge firstOutEdge();

	JJEdge lastInEdge();

	JJEdge lastOutEdge();

	URL getUrl();

	void setUrl(URL u);

	int getCluster();

	void setCluster(int v);

	String getName();

	void setName(String v);

	int getValue();

	void setValue(int v);

	// Color getColor();
	// void setColor(Color v);
	int indeg();

	int outdeg();

	int deg();

	// House keeping don't use

	void addInEdge(JJEdge e);

	void delInEdge(JJEdge e);

	void addOutEdge(JJEdge e);

	void addOutEdgeAfter(JJEdge e, JJEdge e2);

	void addOutEdgeBefore(JJEdge e, JJEdge e2);

	void delOutEdge(JJEdge e);

	double getCoord();

	void setCoord(double v);

	JJGraphicNode getGraphicNode(JJGraphWindow w);

	void setGraphicNode(JJGraphicNode v, JJGraphWindow w);

	void init(JJGraph g, long sn);

	long getSerialNumber();

} // JJNode
