/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * JJNodeImpl.java
 *
 *
 * Created: Fri Feb 26 13:08:14 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import org.carsten.jjgraph.util.Debug;
import org.carsten.jjgraph.util.JJUndo;

public class JJNodeImpl implements JJNode {
	protected int value = -1;
	protected LinkedList<JJEdge> outEdges = new LinkedList<>(); // null;
	protected LinkedList<JJEdge> inEdges = new LinkedList<>(); // null;
	protected LinkedList<JJEdge> edges = new LinkedList<>(); // null;

	// protected JJGraphicNode gn= null;
	protected JJGraph graph;
	protected String name;
	protected URL myUrl;
	protected int cluster = -1;
	protected long serialNumber;

	private final Map<JJGraphWindow, JJGraphicNode> windowMap = new IdentityHashMap<>();
	private JJGraphWindow window;

	/**
	 * Get the value of serialNumber.
	 *
	 * @return Value of serialNumber.
	 */
	@Override
	public long getSerialNumber() {
		return serialNumber;
	}

	/**
	 * Set the value of serialNumber.
	 *
	 * @param v
	 *            Value to assign to serialNumber.
	 */
	public void setSerialNumber(final long v) {
		this.serialNumber = v;
	}

	protected double coord = 0.0;

	/**
	 * Get the value of coord.
	 *
	 * @return Value of coord.
	 */
	@Override
	public double getCoord() {
		return coord;
	}

	/**
	 * Set the value of coord.
	 *
	 * @param v
	 *            Value to assign to coord.
	 */
	@Override
	public void setCoord(final double v) {
		this.coord = v;
	}

	@Override
	public JJGraphicNode getGraphicNode(final JJGraphWindow w) {
		return windowMap.get(w);
	}

	@Override
	public void setGraphicNode(final JJGraphicNode v, final JJGraphWindow w) {
		windowMap.put(w, v);
	}

	@Override
	public JJGraph getGraph() {
		return graph;
	}

	@Override
	public Iterator<JJEdge> outIterator() {
		return outEdges.listIterator();
	}

	@Override
	public Iterator<JJEdge> inIterator() {
		return inEdges.listIterator();
	}

	@Override
	public Iterator<JJEdge> edgeIterator() {
		return edges.listIterator();
	}

	@Override
	public URL getUrl() {
		if (myUrl == null) {
			try {
				myUrl = new URL(getName());
			} catch (final java.net.MalformedURLException e) {
				Debug.println("Couldn't parse URL: " + e.getMessage());
			}
		}

		return myUrl;
	}

	@Override
	public void setUrl(final URL u) {
		myUrl = u;
	}

	@Override
	public int getCluster() {
		return cluster;
	}

	@Override
	public void setCluster(final int v) {
		this.cluster = v;
	}

	@Override
	public String getName() {
		if (name == null)
			name = new String();

		return name;
	}

	@Override
	public void setName(final String v) {
		final JJUndo undo = getGraph().getUndoManager();
		if (undo.getUndoRecording()) {
			final Object par[] = new Object[1];
			par[0] = this.name;
			undo.add("Change node name", this, set_name, par);
		}

		for (final Map.Entry<JJGraphWindow, JJGraphicNode> element : windowMap.entrySet()) {
			final JJGraphicNode gn = element.getValue();
			gn.repaint();
		}

		this.name = v;

		for (final Map.Entry<JJGraphWindow, JJGraphicNode> element : windowMap.entrySet()) {
			final JJGraphicNode gn = element.getValue();
			gn.recomputeNodeSize();
			gn.repaint();
		}
	}

	@Override
	public int getValue() {
		return value;
	}

	@Override
	public void setValue(final int v) {
		this.value = v;
	}

	// public Color getColor() {
	// for(Iterator iter = windowMap.entrySet().iterator(); iter.hasNext();){
	// Map.Entry me = (Map.Entry)iter.next();
	// JJGraphicNode gn = (JJGraphicNode)me.getValue();
	// return gn.getColor();

	// return null;
	// }

	// public void setColor(Color v) {
	// if(v==null)
	// return;

	// for(Iterator iter = windowMap.entrySet().iterator(); iter.hasNext();){
	// Map.Entry me = (Map.Entry)iter.next();
	// JJGraphicNode gn = (JJGraphicNode)me.getValue();
	// gn.setColor(v);
	// }
	// }

	@Override
	public int indeg() {
		return inEdges.size();
	}

	@Override
	public int outdeg() {
		return outEdges.size();
	}

	@Override
	public int deg() {
		return indeg() + outdeg();
	}

