/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Hashtable;

/**
 * JJAnimationControls.java
 *
 *
 * Created: Mon Nov 15 11:12:12 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.event.ChangeListener;

import org.carsten.jjgraph.util.Debug;

public class JJAnimationControls extends JPanel implements ActionListener, ChangeListener {
	private JButton blendB;
	private JSlider speedS;

	private JButton stopB;
	private JButton nextB;
	private JButton rewind;
	private JButton fastForward;

	private JComboBox<String> centerBox;

	private JCheckBox adjustRelPos;
	private JRadioButton moveNewB;
	private JRadioButton moveOldB;

	private JRadioButton pqInterpolBox;
	private JRadioButton clusterInterpolBox;

	private JRadioButton interpolateButton;
	private JRadioButton interpolateOrthoButton;
	private JRadioButton forceDirectedB;
	private JRadioButton springB;

	private JCheckBox rotateBox;
	private JRadioButton rotateAvgB;
	private JRadioButton rotateFullB;
	private JRadioButton rotateNewB;

	protected JJGraphAnimator graphAnimator;

	public void setGraphAnimator(final JJGraphAnimator b) {
		graphAnimator = b;
	}

	private final Timer timer = new Timer((int) JJGraphAnimator.SLEEP_TIME, this);

	public void disableWidgets() {
		blendB.setEnabled(false);
		stopB.setEnabled(false);
		nextB.setEnabled(false);
		rewind.setEnabled(false);
		fastForward.setEnabled(false);
	}

	public void enableWidgets() {
		blendB.setEnabled(true);
		stopB.setEnabled(false);
		nextB.setEnabled(true);
		rewind.setEnabled(true);
		fastForward.setEnabled(true);
	}

	public JJAnimationControls() {
	}

	protected JPanel createButtonRow() {
		final URLClassLoader cl = (URLClassLoader) this.getClass().getClassLoader();

		URL url = cl.findResource("sunSupp/toolbarButtonGraphics/media/play16.gif");
		blendB = new JButton(new ImageIcon(url));
		blendB.setToolTipText("Show animation");
		blendB.addActionListener(this);

		url = cl.findResource("sunSupp/toolbarButtonGraphics/media/stop16.gif");
		stopB = new JButton(new ImageIcon(url));
		stopB.setToolTipText("Stop animation");
		stopB.addActionListener(this);

		url = cl.findResource("sunSupp/toolbarButtonGraphics/media/StepForward16.gif");
		nextB = new JButton(new ImageIcon(url));
		nextB.setToolTipText("Step one frame forward");
		nextB.addActionListener(this);

		url = cl.findResource("sunSupp/toolbarButtonGraphics/media/Rewind16.gif");
		rewind = new JButton(new ImageIcon(url));
		rewind.setToolTipText("Rewind to the first frame");
		rewind.addActionListener(this);

		url = cl.findResource("sunSupp/toolbarButtonGraphics/media/FastForward16.gif");
		fastForward = new JButton(new ImageIcon(url));
		fastForward.setToolTipText("Fast forward to last frame");
		fastForward.addActionListener(this);

		final JPanel buttonPane = new JPanel();

		buttonPane.add(rewind);
		buttonPane.add(fastForward);
		buttonPane.add(stopB);
		buttonPane.add(blendB);
		buttonPane.add(nextB);
		return buttonPane;
	}

	class InterpolateListener implements ActionListener {
		@Override
		public void actionPerformed(final java.awt.event.ActionEvent e) {
			if (e.getSource() == interpolateButton) {
				graphAnimator.setInterpolateMode(JJInterpolator.DIRECT_INTERPOLATION);
			} else if (e.getSource() == interpolateOrthoButton) {
				graphAnimator.setInterpolateMode(JJInterpolator.ORTHO_INTERPOLATION);
			} else if (e.getSource() == forceDirectedB) {
				graphAnimator.setInterpolateMode(JJInterpolator.GEM_INTERPOLATION);
			} else if (e.getSource() == springB) {
				graphAnimator.setInterpolateMode(JJInterpolator.SPRING_INTERPOLATION);
			} else if (e.getSource() == pqInterpolBox) {
				graphAnimator.setInterpolateMode(JJInterpolator.PQ_INTERPOLATION);
			} else if (e.getSource() == clusterInterpolBox) {
				graphAnimator.setInterpolateMode(JJInterpolator.CLUSTER_INTERPOLATION);
			}

		}
	}

	private JPanel createInterpolatePanel() {
		final JPanel bcp = new JPanel();
		bcp.setLayout(new BorderLayout());
		final InterpolateListener tl = new InterpolateListener();

		final JLabel rotateL = new JLabel("Blend");

		final JPanel p1 = new JPanel();
		final JPanel p2 = new JPanel();
		final JPanel p3 = new JPanel();

		interpolateButton = new JRadioButton("Direct", true);
		interpolateButton.addActionListener(tl);
		interpolateOrthoButton = new JRadioButton("Orthogonal");
		interpolateOrthoButton.addActionListener(tl);
		forceDirectedB = new JRadioButton("Force directed");
		forceDirectedB.addActionListener(tl);
		springB = new JRadioButton("Spring");
		springB.addActionListener(tl);
		pqInterpolBox = new JRadioButton("PQ Decomposition", false);
		pqInterpolBox.addActionListener(tl);
		clusterInterpolBox = new JRadioButton("Matrix Cluster", false);
		clusterInterpolBox.addActionListener(tl);

		final ButtonGroup group = new ButtonGroup();
		group.add(interpolateButton);
		group.add(interpolateOrthoButton);
		group.add(forceDirectedB);
		group.add(springB);
		group.add(pqInterpolBox);
		group.add(clusterInterpolBox);

		p1.add(interpolateButton);
		p1.add(interpolateOrthoButton);
		p2.add(forceDirectedB);
		p2.add(springB);
		p3.add(pqInterpolBox);
		p3.add(clusterInterpolBox);

		bcp.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2),
				BorderFactory.createEtchedBorder()));
		bcp.add(rotateL, BorderLayout.NORTH);
		final JPanel p4 = new JPanel();
		p4.setLayout(new BorderLayout());
		p4.add(p1, BorderLayout.NORTH);
		p4.add(p2, BorderLayout.CENTER);
		p4.add(p3, BorderLayout.SOUTH);

		bcp.add(p4, BorderLayout.CENTER);

		return bcp;
	}

	private JPanel createCenterPanel() {
		final JPanel pa = new JPanel();
		final String adjustList[] = { "Constant", "Accel-Decel", "Accelerating", "Decelerating" };
		centerBox = new JComboBox<>(adjustList);
		centerBox.setSelectedIndex(0);
		centerBox.addActionListener(this);

		pa.add(new JLabel("Animation speed: "));
		pa.add(centerBox, BorderLayout.NORTH);
		return pa;
	}

	private JPanel createSpeedPanel() {
		final JPanel pa = new JPanel();

		speedS = new JSlider(JSlider.HORIZONTAL, 1, 20, graphAnimator.getSpeed());
		speedS.addChangeListener(this);

		final Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
		labelTable.put(new Integer(1), new JLabel("Slow"));
		labelTable.put(new Integer(20), new JLabel("Fast"));
		labelTable.put(new Integer(graphAnimator.getSpeed()), new JLabel("^"));
		speedS.setLabelTable(labelTable);
		speedS.setPaintLabels(true);
		speedS.setMajorTickSpacing(5);
		speedS.setMinorTickSpacing(1);
		speedS.setPaintTicks(true);

		pa.add(new JLabel("Animation Speed: "), BorderLayout.CENTER);
		pa.add(speedS, BorderLayout.CENTER);
		return pa;
	}

	public JPanel createTab() {
		final JPanel buttonRow = createButtonRow();

		final JPanel interpolatePanel = createInterpolatePanel();

		final JPanel customPanel = createCustomPanel();
		final JPanel speedPanel = createSpeedPanel();
		final JPanel centerPanel = createCenterPanel();

		final JLabel titleLabel = new JLabel("Marey Control");

		final JPanel p00 = new JPanel();
		p00.setLayout(new BorderLayout());
		p00.add(titleLabel, BorderLayout.NORTH);
		p00.add(centerPanel, BorderLayout.SOUTH);

		final JPanel p11 = new JPanel();
		p11.setLayout(new BorderLayout());
		p11.add(p00, BorderLayout.NORTH);
		p11.add(speedPanel, BorderLayout.SOUTH);

		final JPanel p12 = new JPanel();
		p12.setLayout(new BorderLayout());

		final JPanel p21 = new JPanel();
		p21.setLayout(new BorderLayout());
		p21.add(interpolatePanel, BorderLayout.SOUTH);

		final JPanel p22 = new JPanel();
		p22.setLayout(new BorderLayout());
		p22.add(buttonRow, BorderLayout.NORTH);
		p22.add(customPanel, BorderLayout.SOUTH);

		final JPanel p1 = new JPanel();
		p1.setLayout(new BorderLayout());

		final JPanel p2 = new JPanel();
		p2.setLayout(new BorderLayout());

		final JPanel p0 = new JPanel();
		p0.setLayout(new BorderLayout());

		p1.add(p12, BorderLayout.SOUTH);
		p1.add(p11, BorderLayout.NORTH);

		p2.add(p22, BorderLayout.SOUTH);
		p2.add(p21, BorderLayout.NORTH);

		p0.add(p2, BorderLayout.SOUTH);
		p0.add(p1, BorderLayout.NORTH);

		final JPanel arg = new JPanel();
		arg.setLayout(new BorderLayout());
		arg.add(p0, BorderLayout.NORTH);

		return arg;
	}

	@Override
	public void actionPerformed(final java.awt.event.ActionEvent e) {
		// Debug.println("Should blend now");
		if (e.getSource() == rewind) {
			rewind();
		} else if (e.getSource() == fastForward) {
			fastForward();
		} else if (e.getSource() == nextB) {
			if (graphAnimator.hasNext()) {
				next();
			}
		} else if (e.getSource() == timer) {
			if (!next()) {
				stopAnimation();

			}
		} else if (e.getSource() == blendB) {
			if (!graphAnimator.hasNext())
				rewind();
			if (graphAnimator.hasNext()) {
				startAnimation();
			}
		} else if (e.getSource() == stopB) {
			stopAnimation();
		} else if (e.getSource() == centerBox) {
			// Debug.println("Setting speed fkt");
			graphAnimator.setSpeedFkt(centerBox.getSelectedIndex());
		} else {
			Debug.println("JJAnimationControls: Unexpected action event:" + e);
		}
	}

	@Override
	public void stateChanged(final javax.swing.event.ChangeEvent e) {
		if (e.getSource() == speedS) {
			graphAnimator.setSpeed(speedS.getValue());
		}
	}

	public void rewind() {
		graphAnimator.rewind();
	}

	public void fastForward() {
		graphAnimator.fastForward();
	}

	public boolean next() {
		return graphAnimator.next();
	}

	public void stopAnimation() {
		timer.stop();
		enableWidgets();
	}

	public void startAnimation() {
		disableWidgets();
		stopB.setEnabled(true);
		timer.start();
	}

	public JPanel createCustomPanel() {
		return new JPanel();
	}

} // JJAnimationControls
