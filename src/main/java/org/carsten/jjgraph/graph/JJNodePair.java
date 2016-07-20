/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

/**
 * JJNodePair.java
 *
 *
 * Created: Tue May 18 22:33:20 1999
 *
 * @author Carsten Friedrich
 * @version
 */

public class JJNodePair {

	public JJNode n1 = null;
	public JJNode n2 = null;
	public boolean neighbors = false;

	public JJNodePair(final JJNode tmpN1, final JJNode tmpN2) {
		n1 = tmpN1;
		n2 = tmpN2;
	}

	public JJNode getNodeOne() {
		return n1;
	}

	public JJNode getNodeTwo() {
		return n2;
	}

	public boolean isValid() {
		if ((n1 != null) && (n2 != null))
			return true;
		return false;
	}

} // JJNodePair
