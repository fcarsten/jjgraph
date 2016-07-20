/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

/**
 * IntSet.java
 *
 *
 * Created: Thu Apr 20 11:53:58 2000
 *
 * @author Carsten Friedrich
 * @version
 */

public class IntSet extends IntList {

	@Override
	public void add(final int i) {
		if (contains(i))
			return;

		final IntNode n = new IntNode(i);

		if (first == null) {
			first = n;
			last = n;
			return;
		}

		last.succ = n;
		n.pred = last;
		last = n;
	}

} // IntSet
