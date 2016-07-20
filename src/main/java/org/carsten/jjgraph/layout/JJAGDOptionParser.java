/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.layout;

/**
 * JJAGDOptionParser.java
 *
 *
 * Created: Fri Feb 26 13:50:26 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.List;

import org.carsten.jjgraph.util.Debug;

public class JJAGDOptionParser extends StreamTokenizer {
	public JJAGDOptionParser(final Reader r) {
		super(r);
		// myReader = (Reader) r;

		resetSyntax();
		whitespaceChars('\u0000', ' ');
		parseNumbers();
		eolIsSignificant(false);
		wordChars('A', 'Z');
		wordChars('a', 'z');
		wordChars('[', '[');
		wordChars(']', ']');
		wordChars('_', '_');
		commentChar('#');
		quoteChar('\"');
	}

	public boolean parse(final List<String> list) {
		// Debug.println("Parsing options");

		try {
			while (true) {
				final int c = nextToken();
				switch (c) {
				case StreamTokenizer.TT_EOF:
					// Debug.println("Loading succeeded");
					return true;
				case StreamTokenizer.TT_NUMBER:
					// Debug.println("Found number: " + nval);
					break;
				case StreamTokenizer.TT_WORD:
					// Debug.println("sval:" + sval);

					if (sval.equals("AGD_function_list")) {

						// Debug.println("Parsing functions ....");
						// Debug.println("");
						parseFunctionList(list);
					}
					// Debug.println("Found word: " + sval);
					break;
				default:
					// Debug.println("Found: " + sval);
					break;
				}
			}
		} catch (final IOException e) {
			Debug.println("Error in line " + lineno());
		}
		return false;
	}

	public void parseFunctionList(final List<String> list) throws IOException {
		try {
			while (true) {
				final int c = nextToken();
				switch (c) {
				case StreamTokenizer.TT_EOF:
					throw new IOException("Unexpected EOF");
				case StreamTokenizer.TT_NUMBER:
					break;
				case StreamTokenizer.TT_WORD:
					if (sval.equals("function")) {
						nextToken();
						// Debug.println("Found: " + sval);

						list.add(new String(sval));
					} else if (sval.equals("]")) {
						return;
					} else if (!sval.equals("[")) {
						Debug.println("Unexpected token: " + sval);
					}
					break;
				default:
					break;
				}
			}
		} catch (final IOException e) {
			Debug.println("Error in line " + lineno());
		}
	}
}
