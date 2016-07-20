/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.layout;

/**
 * JJEigenLayout.java
 *
 *
 * Created: Wed Feb 16 16:32:29 2000
 *
 * @author Carsten Friedrich
 * @version
 *
 */

//  import javax.swing.*;
//  import java.awt.*;
//  import java.awt.event.*;
//  import java.awt.geom.*;

import java.util.Collection;
// import java.util.LinkedList;
import java.util.Iterator;

import org.carsten.jjgraph.clustering.JJEigenBipart;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJGraphicNode;
import org.carsten.jjgraph.graph.JJNode;
import org.carsten.jjgraph.layopt.JJLayOpt;
import org.carsten.jjgraph.util.Debug;

import Jama.Matrix;

//  public class JJEigenNode
//  {
//   JJNode node;
//   int value = -1;
//  }

public class JJEigenLayout implements JJLayout {

	private final JJGraphWindow fenster;
	protected JJGraph graph;

	private JJNode eigenLayoutN[];

	@Override
	public int allowsOptimize() {
		return JJLayOpt.TRANSLATE | JJLayOpt.SCALE_PROPORTIONAL | JJLayOpt.FLIP | JJLayOpt.ROTATE;
	}

	private boolean needsInit = true;
	private Matrix em;

	private int x = 1;
	private int y = 2;
	private int z = 3;

	public int getDimensions() {
		return eigenLayoutN.length - 1;
	}

	/**
	 * Get the value of needsInit.
	 *
	 * @return Value of needsInit.
	 */
	public boolean getNeedsInit() {
		return needsInit;
	}

	/**
	 * Set the value of needsInit.
	 *
	 * @param v
	 *            Value to assign to needsInit.
	 */
	public void setNeedsInit(final boolean v) {
		this.needsInit = v;
	}

	/**
	 * Get the value of z.
	 *
	 * @return Value of z.
	 */
	public int getZ() {
		return z;
	}

	/**
	 * Set the value of z.
	 *
	 * @param v
	 *            Value to assign to z.
	 */
	public void setZ(final int v) {
		this.z = v;
	}

	/**
	 * Get the value of y.
	 *
	 * @return Value of y.
	 */
	public int getY() {
		return y;
	}

	/**
	 * Set the value of y.
	 *
	 * @param v
	 *            Value to assign to y.
	 */
	public void setY(final int v) {
		this.y = v;
	}

	/**
	 * Get the value of x.
	 *
	 * @return Value of x.
	 */
	public int getX() {
		return x;
	}

	/**
	 * Set the value of x.
	 *
	 * @param v
	 *            Value to assign to x.
	 */
	public void setX(final int v) {
		this.x = v;
	}

	public JJEigenLayout(final JJGraphWindow f) {
		fenster = f;
		graph = fenster.getGraph();
	}

	@Override
	public void layout() {
		if (needsInit)
			initNodes();

		eigenLayout();
	}

	@Override
	public void layout(final Collection<JJNode> locNodes) {
		if (needsInit)
			initNodes();

		eigenLayout();
	}

	private int matrixMode = JJEigenBipart.LAPLACE;

	/**
	 * Get the value of matrixMode.
	 *
	 * @return Value of matrixMode.
	 */
	public int getMatrixMode() {
		return matrixMode;
	}

	/**
	 * Set the value of matrixMode.
	 *
	 * @param v
	 *            Value to assign to matrixMode.
	 */
	public void setMatrixMode(final int v) {
		this.matrixMode = v;
	}

	void initNodes() {
		final JJEigenBipart eigen = new JJEigenBipart(graph, matrixMode, null);
		eigen.cluster();
		eigenLayoutN = new JJNode[graph.getNumNodes()];
		em = eigen.getMatrix();

		for (final Iterator<JJNode> nodeIter = graph.nodeIterator(); nodeIter.hasNext();) {
			final JJNode tmpN = nodeIter.next();

			eigenLayoutN[tmpN.getValue()] = tmpN;
		}
		needsInit = false;
	}

	@Override
	public String getName() {
		return "Spectral";
	}

	void eigenLayout() {
		final boolean tmpRedraw = fenster.setRedraw(false);

		for (int i = 0; i < eigenLayoutN.length; i++) {
			final JJNode tmpN = eigenLayoutN[i]; // (JJNode) iter.next ();
			final int num = i; // eigenLayoutN[i].node.value; //
								// tmpN.getValue();

			final JJGraphicNode gn = tmpN.getGraphicNode(fenster);

			double ix = gn.getX();
			double iy = gn.getY();
			double iz = gn.getZ();

			if (x > -1) {
				try {
					ix = 500 * em.get(num, x);
				} catch (final ArrayIndexOutOfBoundsException e) {
					Debug.println("Index " + x + " out of range!");
					return;
				}
			}
			if (y > -1) {
				try {
					iy = 500 * em.get(num, y);
				} catch (final ArrayIndexOutOfBoundsException e) {
					Debug.println("Index " + y + " out of range!");
					return;
				}
			}
			if (z > -1) {
				try {
					iz = 500 * em.get(num, z);
				} catch (final ArrayIndexOutOfBoundsException e) {
					Debug.println("Index " + z + " out of range!");
					return;
				}
			}
			fenster.moveNodeTo(tmpN.getGraphicNode(fenster), ix, iy, iz);
		}
		fenster.setRedraw(tmpRedraw);
	}

	@Override
	public void apply(final String s) {
		layout();
	}

	@Override
	public boolean canDo(final String s) {
		return s.equals(getName());

	}

}
