/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

/**
 * IntList.java
 *
 *
 * Created: Wed Apr 19 19:02:37 2000
 *
 * @author Carsten Friedrich
 * @version
 */

public class IntList {
	IntNode first;
	IntNode last;

	public void del(final IntNode n) {

		if (n == first) {
			first = n.succ;
			if (first != null) {
				first.pred = null;
			} else
				last = null;
		} else if (n == last) {
			last = n.pred;
			if (last != null)
				last.succ = null;
		} else {
			IntNode tmpN = first;
			while (tmpN != null) {
				if (tmpN == n) {
					n.succ.pred = n.pred;
					n.pred.succ = n.succ;
					break;
				}
				tmpN = tmpN.succ;
			}
		}
		n.succ = null;
		n.pred = null;
	}

	public boolean contains(final int i) {
		IntNode tmpN = first;
		while (tmpN != null) {
			if (tmpN.getValue() == i)
				return true;
			tmpN = tmpN.succ;
		}

		return false;
	}

	public void add(final int i) {
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

	public void append(final int n) {
		add(n);
	}

	public IntNode first() {
		return first;
	}

	// public IntNode succ(IntNode p)
	// {
	// return p.succ;
	// }

	// public IntNode pred(IntNode p)
	// {
	// return p.pred;
	// }

	public int getValue(final IntNode p) {
		return p.value;
	}

} // IntList
