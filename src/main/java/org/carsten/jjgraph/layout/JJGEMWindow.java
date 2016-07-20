/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.layout;

/**
 * JJBlenderWindow.java
 *
 *
 * Created: Mon Nov 15 11:12:12 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.carsten.jjgraph.graph.JJInspectable;
import org.carsten.jjgraph.util.Debug;
import org.carsten.misc.DecimalField;
import org.carsten.misc.WholeNumberField;

public class JJGEMWindow extends JPanel implements ActionListener, JJInspectable, CaretListener, ChangeListener {
	private final JJGem gem;

	private JSlider finalTemp;
	private JSlider maxIter;

	private WholeNumberField edgeLength; // = 128;
	private DecimalField gravity; // = 128;
	private WholeNumberField maxRealTime;
	private JCheckBox useEdgeWeight;
	private JButton layoutButton;

	private final ActionListener target;

	// public int allowsOptimize()
	// {
	// return gem.allowsOptimize();
	// }

	@Override
	public void caretUpdate(final CaretEvent e) {
		if (e.getSource() == edgeLength) {
			gem.setEdgeLength(edgeLength.getValue());
		}
		if (e.getSource() == maxRealTime) {
			gem.setMaxRealTime(maxRealTime.getValue() * 1000);
		} else if (e.getSource() == gravity) {
			gem.setGravity(gravity.getValue());
		}
	}

	@Override
	public void stateChanged(final ChangeEvent e) {
		if (e.getSource() == finalTemp) {
			gem.setFinalTemp(finalTemp.getValue() / 200.0);
		} else if (e.getSource() == maxIter) {
			gem.setMaxIter(maxIter.getValue() / 10.0);
		}
	}

	@Override
	public String getTabName() {
		return "Gem";
	}

	public JJGEMWindow(final JJGem ge, final ActionListener t) {
		gem = ge;
		target = t;
	}

	@Override
	public JPanel createTab() {
		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		layoutButton = new JButton("Layout");
		layoutButton.setActionCommand(gem.getName());
		layoutButton.addActionListener(target);

		final JPanel controlsPanel = new JPanel();
		controlsPanel.setLayout(new BorderLayout());

		final JPanel tqPanel = new JPanel();
		tqPanel.setLayout(new BorderLayout());
		controlsPanel.add(tqPanel, BorderLayout.CENTER);

		panel.add(layoutButton, BorderLayout.SOUTH);
		panel.add(controlsPanel, BorderLayout.NORTH);

		{
			final JPanel p = new JPanel();
			p.setLayout(new BorderLayout());

			finalTemp = new JSlider(JSlider.HORIZONTAL, 1, 10, (int) (gem.getFinalTemp() * 200));
			finalTemp.addChangeListener(this);
			final Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
			labelTable.put(new Integer(1), new JLabel("High"));
			labelTable.put(new Integer(10), new JLabel("Low"));
			labelTable.put(new Integer((int) (gem.getFinalTemp() * 200)), new JLabel("^"));
			finalTemp.setLabelTable(labelTable);
			finalTemp.setPaintLabels(true);
			finalTemp.setPaintTicks(true);
			p.add(new JLabel("Quality: "), BorderLayout.WEST);
			p.add(finalTemp, BorderLayout.CENTER);
			tqPanel.add(p, BorderLayout.NORTH);
		}

		{
			final JPanel p = new JPanel();
			p.setLayout(new BorderLayout());

			maxIter = new JSlider(JSlider.HORIZONTAL, 1, 40, (int) (gem.getMaxIter() * 10));
			maxIter.addChangeListener(this);
			final Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
			labelTable.put(new Integer(1), new JLabel("Fast"));
			labelTable.put(new Integer(40), new JLabel("Slow"));
			labelTable.put(new Integer((int) (gem.getMaxIter() * 10)), new JLabel("^"));
			maxIter.setLabelTable(labelTable);
			maxIter.setPaintLabels(true);
			maxIter.setPaintTicks(true);
			p.add(new JLabel("Time: "), BorderLayout.WEST);
			p.add(maxIter, BorderLayout.CENTER);
			tqPanel.add(p, BorderLayout.SOUTH);
		}

		final JPanel egPanel = new JPanel();
		egPanel.setLayout(new GridLayout(4, 2));

		edgeLength = new WholeNumberField((int) gem.getEdgeLength(), 5);
		edgeLength.addCaretListener(this);

		egPanel.add(new JLabel("Edge length: "));
		egPanel.add(edgeLength);

		maxRealTime = new WholeNumberField((int) gem.getMaxRealTime() / 1000, 5);
		maxRealTime.addCaretListener(this);

		egPanel.add(new JLabel("Max real time (sec.): "));
		egPanel.add(maxRealTime);

		useEdgeWeight = new JCheckBox("Use edge weight", true);
		useEdgeWeight.addActionListener(this);

		final DecimalFormat gravityFormat = (DecimalFormat) NumberFormat.getNumberInstance();
		gravityFormat.setMaximumFractionDigits(2);
		gravity = new DecimalField(gem.getGravity(), 5, gravityFormat);
		gravity.addCaretListener(this);

		egPanel.add(new JLabel("Gravity: "));
		egPanel.add(gravity);

		egPanel.add(useEdgeWeight);
		egPanel.setBorder(BorderFactory.createEmptyBorder(4, 2, 5, 2));
		controlsPanel.add(egPanel, BorderLayout.WEST);

		final JPanel arg = new JPanel();
		arg.setLayout(new BorderLayout());
		arg.add(panel, BorderLayout.NORTH);

		return arg;
	}

	@Override
	public void actionPerformed(final java.awt.event.ActionEvent e) {
		if (e.getSource() == useEdgeWeight) {
			gem.setUseEdgeWeight(useEdgeWeight.isSelected());
		} else {
			Debug.println("Unexpected action event");
		}
	}

} // JJGEMWindow
