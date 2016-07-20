/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

import java.awt.Rectangle;

/**
 * JJRectangle.java
 *
 *
 * Created: Mon Jan 17 12:47:23 2000
 *
 * @author Carsten Friedrich
 * @version
 */
import java.awt.geom.Rectangle2D;

public class JJRectangle extends Rectangle2D.Double {
	public JJRectangle() {
		super();
	}

	public JJRectangle(final double x, final double y, final double w, final double h) {
		super(x, y, w, h);
	}

	public JJRectangle(final Rectangle r) {
		super(r.x, r.y, r.width, r.height);
	}

	public void divA(final double z) {
		if (z != 0) {
			x /= z;
			y /= z;
			width /= z;
			height /= z;
		}
	}

} // JJRectangle
