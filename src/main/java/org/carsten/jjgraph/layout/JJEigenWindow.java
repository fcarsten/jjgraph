/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.layout;

/**
 * JJEigenWindow.java
 *
 *
 * Created: Mon Nov 15 11:12:12 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.carsten.jjgraph.clustering.JJEigenBipart;
import org.carsten.jjgraph.graph.JJGraphEvent;
import org.carsten.jjgraph.graph.JJGraphListener;
import org.carsten.jjgraph.graph.JJInspectable;
import org.carsten.jjgraph.util.Debug;

public class JJEigenWindow extends JPanel implements ActionListener, JJInspectable, ChangeListener, JJGraphListener

{
	private final JJEigenLayout eigen;
	private final ActionListener target;

	private JSlider jx;
	private JSlider jy;
	private JSlider jz;

	// JButton button;
	private JCheckBox autoLayoutBox;
	private JComboBox<String> matrixModeBox;
	private boolean autoLayout = false;

	private JLabel xLabel;
	private JLabel yLabel;
	private JLabel zLabel;

	private JButton initButton;

	public void disableWidgets() {
		autoLayoutBox.setEnabled(false);
		jx.setEnabled(false);
		jy.setEnabled(false);
		jz.setEnabled(false);

	}

	public void enableWidgets() {
		autoLayoutBox.setEnabled(true);
		jx.setEnabled(true);
		jy.setEnabled(true);
		jz.setEnabled(true);
		jx.setMaximum(eigen.getDimensions());
		jy.setMaximum(eigen.getDimensions());
		jz.setMaximum(eigen.getDimensions());
	}

	@Override
	public void graphStructureChanged(final JJGraphEvent e) {
		disableWidgets();
		eigen.setNeedsInit(true);
	}

	public void graphAppearanceChanged(final JJGraphEvent e) {
	}

	@Override
	public void stateChanged(final ChangeEvent e) {
		if (e.getSource() == autoLayoutBox) {
			autoLayout = autoLayoutBox.isSelected();
			if (autoLayout)
				fireAction();
		} else if ((e.getSource() == jx) && (jx.getValue() != eigen.getX())) {
			if (jx.getValue() > -1)
				xLabel.setText("x :  " + jx.getValue());
			else
				xLabel.setText("x :  Don't change");
			eigen.setX(jx.getValue());
			if (autoLayout)
				fireAction();
		} else if ((e.getSource() == jy) && (jy.getValue() != eigen.getY())) {
			if (jy.getValue() > -1)
				yLabel.setText("y :  " + jy.getValue());
			else
				yLabel.setText("y :  Don't change");
			eigen.setY(jy.getValue());
			if (autoLayout)
				fireAction();
		} else if ((e.getSource() == jz) && (jz.getValue() != eigen.getZ())) {
			if (jz.getValue() > -1)
				zLabel.setText("z :  " + jz.getValue());
			else
				zLabel.setText("z :  Don't change");
			eigen.setZ(jz.getValue());
			if (autoLayout)
				fireAction();
		}
	}

	@Override
	public String getTabName() {
		return "Spectral Layout";
	}

	public JJEigenWindow(final JJEigenLayout el, final ActionListener t) {
		target = t;
		eigen = el;
	}

	@Override
	public JPanel createTab() {
		final JPanel panel = new JPanel();

		xLabel = new JLabel("x :  1");
		yLabel = new JLabel("y :  2");
		zLabel = new JLabel("z :  3");
		jx = new JSlider(JSlider.HORIZONTAL, -1, 4, 1);
		jx.addChangeListener(this);
		jy = new JSlider(JSlider.HORIZONTAL, -1, 4, 2);
		jy.addChangeListener(this);
		jz = new JSlider(JSlider.HORIZONTAL, -1, 4, 3);
		jz.addChangeListener(this);

		autoLayoutBox = new JCheckBox(" Auto layout");
		autoLayoutBox.addChangeListener(this);

		final String matrixList[] = { "Laplace", "Adjacence" };
		matrixModeBox = new JComboBox<>(matrixList);
		matrixModeBox.setSelectedIndex(0);
		matrixModeBox.setActionCommand(eigen.getName());
		matrixModeBox.addActionListener(this);

		// adding the title
		final BorderLayout b1 = new BorderLayout();
		panel.setLayout(b1);

		// adding the entry Panel
		final JPanel entryPanel = new JPanel();
		panel.add(entryPanel, BorderLayout.CENTER);

		final GridLayout g1 = new GridLayout(3, 2);
		entryPanel.setLayout(g1);

		entryPanel.add(xLabel);
		entryPanel.add(jx);

		entryPanel.add(yLabel);
		entryPanel.add(jy);

		entryPanel.add(zLabel);
		entryPanel.add(jz);

		final JPanel buttonP = new JPanel();
		buttonP.setLayout(new BorderLayout());

		initButton = new JButton("Init & Layout");
		initButton.setActionCommand(eigen.getName());
		initButton.addActionListener(this);

		buttonP.add(autoLayoutBox, BorderLayout.WEST);
		buttonP.add(matrixModeBox, BorderLayout.CENTER);
		buttonP.add(initButton, BorderLayout.EAST);

		panel.add(buttonP, BorderLayout.SOUTH);
		disableWidgets();

		final Dimension size = panel.getPreferredSize();

		final JPanel arg = new JPanel();
		arg.setLayout(new BorderLayout());
		arg.add(panel, BorderLayout.NORTH);

		return arg;
	}

	@Override
	public void actionPerformed(final java.awt.event.ActionEvent e) {
		// Debug.println("Should blend now");
		if (e.getSource() == initButton) {
			eigen.initNodes();
			enableWidgets();
			target.actionPerformed(e);
		} else if (e.getSource() == matrixModeBox) {
			if (matrixModeBox.getSelectedItem().equals("Laplace")) {
				eigen.setMatrixMode(JJEigenBipart.LAPLACE);
			} else {
				eigen.setMatrixMode(JJEigenBipart.ADJACENCE);
			}
			if (autoLayout) {
				eigen.initNodes();
				enableWidgets();
				target.actionPerformed(e);
			}
		} else {
			Debug.println("Unexpected action event");
		}
	}

	public void fireAction() {
		target.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, eigen.getName()));
	}

} // JJEigenWindow
