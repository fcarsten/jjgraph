/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

/**
 * JJDfs.java
 *
 *
 * Created: Mon Dec  6 15:19:01 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import java.util.HashMap;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJNode;

public class DfsMap<K, M> extends HashMap<K, M> {
	public DfsEdge getEdge(final JJEdge e) {
		return (DfsEdge) super.get(e);
	}

	public DfsNode getNode(final JJNode e) {
		return (DfsNode) super.get(e);
	}
}
