/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.metromap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * A collection of paths making up a metromap graph.
 */
public class MetroMapGraph {

	private Vector<Node> nodes;
	private Vector<Edge> edges;
	private Vector<Path> paths;

	private String outputFilename = "";
	private boolean addCenterTags = false;

	private final long lastTime = 0;
	private final long currentTime = 0;

	public MetroMapGraph() {
		nodes = new Vector<>();
		edges = new Vector<>();
		paths = new Vector<>();
	}

	public void addPath(final Path path) {
		if (!paths.contains(path)) {
			paths.addElement(path);

			final Vector<Edge> pathEdges = path.getEdges();
			for (int i = 0; i < pathEdges.size(); i++) {
				final Edge edge = pathEdges.elementAt(i);
				Node a = edge.getA();
				Node b = edge.getB();

				if (!edges.contains(edge)) {
					edges.addElement(edge);
					if (a != null) {
						if (nodes.contains(a)) {
							a = nodes.elementAt(nodes.indexOf(a));
						} else {
							nodes.addElement(a);
						}
					}
					if (b != null) {
						if (nodes.contains(b)) {
							b = nodes.elementAt(nodes.indexOf(b));
						} else {
							nodes.addElement(b);
						}
					}

					edge.setNodes(a, b);
				}
			}
		}
	}

	void addEdge(final Edge edge) {
		edges.addElement(edge);
	}

	public void cleanGraph() {
		double minX = 0.0, maxX = 0.0, minY = 0.0, maxY = 0.0;
		boolean started = false;

		// Find (X, Y) coordinate ranges
		for (int i = 0; i < nodes.size(); i++) {
			final Node node = nodes.elementAt(i);

			if (!node.isPlaced()) {
				continue;
			}

			final double x = node.getX(), y = node.getY();

			if (!started || x < minX) {
				minX = x;
			}
			if (!started || x > maxX) {
				maxX = x;
			}
			if (!started || y < minY) {
				minY = y;
			}
			if (!started || y > maxY) {
				maxY = y;
			}

			started = true;
		}

		final double rangeX = maxX - minX, rangeY = maxY - minY;
		double newRangeX = 4000.0, newRangeY = 3550.0;
		final double aspect = rangeX / rangeY;

		if (aspect < (newRangeX / newRangeY)) {
			newRangeX = newRangeY / (rangeY / rangeX);
		} else {
			newRangeY = newRangeX / (rangeX / rangeY);
		}

		// Normalise (X, Y) ranges
		for (int i = 0; i < nodes.size(); i++) {
			final Node node = nodes.elementAt(i);

			if (!node.isPlaced()) {
				continue;
			}

			final double x = node.getX(), y = node.getY();

			node.setX((x - minX) / rangeX * newRangeX - (newRangeX / 2.0));
			node.setY((y - minY) / rangeY * newRangeY - (newRangeY / 2.0));
		}
	}

	protected Node findNode(final String label) {
		for (int i = 0; i < nodes.size(); i++) {
			final Node node = nodes.elementAt(i);

			if (node.getLabel().equals(label)) {
				return node;
			}
		}

		return null;
	}

	protected int getNodeID(final Node node) {
		for (int i = 0; i < nodes.size(); i++) {
			if (nodes.elementAt(i) == node) {
				return (i + 1);
			}
		}

		return -1;
	}

	public java.util.Vector<Edge> getEdges() {
		return edges;
	}

	public java.util.Vector<Node> getNodes() {
		return nodes;
	}

	public java.util.Vector<Path> getPaths() {
		return paths;
	}

	private void initialiseNodePositions() {
		final double rangeMinX = -0.5, rangeMaxX = 0.5;
		final double rangeMinY = -0.5, rangeMaxY = 0.5;

		for (int i = 0; i < nodes.size(); i++) {
			final Node node = nodes.elementAt(i);

			node.setX(Math.random() * (rangeMaxX - rangeMinX) + rangeMinX);
			node.setY(Math.random() * (rangeMaxY - rangeMinY) + rangeMinY);
		}
	}

