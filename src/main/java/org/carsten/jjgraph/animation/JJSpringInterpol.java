/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

import java.awt.Color;
/**
 * JJSpringInterpol.java
 *
 *
 * Created: Wed Feb 16 16:32:29 2000
 *
 * @author Carsten Friedrich
 * @version
 *
 */
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphImpl;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJNode;
import org.carsten.jjgraph.util.Debug;
import org.carsten.jjgraph.util.Edge;
import org.carsten.jjgraph.util.GFMatrix;
import org.carsten.jjgraph.util.JJDelaunay;
import org.carsten.jjgraph.util.JJPoint;

public class JJSpringInterpol {
	protected JJAnimationNode animationNodes[];
	protected JJGraphAnimator graphAnimator;
	protected JJGraph springGraph;
	protected JJSpringInterpolNode springNodes[];
	protected JJGraphWindow springWindow;

	protected static double interEdgeWeight = 0.7;
	protected static double intraEdgeWeight = 1.0;
	protected static double delaunayEdgeWeight = 1.0;
	protected static double centerEdgeWeight = 1.0;
	protected JJPoint springPos[];

	protected int maxFrame = JJSpringInterpolNode.LEVELS + 1;

	private double angle;
	private double sMat[];
	private final double ctm[][] = new double[JJSpringInterpolNode.LEVELS][6];

	private double angleBack;
	private double sMatBack[];
	private final double ctmBack[][] = new double[JJSpringInterpolNode.LEVELS][6];

	void frame(final double d) {
		final int baseFrame = (int) (d * maxFrame);
		final double fraction = d * maxFrame - baseFrame;

		for (final JJSpringInterpolNode springNode : springNodes) {
			final JJPoint pos = new JJPoint(springNode.pos[baseFrame]);
			if (fraction > 0.00000000001) {
				final JJPoint pos2 = springNode.pos[baseFrame + 1];
				pos.x = pos.x * (1 - fraction) + pos2.x * (fraction);
				pos.y = pos.y * (1 - fraction) + pos2.y * (fraction);
			}

			springNode.setRealPosition(pos);
		}
	}

	public static double getInterEdgeWeight() {
		return interEdgeWeight;
	}

	public static void setInterEdgeWeight(final double v) {
		interEdgeWeight = v;
	}

	public static double getCenterEdgeWeight() {
		return centerEdgeWeight;
	}

	public static void setCenterEdgeWeight(final double v) {
		centerEdgeWeight = v;
	}

	public static double getDelaunayEdgeWeight() {
		return delaunayEdgeWeight;
	}

	public static void setDelaunayEdgeWeight(final double v) {
		delaunayEdgeWeight = v;
	}

	public static double getIntraEdgeWeight() {
		return intraEdgeWeight;
	}

	public static void setIntraEdgeWeight(final double v) {
		intraEdgeWeight = v;
	}

	public JJSpringInterpol(final JJGraphAnimator b) {
		graphAnimator = b;
	}

	public JJPoint getCoords(final JJNode n) {
		return springPos[n.getValue()];
	}

	public void addGraphEdges(final Map<JJAnimatedShape, JJSpringInterpolNode> shapeToNode) {
		// Adding graph edges

		final int LEVELS = JJSpringInterpolNode.LEVELS;

		for (final JJAnimationNode animationNode : animationNodes) {
			final JJMovingShape currentShape = animationNode.getMovingShape();
			final JJSpringInterpolNode sourceNode = shapeToNode.get(currentShape);

			for (final Iterator<JJEdge> iter = currentShape.outIterator(); iter.hasNext();) {
				final JJAnimatedShape target = currentShape.opposite((iter.next()));
				final JJSpringInterpolNode targetNode = shapeToNode.get(target);
				if (targetNode != null) { // Otherwise the target node is not
											// moving, i.e.
											// Invisible at the first or last
											// frame
					final double startLength = JJPoint.dist(getCoords(sourceNode.startNode),
							getCoords(targetNode.startNode));
					final double endLength = JJPoint.dist(getCoords(sourceNode.endNode), getCoords(targetNode.endNode));

					for (int k = 0; k < LEVELS; k++) {
						final JJEdge tmpE = springGraph.addEdge(sourceNode.middleNode[k], targetNode.middleNode[k]);
						if (springWindow != null)
							tmpE.getGraphicEdge(springWindow).setColor(new Color(25 + (200 / (LEVELS + 2)) * (k + 1),
									25 + (200 / (LEVELS + 2)) * (k + 1), 25 + (200 / (LEVELS + 2)) * (k + 1)));

						tmpE.setLength(endLength * (k + 1) / (LEVELS + 1)
								+ startLength * ((LEVELS + 1) - (k + 1)) / (LEVELS + 1));

						tmpE.setWeight(intraEdgeWeight * computeModifier(startLength, endLength));
					}
				}
			}
		}
	}