	// public JJNodeImpl(JJGraph g,URL theUrl) {
	// graph=g;
	// myUrl = theUrl;
	// }

	@Override
	public void init(final JJGraph g, final long n) {
		serialNumber = n;
		graph = g;
		myUrl = null;
	}

	@Override
	public void addInEdge(final JJEdge e) {
		inEdges.add(e);
		edges.add(e);
	}

	@Override
	public void delInEdge(final JJEdge e) {
		final boolean tmpB = inEdges.remove(e);

		if (!tmpB) {
			Debug.println("Tried to remove non existing in edge (" + e.getSource().getName() + ","
					+ e.getTarget().getName() + ")");
		}
		edges.remove(e);

	}

	@Override
	public void addOutEdge(final JJEdge e) {
		outEdges.add(e);
		edges.add(e);
	}

	@Override
	public void addOutEdgeAfter(final JJEdge e, final JJEdge e2) {
		outEdges.add(outEdges.indexOf(e2) + 1, e);
		edges.add(edges.indexOf(e2) + 1, e);
	}

	@Override
	public void addOutEdgeBefore(final JJEdge e, final JJEdge e2) {
		outEdges.add(outEdges.indexOf(e2), e);
		edges.add(edges.indexOf(e2), e);
	}

	@Override
	public void delOutEdge(final JJEdge e) {
		final boolean tmpB = outEdges.remove(e);

		if (!tmpB) {
			Debug.println("Tried to remove non existing out edge (" + e.getSource().getName() + ","
					+ e.getTarget().getName() + ")");
		}
		edges.remove(e);
	}

	static protected Method set_name;

	static {
		final Class<JJNode> jjnode = org.carsten.jjgraph.graph.JJNode.class;

		try {
			{
				final Class<?>[] parT = new Class[1];
				parT[0] = String.class;
				set_name = jjnode.getMethod("setName", parT);
			}
		} catch (final java.lang.NoSuchMethodException e) {
			Debug.println("NoSuchMethodException: " + e.getMessage() + " :");
		}
	}

	@Override
	public JJEdge outEdge(final int i) {
		return outEdges.get(i);
	}

	@Override
	public int outIndex(final JJEdge e) {
		return outEdges.indexOf(e);
	}

	@Override
	public JJEdge inEdge(final int i) {
		return inEdges.get(i);
	}

	@Override
	public int inIndex(final JJEdge e) {
		return inEdges.indexOf(e);
	}

	@Override
	public JJNode firstSon() {
		if (outdeg() == 0)
			return null;

		return firstOutEdge().getTarget();
	}

	@Override
	public JJNode lastSon() {
		if (outdeg() == 0)
			return null;

		return lastOutEdge().getTarget();
	}

	@Override
	public JJEdge lastOutEdge() {
		return outEdges.getLast();
	}

	@Override
	public JJEdge firstOutEdge() {
		return outEdges.getFirst();
	}

	@Override
	public JJEdge firstInEdge() {
		return inEdges.getFirst();
	}

	@Override
	public JJEdge lastInEdge() {
		return inEdges.getLast();
	}

	// Tree only !

	@Override
	public JJNode brother() {
		final JJEdge kante = inEdge();

		if (kante != null) {
			JJEdge res;

			res = kante.adjSucc();

			if (res == null)
				res = kante.adjPred();

			if (res != null)
				return res.getTarget(); // g.target(res);
		}
		return null;
	}

	// Tree only !

	@Override
	public JJNode leftBrother() {
		if (indeg() == 0)
			return null;

		final JJEdge tmpE = inEdge().adjPred();
		// v.getGraph().adjPred((JJEdge)v.getInEdges().getFirst());
		if (tmpE != null)
			return tmpE.getTarget();

		return null;
	}

	// Tree only !

	@Override
	public JJNode rightBrother() {
		if (indeg() == 0)
			return null;

		final JJEdge tmpE = inEdge().adjSucc();
		// v.getGraph().adjSucc((JJEdge)v.getInEdges().getFirst());
		if (tmpE != null)
			return tmpE.getTarget();

		return null;
	}

	// Tree only !
	@Override
	public JJNode uncle() {
		return father().brother();
	}

	@Override
	public JJNode grandFather() {
		return father().father();
	}

	@Override
	public JJNode father() {
		if (indeg() == 0)
			return null;

		return inEdge().getSource();
	}

	// Tree only !

	@Override
	public JJEdge inEdge() {

		if (indeg() == 0)
			return null;

		if (indeg() != 1) {
			Debug.println("Tree operation inEdge on non-tree!");
		}

		return firstInEdge();
	}

} // JJNodeImpl