	public void layout(final boolean timing, final String algorithm) throws IOException {
		// storeOriginalDegrees();

		if (algorithm.equals("alg1")) {
			removeDegree2Nodes();
		}

		final File mmapFile = new File("org.carsten.jjgraph.metromap-int.dot");
		mmapFile.delete();

		writeDOTFile("org.carsten.jjgraph.metromap-int.dot");

		final Runtime rt = Runtime.getRuntime();
		Process process = null;
		try {
			process = rt.exec(
					new String[] { "neato", "-Gepsilon=0.01", "-Gstart=rand", "org.carsten.jjgraph.metromap-int.dot" });
		} catch (final IOException e) {
			System.out.println("Can't exec neato.");
			e.printStackTrace();
			return;
		}

		final BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

		String text = "";
		while (true) {
			final String line = br.readLine();

			if (line == null) {
				break;
			}

			text += line + "\n";
		}

		// System.out.println("*****");
		// System.out.println(text);
		// System.out.println("*****");

		final FileOutputStream fos = new FileOutputStream("org.carsten.jjgraph.metromap.neato");
		final PrintWriter pw = new PrintWriter(fos);

		pw.print(text);

		pw.close();
		fos.close();

		readNeatoOutput("org.carsten.jjgraph.metromap.neato");

		if (!timing) {
			cleanGraph();

			if (algorithm.equals("alg1")) {
				String intermediateOutputFilename = "";

				final int index = outputFilename.lastIndexOf(".");

				if (index >= 0) {
					intermediateOutputFilename = outputFilename.substring(0, index) + ".intermediate"
							+ outputFilename.substring(index);
				} else {
					intermediateOutputFilename = outputFilename + ".intermediate";
				}

				if (!writeGMLFile(intermediateOutputFilename, addCenterTags)) {
					System.out.println("");
					System.out.println("Couldn't write to output file '" + intermediateOutputFilename + "'.");
					System.exit(1);
				}
			}
		}

		if (algorithm.equals("alg1")) {
			reinstituteDegree2Nodes();
		}
	}

	public boolean readTextFile(final String filename) {
		try {
			final FileInputStream fis = new FileInputStream(filename);
			final InputStreamReader isr = new InputStreamReader(fis);
			final BufferedReader br = new BufferedReader(isr);

			removeAllPaths();
			final Hashtable<String, Node> nodeHash = new Hashtable<>();

			boolean done = false;
			while (!done) {
				String line = "", intLine = "";
				do {
					intLine = br.readLine();
					if (intLine == null) {
						done = true;
					} else {
						line += intLine.trim();
					}
				} while (intLine != null && intLine.trim().length() > 0);

				line = line.trim();

				String pathName = "";
				final int colonIndex = line.indexOf(":");
				if (colonIndex >= 0) {
					pathName = line.substring(0, colonIndex).trim();
					line = line.substring(colonIndex + 1).trim();
				}

				if (line.startsWith("(")) {
					final int bracketIndex = line.indexOf(")");
					if (bracketIndex < 0) {
						continue;
					}

					line = line.substring(bracketIndex + 1).trim();
				}

				final Path currentPath = new Path();
				currentPath.setName(pathName);

				final Vector<Node> nodes = new Vector<>();
				final StringTokenizer tok = new StringTokenizer(line, ",");
				while (tok.hasMoreTokens()) {
					final String nodeLabel = tok.nextToken().trim();
					// Node node = findNode(nodeLabel);
					// if (node == null) {
					// node = currentPath.findNode(nodeLabel);
					//
					// if (node == null) {
					// node = new Node(nodeLabel);
					// }
					// }

					Node node = nodeHash.get(nodeLabel);

					if (node == null) {
						node = new Node(nodeLabel);
						nodeHash.put(node.getLabel(), node);
					}

					nodes.addElement(node);
				}

				for (int i = 1; i < nodes.size(); i++) {
					final Node nodeA = nodes.elementAt(i - 1);
					final Node nodeB = nodes.elementAt(i);

					// Allow a maximum of one edge on each path between any
					// given pair of nodes
					if (!nodeA.isConnectedTo(nodeB, currentPath)) {
						final Edge edge = new Edge(nodeA, nodeB);
						edge.setPath(currentPath);

						currentPath.addEdge(edge);
					} else {
						final int f = 0;
					}
				}

				addPath(currentPath);
			}

			br.close();
			isr.close();
			fis.close();
		} catch (final Exception e) {
			System.out.println("Exception in MetroMapGraph.readTextFile(String): " + e);
			return false;
		}

		return true;
	}

