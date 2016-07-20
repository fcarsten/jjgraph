/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

import java.util.regex.Pattern;

/**
 * JJRegexpField.java
 *
 *
 * Created: Thu Jul 12 23:14:36 2001
 *
 * @author
 * @version
 */
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public class JJRegexpField extends JTextField {

	private Pattern reg;

	public JJRegexpField(final int cols, final String reg) {
		super(cols);
		setRegexp(reg);
	}

	public JJRegexpField(final String cols, final String reg) {
		super(cols);
		setRegexp(reg);
	}

	public JJRegexpField(final String str, final int cols, final String reg) {
		super(str, cols);
		setRegexp(reg);
	}

	public JJRegexpField(final int cols) {
		super(cols);
	}

	public JJRegexpField(final String cols) {
		super(cols);
	}

	public JJRegexpField(final String str, final int cols) {
		super(str, cols);
	}

	@Override
	protected Document createDefaultModel() {
		return new JJRegexpDocument();
	}

	public void setRegexp(final String exp) {
		reg = Pattern.compile(exp, Pattern.CASE_INSENSITIVE);
	}

	class JJRegexpDocument extends PlainDocument {

		@Override
		public void insertString(final int offs, final String str, final AttributeSet a) throws BadLocationException {

			if (str == null) {
				return;
			}

			if ((reg == null) || reg.matcher(str).matches())
				super.insertString(offs, str, a);
		}
	}

} // JJRegexpField