	double computeModifier(final double startLength, final double endLength) {
		double mod = 0;

		if (endLength * startLength != 0) {
			mod = startLength / endLength;
			if (mod > 1)
				mod = 1 / mod;
		}
		return mod;
	}

	public void addInterPlaneEdges(final JJSpringInterpolNode node) {
		JJEdge tmpE = springGraph.addEdge(node.startNode, node.middleNode[0]);
		final double startLength = JJPoint.dist(node.startPos, node.pos[0]);
		tmpE.setLength(startLength);
		tmpE.setWeight(interEdgeWeight);
		if (springWindow != null)
			tmpE.getGraphicEdge(springWindow).setColor(new Color(100, 250, 100));
		// tmpE.setName(""+tmpE.getLength());

		final int LEVELS = JJSpringInterpolNode.LEVELS;

		for (int i = 1; i < LEVELS; i++) {
			tmpE = springGraph.addEdge(node.middleNode[i - 1], node.middleNode[i]);
			tmpE.setLength(JJPoint.dist(node.pos[i - 1], node.pos[i]));
			// tmpE.setName(""+tmpE.getLength());
			tmpE.setWeight(interEdgeWeight);
			if (springWindow != null)
				tmpE.getGraphicEdge(springWindow)
						.setColor(new Color(100, 250 - (150 / (LEVELS + 2)) * i, 100 + (150 / (LEVELS + 2)) * i));
		}

		tmpE = springGraph.addEdge(node.endNode, node.middleNode[LEVELS - 1]);
		final double endLength = JJPoint.dist(node.endPos, node.pos[LEVELS - 1]);
		tmpE.setLength(endLength);
		tmpE.setWeight(interEdgeWeight);
		if (springWindow != null)
			tmpE.getGraphicEdge(springWindow).setColor(new Color(100, 100, 250));
	}

	JJSpringInterpolNode createInterpolNode(final JJAnimationNode node) {
		final JJSpringInterpolNode springNode = new JJSpringInterpolNode(springGraph, node, ctm, ctmBack, springWindow);
		addInterPlaneEdges(springNode);
		return springNode;
	}

	//
	// Thes following nodes are not affected by spring embedder
	//

	JJSpringInterpolNode createInterpolNode(final JJPoint s, final JJPoint e) {
		final JJSpringInterpolNode node = new JJSpringInterpolNode(springGraph, s, e, ctm, ctmBack, springWindow);
		return node;
	}

