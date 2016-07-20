/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * JJHistogramWindow.java
 *
 *
 * Created: Fri Mar  5 17:17:34 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;

public class JJHistogramWindow extends JFrame implements ChangeListener {
	JJHistogram histo;
	private JSlider columnSlider;
	private JSlider rangeSlider;

	public void addHistogramListener(final JJHistogramListener l) {
		histo.addHistogramListener(l);
	}

	public void removeHistogramListener(final JJHistogramListener l) {
		histo.removeHistogramListener(l);
	}

	public JJHistogramWindow(final double pArray[]) {
		super("Histogram");
		init(pArray, Double.NaN);
	}

	public JJHistogramWindow(final double pArray[], final double pi) {
		super("Histogram");
		init(pArray, pi);
	}

	public JJHistogramWindow(final double pArray[], final String s, final double pi) {
		super("Histogram: " + s);
		init(pArray, pi);
	}

	@Override
	public void stateChanged(final javax.swing.event.ChangeEvent e) {
		if (e.getSource() == columnSlider)
			histo.mc.setColumns(((JSlider) e.getSource()).getValue());
		if (e.getSource() == rangeSlider)
			histo.mc.setRangeDivider(((JSlider) e.getSource()).getValue());
	}

	public void setMultiSelection(final boolean b) {
		histo.setMultiSelection(b);
	}

	void init(final double pArray[], final double pi) {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		histo = new JJHistogram(pArray, pi);
		addWindowListener(histo);

		final java.awt.Container pane = getContentPane();

		columnSlider = new JSlider(JSlider.VERTICAL, 1, pArray.length / 2, Math.min(5, pArray.length));
		columnSlider.addChangeListener(this);

		rangeSlider = new JSlider(0, 1000, 0);
		rangeSlider.addChangeListener(this);

		final JButton clearB = new JButton("Close");
		clearB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				dispose();
			}
		});

		pane.setLayout(new BorderLayout());

		pane.add(histo, BorderLayout.CENTER);
		// pane.add(clearB, BorderLayout.SOUTH);
		pane.add(columnSlider, BorderLayout.WEST);
		pane.add(rangeSlider, BorderLayout.SOUTH);

		pack();
		setVisible(true);
	}

} // JJHistogramWindow
