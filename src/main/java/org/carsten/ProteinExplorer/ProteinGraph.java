/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.ProteinExplorer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * ProteinGraph.java
 *
 *
 * Created: Wed Feb 24 20:10:53 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import org.carsten.jjgraph.graph.JJGraphImpl;
import org.carsten.jjgraph.graph.JJGraphParser;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.util.Debug;

// Comment added by Falk

public class ProteinGraph extends JJGraphImpl {
	public ProteinGraph() {
		// super(CRCNode.class, JJEdgeImpl.class);
		setName("Protein Explorer");
	}

	// public JJGraphWindow createGraphic() {
	// JJGraphWindow fenster = new ProteinWindow(this);
	// windows.add(fenster);
	// return fenster;
	// }

	@Override
	public JJGraphWindow createGraphic(final JJGraphWindow w) {
		Debug.println("Creating graphic");

		JJGraphWindow fenster = null;
		if (w == null)
			fenster = new ProteinWindow(this);
		else
			fenster = new ProteinWindow(this, w);

		windows.add(fenster);
		Debug.println("Have " + windows.size() + " windows");

		return fenster;
	}

	@Override
	public JJGraphParser[] getParserForFileName(final String fileName, final InputStream graphStream)
			throws IOException {
		if (fileName.endsWith(".pt") || fileName.endsWith(".PT")) {
			final JJGraphParser parser[] = new JJGraphParser[1];
			parser[0] = new ProteinParser(new InputStreamReader(graphStream));
			return parser;
		}
		// if(fileName.endsWith(".xls")|| fileName.endsWith(".XLS"))
		// {
		// JJGraphParser parser[] = new JJGraphParser[1];
		// parser[0] = new ProteinExcelParser(graphStream);
		// return parser;
		// }
		return super.getParserForFileName(fileName, graphStream);
	}
}