	protected void initNodes(final JJPoint ac) {
		springNodes = new JJSpringInterpolNode[animationNodes.length];

		final JJPoint startFramePoints[] = new JJPoint[animationNodes.length];
		final JJPoint endFramePoints[] = new JJPoint[animationNodes.length];

		// build graph here
		springGraph = new JJGraphImpl();
		// springWindow = springGraph.createGraphic();
		//
		final Map<JJAnimatedShape, JJSpringInterpolNode> shapeToNode = new HashMap<>();

		// Adding nodes

		final JJPoint startCentre = new JJPoint();
		final JJPoint endCentre = new JJPoint();
		for (int i = 0; i < animationNodes.length; i++) {
			startFramePoints[i] = animationNodes[i].getStartPosition();
			endFramePoints[i] = animationNodes[i].getEndPosition();
			startCentre.plusA(startFramePoints[i]);
			endCentre.plusA(endFramePoints[i]);
		}
		startCentre.divA(startFramePoints.length);
		endCentre.divA(endFramePoints.length);

		createMatrices(ac);

		for (int i = 0; i < animationNodes.length; i++) {
			springNodes[i] = createInterpolNode(animationNodes[i]);
			shapeToNode.put(animationNodes[i].getAnimatedShape(), springNodes[i]);
		}

		JJSpringInterpolNode centreNode = null;

		if (centerEdgeWeight > 0) {
			centreNode = createInterpolNode(startCentre, endCentre);
		}

		springPos = new JJPoint[springGraph.getNumNodes()];
		final int LEVELS = JJSpringInterpolNode.LEVELS;
		int k = 0;

		for (final JJSpringInterpolNode springNode : springNodes) {
			springPos[k] = new JJPoint(springNode.startPos);
			springNode.startNode.setValue(k++);

			for (int j = 0; j < springNode.pos.length; j++) {
				springPos[k] = new JJPoint(springNode.pos[j]);
				springNode.middleNode[j].setValue(k++);
			}

			springPos[k] = new JJPoint(springNode.endPos);
			springNode.endNode.setValue(k++);
		}

		addGraphEdges(shapeToNode);

		// Adding centre to springPos
		if (centreNode != null) {
			springPos[k] = centreNode.startPos;
			centreNode.startNode.setValue(k++);
			for (int j = 0; j < centreNode.pos.length; j++) {
				springPos[k] = new JJPoint(centreNode.pos[j]);
				centreNode.middleNode[j].setValue(k++);
			}
			springPos[k] = new JJPoint(centreNode.endPos);
			centreNode.endNode.setValue(k++);

			// Adding Edges to centre node
			for (final JJAnimationNode animationNode : animationNodes) {
				final JJAnimatedShape currentShape = animationNode.getAnimatedShape();
				final JJSpringInterpolNode sourceNode = shapeToNode.get(currentShape);

				final double startLength = JJPoint.dist(getCoords(sourceNode.startNode), centreNode.startPos);
				final double endLength = JJPoint.dist(getCoords(sourceNode.endNode), centreNode.endPos);
				for (k = 0; k < LEVELS; k++) {
					final JJEdge tmpE = springGraph.addEdge(sourceNode.middleNode[k], centreNode.middleNode[k]);
					if (springWindow != null)
						tmpE.getGraphicEdge(springWindow).setColor(new Color(25 + (200 / (LEVELS + 2)) * (k + 1),
								25 + (200 / (LEVELS + 2)) * (k + 1), 25 + (200 / (LEVELS + 2)) * (k + 1)));

					tmpE.setLength(
							endLength * (k + 1) / (LEVELS + 1) + startLength * ((LEVELS + 1) - (k + 1)) / (LEVELS + 1));

					tmpE.setWeight(centerEdgeWeight * computeModifier(startLength, endLength));
				}
			}
		}

		// Adding Delaunay edges
		if (delaunayEdgeWeight > 0) {
			final JJDelaunay dStart = new JJDelaunay(startFramePoints);

			final Set<Edge> delEdges = dStart.doDelaunay();

			final JJDelaunay dFinal = new JJDelaunay(endFramePoints);
			dFinal.doDelaunay(delEdges);

			for (final Edge edge : delEdges) {
				final JJSpringInterpolNode sourceNode = springNodes[edge.a];
				final JJSpringInterpolNode targetNode = springNodes[edge.b];

				for (k = 0; k < LEVELS; k++) {
					final JJEdge tmpE = springGraph.addEdge(sourceNode.middleNode[k], targetNode.middleNode[k]);
					if (springWindow != null)
						tmpE.getGraphicEdge(springWindow).setColor(new Color(25 + (200 / (LEVELS + 2)) * (k + 1),
								25 + (100 / (LEVELS + 2)) * (k + 1), 25 + (100 / (LEVELS + 2)) * (k + 1)));
					final double startLength = JJPoint.dist(getCoords(sourceNode.startNode),
							getCoords(targetNode.startNode));
					final double endLength = JJPoint.dist(getCoords(sourceNode.endNode), getCoords(targetNode.endNode));

					tmpE.setLength(
							endLength * (k + 1) / (LEVELS + 1) + startLength * ((LEVELS + 1) - (k + 1)) / (LEVELS + 1));

					tmpE.setWeight(delaunayEdgeWeight * computeModifier(startLength, endLength));
				}
			}

			//
			// Adding edge between other nodes of ajacent triangle
			//

			for (final Object element : delEdges) {
				final Edge edge = (Edge) element;
				if ((edge.t1 != null) && (edge.t2 != null) && (edge.t1.opposite(edge) >= 0)
						&& (edge.t2.opposite(edge) >= 0)) {
					Debug.println("Adding triangle edge " + edge.t1.opposite(edge) + " - " + edge.t2.opposite(edge));

					final JJSpringInterpolNode sourceNode = springNodes[edge.t1.opposite(edge)];
					final JJSpringInterpolNode targetNode = springNodes[edge.t2.opposite(edge)];

					for (k = 0; k < LEVELS; k++) {
						final JJEdge tmpE = springGraph.addEdge(sourceNode.middleNode[k], targetNode.middleNode[k]);
						if (springWindow != null)
							tmpE.getGraphicEdge(springWindow).setColor(new Color(25 + (200 / (LEVELS + 2)) * (k + 1),
									25 + (100 / (LEVELS + 2)) * (k + 1), 25 + (100 / (LEVELS + 2)) * (k + 1)));
						final double startLength = JJPoint.dist(getCoords(sourceNode.startNode),
								getCoords(targetNode.startNode));
						final double endLength = JJPoint.dist(getCoords(sourceNode.endNode),
								getCoords(targetNode.endNode));

						tmpE.setLength(endLength * (k + 1) / (LEVELS + 1)
								+ startLength * ((LEVELS + 1) - (k + 1)) / (LEVELS + 1));

						tmpE.setWeight(delaunayEdgeWeight * computeModifier(startLength, endLength));
					}
				}
			}

		}
	}

