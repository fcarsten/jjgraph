/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.layout;

/**
 * JJShake.java
 *
 *
 * Created: Wed Dec  8 11:15:52 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import java.util.Collection;
import java.util.Iterator;

import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJGraphicNode;
import org.carsten.jjgraph.graph.JJNode;
import org.carsten.jjgraph.util.JJPoint;

public class JJShake implements JJLayout {
	private final JJGraphWindow fenster;
	private final JJGraph graph;

	@Override
	public int allowsOptimize() {
		return 0; // JJLayOpt.TRANSLATE | JJLayOpt.SCALE;
	}

	public JJShake(final JJGraphWindow f) {
		fenster = f;
		graph = f.getGraph();
	}

	@Override
	public void layout() {
		for (final Iterator<JJNode> nodeIter = graph.nodeIterator(); nodeIter.hasNext();) {
			final JJNode tmpN = nodeIter.next();
			final JJGraphicNode gn = tmpN.getGraphicNode(fenster);
			final JJPoint coords = gn.getCoords();

			fenster.moveNodeTo(gn, coords.x + 10 / fenster.getZoom() * (-0.5 + Math.random()),
					coords.y + 10 / fenster.getZoom() * (-0.5 + Math.random()));
		}
	}

	@Override
	public void layout(final Collection<JJNode> c) {
		layout();
	}

	@Override
	public String getName() {
		return "Shake layout";

	}

	@Override
	public void apply(final String s) {
		layout();
	}

	@Override
	public boolean canDo(final String s) {
		return s.equals(getName());

	}

} // JJShake
