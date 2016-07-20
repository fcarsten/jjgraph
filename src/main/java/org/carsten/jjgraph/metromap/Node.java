/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.metromap;

import java.util.Vector;

/**
 * A node in a graph.
 */
public class Node {

	private String label;
	private double x, y;
	private Vector<Edge> edges;
	private Vector<Path> paths;
	private boolean placed;
	private int originalDegree;

	public Node() {
		this("Unnamed");
	}

	public Node(final String label) {
		this(label, 0.0, 0.0);
		placed = false;
	}

	public Node(final String label, final double x, final double y) {
		placed = true;
		this.x = x;
		this.y = y;
		this.label = label;
		edges = new Vector<>();
		paths = new Vector<>();
		originalDegree = 0;
	}

	public void addEdge(final Edge edge) {
		if (!edges.contains(edge)) {
			edges.addElement(edge);
		}
	}

	public void addPath(final Path path) {
		if (!paths.contains(path)) {
			paths.addElement(path);
		}
	}

	public Vector<Edge> getEdges() {
		return edges;
	}

	public java.lang.String getLabel() {
		return label;
	}

	public Vector<Path> getPaths() {
		return paths;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public void removeAllEdges() {
		edges = new Vector<>();
	}

	public void removeAllPaths() {
		paths = new Vector<>();
	}

	public void removeEdge(final Edge edge) {
		if (edges.contains(edge)) {
			edges.removeElement(edge);
		}
	}

	public void removePath(final Path path) {
		if (paths.contains(path)) {
			paths.removeElement(path);
		}
	}

	public void setLabel(final java.lang.String newLabel) {
		label = newLabel;
	}

	public void setX(final double newX) {
		x = newX;
		placed = true;
	}

	public void setY(final double newY) {
		y = newY;
		placed = true;
	}

	public int getDegree() {
		return edges.size();
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof Node)) {
			return false;
		}

		final Node node = (Node) o;

		return node.label.equals(label);
	}

	public boolean isPlaced() {
		return placed;
	}

	public void setPlaced(final boolean placed) {
		this.placed = placed;
	}

	public void storeOriginalDegree() {
		originalDegree = getDegree();
	}

	public int getOriginalDegree() {
		return originalDegree;
	}

	public Vector<Edge> getEdges(final Node node2) {
		final Vector<Edge> results = new Vector<>();

		for (int i = 0; i < edges.size(); i++) {
			final Edge edge = edges.elementAt(i);

			if (edge.getAdjacentNode(this) == node2) {
				results.addElement(edge);
			}
		}

		return results;
	}

	public boolean isConnectedTo(final Node node2) {
		return (getEdges(node2).size() > 0);
	}

	public boolean isConnectedTo(final Node node2, final Path path) {
		for (int i = 0; i < edges.size(); i++) {
			final Edge edge = edges.elementAt(i);

			if (edge.getPath() == path && edge.getAdjacentNode(this) == node2) {
				return true;
			}
		}

		return false;
	}

	public boolean isLinkNode() {
		final Vector<Edge> equivalenceSets = new Vector<>();
		final Vector<Integer> equivalenceCounts = new Vector<>();

		for (int i = 0; i < edges.size(); i++) {
			final Edge edge = edges.elementAt(i);
			boolean setFound = false;

			for (int j = 0; j < equivalenceSets.size(); j++) {
				final Edge edge2 = equivalenceSets.elementAt(j);

				if (edge.isEquivalentTo(edge2, false)) {
					setFound = true;
					final Integer count = equivalenceCounts.elementAt(j);
					equivalenceCounts.setElementAt(new Integer(count.intValue() + 1), j);
					break;
				}
			}

			if (setFound) {
				continue;
			}

			equivalenceSets.addElement(edge);
			equivalenceCounts.addElement(new Integer(1));

			if (equivalenceSets.size() > 2) {
				return false;
			}
		}

		if (equivalenceSets.size() != 2) {
			return false;
		}

		return true;

		// Integer count1 = (Integer)equivalenceCounts.elementAt(0);
		// Integer count2 = (Integer)equivalenceCounts.elementAt(1);
		//
		// return (count1.intValue() == count2.intValue());
	}

	public Vector<Object> getPathSets() {
		final Vector<Object> pathSets = new Vector<>();
		int highCount = 0;

		for (int i = 0; i < edges.size(); i++) {
			final Edge edge = edges.elementAt(i);
			boolean setFound = false;

			if (!edge.isInUse()) {
				continue;
			}

			for (int j = 0; j < pathSets.size(); j++) {
				@SuppressWarnings("unchecked")
				final Vector<Edge> pathSet = (Vector<Edge>) pathSets.elementAt(j);
				final Edge edge2 = pathSet.elementAt(0);

				if (edge.getPath() == edge2.getPath()) {
					pathSet.addElement(edge);
					if (pathSet.size() > highCount) {
						highCount = pathSet.size();
					}
					setFound = true;
					break;
				}
			}

			if (setFound) {
				continue;
			}

			final Vector<Object> pathSet = new Vector<>();
			pathSet.addElement(edge);
			pathSets.addElement(pathSet);
			if (pathSet.size() > highCount) {
				highCount = pathSet.size();
			}
		}

		pathSets.addElement(new Integer(highCount));

		return pathSets;
	}

	public boolean hasEquivalentEdge(final Edge edge, final boolean checkIntermediateNodes) {
		for (int i = 0; i < edges.size(); i++) {
			final Edge edge2 = edges.elementAt(i);

			if (edge.isEquivalentTo(edge2, checkIntermediateNodes)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public String toString() {
		return label;
	}

}
