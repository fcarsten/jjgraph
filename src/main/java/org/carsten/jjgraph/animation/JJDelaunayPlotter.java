/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

import javax.swing.event.ChangeEvent;

import org.carsten.jjgraph.util.JJGraphicObject;
import org.carsten.jjgraph.util.JJHistogramListener;
import org.carsten.jjgraph.util.JJPlotter;
import org.carsten.jjgraph.util.JJPoint;

public class JJDelaunayPlotter extends JJPlotter implements JJHistogramListener {

	@Override
	public void add(final JJGraphicObject l) {
		graphicList.add(l);
		repaint();
	}

	public JJDelaunayPlotter(final java.util.List<JJGraphicObject> ll, final double minWeight, final double maxWeight) {
		super(ll);
		fenster.opacitySlider.setMinimum((int) minWeight);
		fenster.opacitySlider.setMaximum((int) maxWeight);
		fenster.opacitySlider.setValue((int) minWeight);
	}

	@Override
	public void clearSelectedRegion(final double min, final double max) {
	}

	@Override
	public void addSelectedRegion(final double min, final double max) {
	}

	@Override
	public void setSelectedRegion(final double x, final double y) {
		fenster.opacitySlider.setValue((int) y);
		repaint();
	}

	@Override
	public void stateChanged(final ChangeEvent e) {
		repaint();
	}

	@Override
	public void unhandledMouseClick(final JJPoint p) {
		for (final JJGraphicObject jjGraphicObject : graphicList) {
			final JJClusterShape s = (JJClusterShape) jjGraphicObject;
			s.mouseClicked(p);
		}
	}

}
