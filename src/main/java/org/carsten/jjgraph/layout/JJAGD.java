/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.layout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
/**
 * JJAGD.java
 *
 *
 * Created: Wed Dec  8 11:15:52 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJGraphicNode;
import org.carsten.jjgraph.graph.JJNode;
import org.carsten.jjgraph.util.Debug;

public class JJAGD implements JJLayout {
	private final JJGraphWindow fenster;
	private final JJGraph graph;
	static File optionFile = null;
	static List<String> algoList;

	public static List<String> getAlgorithms() {
		return algoList;
	}

	static {
		optionFile = getAGDOptions();

		if (optionFile != null) {
			// Debug.println("Looking for AGD algorithms");
			algoList = new LinkedList<>();
			extractAlgorithms(algoList);
		}
	}

	static void extractAlgorithms(final List<String> l) {
		try {
			final JJAGDOptionParser parser = new JJAGDOptionParser(
					new InputStreamReader(new FileInputStream(optionFile)));
			parser.parse(l);
		} catch (final FileNotFoundException e) {
			Debug.println("Could find file: " + e);
		}
	}

	static File getAGDOptions() {
		File tmpFile = null;
		String fileName = null;

		try {
			tmpFile = File.createTempFile("options", ".gml");
			tmpFile.deleteOnExit();
			fileName = tmpFile.getCanonicalPath();
		} catch (final IOException e) {
			Debug.println("Couldn't create tmp file: " + e);
			return null;
		} catch (final SecurityException se) {
			Debug.println("Couldn't create tmp file: " + se);
			return null;
		}

		// Debug.println("options: " + fileName);

		final Runtime rt = Runtime.getRuntime();

		final String args[] = { "agd_server", "list", fileName };

		Process p = null;

		try {
			p = rt.exec(args);
		} catch (final IOException e) {
			Debug.println("Couldn't find agd server:" + e);
			return null;
		}

		Debug.println("AGD server found");

		try {
			final int exit = p.waitFor();
		} catch (final InterruptedException e) {
			Debug.println("InterruptedException: " + e);
		}

		return tmpFile;
	}

	@Override
	public int allowsOptimize() {
		return 0;
	}

	public JJAGD(final JJGraphWindow f) {
		fenster = f;
		graph = f.getGraph();
	}

	@Override
	public void layout() {
	}

	@Override
	public void layout(final Collection<JJNode> c) {
		layout();
	}

	@Override
	public String getName() {
		return "AGD layout";
	}

	@Override
	public void apply(String s) {
		s = s.substring(5);

		// Debug.println("Doing :" + s + ".");

		File graphFile = null;

		String fileName = null;

		try {
			graphFile = File.createTempFile("graph", ".gml");
			graphFile.deleteOnExit();
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
			final String args[] = { "agd_server", s, graphFile.getCanonicalPath(), optionFile.getCanonicalPath(), "gml",
					"gml" };
			p = rt.exec(args);
		} catch (final IOException e) {
			Debug.println("Couldn't find agd server");
			return;
		}

		try {
			final int exit = p.waitFor();
		} catch (final InterruptedException e) {
		}

		try {
			final JJAGDResultParser resParser = new JJAGDResultParser(
					new InputStreamReader(new FileInputStream(graphFile.getCanonicalPath())));

			resParser.parse(fenster, nodeArray);
		} catch (final IOException e) {
			Debug.println("AGD error: " + e);
			return;
		}

		// JJGraph tmpG= new JJGraphImpl();
		// tmpG.createGraphic();
		// try{
		// tmpG.parseFile(fileName);
		// } catch(IOException e)
		// {
		// Debug.println("Couldn't load result file: " +e);
		// return;
		// }

		// Debug.println("Result in: " + fileName);
	}

	@Override
	public boolean canDo(String s) {
		s = s.substring(5);
		return algoList.contains(s);
	}

	JJNode nodeArray[];

	private void saveToFile(final String datei) throws IOException {
		final File tmpF = (new File(datei)).getCanonicalFile();
		final OutputStream theStream = new FileOutputStream(datei);
		final OutputStreamWriter ofDatei = new OutputStreamWriter(theStream);
		nodeArray = new JJNode[graph.getNumNodes()];

		int k = 0;
		// Give every node a unique number

		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			final JJNode tmpN = iter.next();
			nodeArray[k] = tmpN;
			tmpN.setValue(k++);
		}

		ofDatei.write("#jjGraph\n");
		ofDatei.write("graph [\n");
		ofDatei.write(" id 815\n");
		ofDatei.write(" Creator \"JJGraph\"\n");
		ofDatei.write(" directed ");
		if (graph.isDirected())
			ofDatei.write("1\n");
		else
			ofDatei.write("0\n");

		// Write Node

		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			final JJNode tmpN = iter.next();

			ofDatei.write(" node [\n");
			ofDatei.write("   id " + tmpN.getValue() + " \n");
			ofDatei.write("   label \"" + tmpN.getValue() + "\"\n");

			final JJGraphicNode gn = tmpN.getGraphicNode(fenster);
			if (gn != null) {
				ofDatei.write("   graphics [\n");
				ofDatei.write("    x " + gn.getX() + "\n");
				ofDatei.write("    y " + gn.getY() + "\n");
				ofDatei.write("    w " + gn.getWidth() + "\n");
				ofDatei.write("    h " + gn.getHeight() + "\n");
				ofDatei.write("   ]\n");
				ofDatei.write(" ]\n");
			}
		}

		for (final Iterator<JJEdge> edgeIter = graph.edgeIterator(); edgeIter.hasNext();) {
			final JJEdge tmpEdge = edgeIter.next();
			ofDatei.write(" edge [\n");
			ofDatei.write("   label \"" + tmpEdge.getValue() + "\"\n");
			ofDatei.write("   id " + tmpEdge.getValue() + "\n");
			ofDatei.write("   source " + tmpEdge.getSource().getValue() + "\n");
			ofDatei.write("   target " + tmpEdge.getTarget().getValue() + "\n");
			ofDatei.write(" ]\n");

		}
		ofDatei.write("]\n");
		ofDatei.close();

	}

} // JJAGD
