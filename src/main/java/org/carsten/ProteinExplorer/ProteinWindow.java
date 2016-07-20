/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.ProteinExplorer;

/**
 * ProteinWindow.java
 *
 *
 * Created: Thu May 04 09:46:51 2000
 *
 * @author $Author: carsten $
 * @version $Revision: 1.6 $ $Date: 2003/02/09 05:50:52 $
 *
 * $Log: ProteinWindow.java,v $
 * Revision 1.6  2003/02/09 05:50:52  carsten
 * Changed some startup value to better values
 *
 * Revision 1.5  2002/12/05 06:41:28  carsten
 * More network support
 *
 * Revision 1.4  2002/12/04 06:24:10  carsten
 * Added rudimentary network support
 *
 * Revision 1.3  2002/08/30 06:56:11  carsten
 * Fixed null pointer exception bug in actionPerformed
 *
 * Revision 1.2  2002/08/15 00:48:45  carsten
 * Select interaction from node popup menu
 *
 * Revision 1.1  2002/08/14 05:53:36  carsten
 * New Protein Explorer functionality
 * * Node popups
 * * All in one package now
 *
 *
 */

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JMenuItem;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJGraphWindowImpl;
import org.carsten.jjgraph.graph.JJGraphicNode;
import org.carsten.jjgraph.graph.JJInspectable;
import org.carsten.jjgraph.util.Debug;

public class ProteinWindow extends JJGraphWindowImpl {
	private JJProteinInspector protInsp = null;
	static private ProteinListener protListener = null;

	public JJProteinInspector getProteinInspector() {
		return protInsp;
	}

	public ProteinGraph proteinGraph() {
		return (ProteinGraph) graph;
	}

	@Override
	public void addTool(final JJInspectable e) {
		if (e instanceof JJProteinInspector) {
			Debug.println("Adding protein inspector");

			protInsp = (JJProteinInspector) e;
			protInsp.selectInteraction(("None"));
		}

		super.addTool(e);
	}

	@Override
	public void createNodeMenu() {
		nodePopup.removeAll();

		final Set<String> actions = new HashSet<>();
		for (final Object element : selectedNodes) {
			final JJGraphicNode gn = (JJGraphicNode) element;
			for (final Iterator<JJEdge> edgeIter = gn.getNode().edgeIterator(); edgeIter.hasNext();) {
				final JJEdge tmpE = edgeIter.next();
				actions.add(tmpE.getName());
			}
		}
		deselectNodes();

		for (final Object element : actions) {
			final String tmpS = (String) element;
			final JMenuItem item = new JMenuItem(tmpS);
			item.setActionCommand("select " + tmpS);
			item.addActionListener(this);
			nodePopup.add(item);
		}

	}

	@Override
	public void actionPerformed(final java.awt.event.ActionEvent e) {
		if ((e.getActionCommand() != null) && e.getActionCommand().startsWith("select ")) {
			final String command = e.getActionCommand().substring(7);
			Debug.println("Command: " + command);

			if (protInsp != null) {
				protInsp.selectInteraction(command);
			}
		} else
			super.actionPerformed(e);
	}

	@Override
	public void showNodePopup(final Component c, final int x, final int y) {
		createNodeMenu();
		nodePopup.show(c, x, y);
	}

	public ProteinWindow(final JJGraph g, final JJGraphWindow w) {
		this(g);
		if (w != null)
			copyGraphicData(w);
	}

	@Override
	public void copyGraphicData(final JJGraphWindow w) {
		super.copyGraphicData(w);
	}

	public ProteinWindow(final JJGraph g) {
		super(g);
		if (protListener == null) {
			protListener = new ProteinListener(this);
			final Thread thread = new Thread(protListener);
			thread.start();
		}
		zoomTo(0.17);
	}

}
