/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * JJEdgeImpl.java
 *
 *
 * Created: Fri Feb 26 13:08:56 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import org.carsten.jjgraph.util.Debug;
import org.carsten.jjgraph.util.JJUndo;

public class JJEdgeImpl implements JJEdge {
	private JJNode source = null;
	private JJNode target = null;
	private int value;
	private double weight = 1.0;
	private double length = 1.0;
	private String name = "";

	// private double transparency =1.0;

	// public double getTransparency() {return transparency;}
	// public void setTransparency(double v) {this.transparency = v;}

	// public Color getColor() {
	// if(ge != null)
	// return ge.getColor();

	// return null;
	// }

	// public void setColor(Color v) {
	// if((ge != null)&&(v!=null))
	// ge.setColor(v);
	// }

	private final Map<JJGraphWindow, JJGraphicEdge> windowMap = new IdentityHashMap<>();

	@Override
	public JJGraph getGraph() {
		return source.getGraph();
	}

	@Override
	public void revert() {
		final JJUndo undo = getGraph().getUndoManager();
		if (undo.getUndoRecording()) {
			undo.add("Revert edge", this, revert, null);
		}

		target.delInEdge(this);
		source.delOutEdge(this);

		final JJNode tmpN = source;
		source = target;
		target = tmpN;

		if (windowMap != null) {
			final Set<Entry<JJGraphWindow, JJGraphicEdge>> es = windowMap.entrySet();
			for (final Entry<JJGraphWindow, JJGraphicEdge> element : es) {
				final JJGraphicEdge ge = element.getValue();
				ge.revertBends();
			}
		}

		target.addInEdge(this);
		source.addOutEdge(this);

		_repaint();
	}

	@Override
	public void setName(final String v) {
		final JJUndo undo = getGraph().getUndoManager();
		if (undo.getUndoRecording()) {
			final Object par[] = new Object[1];
			par[0] = this.name;
			undo.add("Change edge name", this, set_name, par);
		}

		_repaint();
		this.name = v;

		if (windowMap != null) {
			final Set<Entry<JJGraphWindow, JJGraphicEdge>> es = windowMap.entrySet();
			for (final Entry<JJGraphWindow, JJGraphicEdge> entry : es) {
				final JJGraphicEdge ge = entry.getValue();
				ge.recomputeEdgeSize();
			}
		}

		// if(getGraphicEdge() != null)
		// getGraphicEdge().updateLabelWidth();

		_repaint();
	}

	private void _repaint() {
		if (windowMap != null) {
			final Set<Entry<JJGraphWindow, JJGraphicEdge>> es = windowMap.entrySet();
			for (final Entry<JJGraphWindow, JJGraphicEdge> entry : es) {
				final JJGraphicEdge ge = entry.getValue();
				ge.repaint();
			}
		}
	}

	/**
	 * Get the value of ge.
	 *
	 * @return Value of ge.
	 */

	@Override
	public JJGraphicEdge getGraphicEdge(final JJGraphWindow w) {
		return windowMap.get(w);
	}

	/**
	 * Set the value of ge.
	 *
	 * @param v
	 *            Value to assign to ge.
	 */
	@Override
	public void setGraphicEdge(final JJGraphicEdge v, final JJGraphWindow w) {
		windowMap.put(w, v);
	}

	/**
	 * Get the value of weight.
	 *
	 * @return Value of weight.
	 */
	@Override
	public double getWeight() {
		return weight;
	}

	/**
	 * Set the value of weight.
	 *
	 * @param v
	 *            Value to assign to weight.
	 */
	@Override
	public void setWeight(final double v) {
		this.weight = v;
	}

	/**
	 * Get the value of length.
	 *
	 * @return Value of length.
	 */

	@Override
	public double getLength() {
		return length;
	}

	/**
	 * Set the value of length.
	 *
	 * @param v
	 *            Value to assign to length.
	 */
	@Override
	public void setLength(final double v) {
		this.length = v;
	}

	/**
	 * Get the value of name.
	 *
	 * @return Value of name.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Set the value of name.
	 *
	 * @param v
	 *            Value to assign to name.
	 */

	@Override
	public JJNode getTarget() {
		return target;
	}

	@Override
	public JJNode getSource() {
		return source;
	}

	@Override
	public void init(final JJNode n1, final JJNode n2) {
		source = n1;
		target = n2;
	}

	/**
	 * Get the value of value.
	 *
	 * @return Value of value.
	 */
	@Override
	public int getValue() {
		return value;
	}

	/**
	 * Set the value of value.
	 *
	 * @param v
	 *            Value to assign to value.
	 */
	@Override
	public void setValue(final int v) {
		this.value = v;
	}

	static private Method set_name;
	static private Method revert;

	static {
		final Class<? extends JJEdge> jjedge = org.carsten.jjgraph.graph.JJEdge.class;

		try {
			{
				@SuppressWarnings("unchecked")
				final Class<String>[] parT = new Class[1];
				parT[0] = String.class;
				set_name = jjedge.getMethod("setName", parT);
			}
			{
				revert = jjedge.getMethod("revert", (Class<?>[]) null);
			}
		} catch (final java.lang.NoSuchMethodException e) {
			Debug.println("NoSuchMethodException: " + e.getMessage() + " :");
		}
	}

	@Override
	public JJEdge adjPred() {
		final JJNode knoten = getSource();
		// int index = knoten.getOutEdges().indexOf(this);
		final int index = knoten.outIndex(this);
		if (index < 1)
			return null;

		return knoten.outEdge(index - 1);
	}

	@Override
	public JJEdge adjSucc() {
		final JJNode knoten = getSource();
		final int index = knoten.outIndex(this);
		if (index > knoten.outdeg() - 2)
			return null;

		return knoten.outEdge(index + 1);
	}

	@Override
	public JJNode opposite(final JJNode n) {
		if (getSource() == n) {
			return getTarget();
		}
		return getSource();
	}

	@Override
	public boolean contains(final JJNode n) {
		if ((n == getSource()) || (n == getTarget()))
			return true;
		return false;

	}

}