	public void removeAllPaths() {
		nodes = new Vector<>();
		edges = new Vector<>();
		paths = new Vector<>();
	}

	@Override
	public String toString() {
		String result = "";

		result += paths.size() + " paths consisting of " + edges.size() + " edges adjoining " + nodes.size()
				+ " nodes\n";

		for (int i = 0; i < nodes.size(); i++) {
			final Node node = nodes.elementAt(i);

			result += "Node #" + i + " ('" + node.getLabel() + "') at <" + node.getX() + ", " + node.getY() + ">\n";
		}

		return result;
	}

	public void assignColours() {
		final Hashtable<String, String> defaultColours = new Hashtable<>();
		defaultColours.put("Olympic Park I", "FFEB01");
		defaultColours.put("Olympic Park II", "FFEB01");
		defaultColours.put("Eastern Suburbs & Illawarra 1", "0479BA");
		defaultColours.put("Eastern Suburbs & Illawarra 2", "0479BA");
		defaultColours.put("South Coast 1", "0479BA");
		defaultColours.put("South Coast 2", "0479BA");
		defaultColours.put("Bankstown Line", "6F399A");
		defaultColours.put("Inner West 1", "6F399A");
		defaultColours.put("Inner West 2", "6F399A");
		defaultColours.put("Airport & East Hills 1", "006501");
		defaultColours.put("Airport & East Hills 2", "006501");
		defaultColours.put("South", "006501");
		defaultColours.put("Southern Highlands", "006501");
		defaultColours.put("Southern Highlands ROAD COACH", "707070");
		defaultColours.put("Cumberland", "BE006C");
		defaultColours.put("Carlingford Line", "1D3B8C");
		defaultColours.put("Western 1", "FFE300");
		defaultColours.put("Western 2", "FFE300");
		defaultColours.put("Blue Mountains", "FFE300");
		defaultColours.put("North Shore", "FFE300");
		defaultColours.put("Northern", "FE0405");
		defaultColours.put("Newcastle & Central Coast 1", "FE0405");
		defaultColours.put("Newcastle & Central Coast 2", "FE0405");
		defaultColours.put("Wyong Line", "FE0405");
		defaultColours.put("Hunter 1", "160076");
		defaultColours.put("Hunter 2", "160076");
		defaultColours.put("Papakura - Glen Innes", "006600");
		defaultColours.put("Waitakere", "6699CC");
		defaultColours.put("Papakura", "993366");

		for (int i = 0; i < paths.size(); i++) {
			final Path path = paths.elementAt(i);

			String colour = defaultColours.get(path.getName());
			if (colour == null) {
				colour = "000000";
			}

			path.setColour(colour);
		}
	}

