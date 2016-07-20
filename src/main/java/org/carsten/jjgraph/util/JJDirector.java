/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

/**
  * JJDirector.java
  *
  *
  * Created: Thu May 11 14:58:53 2000
  *
  * @author
  * @version
  */
import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphTool;
import org.carsten.jjgraph.graph.JJGraphWindowImpl;
import org.carsten.jjgraph.graph.JJGraphicNode;
import org.carsten.jjgraph.graph.JJNode;

public class JJDirector implements JJGraphTool {
	private final TreeSet<JJEvent> events = new TreeSet<>();
	private TreeSet<JJEvent> runningEvents;
	private final List<JJNode> dependingList = new LinkedList<>();
	private final JJGraph graph;
	private final JJGraphWindowImpl fenster;

	private int maxTime = 0;
	private int currentTime = 0;
	int frameCounter = 0;

	/**
	 * Get the value of currentTime.
	 *
	 * @return Value of currentTime.
	 */
	public int getCurrentTime() {
		return currentTime;
	}

	/**
	 * Set the value of currentTime.
	 *
	 * @param v
	 *            Value to assign to currentTime.
	 */
	public void setCurrentTime(final int v) {
		this.currentTime = v;
	}

	/**
	 * Get the value of maxTime.
	 *
	 * @return Value of maxTime.
	 */
	public int getMaxTime() {
		return maxTime;
	}

	/**
	 * Set the value of maxTime.
	 *
	 * @param v
	 *            Value to assign to maxTime.
	 */
	public void setMaxTime(final int v) {
		this.maxTime = v;
	}

	public JJDirector(final JJGraphWindowImpl f) {
		graph = f.getGraph();
		fenster = f;
		graph.addTool("JJDirector", this);
	}

	public void addEvent(final JJNode n, final int type, final int time) {
		if ((type == JJEvent.ADD) && (time == 0))
			dependingList.add(n);
		else
			events.add(new JJEvent(n, type, time));

		maxTime = Math.max(maxTime, time);
	}

	public boolean hasNext() {
		return !runningEvents.isEmpty();
	}

	public boolean nextEvent() {
		if (hasNext()) {
			final boolean tmpRedraw = fenster.setRedraw(false);

			JJEvent e = runningEvents.first();
			currentTime = e.time;

			Debug.print("Executing ");
			do {
				runningEvents.remove(e);
				final JJGraphicNode gn = e.node.getGraphicNode(fenster);

				if (e.type == JJEvent.ADD) {
					gn.unhide();
					Debug.print(" add ");
					fenster.repaint(gn);
				} else if (e.type == JJEvent.REMOVE) {
					gn.hide();
					Debug.print(" remove ");
					fenster.repaint(gn);
				}

				Debug.println(" node '" + e.node.getName() + "' at time " + e.time);
				if (hasNext()) {
					e = runningEvents.first();
				} else
					e = null;
			} while ((e != null) && (e.time == currentTime));

			updateDependingList();
			fenster.setRedraw(tmpRedraw);
			// fenster.saveImage("U:\\mpeg\\frame" + frameCounter++ + ".jpeg");
		}
		return hasNext();
	}

	int visibleNeighbors(final JJNode n) {
		int neigh = 0;
		for (final Iterator<JJEdge> iter = n.edgeIterator(); iter.hasNext();) {
			final JJGraphicNode gn = iter.next().opposite(n).getGraphicNode(fenster);
			if (gn.isVisible())
				neigh++;
		}
		return neigh;
	}

	void updateDependingList() {
		for (final JJNode tmpN : dependingList) {
			final JJGraphicNode gn = tmpN.getGraphicNode(fenster);
			if (visibleNeighbors(tmpN) >= 2) {
				gn.unhide();
				// fenster.repaint(gn);
			} else
				gn.hide();
		}
	}

	@SuppressWarnings("unchecked")
	public void rewind() {
		final boolean tmpRedraw = fenster.setRedraw(false);
		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			final JJGraphicNode gn = iter.next().getGraphicNode(fenster);
			gn.hide();
			fenster.repaint(gn);
		}

		fenster.setRedraw(tmpRedraw);
		frameCounter = 0;
		runningEvents = (TreeSet<JJEvent>) events.clone();
	}

	// void updateEdges()
	// {
	// for(Iterator iter = graph.edgeIterator(); iter.hasNext();){
	// JJEdge tmpE = (JJEdge) iter.next();
	// if(tmpE.getTarget().getGraphicNode().isVisible() &&
	// tmpE.getSource().getGraphicNode().isVisible())
	// }

	// }

} // JJDirector
