/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

import java.awt.Image;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import org.carsten.jjgraph.util.JJNodeProp;

public class JJGraphicNodeImageP extends JJGraphicNodeImage implements ActionListener {
	private Image imageTmp = null;
	private Timer timer;

	public void setBored() {
		imageTmp = getImage();
		setImage(JJNodeProp.getPImage());
		timer = new Timer(1000, this);
		timer.start();
	}

	@Override
	public void actionPerformed(final java.awt.event.ActionEvent e) {
		setImage(imageTmp);
		timer.stop();
		timer = null;
	}

	@Override
	public void setImage(final Image i) {
		super.setImage(i);
		knoten.window().repaint(knoten);
	}
}
