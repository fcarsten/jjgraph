/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * JJGraphWindowImpl.java
 *
 *
 * Created: Thu May 04 09:46:51 2000
 *
 * @author $Author: carsten $
 * @version $Revision: 1.11 $ $Date: 2003/02/09 06:11:40 $
 *
 * $Log: JJGraphWindowImpl.java,v $
 * Revision 1.11  2003/02/09 06:11:40  carsten
 * Added visibility change events and query of drawing area
 *
 * Revision 1.10  2003/02/04 00:22:33  carsten
 * Changed _isVisible to isVisible
 *
 * Revision 1.9  2002/09/20 03:45:46  carsten
 * A JJ3DGraph can now be attached to a JJGraphWindow and update
 * when nodes/edges change. Can't delete as yet and is very slow
 *
 * Revision 1.8  2002/09/06 04:47:12  carsten
 * Added constructor which can supply classes for GraphicNode GraphicEdge
 * and GraphicNodeAppearance
 *
 * Revision 1.7  2002/09/02 04:09:07  carsten
 * repaint after makeVisible added
 *
 * Revision 1.6  2002/08/15 08:32:39  falk
 * *** empty log message ***
 *
 * Revision 1.5  2002/08/14 05:54:09  carsten
 * Minor changes
 *
 * Revision 1.4  2002/08/02 07:32:24  carsten
 * Added setForeground(Color c). Is used when drawing edge labels
 *
 * Revision 1.3  2002/07/25 05:02:42  carsten
 * Added CVS variables
 *
 *
 */

import javax.imageio.ImageWriter;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.carsten.jjgraph.animation.JJGraphAnimationWindow;
import org.carsten.jjgraph.util.Debug;
import org.carsten.jjgraph.util.JJ3DGraph;
import org.carsten.jjgraph.util.JJClock;
import org.carsten.jjgraph.util.JJDelaunay;
import org.carsten.jjgraph.util.JJDfs;
import org.carsten.jjgraph.util.JJDirectorWindow;
import org.carsten.jjgraph.util.JJEdgeProp;
import org.carsten.jjgraph.util.JJFontSelectorBox;
import org.carsten.jjgraph.util.JJHTMLManager;
import org.carsten.jjgraph.util.JJNodeProp;
import org.carsten.jjgraph.util.JJPoint;
import org.carsten.jjgraph.util.JJRectangle;
import org.carsten.jjgraph.util.JJSTNum;
import org.carsten.jjgraph.util.JJSelector;
import org.carsten.jjgraph.util.MMWindow;