	public boolean writeMETFile(final String filename) {
		try {
			final FileOutputStream fos = new FileOutputStream(filename);
			final PrintWriter pw = new PrintWriter(fos);

			assignColours();

			pw.println(paths.size() + "," + nodes.size());

			for (int i = 0; i < paths.size(); i++) {
				final Path path = paths.elementAt(i);
				final Vector<Edge> edges = path.getEdges();

				pw.println(path.getColour() + "," + path.getLength());

				for (int j = 0; j < edges.size(); j++) {
					final Edge edge = edges.elementAt(j);
					final Node a = edge.getA();
					final Node b = edge.getB();

					pw.print(getNodeID(a) + ",");

					if (j == (edges.size() - 1)) {
						pw.print(getNodeID(b));
					}
				}

				pw.println();
			}

			for (int i = 0; i < nodes.size(); i++) {
				final Node node = nodes.elementAt(i);

				pw.println((i + 1) + ",\"" + node.getLabel() + "\"," + node.getX() + "," + node.getY());
			}

			pw.close();
			fos.close();
		} catch (final Exception e) {
			System.out.println("Exception in MetroMapGraph.writeMETFile(String): " + e);
			return false;
		}

		return true;
	}

	public boolean writeGMLFile(final String filename, final boolean addCenterTags) {
		try {
			final FileOutputStream fos = new FileOutputStream(filename);
			final PrintWriter pw = new PrintWriter(fos);

			pw.println("#xxx");
			pw.println("graph [");
			pw.println(" directed 0");

			for (int i = 0; i < nodes.size(); i++) {
				final Node node = nodes.elementAt(i);

				if (!node.isPlaced()) {
					continue;
				}

				pw.println(" node [");
				pw.println("   id " + i);
				pw.println("   label \"" + node.getLabel() + "\"");
				pw.println("   graphics [");
				if (addCenterTags) {
					pw.println("    center [");
				}
				pw.println("     x " + node.getX());
				pw.println("     y " + node.getY());
				if (addCenterTags) {
					pw.println("    ]");
				}
				pw.println("   ]");
				pw.println(" ]");
			}

			for (int i = 0; i < edges.size(); i++) {
				final Edge edge = edges.elementAt(i);

				if (!edge.isInUse()) {
					continue;
				}

				pw.println(" edge [");
				pw.println("   source " + nodes.indexOf(edge.getA()));
				pw.println("   target " + nodes.indexOf(edge.getB()));
				pw.println(" ]");
			}

			pw.println("]");

			pw.close();
			fos.close();
		} catch (final Exception e) {
			System.out.println("Exception in MetroMapGraph.writeGMLFile(String, boolean): " + e);
			return false;
		}

		return true;
	}

	public boolean writeDOTFile(final String filename) {
		return writeDOTFile(filename, false);
	}

	public boolean writeDOTFile(final String filename, final boolean writeCoordinates) {
		try {
			final FileOutputStream fos = new FileOutputStream(filename);
			final PrintWriter pw = new PrintWriter(fos);

			pw.println("/* Generated by MetroMapGraph as input to Neato */");
			pw.println("graph org.carsten.jjgraph.metromap {");

			if (writeCoordinates) {
				for (int i = 0; i < nodes.size(); i++) {
					final Node node = nodes.elementAt(i);

					if (!node.isPlaced()) {
						continue;
					}

					pw.println("\t\"" + node.getLabel() + "\" [pos=\"" + node.getX() + "," + node.getY() + "\"];");
				}
			}

			for (int i = 0; i < edges.size(); i++) {
				final Edge edge = edges.elementAt(i);

				if (!edge.isInUse()) {
					continue;
				}

				final double minLength = edge.getMinimumLength();

				pw.println("\t\"" + edge.getA().getLabel() + "\" -- \"" + edge.getB().getLabel() + "\""
						+ ((minLength > Edge.MIN_EDGE_LENGTH) ? " [len = " + minLength + "];" : ";"));
			}

			pw.println("}");

			pw.close();
			fos.close();
		} catch (final Exception e) {
			System.out.println("Exception in MetroMapGraph.readTextFile(String): " + e);
			return false;
		}

		return true;
	}

