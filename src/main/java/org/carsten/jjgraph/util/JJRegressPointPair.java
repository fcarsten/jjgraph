/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

/**
 * JJRegressPointPair.java
 *
 *
 * Created: Thu Apr 12 14:41:44 2001
 *
 * @author
 * @version
 */

public class JJRegressPointPair implements JJRegressable {
	public JJPoint p1;
	public JJPoint p2;

	public JJRegressPointPair(final JJRegressPointPair x) {
		p1 = new JJPoint(x.p1);
		p2 = new JJPoint(x.p2);
	}

	public JJRegressPointPair() {
	}

	@Override
	public JJPoint getPos1() {
		return p1;
	}

	@Override
	public JJPoint getPos2() {
		return p2;
	}
} // JJRegressPointPair
