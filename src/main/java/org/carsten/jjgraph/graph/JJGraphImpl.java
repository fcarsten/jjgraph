/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
/**
 * JJGraphImpl.java
 *
 *
 * Created: Wed Feb 24 20:10:53 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.JOptionPane;

import org.carsten.jjgraph.util.Debug;
import org.carsten.jjgraph.util.JJUndo;

class JJEdgeComp {
	JJEdge edge;
	JJNode node;

	public JJEdgeComp(final JJEdge e, final JJNode n) {
		edge = e;
		node = n;
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof JJEdgeComp) {
			final JJEdgeComp ec = (JJEdgeComp) o;

			if ((ec.node == node) && (edge.getName().equals(ec.edge.getName())))
				return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return node.hashCode();
	}
}

public class JJGraphImpl implements JJGraph {
	private static long currentNodeSerialNumber = 0;
	private boolean structureDirty = false;

	private final JJUndo undoManager;

	private boolean directed = true;
	private File theFile;
	protected JJWindowList windows = new JJWindowList();
	private HashSet<JJNode> nodeSet = null;
	private final HashMap<String, JJGraphTool> tools = new HashMap<>();
	private LinkedList<JJEdge> edgeSet = null;
	private boolean multipleEdges = true;
	private String name = "";

	private JJNode startNode;

	private java.util.List<JJGraphListener> structureListeners;

	private Class<? extends JJNode> nodeClass = JJNodeImpl.class;
	private Class<? extends JJEdge> edgeClass = JJEdgeImpl.class;

	@Override
	public boolean isDirty() {
		return structureDirty;// || appearanceDirty;
	}

	/**
	 * Get the value of startNode.
	 *
	 * @return Value of startNode.
	 */
	@Override
	public JJNode getStartNode() {
		return startNode;
	}

	/**
	 * Set the value of startNode.
	 *
	 * @param v
	 *            Value to assign to startNode.
	 */
	@Override
	public void setStartNode(final JJNode v) {
		this.startNode = v;
	}
	// private boolean undoRecording = true;

	@Override
	public boolean getUndoRecording() {
		return undoManager.getUndoRecording();
	}

	/**
	 * Set the value of undoRecording.
	 *
	 * @param v
	 *            Value to assign to undoRecording.
	 */
	@Override
	public void setUndoRecording(final boolean v) {
		undoManager.setUndoRecording(v);
	}

	@Override
	public Iterator<JJEdge> edgeIterator() {
		return edgeSet.listIterator();
	}

	@Override
	public Iterator<JJNode> nodeIterator() {
		return nodeSet.iterator();
	}

	@Override
	public Collection<JJNode> getNodes() {
		return nodeSet;
	}

	@Override
	public Collection<JJEdge> getEdges() {
		return edgeSet;
	}

	@Override
	public File getFile() {
		// if(theFile == null)
		// theFile = new File("");

		return theFile;
	}

	@Override
	public void setFile(final File v) {
		this.theFile = v;
		for (final Object element : getWindows()) {
			final JJGraphWindow fenster = (JJGraphWindow) element;
			fenster.getFrame().enableSaveReload();
			try {
				if (getName().equals("")) {
					Debug.println("Setting title to: " + v.getCanonicalPath());
					fenster.setTitle("JJGraph: " + v.getCanonicalPath());
				} else
					fenster.setTitle("JJGraph: " + getName());
			} catch (final java.io.IOException e) {
			}
		}
	}

	@Override
	public boolean getDirected() {
		return directed;
	}

	@Override
	public boolean isDirected() {
		return directed;
	}

	@Override
	public void setDirected(final boolean v) {
		if (v != directed) {
			if (undoManager.getUndoRecording()) {
				final Object par[] = { new Boolean(directed) };
				undoManager.add("Change directed", this, set_directed, par);
			}
			sendGraphStructureEvent();

			this.directed = v;
			for (final Object element : getWindows()) {
				final JJGraphWindow fenster = (JJGraphWindow) element;
				fenster.updateDirected(v);
			}
		}
	}

	@Override
	public JJWindowList getWindows() {
		return windows;
	}
	// public void setWindow(JJGraphWindow v) {this.fenster = v;}

	@Override
	public boolean getMultipleEdges() {
		return multipleEdges;
	}

	@Override
	public void setMultipleEdges(final boolean v) {
		this.multipleEdges = v;
	}

	@Override
	public String getName() {
		return new String(name);
	}

	@Override
	public void setName(final String v) {
		if (v != null)
			this.name = v;
	}

	public JJGraphImpl() {
		name = "";
		nodeSet = new HashSet<>();
		edgeSet = new LinkedList<>();
		undoManager = new JJUndo(this);
	}

	public JJGraphImpl(final Class<? extends JJNode> nodeC, final Class<? extends JJEdge> edgeC) {
		nodeClass = nodeC;
		edgeClass = edgeC;

		name = "";
		nodeSet = new HashSet<>();
		edgeSet = new LinkedList<>();
		undoManager = new JJUndo(this);
	}

	@Override
	public int getNumNodes() {
		return nodeSet.size();
	}

	@Override
	public int getNumEdges() {
		return edgeSet.size();
	}

	@Override
	public JJNode addNode() {
		return addNode(++currentNodeSerialNumber);
	}

	protected JJNode addNode(final long sn) {
		try {
			final JJNode newN = nodeClass.newInstance(); // new
															// JJNodeImpl();
			newN.init(this, sn);
			currentNodeSerialNumber = Math.max(currentNodeSerialNumber, sn);

			return addNode(newN);
		} catch (final java.lang.InstantiationException e) {
			System.err.println("Could not create new node: " + e.getMessage());
		} catch (final java.lang.IllegalAccessException e) {
			System.err.println("Illegal access while creating new node: " + e.getMessage());
		}
		return null;
	}

	@Override
	public JJNode addNode(final Class<? extends JJNode> c) {
		final Class<? extends JJNode> tmpC = nodeClass;
		nodeClass = c;
		final JJNode n = addNode();
		nodeClass = tmpC;
		return n;
	}

	public void sendGraphStructureEvent() {
		structureDirty = true;
		if (structureListeners == null)
			return;

		final JJGraphEvent e = new JJGraphEvent(null, JJGraphEvent.MISC_EVENT);

		for (final Object element : structureListeners) {
			((JJGraphListener) element).graphStructureChanged(e);
		}

	}

	public void sendGraphStructureEvent(final JJGraphEvent e) {
		structureDirty = true;
		if (structureListeners == null)
			return;

		for (final Object element : structureListeners) {
			((JJGraphListener) element).graphStructureChanged(e);
		}
	}

	@Override
	public JJNode addNode(final JJNode newN) {
		nodeSet.add(newN);
		for (final Object element : getWindows()) {
			final JJGraphWindow fenster = (JJGraphWindow) element;
			fenster.addNode(newN);
		}

		if (undoManager.getUndoRecording()) {
			// adding undo information

			final Object par[] = new Object[1];
			par[0] = newN;

			undoManager.add("Add node", this, delete_node, par);
		}
		sendGraphStructureEvent(new JJGraphEvent(newN, JJGraphEvent.NODE_ADD_EVENT));

		return newN;
	}

	private JJEdge createEdge(final JJNode n1, final JJNode n2)
			throws java.lang.InstantiationException, java.lang.IllegalAccessException {
		final JJEdge tmpE = edgeClass.newInstance();
		tmpE.init(n1, n2);
		return tmpE;
	}

	@Override
	public JJEdge addEdge(final JJEdge e, final JJNode n2, final int dir) {
		final JJNode n1 = e.getSource();

		JJEdge tmpE = null;

		try {
			tmpE = createEdge(n1, n2); // new JJEdgeImpl(n1, n2);
		} catch (final java.lang.InstantiationException es) {
			System.err.println("Couldn create new edge: " + es.getMessage());
			return null;
		} catch (final java.lang.IllegalAccessException es) {
			System.err.println("Couldn create new edge: " + es.getMessage());
			return null;
		}

		edgeSet.add(tmpE);
		if (dir == after)
			n1.addOutEdgeAfter(tmpE, e);
		else
			n1.addOutEdgeBefore(tmpE, e);
		n2.addInEdge(tmpE);

		for (final Object element : getWindows()) {
			final JJGraphWindow fenster = (JJGraphWindow) element;
			fenster.addEdge(tmpE);
		}

		if (undoManager.getUndoRecording()) {
			// adding undo information
			final Object par[] = new Object[1];
			par[0] = tmpE;

			undoManager.add("Add edge", this, delete_edge, par);
		}
		sendGraphStructureEvent(new JJGraphEvent(tmpE, JJGraphEvent.EDGE_ADD_EVENT));

		return tmpE;

	}

	@Override
	public void openSubtask(final String s) {
		if (getUndoRecording()) {
			getUndoManager().openSubtask(s);
		}
	}

	@Override
	public void closeSubtask(final String s) {
		if (getUndoRecording()) {
			getUndoManager().closeSubtask(s);
		}
	}

	@Override
	public void clear() {
		if (getUndoRecording()) {
			getUndoManager().openSubtask("Clear");
		}

		final boolean tmpRedraw = true;

		// if(fenster != null)
		// tmpRedraw = fenster.setRedraw(false);

		while (!nodeSet.isEmpty()) {
			final JJNode tmpN = nodeIterator().next();
			deleteNode(tmpN);
		}

		// if(fenster != null)
		// fenster.setRedraw(tmpRedraw);

		if (getUndoRecording()) {
			getUndoManager().closeSubtask("Clear");
		}
		structureDirty = false;
		// appearanceDirty = false;
	}

	@Override
	public void deleteNode(final JJNode knoten) {
		// Debug.println("Delete node");

		final boolean tmpRedraw = true;

		// if(fenster != null)
		// tmpRedraw = fenster.setRedraw(false);

		final int tmpDeg = knoten.deg();

		if (getUndoRecording() && (tmpDeg > 0)) {
			getUndoManager().openSubtask("Delete node");
		}

		while (knoten.outdeg() > 0) {
			final Iterator<JJEdge> edgeIter = knoten.outIterator();
			deleteEdge(edgeIter.next());
		}

		while (knoten.indeg() > 0) {
			final Iterator<JJEdge> edgeIter = knoten.inIterator();
			deleteEdge(edgeIter.next());
		}

		for (final Object element : getWindows()) {
			final JJGraphWindow fenster = (JJGraphWindow) element;
			fenster.deleteNode(knoten);
		}

		if (undoManager.getUndoRecording()) {
			// adding undo information

			final Object[] par = new Object[1];
			par[0] = knoten;

			undoManager.add("Delete node", this, add_node, par);
		}

		if (getUndoRecording() && (tmpDeg > 0)) {
			getUndoManager().closeSubtask("Delete node");
		}

		nodeSet.remove(knoten);
		sendGraphStructureEvent(new JJGraphEvent(knoten, JJGraphEvent.NODE_DEL_EVENT));
	}

	@Override
	public JJEdge addEdge(final JJNode n1, final JJNode n2) {
		// if(n1 == n2)
		// {
		// return null;
		// }

		if ((n1 == null) || (n2 == null)) {
			Debug.println("Invalid parameter 'null' in addEdge");
			return null;
		}

		if (!multipleEdges)
			for (final Iterator<JJEdge> edgeIter = n1.outIterator(); edgeIter.hasNext();) {
				final JJEdge tmpE = edgeIter.next();
				if (tmpE.getTarget() == n2)
					return tmpE;
			}

		JJEdge tmpE = null;

		try {
			tmpE = createEdge(n1, n2); // new JJEdgeImpl(n1, n2);
		} catch (final java.lang.InstantiationException e) {
			System.err.println("Couldn create new edge: " + e.getMessage());
			return null;
		} catch (final java.lang.IllegalAccessException e) {
			System.err.println("Couldn create new edge: " + e.getMessage());
			return null;
		}

		return addEdge(tmpE);

	}

	@Override
	public JJEdge addEdge(final JJEdge tmpE) {
		edgeSet.add(tmpE);
		tmpE.getSource().addOutEdge(tmpE);
		tmpE.getTarget().addInEdge(tmpE);

		for (final Object element : getWindows()) {
			final JJGraphWindow fenster = (JJGraphWindow) element;
			fenster.addEdge(tmpE);
		}

		if (undoManager.getUndoRecording()) {
			// adding undo information
			final Object par[] = new Object[1];
			par[0] = tmpE;
			undoManager.add("Add edge", this, delete_edge, par);
		}
		sendGraphStructureEvent(new JJGraphEvent(tmpE, JJGraphEvent.EDGE_ADD_EVENT));

		return tmpE;
	}

	@Override
	public void deleteEdge(final JJEdge kante) {
		for (final Object element : getWindows()) {
			final JJGraphWindow fenster = (JJGraphWindow) element;
			fenster.deleteEdge(kante);
		}

		kante.getTarget().delInEdge(kante);
		kante.getSource().delOutEdge(kante);

		if (undoManager.getUndoRecording()) {
			// adding undo information

			final Object par[] = new Object[1];
			par[0] = kante;

			undoManager.add("Delete edge", this, add_edge, par);
		}

		edgeSet.remove(kante);
		sendGraphStructureEvent(new JJGraphEvent(kante, JJGraphEvent.EDGE_DEL_EVENT));
	}

	@Override
	public JJGraphWindow createGraphic() {
		return createGraphic(null);
	}

	@Override
	public JJGraphWindow createGraphic(final JJGraphWindow w) {
		JJGraphWindow fenster = null;
		if (w == null)
			fenster = new JJGraphWindowImpl(this);
		else
			fenster = new JJGraphWindowImpl(this, w);

		windows.add(fenster);
		return fenster;
	}

	protected void sanityCheck() {
	}

	static String auth = null;
	static boolean proxySet = false;

	static void setProxy() {
		proxySet = true;
		String proxy = JOptionPane.showInputDialog(null, "Are you using a proxy (name:port)?", "IOException",
				JOptionPane.QUESTION_MESSAGE);

		if (proxy != null) {
			proxy = proxy.trim();
			final int index = proxy.indexOf(':');
			if (index == -1)
				return;

			final String host = proxy.substring(0, index);
			final String port = proxy.substring(index + 1, proxy.length());

			System.getProperties().put("proxySet", "true");
			System.getProperties().put("proxyHost", host);
			System.getProperties().put("proxyPort", port);

			// Microsoft VM
			// propSystem.put("firewallSet", "true");
			// propSystem.put("firewallHost", "myProxyServer.com");
			// propSystem.put("firewallPort", "80");
			// propSystem.put("http.proxyHost", "myProxyServer.com");
			// propSystem.put("http.proxyPort", "80");

			final String namePass = JOptionPane.showInputDialog(null, "Do you have to login (name:password)? ",
					"Authentification", JOptionPane.QUESTION_MESSAGE);
			if (namePass != null) {
				auth = Base64.getEncoder().encodeToString(namePass.getBytes());
			}
		}
	}

	@Override
	public void parseUrl(final URL url) throws IOException {
		final InputStreamReader r = null;
		try {
			final URLConnection conn = url.openConnection();
			if (auth != null) {
				conn.setRequestProperty("Proxy-Authorization", auth);
			}
			parseStream(conn.getInputStream(), url.toString());
			sanityCheck();
		} catch (final IOException e) {
			if (!proxySet) {
				setProxy();
				parseUrl(url);
			} else
				throw e;
		}
	}

	@Override
	public void parseFile(String fileName) throws IOException {
		final InputStream graphStream = null;
		final File tmpF = (new File(fileName)).getCanonicalFile();
		fileName = tmpF.getCanonicalPath();

		if (!tmpF.canRead())
			throw new IOException("Can't read graph " + fileName);

		parseStream(new FileInputStream(fileName), fileName);

		setFile(tmpF);

		sanityCheck();
	}

	public JJGraphParser[] getParserForFileName(final String fileName, final InputStream graphStream)
			throws IOException {
		InputStream coordStream = null;
		JJGraphParser parser[] = null;

		if (fileName.endsWith(".g") || fileName.endsWith(".G")) {
			parser = new JJGraphParser[1];
			parser[0] = new GraphedParser(new InputStreamReader(graphStream));
		} else if (fileName.endsWith(".mtx") || fileName.endsWith(".MTX")) {
			parser = new JJGraphParser[1];
			parser[0] = new MatrixMarketParser(new InputStreamReader(graphStream));
		} else if (fileName.endsWith(".gml") || fileName.endsWith(".GML")) {
			parser = new JJGraphParser[1];
			parser[0] = new JJGMLParser(new InputStreamReader(graphStream));
		}
		// else if(fileName.endsWith(".pt")|| fileName.endsWith(".PT"))
		// {
		// parser = new ProteinParser[1];
		// parser[0] = new ProteinParser(new InputStreamReader(graphStream));
		// }
		else if (fileName.endsWith(".rw") || fileName.endsWith(".RW")) {
			parser = new JJGraphParser[1];
			parser[0] = new JJRailParser(new InputStreamReader(graphStream));
		} else if (fileName.endsWith(".src") || fileName.endsWith(".SRC")) {
			File coordFile = new File(fileName.substring(0, fileName.length() - 3) + "xyz");
			if (coordFile.exists()) {
				coordStream = new FileInputStream(coordFile);
			} else {
				coordFile = new File(fileName.substring(0, fileName.length() - 3) + "xyz.gz");
				if (coordFile.exists()) {
					coordStream = new GZIPInputStream(new FileInputStream(coordFile));
				}
			}

			if (coordStream != null) {
				parser = new JJGraphParser[2];
				Debug.println("Parsing coordinates");
				parser[1] = new JJScotchCoordParser(new InputStreamReader(coordStream));
			} else {
				parser = new JJGraphParser[1];
				Debug.println("No coodinates for " + fileName + " found.");
			}
			parser[0] = new JJScotchParser(new InputStreamReader(graphStream));
		} else if (fileName.endsWith(".graph") || fileName.endsWith(".GRAPH")) {
			Debug.println("Looking for: " + fileName.substring(0, fileName.length() - 5) + "xyz");

			File coordFile = new File(fileName.substring(0, fileName.length() - 5) + "xyz");
			if (coordFile.exists()) {
				coordStream = new FileInputStream(coordFile);
			} else {
				Debug.println("NO");

				coordFile = new File(fileName.substring(0, fileName.length() - 5) + "xyz.gz");
				Debug.println("Looking for: " + fileName.substring(0, fileName.length() - 5) + "xyz.gz");

				if (coordFile.exists()) {
					coordStream = new GZIPInputStream(new FileInputStream(coordFile));
				} else
					Debug.println("NO");
			}

			if (coordStream != null) {
				parser = new JJGraphParser[2];
				parser[1] = new JJMetisCoordParser(new InputStreamReader(coordStream));
			} else {
				parser = new JJGraphParser[1];
			}
			parser[0] = new JJMetisParser(new InputStreamReader(graphStream));
		} else {
			parser = new JJGraphParser[1];
			parser[0] = new JJRomeParser(new InputStreamReader(graphStream));
		}
		return parser;
	}

	public void parseStream(InputStream graphStream, String fileName) throws IOException {

		final boolean tmpRedraw = true;
		JJGraphParser parser[] = null;
		// JJCoordParser coordParser = null;

		// if(fenster != null){
		// tmpRedraw = fenster.setRedraw(false);
		getWindows().printNote("Reading " + fileName);
		// }else {
		// Debug.println("Reading " + fileName);
		// }

		if (name.equals(""))
			name = fileName;

		try {
			if (fileName.endsWith(".gz") || fileName.endsWith(".GZ")) {
				graphStream = new GZIPInputStream(graphStream);
				fileName = fileName.substring(0, fileName.length() - 3);
				// Debug.println(":" + fileName);
			}

			parser = getParserForFileName(fileName, graphStream);

			if (getUndoRecording()) {
				getUndoManager().openSubtask("Load file " + fileName);
			}

			try {
				for (final JJGraphParser element : parser)
					element.parse(this);
			} catch (final org.carsten.jjgraph.graph.TokenMgrError e) {
				throw new IOException(e.getMessage());
			}
		} finally {
			try {
				if (graphStream != null)
					graphStream.close();
				// !!! Coord stream might not close correctly !!!
				// if(coordStream != null)
				// coordStream.close();
			} catch (final IOException e) {
			}

			if (getUndoRecording()) {
				getUndoManager().closeSubtask("Load file " + fileName);
			}
			// if(fenster != null)
			// {
			// fenster.setRedraw(tmpRedraw);
			// }
		}

		Debug.println("Loaded " + getNumNodes() + " nodes and " + getNumEdges() + " edges.");

		structureDirty = false;
		// appearanceDirty = false;
	}

	public void parseLayout(String fileName) throws IOException {
		final boolean tmpRedraw = true;

		final File tmpF = (new File(fileName)).getCanonicalFile();
		fileName = tmpF.getCanonicalPath();

		// if(fenster != null){
		// tmpRedraw = fenster.setRedraw(false);
		getWindows().printNote("Reading " + fileName);
		// }else {
		// Debug.println("Reading " + fileName);
		// }

		if (!tmpF.canRead())
			throw new IOException("Can't read file " + fileName);

		JJGraphParser parser = null;

		if (name.equals(""))
			name = fileName;

		try (InputStream graphStream = new FileInputStream(fileName)) {
			InputStream inStream = graphStream;

			if (fileName.endsWith(".gz") || fileName.endsWith(".GZ")) {
				inStream = new GZIPInputStream(graphStream);
				fileName = fileName.substring(0, fileName.length() - 3);
				// Debug.println(":" + fileName);
			}

			if (fileName.endsWith(".g") || fileName.endsWith(".G"))
				parser = new GraphedAttributeParser(new InputStreamReader(inStream));
			else
				throw new IOException("Does not look like a Graphed file.\nFilename should end with .g or .g.gz");

			if (getUndoRecording()) {
				getUndoManager().openSubtask("Load Layout " + fileName);
			}

			try {
				parser.parse(this);
			} catch (final org.carsten.jjgraph.graph.TokenMgrError e) {
				throw new IOException(e.getMessage());
			} finally {
				if (getUndoRecording()) {
					getUndoManager().closeSubtask("Load Layout " + fileName);
				}
				// if(fenster != null)
				// {
				// fenster.setRedraw(tmpRedraw);
				// }
			}
		}
		Debug.println("Loaded layout for" + getNumNodes() + " nodes and " + getNumEdges() + " edges.");

		structureDirty = false;
		// appearanceDirty = false;
	}

	@Override
	public boolean paste(final Reader tmpStream) throws IOException {
		// boolean tmpRedraw = fenster.setRedraw(false);
		getWindows().printNote("Pasting ... ");

		final JJGraphParser parser = new GraphedParser(tmpStream);
		if (getUndoRecording()) {
			getUndoManager().openSubtask("Pasting");
		}

		setNodeValue(-1);

		try {
			parser.parse(this);
		} catch (final org.carsten.jjgraph.graph.TokenMgrError e) {
			getWindows().printError(e.getMessage());
		}

		// fenster.setRedraw(tmpRedraw);

		if (getUndoRecording()) {
			getUndoManager().closeSubtask("Pasting");
		}

		tmpStream.close();
		return true;
	}

	@Override
	public boolean areNeighbors(final JJNode n1, final JJNode n2) {
		for (final Iterator<JJEdge> edgeIter = n1.inIterator(); edgeIter.hasNext();) {
			final JJNode tmpS = edgeIter.next().getSource();
			if (n2 == tmpS)
				return true;
		}

		for (final Iterator<JJEdge> edgeIter = n1.outIterator(); edgeIter.hasNext();) {
			final JJNode tmpT = edgeIter.next().getTarget();
			if (n2 == tmpT)
				return true;
		}
		return false;
	}

	@Override
	public int getNumCuts() {
		int numCuts = 0;

		for (final Iterator<JJEdge> edgeIter = edgeSet.listIterator(); edgeIter.hasNext();) {
			final JJEdge tmpE = edgeIter.next();
			if ((tmpE.getTarget().getCluster() != tmpE.getSource().getCluster())
					&& (tmpE.getTarget().getCluster() != -1) && (tmpE.getSource().getCluster() != -1)) {
				numCuts++;
			}
		}
		return numCuts;
	}

	static JJGraph randomGraph(final int nodeNum, final double alpha) {
		final JJGraph tmpG = new JJGraphImpl();

		for (int i = 0; i < nodeNum; i++) {
			tmpG.addNode().setValue(i);
		}

		if (nodeNum > 1) {
			final double p = alpha / (nodeNum - 1);

			for (final Iterator<JJNode> iter = tmpG.nodeIterator(); iter.hasNext();) {
				final JJNode tmpN = iter.next();
				for (final Iterator<JJNode> iter2 = tmpG.nodeIterator(); iter2.hasNext();) {
					final JJNode tmpN2 = iter2.next();
					if ((tmpN != tmpN2) && (Math.random() <= p))
						tmpG.addEdge(tmpN, tmpN2);
				}
			}
		}
		// Debug.println("Created graph with " + tmpG.getNumNodes() +
		// " nodes and " + tmpG.getNumEdges() + " edges.");

		return tmpG;
	}

	private void markDFS(final JJNode knoten, final int mark) {
		if (knoten.getValue() == mark)
			return;

		knoten.setValue(mark);

		for (final Iterator<JJEdge> edgeIter = knoten.inIterator(); edgeIter.hasNext();) {
			final JJNode tmpS = edgeIter.next().getSource();
			markDFS(tmpS, mark);
		}

		for (final Iterator<JJEdge> edgeIter = knoten.outIterator(); edgeIter.hasNext();) {
			final JJNode tmpT = edgeIter.next().getTarget();
			markDFS(tmpT, mark);
		}
	}

	@Override
	public void makeConnected() {
		if (getUndoRecording()) {
			getUndoManager().openSubtask("Make connected");
		}

		if (getNumNodes() < 1)
			return;

		for (final Object element : nodeSet) {
			final JJNode tmpN = (JJNode) element;
			tmpN.setValue(-1);
		}

		final JJNode firstN = nodeSet.iterator().next();

		markDFS(firstN, 0);

		for (final Object element : nodeSet) {
			final JJNode tmpN = (JJNode) element;
			if (tmpN.getValue() == -1) {
				addEdge(firstN, tmpN);
				markDFS(tmpN, 0);
			}
		}

		if (getUndoRecording()) {
			getUndoManager().closeSubtask("Make connected");
		}

	}

	@Override
	public JJNode findNodeWithName(final String nameP) {
		for (final Object element : nodeSet) {
			final JJNode tmpN = (JJNode) element;
			if (tmpN.getName().equals(nameP))
				return tmpN;
		}
		return null;

	}

	@Override
	public void saveToFile(final JJGraphWindow w) throws IOException {
		if ((getFile() == null) || (!getFile().canWrite())) {
			throw new IOException("Cannot write to file " + getFile().getAbsolutePath());
		}

		// Debug.println("Saving graph to "+getFile().getAbsolutePath());
		saveToFile(w, getFile().getAbsolutePath(), nodeSet);
	}

	@Override
	public void saveToFile(final JJGraphWindow w, final String fileName) throws IOException {
		saveToFile(w, fileName, nodeSet);
	}

	@Override
	public void saveToFile(final JJGraphWindow window, String datei, final Set<JJNode> nodes) throws IOException {
		final File tmpF = (new File(datei)).getCanonicalFile();

		Debug.println("Saving graph to " + datei);
		boolean compressed = false;
		OutputStream tmpStream = null;

		try {
			if (datei.endsWith(".gz") || datei.endsWith(".GZ")) {
				datei = datei.substring(0, datei.length() - 3);
				// Debug.println(":"+datei);
				compressed = true;
			}

			if (datei.endsWith(".g") || datei.endsWith(".G")) {
				if (compressed) {
					tmpStream = new FileOutputStream(datei + ".gz");
					tmpStream = new GZIPOutputStream(tmpStream);
				} else {
					tmpStream = new FileOutputStream(datei);
				}
				_saveGraphEd(window, tmpStream, nodes);
			}
			// else if(datei.endsWith(".pdp") || datei.endsWith(".PDP"))
			// {
			// tmpStream = new FileOutputStream(datei);
			// _savePDP(tmpStream, nodes);
			// }
			else if (datei.endsWith(".gml") || datei.endsWith(".GML")) {
				if (compressed) {
					tmpStream = new FileOutputStream(datei + ".gz");
					tmpStream = new GZIPOutputStream(tmpStream);
				} else {
					tmpStream = new FileOutputStream(datei);
				}
				_saveGML(window, tmpStream, nodes, null);
			} else {
				getWindows().printError("File must have extension .g or .g.gz");
				// _saveGML(tmpStream, nodes);
			}

		} finally {
			if (tmpStream != null) {
				try {
					Debug.println("Closing stream");
					tmpStream.close();
				} catch (final IOException e) {
				}
			}
		}

		structureDirty = false;
		// appearanceDirty = false;

		setFile(tmpF);
	}

	@Override
	public void numberNodes(int start) {
		for (final Iterator<JJNode> iter = nodeIterator(); iter.hasNext();) {
			final JJNode tmpN = iter.next();
			tmpN.setValue(start++);
		}
	}

	@Override
	public void setNodeValue(final int v) {
		for (final Iterator<JJNode> iter = nodeIterator(); iter.hasNext();) {
			final JJNode tmpN = iter.next();
			tmpN.setValue(v);
		}
	}

	// private void _savePDP (OutputStream theStream, Collection nodes)
	// throws IOException
	// {
	// OutputStreamWriter ofDatei = new OutputStreamWriter(theStream);
	// _savePDP(ofDatei, nodes);
	// }

	// private void _savePDP (Writer ofDatei, Collection nodes)
	// throws IOException
	// {
	// Debug.println("Saving pdp format");

	// int k=1;
	// String tmpString;
	// // Give every node a unique number

	// for(Iterator iter = nodes.iterator (); iter.hasNext (); ) {
	// JJNode tmpN = (JJNode) iter.next ();
	// tmpN.setValue(k++);
	// }

	// ofDatei.write( "GRAPH \"Photo Book\" = DIRECTED\n{$\n");

	// ofDatei.write("\n$}\n");

	// // Write Node

	// for(Iterator iter = nodes.iterator (); iter.hasNext (); ) {
	// JJNode tmpN = (JJNode) iter.next ();

	// // Write Node Data

	// ofDatei.write("" + tmpN.getValue() + " {$");
	// JJGraphicNode gn= tmpN.getGraphicNode();

	// if(gn != null)
	// {
	// ofDatei.write(" NP " + (int)gn.getX() + " " + (int)gn.getY());
	// ofDatei.write(" FF_3D " + (int)gn.getX() + " " + (int)gn.getY()
	// + " " + (int)gn.getZ() );
	// }
	// ofDatei.write(" FILES \"picImg.jpg\" \"picBlurb.jpg\" \"picText.txt\" ");

	// ofDatei.write(" $} \"" + tmpN.getValue() + "\"\n");

	// // Write edges going out of this node if their target belongs
	// // to the node list

	// for(Iterator edgeIter = tmpN.outIterator();
	// edgeIter.hasNext();)
	// {
	// JJEdge tmpEdge = (JJEdge) edgeIter.next();

	// if(nodes.contains (tmpEdge.getTarget()))
	// {
	// // Write tmpEdge data

	// ofDatei.write(" " + tmpEdge.getTarget().getValue() + " {$");
	// ofDatei.write(" $} \"");
	// ofDatei.write("\"\n");
	// }
	// }
	// ofDatei.write(" ;\n");
	// }
	// ofDatei.write("END\n");
	// ofDatei.close();
	// }

	@Override
	public String saveGraphEdToString(final JJGraphWindow window, final Collection<JJNode> nodes) throws IOException {
		final StringWriter s = new StringWriter();
		_saveGraphEd(window, s, nodes);
		return s.toString();
	}

	@Override
	public String saveGMLToString(final JJGraphWindow window, Collection<JJNode> nodes, final Collection<JJEdge> edges)
			throws IOException {
		final StringWriter s = new StringWriter();
		if (nodes == null)
			nodes = getNodes();

		_saveGML(window, s, nodes, edges);
		return s.toString();
	}

	private void _saveGraphEd(final JJGraphWindow window, final OutputStream theStream, final Collection<JJNode> nodes)
			throws IOException {
		final OutputStreamWriter ofDatei = new OutputStreamWriter(theStream);
		_saveGraphEd(window, ofDatei, nodes);
	}

	private void _saveGraphEd(final JJGraphWindow window, final Writer ofDatei, final Collection<JJNode> nodes)
			throws IOException {
		Debug.println("Saving graphed format");

		final int k = 1;
		final String tmpString;
		// Give every node a unique number

		// for(Iterator iter = nodes.iterator (); iter.hasNext (); ) {
		// JJNode tmpN = (JJNode) iter.next ();
		// tmpN.setValue(k++);
		// }

		// Write Graph
		if (directed)
			ofDatei.write("GRAPH \"" + name + "\" = DIRECTED\n{$\n");
		else
			ofDatei.write("GRAPH \"" + name + "\" = UNDIRECTED\n{$\n");

		ofDatei.write("\n$}\n");

		// Write Node

		for (final Object element : nodes) {
			final JJNode tmpN = (JJNode) element;

			// Write Node Data

			ofDatei.write("" + tmpN.getSerialNumber() + " {$");

			// Write graphic information if available
			if (window != null) {
				final JJGraphicNode gn = tmpN.getGraphicNode(window);

				ofDatei.write(" FF_C \"#");

				if (gn.getColor().getRed() < 16)
					ofDatei.write("0");
				ofDatei.write(Integer.toHexString(gn.getColor().getRed()));

				if (gn.getColor().getGreen() < 16)
					ofDatei.write("0");
				ofDatei.write(Integer.toHexString(gn.getColor().getGreen()));

				if (gn.getColor().getBlue() < 16)
					ofDatei.write("0");
				ofDatei.write(Integer.toHexString(gn.getColor().getBlue()));

				ofDatei.write("\"");

				ofDatei.write(" NP " + (int) gn.getX() + " " + (int) gn.getY());
				ofDatei.write(" FF_3D " + (int) gn.getX() + " " + (int) gn.getY() + " " + (int) gn.getZ());

				if ((window instanceof JJGraphWindowImpl) && gn.getVisible().get(((JJGraphWindowImpl) window).HIDDEN))
					ofDatei.write(" JJ_HIDDEN ");

				if (gn instanceof JJGraphicNodeImpl) {
					final JJGraphicNodeImpl gni = (JJGraphicNodeImpl) gn;

					final JJGraphicNodeAppearance app = gni.getAppearance();
					ofDatei.write(" JJ_APP \"" + app.getClass().getName() + "\"");
					if (app instanceof JJGraphicNodeImage) {
						final JJGraphicNodeImage gnimage = (JJGraphicNodeImage) app;
						ofDatei.write(" \"");
						URL url = gnimage.getImageURL();

						final String path = url.toExternalForm();

						final URLClassLoader cl = (URLClassLoader) JJGraphImpl.class.getClassLoader();
						final URL urls[] = cl.getURLs();

						for (final URL url2 : urls) {
							final String tmpS = url2.toExternalForm();
							Debug.println("Testting path: " + path + " with " + tmpS);

							if (path.startsWith(tmpS)) {
								Debug.println("Hit!");

								ofDatei.write(path.substring(tmpS.length()));
								url = null;
								break;
							}

						}

						if (url != null)
							ofDatei.write(url.toExternalForm());

						ofDatei.write("\" ");

					}

				}

			}

			ofDatei.write(" $} \"" + tmpN.getName() + "\"\n");

			// Write edges going out of this node if their target belongs
			// to the node list

			for (final Iterator<JJEdge> edgeIter = tmpN.outIterator(); edgeIter.hasNext();) {
				final JJEdge tmpEdge = edgeIter.next();

				if (nodes.contains(tmpEdge.getTarget())) {
					// Write tmpEdge data

					ofDatei.write(" " + tmpEdge.getTarget().getSerialNumber() + " {$");

					if (tmpEdge.getWeight() != 1.0)
						ofDatei.write(" FF_WE " + (int) (tmpEdge.getWeight() * 100));

					if (window != null) {
						final JJGraphicEdge ge = tmpEdge.getGraphicEdge(window);
						ofDatei.write(" FF_C \"#");

						if (ge.getColor().getRed() < 16)
							ofDatei.write("0");
						ofDatei.write(Integer.toHexString(ge.getColor().getRed()));

						if (ge.getColor().getGreen() < 16)
							ofDatei.write("0");
						ofDatei.write(Integer.toHexString(ge.getColor().getGreen()));

						if (ge.getColor().getBlue() < 16)
							ofDatei.write("0");
						ofDatei.write(Integer.toHexString(ge.getColor().getBlue()));

						ofDatei.write("\"");
					}

					ofDatei.write(" $} \"");
					if (tmpEdge.getName() != null)
						ofDatei.write(tmpEdge.getName());
					ofDatei.write("\"\n");
				}
			}
			ofDatei.write(" ;\n");
		}
		ofDatei.write("END\n");
		ofDatei.close();
	}

	private void _saveGML(final JJGraphWindow w, final OutputStream theStream, final Collection<JJNode> nodes,
			final Collection<JJEdge> edges) throws IOException {
		final OutputStreamWriter ofDatei = new OutputStreamWriter(theStream);
		_saveGML(w, ofDatei, nodes, edges);
	}

	private void _saveGML(final JJGraphWindow window, final Writer ofDatei, Collection<JJNode> nodes,
			Collection<JJEdge> edges) throws IOException {
		Debug.println("Saving GML format");
		int k = 1;
		final String tmpString;
		// Give every node a unique number

		if (nodes == null)
			nodes = getNodes();
		if (edges == null)
			edges = getEdges();

		for (final Object element : nodes) {
			final JJNode tmpN = (JJNode) element;
			tmpN.setValue(k++);
		}

		ofDatei.write("#jjGraph\n");
		ofDatei.write("graph [\n");
		ofDatei.write(" id 815\n");
		ofDatei.write(" Creator \"JJGraph\"\n");
		ofDatei.write(" directed ");
		if (isDirected())
			ofDatei.write("1\n");
		else
			ofDatei.write("0\n");

		// Write Node

		for (final Object element : nodes) {
			final JJNode tmpN = (JJNode) element;

			ofDatei.write(" node [\n");
			ofDatei.write("   id " + tmpN.getSerialNumber() + " \n");
			ofDatei.write("   label \"" + tmpN.getName() + "\"\n");

			final JJGraphicNode gn = tmpN.getGraphicNode(window);
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

		for (final Object element : edges) {
			final JJEdge tmpEdge = (JJEdge) element;
			ofDatei.write(" edge [\n");
			ofDatei.write("   label \"" + tmpEdge.getName() + "\"\n");
			ofDatei.write("   id " + tmpEdge.getValue() + "\n");
			ofDatei.write("   source " + tmpEdge.getSource().getSerialNumber() + "\n");
			ofDatei.write("   target " + tmpEdge.getTarget().getSerialNumber() + "\n");
			ofDatei.write(" ]\n");

		}
		ofDatei.write("]\n");
		ofDatei.close();
	}

	@Override
	public void printArray(final int dist[][]) {

		for (final int[] element : dist) {
			for (final int element2 : element) {
				Debug.print(" " + element2);
			}
			Debug.println("");
		}
		Debug.println("");
	}

	@Override
	public int diameter() {
		final int numNodes = getNumNodes();
		final int dist[][] = new int[numNodes][numNodes];
		final JJNode nodes[] = new JJNode[numNodes];

		int i = 0;
		for (final Object element : nodeSet) {
			final JJNode tmpN = (JJNode) element;
			tmpN.setValue(i);
			nodes[i++] = tmpN;
		}

		//
		// init array
		//

		for (i = 0; i < numNodes; i++) {
			for (final Iterator<JJEdge> edgeIter = nodes[i].outIterator(); edgeIter.hasNext();) {
				final JJEdge tmpE = edgeIter.next();
				dist[i][tmpE.getTarget().getValue()] = 1;
			}
		}

		// fill array
		//

		boolean didChange = true;
		while (didChange) {
			didChange = false;
			// printArray(dist);

			for (i = 0; i < numNodes; i++) {
				for (final Iterator<JJEdge> edgeIter = nodes[i].outIterator(); edgeIter.hasNext();) {
					final JJNode target = edgeIter.next().getTarget();

					for (int k = 0; k < numNodes; k++) {
						final int aktuDist = dist[k][i];
						if (aktuDist > 0)
							if ((dist[k][target.getValue()] == 0) || (dist[k][target.getValue()] > aktuDist + 1)) {
								dist[k][target.getValue()] = aktuDist + 1;
								didChange = true;
							}
					}
				}
			}
		}

		int maxDist = 0;
		int minDist = numNodes;

		for (i = 0; i < numNodes; i++) {
			for (int k = 0; k < numNodes; k++) {
				if (k != i) {
					maxDist = Math.max(maxDist, dist[i][k]);
					minDist = Math.min(minDist, dist[i][k]);
				}
			}
		}

		if (minDist == 0)
			getWindows().printError("Graph not strongly connected");

		// Debug.println("Diameter: " + maxDist);
		return maxDist;
	}

	@Override
	public JJUndo getUndoManager() {
		return undoManager;
	}

	static Class<? extends JJGraph> jjgraph = org.carsten.jjgraph.graph.JJGraph.class;
	static private Method delete_node;
	static private Method add_node;
	static private Method delete_edge;
	static private Method add_edge;
	static private Method set_directed;

	static {
		try {
			{
				final Class<?>[] parT = new Class[1];
				parT[0] = org.carsten.jjgraph.graph.JJNode.class;
				delete_node = jjgraph.getMethod("deleteNode", parT);
			}
			{
				final Class<?>[] parT = new Class[1];
				parT[0] = org.carsten.jjgraph.graph.JJEdge.class;
				delete_edge = jjgraph.getMethod("deleteEdge", parT);
			}
			{
				final Class<?>[] parT = new Class[1];
				parT[0] = org.carsten.jjgraph.graph.JJNode.class;
				add_node = jjgraph.getMethod("addNode", parT);
			}
			{
				final Class<?>[] parT = new Class[1];
				parT[0] = org.carsten.jjgraph.graph.JJEdge.class;
				add_edge = jjgraph.getMethod("addEdge", parT);
			}
			{
				final Class<?>[] parT = { boolean.class };
				set_directed = jjgraph.getMethod("setDirected", parT);
			}

		} catch (final java.lang.NoSuchMethodException e) {
			Debug.println("NoSuchMethodException: " + e.getMessage() + " :");
		}
	}

	@Override
	public void addStructureListener(final JJGraphListener e) {
		if (structureListeners == null)
			structureListeners = new LinkedList<>();
		structureListeners.add(e);

	}

	@Override
	public void removeStructureListener(final JJGraphListener e) {
		if (structureListeners == null)
			return;

		structureListeners.remove(e);
	}

	@Override
	public void addTool(final String s, final JJGraphTool o) {
		tools.put(s, o);
	}

	@Override
	public JJGraphTool getTool(final String s) {
		return tools.get(s);
	}

	public void removeParallelEdgesDirected() {
		int count = 0;

		for (final Iterator<JJNode> nodeIter = nodeIterator(); nodeIter.hasNext();) {
			final JJNode tmpN = nodeIter.next();

			final Set<JJEdge> delList = new HashSet<>(); // Reflexive edges
															// appear
			// twice!!!
			final Set<JJEdgeComp> set = new HashSet<>();

			for (final Iterator<JJEdge> iter = tmpN.inIterator(); iter.hasNext();) {
				final JJEdge tmpE = iter.next();
				final JJNode other = tmpE.opposite(tmpN);
				final JJEdgeComp ec = new JJEdgeComp(tmpE, other);
				if (set.contains(ec)) {
					delList.add(tmpE);
				} else
					set.add(ec);
			}
			set.clear();

			for (final Iterator<JJEdge> iter = tmpN.outIterator(); iter.hasNext();) {
				final JJEdge tmpE = iter.next();
				final JJNode other = tmpE.opposite(tmpN);
				final JJEdgeComp ec = new JJEdgeComp(tmpE, other);
				if (set.contains(ec)) {
					delList.add(tmpE);
				} else
					set.add(ec);
			}
			for (final Object element : delList) {
				deleteEdge((JJEdge) element);
				count++;
			}
		}
		getWindows().printNote("Removed " + count + " directed  edges");
	}

	public void removeParallelEdgesUndirected() {
		int count = 0;

		for (final Iterator<JJNode> nodeIter = nodeIterator(); nodeIter.hasNext();) {
			final JJNode tmpN = nodeIter.next();

			final Set<JJEdge> delList = new HashSet<>(); // Reflexive edges
															// appear
			// twice!!!
			final Set<JJEdgeComp> set = new HashSet<>();

			for (final Iterator<JJEdge> iter = tmpN.edgeIterator(); iter.hasNext();) {
				final JJEdge tmpE = iter.next();
				final JJNode other = tmpE.opposite(tmpN);
				final JJEdgeComp ec = new JJEdgeComp(tmpE, other);

				if (set.contains(ec)) {
					delList.add(tmpE);
				} else
					set.add(ec);
			}
			for (final Object element : delList) {
				deleteEdge((JJEdge) element);
				count++;
			}
		}
		getWindows().printNote("Removed " + count + " undirected edges");
	}

	@Override
	public void removeParallelEdges() {
		if (isDirected())
			removeParallelEdgesDirected();
		else
			removeParallelEdgesUndirected();
	}

	@Override
	public void removeReflexiveEdges() {
		final java.util.List<JJEdge> delList = new LinkedList<>();
		for (final Iterator<JJEdge> iter = edgeIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			if (tmpE.getSource() == tmpE.getTarget())
				delList.add(tmpE);
		}
		for (final Object element : delList) {
			deleteEdge((JJEdge) element);
		}
	}

} // JJGraphImpl
