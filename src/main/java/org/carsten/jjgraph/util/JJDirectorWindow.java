/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;

/**
 * JJDirectorWindow.java
 *
 *
 * Created: Mon Nov 15 11:12:12 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphEvent;
import org.carsten.jjgraph.graph.JJGraphListener;
import org.carsten.jjgraph.graph.JJGraphWindowImpl;
import org.carsten.jjgraph.graph.JJInspectable;

public class JJDirectorWindow extends JPanel implements ActionListener, JJInspectable, JJGraphListener {
	private JButton startPos;
	private JButton nextB;
	private JButton startB;
	private JButton stopB;
	private JProgressBar progress;

	private final JJGraph graph;
	private final JJDirector director;
	private final Timer timer = new Timer(40, this);

	@Override
	public String getTabName() {
		return "Director";
	}

	public void disableWidgets() {
		nextB.setEnabled(false);
		startB.setEnabled(false);
		stopB.setEnabled(false);
	}

	public void enableWidgets() {
		nextB.setEnabled(true);
		startB.setEnabled(true);
		stopB.setEnabled(true);
		timer.stop();
	}

	@Override
	public void graphStructureChanged(final JJGraphEvent e) {
		disableWidgets();
	}

	public void graphAppearanceChanged(final JJGraphEvent e) {
	}

	public JJDirectorWindow(final JJGraphWindowImpl g) {
		graph = g.getGraph();
		graph.addStructureListener(this);
		director = new JJDirector(g);
	}

	@Override
	public JPanel createTab() {
		final JPanel panel = new JPanel();

		final JLabel titleLabel = new JLabel("Director Control");

		startPos = new JButton("Init");
		startPos.addActionListener(this);

		nextB = new JButton("Step");
		nextB.addActionListener(this);
		nextB.setEnabled(false);

		startB = new JButton("Run");
		startB.addActionListener(this);
		startB.setEnabled(false);

		stopB = new JButton("Stop");
		stopB.addActionListener(this);
		stopB.setEnabled(false);

		final BorderLayout b1 = new BorderLayout();
		panel.setLayout(b1);

		final JPanel p = new JPanel();

		panel.add(titleLabel, BorderLayout.NORTH);

		p.add(startPos);
		p.add(nextB);
		p.add(startB);
		p.add(stopB);

		progress = new JProgressBar(0, 1);
		panel.add(progress, BorderLayout.CENTER);
		panel.add(p, BorderLayout.SOUTH);

		final JPanel arg = new JPanel();
		arg.setLayout(new BorderLayout());
		arg.add(panel, BorderLayout.NORTH);

		return arg;
	}

	@Override
	public void actionPerformed(final java.awt.event.ActionEvent e) {
		// Debug.println("Should blend now");
		if (e.getSource() == startPos) {
			director.rewind();
			progress.setMaximum(director.getMaxTime());
			progress.setValue(1);

			enableWidgets(); // nextB.setEnabled(true);
		} else if (e.getSource() == startB) {
			timer.start();
		} else if (e.getSource() == stopB) {
			timer.stop();
		} else if ((e.getSource() == nextB) || (e.getSource() == timer)) {
			if (!director.nextEvent())
				disableWidgets();

			// nextB.setEnabled(director.nextEvent());
			progress.setValue(director.getCurrentTime());
		} else {
			Debug.println("Unexpected action event");
		}
	}
} // JJDirectorWindow
