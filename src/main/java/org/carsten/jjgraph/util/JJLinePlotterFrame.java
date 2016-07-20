/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

/**
 * JJGraphWindow.java
 *
 *
 * Created: Fri Feb 26 18:35:41 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class JJLinePlotterFrame extends JFrame implements ChangeListener {
	private final JJLinePlotter panel;
	JSlider opacitySlider;

	public JJLinePlotter getPanel() {
		return panel;
	}

	private JScrollPane scrollPane;

	public Component createComponents() {
		final java.awt.Container pane = getContentPane();

		// panel.setPreferredSize(new Dimension(10000, 10000));
		// panel.setSize(new Dimension(10000, 10000));

		scrollPane = new JScrollPane(panel);
		scrollPane.setPreferredSize(new Dimension(600, 550));
		scrollPane.setSize(new Dimension(600, 550));

		// JPanel p = new JPanel();

		opacitySlider = new JSlider(0, 255, 20);
		opacitySlider.addChangeListener(this);

		pane.setLayout(new BorderLayout());

		pane.add(scrollPane, BorderLayout.CENTER);
		pane.add(opacitySlider, BorderLayout.SOUTH);

		return pane;
	}

	public JJLinePlotterFrame(final JJLinePlotter fenster) {
		super("JJLinePlotter ");
		panel = fenster;
	}

	@Override
	public void stateChanged(final ChangeEvent e) {
		if (opacitySlider.getValueIsAdjusting())
			panel.repaint();
		else
			Debug.println("No real change");

	}

	public void initComponents() {
		final Component contents = createComponents();
		pack();
		setVisible(true);
	}

}
