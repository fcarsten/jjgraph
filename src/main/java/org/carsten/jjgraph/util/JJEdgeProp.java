/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionListener;

/**
 * JJEdgeProp.java
 *
 *
 * Created: Mon Nov 15 11:12:12 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindowImpl;
import org.carsten.jjgraph.graph.JJInspectable;

public class JJEdgeProp extends JPanel implements JJInspectable, ActionListener {
	private final JJGraph graph;
	private final JJGraphWindowImpl fenster;
	private JButton colourButton;
	private final JJNumberField weightField = new JJNumberField("1", 3);

	@Override
	public void actionPerformed(final java.awt.event.ActionEvent e) {
		final boolean tmpRedraw = fenster.setRedraw(false);

		if (e.getSource() == colourButton) {
			final Color tmpC = JColorChooser.showDialog(this, "Choose Edge Color", fenster.getEdgeColor());
			if (tmpC != null) {
				colourButton.setBackground(tmpC);
				fenster.setEdgeColor(tmpC);
			}
		} else if (e.getSource() == weightField) {
			fenster.setDefaultEdgeWeight(Integer.parseInt(weightField.getText()));

		}

		fenster.setRedraw(tmpRedraw);
	}

	@Override
	public JPanel createTab() {
		final JPanel arg = new JPanel();
		arg.setLayout(new BorderLayout());
		arg.add(new JLabel("Set edge properties"), BorderLayout.NORTH);

		final JPanel controls = new JPanel();
		controls.add(new JLabel("Colour: "));

		colourButton = new JButton("   ");
		colourButton.setBackground(fenster.getEdgeColor());
		colourButton.addActionListener(this);
		controls.add(colourButton);
		controls.add(new JLabel("Default weight: "));
		controls.add(weightField);
		weightField.addActionListener(this);

		arg.add(controls, BorderLayout.CENTER);

		final JPanel p = new JPanel();

		setLayout(new BorderLayout());
		add(arg, BorderLayout.NORTH);

		return this;
	}

	public JJEdgeProp(final JJGraphWindowImpl f) {
		fenster = f;
		graph = fenster.getGraph();
	}

	@Override
	public String getTabName() {
		return "Edge Properties";
	}

} // JJEdgeProp
