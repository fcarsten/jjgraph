/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

/**
 * JJMetisCoordParser.java
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
import java.util.Iterator;

import org.carsten.jjgraph.util.Debug;

public class JJMetisCoordParser extends StreamTokenizer implements JJCoordParser {

	public JJMetisCoordParser(final Reader r) {
		super(r);

		resetSyntax();
		whitespaceChars('\u0000', '\u0020');
		parseNumbers();
		wordChars('#', '#');
		eolIsSignificant(true);
	}

	public double ffNextNumber(final int c) throws IOException {

		if (c != StreamTokenizer.TT_NUMBER) {
			throw new IOException();
		}

		return nval;
	}

	@Override
	public boolean parse(final JJGraph graph) {
		final JJWindowList windows = graph.getWindows();

		final JJNode nodeMap[] = new JJNode[graph.getNumNodes() + 1];

		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			final JJNode tmpN = iter.next();
			nodeMap[tmpN.getValue()] = tmpN;
		}

		try {
			int c = nextToken();
			int nodeNum = 1;

			while (true) {
				final double x = 50.0 * ffNextNumber(c);
				final double y = 50.0 * ffNextNumber(nextToken());

				c = nextToken();
				double z = 0;

				if (c != StreamTokenizer.TT_EOL) {
					z = 50.0 * ffNextNumber(c);
					c = nextToken();
					if (c != StreamTokenizer.TT_EOL)
						Debug.println("Couldn't find eol");
				}

				// Debug.println("Moving node " + nodeNum + " to " + x + "," +y
				// + "," +z );
				for (final Object element : windows) {
					final JJGraphWindow window = (JJGraphWindow) element;
					final JJGraphicNode gn = nodeMap[nodeNum].getGraphicNode(window);

					if (gn != null) {
						window.moveNodeTo(gn, x, y, z);
					}
				}
				c = nextToken();
				nodeNum++;
				if (c == StreamTokenizer.TT_EOF)
					return true;
			}
		} catch (final IOException e) {
			Debug.println("Error in line " + lineno());
			return false;
		}
	}

} // JJMetisCoordParser
