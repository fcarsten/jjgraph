/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

/**
 * JJLineObject.java
 *
 *
 * Created: Thu Apr 26 14:43:22 2001
 *
 * @author
 * @version
 */
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

public class JJLineObject implements JJGraphicObject {
	Line2D.Double line;

	public JJLineObject(final Line2D.Double l) {
		line = l;
	}

	public JJLineObject(final JJPoint p1, final JJPoint p2) {
		line = new Line2D.Double(p1, p2);
	}

	@Override
	public void draw(final Graphics2D gc, final int opacity) {
		gc.drawLine((int) (line.x1), (int) (line.y1), (int) (line.x2), (int) (line.y2));

	}

	@Override
	public Rectangle2D getBounds2D() {
		return line.getBounds2D();
	}

} // JJLineObject
