/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

/**
 * JJInspector.java
 *
 *
 * Created: Thu May 04 09:46:51 2000
 *
 * @author
 * @version
 */
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeListener;

public class JJInspector extends JDialog implements ChangeListener {

	JJGraphWindow fenster;
	JJGraph graph;
	JTabbedPane tabbedPane;
	Set<JJInspectable> toolSet = new HashSet<>();

	@Override
	public void stateChanged(final javax.swing.event.ChangeEvent e) {
		// Debug.println("State changed to " +
		// tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()));
	}

	public JJInspector(final JFrame owner, final JJGraphWindow f) {
		super(owner);
		fenster = f;
		graph = fenster.getGraph();

		tabbedPane = new JTabbedPane();
		final Container contentPane = getContentPane();
		tabbedPane.addChangeListener(this);

		contentPane.add(tabbedPane, BorderLayout.CENTER);

		setTitle("Inspector");
		// setVisible(true);

		final Dimension size = new Dimension(430, 400);

		tabbedPane.setMaximumSize(size);
		tabbedPane.setPreferredSize(size);
		tabbedPane.setMinimumSize(size);
		tabbedPane.setSize(size);

		// pack();
	}

	// {
	// //Force the window to be 400+ pixels wide.
	// public Dimension getPreferredSize() {
	// Dimension size = super.getPreferredSize();
	// size.width = 400;
	// return size;
	// }
	// };
	//
	// private boolean ft = true;

	public void addTool(final JJInspectable g) {
		if (!toolSet.contains(g)) {
			tabbedPane.addTab(g.getTabName(), g.createTab());
			toolSet.add(g);
		}
	}

	public JJInspectable getTool(final Class<? extends JJInspectable> cl) {
		for (final JJInspectable o : toolSet) {
			if (o.getClass().equals(cl))
				return o;
		}
		return null;
	}

} // JJInspector
