/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

import java.io.File;
import java.io.IOException;
import java.net.URL;
/**
 * JJGraph.java
 *
 *
 * Created: Wed Feb 24 20:10:53 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.carsten.jjgraph.util.JJUndo;

public interface JJGraph {
	int after = 0;
	int before = 1;

	void addStructureListener(JJGraphListener e);

	void removeStructureListener(JJGraphListener e);

	Iterator<JJEdge> edgeIterator();

	Iterator<JJNode> nodeIterator();

	Collection<JJNode> getNodes();

	Collection<JJEdge> getEdges();

	File getFile();

	void setFile(File v);

	boolean getDirected();

	boolean isDirected();

	void setDirected(boolean v);

	JJWindowList getWindows();
	// public void setWindow(JJGraphWindow v);

	boolean getMultipleEdges();

	void setMultipleEdges(boolean v);

	String getName();

	void setName(String v);

	int getNumNodes();

	int getNumEdges();

	JJNode addNode(JJNode n); // Add node n to the graph

	JJNode addNode(); // Create new node and add it to graph

	JJNode addNode(Class<? extends JJNode> c); // Create new node and add it to
												// graph

	JJEdge addEdge(JJEdge e); // Add edge e to the graph

	JJEdge addEdge(JJNode n1, JJNode n2); // Create an edge from n1
											// to n2 in the graph

	JJEdge addEdge(JJEdge e, JJNode n2, int dir);
	//
	// Add an edge from the source of e to node n2 "after" or "before"
	// e to the graph
	//

	void clear();

	void deleteNode(JJNode knoten);

	void deleteEdge(JJEdge kante);

	JJGraphWindow createGraphic();

	JJGraphWindow createGraphic(JJGraphWindow w);

	void parseFile(String fileName) throws IOException;

	void parseUrl(URL url) throws IOException;

	boolean areNeighbors(JJNode n1, JJNode n2);

	int getNumCuts();

	void makeConnected();

	JJNode findNodeWithName(String nameP);

	void saveToFile(JJGraphWindow w) throws IOException;

	void saveToFile(JJGraphWindow w, String fileName) throws IOException;

	void saveToFile(JJGraphWindow w, String datei, Set<JJNode> nodes) throws IOException;

	void numberNodes(int start);

	void setNodeValue(int v);

	void printArray(int dist[][]);

	int diameter();

	JJUndo getUndoManager();

	boolean getUndoRecording();

	void setUndoRecording(boolean v);

	JJNode getStartNode();

	void setStartNode(JJNode n);

	boolean isDirty();

	JJGraphTool getTool(String s);

	void addTool(String s, JJGraphTool o);

	boolean paste(java.io.Reader o) throws IOException;

	String saveGraphEdToString(JJGraphWindow w, Collection<JJNode> nodes) throws IOException;

	String saveGMLToString(JJGraphWindow w, Collection<JJNode> nodes, Collection<JJEdge> edges) throws IOException;

	void removeParallelEdges();

	void removeReflexiveEdges();

	void openSubtask(String s);

	void closeSubtask(String s);

} // JJGraph
