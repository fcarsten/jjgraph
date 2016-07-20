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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.carsten.jjgraph.graph.JJInspectable;
import org.carsten.jjgraph.util.Debug;
import org.carsten.misc.WholeNumberField;

public class JJOrthoWindow extends JPanel implements ActionListener, JJInspectable, CaretListener {
	private final JJOrtho ortho;

	private WholeNumberField xOffSet; // = 128;
	private WholeNumberField yOffSet; // = 128;

	private JCheckBox threeD;
	private JCheckBox anim;
	private JCheckBox doSwapping;
	private JCheckBox doSwapBadies;
	private JCheckBox doEdgeAdjustment;
	private JComboBox<String> initPlacement;
	private JButton layoutButton;
	private final ActionListener target;

	@Override
	public void caretUpdate(final CaretEvent e) {
		if (e.getSource() == xOffSet) {
			ortho.setxoff(xOffSet.getValue());
		} else if (e.getSource() == yOffSet) {
			ortho.setyoff(yOffSet.getValue());
		}
	}

	@Override
	public String getTabName() {
		return "Ortho";
	}

	public JJOrthoWindow(final JJOrtho o, final ActionListener t) {
		ortho = o;
		target = t;
	}

	@Override
	public JPanel createTab() {
		layoutButton = new JButton("Layout");
		layoutButton.setActionCommand(ortho.getName());
		layoutButton.addActionListener(target);

		final String initPlace[] = { "adjust", "random" };
		initPlacement = new JComboBox<>(initPlace);
		initPlacement.setSelectedIndex(0);
		initPlacement.addActionListener(this);

		xOffSet = new WholeNumberField((int) ortho.getxoff(), 5);
		xOffSet.addCaretListener(this);

		yOffSet = new WholeNumberField((int) ortho.getyoff(), 5);
		yOffSet.addCaretListener(this);

		threeD = new JCheckBox("3D", false);
		threeD.addActionListener(this);
		threeD.setEnabled(false);

		anim = new JCheckBox("Animate", true);
		anim.addActionListener(this);
		doSwapping = new JCheckBox("Do swapping", false);
		doSwapping.addActionListener(this);
		doSwapBadies = new JCheckBox("Swap badies", false);
		doSwapBadies.addActionListener(this);
		doEdgeAdjustment = new JCheckBox("Adjust Edges", true);
		doEdgeAdjustment.addActionListener(this);

		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		panel.add(layoutButton, BorderLayout.SOUTH);
		panel.add(new JLabel(ortho.getName() + " control"), BorderLayout.NORTH);

		final JPanel controls = new JPanel();
		controls.setLayout(new GridLayout(6, 2));

		controls.add(new JLabel("Initial placement:"));
		controls.add(initPlacement);

		controls.add(new JLabel("X offset: "));
		controls.add(xOffSet);

		controls.add(new JLabel("Y offset: "));
		controls.add(yOffSet);

		controls.add(doSwapping);
		controls.add(doSwapBadies);

		controls.add(threeD);
		// controls.add(anim);

		controls.add(doEdgeAdjustment);
		panel.add(controls, BorderLayout.CENTER);

		final JPanel arg = new JPanel();
		arg.setLayout(new BorderLayout());
		arg.add(panel, BorderLayout.NORTH);

		return arg;
	}

	@Override
	public void actionPerformed(final java.awt.event.ActionEvent e) {
		// Debug.println("Should blend now");
		if (e.getSource() == threeD) {
			ortho.set3D(threeD.isSelected());
		} else if (e.getSource() == anim) {
			ortho.setAnim(anim.isSelected());
		} else if (e.getSource() == doSwapping) {
			ortho.setSwapping(doSwapping.isSelected());
		} else if (e.getSource() == doSwapBadies) {
			ortho.setSwapBadies(doSwapBadies.isSelected());
		} else if (e.getSource() == doEdgeAdjustment) {
			ortho.setEdgeAdjust(doEdgeAdjustment.isSelected());
		} else if (e.getSource() == initPlacement) {
			if (initPlacement.getSelectedIndex() == 0) {
				ortho.setInitPlacement("adjust");
			} else {
				ortho.setInitPlacement("random");
			}
		} else {
			Debug.println("Unexpected action event");
		}
	}

} // JJOrthoWindow