public class JJGraphWindowImpl extends JJGraphWindow
		implements MouseListener, MouseMotionListener, Printable, KeyListener, JJInspectable, JJGraphListener {

	protected final static RenderingHints AALIAS = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
	protected final static RenderingHints NO_TEXT_AALIAS = new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING,
			RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
	final static int MIN_X = 50;
	final static int MIN_Y = 50;
	final static double FANG_RADIUS = 5.0;

	protected JJGraphFrame fenster;

	protected JJHTMLManager htmlManager;
	protected Rectangle repaintRect;
	protected double zoom = 1.0;

	protected LinkedList<JJGraphicNode> selectedNodes = new LinkedList<>();
	protected LinkedList<JJGraphicNodeAppearance> boredNodes = new LinkedList<>();

	protected JJGraphicEdge selectedEdge = null;

	protected JJPoint dragPoint;
	protected JJPoint dragStart;
	protected Image dragBackground;
	protected boolean dragSelect = false;
	protected boolean zoomSelect = false;

	protected JJGraph graph;

	protected Image offscreen;

	protected Dimension offscreensize;
	protected Graphics2D offGraphics;
	protected JPopupMenu graphPopup = null;
	protected JPopupMenu nodePopup = null;
	protected JPopupMenu edgePopup = null;

	protected boolean showNodeLabels = true;
	protected boolean drawSplines = false;

	protected boolean showEdgeLabels = true;
	// protected boolean showNodes = true;
	// protected boolean showEdges = true;
	protected boolean shortLabels = true;
	protected boolean outline = false;

	protected Class<? extends JJGraphicNode> graphicNodeClass = JJGraphicNodeImpl.class;
	protected Class<? extends JJGraphicNodeAppearance> graphicNodeAppearanceClass = JJGraphicNodeDefault.class;
	protected Class<? extends JJGraphicEdge> graphicEdgeClass = JJGraphicEdgeImpl.class;

	protected Color nodeColor = new Color(200, 200, 200);
	protected Color edgeColor = new Color(100, 100, 100);

	protected BitSet clearMask;

	protected int gridSize = 0;
	protected javax.swing.Timer boredTimer = new javax.swing.Timer(30 * 1000, this);

	protected int defaultEdgeWeight = 1;

	// public boolean showEdges()
	// {
	// return fenster.getShowEdges();
	// }

	// public boolean showNodes()
	// {
	// return fenster.getShowNodes();
	// }

	/**
	 * Get the value of shortLabels.
	 *
	 * @return value of shortLabels.
	 */
	public boolean isShortLabels() {
		return shortLabels;
	}

	/**
	 * Set the value of shortLabels.
	 *
	 * @param v
	 *            Value to assign to shortLabels.
	 */
	public void setShortLabels(final boolean v) {
		this.shortLabels = v;
	}

	public void setShowEdgeLabels(final boolean b) {
		if (showEdgeLabels != b)
			showEdgeLabels = b;
		if (fenster != null)
			fenster.setShowEdgeLabels(b);

	}

	/**
	 * Get the value of drawSplines.
	 *
	 * @return value of drawSplines.
	 */
	@Override
	public boolean isDrawSplines() {
		return drawSplines;
	}

	/**
	 * Set the value of drawSplines.
	 *
	 * @param v
	 *            Value to assign to drawSplines.
	 */
	public void setDrawSplines(final boolean v) {
		this.drawSplines = v;
	}

	/**
	 * Get the value of defaultEdgeWeight.
	 *
	 * @return Value of defaultEdgeWeight.
	 */
	public int getDefaultEdgeWeight() {
		return defaultEdgeWeight;
	}

	/**
	 * Set the value of defaultEdgeWeight.
	 *
	 * @param v
	 *            Value to assign to defaultEdgeWeight.
	 */
	public void setDefaultEdgeWeight(final int v) {
		this.defaultEdgeWeight = v;
	}

	public int getGridSize() {
		return gridSize;
	}

	public void setGridSize(final int v) {
		this.gridSize = v;
		_repaint();
	}

	public Color getNodeColor() {
		return nodeColor;
	}

	public void setNodeColor(final Color v) {
		this.nodeColor = v;
	}

	public Color getEdgeColor() {
		return edgeColor;
	}

	public void setEdgeColor(final Color v) {
		this.edgeColor = v;
	}

	public Class<? extends JJGraphicNode> getGraphicNodeClass() {
		return graphicNodeClass;
	}

	public void setGraphicNodeClass(final Class<? extends JJGraphicNode> v) {
		this.graphicNodeClass = v;
	}

	public Class<? extends JJGraphicNodeAppearance> getGraphicNodeAppearanceClass() {
		return graphicNodeAppearanceClass;
	}

	public void setGraphicNodeAppearanceClass(final Class<? extends JJGraphicNodeAppearance> v) {
		this.graphicNodeAppearanceClass = v;
	}

	public Class<? extends JJGraphicEdge> getGraphicEdgeClass() {
		return graphicEdgeClass;
	}

	public void setGraphicEdgeClass(final Class<? extends JJGraphicEdge> v) {
		this.graphicEdgeClass = v;
	}

	public boolean getShowNodes() {
		return fenster.getShowNodes();
	}

	public boolean getShowEdges() {
		return fenster.getShowEdges();
	}

	@Override
	public boolean getOutline() {
		return outline;
	}

	protected boolean buildStructureGraph = false;

	// protected HashSet invisibleCluster= null;

	public static final int xOffset = JJGraphFrame.canvasWidth / 2; // 5000;
	public static final int yOffset = JJGraphFrame.canvasHeight / 2; // 5000;

	protected boolean redraw = true;

	protected String statusMsg = "";
	protected JJClock clock;

	protected ResourceBundle sprache = ResourceBundle.getBundle("org.carsten.jjgraph.graph.Messages",
			Locale.getDefault());

	public final static int BARRY_CENTER = 0;
	public final static int BOUNDING_BOX = 1;

	protected int centerMode = BOUNDING_BOX;

	protected JJPreDrawer predrawer;

	@Override
	public int getNodeLabelPosition() {
		if (showNodeLabels)
			return LABEL_INSIDE;
		return LABEL_NONE;
	}

	@Override
	public int getEdgeLabelPosition() {
		if (showEdgeLabels)
			return LABEL_INSIDE;
		return LABEL_NONE;
	}

	/**
	 * Get the value of redrawer.
	 *
	 * @return Value of redrawer.
	 */
	public JJPreDrawer getPredrawer() {
		return predrawer;
	}

	/**
	 * Set the value of redrawer.
	 *
	 * @param v
	 *            Value to assign to redrawer.
	 */
	public void setPredrawer(final JJPreDrawer v) {
		this.predrawer = v;
	}

	public int getCenterMode() {
		return centerMode;
	}

	public void setCenterMode(final int v) {
		if (v != this.centerMode) {
			this.centerMode = v;
		}
	}

	public String getString(final String s) {
		String res;
		try {
			res = sprache.getString(s);
		} catch (final java.util.MissingResourceException e) {
			return "Couldn't find message for selected language";
		}
		return res;
	}

	public void setLanguage(final String s) {
		if (s.equals("deutsch")) {
			sprache = ResourceBundle.getBundle("org.carsten.jjgraph.graph.Messages", new Locale("de", "DE"));
		} else if (s.equals("english")) {
			sprache = ResourceBundle.getBundle("org.carsten.jjgraph.graph.Messages", new Locale("en", "AU"));
		}
		clock.setLanguage(s);
		clock.restart();
	}

	public final static int STATUS_NOTE = 0;
	public final static int STATUS_WARNING = 1;
	public final static int STATUS_ERROR = 2;
	protected Color defColor = new Color(102, 102, 153);
	protected Color redColor = new Color(200, 102, 102);

	@Override
	public void printNote(final String s) {
		setStatusMsg(s, STATUS_NOTE);
	}

	@Override
	public void printWarning(final String s) {
		setStatusMsg(s, STATUS_WARNING);
	}

	@Override
	public void printError(final String s) {
		setStatusMsg(s, STATUS_ERROR);
		Debug.println(s);

	}

	public String getStatusMsg() {
		return statusMsg;
	}

	public void setStatusMsg(final String v, final int type) {
		this.statusMsg = v;
		if (clock != null)
			clock.restart();
		if (fenster != null) {
			if (type > STATUS_WARNING)
				fenster.setStatusColor(redColor);
			else
				fenster.setStatusColor(defColor);
		}

		updateStatus();
	}

	public void setTime(final String s) {
		statusMsg = s;
		fenster.setStatusColor(defColor);
		updateStatus();
	}

	@Override
	public double getZoom() {
		return zoom;
	}

	@Override
	public Collection<JJGraphicNode> getSelectedNodes() {
		return selectedNodes;
	}

	@Override
	public JJGraphicEdge getSelectedEdge() {
		return selectedEdge;
	}

	@Override
	public JJGraph getGraph() {
		return graph;
	}

	@Override
	public boolean getRedraw() {
		return redraw;
	}

	@Override
	public boolean setRedraw(final boolean v) {
		final boolean oldVal = redraw;
		// if(v != redraw)
		// Debug.println("Setting redraw to: " + v);

		if (v == true)
			paintDirty();

		this.redraw = v;
		// if(this.redraw){
		// sendStartRedraw();
		// }
		// else
		// {
		// sendStopRedraw();
		// }

		return oldVal;
	}

	public void showAll() {
		// invisibleCluster.clear();
		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			iter.next().getGraphicNode(this).getVisible().andNot(clearMask);
		}

		for (final Iterator<JJEdge> iter = graph.edgeIterator(); iter.hasNext();) {
			iter.next().getGraphicEdge(this).getVisible().andNot(clearMask);
		}
	}

	@Override
	public JJGraphFrame getFrame() {
		return fenster;
	}

	@Override
	public boolean isFocusTraversable() {
		return isFocusable();
	}

	@Override
	public boolean isFocusable() {
		return true;
	}

	public JJGraphWindowImpl() {
		clearMask = new BitSet();
		clearMask.set(HIDDEN, true);
		setBackground(new Color(220, 220, 250));
		// setBackground(new Color(255,255,255));

	}

	public void copyGraphicData(final JJGraphWindow w) {
		if ((w == null) || (w.getGraph() != getGraph()))
			return;

		setBackground(w.getBackground());
		setForeground(w.getForeground());

		fenster.copyData(w.getFrame());

		zoomTo(w.getZoom());
		setTitle(w.getTitle());

		for (final Iterator<JJNode> nodeIter = graph.nodeIterator(); nodeIter.hasNext();) {
			final JJNode tmpN = nodeIter.next();
			final JJGraphicNode gNew = tmpN.getGraphicNode(this);
			final JJGraphicNode gOld = tmpN.getGraphicNode(w);
			gNew.setColor(gOld.getColor());
			// gNew.setWidth(gOld.getWidth());
			// gNew.setHeight(gOld.getHeight());
			moveNodeTo(gNew, gOld.getCoords());
			gNew.getVisible().or(gOld.getVisible());

		}

		for (final Iterator<JJEdge> edgeIter = graph.edgeIterator(); edgeIter.hasNext();) {
			final JJEdge tmpE = edgeIter.next();
			final JJGraphicEdge gNew = tmpE.getGraphicEdge(this);
			final JJGraphicEdge gOld = tmpE.getGraphicEdge(w);

			gNew.setColor(gOld.getColor());
			if (gOld.getBends() != null) {
				final java.util.List<JJPoint> bends = new ArrayList<>();
				bends.addAll(gOld.getBends());
				gNew.setBends(bends);
			}
			gNew.getVisible().or(gOld.getVisible());

		}
	}

	public JJGraphWindowImpl(final JJGraph g, final JJGraphWindow w) {
		this(g);
		if (w != null)
			copyGraphicData(w);
	}

	public JJGraphWindowImpl(final JJGraph g, final Class<? extends JJGraphicNode> gn,
			final Class<? extends JJGraphicEdge> ge, final Class<? extends JJGraphicNodeAppearance> na) {
		graphicNodeClass = gn;
		graphicNodeAppearanceClass = na;
		graphicEdgeClass = ge;
		init(g);
	}

	public JJGraphWindowImpl(final JJGraph g) {
		init(g);
	}

	protected void init(final JJGraph g) {
		setBackground(new Color(220, 220, 250));
		// setBackground(new Color(255,255,255));
		clearMask = new BitSet();
		clearMask.set(HIDDEN, true);

		graph = g;
		final Font f = getFont();
		setFont(new Font("SansSerif", f.getStyle(), f.getSize()));

		// invisibleCluster = new HashSet();

		fenster = new JJGraphFrame(this);
		fenster.initComponents();

		if (graph.getFile() != null) {
			fenster.enableSaveReload();
		}

		final MouseMotionListener doScrollRectToVisible = new MouseMotionAdapter() {
			@Override
			public void mouseDragged(final MouseEvent e) {
				final Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
				((JPanel) e.getSource()).scrollRectToVisible(r);
			}
		};
		addMouseMotionListener(doScrollRectToVisible);

		setDoubleBuffered(false);

		initPopupMenu();
		setAutoscrolls(true);

		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);

		for (final Iterator<JJNode> nodeIter = graph.nodeIterator(); nodeIter.hasNext();) {
			final JJNode tmpN = nodeIter.next();
			addNode(tmpN);
		}

		for (final Iterator<JJEdge> edgeIter = graph.edgeIterator(); edgeIter.hasNext();) {
			final JJEdge tmpE = edgeIter.next();
			addEdge(tmpE);
		}

		fenster.centerOn(xOffset, yOffset);
		fenster.addTool(new JJNodeProp(this));
		fenster.addTool(new JJEdgeProp(this));
		fenster.addTool(new JJGraphAnimationWindow(this));
		fenster.addTool(new JJSelector(this));
		fenster.addTool(new JJDirectorWindow(this));

		htmlManager = new JJHTMLManager(this); // null;
		clock = new JJClock(this, "english");

		g.addStructureListener(this);
		boredTimer.start();
	}

	@Override
	public void initPopupMenu() {
		graphPopup = new JPopupMenu();
		nodePopup = new JPopupMenu();
		edgePopup = new JPopupMenu();

		graphPopup.addSeparator();
		fenster.createGraphMenu(graphPopup, this);
		graphPopup.addSeparator();

		nodePopup.addSeparator();
		createNodeMenu();
		nodePopup.addSeparator();

		edgePopup.addSeparator();
		fenster.createEdgeMenu(edgePopup, this);
		edgePopup.addSeparator();

	}

	public void createNodeMenu() {
		JMenuItem item;

		item = new JMenuItem("Hide");
		item.setActionCommand("HideNode");
		item.addActionListener(this);
		nodePopup.add(item);

		item = new JMenuItem("Set color");
		item.setActionCommand("setColor");
		item.addActionListener(this);
		nodePopup.add(item);

		item = new JMenuItem("Move to zero");
		item.setActionCommand("moveToZero");
		item.addActionListener(this);
		nodePopup.add(item);

		item = new JMenuItem("Expand URL");
		item.setActionCommand("Expand");
		item.addActionListener(this);
		nodePopup.add(item);
	}

	// public void showOnly(int cluster)
	// {
	// for(Iterator iter = graph.nodeIterator(); iter.hasNext (); ) {
	// JJNode tmpN = (JJNode) iter.next ();
	// if(tmpN.getCluster() != cluster)
	// invisibleCluster.add(new Integer(tmpN.getCluster()));
	// }
	// updateVisibility();
	// }

	@Override
	public void actionPerformed(final ActionEvent e) {
		final boolean tmpRedraw = setRedraw(false);
		setBusy(true);

		boredTimer.restart();

		if ((selectedEdge != null) && (e.getActionCommand() != null)) {
			if (e.getActionCommand().equals("revert")) {
				selectedEdge.getEdge().revert();
			} else if (e.getActionCommand().equals("removeBends")) {
				selectedEdge.removeBends();
			} else if (e.getActionCommand().equals("bendsToNodes")) {
				selectedEdge.bendsToNodes();
			} else if (e.getActionCommand().equals("setEdgeColor")) {
				final Color tmpC = JColorChooser.showDialog(this, "Choose Edge Color", null);
				if (tmpC != null)
					selectedEdge.setColor(tmpC);
			}
		}

		if ((e.getActionCommand() != null) && !selectedNodes.isEmpty()) {
			Color tmpC = null;

			if (e.getActionCommand().equals("setColor")) {
				tmpC = JColorChooser.showDialog(this, "Choose Node Color", null);
			}

			for (final Iterator<JJGraphicNode> iter = selectedNodes.listIterator(); iter.hasNext();) {
				final JJGraphicNode pick = iter.next();
				// if (e.getActionCommand().equals("ShowOnly")) {
				// showOnly(pick.getNode().getCluster());
				// }
				// else if (e.getActionCommand().equals("Hide")) {
				// hideCluster(pick.getNode().getCluster());
				// }
				// else if (e.getActionCommand().equals("Show")) {
				// showCluster(pick.getNode().getCluster());
				// }
				if (e.getActionCommand().equals("HideNode")) {
					pick.hide();
				} else if (e.getActionCommand().equals("setColor") && (tmpC != null)) {
					pick.setColor(tmpC);
				} else if (e.getActionCommand().equals("moveToZero")) {
					moveNodeTo(pick, 0, 0);
				}

				// else if (e.getActionCommand().equals("ShowOtherWindow") &&
				// (otherWindow != null)) {
				// otherWindow.showCluster(pick.getNode().getCluster());
				// }
				// else if (e.getActionCommand().equals("HideOtherWindow") &&
				// (otherWindow != null)) {
				// otherWindow.hideCluster(pick.getNode().getCluster());
				// }
				else if (e.getActionCommand().equals("Expand")) {
					if (htmlManager != null) {
						getGraph().getUndoManager().openSubtask("Expand URL");
						htmlManager.expandNode(pick.getNode());
						getGraph().getUndoManager().closeSubtask("Expand URL");
					}
				}
			}
		}

		if (e.getSource() == boredTimer) {
			if (!boredNodes.isEmpty()) {
				final int i = (int) (Math.random() * boredNodes.size());
				final JJGraphicNodeImageP gnip = (JJGraphicNodeImageP) boredNodes.get(i);
				gnip.setBored();
			}
		}
		// else if (e.getActionCommand().equals("ShowAllCluster")) {
		// showAllClusters();
		// _repaint();
		// }
		else if (e.getActionCommand().equals("showAll")) {
			showAll();
			_repaint();
		} else if (e.getActionCommand().equals("ShowNodeLabels")) {
			showNodeLabels = ((JCheckBoxMenuItem) e.getSource()).getState();
			_repaint();
		} else if (e.getActionCommand().equals("ShowEdgeLabels")) {
			showEdgeLabels = ((JCheckBoxMenuItem) e.getSource()).getState();
			_repaint();
		} else if (e.getActionCommand().equals("drawSplines")) {
			drawSplines = ((JCheckBoxMenuItem) e.getSource()).getState();
			_repaint();
		} else if (e.getActionCommand().equals("shortLabels")) {
			shortLabels = ((JCheckBoxMenuItem) e.getSource()).getState();
			recomputeNodeWidth();
			recomputeEdgeWidth();
			_repaint();
		} else if (e.getActionCommand().equals("outline")) {
			outline = ((JCheckBoxMenuItem) e.getSource()).getState();
			_repaint();
		} else if (e.getActionCommand().equals("grid")) {
			final boolean grid = ((JCheckBoxMenuItem) e.getSource()).getState();
			if (grid)
				setGridSize(20);
			else
				setGridSize(0);
		} else if (e.getActionCommand().equals("font")) {
			selectFont();
		} else if (e.getActionCommand().equals("ShowEdges")) {
			// showEdges = ((JCheckBoxMenuItem)e.getSource()).getState();
			_repaint();
		} else if (e.getActionCommand().equals("ShowDirection")) {
			final boolean direction = ((JCheckBoxMenuItem) e.getSource()).getState();
			graph.setDirected(direction);
			_repaint();
		} else if (e.getActionCommand().equals("paste")) {
			handlePaste();
		} else if (e.getActionCommand().equals("copy")) {
			handleCopy();
		} else if (e.getActionCommand().equals("cut")) {
			handleCut();
		} else if (e.getActionCommand().equals("ShowNodes")) {
			// showNodes = ((JCheckBoxMenuItem)e.getSource()).getState();
			_repaint();
		} else if (e.getActionCommand().equals("makeVisible")) {
			for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();)
				iter.next().getGraphicNode(this).getVisible().clear();
			_repaint();
		} else if (e.getActionCommand().equals("print")) {
			print();
		} else if (e.getActionCommand().equals("quit")) {
			final int i = JOptionPane.showConfirmDialog(null, "Really quit?", "Warning", JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if (i == JOptionPane.YES_OPTION)
				System.exit(0);
		} else if (e.getActionCommand().equals("close")) {
			if (getGraph().isDirty() && (!Debug.isApplet)) {
				final int i = JOptionPane.showConfirmDialog(null, "Graph has not been saved. Save now?", "Warning",
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
				if (i == JOptionPane.YES_OPTION)
					save();
				else if (i == JOptionPane.CANCEL_OPTION)
					return;
			}

			fenster.close(); // System.exit(0);
		} else if (e.getActionCommand().equals("new")) {
			final JJGraph tmpG = new JJGraphImpl();
			tmpG.createGraphic();
		} else if (e.getActionCommand().equals("newView")) {
			getGraph().createGraphic(this);
		} else if (e.getActionCommand().equals("load")) {
			load();
		} else if (e.getActionCommand().equals("loadLayout")) {
			loadLayout();
		} else if (e.getActionCommand().equals("reload")) {
			reload();
		} else if (e.getActionCommand().equals("save")) {
			save();
		} else if (e.getActionCommand().equals("saveas")) {
			saveAs();
		} else if (e.getActionCommand().equals("diameter")) {
			printNote("Diameter: " + graph.diameter());
		} else if (e.getActionCommand().equals("clear")) {
			graph.clear();
		} else if (e.getActionCommand().equals("clearEdges")) {
			clearEdges();
		} else if (e.getActionCommand().equals("view3D")) {
			show3D();
		} else if (e.getActionCommand().equals("memMon")) {
			Debug.println("MStarting memory monitor");
			new MMWindow();
		} else if (e.getActionCommand().equals("outputWindow")) {
			Debug.showWindow();
		} else if (Debug.DEBUG && e.getActionCommand().equals("bbox")) {
			Debug.println("BBox" + boundingBox());
			printNote("BBox" + boundingBox());
		} else if (e.getActionCommand().equals("dfs")) {
			final JJDfs dfs = new JJDfs(graph, this);
			dfs.doDfs();
		} else if (e.getActionCommand().equals("removeCycles")) {
			final JJDfs dfs = new JJDfs(graph);
			dfs.doDfs();
			dfs.removeCycles();
		} else if (e.getActionCommand().equals("stnum")) {
			JJSTNum.compSTNum(graph, null, null);
		} else if (e.getActionCommand().equals("findGraph")) {
			findGraph();
		} else if (e.getActionCommand().equals("removeParallelEdges")) {
			graph.removeParallelEdges();
		} else if (e.getActionCommand().equals("removeReflexiveEdges")) {
			graph.removeReflexiveEdges();
		} else if (e.getActionCommand().equals("rotate90")) {
			rotateGraph(90);
		} else if (e.getActionCommand().equals("rotate180")) {
			rotateGraph(180);
		} else if (e.getActionCommand().equals("rotate270")) {
			rotateGraph(270);
		} else if (e.getActionCommand().equals("rotateOther")) {
			final String result = JOptionPane.showInputDialog(null, "Enter angle", "Rotate",
					JOptionPane.QUESTION_MESSAGE);
			int r = 0;

			if (result != null) {
				try {
					r = Integer.parseInt(result);
				} catch (final NumberFormatException ex) {
					printError("Input a valid number");
					return;
				}
			} else
				return;

			if (r != 0)
				rotateGraph(r);
		} else if (e.getActionCommand().equals("scale50")) {
			scaleGraph(0.50);
		} else if (e.getActionCommand().equals("scale75")) {
			scaleGraph(0.75);
		} else if (e.getActionCommand().equals("scale150")) {
			scaleGraph(1.50);
		} else if (e.getActionCommand().equals("scale200")) {
			scaleGraph(2.00);
		} else if (e.getActionCommand().equals("scale300")) {
			scaleGraph(3.00);
		} else if (e.getActionCommand().equals("scale400")) {
			scaleGraph(4.00);
		} else if (e.getActionCommand().equals("scaleOther")) {
			final String result = JOptionPane.showInputDialog(null, "Enter factor in %", "Scale",
					JOptionPane.QUESTION_MESSAGE);
			int r = 0;

			if (result != null) {
				try {
					r = Integer.parseInt(result);
				} catch (final NumberFormatException ex) {
					printError("Input a valid number");
					return;
				}
			} else
				return;

			if (r != 0)
				scaleGraph(r / 100.0);
			// scaleGraph();
		} else if (e.getActionCommand().equals("skew")) {
			final String result = JOptionPane.showInputDialog(null, "Enter skew angle", "Skew",
					JOptionPane.QUESTION_MESSAGE);
			int r = 0;

			if (result != null) {
				try {
					r = Integer.parseInt(result);
				} catch (final NumberFormatException ex) {
					printError("Input a valid number");
					return;
				}
			} else
				return;

			if (r != 0)
				skewGraph(r);
			// scaleGraph();
		} else if (e.getActionCommand().equals("zoomToFit")) {
			zoomToFit();
		} else if (e.getActionCommand().equals("flipX")) {
			flipX();
		} else if (e.getActionCommand().equals("flipY")) {
			flipY();
		} else if (e.getActionCommand().equals("centerGraph")) {
			centerGraph();
		} else if (e.getActionCommand().equals("delaunay")) {
			computeDelaunay();
		} else if (e.getActionCommand().equals("removeLinkNodes")) {
			removeLinkNodes();
		} else if (e.getActionCommand().equals("compactEdges")) {
			compactEdges();
		} else if (e.getActionCommand().equals("saveToImage")) {
			saveImage();
		} else if (e.getActionCommand().equals("saveToPovray")) {
			// Debug.println("Saving povray format");
			savePovray();
		} else if (e.getActionCommand().equals("removeBends")) {
			removeBends();
		} else if (e.getActionCommand().equals("allBendsToNodes")) {
			bendsToNodes();
		} else if (e.getActionCommand().equals("revertEdges")) {
			revertEdges();
		}

		setRedraw(tmpRedraw);
		setBusy(false);
	}

	public void revertEdges() {
		for (final Iterator<JJEdge> iter = graph.edgeIterator(); iter.hasNext();) {
			final JJEdge e = iter.next();
			e.revert();
		}
		_repaint();

	}

	class EdgeComparator implements Comparator<JJEdge> {
		boolean directed;

		public EdgeComparator(final boolean d) {
			directed = d;
		}

		@Override
		public int compare(final JJEdge o1, final JJEdge o2) {
			if ((o1 instanceof JJEdge) && (o2 instanceof JJEdge)) {
				JJEdge e1, e2;

				e1 = o1;
				e2 = o2;
				JJNode s1, s2, t1, t2;

				s1 = e1.getSource();
				s2 = e2.getSource();
				t1 = e1.getTarget();
				t2 = e2.getTarget();
				if (!directed) {
					if (s1.getSerialNumber() > t1.getSerialNumber()) {
						final JJNode tmpN = s1;
						s1 = t1;
						t1 = tmpN;
					}
					if (s2.getSerialNumber() > t2.getSerialNumber()) {
						final JJNode tmpN = s2;
						s2 = t2;
						t2 = tmpN;
					}
				}
				if (s1.getSerialNumber() < s2.getSerialNumber())
					return -1;
				else if (s1.getSerialNumber() > s2.getSerialNumber())
					return 1;
				else if (t1.getSerialNumber() < t2.getSerialNumber())
					return -1;
				else if (t1.getSerialNumber() > t2.getSerialNumber())
					return 1;

			}
			return 0;
		}
	}

	public void compactEdges() {
		Debug.println("Compacting edges");

		final java.util.List<JJEdge> edges = new ArrayList<>();
		edges.addAll(graph.getEdges());
		final EdgeComparator comp = new EdgeComparator(graph.isDirected());
		Collections.sort(edges, comp);
		int k = 0;
		JJEdge pointer = edges.get(k++);
		while (k < edges.size()) {
			final JJEdge o = edges.get(k++);
			if (comp.compare(pointer, o) == 0) {
				pointer.setName(pointer.getName() + "," + o.getName());
				graph.deleteEdge(o);
			} else
				pointer = o;
		}
	}

	public void removeLinkNodes() {
		final JJNode c[] = new JJNode[2];
		final java.util.List<JJNode> linkNodes = new LinkedList<>();

		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			final JJNode tmpN = iter.next();
			if (tmpN.deg() == 2) {
				linkNodes.add(tmpN);
			}
		}

		while (!linkNodes.isEmpty()) {
			final JJNode tmpN = linkNodes.get(0);

			if (tmpN.indeg() == 2) {
				tmpN.inEdge(1).revert();
			} else if (tmpN.outdeg() == 2) {
				tmpN.outEdge(1).revert();
			}

			final java.util.List<JJPoint> newBends = new LinkedList<>();
			JJGraphicEdge ge = tmpN.firstInEdge().getGraphicEdge(this);

			if (ge != null) {
				final java.util.List<JJPoint> bends = ge.getBends();
				if (bends != null) {
					newBends.addAll(bends);
					newBends.remove(newBends.size() - 1);
				}
			}

			ge = tmpN.firstOutEdge().getGraphicEdge(this);
			final java.util.List<JJPoint> bends = ge.getBends();
			if (bends != null) {
				newBends.addAll(bends);
			}

			final JJEdge tmpE = graph.addEdge(tmpN.firstInEdge().getSource(), tmpN.firstOutEdge().getTarget());

			ge = tmpE.getGraphicEdge(this);

			if (newBends.size() > 0)
				ge.setBends(newBends);

			graph.deleteNode(tmpN);
			linkNodes.remove(0);
		}
	}

	public void computeDelaunay() {
		if (graph.getNumNodes() == 0)
			return;

		final boolean tmpRedraw = setRedraw(false);
		getGraph().getUndoManager().openSubtask("Delaunay Triangulation");

		new JJDelaunay(this);

		getGraph().getUndoManager().closeSubtask("Delaunay Triangulation");
		setRedraw(tmpRedraw);
	}

	public void handlePaste() {
		final Clipboard c = fenster.getToolkit().getSystemClipboard();
		final Transferable t = c.getContents(null);
		if (t == null) {
			printError("Empty clipboard");
			Debug.println("Empty clipboard");
			return;
		}
		final DataFlavor[] df = t.getTransferDataFlavors();
		for (final DataFlavor element : df) {
			// Debug.println(df[i].toString());
			if (DataFlavor.getTextPlainUnicodeFlavor().isMimeTypeEqual(element)) {
				// if(df[i].getRepresentationClass() ==
				// java.io.InputStream.class){
				try {
					final java.io.Reader s = element.getReaderForText(t);
					// = (java.io.Reader) t.getTransferData(df[i]);
					graph.paste(s);
					return;
				} catch (final java.awt.datatransfer.UnsupportedFlavorException e) {
					Debug.println(e.toString());
				} catch (final java.io.IOException e) {
					Debug.println(e.toString());
				}
			}
		}
		printError("Incompatible Data format");
	}

	public void handleCopy() {
		String copyString;
		final Clipboard c = fenster.getToolkit().getSystemClipboard();
		Collection<JJNode> copyNodeSet = new HashSet<>();
		for (final JJGraphicNode element : getSelectedNodes()) {
			copyNodeSet.add(element.getNode());
		}

		if (copyNodeSet.size() == 0)
			copyNodeSet = graph.getNodes();

		try {
			copyString = graph.saveGraphEdToString(this, copyNodeSet);
		} catch (final java.io.IOException e) {
			printError(e.getMessage());
			Debug.println(e.toString());
			return;
		}
		final StringSelection s = new StringSelection(copyString);

		c.setContents(s, s);
	}

	public void handleCut() {
		handleCopy();
		deleteSelectedNodes();
	}

	public void deleteSelectedNodes() {
		final boolean tmpRedraw = setRedraw(false);
		while (!selectedNodes.isEmpty()) {
			final JJGraphicNode pick = selectedNodes.getFirst();
			graph.deleteNode(pick.getNode());
		}
		setRedraw(tmpRedraw);
	}

	public void zoomToFit() {
		if (graph.getNumNodes() < 2) {
			printError("Need at least two nodes to fit");
			fenster.beep();
			return;
		}

		final boolean tmpR = setRedraw(false);
		final JJRectangle bbox = new JJRectangle(boundingBox());

		final JJPoint center = new JJPoint(bbox.x + bbox.width / 2, bbox.y + bbox.height / 2);

		double scale = 1;

		if ((bbox.width > 0) && (bbox.height > 0)) {
			final JJRectangle bounds = new JJRectangle(getVisibleRect());
			scale = Math.min((bounds.getWidth()) / bbox.getWidth(), (bounds.getHeight()) / bbox.getHeight());

			zoomTo(zoom * scale);
		}

		fenster.centerOn((int) ((center.getX() - xOffset) * scale + xOffset),
				(int) ((center.getY() - yOffset) * scale + yOffset));
		setRedraw(tmpR);
	}

	public void centerGraph() {
		if (graph.getNumNodes() < 1)
			return;

		getGraph().getUndoManager().openSubtask("Center Graph");
		final boolean tmpRedraw = setRedraw(false);
		// JJRectangle bbox = new JJRectangle(boundingBox());

		// JJPoint center = new JJPoint(bbox.x + bbox.width/2,
		// bbox.y + bbox.height/2);
		final JJPoint center = getGraphCenter();

		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			final JJNode tmpN = iter.next();
			rmoveNodeTo(tmpN.getGraphicNode(this), -center.x, -center.y);
		}

		for (final Iterator<JJEdge> iter = graph.edgeIterator(); iter.hasNext();) {
			final JJEdge tmpN = iter.next();
			rmoveEdgeTo(tmpN.getGraphicEdge(this), -center.x, -center.y);
		}

		fenster.centerOn(xOffset, yOffset);
		setRedraw(tmpRedraw);
		getGraph().getUndoManager().closeSubtask("Center Graph");
	}

	public void saveImage() {
		final FileDialog fd = new FileDialog(fenster, "Save image");
		fd.setMode(FileDialog.SAVE);
		fd.setVisible(true);
		if (fd.getFile() != null) {
			saveImage(fd.getDirectory() + fd.getFile());
		}
	}

	public void savePovray() {
		final FileDialog fd = new FileDialog(fenster, "Save as povray");
		fd.setMode(FileDialog.SAVE);
		fd.setVisible(true);
		if (fd.getFile() != null) {
			savePovray(fd.getFile());
		}
	}

	public void savePovray(final String fd) {
		try {
			final OutputStream tmpStream = new FileOutputStream(fd);
			final OutputStreamWriter ofDatei = new OutputStreamWriter(tmpStream);

			writePovHeader(ofDatei);
			writePovGraph(ofDatei);

			ofDatei.close();
		} catch (final IOException e) {
			printError("Couldnt save povray file: " + e.getMessage());
		}
	}

	protected void writePovHeader(final OutputStreamWriter ofDatei) throws IOException {
		ofDatei.write("#include \"colors.inc\" \n");
		ofDatei.write("#include \"chars.inc\" \n");
		ofDatei.write("#include \"textures.inc\" \n");
		ofDatei.write("#declare Gold_Nugget1 = \n");
		ofDatei.write("texture {\n");
		ofDatei.write("    pigment { color rgb <0.5, 0.35, 0.25> }\n");
		ofDatei.write("    finish {\n");
		ofDatei.write("        ambient 0.1\n");
		ofDatei.write("        diffuse 0.65\n");
		ofDatei.write("        specular 0.85\n");
		ofDatei.write("        roughness 0.01\n");
		ofDatei.write("        brilliance 1.5\n");
		ofDatei.write("    }\n");
		ofDatei.write("}\n");
		ofDatei.write("#declare Soft_Silver1 = \n");
		ofDatei.write("texture {\n");
		ofDatei.write("    pigment { color rgb <0,0,0> }\n");
		ofDatei.write("/*    pigment { color rgb <0.55, 0.5, 0.45> }\n");
		ofDatei.write("    finish {\n");
		ofDatei.write("        ambient 0.1\n");
		ofDatei.write("        diffuse 0.65\n");
		ofDatei.write("        specular 0.85\n");
		ofDatei.write("        roughness 0.01\n");
		ofDatei.write("        brilliance 1.5\n");
		ofDatei.write("    }*/\n");
		ofDatei.write("}\n");
		ofDatei.write("fog {\n");
		ofDatei.write("         color rgb <0.7, 0.8, 1>\n");
		ofDatei.write("         distance 10000\n");
		ofDatei.write(" }\n");
		ofDatei.write("background {color Black } \n");
		ofDatei.write("camera {\n");
		ofDatei.write("    location <0, 100, -1500>\n");
		ofDatei.write("    direction z\n");
		ofDatei.write("	look_at <-0, -0, -80>\n");
		ofDatei.write("    rotate <0, 180+clock*360, 0>\n");
		ofDatei.write("  }\n");
		ofDatei.write("\n");
		ofDatei.write("light_source {<1000,1000,-4000> color rgb <5, 5, 5> rotate <0, 180+clock*360, 0> } \n");
		ofDatei.write("\n");
		ofDatei.write("plane {y, -1000 texture { White_Marble scale 100 } } \n");
		ofDatei.write("\n");
	}

	protected void writePovGraph(final OutputStreamWriter ofDatei) throws IOException {
		float xOff = 0f;
		float yOff = 0f;
		float zOff = 0f;
		final float numNodes = graph.getNumNodes();

		// The barry center of the graph should be ay 0,0,0

		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			final JJNode tmpN = iter.next();
			final JJGraphicNode gn = tmpN.getGraphicNode(this);

			xOff -= ((float) gn.getX()) / numNodes;
			yOff -= ((float) gn.getY()) / numNodes;
			zOff -= ((float) gn.getZ()) / numNodes;
		}

		// Add all nodes

		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			final JJNode tmpN = iter.next();
			final JJGraphicNode gn = tmpN.getGraphicNode(this);

			ofDatei.write("sphere {<");
			ofDatei.write(" " + (int) (gn.getX() + xOff));
			ofDatei.write("," + (int) (gn.getY() + yOff));
			ofDatei.write("," + (int) (gn.getZ() + zOff) + "> 10 texture { Gold_Nugget1  finish { phong 0.9 } } }\n");
		}

		// add all edges

		for (final Iterator<JJEdge> iter = graph.edgeIterator(); iter.hasNext();) {

			final JJEdge tmpE = iter.next();
			final JJGraphicEdge ge = tmpE.getGraphicEdge(this);
			final JJGraphicNode gs = tmpE.getSource().getGraphicNode(this);
			final JJGraphicNode gt = tmpE.getTarget().getGraphicNode(this);

			final double sx = gs.getX() + xOff;
			final double sy = gs.getY() + yOff;
			final double sz = gs.getZ() + zOff;

			final double tx = gt.getX() + xOff;
			final double ty = gt.getY() + yOff;
			final double tz = gt.getZ() + zOff;

			if (((int) sx != (int) tx) || ((int) sy != (int) ty) || ((int) sz != (int) tz)) {
				ofDatei.write("cylinder {<");
				ofDatei.write("" + (int) sx + "," + (int) sy + "," + (int) sz + ">, <");
				ofDatei.write(
						"" + (int) tx + "," + (int) ty + "," + (int) tz + ">, 3 texture { pigment { color rgbt <");

				ofDatei.write("" + ge.getColor().getRed() / 255.0f + ",");
				ofDatei.write("" + ge.getColor().getGreen() / 255.0f + ",");
				ofDatei.write("" + ge.getColor().getBlue() / 255.0f + ",");
				ofDatei.write("" + ge.getColor().getAlpha() / 255.0f + "> }");
				// ofDatei.write("" + (1.0f-(float)tmpE.getTransparency()) + ">
				// }");
				ofDatei.write("finish { phong 0.9 } } }\n");

				if (graph.getDirected()) {
					final double dx = tx - sx;
					final double dy = ty - sy;
					final double dz = tz - sz;

					final double x1 = 10.0 / Math.sqrt(dx * dx + dy * dy + dz * dz);
					final double x2 = 20.0 / Math.sqrt(dx * dx + dy * dy + dz * dz);

					// double ax = x*dx;
					// double ay = x*dy;
					// double az = x*dz;

					ofDatei.write("cone {<");
					ofDatei.write("" + (int) (tx - x2 * dx) + "," + (int) (ty - x2 * dy) + "," + (int) (tz - x2 * dz)
							+ ">, 5 <");

					ofDatei.write("" + (int) (tx - x1 * dx) + "," + (int) (ty - x1 * dy) + "," + (int) (tz - x1 * dz)
							+ ">, 0 texture { pigment { color rgbt <");

					ofDatei.write("" + ge.getColor().getRed() / 255.0f + ",");
					ofDatei.write("" + ge.getColor().getGreen() / 255.0f + ",");
					ofDatei.write("" + ge.getColor().getBlue() / 255.0f + ",");
					ofDatei.write("" + ge.getColor().getAlpha() / 255.0f + "> }");
					// ofDatei.write("" + (1.0f-(float)tmpE.getTransparency()) +
					// "> }");
					ofDatei.write("finish { phong 0.7 } } }\n");
				}
			}
		}
	}

	@Override
	public void saveImage(final String fileName) {
		OutputStream os = null;

		try {
			os = new FileOutputStream(fileName);
			// if(fileName.endsWith(".tiff") || fileName.endsWith(".tif"))
			// saveImage(ImageCodec.createImageEncoder("tiff", os, null));
			// else if(fileName.endsWith(".jpeg") || fileName.endsWith(".jpg"))
			// saveImage(ImageCodec.createImageEncoder("jpeg", os, null));
			// else if(fileName.endsWith(".bmp"))
			// saveImage(ImageCodec.createImageEncoder("bmp", os, null));
			// else if(fileName.endsWith(".png"))
			// saveImage(ImageCodec.createImageEncoder("png", os, null));
			// else if(fileName.endsWith(".ppm")|| fileName.endsWith(".pnm"))
			// saveImage(ImageCodec.createImageEncoder("pnm", os, null));
			// else
			{
				final String error = "Unknow image format\nchoose .tiff, .jpeg, .bmp, .png, or .ppm";

				JOptionPane.showMessageDialog(null, error);
			}

		} catch (final java.io.IOException e) {
			printError("Couldn save file " + e.getMessage());
		} finally {
			try {
				if (os != null) {
					os.flush();
					os.close();
				}
			} catch (final java.io.IOException e) {
			}
		}
	}

	public void saveImage(final ImageWriter tie) throws IOException {

		final Rectangle rect = getVisibleRect();
		rect.width += rect.width % 8;
		rect.height += rect.height % 8;

		final BufferedImage bi = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_4BYTE_ABGR);

		final Graphics2D tmpG = bi.createGraphics();
		tmpG.addRenderingHints(AALIAS);
		tmpG.addRenderingHints(NO_TEXT_AALIAS);
		tmpG.setFont(getFont());

		tmpG.translate(-rect.x, -rect.y);
		tmpG.setColor(getBackground()); // new Color(250, 250, 250));
		tmpG.fillRect(rect.x, rect.y, rect.width, rect.height);

		if (predrawer != null) {
			predrawer.draw(tmpG);
		}

		doPaint(tmpG);

		tmpG.setColor(Color.black);
		tmpG.drawRect(rect.x, rect.y, rect.width - 1, rect.height - 1);

		final BufferedImage bi2 = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_3BYTE_BGR);

		final Graphics2D tmpG2 = bi2.createGraphics();
		final MediaTracker trackie = new MediaTracker(this);
		trackie.addImage(bi2, 0);

		tmpG2.drawImage(bi, null, null);
		try {
			trackie.waitForID(0);
		} catch (final InterruptedException oopsie) {
			Debug.println("Error waiting for draw");
		}

		tie.write(bi2);
	}

	@Override
	public void findGraph() {
		if (graph.getNumNodes() == 0)
			return;

		JJGraphicNode center = null;

		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			final JJNode tmpN = iter.next();
			final JJGraphicNode gn = tmpN.getGraphicNode(this);

			if (gn.isVisible()) {
				center = gn;
				break;
			}
		}

		if (center == null)
			return;

		fenster.centerOn((int) (center.getX() * zoom + xOffset), (int) (center.getY() * zoom + yOffset));
	}

	protected void print() {
		final PrinterJob printJob = PrinterJob.getPrinterJob();

		printJob.setPrintable(this);
		if (printJob.printDialog()) {
			try {
				printJob.print();
			} catch (final java.awt.print.PrinterException ex) {
				printError(ex.getMessage());
			}
		}
	}

	protected void save() {
		if (graph.getFile().canWrite()) {
			try {
				graph.saveToFile(this);
			} catch (final IOException ex) {
				printError(ex.getMessage());
			}
		} else
			saveAs();
	}

	protected void load() {
		final FileDialog fd = new FileDialog(fenster, "Load Graph");

		if ((graph.getFile() != null) && graph.getFile().exists())
			fd.setDirectory(graph.getFile().getAbsoluteFile().getParent());

		fd.setVisible(true);

		if (fd.getFile() != null) {

			try {
				final JJGraph tmpG = graph.getClass().newInstance();
				tmpG.createGraphic();

				tmpG.parseFile(fd.getDirectory() + fd.getFile());
			} catch (final IOException ex) {
				printError(ex.getMessage());
				Debug.println(ex.getMessage());
			} catch (final InstantiationException ex) {
				Debug.println("Could not create new graph: " + ex.getMessage());
			} catch (final IllegalAccessException ex) {
				Debug.println("Could not create new graph: " + ex.getMessage());
			}
		}
	}

	protected void loadLayout() {
		if (!(graph instanceof JJGraphImpl))
			return;

		final JJGraphImpl gi = (JJGraphImpl) graph;

		final FileDialog fd = new FileDialog(fenster, "Load Graph Layout");

		if ((gi.getFile() != null) && gi.getFile().exists())
			fd.setDirectory(gi.getFile().getAbsoluteFile().getParent());

		fd.setVisible(true);

		if (fd.getFile() != null) {
			try {
				gi.parseLayout(fd.getDirectory() + fd.getFile());
			} catch (final IOException ex) {
				printError(ex.getMessage());
				Debug.println(ex.getMessage());
			}
		}
	}

	protected void reload() {
		// Debug.println("Reloading...");

		if ((graph.getFile() != null) && (graph.getFile().exists())) {
			if (getGraph().isDirty()) {
				final int i = JOptionPane.showConfirmDialog(null, "Graph has not been saved. Save now?", "Warning",
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
				if (i == JOptionPane.YES_OPTION)
					save();
				else if (i == JOptionPane.CANCEL_OPTION)
					return;
			}

			graph.clear();

			try {
				graph.parseFile(graph.getFile().getAbsolutePath());
			} catch (final IOException ex) {
				printError(ex.getMessage());
				Debug.println(ex.getMessage());
			}
		} else
			load();
	}

	protected void saveAs() {
		final FileDialog fd = new FileDialog(fenster, "Save Graph", FileDialog.SAVE);

		if (graph.getFile() != null) {
			fd.setDirectory(graph.getFile().getParent());
			// Debug.println("Setting dir: " +
			// graph.getFile().getAbsoluteFile().getParent());

			if (graph.getFile().canWrite()) {
				// Debug.println("Setting file: " + graph.getFile().getName());
				fd.setFile(graph.getFile().getName());
			}
		}

		fd.setVisible(true);

		if (fd.getFile() != null) {
			try {
				graph.saveToFile(this, fd.getDirectory() + fd.getFile());
			} catch (final IOException ex) {
				printError(ex.getMessage());
			}
		}
	}

	@Override
	public void setBusy(final boolean flag) {
		fenster.setBusy(flag);
	}

	@Override
	public void updateDirected(final boolean v) {
		if (fenster.directedItem != null)
			fenster.directedItem.setSelected(v);
		_repaint();
	}

	@Override
	protected void deleteNode(final JJNode n) {
		final JJGraphicNode gn = n.getGraphicNode(this);

		selectedNodes.remove(gn);
		if (gn instanceof JJGraphicNodeImpl)
			boredNodes.remove(((JJGraphicNodeImpl) gn).getAppearance());
		repaint(gn);
	}

	@Override
	public JJGraphicNode addNode(final JJNode knoten) {

		JJGraphicNode n = knoten.getGraphicNode(this);
		if (n == null) {
			try {
				n = graphicNodeClass.newInstance();
				n.init(this, knoten, nodeColor);
				if (n instanceof JJGraphicNodeImpl) {
					final JJGraphicNodeImpl ni = (JJGraphicNodeImpl) n;

					final JJGraphicNodeAppearance app = graphicNodeAppearanceClass.newInstance();
					app.init(ni);
					ni.setAppearance(app);
					if (app instanceof JJGraphicNodeImageP) {
						boredNodes.add(app);
					}
				}
			} catch (final java.lang.InstantiationException e) {
				return null;
			} catch (final java.lang.IllegalAccessException e) {
				return null;
			}
		}

		// if(!invisibleCluster.contains(new Integer(knoten.getCluster()))){
		// n.hide();
		// repaint(n);
		// }
		sendAddNodeEvent(n);

		return n;
	}

	@Override
	public void deleteEdge(final JJEdge kante) {
		final JJGraphicEdge ge = kante.getGraphicEdge(this);
		if (ge == null)
			return;

		repaint(ge);
	}

	@Override
	public JJEdge addEdge(final JJEdge edge) {
		JJGraphicEdge e = edge.getGraphicEdge(this);

		if (e == null) {
			try {
				e = graphicEdgeClass.newInstance();
				e.init(this, edge, edgeColor);
			} catch (final java.lang.InstantiationException ex) {
				return null;
			} catch (final java.lang.IllegalAccessException ex) {
				return null;
			}
		}

		// edges.add(e);
		// if(edge.getTarget().getGraphicNode().isVisible() &&
		// edge.getSource().getGraphicNode().isVisible())
		// {
		// e.setVisible(true);
		// visibleEdges.add(e);
		repaint(e);
		// }
		sendAddEdgeEvent(e);

		return edge;
	}

	@Override
	public void repaint(final JJGraphicEdge e) {
		final Rectangle tmpRect = getBounds(e);
		if (tmpRect == null)
			return;

		if (repaintRect == null)
			repaintRect = tmpRect;
		else
			repaintRect.add(tmpRect);

		if (redraw)
			paintDirty();
	}

	@Override
	public void repaint(final Rectangle tmpRect) {
		if (repaintRect == null)
			repaintRect = new Rectangle(tmpRect);
		else
			repaintRect.add(tmpRect);

		if (redraw)
			paintDirty();
	}

	@Override
	public void repaint(final JJGraphicNode n) {
		final Rectangle tmpRect = getBounds(n);
		if (tmpRect == null)
			return;

		if (repaintRect == null)
			repaintRect = tmpRect;
		else
			repaintRect.add(tmpRect);

		if (redraw)
			paintDirty();
	}

	@Override
	public void paintDirty() {
		if (repaintRect != null) {
			super.repaint(repaintRect.x - 5, repaintRect.y - 5, repaintRect.width + 10, repaintRect.height + 10);
		}
		repaintRect = null;
	}

	// public void paintNode(Graphics2D g, JJGraphicNode n)
	// {
	// n.paint(g);
	// }

	@Override
	public void showNodes(final boolean flag) {
		// showNodes = flag;
		fenster.setShowNodes(flag);
	}

	// protected void updateVisibility() {
	// for(Iterator iter = graph.nodeIterator();
	// iter.hasNext();){
	// ((JJNode)iter.next()).getGraphicNode().unhide();
	// }

	// for(Iterator iter = graph.edgeIterator();
	// iter.hasNext();){
	// JJGraphicEdge loopE = ((JJEdge)iter.next()).getGraphicEdge();
	// JJNode source = loopE.getEdge().getSource();
	// JJNode target = loopE.getEdge().getTarget();

	// if( (!invisibleCluster.contains(new Integer(source.getCluster()))) ||
	// (!invisibleCluster.contains(new Integer(target.getCluster()))))
	// {
	// JJGraphicNode graphicSource = source.getGraphicNode();
	// JJGraphicNode graphicTarget = target.getGraphicNode();

	// graphicSource.unhide();
	// graphicTarget.unhide();
	// }
	// }

	// // To get all nodes without edges

	// for(Iterator iter = graph.nodeIterator();
	// iter.hasNext();){
	// JJGraphicNode loopN = ((JJNode)iter.next()).getGraphicNode();

	// if(! invisibleCluster.contains(new
	// Integer(loopN.getNode().getCluster())))
	// {
	// loopN.hide();
	// }
	// }
	// }

	@Override
	public void selectNodesInRectangle(final Rectangle rect) {
		deselectEdge();
		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			final JJGraphicNode gn = iter.next().getGraphicNode(this);
			if (gn.isVisible()) {
				final Rectangle testShape = getBounds(gn);

				if ((testShape != null) && rect.contains(testShape)) {
					select(gn);
				}
			}
		}
	}

	@Override
	public void select(final JJGraphicNode gn) {
		gn.setSelected(true);
		selectedNodes.add(gn);
		repaint(gn);
	}

	public void deselect(final JJGraphicNode gn) {
		gn.setSelected(false);
		selectedNodes.remove(gn);
		repaint(gn);
	}

	@Override
	public JJGraphicNode findNodeAt(final JJPoint p) {
		JJGraphicNode nodeAt = null;

		try {
			for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
				final JJGraphicNode n = iter.next().getGraphicNode(this);
				final Rectangle tmpRect = getBounds(n);
				if ((tmpRect != null) && (tmpRect.contains((int) p.getX() + xOffset, (int) p.getY() + yOffset))) {
					nodeAt = n;
				}
			}
		} catch (final ConcurrentModificationException e) {
		}

		return nodeAt;
	}

	@Override
	public boolean isDrawn(final JJGraphicEdge e) {
		final JJEdge edge = e.getEdge();

		return e.isVisible() && edge.getSource().getGraphicNode(this).isVisible()
				&& edge.getTarget().getGraphicNode(this).isVisible();
	}

	@Override
	public JJGraphicEdge findEdgeAt(final JJPoint p) {
		// JJGraphicEdge edgeAt = null;

		for (final Iterator<JJEdge> iter = graph.edgeIterator(); iter.hasNext();) {
			final JJEdge e = iter.next();
			final JJGraphicEdge gEdge = e.getGraphicEdge(this);
			final JJGraphicNode gs = e.getSource().getGraphicNode(this);
			final JJGraphicNode gt = e.getTarget().getGraphicNode(this);

			if (isDrawn(gEdge)) {
				int x1 = (int) (gs.getX() * zoom);
				int y1 = (int) (gs.getY() * zoom);
				final int x2 = (int) (gt.getX() * zoom);
				final int y2 = (int) (gt.getY() * zoom);

				if (gEdge.getBends() != null) {
					for (final Iterator<JJPoint> iter2 = gEdge.getBends().listIterator(); iter2.hasNext();) {
						final JJPoint p2 = iter2.next();
						final int tmpX = (int) (p2.getX() * zoom);
						final int tmpY = (int) (p2.getY() * zoom);
						if (Line2D.ptSegDist(x1, y1, tmpX, tmpY, (int) p.getX(), (int) p.getY()) < FANG_RADIUS) {
							return gEdge;
						}
						x1 = tmpX;
						y1 = tmpY;
					}
				}
				if (Line2D.ptSegDist(x1, y1, x2, y2, (int) p.getX(), (int) p.getY()) < FANG_RADIUS) {
					return gEdge;
				}
			}
		}

		return null;
	}

	@Override
	public void addNewElementAt(final JJPoint p) {
		final JJGraphicNode targetGN = findNodeAt(p);

		if ((targetGN != null) && (!selectedNodes.isEmpty())) {
			for (final Iterator<JJGraphicNode> iter = selectedNodes.listIterator(); iter.hasNext();) {
				final JJGraphicNode pick = iter.next();
				final JJEdge e = graph.addEdge(pick.getNode(), targetGN.getNode());
				e.setWeight(defaultEdgeWeight);
			}
		} else {
			if (graph.getUndoRecording()) {
				graph.getUndoManager().openSubtask("Create node");
			}

			final JJNode tmpN = graph.addNode();
			moveNodeTo(tmpN.getGraphicNode(this), JJPoint.div(p, zoom));

			if (graph.getUndoRecording()) {
				graph.getUndoManager().closeSubtask("Create node");
			}

		}
	}

	@Override
	public void moveNodeTo(final JJGraphicNode n, final JJPoint p) {
		moveNodeTo(n, p.x, p.y, p.getZ());
	}

	@Override
	public void moveNodeTo(final JJGraphicNode n, final double x, final double y) {
		moveNodeTo(n, x, y, 0.0);
	}

	private final JJGraphEvent nodeEvent = new JJGraphEvent(null, 0);
	private final JJGraphEvent edgeEvent = new JJGraphEvent(null, 0);
	private final JJGraphEvent graphEvent = new JJGraphEvent(null, 0);

	public synchronized void sendStopRedraw() {
		graphEvent.setSource(this);
		graphEvent.setType(JJGraphEvent.DISABLE_REDRAW);
		sendGraphAppearanceEvent(graphEvent);
	}

	public synchronized void sendStartRedraw() {
		graphEvent.setSource(this);
		graphEvent.setType(JJGraphEvent.ENABLE_REDRAW);
		sendGraphAppearanceEvent(graphEvent);
	}

	public synchronized void sendAddNodeEvent(final JJGraphicNode gn) {
		nodeEvent.setSource(gn);
		nodeEvent.setType(JJGraphEvent.NODE_ADD_EVENT);
		sendGraphAppearanceEvent(nodeEvent);
	}

	public synchronized void sendAddEdgeEvent(final JJGraphicEdge gn) {
		edgeEvent.setSource(gn);
		edgeEvent.setType(JJGraphEvent.EDGE_ADD_EVENT);
		sendGraphAppearanceEvent(edgeEvent);
	}

	public synchronized void sendNodeColourChangeEvent(final JJGraphicNode gn) {
		nodeEvent.setSource(gn);
		nodeEvent.setType(JJGraphEvent.NODE_COLOUR_EVENT);
		sendGraphAppearanceEvent(nodeEvent);
	}

	@Override
	public synchronized void sendEdgeColourChangeEvent(final JJGraphicEdge gn) {
		edgeEvent.setSource(gn);
		edgeEvent.setType(JJGraphEvent.EDGE_COLOUR_EVENT);
		sendGraphAppearanceEvent(edgeEvent);
	}

	@Override
	public synchronized void sendEdgeVisibilityChangeEvent(final JJGraphicEdge gn) {
		edgeEvent.setSource(gn);
		edgeEvent.setType(JJGraphEvent.EDGE_VISBILITY_CHANGE);
		sendGraphAppearanceEvent(edgeEvent);
	}

	public synchronized void sendNodeVisibilityChangeEvent(final JJGraphicNode gn) {
		nodeEvent.setSource(gn);
		nodeEvent.setType(JJGraphEvent.NODE_VISBILITY_CHANGE);
		sendGraphAppearanceEvent(nodeEvent);
	}

	public synchronized void sendNodeMoveEvent(final JJGraphicNode gn) {
		for (final Iterator<JJEdge> edgeIter = gn.getNode().edgeIterator(); edgeIter.hasNext();) {
			final JJGraphicEdge ge = edgeIter.next().getGraphicEdge(this);
			edgeEvent.setSource(ge);
			edgeEvent.setType(JJGraphEvent.EDGE_MOVE_EVENT);
			sendGraphAppearanceEvent(edgeEvent);
		}

		nodeEvent.setSource(gn);
		nodeEvent.setType(JJGraphEvent.NODE_MOVE_EVENT);
		sendGraphAppearanceEvent(nodeEvent);
	}

	@Override
	public void moveNodeTo(final JJGraphicNode n, final double x, final double y, final double z) {
		Iterator<JJEdge> edgeIter = null;

		final boolean tmpRedraw = setRedraw(false);

		repaint(n);

		for (edgeIter = n.getNode().outIterator(); edgeIter.hasNext();) {
			repaint(edgeIter.next().getGraphicEdge(this));
		}

		for (edgeIter = n.getNode().inIterator(); edgeIter.hasNext();) {
			repaint(edgeIter.next().getGraphicEdge(this));
		}

		n.moveTo(x, y, z);

		repaint(n);

		for (edgeIter = n.getNode().edgeIterator(); edgeIter.hasNext();) {
			final JJGraphicEdge ge = edgeIter.next().getGraphicEdge(this);
			// edgeMoveEvent.setSource(ge);
			repaint(ge);
		}

		// for(edgeIter = n.getNode().inIterator(); edgeIter.hasNext();) {
		// repaint(((JJEdge)edgeIter.next()).getGraphicEdge(this));
		// }

		setRedraw(tmpRedraw);
	}

	public void burstMoveNodeTo(final JJGraphicNode n, final JJPoint p) {
		burstMoveNodeTo(n, p.x, p.y, p.getZ());
	}

	public void burstMoveNodeTo(final JJGraphicNode n, final double x, final double y, final double z) {
		n.moveTo(x, y, z);
	}

	@Override
	public void rmoveNodeTo(final JJGraphicNode n, final double x, final double y) {
		rmoveNodeTo(n, x, y, 0);
	}

	@Override
	public void rmoveNodeTo(final JJGraphicNode n, final double x, final double y, final double z) {
		moveNodeTo(n, n.getX() + x, n.getY() + y, n.getZ() + z);
	}

	@Override
	public void rmoveEdgeTo(final JJGraphicEdge e, final JJPoint p) {
		rmoveEdgeTo(e, p.x, p.y);
	}

	@Override
	public void rmoveEdgeTo(final JJGraphicEdge e, final double x, final double y) {
		if (e.getBends() == null)
			return;

		final boolean tmpRedraw = setRedraw(false);
		repaint(e);
		e.rmoveTo(x, y);
		repaint(e);
		setRedraw(tmpRedraw);
	}

	@Override
	public void moveBendTo(final JJGraphicEdge e, final int i, final double x, final double y) {
		if (e.getBends() == null)
			return;

		final boolean tmpRedraw = setRedraw(false);
		repaint(e);
		e.moveBendTo(i, x, y);
		repaint(e);
		setRedraw(tmpRedraw);
	}

	@Override
	public void rmoveNodeTo(final JJGraphicNode n, final JJPoint p) {
		moveNodeTo(n, n.getX() + p.getX(), n.getY() + p.getY());
	}

	protected void deleteElementAt(final JJPoint punkt) {
		final JJGraphicNode gn = findNodeAt(punkt);

		if (gn != null) {
			graph.deleteNode(gn.getNode());
			return;
		}

		final JJGraphicEdge ge = findEdgeAt(punkt);

		if (ge != null) {
			if (selectedEdge == ge) {
				selectedEdge = null;
			}

			graph.deleteEdge(ge.getEdge());
		}
	}

	protected void updateToolTip(final JJPoint punkt) {
		final JJGraphicNode gn = findNodeAt(punkt);

		if (gn != null) {
			// boolean tmpP = isShortLabels();
			// setShortLabels(false);
			setToolTipText(gn.getNode().getName());
			// setShortLabels(tmpP);
		} else
			setToolTipText(null);
	}

	public void deselectNodes() {
		for (final Iterator<JJGraphicNode> iter = selectedNodes.listIterator(); iter.hasNext();) {
			final JJGraphicNode gn = iter.next();
			iter.remove();
			gn.setSelected(false);
			repaint(gn);
		}
		// selectedNodes.clear();
	}

	public void deselectEdge() {
		if (selectedEdge != null) {
			selectedEdge.setSelected(false);
			repaint(selectedEdge);
		}

		selectedEdge = null;
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		boredTimer.restart();

		requestFocus();
		dragPoint = new JJPoint(e.getX() - xOffset, e.getY() - yOffset);

		if (!isPopupTrigger(e) && e.isControlDown()) // Add element
		{
			addNewElementAt(dragPoint);
		} else if (!e.isPopupTrigger()) {
			deselectEdge();
			final JJGraphicNode pick = findNodeAt(dragPoint);

			if (pick != null) { // Found a node
				if (!e.isShiftDown()) {
					if (!pick.isSelected()) {
						deselectNodes();
						select(pick);
					}
				} else {
					if (!pick.isSelected()) {
						select(pick);
					} else
						deselect(pick);
				}
			} else { // Looking for an edge
				selectedEdge = findEdgeAt(dragPoint);
				if (selectedEdge != null) { // Found an edge
					deselectNodes();
					selectedEdge.setSelected(true);
					repaint(selectedEdge);
				} else if (!isPopupTrigger(e)) {
					dragBackground = createImage(offscreen.getWidth(null), offscreen.getHeight(null));
					final Graphics2D dragGraphics = (Graphics2D) dragBackground.getGraphics();
					dragGraphics.drawImage(offscreen, 0, 0, null);
					if (e.isAltDown())
						zoomSelect = true;
					else
						dragSelect = true;
					dragStart = new JJPoint(dragPoint);
				}
			}
		} else if (e.isPopupTrigger()) { // Never happens on MS Windows
			if (!selectedNodes.isEmpty()) {
				showNodePopup(e.getComponent(), e.getX(), e.getY());
			} else if (selectedEdge != null) {
				edgePopup.show(e.getComponent(), e.getX(), e.getY());
			} else {
				graphPopup.show(e.getComponent(), e.getX(), e.getY());
			}
		}
		paintDirty();
	}

	public void showNodePopup(final Component c, final int x, final int y) {
		nodePopup.show(c, x, y);
	}

	protected boolean isPopupTrigger(final MouseEvent e) {
		return (e.getButton() == MouseEvent.BUTTON3);
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		if (e == null || dragPoint == null) {
			return;
		}
		boredTimer.restart();
		if (!isPopupTrigger(e)) {
			if (dragSelect || zoomSelect) {
				dragPoint.x = e.getX() - xOffset;
				dragPoint.y = e.getY() - yOffset;
				_repaint();
			} else { // move selected nodes
				if ((dragStart != null) && graph.getUndoRecording()) {
					graph.setUndoRecording(false);
				}
				final int gs = (getGridSize());

				final double xDiff = ((e.getX() - xOffset) - dragPoint.x);
				final double yDiff = ((e.getY() - yOffset) - dragPoint.y);

				if ((Math.abs(xDiff) >= gs * zoom) || (Math.abs(yDiff) >= gs * zoom)) {
					double moveX = xDiff / zoom;
					double moveY = yDiff / zoom;

					if (gs > 0) {
						moveX = ((int) (xDiff / zoom + 0.5) / gs) * gs;
						moveY = ((int) (yDiff / zoom + 0.5) / gs) * gs;
					}

					final boolean tmpRedraw = setRedraw(false);
					for (final Iterator<JJGraphicNode> iter = selectedNodes.listIterator(); iter.hasNext();) {
						final JJGraphicNode pick = iter.next();
						rmoveNodeTo(pick, moveX, moveY);
						for (final Iterator<JJEdge> edgeIter = pick.getNode().outIterator(); edgeIter.hasNext();) {
							final JJEdge tmpE = edgeIter.next();
							if (tmpE.getTarget().getGraphicNode(this).isSelected()) {
								rmoveEdgeTo(tmpE.getGraphicEdge(this), xDiff / zoom, yDiff / zoom);
							}
						}
					}

					setRedraw(tmpRedraw);
					if (Math.abs(xDiff) >= gs * zoom)
						dragPoint.x += moveX * zoom;
					if (Math.abs(yDiff) >= gs * zoom)
						dragPoint.y += moveY * zoom;
				}

			}
		}
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
		updateToolTip(new JJPoint(e.getX() - xOffset, e.getY() - yOffset));
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		boredTimer.restart();
		if (isPopupTrigger(e)) {
			if (e.isPopupTrigger()) {
				if (!selectedNodes.isEmpty()) {
					showNodePopup(e.getComponent(), e.getX(), e.getY());
				} else if (selectedEdge != null) {
					edgePopup.show(e.getComponent(), e.getX(), e.getY());
				} else {
					graphPopup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		} else if (dragSelect || zoomSelect) {
			deselectNodes();
			final Rectangle catchRect = new Rectangle((int) dragStart.x, (int) dragStart.y, 0, 0);
			catchRect.add(dragPoint.x, dragPoint.y);

			catchRect.x += xOffset;
			catchRect.y += yOffset;
			if (zoomSelect) {
				zoomTo(catchRect);
			} else {
				selectNodesInRectangle(catchRect);
			}
			dragSelect = false;
			zoomSelect = false;
			repaint(catchRect);
		} else {
			if (!graph.getUndoRecording()) {
				graph.setUndoRecording(true);

				dragPoint.x = e.getX() - xOffset;
				dragPoint.y = e.getY() - yOffset;

				dragPoint.minusA(dragStart);

				if (selectedNodes.size() > 1) {
					graph.getUndoManager().openSubtask("Move nodes");
				}

				for (final Iterator<JJGraphicNode> iter = selectedNodes.listIterator(); iter.hasNext();) {
					final JJGraphicNode pick = iter.next();
					pick.createUndoMoveEventTo(pick.getCoords().minusA(dragPoint));

					for (final Iterator<JJEdge> edgeIter = pick.getNode().outIterator(); edgeIter.hasNext();) {
						final JJEdge tmpE = edgeIter.next();
						if (tmpE.getTarget().getGraphicNode(this).isSelected()) {
							tmpE.getGraphicEdge(this).createUndoMoveEventR(-dragPoint.x, -dragPoint.y);
						}
					}

				}
				if (selectedNodes.size() > 1) {
					graph.getUndoManager().closeSubtask("Move nodes");
				}
			}
		}
		paintDirty();
	}

	@Override
	public int print(final Graphics g, final PageFormat pf, final int pi) throws PrinterException {
		if (pi >= 1) {
			return Printable.NO_SUCH_PAGE;
		}

		final Graphics2D tmpG = (Graphics2D) g;
		tmpG.setFont(getFont());

		tmpG.addRenderingHints(AALIAS);
		tmpG.addRenderingHints(NO_TEXT_AALIAS);
		final Rectangle bounds = g.getClipBounds();
		// Debug.println(bounds);

		g.setColor(Color.white);
		g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
		g.setColor(Color.black);
		// g.drawRect(bounds.x+1, bounds.y+1, bounds.width-2,
		// bounds.height-2);

		// Rectangle offSet = new Rectangle();

		final Rectangle bbox = boundingBox(); // doPaint(tmpG, offSet, false);

		// Debug.println("BBox :" +bbox);

		final boolean doRotate = false;
		double scale = 1.0;

		scale = Math.min((bounds.getWidth()) / bbox.getWidth(), (bounds.getHeight()) / bbox.getHeight());

		tmpG.translate(bounds.getX(), bounds.getY());
		tmpG.scale(scale, scale);
		tmpG.translate(-bbox.getX(), -bbox.getY());

		doPaint(tmpG);

		return Printable.PAGE_EXISTS;
	}

	@Override
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);

		final Rectangle rect = g.getClipBounds();

		final Dimension d = new Dimension(rect.width, rect.height);

		// Debug.println("Painting component " + d);

		boolean newOffScreen = false;

		if ((offscreen == null) || (d.width != offscreensize.width) || (d.height != offscreensize.height)) {
			if ((d.height <= 0) || (d.width <= 0))
				return;
			offscreen = createImage(d.width, d.height);
			offscreensize = d;
			offGraphics = (Graphics2D) offscreen.getGraphics();
			offGraphics.addRenderingHints(AALIAS);
			offGraphics.addRenderingHints(NO_TEXT_AALIAS);
			offGraphics.clipRect(0, 0, d.width, d.height);
			offGraphics.setFont(getFont());
			newOffScreen = true;
		}

		offGraphics.translate(-rect.x, -rect.y);

		if ((dragSelect || zoomSelect) && (!newOffScreen)) {
			offGraphics.drawImage(dragBackground, rect.x, rect.y, null);
			offGraphics.setColor(Color.red);
			offGraphics.drawRect((int) Math.min(dragStart.x, dragPoint.x) + xOffset,
					(int) Math.min(dragStart.y, dragPoint.y) + yOffset, (int) Math.abs(dragStart.x - dragPoint.x),
					(int) Math.abs(dragStart.y - dragPoint.y));
		} else {

			offGraphics.setColor(getBackground());// new Color(250, 250, 250));
			offGraphics.fillRect(rect.x, rect.y, d.width, d.height);
			offGraphics.setColor(Color.black);
			if (predrawer != null) {
				predrawer.draw(offGraphics);
			}

			// doPaint((Graphics2D) offGraphics, rect);
			// offGraphics.translate(xOffset, yOffset);
			doPaint(offGraphics);

			if (dragSelect || zoomSelect) {
				dragBackground = createImage(d.width, d.height);
				dragBackground.getGraphics().drawImage(offscreen, 0, 0, null);
			}
		}

		g.drawImage(offscreen, rect.x, rect.y, null);
		offGraphics.translate(rect.x, rect.y);
	}

	// public void paintEdge(Graphics2D g, JJGraphicEdge e)
	// {
	// e.paint(g);
	// }

	public Rectangle boundingBox() {
		return boundingBox(false);
	}

	public Rectangle boundingBox(final boolean selectedOnly) {
		Rectangle bbox = null;

		if (fenster.getShowEdges()) {
			for (Iterator<JJEdge> iter = graph.edgeIterator(); iter.hasNext();) {
				JJGraphicEdge e;
				try {
					e = iter.next().getGraphicEdge(this);
				} catch (final java.util.ConcurrentModificationException exep) {
					iter = graph.edgeIterator();
					continue;
				}

				if (isDrawn(e) && ((!selectedOnly) || nodesSelected(e))) {
					if (bbox == null)
						bbox = getBounds(e);
					else {
						final Rectangle tmpR = getBounds(e);
						if (tmpR != null)
							bbox.add(tmpR);
					}
				}
			}
		}

		if (fenster.getShowNodes()) {
			for (Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
				JJGraphicNode n;
				try {
					n = iter.next().getGraphicNode(this);
				} catch (final java.util.ConcurrentModificationException exep) {
					iter = graph.nodeIterator();
					continue;
				}
				if (n.isVisible() && ((!selectedOnly) || n.isSelected())) {
					if (bbox == null)
						bbox = getBounds(n); // n.getBounds();
					else {
						final Rectangle r = getBounds(n);
						if (r != null)
							bbox.add(getBounds(n)); // n.getBounds());
					}
				}
			}
		}

		if (bbox == null)
			bbox = new Rectangle();
		// Debug.println("BBox final:" +bbox);

		return bbox;
	}

	public void doPaint(final Graphics2D offGraphics) {
		// FontMetrics fm = offGraphics.getFontMetrics();

		offGraphics.setColor(Color.black);

		if (fenster.getShowEdges()) {
			for (Iterator<JJEdge> iter = graph.edgeIterator(); iter.hasNext();) {
				JJGraphicEdge e;
				JJEdge tmpE;

				try {
					tmpE = iter.next();
					e = tmpE.getGraphicEdge(this);
				} catch (final java.util.ConcurrentModificationException exep) {
					iter = graph.edgeIterator();
					continue;
				}
				if ((e != null) && isDrawn(e))
					e.paint(offGraphics);

			}
		}

		if (fenster.getShowNodes()) {

			for (Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
				JJGraphicNode n;
				try {
					n = iter.next().getGraphicNode(this);
				} catch (final java.util.ConcurrentModificationException exep) {
					iter = graph.nodeIterator();
					continue;
				}
				if ((n != null) && n.isVisible())
					n.paint(offGraphics);
			}
		}
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		boredTimer.restart();
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
	}

	@Override
	public void mouseExited(final MouseEvent e) {
	}

	@Override
	public void keyPressed(final KeyEvent e) {
	}

	@Override
	public void keyReleased(final KeyEvent e) {
		if (e.isControlDown() || e.isAltDown()) {
			switch (e.getKeyCode()) {
			case 'X': {
				handleCut();
				break;
			}
			case 'C': {
				handleCopy();
				break;
			}
			case 'V': {
				handlePaste();
				break;
			}
			default: {
			}
			}
		}
	}

	@Override
	public void keyTyped(final KeyEvent e) {
		boredTimer.restart();

		if (e.isControlDown() || e.isAltDown())
			return;

		final boolean tmpRedraw = setRedraw(false);

		if (selectedNodes.size() > 0) {
			getGraph().getUndoManager().openSubtask("Multi node keyboard");

			for (final Iterator<JJGraphicNode> iter = selectedNodes.listIterator(); iter.hasNext();) {
				final JJGraphicNode pick = iter.next();
				final JJNode knoten = pick.getNode();
				switch (e.getKeyChar()) {
				case 127:
				case 8: {
					if (knoten.getName().length() > 0) {
						String tmpS = knoten.getName();
						tmpS = tmpS.substring(0, tmpS.length() - 1);
						knoten.setName(tmpS);
					}
					break;
				}
				default: {
					knoten.setName(knoten.getName() + e.getKeyChar());
				}
				}
				// repaint(pick);
			}

			getGraph().getUndoManager().closeSubtask("Multi node keyboard");
		}

		if (selectedEdge != null) {
			final JJEdge kante = selectedEdge.getEdge();
			repaint(selectedEdge);

			if (e.isAltDown()) {
				switch (e.getKeyChar()) {
				case 'x': {
					graph.deleteEdge(kante);
					break;
				}
				default: {
				}
				}
			} else {
				switch (e.getKeyChar()) {
				case 127:
				case 8: {
					if (kante.getName().length() > 0) {
						String tmpS = kante.getName();
						tmpS = tmpS.substring(0, tmpS.length() - 1);
						kante.setName(tmpS);
					}
					break;
				}
				default: {
					kante.setName(kante.getName() + e.getKeyChar());
				}
				}
			}
		}

		setRedraw(tmpRedraw);
	}

	@Override
	public void zoomTo(final double x) {

		fenster.zoomTo(x);
	}

	public void _zoomTo(final double x) {
		zoom = x;
		// _repaint();
	}

	@Override
	public void removeBends() {
		getGraph().getUndoManager().openSubtask("Remove bends");
		for (final Iterator<JJEdge> edgeIter = graph.edgeIterator(); edgeIter.hasNext();) {

			final JJEdge tmpE = edgeIter.next();
			final JJGraphicEdge ge = tmpE.getGraphicEdge(this);

			repaint(ge);
			ge.removeBends();
			repaint(ge);
		}

		getGraph().getUndoManager().closeSubtask("Remove bends");
	}

	public void bendsToNodes() {
		getGraph().getUndoManager().openSubtask("All Bends to Nodes");
		final java.util.List<JJEdge> edges = new ArrayList<>(graph.getEdges());

		for (final JJEdge jjEdge : edges) {

			final JJEdge tmpE = jjEdge;
			tmpE.getGraphicEdge(this).bendsToNodes();
		}

		getGraph().getUndoManager().closeSubtask("All Bends to Nodes");
	}

	// public void addBendFirst(JJGraphicEdge e, JJPoint p)
	// {
	// e.addBendFirst(p);
	// repaint(e);
	// }

	@Override
	public void show3D() {
		addAppearanceListener(new JJ3DGraph(this));
	}

	protected void _repaint() {
		// Debug.println("_Repainting");
		repaintRect = null;
		super.repaint();
	}

	@Override
	public void forceRepaintAll() {
		_repaint();
	}

	@Override
	public void repaint() {
		if (redraw) {
			repaintRect = null;
			super.repaint();
		} else
			repaint(getVisibleRect());

	}

	protected double getX(final JJGraphicNode n) {
		return deviceAdjustX(n.getX());
	}

	protected double getY(final JJGraphicNode n) {
		return deviceAdjustY(n.getY());
	}

	@Override
	public double deviceAdjustX(final double x) {
		final int gs = (gridSize);
		if (gs > 0)
			return ((int) (x + 0.5) / gs) * gs * zoom + xOffset;

		return x * zoom + xOffset;
	}

	@Override
	public double deviceAdjustY(final double y) {
		final int gs = (gridSize);
		if (gs > 0)
			return ((int) (y + 0.5) / gs) * gs * zoom + yOffset;

		return y * zoom + yOffset;
	}

	@Override
	public double deviceAdjustWidth(final double x) {
		return x * zoom;
	}

	@Override
	public double deviceAdjustHeight(final double y) {
		return y * zoom;
	}

	@Override
	public Rectangle deviceAdjust(final Rectangle r) {
		r.x = (int) (r.x * zoom + xOffset);
		r.y = (int) (r.y * zoom + xOffset);
		r.width = (int) deviceAdjustWidth(r.width);
		r.height = (int) deviceAdjustHeight(r.height);
		return r;
	}

	@Override
	public Rectangle getVisibleBounds() {
		final Rectangle r = getVisibleRect();

		r.x = (int) ((r.x - xOffset) / zoom);
		r.y = (int) ((r.y - yOffset) / zoom);
		r.width = (int) (r.width / zoom);
		r.height = (int) (r.height / zoom);
		return r;

	}

	@Override
	public Rectangle getBounds(final JJGraphicNode n) {
		if ((n == null) || (!fenster.getShowNodes()) || (!n.isVisible())) {
			return null;
		}

		final double w = n.getWidth();
		final double h = n.getHeight();

		return new Rectangle((int) (getX(n) - w / 2.0), (int) (getY(n) - h / 2.0), (int) w + 1, (int) h + 1);
	}

	@Override
	public Rectangle getBounds(final JJGraphicEdge e) {
		if ((!fenster.getShowEdges()) || (!isDrawn(e)))
			return null;

		final Rectangle r = e.getBounds();
		return r;
	}

	@Override
	public String adjustLabel(final String name) {
		if (name == null)
			return "";

		if (shortLabels && (name.length() > 20))
			return "..." + name.substring(name.length() - 17);
		return name;
	}

	class TabListener implements ActionListener {
		@Override
		public void actionPerformed(final java.awt.event.ActionEvent e) {
			Debug.println("Should change color now");
		}

	}

	@Override
	public JPanel createTab() {
		final JPanel p = new JPanel();
		final JButton b = new JButton("Change Color");
		final TabListener l = new TabListener();
		b.addActionListener(l);

		p.add(b);
		return p;
	}

	@Override
	public String getTabName() {
		return "Graph";
	}

	@Override
	public void addTool(final JJInspectable e) {
		fenster.addTool(e);
	}

	@Override
	public JJInspectable getTool(final Class<? extends JJInspectable> c) {
		return fenster.getTool(c);
	}

	void recomputeNodeWidth() {
		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			iter.next().getGraphicNode(this).recomputeNodeSize();
		}
	}

	void recomputeEdgeWidth() {
		for (final Iterator<JJEdge> iter = graph.edgeIterator(); iter.hasNext();) {
			iter.next().getGraphicEdge(this).recomputeEdgeSize();
		}
	}

	// void invalidateWidth(JJGraphicNode n)
	// {
	// n.setFontMetrics(null);
	// }

	@Override
	public void graphStructureChanged(final JJGraphEvent e) {
		updateStatus();
	}

	public void updateStatus() {
		fenster.setStatusMessage(getString("nodes") + ": " + graph.getNumNodes() + " " + getString("edges") + ": "
				+ graph.getNumEdges() + ",    Status: " + statusMsg);
	}

	public void graphAppearanceChanged(final JJGraphEvent e) {
	}

	public boolean nodesSelected(final JJGraphicEdge e) {
		return nodesSelected(e.getEdge());
	}

	public boolean nodesSelected(final JJEdge e) {
		final JJGraphicNode sn = e.getSource().getGraphicNode(this);
		final JJGraphicNode tn = e.getTarget().getGraphicNode(this);
		return (sn.isSelected() && tn.isSelected());
	}

	public void clearEdges() {
		final boolean tmpRedraw = setRedraw(false);
		getGraph().getUndoManager().openSubtask("Remove edges");

		JJEdge edges[] = new JJEdge[0];
		edges = graph.getEdges().toArray(edges);
		for (final JJEdge edge : edges) {
			getGraph().deleteEdge(edge);
		}

		getGraph().getUndoManager().closeSubtask("Remove edges");
		setRedraw(tmpRedraw);
	}

	public void rotateGraph(final double alpha) {
		if (graph.getNumNodes() * alpha == 0)
			return;

		final boolean tmpRedraw = setRedraw(false);
		getGraph().getUndoManager().openSubtask("Rotate Graph");

		boolean selectedOnly = false;

		if (selectedNodes.size() > 1) {
			selectedOnly = true;
		}

		final JJPoint center = getGraphCenter(selectedOnly);

		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			final JJGraphicNode n = iter.next().getGraphicNode(this);

			if ((!selectedOnly) || n.isSelected()) {
				final JJPoint p = n.getCoords();

				p.minusA(center);
				p.rotateA(alpha);
				p.plusA(center);
				moveNodeTo(n, p);
			}
		}

		for (final Iterator<JJEdge> iter = getGraph().edgeIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			final JJGraphicEdge ge = tmpE.getGraphicEdge(this);

			if ((!selectedOnly) || nodesSelected(tmpE)) {
				if (ge.getBends() != null) {
					int k = 0;
					for (final Object element : ge.getBends()) {
						final JJPoint p = (JJPoint) element;

						p.minusA(center);
						p.rotateA(alpha);
						p.plusA(center);
						moveBendTo(ge, k++, p.x, p.y);
					}
				}
			}
		}

		getGraph().getUndoManager().closeSubtask("Rotate Graph");
		setRedraw(tmpRedraw);
	}

	public void flipX() {
		if (graph.getNumNodes() == 0)
			return;

		final boolean tmpRedraw = setRedraw(false);
		getGraph().getUndoManager().openSubtask("Flip X");

		boolean selectedOnly = false;

		if (selectedNodes.size() > 1) {
			selectedOnly = true;
		}

		final JJPoint center = getGraphCenter(selectedOnly);

		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			final JJGraphicNode n = iter.next().getGraphicNode(this);
			if ((!selectedOnly) || n.isSelected()) {
				final JJPoint p = n.getCoords();

				p.minusA(center);
				p.y = -p.y; // rotateA(alpha);
				p.plusA(center);
				moveNodeTo(n, p);
			}
		}

		for (final Iterator<JJEdge> iter = getGraph().edgeIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			if ((!selectedOnly) || nodesSelected(tmpE)) {
				final JJGraphicEdge ge = tmpE.getGraphicEdge(this);
				if (ge.getBends() != null) {
					int k = 0;
					for (final Object element : ge.getBends()) {
						final JJPoint p = (JJPoint) element;

						p.minusA(center);
						p.y = -p.y; // rotateA(alpha);
						p.plusA(center);
						moveBendTo(ge, k++, p.x, p.y);
					}
				}
			}
		}

		getGraph().getUndoManager().closeSubtask("Flip X");
		setRedraw(tmpRedraw);
	}

	public void flipY() {
		if (graph.getNumNodes() == 0)
			return;

		final boolean tmpRedraw = setRedraw(false);
		getGraph().getUndoManager().openSubtask("Flip Y");

		boolean selectedOnly = false;

		if (selectedNodes.size() > 1) {
			selectedOnly = true;
		}

		final JJPoint center = getGraphCenter(selectedOnly);

		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			final JJGraphicNode n = iter.next().getGraphicNode(this);
			if ((!selectedOnly) || n.isSelected()) {
				final JJPoint p = n.getCoords();

				p.minusA(center);
				p.x = -p.x; // rotateA(alpha);
				p.plusA(center);
				moveNodeTo(n, p);
			}
		}

		for (final Iterator<JJEdge> iter = getGraph().edgeIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			if ((!selectedOnly) || nodesSelected(tmpE)) {
				final JJGraphicEdge ge = tmpE.getGraphicEdge(this);
				if (ge.getBends() != null) {
					int k = 0;
					for (final Object element : ge.getBends()) {
						final JJPoint p = (JJPoint) element;

						p.minusA(center);
						p.x = -p.x; // rotateA(alpha);
						p.plusA(center);
						moveBendTo(ge, k++, p.x, p.y);
					}
				}
			}
		}

		getGraph().getUndoManager().closeSubtask("Flip Y");
		setRedraw(tmpRedraw);
	}

	public void scaleGraph(final double f) {
		if (graph.getNumNodes() * f == 0)
			return;

		final boolean tmpRedraw = setRedraw(false);
		getGraph().getUndoManager().openSubtask("Scale Graph");

		boolean selectedOnly = false;

		if (selectedNodes.size() > 1) {
			selectedOnly = true;
		}

		final JJPoint center = getGraphCenter(selectedOnly);

		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			final JJGraphicNode n = iter.next().getGraphicNode(this);
			if ((!selectedOnly) || n.isSelected()) {
				final JJPoint p = n.getCoords();

				p.minusA(center);
				p.multA(f);
				p.plusA(center);
				moveNodeTo(n, p);
			}
		}

		for (final Iterator<JJEdge> iter = getGraph().edgeIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			final JJGraphicEdge ge = tmpE.getGraphicEdge(this);
			if ((!selectedOnly) || nodesSelected(tmpE)) {
				if (ge.getBends() != null) {
					int k = 0;
					for (final Object element : ge.getBends()) {
						final JJPoint p = (JJPoint) element;

						p.minusA(center);
						p.multA(f);
						p.plusA(center);
						moveBendTo(ge, k++, p.x, p.y);
					}
				}
			}
		}

		getGraph().getUndoManager().closeSubtask("Scale Graph");
		setRedraw(tmpRedraw);
	}

	public void skewGraph(final double f) {
		if (graph.getNumNodes() * f == 0)
			return;

		final boolean tmpRedraw = setRedraw(false);
		getGraph().getUndoManager().openSubtask("Skew Graph");

		boolean selectedOnly = false;

		if (selectedNodes.size() > 1) {
			selectedOnly = true;
		}

		final JJPoint center = getGraphCenter(selectedOnly);

		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			final JJGraphicNode n = iter.next().getGraphicNode(this);
			if ((!selectedOnly) || n.isSelected()) {
				final JJPoint p = n.getCoords();

				p.minusA(center);
				final double x = p.x - Math.sin(f * Math.atan2(0, -1) / 180.0) * p.y;
				final double y = Math.cos(f * Math.atan2(0, -1) / 180.0) * p.y;
				p.x = x;
				p.y = y;
				p.plusA(center);
				moveNodeTo(n, p);
			}
		}

		for (final Iterator<JJEdge> iter = getGraph().edgeIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			final JJGraphicEdge ge = tmpE.getGraphicEdge(this);
			if ((!selectedOnly) || nodesSelected(tmpE)) {
				if (ge.getBends() != null) {
					int k = 0;
					for (final Object element : ge.getBends()) {
						final JJPoint p = (JJPoint) element;

						p.minusA(center);
						final double x = p.x - Math.sin(f * Math.atan2(0, -1) / 180.0) * p.y;
						final double y = Math.cos(f * Math.atan2(0, -1) / 180.0) * p.y;
						p.x = x;
						p.y = y;
						p.plusA(center);
						moveBendTo(ge, k++, p.x, p.y);
					}
				}
			}
		}

		getGraph().getUndoManager().closeSubtask("Skew Graph");
		setRedraw(tmpRedraw);
	}

	@Override
	public JJPoint getGraphCenter() {
		return getGraphCenter(false);
	}

	public JJPoint getGraphCenter(final boolean selectedOnly) {
		if (centerMode == BARRY_CENTER) {
			return computeBarryCenter(selectedOnly);
		} else {
			return computeBoundingBoxCenter(selectedOnly);
		}
	}

	public JJPoint computeBoundingBoxCenter(final boolean selectedOnly) {
		final JJRectangle bbox = new JJRectangle(boundingBox(selectedOnly));

		final JJPoint center = new JJPoint(((bbox.x + bbox.width / 2) - xOffset) / zoom,
				((bbox.y + bbox.height / 2) - yOffset) / zoom);
		return center;
	}

	public JJPoint computeBarryCenter(final boolean selectedOnly) {
		final JJPoint centerNew = new JJPoint();
		int numNew = 0;

		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			final JJNode tmpN = iter.next();
			final JJGraphicNode gn = tmpN.getGraphicNode(this);
			if ((!selectedOnly) || gn.isSelected()) {
				if (gn.isVisible()) {
					centerNew.plusA(gn.getCoords());
					numNew++;
				}
			}
		}

		if (numNew != 0)
			centerNew.divA(numNew);
		return centerNew;
	}

	public void selectNodes(final Collection<JJGraphicNode> c) {
		if ((c == null) || (c.size() == 0))
			return;

		deselectNodes();
		deselectEdge();

		for (final JJGraphicNode gn : c) {
			if (gn.isVisible()) {
				select(gn);
			}
		}
	}

	@Override
	public void setTitle(final String t) {
		fenster.setTitle(t);
	}

	@Override
	public String getTitle() {
		return fenster.getTitle();
	}

	public void openSubtask(final String s) {
		graph.openSubtask(s);
	}

	public void closeSubtask(final String s) {
		graph.closeSubtask(s);
	}

	public void selectFont() {
		final Font f = JJFontSelectorBox.showDialog(getFont(), fenster);
		if (f != null) {
			setFont(f);
			_repaint();
		}
	}

	public void zoomTo(final Rectangle rect) {
		final boolean tmpR = getRedraw();
		setRedraw(false);

		fenster.centerOn(rect.x + (rect.width / 2), rect.y + (rect.height / 2));

		final JJRectangle bounds = new JJRectangle(getVisibleRect());
		final double scale = Math.min((bounds.getWidth()) / rect.width, (bounds.getHeight()) / rect.height);

		zoomTo(zoom * scale);
		setRedraw(tmpR);
	}

	Image masterImage;
	URL masterURL;

	public URL getMasterImageURL() {
		return masterURL;
	}

	public void setMasterImageURL(final URL v) {
		masterURL = v;
	}

	public Image getMasterImage() {
		return masterImage;
	}

	public void setMasterImage(final Image v) {
		masterImage = v;
	}

}
