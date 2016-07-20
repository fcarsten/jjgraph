/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * JJGraphWindow.java
 *
 *
 * Created: Fri Feb 26 18:35:41 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.carsten.jjgraph.clustering.JJCluster;
import org.carsten.jjgraph.clustering.JJEigenBipart;
import org.carsten.jjgraph.clustering.JJMultiwayKL;
import org.carsten.jjgraph.clustering.JJRandBipart;
import org.carsten.jjgraph.clustering.JJRandCluster;
import org.carsten.jjgraph.clustering.JJSA;
import org.carsten.jjgraph.francois.FR;
import org.carsten.jjgraph.francois.FRCC;
import org.carsten.jjgraph.francois.JJSugi;
import org.carsten.jjgraph.layout.JJAGD;
import org.carsten.jjgraph.layout.JJCircularLayout;
import org.carsten.jjgraph.layout.JJDOT;
import org.carsten.jjgraph.layout.JJEigenLayout;
import org.carsten.jjgraph.layout.JJEigenWindow;
import org.carsten.jjgraph.layout.JJGEMWindow;
import org.carsten.jjgraph.layout.JJGem;
import org.carsten.jjgraph.layout.JJLayout;
import org.carsten.jjgraph.layout.JJOrtho;
import org.carsten.jjgraph.layout.JJOrthoWindow;
import org.carsten.jjgraph.layout.JJRandomLayout;
import org.carsten.jjgraph.layout.JJShake;
import org.carsten.jjgraph.layout.JJSpring3D;
import org.carsten.jjgraph.layout.JJTree;
import org.carsten.jjgraph.layout.JJTutte;
import org.carsten.jjgraph.metromap.JJMetromap;
import org.carsten.jjgraph.util.Debug;
import org.carsten.jjgraph.util.JJUndoWindow;

