/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

/**
 * JJScotchParser.java
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

public class JJScotchParser extends StreamTokenizer implements JJGraphParser {

	public JJScotchParser(final Reader r) {
		super(r);

		resetSyntax();
		whitespaceChars('\u0000', '\u0020');
		parseNumbers();
		wordChars('#', '#');
	}

	public int ffNextNumber(final int c) throws IOException {

		if (c != StreamTokenizer.TT_NUMBER) {
			throw new IOException();
		}
		return (int) nval;
	}

	@Override
	public boolean parse(final JJGraph graph) {
		JJNode nodeMap[] = null; // new HashMap();
		graph.setDirected(false);
		for (final JJGraphWindow jjGraphWindow : graph.getWindows())
			jjGraphWindow.showNodes(false);

		try {
			final int numNodes = ffNextNumber(nextToken());

			int numEdges = ffNextNumber(nextToken()); // Ignore total number of
														// edges

			nodeMap = new JJNode[numNodes + 1];
			Debug.println("Creating " + numNodes + " nodes.");

			for (int i = 0; i < numNodes; i++) {
				final JJNode tmpN = graph.addNode();
				tmpN.setValue(i);
				tmpN.setName("" + i);
				nodeMap[i] = tmpN;
			}

			while (true) {
				int c = nextToken(); // Node number

				if (c == StreamTokenizer.TT_EOF) {
					return true;
				}

				c = ffNextNumber(c);
				final JJNode currentNode = nodeMap[c];

				ffNextNumber(nextToken()); // Ignore 1

				numEdges = ffNextNumber(nextToken());

				for (int i = 0; i < numEdges; i++) {
					ffNextNumber(nextToken()); // Ignore 1
					final int targetId = ffNextNumber(nextToken());
					if (targetId > c) {
						final JJNode target = nodeMap[targetId];
						graph.addEdge(currentNode, target);
					}
				}

			}
		} catch (final IOException e) {
			Debug.println("Error in line " + lineno());
		}

		return false;
	}

} // JJScotchParser
