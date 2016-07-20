/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.ProteinExplorer;

import java.awt.Rectangle;
/**
 * JJProteinLayout.java
 *
 *
 * Created: Wed Dec  8 11:15:52 1999
 *
 * @author $Author: carsten $
 * @version $Revision: 1.4 $ $Date: 2003/02/09 05:52:13 $
 */
import java.util.Collection;
import java.util.Iterator;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJGraphicEdge;
import org.carsten.jjgraph.graph.JJGraphicNode;
import org.carsten.jjgraph.graph.JJNode;
import org.carsten.jjgraph.layout.JJGem;
import org.carsten.jjgraph.layout.JJLayout;

public class JJProteinLayout implements JJLayout {
	private final JJGraphWindow fenster;
	private final JJProteinInspector insp;

	private final JJGraph graph;
	private final JJGem gem;

	public JJProteinLayout(final JJProteinInspector i) {
		insp = i;

		fenster = i.getWindow();
		graph = fenster.getGraph();
		gem = new JJGem(fenster);
	}

	@Override
	public int allowsOptimize() {
		return 0; // JJLayOpt.TRANSLATE | JJLayOpt.SCALE;
	}

	public Rectangle getLayoutBounds() {
		final Rectangle r = fenster.getVisibleBounds();
		// fenster.deviceAdjust(r);

		// double width = Math.max(50, r.getWidth()*0.9)/ fenster.getZoom()-10;
		// double height = Math.max(50, r.getHeight()*0.9)/
		// fenster.getZoom()-10;
		// double x = r.getX()/ fenster.getZoom()+5;
		// double y = r.getY()/ fenster.getZoom()+5;
		// r.x = (int)x;
		// r.y = (int)y;
		// r.width = (int)width;
		// r.height = (int)height;
		r.x += 5;
		r.y += 5;
		r.width -= 10;
		r.height -= 10;
		return r;
	}

	@Override
	public void layout() {
		fenster.removeBends();
		final double radius = circularLayout();
		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			final JJNode tmpN = iter.next();
			final JJGraphicNode gn = tmpN.getGraphicNode(fenster);
			boolean lonely = true;

			for (final Iterator<JJEdge> edgeIter = tmpN.edgeIterator(); edgeIter.hasNext();) {
				final JJEdge tmpE = edgeIter.next();
				final JJGraphicEdge ge = tmpE.getGraphicEdge(fenster);
				if (ge.isVisible()) {
					lonely = false;
					break;
				}

			}
			if (lonely) {
				gn.setFixed(true);
				if (insp.isHideUninvolved())
					gn.hide();
			} else {
				gn.setFixed(false);
				if (insp.isHideUninvolved())
					gn.unhide();
			}
		}

		final Rectangle bounds = getLayoutBounds();
		if (insp.isHideUninvolved()) {
			gem.setBounds(bounds);
			gem.setRadius(Double.NaN);
		} else {
			gem.setBounds(bounds);
			gem.setRadius(radius * 0.75);
		}

		gem.setGravity(0.5);

		gem.layout();
		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			final JJNode tmpN = iter.next();
			final JJGraphicNode gn = tmpN.getGraphicNode(fenster);
			if (gn.isFixed())
				fenster.rmoveNodeTo(gn, 0, 0, -1000);

		}
	}

	public double circularLayout() {
		final Rectangle r = fenster.getVisibleRect();

		final double width = Math.max(50, r.getWidth() * 0.9) / fenster.getZoom();
		final double height = Math.max(50, r.getHeight() * 0.9) / fenster.getZoom();

		final double radius = Math.min(width, height) / 2.0;

		final int steps = 1 + graph.getNumNodes() / 4;

		final double deltaY = radius / steps;

		int counter = 1;

		for (final Iterator<JJNode> nodeIter = graph.nodeIterator(); nodeIter.hasNext();) {
			final JJNode tmpN = nodeIter.next();
			final double y = -radius + (counter / 2) * deltaY;
			double x = Math.sqrt(radius * radius - y * y);
			if ((counter % 2) != 0)
				x = -x;
			fenster.moveNodeTo(tmpN.getGraphicNode(fenster), x, y);
			counter++;

		}
		return radius;
	}

	@Override
	public void layout(final Collection<JJNode> c) {
		layout();
	}

	@Override
	public String getName() {
		return "Protein layout";

	}

	@Override
	public void apply(final String s) {
		layout();
	}

	@Override
	public boolean canDo(final String s) {
		return s.equals(getName());

	}

} // JJProteinLayout
