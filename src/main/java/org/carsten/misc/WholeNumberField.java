/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.misc;

import java.awt.Toolkit;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public class WholeNumberField extends JTextField {
	private final Toolkit toolkit;
	private final NumberFormat integerFormatter;

	public WholeNumberField(final int value, final int columns) {
		super(columns);
		toolkit = Toolkit.getDefaultToolkit();
		integerFormatter = NumberFormat.getNumberInstance(Locale.US);
		integerFormatter.setParseIntegerOnly(true);
		setValue(value);
	}

	public int getValue() {
		int retVal = 0;
		try {
			retVal = integerFormatter.parse(getText()).intValue();
		} catch (final ParseException e) {
			// This should never happen because insertString allows
			// only properly formatted data to get in the field.
			toolkit.beep();
		}
		return retVal;
	}

	public void setValue(final int value) {
		setText(integerFormatter.format(value));
	}

	@Override
	protected Document createDefaultModel() {
		return new WholeNumberDocument();
	}

	protected class WholeNumberDocument extends PlainDocument {
		@Override
		public void insertString(final int offs, final String str, final AttributeSet a) throws BadLocationException {
			final char[] source = str.toCharArray();
			final char[] result = new char[source.length];
			int j = 0;

			for (int i = 0; i < result.length; i++) {
				if (Character.isDigit(source[i]))
					result[j++] = source[i];
				else {
					toolkit.beep();
					System.err.println("insertString: " + source[i]);
				}
			}
			super.insertString(offs, new String(result, 0, j), a);
		}
	}
}
