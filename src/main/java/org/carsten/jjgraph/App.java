/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph;

import java.io.IOException;

import javax.swing.UIManager;

import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphImpl;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.util.Debug;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(final String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception e) {
		}
		// System.getProperties().list(System.out);

		Debug.isApplet = false;
		// new MMWindow();
		if (args.length == 0) {
			final JJGraph graph = new JJGraphImpl();
			final JJGraphWindow fenster = graph.createGraphic();
		} else
			for (final String arg : args) {
				final JJGraph graph = new JJGraphImpl();
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
}
