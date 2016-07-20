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
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

public class JJBlueLine extends JJLineObject {

	public JJBlueLine(final Line2D.Double l) {
		super(l);
	}

	public JJBlueLine(final JJPoint p1, final JJPoint p2) {
		super(p1, p2);
	}

	@Override
	public void draw(final Graphics2D gc, final int opacity) {
		gc.setPaint(new Color(0, 0, 255, opacity));

		gc.drawLine((int) (line.x1), (int) (line.y1), (int) (line.x2), (int) (line.y2));

	}

} // JJLineObject