	public boolean readNeatoOutput(final String filename) {
		try {
			String line = null;
			final FileInputStream fis = new FileInputStream(filename);
			final BufferedReader br = new BufferedReader(new InputStreamReader(fis));

			while (true) {
				line = br.readLine();

				if (line == null) {
					br.close();
					fis.close();

					return false;
				}

				if (line.trim().equals("graph org.carsten.jjgraph.metromap {")) {
					break;
				}
			}

			while (true) {
				line = br.readLine();

				if (line == null || line.trim().equals("}")) {
					break;
				}

				final String delims = "[]\" \t\r\n";
				StringTokenizer tok = new StringTokenizer(line, delims, true);

				String key = "";
				while (tok.hasMoreTokens()) {
					final String token = tok.nextToken();
					final boolean isDelim = delims.indexOf(token) >= 0;

					if (token.equals("[") || token.equals("]") || token.equals(";")) {
						break;
					}

					if (!isDelim) {
						key += token + " ";
					}
				}

				key = key.trim();
				if (key.length() <= 0 || key.equals("node") || key.equals("graph") || key.indexOf("--") >= 0) {
					continue;
				}

				String posString = null;
				while (tok.hasMoreTokens()) {
					final String token = tok.nextToken();
					if (token.equals(";")) {
						break;
					}

					if (token.startsWith("pos") && tok.hasMoreTokens()) {
						tok.nextToken();

						if (tok.hasMoreTokens()) {
							posString = tok.nextToken();
							break;
						}
					}
				}

				if (posString == null) {
					continue;
				}

				double posX = 0.0, posY = 0.0;
				tok = new StringTokenizer(posString, ",=\" \t\r\n", false);

				if (!tok.hasMoreTokens()) {
					continue;
				}
				String token = tok.nextToken();
				try {
					posX = Double.parseDouble(token);
				} catch (final Exception e) {
					continue;
				}
				if (!tok.hasMoreTokens()) {
					continue;
				}
				token = tok.nextToken();
				try {
					posY = Double.parseDouble(token);
				} catch (final Exception e) {
					continue;
				}

				final Node node = findNode(key);
				if (node == null) {
					continue;
				}

				node.setX(posX);
				node.setY(posY);
			}

			br.close();
			fis.close();
		} catch (final Exception e) {
			System.out.println("Exception in MetroMapGraph.readTextFile(String): " + e);
			return false;
		}

		return true;
	}

