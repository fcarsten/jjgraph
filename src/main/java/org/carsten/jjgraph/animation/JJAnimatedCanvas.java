/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

import java.awt.Color;

/**
 * JJAnimatedCanvas.java
 *
 *
 * Created: Thu Mar 16 15:19:26 2000
 *
 * @author Carsten Friedrich
 * @version
 */
import org.carsten.jjgraph.graph.JJGraphWindowImpl;
import org.carsten.jjgraph.graph.JJGraphicNode;
import org.carsten.jjgraph.util.JJPoint;

public class JJAnimatedCanvas implements JJAnimationCanvas {
	JJGraphWindowImpl graphWindow;

	public JJAnimatedCanvas(final JJGraphWindowImpl w) {
		graphWindow = w;
	}

	@Override
	public double getZoom() {
		return graphWindow.getZoom();
	}

	@Override
	public void refresh() {
		graphWindow.forceRepaintAll();
	}

	@Override
	public void setPosition(final JJAnimatedShape shape, final JJPoint newPosition) {
		if (shape instanceof JJGraphicNode)
			graphWindow.burstMoveNodeTo((JJGraphicNode) shape, newPosition.x, newPosition.y, newPosition.z);
	}

	public void openSubtask(final String s) {
		graphWindow.getGraph().getUndoManager().openSubtask(s);
	}

	public void closeSubtask(final String s) {
		graphWindow.getGraph().getUndoManager().closeSubtask(s);
	}

	public void setUndoRecording(final boolean b) {
		graphWindow.getGraph().getUndoManager().setUndoRecording(b);
	}

	public boolean setRedraw(final boolean b) {
		return graphWindow.setRedraw(b);
	}

	public void saveImage(final String s) {
		graphWindow.saveImage(s);
	}

	// public Rectangle getVisibleRect()
	// {
	// return graphWindow.getVisibleRect();
	// }

	// public Rectangle boundingBox()
	// {
	// return graphWindow.boundingBox();
	// }

	// public void setPredrawer(JJPreDrawer s)
	// {
	// graphWindow.setPredrawer(s);
	// }

	@Override
	public Color getBackground() {
		return graphWindow.getBackground();
	}

	@Override
	public void unselectAll() {
		graphWindow.deselectNodes();
	}

	@Override
	public void select(final JJAnimatedShape shape) {
		graphWindow.select((JJGraphicNode) shape);
	}

} // JJAnimatedCanvas