public class JJGraphFrame extends JFrame
		implements DropTargetListener, ChangeListener, WindowListener, ActionListener, AdjustmentListener {
	protected JJGraphWindowImpl panel;
	public final static int canvasWidth = 5000;
	public final static int canvasHeight = 5000;

	public JJGraphWindowImpl getPanel() {
		return panel;
	}

	protected JCheckBoxMenuItem showEdgeLabelsItem;
	protected JJLayoutMenuHandler layoutMenuHandler;
	protected JJClusterMenuHandler clusterMenuHandler;
	protected JCheckBoxMenuItem showNodesButton;
	protected JCheckBoxMenuItem showEdgesButton;

	protected JScrollPane scrollPane;
	protected JMenuItem reloadItem;
	protected JMenuItem saveItem;
	protected JMenuItem directedItem;
	protected JMenuItem optimizeItem;
	protected JSlider zoomScale;
	protected JJInspector inspector;
	protected JLabel statusLabel;
	protected JJHelp helpPanel;

	public void setShowEdgeLabels(final boolean b) {
		if (showEdgeLabelsItem.isSelected() != b)
			showEdgeLabelsItem.setSelected(b);
	}

	public void setStatusMessage(final String s) {
		if (statusLabel != null)
			statusLabel.setText(s);
		else
			Debug.println(s);

	}

	public void setStatusColor(final Color c) {
		if (statusLabel != null)
			statusLabel.setForeground(c);
	}

	public JJClusterMenuHandler getClusterHandler() {
		return clusterMenuHandler;
	}

	public void enableSaveReload() {
		if (reloadItem != null)
			reloadItem.setEnabled(true);
		if (saveItem != null)
			saveItem.setEnabled(true);
	}

	public void createToolsMenu(final JComponent theMenu, final ActionListener target) {
		JMenuItem item;

		item = new JMenuItem("Inspector");
		item.setActionCommand("inspector");
		item.setToolTipText("Set tool specific options");
		item.addActionListener(this);
		theMenu.add(item);

		item = new JMenuItem("Remove Bends");
		item.setActionCommand("removeBends");
		item.setToolTipText("Straightens all edges");
		item.addActionListener(target);
		theMenu.add(item);

		item = new JMenuItem("Show all");
		item.setActionCommand("showAll");
		item.setToolTipText("Make all graph elements visible");
		item.addActionListener(target);
		theMenu.add(item);

		item = new JMenuItem("Bends to Nodes");
		item.setActionCommand("allBendsToNodes");
		item.setToolTipText("Replace all bends in edges by new nodes");
		item.addActionListener(target);
		theMenu.add(item);

		item = new JMenuItem("Revert all edges");
		item.setActionCommand("revertEdges");
		item.setToolTipText("Revert the direction of all edges");
		item.addActionListener(target);
		theMenu.add(item);

		// item = new JMenuItem("Remove parallel edges");
		// item.setActionCommand("removeParallelEdges");
		// item.setToolTipText("Remove all but one edges between each source and
		// target node");
		// item.addActionListener(target);
		// theMenu.add(item);

		item = new JMenuItem("Remove reflexiv edges");
		item.setActionCommand("removeReflexiveEdges");
		item.setToolTipText("Remove all edges with same source and target node");
		item.addActionListener(target);
		theMenu.add(item);

		item = new JMenuItem("Find Graph");
		item.setActionCommand("findGraph");
		item.setToolTipText("Scroll graph into the visible region of the window");
		item.addActionListener(target);
		theMenu.add(item);

		item = new JMenuItem("Center Graph");
		item.setActionCommand("centerGraph");
		item.setToolTipText("Move center of graph to (0,0,0)");
		item.addActionListener(target);
		theMenu.add(item);

		item = new JMenuItem("Delaunay");
		item.setActionCommand("delaunay");
		item.setToolTipText("Compute Delaunay triangulation of nodes");
		item.addActionListener(target);
		theMenu.add(item);

		item = new JMenuItem("Remove Link Nodes");
		item.setActionCommand("removeLinkNodes");
		item.setToolTipText("Replace nodes of degree 2 with an edge");
		item.addActionListener(target);
		theMenu.add(item);

		item = new JMenuItem("Compact Edges");
		item.setActionCommand("compactEdges");
		item.setToolTipText("Replace edges between same nodes by one egde");
		item.addActionListener(target);
		theMenu.add(item);

		final JMenu transformMenu = new JMenu("Transform");
		transformMenu.setToolTipText("Geometric transformations of the graph");
		theMenu.add(transformMenu);

		final JMenu rotateMenu = new JMenu("Rotate");
		rotateMenu.setToolTipText("Rotate graph");
		transformMenu.add(rotateMenu);

		item = new JMenuItem("90");
		item.setActionCommand("rotate90");
		item.setToolTipText("Rotate graph by 90 degree");
		item.addActionListener(target);
		rotateMenu.add(item);

		item = new JMenuItem("180");
		item.setActionCommand("rotate180");
		item.setToolTipText("Rotate graph by 180 degree");
		item.addActionListener(target);
		rotateMenu.add(item);

		item = new JMenuItem("270");
		item.setActionCommand("rotate270");
		item.setToolTipText("Rotate graph by 270 degree");
		item.addActionListener(target);
		rotateMenu.add(item);

		item = new JMenuItem("other");
		item.setActionCommand("rotateOther");
		item.setToolTipText("Rotate graph by user specified degree");
		item.addActionListener(target);
		rotateMenu.add(item);

		final JMenu scaleMenu = new JMenu("Scale");
		rotateMenu.setToolTipText("Scale graph");
		transformMenu.add(scaleMenu);

		item = new JMenuItem("50");
		item.setActionCommand("scale50");
		item.setToolTipText("Scale graph by 50%");
		item.addActionListener(target);
		scaleMenu.add(item);

		item = new JMenuItem("75");
		item.setActionCommand("scale75");
		item.setToolTipText("Scale graph by 75%");
		item.addActionListener(target);
		scaleMenu.add(item);

		item = new JMenuItem("200");
		item.setActionCommand("scale200");
		item.setToolTipText("Scale graph by 200%");
		item.addActionListener(target);
		scaleMenu.add(item);

		item = new JMenuItem("300");
		item.setActionCommand("scale300");
		item.setToolTipText("Scale graph by 300%");
		item.addActionListener(target);
		scaleMenu.add(item);

		item = new JMenuItem("400");
		item.setActionCommand("scale400");
		item.setToolTipText("Scale graph by 400%");
		item.addActionListener(target);
		scaleMenu.add(item);

		item = new JMenuItem("other");
		item.setActionCommand("scaleOther");
		item.setToolTipText("Scale graph by user specified %");
		item.addActionListener(target);
		scaleMenu.add(item);

		// item = new JMenuItem("Zoom to fit");
		// item.setActionCommand("zoomToFit");
		// item.addActionListener(target);
		// theMenu.add(item);

		final JMenu subMenu = new JMenu("other");
		subMenu.setToolTipText("Other geometric transformations");
		theMenu.add(subMenu);

		item = new JMenuItem("Flip X");
		item.setActionCommand("flipX");
		item.setToolTipText("Flip graph along X axis");
		item.addActionListener(target);
		transformMenu.add(item);

		item = new JMenuItem("Flip Y");
		item.setActionCommand("flipY");
		item.setToolTipText("Flip graph along Y axis");
		item.addActionListener(target);
		transformMenu.add(item);

		item = new JMenuItem("Skew");
		item.setActionCommand("skew");
		item.setToolTipText("Skew graph by user specified angle");
		item.addActionListener(target);
		transformMenu.add(item);

		item = new JMenuItem("Make all nodes visible");
		item.setActionCommand("makeVisible");
		item.setToolTipText("Make all nodes visible");
		item.addActionListener(target);
		subMenu.add(item);

		item = new JMenuItem("Diameter");
		item.setActionCommand("diameter");
		item.setToolTipText("Compute and ouput the diameter of the graph");
		item.addActionListener(target);
		subMenu.add(item);

		item = new JMenuItem("Dfs");
		item.setActionCommand("dfs");
		item.setToolTipText("Compute DFS numbering of the graph");
		item.addActionListener(target);
		subMenu.add(item);

		item = new JMenuItem("ST-Numbering");
		item.setActionCommand("stnum");
		item.setToolTipText("Compute ST numbering of the graph");
		item.addActionListener(target);
		subMenu.add(item);

		item = new JMenuItem("View 3D");
		item.setActionCommand("view3D");
		item.setToolTipText("View graph in 3D window");
		item.addActionListener(target);
		theMenu.add(item);

		if (!Debug.isApplet) {
			try {
				Class.forName("javax.media.j3d.Canvas3D");
			} catch (final ClassNotFoundException c) {
				Debug.println("Couldn't find 3D library");
				item.setEnabled(false);
			} catch (final java.lang.UnsatisfiedLinkError c) {
				Debug.println("Couldn't link 3D library");
				item.setEnabled(false);
			} catch (final java.lang.NoClassDefFoundError c) {
				Debug.println("Couldn't find 3D definitions");
				item.setEnabled(false);
			}
		} else
			item.setEnabled(false);

		if (Debug.DEBUG) {
			item = new JMenuItem("Bounding Box");
			item.setActionCommand("bbox");
			item.setToolTipText("Compute and output bounding box of graph drawing");
			item.addActionListener(target);
			theMenu.add(item);
		}
	}

	public void createEditMenu(final JMenuBar menuBar, final ActionListener target) {

		final JMenu theMenu = new JMenu("Edit");
		theMenu.getAccessibleContext().setAccessibleDescription("Edit");
		theMenu.setToolTipText("Edit menu");
		menuBar.add(theMenu);
		JMenuItem item;

		final JJUndoWindow uw = new JJUndoWindow(getPanel().getGraph().getUndoManager());

		directedItem = new JMenuItem("Undo");
		directedItem.setActionCommand("undo");
		directedItem.setToolTipText("Undo last operation");
		directedItem.addActionListener(uw);
		theMenu.add(directedItem);
		addTool(uw);

		directedItem = new JCheckBoxMenuItem("Directed", panel.getGraph().isDirected());
		directedItem.setActionCommand("ShowDirection");
		directedItem.setToolTipText("Change directedness of graph");
		directedItem.addActionListener(target);
		theMenu.add(directedItem);

		item = new JMenuItem("Cut");
		item.setActionCommand("cut");
		item.setToolTipText("Cut selected sub graph to clipboard");
		item.addActionListener(target);
		// item.setEnabled(false);
		theMenu.add(item);

		item = new JMenuItem("Copy");
		item.setActionCommand("copy");
		item.setToolTipText("Copy selected subgraph to clipboard");
		item.addActionListener(target);
		// item.setEnabled(false);
		theMenu.add(item);

		item = new JMenuItem("Paste");
		item.setActionCommand("paste");
		item.setToolTipText("Paste from clipboard");
		item.addActionListener(target);
		// item.setEnabled(false);
		theMenu.add(item);

		item = new JMenuItem("Remove all edges");
		item.setActionCommand("clearEdges");
		item.setToolTipText("Remove all edge from graph");
		item.addActionListener(target);
		theMenu.add(item);

		item = new JMenuItem("Clear");
		item.setActionCommand("clear");
		item.setToolTipText("Remove all nodes and edges from graph");
		item.addActionListener(target);
		theMenu.add(item);
	}

	class LanguageListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			panel.setLanguage(e.getActionCommand());
		}

	}

	public void setShowNodes(final boolean flag) {
		showNodesButton.setSelected(flag);
	}

	public void setShowEdges(final boolean flag) {
		showEdgesButton.setSelected(flag);
	}

	public boolean getShowNodes() {
		return showNodesButton.isSelected();
	}

	public boolean getShowEdges() {
		return showEdgesButton.isSelected();
	}

	public void createOptionsMenu(final JMenuBar menuBar, final ActionListener target) {
		// Build the first menu.
		final JMenu theMenu = new JMenu("Options");
		theMenu.getAccessibleContext().setAccessibleDescription("Options");
		theMenu.setToolTipText("Configure tools and appearance");
		menuBar.add(theMenu);

		createOptionsMenuItems(theMenu, target);
	}

	public void createOptionsMenuItems(final JMenu theMenu, final ActionListener target) {
		JMenuItem item;

		showNodesButton = new JCheckBoxMenuItem("Show Nodes", true);
		showNodesButton.setActionCommand("ShowNodes");
		showNodesButton.setToolTipText("Show/hide nodes");
		showNodesButton.addActionListener(target);
		theMenu.add(showNodesButton);

		showEdgesButton = new JCheckBoxMenuItem("Show Edges", true);
		showEdgesButton.setActionCommand("ShowEdges");
		showEdgesButton.setToolTipText("Show/hide edges");
		showEdgesButton.addActionListener(target);
		theMenu.add(showEdgesButton);

		item = new JCheckBoxMenuItem(" Draw Splines", true);
		item.setActionCommand("drawSplines");
		item.setToolTipText("Draw edges with bends as splines if possible");
		item.addActionListener(target);
		item.setSelected(false);
		theMenu.add(item);

		item = new JCheckBoxMenuItem("Show node labels", true);
		item.setActionCommand("ShowNodeLabels");
		item.setToolTipText("Show/hide node labels");
		item.addActionListener(target);
		theMenu.add(item);

		showEdgeLabelsItem = new JCheckBoxMenuItem("Show edge labels", true);
		showEdgeLabelsItem.setActionCommand("ShowEdgeLabels");
		showEdgeLabelsItem.setToolTipText("Show/hide edge labels");
		showEdgeLabelsItem.addActionListener(target);
		theMenu.add(showEdgeLabelsItem);

		item = new JCheckBoxMenuItem("Short labels", true);
		item.setActionCommand("shortLabels");
		item.setToolTipText("Abbreviate long labels");
		item.addActionListener(target);
		theMenu.add(item);

		item = new JCheckBoxMenuItem("Outline", false);
		item.setActionCommand("outline");
		item.setToolTipText("Draw nodes with outline");
		item.addActionListener(target);
		theMenu.add(item);

		// item = new JCheckBoxMenuItem("Grid", false);
		// item.setActionCommand("grid");
		// item.setToolTipText("Place nodes on grid positions");
		// item.addActionListener(target);
		// theMenu.add(item);

		item = new JMenuItem("Font");
		item.setActionCommand("font");
		item.setToolTipText("Choose font for labels");
		item.addActionListener(target);
		theMenu.add(item);

		final JMenu submenu = new JMenu("Language");
		submenu.setToolTipText("Select Language");
		theMenu.add(submenu);
		final LanguageListener l = new LanguageListener();

		final ButtonGroup group = new ButtonGroup();

		item = new JRadioButtonMenuItem("English", true);
		item.setActionCommand("english");
		item.addActionListener(l);
		submenu.add(item);
		group.add(item);

		item = new JRadioButtonMenuItem("Deutsch", false);
		item.setActionCommand("deutsch");
		item.addActionListener(l);
		submenu.add(item);
		group.add(item);

		item = new JRadioButtonMenuItem("Boarisch", false);
		item.setActionCommand("boarisch");
		item.addActionListener(l);
		submenu.add(item);
		group.add(item);

		item = new JRadioButtonMenuItem("Dansk", false);
		item.setActionCommand("dansk");
		item.addActionListener(l);
		submenu.add(item);
		group.add(item);

		item = new JRadioButtonMenuItem("Svenska", false);
		item.setActionCommand("svenska");
		item.addActionListener(l);
		submenu.add(item);
		group.add(item);

		item = new JRadioButtonMenuItem("Espanol", false);
		item.setActionCommand("espanol");
		item.addActionListener(l);
		submenu.add(item);
		group.add(item);

		item = new JRadioButtonMenuItem("Francais", false);
		item.setActionCommand("francais");
		item.addActionListener(l);
		submenu.add(item);
		group.add(item);

		item = new JRadioButtonMenuItem("Seggssch", false);
		item.setActionCommand("seggssch");
		item.addActionListener(l);
		submenu.add(item);
		group.add(item);

	}

	public void createFileMenu(final JMenu theMenu, final ActionListener target) {
		JMenuItem item;

		item = new JMenuItem("New graph");
		item.setActionCommand("new");
		item.setToolTipText("Create new graph");
		item.addActionListener(target);
		theMenu.add(item);

		item = new JMenuItem("New view");
		item.setActionCommand("newView");
		item.setToolTipText("Create new view on current graph");
		item.addActionListener(target);
		theMenu.add(item);

		if (!Debug.isApplet) {
			item = new JMenuItem("Load");
			item.setActionCommand("load");
			item.setToolTipText("Load graph from file");
			item.addActionListener(target);
			theMenu.add(item);

			item = new JMenuItem("Load Layout");
			item.setActionCommand("loadLayout");
			item.setToolTipText("Load new layout for current graph");
			item.addActionListener(target);
			theMenu.add(item);

			reloadItem = new JMenuItem("Reload");
			reloadItem.setActionCommand("reload");
			reloadItem.setToolTipText("Reload graph from file");
			reloadItem.addActionListener(target);
			reloadItem.setEnabled(false);
			theMenu.add(reloadItem);

			saveItem = new JMenuItem("Save");

			saveItem.setActionCommand("save");
			saveItem.setToolTipText("Save graph to file");
			saveItem.addActionListener(target);
			saveItem.setEnabled(false);
			theMenu.add(saveItem);

			item = new JMenuItem("Save as");
			item.setActionCommand("saveas");
			item.setToolTipText("Save graph into new file");
			item.addActionListener(target);
			theMenu.add(item);

			item = new JMenuItem("Save image");
			item.setActionCommand("saveToImage");
			item.setToolTipText("Save graph as image file");
			item.addActionListener(target);
			theMenu.add(item);

			item = new JMenuItem("Save povray");
			item.setActionCommand("saveToPovray");
			item.setToolTipText("Save graph as 3D Povray file");
			item.addActionListener(target);
			theMenu.add(item);
		}

		item = new JMenuItem("Print");
		item.setActionCommand("print");
		item.setToolTipText("Print graph");
		item.addActionListener(target);
		theMenu.add(item);

		theMenu.addSeparator();

		item = new JMenuItem("Close");
		item.setActionCommand("close");
		item.setToolTipText("Close window");
		item.addActionListener(target);
		theMenu.add(item);

		if (!Debug.isApplet) {
			item = new JMenuItem("Quit");
			item.setActionCommand("quit");
			item.setToolTipText("Exit program");
			item.addActionListener(target);
			theMenu.add(item);
		}

	}

	public void applyLayout(final String s) {
		layoutMenuHandler.apply(s);
	}

	public void createLayoutMenu(final JComponent theMenu, final ActionListener target) {
		final JMenuItem item;

		optimizeItem = new JCheckBoxMenuItem("Optimize", false);
		optimizeItem.setActionCommand("optimizeLayout");
		optimizeItem.setToolTipText("Activate automativ layout optimization");
		optimizeItem.addActionListener(target);
		theMenu.add(optimizeItem);
		addTool(layoutMenuHandler.getLayoptPanel());

		// addLayoutMenuItem(theMenu, target, new JJProteinLayout(panel));
		addLayoutMenuItem(theMenu, target, new JJRandomLayout(panel));
		addLayoutMenuItem(theMenu, target, new JJCircularLayout(panel));
		addLayoutMenuItem(theMenu, target, new JJShake(panel));
		addLayoutMenuItem(theMenu, target, new JJTutte(panel));
		final JJGem gem = new JJGem(panel);
		panel.addTool(new JJGEMWindow(gem, target));
		addLayoutMenuItem(theMenu, target, gem);
		addLayoutMenuItem(theMenu, target, new FRCC(panel));
		addLayoutMenuItem(theMenu, target, new FR(panel));
		// addLayoutMenuItem(theMenu, target, new JJSpring(panel));
		addLayoutMenuItem(theMenu, target, new JJSpring3D(panel));
		addLayoutMenuItem(theMenu, target, new JJSugi(panel));
		// addLayoutMenuItem(theMenu, target, new JJFTree(panel));
		final JJEigenLayout eigen = new JJEigenLayout(panel);
		final JJEigenWindow eigenW = new JJEigenWindow(eigen, target);
		panel.addTool(eigenW);
		panel.getGraph().addStructureListener(eigenW);
		addLayoutMenuItem(theMenu, target, eigen);
		addLayoutMenuItem(theMenu, target, new JJTree(panel));
		final JJOrtho ort = new JJOrtho(panel);
		addTool(new JJOrthoWindow(ort, target));
		addLayoutMenuItem(theMenu, target, ort);
		if (!Debug.isApplet)
			addLayoutMenuItem(theMenu, target, new JJDOT(panel));
		addLayoutMenuItem(theMenu, target, new JJMetromap(panel));

		final java.util.List<String> algos = JJAGD.getAlgorithms();

		if ((algos != null) && (!algos.isEmpty())) {
			final JMenu subMenu = new JMenu("AGD");
			theMenu.add(subMenu);
			final JJAGD agd = new JJAGD(panel);
			for (final String algo : algos) {
				addLayoutMenuItem(subMenu, target, agd, algo, "agd::" + algo);
			}
		}
	}

	public void addLayoutMenuItem(final JComponent theMenu, final ActionListener target, final JJLayout l) {
		JMenuItem item;

		layoutMenuHandler.add(l);

		item = new JMenuItem(l.getName());
		item.setActionCommand(l.getName());
		item.addActionListener(target);
		theMenu.add(item);
	}

	public void addLayoutMenuItem(final JComponent theMenu, final ActionListener target, final JJLayout l,
			final String name, final String command) {
		JMenuItem item;

		layoutMenuHandler.add(l);

		item = new JMenuItem(name);
		item.setActionCommand(command);
		item.addActionListener(target);
		theMenu.add(item);
	}

	public void createClusterMenu(final JMenu theMenu, final ActionListener target) {

		theMenu.addSeparator();

		addClusterMenuItem(theMenu, target, new JJRandCluster(panel.getGraph(), 4, panel));
		addClusterMenuItem(theMenu, target, new JJRandBipart(panel.getGraph(), panel));
		addClusterMenuItem(theMenu, target, new JJEigenBipart(panel.getGraph(), panel));

		theMenu.addSeparator();

		addClusterMenuItem(theMenu, target, new JJMultiwayKL(panel.getGraph(), 4, panel));
		addClusterMenuItem(theMenu, target, new JJSA(panel.getGraph(), 4, panel));

		theMenu.addSeparator();

		JMenuItem item = new JCheckBoxMenuItem("Create Structure Graph", false);
		item.setActionCommand("structureGraph");
		item.addActionListener(target);
		theMenu.add(item);

		item = new JCheckBoxMenuItem("Color graph", false);
		item.setActionCommand("colorGraph");
		item.addActionListener(target);
		theMenu.add(item);
	}

	public void addClusterMenuItem(final JComponent theMenu, final ActionListener target, final JJCluster l) {
		JMenuItem item;

		clusterMenuHandler.add(l);

		item = new JMenuItem(l.getName());
		item.setActionCommand(l.getName());
		item.addActionListener(target);
		theMenu.add(item);
	}

	public void createEdgeMenu(final JComponent theMenu, final ActionListener target) {
		JMenuItem item;

		item = new JMenuItem("Revert");
		item.setActionCommand("revert");
		item.setToolTipText("Revert direction of this edge");
		item.addActionListener(target);
		theMenu.add(item);

		item = new JMenuItem("Remove bends");
		item.setActionCommand("removeBends");
		item.setToolTipText("Remove all bends from this edge");
		item.addActionListener(target);
		theMenu.add(item);

		item = new JMenuItem("Bends to nodes");
		item.setActionCommand("bendsToNodes");
		item.setToolTipText("Replace all bends in this edge by new nodes");
		item.addActionListener(target);
		theMenu.add(item);

		item = new JMenuItem("Set color");
		item.setActionCommand("setEdgeColor");
		item.setToolTipText("Change the colour of this edge");
		item.addActionListener(target);
		theMenu.add(item);

	}

	public void createGraphMenu(final JComponent theMenu, final ActionListener target) {
		JMenuItem item;

		item = new JMenuItem("Show all");
		item.setActionCommand("showAll");
		item.setToolTipText("Make all nodes and edges of the graph visible");
		item.addActionListener(target);
		theMenu.add(item);
	}

	@Override
	public void adjustmentValueChanged(final java.awt.event.AdjustmentEvent e) {
		if (panel != null) {
			panel.repaint();
		}
	}

	public JPanel createStatusLine() {
		final JPanel p = new JPanel();

		statusLabel = new JLabel("Nodes: 0 Edges: 0    Status: ", JLabel.LEFT);
		p.setLayout(new BorderLayout());

		p.add(statusLabel, BorderLayout.WEST);
		return p;
	}

	public Component createComponents() {
		final java.awt.Container pane = getContentPane();

		panel.setPreferredSize(new Dimension(canvasWidth, canvasHeight));
		panel.setSize(new Dimension(canvasWidth, canvasHeight));

		scrollPane = new JScrollPane(panel);
		scrollPane.setPreferredSize(new Dimension(600, 400));
		scrollPane.setSize(new Dimension(600, 400));

		final String s = System.getProperty("java.specification.version");
		final double version = Double.parseDouble(s);
		if (version < 1.3) { // Wrong redraw events generated in versions < jdk
								// 1.3
			scrollPane.getHorizontalScrollBar().addAdjustmentListener(this);
			scrollPane.getVerticalScrollBar().addAdjustmentListener(this);
		}

		final JPanel p = new JPanel();

		zoomScale = new JSlider(2, 500, 100);
		zoomScale.addChangeListener(this);
		zoomScale.setToolTipText("Set view magnification");
		p.add(new JLabel("Zoom:"));

		p.add(zoomScale);

		final JButton zf = new JButton("Fit");
		zf.setActionCommand("zoomToFit");
		zf.setToolTipText("Change view magnification to graph size");
		zf.addActionListener(panel);
		p.add(zf);

		pane.setLayout(new BorderLayout());

		// scrollPane.getViewport().setBackingStoreEnabled(true);

		//
		// Creating the Main Menu
		//

		JMenuBar menuBar;
		JMenu menu;
		final JMenuItem menuItem;

		// Create the menu bar.
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		// Build the first menu.
		menu = new JMenu("File");
		menu.getAccessibleContext().setAccessibleDescription("File");
		menu.setToolTipText("File operations menu");
		menuBar.add(menu);

		createFileMenu(menu, panel);

		createEditMenu(menuBar, panel);
		createOptionsMenu(menuBar, panel);

		menu = new JMenu("Tools");
		menu.getAccessibleContext().setAccessibleDescription("Tools");
		menu.setToolTipText("Misc. Tools menu ");
		menuBar.add(menu);

		createToolsMenu(menu, panel);

		menuBar.add(createClusterMenu());

		menu = new JMenu("Layout");
		menu.getAccessibleContext().setAccessibleDescription("Layout menu");
		menu.setToolTipText("Graph layout algorithms");
		menuBar.add(menu);
		createLayoutMenu(menu, layoutMenuHandler);

		menuBar.add(p);

		menu = new JMenu("Help");
		menu.getAccessibleContext().setAccessibleDescription("Help");
		menu.setToolTipText("Help and diagnostic tools");
		menuBar.add(menu);
		createHelpMenu(menu, panel);

		pane.add(scrollPane, BorderLayout.CENTER);
		// pane.add(p, BorderLayout.SOUTH);

		pane.add(createStatusLine(), BorderLayout.SOUTH);
		return pane;
	}

	public void createHelpMenu(final JMenu theMenu, final ActionListener target) {
		JMenuItem item = new JMenuItem("Help");
		item.setActionCommand("Help");
		item.setToolTipText("Display help window");
		item.addActionListener(this);
		theMenu.add(item);

		item = new JMenuItem("Memory monitor");
		item.setActionCommand("memMon");
		item.setToolTipText("Display memory usage diagnostics");
		item.addActionListener(target);
		theMenu.add(item);

		item = new JMenuItem("Ouput Window");
		item.setActionCommand("outputWindow");
		item.setToolTipText("Display error, warning, and info messages");
		item.addActionListener(target);
		theMenu.add(item);

		// ActionListener histoListener = new ActionListener() {
		// public void actionPerformed(ActionEvent e) {
		// double d[] = {-10, 20, 5, 13, 2, 3, 2, 2,2};
		// new JJHistogramWindow(d);
		// }
		// };

		// item = new JMenuItem("Histogram Window");
		// item.addActionListener(histoListener);
		// theMenu.add(item);
	}

	public void setBusy(final boolean flag) {
		if (flag == true) {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		} else {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}

		getToolkit().sync();
	}

	public void buildHelp() {
		helpPanel = new JJHelp("org/carsten/jjgraph/docs/jjhelp.html");
	}

	public JJGraphFrame(final JJGraphWindowImpl fenster) {
		super("JJGraph: " + fenster.getGraph().getName());
		buildHelp();

		if (fenster.getGraph().getName().equals("") && (fenster.getGraph().getFile() != null))
			try {
				setTitle("JJGraph: " + fenster.getGraph().getFile().getCanonicalPath());
			} catch (final java.io.IOException e) {
			}

		panel = fenster;
	}

	public void initComponents() {
		inspector = new JJInspector(this, panel);

		// inspector.pack();
		// inspector.show();

		// inspector.dispose();

		layoutMenuHandler = new JJLayoutMenuHandler(panel, this);
		clusterMenuHandler = new JJClusterMenuHandler(panel);

		helpPanel.dispose();

		final Component contents = createComponents();
		pack();
		setVisible(true);
		panel.setDropTarget(new DropTarget(panel, this));
		addWindowListener(this);

	}

	public void close() {
		dispose();
		inspector.dispose();
	}

	@Override
	public void windowClosing(final WindowEvent e) {
		// Debug.println("Window closeing");
		if (panel.getGraph().isDirty()) {
			final int i = JOptionPane.showConfirmDialog(null, "Graph was modified. Save now?", "Warning",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (i == JOptionPane.YES_OPTION)
				panel.save();
		}

		close();
	}

	@Override
	public void windowOpened(final WindowEvent e) {
	}

	@Override
	public void windowClosed(final WindowEvent e) {
	}

	@Override
	public void windowActivated(final WindowEvent e) {
	}

	@Override
	public void windowDeactivated(final WindowEvent e) {
	}

	@Override
	public void windowIconified(final WindowEvent e) {
		inspector.dispose();
		helpPanel.dispose();
	}

	@Override
	public void windowDeiconified(final WindowEvent e) {
	}

	@Override
	public void dragExit(final java.awt.dnd.DropTargetEvent e) {
	}

	@Override
	public void drop(final java.awt.dnd.DropTargetDropEvent e) {
		Debug.println("drop");
		final DropTargetContext targetContext = e.getDropTargetContext();

		boolean outcome = false;

		if ((e.getSourceActions() & DnDConstants.ACTION_COPY) != 0)
			e.acceptDrop(DnDConstants.ACTION_COPY);
		else {
			e.rejectDrop();
			return;
		}

		final DataFlavor[] dataFlavors = e.getCurrentDataFlavors();
		DataFlavor transferDataFlavor = null;

		Debug.println(DataFlavor.javaFileListFlavor.getMimeType());

		for (final DataFlavor dataFlavor : dataFlavors) {
			Debug.println(dataFlavor.getMimeType());
			if (DataFlavor.javaFileListFlavor.equals(dataFlavor)) {
				Debug.println("matched");
				transferDataFlavor = dataFlavor;
				break;
			}
		}

		if (transferDataFlavor != null) {
			final Transferable t = e.getTransferable();
			// InputStream is = null;
			java.util.List<File> fl = null;

			try {
				Debug.println("get list");
				fl = (java.util.List<File>) t.getTransferData(transferDataFlavor);
			} catch (final IOException ioe) {
				// ioe.printStackTrace();
				Debug.println(ioe.getMessage());
				targetContext.dropComplete(false);

				return;
			} catch (final UnsupportedFlavorException ufe) {
				// ufe.printStackTrace();
				Debug.println(ufe.getMessage());
				targetContext.dropComplete(false);

				return;
			}

			if (fl != null) {
				outcome = true;
				for (final Iterator<File> iter = fl.listIterator(); iter.hasNext();) {

					final File tmpF = iter.next();
					Debug.println("" + tmpF);

					JJGraph graph = panel.getGraph();
					JJGraphWindow w = panel;

					if (graph.getNumNodes() != 0) {
						try {
							graph = graph.getClass().newInstance();
							if (graph.getWindows().isEmpty())
								w = graph.createGraphic();
						} catch (final InstantiationException e2) {
							Debug.println("Could not create new graph: " + e2);
						} catch (final IllegalAccessException e2) {
							Debug.println("Could not create new graph: " + e2);
						}
					}
					try {
						w.setBusy(true);
						w.setRedraw(false);
						graph.parseFile(tmpF.toString());
					} catch (final java.io.IOException e2) {
						Debug.println("Drag & Drop problem: " + e2);
					} finally {
						w.setBusy(false);
						w.setRedraw(true);
					}

				}
			}
		}

		targetContext.dropComplete(outcome);
	}

	@Override
	public void stateChanged(final ChangeEvent e) {
		if (e.getSource() == zoomScale) {
			final boolean oldV = panel.setRedraw(false);

			final double oldZoom = panel.getZoom();
			double newZoom = zoomScale.getValue();
			newZoom = (newZoom * newZoom) / (100.0 * 100.0);

			if (oldZoom != newZoom) {
				final JViewport vp = scrollPane.getViewport();
				final Rectangle vr = vp.getViewRect();

				vr.x = (int) (((vr.x + vr.width / 2.0) - JJGraphWindowImpl.xOffset) / oldZoom);
				vr.y = (int) (((vr.y + vr.height / 2.0) - JJGraphWindowImpl.yOffset) / oldZoom);

				panel._zoomTo(newZoom);
				centerOn((int) (vr.x * newZoom + JJGraphWindowImpl.xOffset),
						(int) (vr.y * newZoom + JJGraphWindowImpl.yOffset));
				panel.repaint();
			}
			panel.setRedraw(oldV);
		}

	}

	public void zoomTo(final double x) {
		zoomScale.setValue((int) Math.sqrt(x * 100.0 * 100.0));
		// panel._zoomTo(x);
	}

	@Override
	public void dropActionChanged(final java.awt.dnd.DropTargetDragEvent e) {
	}

	@Override
	public void dragOver(final java.awt.dnd.DropTargetDragEvent e) {
	}

	@Override
	public void dragEnter(final java.awt.dnd.DropTargetDragEvent e) {
	}

	public void centerOn(final int x, final int y) {
		if ((x < 0) || (x > canvasWidth) || (y < 0) || (y > canvasHeight)) {
			panel.printError("Invalid scroll coordinates (" + x + "," + y + ")");
			return;
		}

		final JViewport vp = scrollPane.getViewport();
		final Rectangle vr = vp.getViewRect();

		vr.x = x - vr.width / 2;
		vr.y = y - vr.height / 2;

		vp.setViewPosition(new Point(vr.x, vr.y));
	}

	public void addTool(final JJInspectable i) {
		inspector.addTool(i);
	}

	public JJInspectable getTool(final Class<? extends JJInspectable> c) {
		return inspector.getTool(c);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getActionCommand().equals("inspector")) {
			inspector.pack();
			inspector.setVisible(true);
			;
		}
		if (e.getActionCommand().equals("Help")) {
			helpPanel.pack();
			helpPanel.setVisible(true);
		}
	}

	public JMenu createClusterMenu() {
		final JMenu menu = new JMenu("Clustering");
		menu.setToolTipText("Graph clustering algorithms");
		menu.getAccessibleContext().setAccessibleDescription("Clustering");

		createClusterMenu(menu, clusterMenuHandler);
		return menu;
	}

	public void beep() {
		getToolkit().beep();
	}

	public void copyData(final JJGraphFrame f) {
		setShowNodes(f.getShowNodes());
		setShowEdges(f.getShowEdges());
	}

} // JJGraphWindow
