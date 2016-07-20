/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.misc;

import java.awt.Toolkit;
import java.text.NumberFormat;

import javax.swing.JTextField;

public class DecimalField extends JTextField {
	private final NumberFormat format;

	public DecimalField(final double value, final int columns, final NumberFormat f) {
		super(columns);
		setDocument(new FormattedDocument(f));
		format = f;
		setValue(value);
	}

	public double getValue() {
		double retVal = 0.0;

		try {
			retVal = format.parse(getText()).doubleValue();
		} catch (final java.text.ParseException e) {
			// This should never happen because insertString allows
			// only properly formatted data to get in the field.
			Toolkit.getDefaultToolkit().beep();
			System.err.println("getValue: could not parse: " + getText());
		}
		return retVal;
	}

	public void setValue(final double value) {
		setText(format.format(value));
	}
}
