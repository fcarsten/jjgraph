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
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

public class JJArrayObject {
	GeneralPath array;

	public JJArrayObject(final GeneralPath l) {
		array = l;
	}

	public JJArrayObject(final double v[]) {
		array = new GeneralPath();
		final double num = v.length;
		for (int i = 0; i < num; i++) {
			JJPoint p = null;
			if (i < 4)
				p = new JJPoint(50 * i, 500 * v[i]);
			else
				p = new JJPoint(50 * i, v[i]);

			if (i == 0)
				array.moveTo((int) p.x, (int) p.y);
			else
				array.lineTo((int) p.x, (int) p.y);
		}
	}

	public void draw(final Graphics2D gc, final int opacity) {
		gc.setPaint(new Color(0, 0, 255, opacity));
		gc.draw(array);
	}

	public Rectangle2D getBounds2D() {
		return array.getBounds2D();
	}

} // JJArrayObject
