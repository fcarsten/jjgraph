/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

public class JJNumberField extends JJRegexpField {

	public JJNumberField(final int cols) {
		super(cols);
		init();
	}

	public JJNumberField(final String cols) {
		super(cols);
		init();
	}

	public JJNumberField(final String str, final int cols) {
		super(str, cols);
		init();
	}

	private void init() {
		setRegexp("[0-9]*");
	}

} // JJRegexpField
