/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

/**
 * JJRomeParser.java
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
import java.util.HashMap;

import org.carsten.jjgraph.util.Debug;

public class JJRomeParser extends StreamTokenizer implements JJGraphParser {
	static int NODE_MODE = 1;
	static int EDGE_MODE = 2;
	// FastFileReader myReader;

	public JJRomeParser(final Reader r) {
		super(r);
		// myReader = (FastFileReader) r;

		resetSyntax();
		whitespaceChars('\u0000', '\u0020');
		parseNumbers();
		// eolIsSignificant(true);
		wordChars('#', '#');
	}

	@Override
	public boolean parse(final JJGraph graph) {
		int mode = NODE_MODE;
		final HashMap<Integer, JJNode> nodeMap = new HashMap<>();

		try {
			out: while (true) {
				final int c = nextToken();
				switch (c) {
				case StreamTokenizer.TT_EOF:
					// Debug.println("Loading succeeded");
					return true;
				case StreamTokenizer.TT_NUMBER:
					if (mode == NODE_MODE) {
						final JJNode tmpN = graph.addNode();

						tmpN.setValue((int) nval);
						tmpN.setName("" + (int) nval);
						nodeMap.put(new Integer((int) nval), tmpN);
						// Debug.print("\rNodes: " + graph.getNumNodes() +
						// " Edges: " + graph.getNumEdges());
						nextToken();
					} else {
						JJNode sourceN = null;
						JJNode targetN = null;

						nextToken();
						nextToken();
						sourceN = nodeMap.get(new Integer((int) nval));
						if (sourceN == null) {
							// Debug.println("Couldn't find source node \n" +
							// (int)nval);
							sourceN = graph.addNode();
							// Debug.print("\rNodes: " + graph.getNumNodes() +
							// " Edges: " + graph.getNumEdges());

						}

						nextToken();
						targetN = nodeMap.get(new Integer((int) nval));
						if (targetN == null) {
							targetN = graph.addNode();
							// Debug.println("Couldn't find target node \n" +
							// (int)nval);
							// Debug.print("\rNodes: " + graph.getNumNodes() +
							// " Edges: " + graph.getNumEdges());
						}
						graph.addEdge(sourceN, targetN);
						// Debug.print("\rNodes: " + graph.getNumNodes() +
						// " Edges: " + graph.getNumEdges());
					}
					break;
				case StreamTokenizer.TT_WORD:
					if (sval.equals("#")) {
						// Debug.println("Edge Mode ....");
						mode = EDGE_MODE;
					} else {
						Debug.println("Unknown word " + sval);
					}
					break;
				default:
					break out;
				}
			}
		} catch (final IOException e) {
			Debug.println("Error in line " + lineno());
		}

		return false;
	}

} // JJRomeParser
