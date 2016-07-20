/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

/**
 * JJArrayObject.java
 *
 *
 * Created: Thu Apr 26 14:43:22 2001
 *
 * @author
 * @version
 */
import java.awt.Color;
import java.awt.Graphics2D;

public class JJGreenArrayObject extends JJArrayObject {

	public JJGreenArrayObject(final double v[]) {
		super(v);
	}

	@Override
	public void draw(final Graphics2D gc, final int opacity) {
		gc.setPaint(new Color(0, 150, 0, Math.min(255, 50 + opacity)));
		gc.draw(array);
	}

} // JJArrayObject
