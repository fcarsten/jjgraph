/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

/**
 * JJTriangleObject.java
 *
 *
 * Created: Thu Apr 26 14:43:22 2001
 *
 * @author
 * @version
 */
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;

public class JJBlueTriangle extends JJTriangleObject {
	public JJBlueTriangle(final GeneralPath l) {
		super(l);
	}

	public JJBlueTriangle(final JJPoint p[]) {
		super(p);
	}

	@Override
	public void draw(final Graphics2D gc, final int opacity) {
		gc.setPaint(new Color(255, 0, 0, Math.min(155, 50 + opacity)));
		gc.draw(triangle);
		gc.setPaint(new Color(0, 0, 255, Math.min(155, 50 + opacity)));
		gc.fill(triangle);
	}

} // JJTriangleObject
