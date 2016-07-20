/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.francois;

import java.util.Collection;
import java.util.Iterator;

import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJNode;
import org.carsten.jjgraph.layopt.JJLayOpt;
import org.carsten.jjgraph.layout.JJLayout;

/** an abstract class for spring like algorithms */
abstract public class Spring implements Runnable, JJLayout {
	JJGraph graph;
	JJGraphWindow window;

	int numNodes;
	double dampening = 1.0;

	boolean suspended = false;

	public Spring(final JJGraphWindow g) {
		window = g;
		graph = g.getGraph();
	}

	public JJGraph getGraph() {
		return graph;
	}

	JJFRNode[] nodeInfo = null;

	@Override
	public int allowsOptimize() {
		return JJLayOpt.TRANSLATE | JJLayOpt.SCALE_PROPORTIONAL | JJLayOpt.FLIP | JJLayOpt.ROTATE;
	}

	public JJFRNode getNodeInfo(final int l) {
		return nodeInfo[l];
	}

	void initNodes() {
		numNodes = graph.getNumNodes();
		nodeInfo = new JJFRNode[numNodes];
		int i = 0;

		for (final Iterator<JJNode> ne = getGraph().nodeIterator(); ne.hasNext();) {
			final JJNode n = ne.next();
			nodeInfo[i] = new JJFRNode(n.getGraphicNode(window));
			n.setValue(i++);
		}
	}

	/** reset all forces i.e =0 for each node */
	protected void initForces() {
		int i = 0;

		for (i = 0; i < numNodes; i++) {
			nodeInfo[i].force.dx = 0;
			nodeInfo[i].force.dy = 0;
		}
	}

	/** apply force to each node */
	protected void commitForces() {
		for (int i = 0; i < nodeInfo.length; i++) {
			// if(Math.abs(nodeInfo[i].force.dx) > 100.0 ||
			// Math.abs(nodeInfo[i].force.dy) > 100.0) {
			// System.out.println("FORCE = <" + nodeInfo[i].force.dx + ", " +
			// nodeInfo[i].force.dy + ">");
			// }
			nodeInfo[i].pos.x += nodeInfo[i].force.dx * dampening;
			nodeInfo[i].pos.y += nodeInfo[i].force.dy * dampening;
		}
	}

	protected void writeNodePos() {
		for (final JJFRNode element : nodeInfo) {
			window.moveNodeTo(element.node, element.pos);
		}
	}

	/** one iteration step */
	abstract public void step();

	/** start of iteration */
	abstract protected void initStep();

	abstract protected void nextStep();

	abstract protected boolean hasNextStep();

	public void doRun() {
		initNodes();
		initStep();
		while (hasNextStep()) {
			step();
			nextStep();
		}
		writeNodePos();
		nodeInfo = null;
	}

	@Override
	public void run() {
		initNodes();
		initStep();
		while (hasNextStep() && !suspended) {
			step();
			nextStep();
			writeNodePos();
		}

		nodeInfo = null;
	}

	@Override
	public void layout() {
		run();
	}

	@Override
	public void layout(final Collection<JJNode> c) {
		run();
	}

	public synchronized void start() {
		final Thread relaxer = new Thread(this);
		relaxer.setPriority(Thread.MIN_PRIORITY);
		relaxer.setName("Francois layout");
		relaxer.start();
	}

	public synchronized void stop() {
		suspended = true;
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
