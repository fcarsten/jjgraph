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
import java.awt.geom.Rectangle2D;

public class JJTriangleObject implements JJGraphicObject {
	GeneralPath triangle;

	public JJTriangleObject(final GeneralPath l) {
		triangle = l;
	}

	public JJTriangleObject(final JJPoint p[]) {
		triangle = new GeneralPath();
		final double num = p.length;

		for (int i = 0; i < num; i++) {
			if (i == 0)
				triangle.moveTo((int) p[i].x, (int) p[i].y);
			else
				triangle.lineTo((int) p[i].x, (int) p[i].y);
		}
		triangle.lineTo((int) p[0].x, (int) p[0].y);
	}

	@Override
	public void draw(final Graphics2D gc, final int opacity) {
		gc.setPaint(new Color(0, 0, 255, opacity));
		gc.draw(triangle);
	}

	@Override
	public Rectangle2D getBounds2D() {
		return triangle.getBounds2D();
	}

} // JJTriangleObject
