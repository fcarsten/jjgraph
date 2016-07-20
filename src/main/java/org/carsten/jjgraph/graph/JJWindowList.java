/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

/**
 * JJWindowList.java
 *
 *
 * Created: Tue Jul 02 09:23:20 2002
 *
 * @author <a href="mailto:friedric@cs.newcastle.edu.au"></a>
 * @version
 */
import java.util.ArrayList;

public class JJWindowList extends ArrayList<JJGraphWindow> {

	public void printError(final String s) {
		for (final Object element : this) {
			((JJGraphWindow) element).printError(s);
		}
		if (isEmpty()) {
			System.err.println("Error: " + s);
		}

	}

	public void printWarning(final String s) {
		for (final Object element : this) {
			((JJGraphWindow) element).printWarning(s);
		}
		if (isEmpty()) {
			System.err.println("Warning: " + s);
		}
	}

	public void printNote(final String s) {
		for (final Object element : this) {
			((JJGraphWindow) element).printNote(s);
		}
		if (isEmpty()) {
			System.err.println("Note: " + s);
		}
	}

	public void setBusy(final boolean s) {
		for (final Object element : this) {
			((JJGraphWindow) element).setBusy(s);
		}
	}

}// JJWindowList