	public void removeNode(final Node node) {
		if (node == null) {
			return;
		}

		/*
		 * Vector nodeEdges = node.getEdges(); Edge firstEdge = (Edge)
		 * nodeEdges.elementAt(0); Node adjNode =
		 * firstEdge.getAdjacentNode(node); Node adjNode2 = null;
		 *
		 * // Check for multiple edge case boolean multipleEdges = false; for
		 * (int i = 1; i < nodeEdges.size(); i++) { Edge edge = (Edge)
		 * nodeEdges.elementAt(i); Node thisAdjNode =
		 * edge.getAdjacentNode(node);
		 *
		 * if (thisAdjNode == adjNode) { // Multiple edges between 2 nodes
		 * edge.split(this); multipleEdges = true; } else if(adjNode2 == null) {
		 * adjNode2 = thisAdjNode; } } if (multipleEdges) {
		 * firstEdge.split(this); return; }
		 *
		 * for (int i = 1; i < nodeEdges.size(); i++) { Edge edge = (Edge)
		 * nodeEdges.elementAt(i); adjNode2 = edge.getAdjacentNode(node);
		 *
		 * Edge newEdge = new Edge(adjNode, adjNode2);
		 * newEdge.addIntermediateNodesFromEdge(firstEdge);
		 * newEdge.addIntermediateNode(node);
		 * newEdge.addIntermediateNodesFromEdge(edge);
		 * edges.addElement(newEdge);
		 *
		 * adjNode2.removeEdge(edge); edge.setInUse(false); }
		 *
		 * firstEdge.setInUse(false); adjNode.removeEdge(firstEdge);
		 * node.removeAllEdges(); node.removeAllPaths();
		 */

		// Join edges on same path
		final Vector<Object> pathSets = node.getPathSets();
		final int highCount = ((Integer) pathSets.elementAt(pathSets.size() - 1)).intValue();
		pathSets.removeElementAt(pathSets.size() - 1);
		if (highCount < 2) {
			// Don't remove node unless it has degree 2+ on at least one path
			return;
		}
		for (int i = 0; i < pathSets.size(); i++) {
			@SuppressWarnings("unchecked")
			final Vector<Edge> pathSet = (Vector<Edge>) pathSets.elementAt(i);

			if (pathSet.size() != 2) {
				for (int j = 0; j < pathSet.size(); j++) {
					final Edge edge = pathSet.elementAt(j);

					final Node adjNode = edge.getAdjacentNode(node);
					node.removeEdge(edge);
					adjNode.removeEdge(edge);
					edge.setInUse(false);
					edge.setRestoreAfterLayout(true);
				}

				continue;
			}

			final Edge edge1 = pathSet.elementAt(0);
			final Edge edge2 = pathSet.elementAt(1);
			final Path path = edge1.getPath();

			final Node adjNode = edge1.getAdjacentNode(node);
			final Node adjNode2 = edge2.getAdjacentNode(node);

			final Node aa = edge1.getA();
			final Node bb = edge2.getB();

			if (adjNode == adjNode2) {
				continue;
			}

			final Edge newEdge = new Edge(adjNode, adjNode2);
			newEdge.setPath(path);
			newEdge.addIntermediateNodesFromEdge(edge1);
			newEdge.addIntermediateNode(node);
			newEdge.addIntermediateNodesFromEdge(edge2);

			adjNode.removeEdge(newEdge);
			adjNode2.removeEdge(newEdge);

			boolean mustSplit = false;
			if (adjNode.hasEquivalentEdge(newEdge, false) && !adjNode.hasEquivalentEdge(newEdge, true)) {
				mustSplit = true;
			}

			adjNode.addEdge(newEdge);
			adjNode2.addEdge(newEdge);

			edges.addElement(newEdge);

			edge1.setInUse(false);
			edge2.setInUse(false);

			adjNode.removeEdge(edge1);
			adjNode2.removeEdge(edge2);

			// Split edges if necessary
			if (mustSplit) {
				// Vector nodeEdges = adjNode.getEdges(adjNode2);
				// for (int j = 0; j < nodeEdges.size(); j++) {
				// Edge edge = (Edge) nodeEdges.elementAt(j);
				//
				// edge.split(this);
				// }
				newEdge.split(this);
			}
		}

		node.removeAllEdges();
	}

	public void removeDegree2Nodes() {
		final int numNodes = nodes.size();
		for (int i = 0; i < numNodes; i++) {
			final Node node = nodes.elementAt(i);
			System.out.print(node.getLabel());
			if (node.isLinkNode()) {
				removeNode(node);
				System.out.print("\t*");
			}
			System.out.println();
		}
	}

	public void reinstituteDegree2Nodes() {
		for (int i = 0; i < edges.size(); i++) {
			final Edge edge = edges.elementAt(i);

			if (edge.isRestoreAfterLayout()) {
				edge.setInUse(true);
			} else if (!edge.isInUse()) {
				continue;
			}

			edge.reinstituteIntermediateNodes(this);
		}
	}

	public void storeOriginalDegrees() {
		for (int i = 0; i < nodes.size(); i++) {
			final Node node = nodes.elementAt(i);

			node.storeOriginalDegree();
		}
	}

	public void printDegreeCounts() {
		final int degreeCounts[] = new int[11];
		for (int i = 0; i < nodes.size(); i++) {
			final Node node = nodes.elementAt(i);
			degreeCounts[node.getDegree()]++;
		}

		System.out.println(degreeCounts[0] + " degree 0 nodes");
		System.out.println(degreeCounts[1] + " degree 1 nodes");
		System.out.println(degreeCounts[2] + " degree 2 nodes");
		System.out.println(degreeCounts[3] + " degree 3 nodes");
		System.out.println(degreeCounts[4] + " degree 4 nodes");
		System.out.println(degreeCounts[5] + " degree 5 nodes");
		System.out.println(degreeCounts[6] + " degree 6 nodes");
		System.out.println(degreeCounts[7] + " degree 7 nodes");
		System.out.println(degreeCounts[8] + " degree 8 nodes");
		System.out.println(degreeCounts[9] + " degree 9 nodes");
		System.out.println(degreeCounts[10] + " degree 10 nodes");
	}

