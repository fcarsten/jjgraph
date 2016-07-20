/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

/**
 * JJGraphEvent.java
 *
 *
 * Created: Thu May 04 12:23:07 2000
 *
 * @author
 * @version
 */

public class JJGraphEvent {
	public final static int MISC_EVENT = 0;
	public final static int NODE_ADD_EVENT = 1;
	public final static int NODE_DEL_EVENT = 2;
	public final static int EDGE_ADD_EVENT = 3;
	public final static int EDGE_DEL_EVENT = 4;
	public final static int BULK_EVENT = 5;
	public final static int NODE_MOVE_EVENT = 6;
	public final static int EDGE_MOVE_EVENT = 7;
	public final static int NODE_COLOUR_EVENT = 8;
	public final static int EDGE_COLOUR_EVENT = 9;
	public final static int NODE_VISBILITY_CHANGE = 10;
	public final static int EDGE_VISBILITY_CHANGE = 11;
	public final static int ENABLE_REDRAW = 12;
	public final static int DISABLE_REDRAW = 13;
	private Object element;
	private int type;

	public JJGraphEvent(final Object e, final int t) {
		element = e;
	}

	public int getType() {
		return type;
	}

	public void setType(final int t) {
		type = t;
	}

	public JJNode getNode() {
		switch (type) {
		case NODE_ADD_EVENT:
		case NODE_DEL_EVENT:
			return (JJNode) element;
		default:
			return null;
		}
	}

	public JJEdge getEdge() {
		switch (type) {
		case EDGE_ADD_EVENT:
		case EDGE_DEL_EVENT:
			return (JJEdge) element;
		default:
			return null;
		}
	}

	public Object getSource() {
		return element;
	}

	public void setSource(final Object o) {
		element = o;
	}

} // JJGraphEvent
