/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.metromap;

import java.util.Vector;

/**
 * An edge adjoining two nodes in a graph.
 */
public class Edge {

	public static final double NODE_WIDTH = 1.0;
	public static final double MIN_EDGE_LENGTH = 1.0;

	private boolean restoreAfterLayout = false;
	private boolean inUse = true;
	private Node a, b;
	private Vector<Node> intermediateNodes = new Vector<>();
	private Path path = null;

	public Edge() {
		a = b = null;
	}

	public Edge(final Node a, final Node b) {
		setNodes(a, b);
	}

	public Node getA() {
		return a;
	}

	public Node getAdjacentNode(final Node node) {
		if (a == node) {
			return b;
		} else if (b == node) {
			return a;
		}

		return null;
	}

	public Node getB() {
		return b;
	}

	public void setNodes(final Node a, final Node b) {
		if (this.a != null) {
			this.a.removeEdge(this);
		}
		if (this.b != null) {
			this.b.removeEdge(this);
		}

		if (a == null || b == null) {
			this.a = this.b = null;
		} else {
			this.a = a;
			this.b = b;
			this.a.addEdge(this);
			this.b.addEdge(this);
		}
	}

	public double getMinimumLength() {
		// if (a.getDegree() == 1 || b.getDegree() == 1) {
		// return MIN_EDGE_LENGTH;
		// }
		// else {
		return intermediateNodes.size() * NODE_WIDTH + (intermediateNodes.size() + 1) * MIN_EDGE_LENGTH;
		// }
	}

	public void addIntermediateNode(final Node node) {
		intermediateNodes.addElement(node);
	}

	public void addIntermediateNodesFromEdge(final Edge edge) {
		intermediateNodes.addAll(edge.intermediateNodes);
	}

	public void removeAllIntermediateNodes() {
		intermediateNodes = new Vector<>();
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof Edge)) {
			return false;
		}

		final Edge edge = (Edge) o;

		// return (edge.a.equals(a) && edge.b.equals(b)) || (edge.a.equals(b) &&
		// edge.b.equals(a));
		return edge == this;
	}

	public boolean isEquivalentTo(final Edge edge2, final boolean checkIntermediateNodes) {
		final boolean result = (edge2.a.equals(a) && edge2.b.equals(b)) || (edge2.a.equals(b) && edge2.b.equals(a));

		if (!checkIntermediateNodes || !result) {
			return result;
		}

		if (intermediateNodes.size() != edge2.intermediateNodes.size()) {
			return false;
		}

		for (int i = 0; i < intermediateNodes.size(); i++) {
			final Node node = intermediateNodes.elementAt(i);

			if (!edge2.intermediateNodes.contains(node)) {
				return false;
			}
		}

		return true;
	}

	public void reinstituteIntermediateNodes(final MetroMapGraph parent) {
		if (intermediateNodes.size() <= 0) {
			return;
		}

		final double x0 = a.getX(), xN = b.getX();
		final double y0 = a.getY(), yN = b.getY();
		final double N = intermediateNodes.size() + 1;
		final double xGradient = (xN - x0) / N;
		final double yGradient = (yN - y0) / N;
		Node previousNode = null;

		for (int i = 0; i < intermediateNodes.size(); i++) {
			final Node node = intermediateNodes.elementAt(i);

			node.setX(x0 + xGradient * (i + 1));
			node.setY(y0 + yGradient * (i + 1));

			if (previousNode != null) {
				final Edge edge = new Edge(previousNode, node);
				parent.addEdge(edge);
			}

			previousNode = node;
		}

		final Edge backEdge = new Edge(intermediateNodes.lastElement(), b);
		parent.addEdge(backEdge);
		b.removeEdge(this);
		b = intermediateNodes.elementAt(0);

		intermediateNodes = new Vector<>();
	}

	/**
	 * Returns the inUse.
	 *
	 * @return boolean
	 */
	public boolean isInUse() {
		return inUse;
	}

	/**
	 * Sets the inUse.
	 *
	 * @param inUse
	 *            The inUse to set
	 */
	public void setInUse(final boolean inUse) {
		this.inUse = inUse;
	}

	/**
	 * Returns true if edge has intermediate nodes.
	 */
	public boolean hasIntermediateNodes() {
		return !intermediateNodes.isEmpty();
	}

	/**
	 * Splits the edge into two, about the median intermediate node.
	 */
	public void split(final MetroMapGraph parent) {
		if (parent == null || !inUse || !hasIntermediateNodes()) {
			return;
		}

		final int medianNodeIndex = intermediateNodes.size() / 2;
		final Node medianNode = intermediateNodes.elementAt(medianNodeIndex);

		final Edge e1 = new Edge(a, medianNode);
		final Edge e2 = new Edge(medianNode, b);
		e1.setPath(path);
		e2.setPath(path);

		for (int i = 0; i < intermediateNodes.size(); i++) {
			if (i < medianNodeIndex) {
				e1.addIntermediateNode(intermediateNodes.elementAt(i));
			} else if (i > medianNodeIndex) {
				e2.addIntermediateNode(intermediateNodes.elementAt(i));
			}
		}

		inUse = false;
		intermediateNodes.clear();

		a.removeEdge(this);
		b.removeEdge(this);

		parent.addEdge(e1);
		parent.addEdge(e2);
	}

	/**
	 * @return Path
	 */
	public Path getPath() {
		return path;
	}

	/**
	 * Sets the path.
	 *
	 * @param path
	 *            The path to set
	 */
	public void setPath(final Path path) {
		this.path = path;
	}

	/**
	 * @return boolean
	 */
	public boolean isRestoreAfterLayout() {
		return restoreAfterLayout;
	}

	/**
	 * Sets the restoreAfterLayout.
	 *
	 * @param restoreAfterLayout
	 *            The restoreAfterLayout to set
	 */
	public void setRestoreAfterLayout(final boolean restoreAfterLayout) {
		this.restoreAfterLayout = restoreAfterLayout;
	}

	@Override
	public String toString() {
		return a.toString() + "->" + b.toString();
	}

}
