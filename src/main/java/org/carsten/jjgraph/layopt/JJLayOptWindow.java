/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.layopt;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * JJLayOptWindow.java
 *
 *
 * Created: Mon Nov 15 11:12:12 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.carsten.jjgraph.graph.JJGraphWindowImpl;
import org.carsten.jjgraph.graph.JJInspectable;
import org.carsten.jjgraph.util.Debug;

public class JJLayOptWindow extends JPanel implements ActionListener, JJInspectable {
	private JButton optimizeB;
	private JButton setStartB;
	private JButton setEndB;
	private JButton rewindB;
	private JButton playB;

	private JCheckBox translateBox;
	private JCheckBox scaleBox;
	private JCheckBox scalePropBox;
	private JCheckBox orthoBox;
	private JCheckBox flipBox;
	private JCheckBox rotateBox;
	private JCheckBox skewBox;

	protected JJLayOpt optimizer;

	public JJLayOpt getOptimizer() {
		return optimizer;
	}

	public void setOptimizer(final JJLayOpt b) {
		optimizer = b;
	}

	public JJLayOptWindow(final JJGraphWindowImpl f) {
		optimizer = new JJLayOpt(f);
	}

	public void disableWidgets() {
		optimizeB.setEnabled(false);
	}

	public void enableWidgets() {
		optimizeB.setEnabled(true);
	}

	protected JPanel createButtonRow() {
		final JPanel buttonPane = new JPanel();
		if (Debug.DEBUG) {
			optimizeB = new JButton("Opt");
			optimizeB.addActionListener(this);

			final URLClassLoader cl = (URLClassLoader) this.getClass().getClassLoader();

			URL url = cl.findResource("org/carsten/jjgraph/icons/set_start.gif");
			setStartB = new JButton(new ImageIcon(url));
			setStartB.addActionListener(this);

			url = cl.findResource("org/carsten/jjgraph/icons/set_stop.gif");
			setEndB = new JButton(new ImageIcon(url));
			setEndB.addActionListener(this);

			url = cl.findResource("org/carsten/jjgraph/icons/backward.gif");
			rewindB = new JButton(new ImageIcon(url));
			rewindB.addActionListener(this);

			url = cl.findResource("org/carsten/jjgraph/icons/next.gif");
			playB = new JButton(new ImageIcon(url));
			playB.addActionListener(this);

			buttonPane.add(optimizeB);
			buttonPane.add(setStartB);
			buttonPane.add(setEndB);
			buttonPane.add(rewindB);
			buttonPane.add(playB);
		}
		return buttonPane;
	}

	class OptionListener implements ActionListener {
		@Override
		public void actionPerformed(final java.awt.event.ActionEvent e) {
			if (e.getSource() == translateBox) {
				optimizer.setTranslate(translateBox.isSelected());
			} else if (e.getSource() == scaleBox) {
				optimizer.setScale(scaleBox.isSelected());
			} else if (e.getSource() == scalePropBox) {
				optimizer.setScaleProp(scalePropBox.isSelected());
			} else if (e.getSource() == orthoBox) {
				optimizer.setOrtho(orthoBox.isSelected());
			} else if (e.getSource() == flipBox) {
				optimizer.setFlip(flipBox.isSelected());
			} else if (e.getSource() == rotateBox) {
				optimizer.setRotate(rotateBox.isSelected());
			} else if (e.getSource() == skewBox) {
				optimizer.setSkew(skewBox.isSelected());
			}
		}
	}

	private JPanel createOptionsPanel() {
		final JPanel bcp = new JPanel();
		bcp.setLayout(new GridLayout(3, 3));
		final OptionListener cal = new OptionListener();

		translateBox = new JCheckBox("Translate", true);
		translateBox.addActionListener(cal);
		scaleBox = new JCheckBox("Scale", true);
		scaleBox.addActionListener(cal);
		scalePropBox = new JCheckBox("Proportional", true);
		scalePropBox.addActionListener(cal);
		flipBox = new JCheckBox("Flip", true);
		flipBox.addActionListener(cal);
		rotateBox = new JCheckBox("Rotate", true);
		rotateBox.addActionListener(cal);
		orthoBox = new JCheckBox("Orthogonal", false);
		orthoBox.addActionListener(cal);
		skewBox = new JCheckBox("Skew", false);
		skewBox.addActionListener(cal);

		bcp.add(translateBox);
		bcp.add(scaleBox);
		bcp.add(scalePropBox);
		bcp.add(flipBox);
		bcp.add(rotateBox);
		bcp.add(orthoBox);
		bcp.add(skewBox);

		return bcp;
	}

	@Override
	public JPanel createTab() {
		final JPanel buttonRow = createButtonRow();

		final JPanel optionsPanel = createOptionsPanel();

		final JLabel titleLabel = new JLabel("Layout Optimizer Control");

		final JPanel p00 = new JPanel();
		p00.setLayout(new BorderLayout());
		p00.add(titleLabel, BorderLayout.NORTH);

		final JPanel p11 = new JPanel();
		p11.setLayout(new BorderLayout());
		p11.add(p00, BorderLayout.NORTH);
		p11.add(optionsPanel, BorderLayout.SOUTH);

		final JPanel p12 = new JPanel();
		p12.setLayout(new BorderLayout());
		p12.add(p11, BorderLayout.NORTH);
		p12.add(buttonRow, BorderLayout.SOUTH);

		final JPanel arg = new JPanel();
		arg.setLayout(new BorderLayout());
		arg.add(p12, BorderLayout.NORTH);

		return arg;
	}

	@Override
	public void actionPerformed(final java.awt.event.ActionEvent e) {
		// Debug.println("Should blend now");
		if (e.getSource() == optimizeB) {
			optimizer.optimize();
		} else if (e.getSource() == setStartB) {
			optimizer.setStart();
		} else if (e.getSource() == setEndB) {
			optimizer.setEnd();
		} else if (e.getSource() == playB) {
			optimizer.play();
		} else if (e.getSource() == rewindB) {
			optimizer.rewind();
		} else if (e.getSource() == rewindB) {
			optimizer.play();
		} else {
			Debug.println("Unexpected action event");
		}
	}

	@Override
	public String getTabName() {
		return "Layout Optimizer";

	}

} // JJLayOptWindow
