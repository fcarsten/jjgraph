/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.layout;

/**
 * JJTutte.java
 *
 *
 * Created: Wed Dec  8 13:15:30 1999
 *
 * @author $Author: carsten $
 * @version $Revision: 1.3 $ $Date: 2003/02/04 00:22:32 $
 *
 * $Log: JJTutte.java,v $
 * Revision 1.3  2003/02/04 00:22:32  carsten
 * Changed _isVisible to isVisible
 *
 * Revision 1.2  2002/07/31 07:07:44  carsten
 * Checking for valid coordinates before computing forces
 *
 */
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.carsten.jjgraph.animation.JJAnimateable;
import org.carsten.jjgraph.animation.JJAnimationNode;
import org.carsten.jjgraph.animation.JJGraphAnimationNode;
import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJGraphicNode;
import org.carsten.jjgraph.graph.JJNode;

import Jama.Matrix;

public class JJTutte implements JJAnimateable, JJLayout {
	private final JJGraph graph;
	private final JJGraphWindow fenster;
	protected JJTutteNode tutteNodes[];
	// private JJNode graphNodes[];
	JJAnimationNode[] animationNodes;

	protected int numNodes = 0;

	public final static double MINDIST = 50.0;

	public JJTutte(final JJGraphWindow f) {
		fenster = f;
		graph = fenster.getGraph();
	}

	@Override
	public int allowsOptimize() {
		return 0;
	}

	@Override
	public JJAnimationNode[] generateAnimationInfo() {
		// int numNodes = graph.getNumNodes();
		// graph.numberNodes (0);

		final Collection<JJGraphicNode> fixedNodes = fenster.getSelectedNodes();

		if (do_tutte(fixedNodes)) {
			animationNodes = new JJAnimationNode[numNodes];

			for (int i = 0; i < numNodes; i++) {
				animationNodes[i] = new JJGraphAnimationNode(tutteNodes[i].node.getGraphicNode(fenster),
						fenster.getBackground());
				animationNodes[i].setPosition(tutteNodes[i].newPos);
			}
			return animationNodes;

		}
		return null;

	}

	@Override
	public void layout() {
		layoutWithFixedNodes(fenster.getSelectedNodes());
	}

