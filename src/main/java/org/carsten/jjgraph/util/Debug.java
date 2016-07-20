/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Debug
 *
 *
 * Created: Sun Nov 19 14:50:04 2000
 *
 * @author
 * @version
 */
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

public final class Debug extends OutputStream {
	static public final boolean DEBUG = true;
	static private JFrame outputWindow;
	static JTextArea outputArea;
	public static boolean isApplet = true;

	@Override
	public void write(final int b) {
		if (outputArea == null) {
			System.err.write(b);
		} else
			outputArea.append("" + ((char) b));
	}

	static public void println(final String s) {
		if (DEBUG) {
			if ((outputArea == null) || (!outputWindow.isDisplayable())) {
				System.err.println(s);
			} else
				outputArea.append(s + "\n");
		}
	}

	static public void print(final String s) {
		if (DEBUG) {
			if (outputArea == null)
				System.err.print(s);
			else
				outputArea.append(s);
		}
	}

	static public void showWindow() {
		if (DEBUG) {
			if (outputWindow == null) {
				outputWindow = new JFrame("Output Window");

				outputWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

				System.setErr(new PrintStream(new Debug()));

				System.setOut(new PrintStream(new Debug()));

				outputArea = new JTextArea();
				final java.awt.Container pane = outputWindow.getContentPane();

				final JScrollPane scrollPane = new JScrollPane(outputArea);
				scrollPane.setPreferredSize(new Dimension(600, 400));
				scrollPane.setSize(new Dimension(600, 400));

				final JButton clearB = new JButton("Clear");
				clearB.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent ae) {
						outputArea.setText("");
					}
				});

				pane.setLayout(new BorderLayout());

				pane.add(scrollPane, BorderLayout.CENTER);
				pane.add(clearB, BorderLayout.SOUTH);
			}

			outputWindow.pack();
			outputWindow.setVisible(true);
		}
	}

	static void hideWindow() {
		if (outputWindow != null) {
			outputWindow.dispose();
		}
	}

} // Debug