	void resetCTMs() {
		for (int i = 0; i < JJSpringInterpolNode.LEVELS; i++) {
			ctm[i][0] = ctm[i][3] = 1;
			ctm[i][1] = ctm[i][2] = ctm[i][4] = ctm[i][5] = 0;
			ctmBack[i][0] = ctmBack[i][3] = 1;
			ctmBack[i][1] = ctmBack[i][2] = ctmBack[i][4] = ctmBack[i][5] = 0;
		}
	}

	void createMatrices(final JJPoint center) {
		final int LEVELS = JJSpringInterpolNode.LEVELS;

		final double source[] = { 1, 0, 0, 1, 0, 0 };
		double currentAngle = 0;
		final double mat[][] = new double[LEVELS][6];
		double currentAngleBack = 0;
		final double matBack[][] = new double[LEVELS][6];

		resetCTMs();

		for (int i = 0; i < LEVELS; i++) {
			currentAngle += angle / (LEVELS + 1);
			currentAngleBack += angleBack / (LEVELS + 1);

			for (int k = 0; k < 6; k++) {
				final double dist = sMat[k] - source[k];
				mat[i][k] = source[k] + (dist / (LEVELS + 1)) * (i + 1);

				final double distBack = sMatBack[k] - source[k];
				matBack[i][k] = source[k] + (distBack / (LEVELS + 1)) * (i + 1);
			}
			ctm[i][4] = -center.x;
			ctm[i][5] = -center.y;

			ctmBack[i][4] = -center.x;
			ctmBack[i][5] = -center.y;

			final double rm[] = new double[6];
			final double rmBack[] = new double[6];

			GFMatrix.rotatematrix(Math.toDegrees(currentAngle), rm);
			GFMatrix.concatmatrix(ctm[i], rm, ctm[i]);
			GFMatrix.concatmatrix(ctm[i], mat[i], ctm[i]);

			GFMatrix.rotatematrix(Math.toDegrees(currentAngleBack), rmBack);
			GFMatrix.concatmatrix(ctmBack[i], rmBack, ctmBack[i]);
			GFMatrix.concatmatrix(ctmBack[i], matBack[i], ctmBack[i]);

			ctm[i][4] += center.x;
			ctm[i][5] += center.y;

			ctmBack[i][4] += center.x;
			ctmBack[i][5] += center.y;
		}
	}

	void init(final JJAnimationNode bn[], final double q[], final double s[], final double qBack[],
			final double sBack[], final JJPoint ac) {
		// if((animationNodes == bn) || (bn == null))
		// return;

		animationNodes = bn;

		angle = Math.acos(q[0]);
		if (Math.abs(Math.sin(angle) - q[1]) > JJMatrixInterpol.EPSILON)
			angle = -angle;

		angle = Math.toDegrees(angle);

		while (angle > 180)
			angle -= 360;
		while (angle < -180)
			angle += 360;

		Debug.println(" QBack: [" + q[0] + "," + q[2] + "]");
		Debug.println(" QBack: [" + q[1] + "," + q[3] + "]");

		angleBack = Math.acos(qBack[0]);
		if (Math.abs(Math.sin(angleBack) - qBack[1]) > JJMatrixInterpol.EPSILON)
			angleBack = -angleBack;

		angleBack = Math.toDegrees(angleBack);

		// Lets make sure we rotate in the same direction

		while (angleBack > 180)
			angleBack -= 360;
		while (angleBack < -180)
			angleBack += 360;

		if ((angleBack > 170) && (angle > 170)) {
			angleBack -= 360;
		} else if ((angleBack < -170) && (angle < -170)) {
			angleBack += 360;
		}

		angle = Math.toRadians(angle);
		angleBack = Math.toRadians(angleBack);

		Debug.println(" angle " + Math.toDegrees(angle));
		Debug.println(" backAngle " + Math.toDegrees(angleBack));

		sMat = s;
		sMatBack = sBack;

		initNodes(ac);
		springInit();
		springDo();
		reset();
	}

