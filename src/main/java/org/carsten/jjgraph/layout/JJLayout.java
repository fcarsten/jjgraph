/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.layout;

/**
 * JJLayout.java
 *
 *
 * Created: Mon Apr 17 16:21:22 2000
 *
 * @author Carsten Friedrich
 * @version
 */
import java.util.Collection;

import org.carsten.jjgraph.graph.JJNode;

public interface JJLayout {

	void apply(String s);

	boolean canDo(String s);

	void layout();

	void layout(Collection<JJNode> c);

	String getName();

	int allowsOptimize();

} // JJLayout
