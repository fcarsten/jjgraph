/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

import java.awt.BorderLayout;

/**
 * MMWindow.java
 *
 *
 * Created: Fri Mar  5 17:17:34 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import javax.swing.JFrame;

import sunSupp.MemoryMonitor;

public class MMWindow extends JFrame {

	public MMWindow() {
		super("Memory monitor");

		final MemoryMonitor mm = new MemoryMonitor();

		getContentPane().add(mm, BorderLayout.CENTER);
		// mm.start();
		pack();
		setVisible(true);
	}

} // MMWindow
