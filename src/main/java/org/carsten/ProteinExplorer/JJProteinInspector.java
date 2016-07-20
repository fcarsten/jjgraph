/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.ProteinExplorer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * JJProteinInspector.java
 *
 *
 * Created: Mon Nov 15 11:12:12 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.carsten.jjgraph.animation.JJGraphAnimationWindow;
import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJGraphicEdge;
import org.carsten.jjgraph.graph.JJInspectable;
import org.carsten.jjgraph.layout.JJLayout;
import org.carsten.jjgraph.util.Debug;

public class JJProteinInspector implements JJInspectable, ActionListener, ChangeListener {
	private final JJGraph graph;
	private final JJGraphWindow fenster;
	private JComboBox<String> actionBox;
	private final Set<String> actionSet = new HashSet<>();
	private final JJLayout proteinLayout;
	private final JCheckBox hideUninvolvedBox = new JCheckBox("Hide non involved proteins", true);
	private final JSlider historySizeSlider = new JSlider(1, 10, 1);
	private final JLabel historySizeLabel = new JLabel("History: 1");
	private String selectionHistory[] = { "None" };
	private final Color edgeColors[] = { Color.red, Color.blue, Color.green, Color.cyan, Color.magenta, Color.yellow,
			new Color(100, 0, 0), new Color(0, 100, 0), new Color(0, 0, 100), new Color(150, 150, 150) };
	private int colorIterator = 0;

	@Override
	public void stateChanged(final ChangeEvent e) {
		historySizeLabel.setText("History: " + historySizeSlider.getValue());
	}

	public JJGraphWindow getWindow() {
		return fenster;
	}

	public boolean isHideUninvolved() {
		return hideUninvolvedBox.isSelected();
	}

	public void setHideUninvolved(final boolean b) {
		hideUninvolvedBox.setSelected(b);
	}

	public Collection<String> getActionSet() {
		return actionSet;
	}

	@Override
	public void actionPerformed(final java.awt.event.ActionEvent e) {
		if (e.getSource() == actionBox) {
			final String str = (String) actionBox.getSelectedItem();
			selectInteraction(str);
		}
	}

	public void selectInteraction(final String str) {
		final JJGraphAnimationWindow animWindow = (JJGraphAnimationWindow) fenster
				.getTool(JJGraphAnimationWindow.class);
		if (animWindow != null) {
			animWindow.setStartPos();
		} else {
			Debug.println("Couldn't find animator");
		}

		colorIterator++;
		colorIterator = colorIterator % edgeColors.length;

		if (historySizeSlider.getValue() != selectionHistory.length) {
			final String tmpS[] = new String[historySizeSlider.getValue()];
			for (int i = 0; i < Math.min(tmpS.length, selectionHistory.length); i++) {
				tmpS[i] = selectionHistory[i];
			}
			selectionHistory = tmpS;
		}

		for (int i = selectionHistory.length - 1; i > 0; i--) {
			selectionHistory[i] = selectionHistory[i - 1];
		}
		selectionHistory[0] = str;
		showOnlyEdges();

		proteinLayout.layout();

		if (animWindow != null) {
			animWindow.setEndPos();
			animWindow.rewind();
			animWindow.startAnimation();
		}
	}

	public void showOnlyEdges() {
		// Debug.println("Showing edges");

		for (final Iterator<JJEdge> iter = graph.edgeIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			final JJGraphicEdge ge = tmpE.getGraphicEdge(fenster);
			ge.hide();
			for (int i = 0; i < selectionHistory.length; i++) {

				if (selectionHistory[i] != null) {
					if (selectionHistory[i].compareToIgnoreCase("None") == 0)
						break;
					if (selectionHistory[i].compareToIgnoreCase(tmpE.getName()) == 0
							|| (selectionHistory[i].compareToIgnoreCase("All") == 0 && i == 0)) {
						ge.unhide();
						ge.setColor(edgeColors[(edgeColors.length + i - colorIterator) % edgeColors.length]);
						break;
					}
				}
			}
		}
	}

	@Override
	public JPanel createTab() {
		final JPanel arg = new JPanel();
		arg.setLayout(new BorderLayout());
		arg.add(new JLabel("Select proteins"), BorderLayout.NORTH);

		final String actionList[] = new String[actionSet.size() + 2];
		int i = 2;
		actionList[0] = "None";
		actionList[1] = "All";

		for (final String string : actionSet) {
			actionList[i++] = string;
		}

		actionBox = new JComboBox<>(actionList);
		actionBox.setSelectedIndex(0);
		actionBox.addActionListener(this);

		final JPanel controls = new JPanel();
		controls.setLayout(new GridLayout(2, 2));

		controls.add(actionBox);
		controls.add(hideUninvolvedBox);
		controls.add(historySizeLabel);
		controls.add(historySizeSlider);
		historySizeSlider.addChangeListener(this);
		arg.add(controls, BorderLayout.CENTER);

		final JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(arg, BorderLayout.NORTH);

		return p;
	}

	public JJProteinInspector(final JJGraphWindow f, final Set<String> as) {
		fenster = f;
		graph = fenster.getGraph();
		actionSet.clear();
		actionSet.addAll(as);
		proteinLayout = new JJProteinLayout(this);

	}

	@Override
	public String getTabName() {
		return "ProteinInspector";
	}

} // JJProteinInspector
