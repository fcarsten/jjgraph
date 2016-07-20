/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

/**
 * JJMisc.java
 *
 *
 * Created: Mon Dec  6 17:38:02 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import java.util.Iterator;

public class JJMisc {

	public static void moveEdges(final JJNode start, final JJNode end, final JJNode dest) {
		moveEdges(start.inEdge(), end.inEdge(), dest);
	}

	public static void moveEdges(final JJNode start, final JJNode end, final JJEdge dest, final int dir) {
		moveEdges(start.inEdge(), end.inEdge(), dest, dir);
	}

	public static void moveEdges(final JJEdge start, final JJEdge end, final JJEdge dest, final int dir) {
		final JJGraph g = start.getGraph(); // graph_of(start);
		JJEdge loopE = start;
		int loopDir = dir;
		JJEdge newDest = dest;

		while (true) {
			final JJEdge newLoopE = loopE.adjSucc();
			final JJNode tmpTarget = loopE.getTarget(); // g.target(loopE);
			newDest = g.addEdge(newDest, tmpTarget, loopDir);
			loopDir = JJGraph.after;

			g.deleteEdge(loopE);
			if (loopE == end)
				break;
			loopE = newLoopE;
			// assert(loopE);
		}
	}

	static public JJNode firstSuccessor(final JJNode vater, final JJNode n1, final JJNode n2) {
		for (final Iterator<JJEdge> iter = vater.outIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();

			if (tmpE.getTarget() == n1)
				return n1;
			if (tmpE.getTarget() == n2)
				return n2;
		}
		return null;
	}

	public static void moveEdges(final JJEdge start, final JJEdge end, final JJNode dest) {
		final JJGraph g = start.getGraph(); // graph_of(start);
		// assert(g.source(start) == g.source(end));
		JJEdge loopE = start;

		while (true) {
			final JJEdge newLoopE = loopE.adjSucc();
			final JJNode tmpTarget = loopE.getTarget(); // g.target(loopE);
			g.addEdge(dest, tmpTarget);
			g.deleteEdge(loopE);
			if (loopE == end)
				break;
			loopE = newLoopE;
			// assert(loopE);
		}
	}

	static public JJEdge findEdgeBetween(final JJNode n1, final JJNode n2) {
		for (final Iterator<JJEdge> iter = n1.outIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			if (tmpE.getTarget() == n2)
				return tmpE;
		}

		for (final Iterator<JJEdge> iter = n2.outIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			if (tmpE.getTarget() == n1)
				return tmpE;
		}

		return null;
	}

} // JJMisc
