/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.metromap;

import java.util.Hashtable;
import java.util.Vector;

/**
 * A path of edges in a graph.
 */
public class Path {

	private Vector<Edge> edges;
	private Hashtable<Node, Integer> nodeStartDistances;
	private Hashtable<Node, Integer> nodeEndDistances;
	private boolean updated = true;
	private String colour = "000000";
	private String name = "";

	public Path() {
		edges = new Vector<>();
		nodeStartDistances = new Hashtable<>();
		nodeEndDistances = new Hashtable<>();
	}

	public void addEdge(final Edge edge) {
		if (edge == null) {
			return;
		}

		edges.addElement(edge);

		final Node a = edge.getA();
		final Node b = edge.getB();
		a.addPath(this);
		b.addPath(this);
		updated = false;
	}

	public Vector<Edge> getEdges() {
		return edges;
	}

	public Node getEndNode() {
		return (edges.size() > 0) ? edges.elementAt(edges.size() - 1).getB() : null;
	}

	public int getNodeEndDistance(final Node node) {
		if (!updated) {
			updateNodeDistances();
		}

		final Integer result = nodeEndDistances.get(node);
		return (result == null) ? 0 : result.intValue();
	}

	public int getNodeStartDistance(final Node node) {
		if (!updated) {
			updateNodeDistances();
		}

		final Integer result = nodeStartDistances.get(node);
		return (result == null) ? 0 : result.intValue();
	}

	public Node getStartNode() {
		return (edges.size() > 0) ? edges.elementAt(0).getA() : null;
	}

	public void removeAllEdges() {
		edges = new Vector<>();
		nodeStartDistances = new Hashtable<>();
		nodeEndDistances = new Hashtable<>();
		updated = true;
	}

	public void removeEdge(final Edge edge) {
		if (edges.contains(edge)) {
			edges.removeElement(edge);
		}

		updated = false;
	}

	public void updateNodeDistances() {
		for (int i = 0; i < edges.size(); i++) {
			final Edge edge = edges.elementAt(i);
			final Node a = edge.getA();
			final Node b = edge.getB();

			// Get current start and end distances for nodes A and B
			final Integer aStartDist = nodeStartDistances.get(a), aEndDist = nodeEndDistances.get(a);
			final Integer bStartDist = nodeStartDistances.get(b), bEndDist = nodeEndDistances.get(b);

			// Calculate new distances for nodes
			final int newAStartDist = i * 2, newAEndDist = (edges.size() * 2) - (i * 2) + 1;
			final int newBStartDist = i * 2 + 1, newBEndDist = (edges.size() * 2) - (i * 2);

			// Update distances
			if (aStartDist == null) {
				nodeStartDistances.put(a, new Integer(newAStartDist));
			}
			nodeEndDistances.put(a, new Integer(newAEndDist));
			if (bStartDist == null) {
				nodeStartDistances.put(b, new Integer(newBStartDist));
			}
			nodeEndDistances.put(b, new Integer(newBEndDist));
		}

		updated = true;
	}

	public Node findNode(final String nodeLabel) {
		for (int i = 0; i < edges.size(); i++) {
			final Edge edge = edges.elementAt(i);

			if (edge.getA().getLabel().equals(nodeLabel)) {
				return edge.getA();
			}

			if (edge.getB().getLabel().equals(nodeLabel)) {
				return edge.getB();
			}
		}

		return null;
	}

	/**
	 * @return String
	 */
	public String getColour() {
		return colour;
	}

	/**
	 * Sets the colour.
	 *
	 * @param colour
	 *            The colour to set
	 */
	public void setColour(final String colour) {
		this.colour = colour;
	}

	/**
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name
	 *            The name to set
	 */
	public void setName(final String name) {
		this.name = name;
	}

	public int getLength() {
		return edges.size() + 1;
	}

}
