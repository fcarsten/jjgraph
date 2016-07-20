/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.misc;

import java.awt.Toolkit;
import java.text.Format;
import java.text.ParseException;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class FormattedDocument extends PlainDocument {
	private final Format format;

	public FormattedDocument(final Format f) {
		format = f;
	}

	public Format getFormat() {
		return format;
	}

	@Override
	public void insertString(final int offs, final String str, final AttributeSet a) throws BadLocationException {

		final String currentText = getText(0, getLength());
		final String beforeOffset = currentText.substring(0, offs);
		final String afterOffset = currentText.substring(offs, currentText.length());
		final String proposedResult = beforeOffset + str + afterOffset;

		try {
			format.parseObject(proposedResult);
			super.insertString(offs, str, a);
		} catch (final ParseException e) {
			Toolkit.getDefaultToolkit().beep();
			System.err.println("insertString: could not parse: " + proposedResult);
		}
	}

	@Override
	public void remove(final int offs, final int len) throws BadLocationException {
		final String currentText = getText(0, getLength());
		final String beforeOffset = currentText.substring(0, offs);
		final String afterOffset = currentText.substring(len + offs, currentText.length());
		final String proposedResult = beforeOffset + afterOffset;

		try {
			if (proposedResult.length() != 0)
				format.parseObject(proposedResult);
			super.remove(offs, len);
		} catch (final ParseException e) {
			Toolkit.getDefaultToolkit().beep();
			System.err.println("remove: could not parse: " + proposedResult);
		}
	}
}
