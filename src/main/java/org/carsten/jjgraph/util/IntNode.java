/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

/**
 * IntNode.java
 *
 *
 * Created: Wed Apr 19 19:00:08 2000
 *
 * @author Carsten Friedrich
 * @version
 */

public class IntNode {
	IntNode pred;
	IntNode succ;
	int value;

	public IntNode succ() {
		return succ;
	}

	public IntNode pred() {
		return pred;
	}

	public int getValue() {
		return value;
	}

	public IntNode(final int i) {
		value = i;
	}

} // IntNode
