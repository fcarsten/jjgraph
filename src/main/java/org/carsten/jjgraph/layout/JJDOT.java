/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.layout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
/**
 * JJDOT.java
 *
 *
 * Created: Wed Dec  8 11:15:52 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import java.util.Collection;
import java.util.Iterator;

import org.carsten.jjgraph.graph.JJDotParser;
import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJNode;
import org.carsten.jjgraph.util.Debug;

public class JJDOT implements JJLayout {
	private final JJGraphWindow fenster;
	private final JJGraph graph;

	@Override
	public int allowsOptimize() {
		return 0;
	}

	public JJDOT(final JJGraphWindow f) {
		fenster = f;
		graph = f.getGraph();
	}

	@Override
	public void layout(final Collection<JJNode> c) {
		layout();
	}

	@Override
	public String getName() {
		return "Dot";
	}

	@Override
	public void layout() {
		File graphFile = null;
		File outFile = null;

		String fileName = null;

		try {
			graphFile = File.createTempFile("graph", ".dot");
			graphFile.deleteOnExit();
			outFile = File.createTempFile("graph", ".dot");
			outFile.deleteOnExit();
			fileName = graphFile.getCanonicalPath();
		} catch (final IOException e) {
			Debug.println("Couldn't create tmp file: " + e);
			return;
		}

		try {
			saveToFile(fileName);
		} catch (final IOException e) {
			Debug.println("Couldn't save graph to tmp file: " + e);
			return;
		}

		final Runtime rt = Runtime.getRuntime();

		Process p = null;

		try {
			final String args[] = { "dot", "-o" + outFile.getCanonicalPath(), graphFile.getCanonicalPath() };
			p = rt.exec(args);
		} catch (final IOException e) {
			Debug.println("Couldn't find dot server");
			return;
		}

		try {
			final int exit = p.waitFor();
		} catch (final InterruptedException e) {
		}

		parseResult(outFile);
	}

	public void parseResult(final File outFile) {
		try {
			final InputStream f = new FilterInputStream(new FileInputStream(outFile)) {
				@Override
				public int read() throws IOException {
					int c = super.read();
					if (c == '\\') {
						mark(2);
						final int d = super.read();
						if (d == '\n') {
							c = super.read();
						} else if (d == '\r') {
							c = super.read();
							c = super.read();
						} else {
							reset();
						}
					}
					return c;
				}

				@Override
				public int read(final byte b[], final int off, final int len) throws IOException {
					if (b == null) {
						throw new NullPointerException();
					} else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length)
							|| ((off + len) < 0)) {
						throw new IndexOutOfBoundsException();
					} else if (len == 0) {
						return 0;
					}

					int c = read();
					if (c == -1) {
						return -1;
					}
					b[off] = (byte) c;

					int i = 1;
					try {
						for (; i < len; i++) {
							c = read();
							if (c == -1) {
								break;
							}
							b[off + i] = (byte) c;
						}
					} catch (final IOException ee) {
					}
					return i;
				}
			};

			final JJDotParser parser = new JJDotParser(f);
			parser.parse(graph);

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void apply(final String s) {
		layout();
	}

	@Override
	public boolean canDo(final String s) {
		return s.equals("Dot");
	}

	JJNode nodeArray[];

	private void saveToFile(final String datei) throws IOException {
		final File tmpF = (new File(datei)).getCanonicalFile();
		final OutputStream theStream = new FileOutputStream(datei);
		final OutputStreamWriter ofDatei = new OutputStreamWriter(theStream);
		nodeArray = new JJNode[graph.getNumNodes()];
		Debug.println("Saving to " + datei);

		if (graph.isDirected())
			ofDatei.write("digraph d {\n");
		else
			ofDatei.write("graph d {\n");

		// Write Node

		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			final JJNode tmpN = iter.next();
			ofDatei.write(" " + tmpN.getSerialNumber() + " [ ];\n");
		}

		for (final Iterator<JJEdge> edgeIter = graph.edgeIterator(); edgeIter.hasNext();) {
			final JJEdge tmpEdge = edgeIter.next();
			ofDatei.write(" " + tmpEdge.getSource().getSerialNumber() + " -> " + tmpEdge.getTarget().getSerialNumber()
					+ " [ ];\n");
		}
		ofDatei.write("}\n");
		ofDatei.close();
	}

} // JJDOT
