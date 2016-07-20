/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.clustering;

/**
 * JJNodeClusterPair.java
 *
 *
 * Created: Tue May 18 22:33:20 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import org.carsten.jjgraph.graph.JJNode;

public class JJNodeClusterPair {

	JJNode n1 = null;
	int cluster = -1;

	public JJNodeClusterPair(final JJNode tmpN1, final int cl) {
		n1 = tmpN1;
		cluster = cl;
	}

	boolean isValid() {
		if ((n1 != null) && (cluster >= 0))
			return true;
		return false;
	}

} // JJNodeClusterPair
