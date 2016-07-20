/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

import java.awt.Color;
/**
 * JJRailParser.java
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

public class JJRailParser extends StreamTokenizer implements JJGraphParser {
	public JJRailParser(final Reader r) {
		super(r);
		resetSyntax();
		whitespaceChars('\u0000', '\u0020');
		parseNumbers();
		// eolIsSignificant(true);
		wordChars('A', 'Z');
		quoteChar('\"');
	}

	@Override
	public boolean parse(final JJGraph graph) {
		final HashMap<String, JJNode> nodeMap = new HashMap<>();
		int line = 0;
		JJNode currentNode = null;
		Color color = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());

		try {
			while (true) {
				final int c = nextToken();
				switch (c) {
				case StreamTokenizer.TT_EOF:
					// Debug.println("Loading succeeded");
					return true;
				case StreamTokenizer.TT_WORD: {
					if (sval.equals("EOL")) {
						line++;
						currentNode = null;
						color = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
					}
					break;
				}
				case '\"': {
					Debug.println("Station: " + sval);
					final String station = sval.trim().toLowerCase();
					JJNode newNode = nodeMap.get(station);
					if (newNode == null) {
						newNode = graph.addNode();
						newNode.setName(station);
						nodeMap.put(station, newNode);
					}
					if (currentNode != null) {
						final JJEdge tmpE = graph.addEdge(currentNode, newNode);
						tmpE.setValue(line);
						final JJWindowList fenster = graph.getWindows();
						for (final Object element : fenster) {
							final JJGraphWindow window = (JJGraphWindow) element;
							final JJGraphicEdge ge = tmpE.getGraphicEdge(window);
							ge.setColor(color);
						}
					}
					currentNode = newNode;
				}
					break;
				default:
				}
			}
		} catch (final IOException e) {
			Debug.println("Error in line " + lineno());
		}

		return false;
	}

} // JJRailParser
