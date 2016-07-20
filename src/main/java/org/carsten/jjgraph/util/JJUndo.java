/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.carsten.jjgraph.graph.JJGraph;

/**
 * JJUndo.java
 *
 *
 * Created: Wed Apr 12 17:11:57 2000
 *
 * @author Carsten Friedrich
 * @version
 */

public class JJUndo {

	private final JJGraph graph;
	private JJUndoWindow fenster;
	private final List<JJUndoNode> undoList;
	private final static Object openSubtaskO = new Object();
	private final static Object closeSubtaskO = new Object();

	private boolean undoRecording = true;

	/**
	 * Get the value of undoRecording.
	 *
	 * @return Value of undoRecording.
	 */
	public boolean getUndoRecording() {
		return undoRecording;
	}

	/**
	 * Set the value of undoRecording.
	 *
	 * @param v
	 *            Value to assign to undoRecording.
	 */
	public void setUndoRecording(final boolean v) {
		this.undoRecording = v;
	}

	public JJUndo(final JJGraph g) {
		graph = g;
		undoList = new LinkedList<>();

		fenster = null;
	}

	public void setWindow(final JJUndoWindow f) {
		fenster = f;
		for (final JJUndoNode ui : undoList) {

			if (ui.getTarget() == openSubtaskO) {
				fenster.addFolder(ui.toString());
			} else if (ui.getTarget() == closeSubtaskO) {
				fenster.closeFolder();
			} else {
				fenster.addLeaf(ui.toString());
			}
		}

	}

	public void undo() throws JJUndoOnUnstableGraphException {
		if (undoRecording == false) {
			throw new JJUndoOnUnstableGraphException("Tried undo on unstable graph");
		}

		JJUndoNode undoInfo = undoList.remove(undoList.size() - 1);

		// Debug.println("Undo: " + undoInfo);

		while ((undoInfo.getTarget() == openSubtaskO) || (undoInfo.getTarget() == closeSubtaskO)) {
			undoInfo = undoList.remove(undoList.size() - 1);
			// Debug.println("Undo: " + undoInfo);
		}

		setUndoRecording(false);
		undoInfo.execute();
		setUndoRecording(true);
	}

	public void openSubtask(final String s) {
		if (!getUndoRecording())
			return;

		final JJUndoNode tmpN = new JJUndoNode(s, openSubtaskO, null, null);
		undoList.add(tmpN);

		if (fenster != null) {
			fenster.addFolder(s);
		}
	}

	public void closeSubtask(final String s) {
		if (!getUndoRecording())
			return;

		final JJUndoNode tmpN = new JJUndoNode(s, closeSubtaskO, null, null);
		undoList.add(tmpN);

		if (fenster != null) {
			fenster.closeFolder();
		}
	}

	public void add(final String s, final Object target, final Method method, final Object[] paras) {
		if (!getUndoRecording())
			return;

		// Debug.println("Adding " + method + " on target " + target);

		final JJUndoNode tmpN = new JJUndoNode(s, target, method, paras);
		undoList.add(tmpN);

		if (fenster != null) {
			fenster.addLeaf(s);
		}

	}

} // JJUndo
