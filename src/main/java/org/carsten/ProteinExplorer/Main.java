/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.ProteinExplorer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JLabel;

import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphImpl;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.util.Debug;

public class Main extends JApplet implements ActionListener {
	private JButton button;

	public Main() {
		if (!Debug.isApplet)
			getRootPane().putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);
	}

	public static void main(final String[] args) {
		// System.getProperties().list(System.out);

		Debug.isApplet = false;
		// new MMWindow();
		if (args.length == 0) {
			final JJGraph graph = new ProteinGraph();
			final JJGraphWindow fenster = graph.createGraphic();
		} else
			for (final String arg : args) {
				final JJGraph graph = new ProteinGraph();
				final JJGraphWindow w = graph.createGraphic();
				w.setRedraw(false);
				w.setBusy(true);
				try {
					graph.parseFile(arg);
				} catch (final IOException e) {
					System.out.println(e.getMessage());
				} finally {
					w.setRedraw(true);
					w.setBusy(false);
				}

			}
	}

	@Override
	public void actionPerformed(final java.awt.event.ActionEvent e) {
		button.setText("Start JJGraph");
		try {
			final JJGraph graph = new JJGraphImpl();
			graph.createGraphic();
		} catch (final Exception ex) {
			Debug.println("Exception : " + ex.getMessage());

		}

	}

	@Override
	public void init() {
		button = new JButton("Start JJGraph");
		button.addActionListener(this);

		button.setHorizontalAlignment(JLabel.CENTER);
		button.setBorder(BorderFactory.createMatteBorder(1, 1, 2, 2, Color.black));

		getContentPane().add(button, BorderLayout.CENTER);
	}

}
