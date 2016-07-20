/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

/**
 * JJEdge.java
 *
 *
 * Created: Fri Feb 26 13:08:56 1999
 *
 * @author Carsten Friedrich
 * @version
 */

public interface JJEdge {
	void init(JJNode n1, JJNode n2);

	// double getTransparency();
	// void setTransparency(double v);
	JJGraph getGraph();

	void revert();

	JJGraphicEdge getGraphicEdge(JJGraphWindow w);

	void setGraphicEdge(JJGraphicEdge v, JJGraphWindow w);

	double getWeight();

	void setWeight(double v);

	double getLength();

	void setLength(double v);

	String getName();

	void setName(String v);

	JJNode getTarget();

	JJNode getSource();

	// Color getColor();
	// void setColor(Color color);
	int getValue();

	void setValue(int i);

	JJEdge adjPred();

	JJEdge adjSucc();

	JJNode opposite(JJNode n);

	boolean contains(JJNode n);
}
