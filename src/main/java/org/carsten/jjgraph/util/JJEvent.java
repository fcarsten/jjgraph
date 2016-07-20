/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

/**
 * JJEvent.java
 *
 *
 * Created: Thu May 11 15:53:24 2000
 *
 * @author
 * @version
 */
import org.carsten.jjgraph.graph.JJNode;

public class JJEvent implements Comparable<JJEvent> {
	public final static int ADD = 0;
	public final static int REMOVE = 1;
	JJNode node;
	int type;
	int time;

	public JJEvent(final JJNode n, final int t, final int x) {
		node = n;
		type = t;
		time = x;
	}

	int compareRest(final JJEvent e) {
		if (e.type != type)
			return e.type - type;

		if (e.node == node)
			return 0;

		return e.toString().compareTo(this.toString());

	}

	@Override
	public int compareTo(final JJEvent e) {
		if (e.time == time)
			return compareRest(e);
		if (e.time < time)
			return 1;
		return -1;
	}
} // JJEvent