	void reset() {
	}

	protected double temperature = 0.0;

	protected double finalTemp = 0.02;
	protected double maxIter = 3;

	protected int maxRounds;
	protected int numRounds;
	protected int minRounds = 10;

	protected double stopTemp;
	protected long startTime;

	protected long maxRealTime = 120 * 1000;
	protected int breakCounter = 0;

	void springInit() {

		temperature = 0;
		numRounds = 0;
		breakCounter = 0;

		final int numNodes = springNodes.length;

		maxRounds = (int) (maxIter * numNodes * numNodes);
		// Debug.println("Max rounds: " + maxRounds);

		stopTemp = finalTemp * finalTemp * 128 * 128 * numNodes;
		startTime = System.currentTimeMillis();
	}

	void springDo() {
		Debug.println("Springo do");
		if (springWindow != null)
			springWindow.setRedraw(false);
		while (nextSpringRound())
			;
		writeSpringPos();
		addFirstAndLastFrame();
		if (springWindow != null)
			springWindow.setRedraw(true);

		Debug.println("Spring done");
	}

	public void addFirstAndLastFrame() {
		for (final JJSpringInterpolNode springNode : springNodes) {
			springNode.addFirstAndLastFrame();
		}

	}

	public boolean nextSpringRound() {
		// Debug.println("Temp: " + temperature + " time: " +
		// (System.currentTimeMillis() - startTime));

		if (((maxRealTime > 0) && (System.currentTimeMillis() - startTime) > maxRealTime))
			return false;

		if ((numRounds > minRounds)
				&& (((temperature > 0) && (temperature <= stopTemp)) || (numRounds++ >= maxRounds))) {
			return false;
		}
		final double oldTemp = temperature;

		for (int k = 0; k < springNodes.length; k++) { // deterministic schedule
			final int next = k;

			for (int i = 0; i < JJSpringInterpolNode.LEVELS; i++)
				updatePos(springNodes[next], updateImpulse(springNodes[next], i), i);
		}

		if (oldTemp <= temperature)
			breakCounter += 2;
		else if (breakCounter > 0)
			breakCounter--;

		if (breakCounter > 50)
			return false;

		return true;
	}

	protected void writeSpringPos() {
		if (springWindow == null)
			return;

		for (final JJSpringInterpolNode springNode : springNodes) {
			for (int i = 0; i < JJSpringInterpolNode.LEVELS; i++) {
				springWindow.moveNodeTo(springNode.middleNode[i].getGraphicNode(springWindow), springNode.pos[i]);
			}
		}
	}

	void updatePos(final JJSpringInterpolNode v, final JJPoint i, final int index) {
		i.z = 0; // Confining to two dimensions

		v.pos[index].plusA(i);
		springPos[v.middleNode[index].getValue()].plusA(i);
	}

	JJPoint updateImpulse(final JJSpringInterpolNode v, final int index) {
		final JJPoint i = new JJPoint();

		temperature -= v.heat[index];
		v.heat[index] = 0;

		double divider = 0;

		for (final Iterator<JJEdge> iter = v.middleNode[index].edgeIterator(); iter.hasNext();) {
			// computing forces

			final JJEdge tmpE = iter.next();

			final JJNode k = tmpE.opposite(v.middleNode[index]);
			final JJPoint kPos = getCoords(k);

			final JJPoint d = JJPoint.minus(v.pos[index], kPos);
			double dist = JJPoint.dist(v.pos[index], kPos);

			double attract = tmpE.getLength() - dist;
			attract /= 2.0; // Half force as every edge counts at two nodes!

			// Debug.println("Attract: " + attract);

			v.heat[index] += Math.abs(attract);

			if (dist == 0)
				dist = 1;

			i.plusA(d.divA(dist).multA(attract * tmpE.getWeight()));
			divider += tmpE.getWeight();
		}

		if (divider != 0)
			i.divA(divider);

		temperature += v.heat[index];

		return i;
	}
}
