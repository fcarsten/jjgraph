/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

/**
 * JJHelp.java
 *
 *
 * Created: Thu May 04 09:46:51 2000
 *
 * @author
 * @version
 */
import java.awt.Dimension;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.carsten.jjgraph.util.Debug;

public class JJHelp extends JFrame {

	public JJHelp(final String helpfile) {
		final URLClassLoader cl = (URLClassLoader) this.getClass().getClassLoader();

		final URL url = cl.findResource(helpfile);
		try {
			final JEditorPane tf = new JEditorPane(url);
			tf.setPreferredSize(new Dimension(500, 650));
			getContentPane().add(new JScrollPane(tf));
		} catch (final java.io.IOException e) {

			Debug.println(e.getMessage());
			final JTextArea tf = new JTextArea("Couldn't load help file");
			getContentPane().add(tf);
		}

		setTitle("JJGraph Help");
	}

} // JJHelp
