/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

/**
 * JJAuxStruct.java
 *
 *
 * Created: Mon Dec  6 15:06:29 1999
 *
 * @author Carsten Friedrich
 * @version
 */

import java.util.HashMap;

import org.carsten.jjgraph.graph.JJNode;
import org.carsten.jjgraph.graph.JJNodePair;

public class JJAuxStruct {
	private final HashMap<JJNodePair, JJNode> aux1;
	private final HashMap<JJNodePair, JJNode> aux2;

	public JJAuxStruct() {
		aux1 = new HashMap<>();
		aux2 = new HashMap<>();
	}

	public void clear() {
		aux1.clear();
		aux2.clear();
	}

	public boolean defined(final JJNodePair p) {
		if ((aux1.get(p) != null) || (aux2.get(p) != null))
			return true;

		return false;
	}

	public boolean defined(final JJNodePair p, final JJNode v) {
		if (((aux1.get(p) != null) && (aux1.get(p) == v)) || ((aux2.get(p) != null) && (aux2.get(p) == v)))
			return true;

		return false;
	}

	public JJNode get(final JJNodePair p) {
		if (aux1.get(p) != null)
			return aux1.get(p);

		if (aux2.get(p) != null)
			return aux2.get(p);

		// assert("operator[] with invalid JJNodePair" == NULL);
		return null;
	}

	public void undefine(final JJNodePair p, final JJNode v) {
		if ((aux1.get(p) != null) && (aux1.get(p) == v))
			aux1.remove(p);

		if ((aux2.get(p) != null) && (aux2.get(p) == v))
			aux2.remove(p);
	}

	public void define(final JJNodePair p, final JJNode v) {
		if (aux1.get(p) != null) {
			if (aux1.get(p) == v)
				return;

			if (aux2.get(p) != null) {
				if (aux2.get(p) == v)
					return;

				// assert("define out of capacity" == NULL);
			}

			aux2.put(p, v);
		} else {
			if ((aux2.get(p) != null) && (aux2.get(p) == v))
				return;

			aux1.put(p, v);
		}
	}

} // JJAuxStruct