	public void presetGeometry(final String filename) {
		try (FileInputStream fis = new FileInputStream(filename);
				InputStreamReader isr = new InputStreamReader(fis);
				BufferedReader br = new BufferedReader(isr)) {

			String line = "";
			while (true) {
				line = br.readLine();

				if (line == null) {
					break;
				}

				if (line.length() <= 0) {
					continue;
				}

				System.out.println(line);

				final StringTokenizer tok = new StringTokenizer(line, ",\r\n");

				final String name = tok.nextToken().trim();
				final String NslashE = tok.nextToken().trim().toUpperCase();

				if (NslashE.length() <= 1 || NslashE.indexOf("N") < 0 || NslashE.indexOf("E") < 0
						|| NslashE.indexOf("/") < 0) {
					continue;
				}

				final String Nstr = NslashE.substring(0, NslashE.indexOf("N"));
				final String Estr = NslashE.substring(NslashE.indexOf("/") + 1, NslashE.indexOf("E"));

				final double N = Double.parseDouble(Nstr);
				final double E = Double.parseDouble(Estr);

				final Node node = findNode(name);

				if (node != null) {
					node.setX((E - 300.0) / 0.5);
					node.setY((6200.0 - N) / 0.5);
				} else {
					System.out.println("Can't find node '" + name + "'");
				}
			}
		} catch (final Exception e) {
			System.out.println("Exception occurred in MetroMapGraph.presetGeometry: " + e);
			e.printStackTrace();
		}
	}

	public static void process(final String filename, final String algorithm) throws IOException {
		final MetroMapGraph mmGraph = new MetroMapGraph();

		final int index = filename.lastIndexOf(".");
		if (index >= 0) {
			mmGraph.outputFilename = filename.substring(0, index) + "." + algorithm + ".GML";
		} else {
			mmGraph.outputFilename = filename + "." + algorithm + ".GML";
		}
		mmGraph.addCenterTags = false;

		if (!mmGraph.readTextFile(filename)) {
			return;
		}
		final long startTime = System.currentTimeMillis();
		mmGraph.layout(true, algorithm);
		final long endTime = System.currentTimeMillis();

		if (!mmGraph.readTextFile(filename)) {
			return;
		}
		mmGraph.layout(false, algorithm);

		if (!mmGraph.writeGMLFile(mmGraph.outputFilename, mmGraph.addCenterTags)) {
			return;
		}

		// mmGraph.printDegreeCounts();
		// System.out.println(mmGraph);

		System.out.println(filename + "\t" + algorithm + "\t" + (endTime - startTime));
	}

