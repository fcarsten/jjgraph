/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.metromap;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Vector;

/**
 * A canvas for intermediate display of a metromap graph.
 */
public class MetroMapGraphCanvas extends java.applet.Applet {

	private MetroMapGraph mmGraph = null;

	public MetroMapGraphCanvas() {
		super();
	}

	public MetroMapGraph getMmGraph() {
		return mmGraph;
	}

	@Override
	public void paint(final Graphics g) {
		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth(), getHeight());

		if (mmGraph == null) {
			return;
		}

		final Vector<Node> nodes = mmGraph.getNodes();
		final Vector<Edge> edges = mmGraph.getEdges();

		// Find (X, Y) min/max of nodes
		double minNodeX = 9999999999.0, minNodeY = 9999999999.0;
		double maxNodeX = -9999999999.0, maxNodeY = -9999999999.0;
		for (int i = 0; i < nodes.size(); i++) {
			final Node node = nodes.elementAt(i);

			if (!node.isPlaced()) {
				continue;
			}

			final double nodeX = node.getX();
			final double nodeY = node.getY();

			if (nodeX < minNodeX) {
				minNodeX = nodeX;
			}
			if (nodeX > maxNodeX) {
				maxNodeX = nodeX;
			}
			if (nodeY < minNodeY) {
				minNodeY = nodeY;
			}
			if (nodeY > maxNodeY) {
				maxNodeY = nodeY;
			}
		}

		for (int i = 0; i < edges.size(); i++) {
			final Edge edge = edges.elementAt(i);
			final Node nodeA = edge.getA();
			final Node nodeB = edge.getB();

			final int nodeXA = scaleX(nodeA.getX(), minNodeX, maxNodeX);
			final int nodeYA = scaleY(nodeA.getY(), minNodeY, maxNodeY);
			final int nodeXB = scaleX(nodeB.getX(), minNodeX, maxNodeX);
			final int nodeYB = scaleY(nodeB.getY(), minNodeY, maxNodeY);

			g.setColor(Color.blue);
			g.drawLine(nodeXA, nodeYA, nodeXB, nodeYB);
		}

		for (int i = 0; i < nodes.size(); i++) {
			final Node node = nodes.elementAt(i);

			final int nodeX = scaleX(node.getX(), minNodeX, maxNodeX);
			final int nodeY = scaleY(node.getY(), minNodeY, maxNodeY);

			g.setColor(Color.black);
			g.fillOval(nodeX - 3, nodeY - 3, 6, 6);
			// g.setColor(Color.red);
			// g.drawString(node.getLabel(), nodeX + 5, nodeY - 6);
		}
	}

	private int scaleX(final double x, final double min, final double max) {
		double diff = max - min;

		if (diff == 0.0) {
			diff = 1.0;
		}

		return (int) (((x - min) * (getWidth() - 25)) / diff) + 10;
	}

	private int scaleY(final double y, final double min, final double max) {
		double diff = max - min;

		if (diff == 0.0) {
			diff = 1.0;
		}

		return (int) (((y - min) * (getHeight() - 30)) / diff) + 18;
	}

	public void setMmGraph(final MetroMapGraph newMmGraph) {
		mmGraph = newMmGraph;
	}

}
