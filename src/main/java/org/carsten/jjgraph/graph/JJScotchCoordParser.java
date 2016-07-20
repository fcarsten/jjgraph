/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

/**
 * JJScotchCoordParser.java
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

public class JJScotchCoordParser extends StreamTokenizer implements JJCoordParser {

	public JJScotchCoordParser(final Reader r) {
		super(r);

		resetSyntax();
		whitespaceChars('\u0000', ' ');
		// parseNumbers();
		wordChars(' ' + 1, '\u00ff');
	}

	public double ffNextNumber(final int c) throws IOException {

		if (c != StreamTokenizer.TT_NUMBER) {
			// Debug.println("Token : \"" + toString() +"\"");
			return Double.parseDouble(sval.toUpperCase());
		}
		return nval;
	}

	@Override
	public boolean parse(final JJGraph graph) {
		final JJWindowList windows = graph.getWindows();

		final JJNode nodeMap[] = new JJNode[graph.getNumNodes()];

		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			final JJNode tmpN = iter.next();
			nodeMap[tmpN.getValue()] = tmpN;
		}

		try {
			final int dimensions = (int) ffNextNumber(nextToken());
			final int numNodes = (int) ffNextNumber(nextToken());

			for (int i = 0; i < numNodes; i++) {
				final int nodeNum = (int) ffNextNumber(nextToken());
				final double x = 50.0 * ffNextNumber(nextToken());
				final double y = 50.0 * ffNextNumber(nextToken());
				double z = 0;

				if (dimensions == 3)
					z = 50.0 * ffNextNumber(nextToken());
				// Debug.println("Moving node " + nodeNum + " to " + x + "," +y
				// + "," +z );
				for (final Object element : windows) {
					final JJGraphWindow f = (JJGraphWindow) element;
					final JJGraphicNode gn = nodeMap[nodeNum].getGraphicNode(f);
					if (gn != null)
						f.moveNodeTo(gn, x, y, z);
				}
			}
		} catch (final IOException e) {
			Debug.println("Error in line " + lineno());
			return false;
		}

		return true;
	}

} // JJScotchCoordParser