	public void layoutWithFixedNodes(final Collection<JJGraphicNode> locNodes) {
		if (locNodes.size() < 3) {
			JOptionPane.showMessageDialog(null, "Select >= 3 nodes of the outer face", "alert",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (do_tutte(locNodes))
			moveNodes();
	}

	void initNodes(final Collection<JJGraphicNode> fixedNodes) {
		graph.setNodeValue(-1);
		numNodes = graph.getNumNodes() - fixedNodes.size();

		tutteNodes = new JJTutteNode[numNodes];
		// graphNodes = new JJNode[numNodes];
		int i = 0;

		for (final Iterator<JJNode> nodeIter = graph.nodeIterator(); nodeIter.hasNext();) {
			final JJNode tmpN = nodeIter.next();
			if (!fixedNodes.contains(tmpN.getGraphicNode(fenster))) {
				// JJGraphicNode tmpGN = tmpN.getGraphicNode();
				final JJTutteNode tutteN = new JJTutteNode(tmpN);
				tutteN.fixed = false;

				// tutteN.weight = 0;

				tutteNodes[i] = tutteN;
				tmpN.setValue(i++);
			}
		}

		// for(Iterator iter = fixedNodes.iterator(); iter.hasNext();){
		// JJNode v= (JJNode) iter.next();
		// tutteNodes[v.getValue()].fixed = true;
		// }
	}

	boolean do_tutte(final Collection<JJGraphicNode> fixedNodes) {

		// does the actual computation. fixed_nodes and fixed_positions
		// contain the nodes with fixed positions.
		// Debug.println( "Doing do_tutte");

		if (fixedNodes.size() == graph.getNumNodes())
			return false;

		// all nodes have fixed positions - nothing left to do

		initNodes(fixedNodes);

		Matrix coord = null; // = new Matrix(n,1);

		// coordinates (first x then y)

		final Matrix rhs = new Matrix(numNodes, 1); // right hand side
		final Matrix A = new Matrix(numNodes, numNodes); // equations

		for (int i = 0; i < numNodes; i++) {
			final JJNode v = tutteNodes[i].node;
			final double one_over_d = 1.0 / (v.deg());

			for (final Iterator<JJEdge> iter2 = v.outIterator(); iter2.hasNext();) {
				final JJEdge e = iter2.next();
				final JJNode w = e.getTarget(); // (v == source(e)) ? target(e)
												// : source(e);

				if (w.getValue() != -1) { // not fixed
					A.set(v.getValue(), w.getValue(), one_over_d);
				}
			}

			for (final Iterator<JJEdge> iter2 = v.inIterator(); iter2.hasNext();) {
				final JJEdge e = iter2.next();
				final JJNode w = e.getSource(); // (v == source(e)) ? target(e)
												// : source(e);

				if (w.getValue() != -1) { // not fixed
					A.set(v.getValue(), w.getValue(), one_over_d);
				}
			}

			A.set(v.getValue(), v.getValue(), -1);
		}

		if (A.det() == 0)
			return false;

		// compute right hand side for x coordinates
		for (int i1 = 0; i1 < numNodes; i1++) {
			final JJNode v = tutteNodes[i1].node;

			rhs.set(i1, 0, 0.0);
			// double one_over_d = 1.0/double(G.degree(v));
			final double one_over_d = 1.0 / (v.deg());

			for (final Iterator<JJEdge> iter2 = v.outIterator(); iter2.hasNext();) {
				final JJEdge e = iter2.next();
				final JJNode w = e.getTarget(); // (v == source(e)) ? target(e)
												// : source(e);
				if (w.getValue() == -1) {
					rhs.set(i1, 0, rhs.get(i1, 0) - (one_over_d * w.getGraphicNode(fenster).getX()));
				}
			}

			for (final Iterator<JJEdge> iter2 = v.inIterator(); iter2.hasNext();) {
				final JJEdge e = iter2.next();
				final JJNode w = e.getSource(); // (v == source(e)) ? target(e)
												// : source(e);

				if (w.getValue() == -1) {
					rhs.set(i1, 0, rhs.get(i1, 0) - (one_over_d * w.getGraphicNode(fenster).getX()));
				}
			}
		}

		// compute x coordinates
		coord = A.solve(rhs);

		for (int i = 0; i < numNodes; i++) {
			final JJNode v = tutteNodes[i].node;
			tutteNodes[i].newPos.setX(coord.get(v.getValue(), 0));
		}

		// compute right hand side for y coordinates
		for (int i1 = 0; i1 < numNodes; i1++) {
			final JJNode v = tutteNodes[i1].node;

			rhs.set(i1, 0, 0);

			final double one_over_d = 1.0 / (v.deg());

			for (final Iterator<JJEdge> iter2 = v.outIterator(); iter2.hasNext();) {
				final JJEdge e = iter2.next();
				final JJNode w = e.getTarget(); // (v == source(e)) ? target(e)
												// : source(e);

				if (w.getValue() == -1) {
					rhs.set(i1, 0, rhs.get(i1, 0) - (one_over_d * w.getGraphicNode(fenster).getY()));
				}
			}

			for (final Iterator<JJEdge> iter2 = v.inIterator(); iter2.hasNext();) {
				final JJEdge e = iter2.next();
				final JJNode w = e.getSource(); // (v == source(e)) ? target(e)
												// : source(e);

				if (w.getValue() == -1) {
					rhs.set(i1, 0, rhs.get(i1, 0) - (one_over_d * w.getGraphicNode(fenster).getY()));
				}
			}
		}

		// compute x coordinates
		coord = A.solve(rhs);

		for (int i = 0; i < numNodes; i++) {
			final JJNode v = tutteNodes[i].node;
			tutteNodes[i].newPos.setY(coord.get(i, 0));
		}

		return true;
	}

	public void moveNodes() {
		for (int i = 0; i < numNodes; i++) {
			fenster.moveNodeTo(tutteNodes[i].node.getGraphicNode(fenster), tutteNodes[i].newPos);
		}
	}

	@Override
	public String getName() {
		return "Tutte";
	}

	@Override
	public void apply(final String s) {
		layout();
	}

	@Override
	public boolean canDo(final String s) {
		return s.equals(getName());

	}

	@Override
	public void layout(final Collection<JJNode> c) {
		layout();
	}

} // JJTutte
