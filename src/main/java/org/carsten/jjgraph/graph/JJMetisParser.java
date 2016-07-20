/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

/**
 * JJMetisParser.java
 *
 *
 * Created: Fri Feb 26 13:50:26 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;

import org.carsten.jjgraph.util.Debug;

public class JJMetisParser extends StreamTokenizer implements JJGraphParser {
	// FastFileReader myReader;

	public JJMetisParser(final Reader r) {
		super(r);
		// myReader = (FastFileReader) r;

		resetSyntax();
		whitespaceChars('\u0000', '\u0020');
		parseNumbers();
		eolIsSignificant(true);
	}

	public int ffNextNumber(final int c) throws IOException {

		if (c != StreamTokenizer.TT_NUMBER) {
			throw new IOException();
		}
		return (int) nval;
	}

	@Override
	public boolean parse(final JJGraph graph) {
		JJNode nodeMap[] = null;
		graph.setDirected(false);
		for (final JJGraphWindow jjGraphWindow : graph.getWindows())
			jjGraphWindow.showNodes(false);

		try {
			final int numNodes = ffNextNumber(nextToken());

			final int numEdges = ffNextNumber(nextToken()); // Ignore total
															// number of edges

			// myReader.progressLabel.setText("Creating " + numNodes + "
			// nodes.");
			Debug.println("Creating " + numNodes + " nodes.");

			nodeMap = new JJNode[numNodes + 2];

			for (int i = 1; i <= numNodes; i++) {
				final JJNode tmpN = graph.addNode();
				tmpN.setValue(i);
				tmpN.setName("" + i);
				nodeMap[i] = tmpN; // .add(new Integer(i), tmpN);
			}

			// myReader.progressLabel.setText("Loading edges.");
			Debug.println("Loading " + numEdges + " edges.");
			// Debug.println("");

			int aktuNode = 1;
			JJNode currentNode = null;

			while (true) {
				final int c = nextToken(); // Node number

				if (c == StreamTokenizer.TT_EOF) {
					// Debug.println("Loading succeeded");
					// Debug.println("Nodes: " + graph.getNumNodes() + ", Edges:
					// " +
					// graph.getNumEdges());
					return true;
				} else if (c == StreamTokenizer.TT_EOL) {
					// Debug.println("AktuNode " + aktuNode);
					if (aktuNode <= numNodes)
						currentNode = nodeMap[aktuNode++];
					else {
						// Debug.println("EOF expected abort loading.");
						// Debug.println("Nodes: " + graph.getNumNodes() + ",
						// Edges: " +
						// graph.getNumEdges());
						return true;
					}

				} else if ((c == StreamTokenizer.TT_NUMBER) && (currentNode != null)) {
					final int tmpI = ffNextNumber(c);

					if (tmpI >= aktuNode) {
						graph.addEdge(currentNode, nodeMap[ffNextNumber(c)]);
						// Debug.print("\rNodes: " + graph.getNumNodes() + ",
						// Edges: " +
						// graph.getNumEdges());
					}

				}
			}
		} catch (final IOException e) {
			Debug.println("Error in line " + lineno());
		}

		return false;
	}

} // JJMetisParser
