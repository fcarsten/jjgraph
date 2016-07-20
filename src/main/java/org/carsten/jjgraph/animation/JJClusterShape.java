/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

/**
 * JJClusterShape.java
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
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import org.carsten.jjgraph.util.JJGraphicObject;
import org.carsten.jjgraph.util.JJPoint;

public class JJClusterShape implements JJGraphicObject {
	GeneralPath path;
	boolean active = true;
	Line2D.Double e[] = new Line2D.Double[3];
	double quality[] = new double[3];

	@Override
	public void draw(final Graphics2D gc, final int opacity) {
		int cluster = 0;

		for (int i = 0; i < e.length; i++) {
			if ((quality[i] > opacity) || (quality[i] < 0))
				gc.setPaint(new Color(0, 0, 0, 250));
			else {
				cluster++;
				gc.setPaint(new Color(0, 0, 0, 50));
			}

			gc.draw(e[i]);
		}

		gc.setPaint(new Color(0, 0, 255, 50 + (cluster * 50)));
		gc.fill(path);
	}

	public JJClusterShape(final JJPoint p[], final double weights[]) {
		path = new GeneralPath();
		final double num = p.length;

		for (int i = 0; i < num; i++) {
			if (i == 0)
				path.moveTo((int) p[i].x, (int) p[i].y);
			else
				path.lineTo((int) p[i].x, (int) p[i].y);

			e[i] = new Line2D.Double(p[i], p[(i + 1) % 3]);
			quality[i] = weights[i];
		}
		path.lineTo((int) p[0].x, (int) p[0].y);
	}

	@Override
	public Rectangle2D getBounds2D() {
		return path.getBounds2D();
	}

	public void mouseClicked(final JJPoint p) {
	}

} // JJClusterShape
