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
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

public class JJRedGreenLine extends JJLineObject {

	public JJRedGreenLine(final Line2D.Double l) {
		super(l);
	}

	@Override
	public void draw(final Graphics2D gc, final int opacity) {
		gc.setPaint(new GradientPaint((float) (line.x1), (float) (line.y1), new Color(255, 0, 0, opacity),
				(float) (line.x2), (float) (line.y2), new Color(0, 255, 0, opacity)));
		gc.drawLine((int) (line.x1), (int) (line.y1), (int) (line.x2), (int) (line.y2));

	}

} // JJLineObject
