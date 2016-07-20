/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.util.Collection;
import java.util.LinkedList;

/**
 * JJGraphWindowImpl.java
 *
 *
 * Created: Thu May 04 09:46:51 2000
 *
 * @author
 * @version
 */
import javax.swing.JPanel;

import org.carsten.jjgraph.util.JJPoint;

public abstract class JJGraphWindow extends JPanel implements ActionListener {
	final static int MIN_X = 50;
	final static int MIN_Y = 50;
	final static double FANG_RADIUS = 5.0;

	public final static int LABEL_NONE = 0;
	public final static int LABEL_TOP = 1;
	public final static int LABEL_BOTTOM = 2;
	public final static int LABEL_INSIDE = 3;
	public final static int LABEL_LEFT = 4;
	public final static int LABEL_RIGHT = 5;
	private boolean appearanceDirty = false;
	private java.util.List<JJGraphWindowListener> appearanceListeners;

	private int visibilityIndex = 0;
	public int HIDDEN = nextVisibilityIndex();

	protected int nextVisibilityIndex() {
		return ++visibilityIndex;
	}

	public int getNodeLabelPosition() {
		return LABEL_NONE;
	}

	public int getEdgeLabelPosition() {
		return LABEL_NONE;
	}

	abstract public void showNodes(boolean flag);

	abstract public void printError(String s);

	abstract public void printNote(String s);

	abstract public void printWarning(String s);

	abstract public Collection<JJGraphicNode> getSelectedNodes();

	abstract public JJGraphicEdge getSelectedEdge();

	abstract public JJGraph getGraph();

	abstract public boolean getRedraw();

	abstract public boolean setRedraw(boolean v);

	abstract public JJGraphFrame getFrame();

	public void applyLayout(final String s) {
		getFrame().applyLayout(s);
	}

	// abstract public boolean isVisible(int cluster);
	// abstract public void showCluster(int c);
	// abstract public void showAllClusters();
	// abstract public void hideCluster(int c);
	abstract public void initPopupMenu();

	// abstract public void showOnly(int cluster);
	// abstract public void actionPerformed(ActionEvent e);
	abstract public void findGraph();

	abstract public void setBusy(boolean flag);

	abstract protected void deleteNode(JJNode knoten);

	abstract public JJGraphicNode addNode(JJNode knoten);

	abstract public void deleteEdge(JJEdge kante);

	abstract public JJEdge addEdge(JJEdge edge);

	abstract public void repaint(JJGraphicEdge e);

	abstract public void repaint(JJGraphicNode n);

	abstract public void paintDirty();

	abstract public void selectNodesInRectangle(Rectangle rect);

	abstract public JJGraphicNode findNodeAt(JJPoint p);

	abstract public JJGraphicEdge findEdgeAt(JJPoint p);

	abstract public void addNewElementAt(JJPoint p);

	abstract public void moveNodeTo(JJGraphicNode n, JJPoint p);

	abstract public void moveNodeTo(JJGraphicNode n, double x, double y);

	abstract public void moveNodeTo(JJGraphicNode n, double x, double y, double z);

	abstract public void rmoveNodeTo(JJGraphicNode n, double x, double y);

	abstract public void rmoveNodeTo(JJGraphicNode n, double x, double y, double z);

	abstract public void rmoveEdgeTo(JJGraphicEdge e, double x, double y);

	abstract public void rmoveEdgeTo(JJGraphicEdge e, JJPoint p);

	abstract public void rmoveNodeTo(JJGraphicNode n, JJPoint p);

	abstract public void moveBendTo(JJGraphicEdge e, int i, double x, double y);

	abstract public int print(Graphics g, PageFormat pf, int pi) throws PrinterException;

	abstract public double getZoom();

	abstract public void zoomTo(double x);

	abstract public void removeBends();

	abstract public void show3D();

	abstract public void updateDirected(boolean v);

	// abstract public int getWidth(JJGraphicNode n);
	// abstract public int getHeight(JJGraphicNode n);
	abstract public void forceRepaintAll();

	abstract public void addTool(JJInspectable e);

	abstract public JJInspectable getTool(Class<? extends JJInspectable> c);

	abstract public void saveImage(String fileName); // save graph as pixel
														// image

	abstract public JJPoint getGraphCenter();

	abstract public void setTitle(String t);

	abstract public String getTitle();

	abstract public void select(JJGraphicNode gn);

	public boolean getOutline() {
		return false;
	}

	public double deviceAdjustX(final double x) {
		return x;
	}

	public double deviceAdjustWidth(final double x) {
		return x;
	}

	public double deviceAdjustHeight(final double x) {
		return x;
	}

	public double deviceAdjustY(final double x) {
		return x;
	}

	public Rectangle deviceAdjust(final Rectangle x) {
		return x;
	}

	abstract public Rectangle getVisibleBounds();

	public String adjustLabel(final String s) {
		return s;
	}

	abstract public Rectangle getBounds(JJGraphicNode n);

	abstract public Rectangle getBounds(JJGraphicEdge n);

	public void addAppearanceListener(final JJGraphWindowListener e) {
		if (appearanceListeners == null)
			appearanceListeners = new LinkedList<>();
		appearanceListeners.add(e);

	}

	public void removeAppearanceListener(final JJGraphWindowListener e) {
		if (appearanceListeners == null)
			return;

		appearanceListeners.remove(e);
	}

	public void sendGraphAppearanceEvent() {
		final JJGraphEvent e = new JJGraphEvent(null, JJGraphEvent.MISC_EVENT);
		sendGraphAppearanceEvent(e);
	}

	public synchronized void sendGraphAppearanceEvent(final JJGraphEvent e) {
		appearanceDirty = true;
		if (appearanceListeners == null)
			return;
		for (final JJGraphWindowListener jjGraphWindowListener : appearanceListeners) {
			jjGraphWindowListener.graphAppearanceChanged(e);
		}
	}

	public boolean isDrawn(final JJGraphicEdge e) {
		return true;
	}

	public boolean isDrawn(final JJGraphicNode n) {
		return true;
	}

	abstract public boolean isDrawSplines();

	abstract public void sendEdgeColourChangeEvent(JJGraphicEdge edge);

	abstract public void sendEdgeVisibilityChangeEvent(JJGraphicEdge edge);

}