	public static void main(final String[] args) throws IOException {
		// process("c:\\metromap-output\\3\\data1.txt", "alg1");
		// process("c:\\metromap-output\\3\\data2.txt", "alg1");
		// process("c:\\metromap-output\\3\\data3.txt", "alg1");
		// process("c:\\metromap-output\\3\\data4.txt", "alg1");
		// process("c:\\metromap-output\\3\\2-straight-long.txt", "alg1");
		// process("c:\\metromap-output\\3\\2-straight-long-joined.txt",
		// "alg1");
		// process("c:\\metromap-output\\3\\2-straight-short.txt", "alg1");
		// process("c:\\metromap-output\\3\\2-straight-short-joined.txt",
		// "alg1");
		// process("c:\\metromap-output\\3\\4-circle.txt", "alg1");
		// process("c:\\metromap-output\\3\\6-circle-crossed.txt", "alg1");
		// process("c:\\metromap-output\\3\\multiedge1.txt", "alg1");
		// process("c:\\metromap-output\\3\\multiedge2.txt", "alg1");
		// process("c:\\metromap-output\\3\\cityrail.txt", "alg1");
		process("c:\\metromap-output\\3\\cityrail-detailed.txt", "alg1");

		/*
		 * process("c:\\metromap-output\\3\\data1.txt", "alg2");
		 * process("c:\\metromap-output\\3\\data2.txt", "alg2");
		 * process("c:\\metromap-output\\3\\data3.txt", "alg2");
		 * process("c:\\metromap-output\\3\\data4.txt", "alg2");
		 * process("c:\\metromap-output\\3\\2-straight-long.txt", "alg2");
		 * process("c:\\metromap-output\\3\\2-straight-long-joined.txt",
		 * "alg2"); process("c:\\metromap-output\\3\\2-straight-short.txt",
		 * "alg2");
		 * process("c:\\metromap-output\\3\\2-straight-short-joined.txt",
		 * "alg2"); process("c:\\metromap-output\\3\\4-circle.txt", "alg2");
		 * process("c:\\metromap-output\\3\\6-circle-crossed.txt", "alg2");
		 * process("c:\\metromap-output\\3\\multiedge1.txt", "alg2");
		 * process("c:\\metromap-output\\3\\multiedge2.txt", "alg2");
		 * process("c:\\metromap-output\\3\\cityrail.txt", "alg2");
		 */

		/*
		 * if (args.length < 2) { System.out.
		 * println("USAGE: java org.carsten.jjgraph.metromap.MetroMapGraph <inputdata.txt> <outputdata.gml> [addcentertags]"
		 * ); System.out.
		 * println("The optional 'addcentertags' parameter will cause a 'center' block to be added"
		 * ); System.out.
		 * println("into the .GML output inside the 'graphics' block, which seems to be necessary"
		 * );
		 * System.out.println("for correct viewing with the VGJ graph viewer.");
		 * System.exit(1); }
		 *
		 * MetroMapGraph mmGraph = new MetroMapGraph();
		 *
		 * if (args.length >= 3 && args[2].equalsIgnoreCase("addcentertags")) {
		 * mmGraph.addCenterTags = true; } mmGraph.outputFilename = args[1];
		 *
		 * if (!mmGraph.readTextFile(args[0])) { System.out.println("");
		 * System.out.println("Couldn't read input data from file '" + args[0] +
		 * "', please ensure valid file is given."); System.exit(1); }
		 * System.out.println( "Laying out metromap with " +
		 * mmGraph.nodes.size() + " nodes in " + mmGraph.edges.size() +
		 * " edges making up " + mmGraph.paths.size() + " paths."); long
		 * startTime = System.currentTimeMillis(); mmGraph.layout(); long
		 * endTime = System.currentTimeMillis();
		 *
		 * System.out.println("*** LAYOUT TOOK " + (endTime - startTime) +
		 * " MILLISECONDS ***");
		 *
		 * if (!mmGraph.writeGMLFile(mmGraph.outputFilename,
		 * mmGraph.addCenterTags)) { System.out.println("");
		 * System.out.println("Couldn't write to output file '" +
		 * mmGraph.outputFilename + "'."); System.exit(1); }
		 *
		 * System.out.
		 * println("Metromap layout complete, results written to file '" +
		 * mmGraph.outputFilename + "'.");
		 *
		 * Frame displayFrame = new Frame(); MetroMapGraphCanvas canvas = new
		 * MetroMapGraphCanvas(); displayFrame.setSize(500, 300);
		 * displayFrame.add(canvas); displayFrame.addWindowListener(new
		 * WindowAdapter() { public void windowClosing(WindowEvent e) {
		 * System.exit(0); } }); canvas.setLocation(0, 0); canvas.setSize(500,
		 * 300); canvas.setMmGraph(mmGraph); displayFrame.show();
		 *
		 */
	}

}
