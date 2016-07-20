/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

/**
 * JJLayoutMenuHandler.java
 *
 *
 * Created: Mon Apr 17 17:01:49 2000
 *
 * @author Carsten Friedrich
 * @version
 */
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JCheckBoxMenuItem;

import org.carsten.jjgraph.layopt.JJLayOpt;
import org.carsten.jjgraph.layopt.JJLayOptWindow;
import org.carsten.jjgraph.layout.JJLayout;
import org.carsten.jjgraph.util.Debug;

public class JJLayoutMenuHandler implements ActionListener {
	private boolean optimize = false;
	private final JJLayOpt optimizer;

	private final LinkedList<JJLayout> layoutAlgos = new LinkedList<>();
	private final JJGraphWindow window;
	private JJLayOptWindow layoptPanel;

	/**
	 * Get the value of layoptPanel.
	 *
	 * @return value of layoptPanel.
	 */
	public JJLayOptWindow getLayoptPanel() {
		return layoptPanel;
	}

	/**
	 * Set the value of layoptPanel.
	 *
	 * @param v
	 *            Value to assign to layoptPanel.
	 */
	public void setLayoptPanel(final JJLayOptWindow v) {
		this.layoptPanel = v;
	}

	public JJLayoutMenuHandler(final JJGraphWindow g, final JJGraphFrame f) {
		window = g;
		setLayoptPanel(new JJLayOptWindow(f.getPanel()));
		optimizer = layoptPanel.getOptimizer();
	}

	public void add(final JJLayout l) {
		layoutAlgos.add(l);
	}

	public void apply(final String s) {
		for (final JJLayout l : layoutAlgos) {
			if (l.canDo(s)) {
				// Debug.println("Using: " + l.getName());
				window.getGraph().getUndoManager().openSubtask(l.getName());
				window.setBusy(true);
				window.removeBends();
				if (optimize && (l.allowsOptimize() != 0))
					optimizer.setStart();

				l.apply(s);

				if (optimize && (l.allowsOptimize() != 0)) {
					final int oldSettings = optimizer.getOptimizeFlag();
					optimizer.setOptimizeFlag(l.allowsOptimize());
					optimizer.setEnd();
					optimizer.optimize();
					optimizer.setOptimizeFlag(oldSettings);
				}

				window.getGraph().getUndoManager().closeSubtask(l.getName());
				window.setBusy(false);
				return;
			}
		}
		Debug.println("Couldn't find layout \"" + s + "\"");

	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getActionCommand().equals("optimizeLayout")) {
			optimize = ((JCheckBoxMenuItem) e.getSource()).isSelected();
		} else
			apply(e.getActionCommand());

	}

} // JJLayoutMenuHandler
