/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

/**
 * JJTreeBufferSpringInterpol.java
 *
 *
 * Created: Wed Feb 16 16:32:29 2000
 *
 * @author Carsten Friedrich
 * @version
 *
 */
import java.util.Iterator;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJNode;
import org.carsten.jjgraph.util.Debug;
import org.carsten.jjgraph.util.JJPoint;

public class JJTreeBufferSpringInterpol extends JJSpringInterpol {
	int leftParent[] = new int[JJSpringInterpolNode.LEVELS];
	int rightParent[] = new int[JJSpringInterpolNode.LEVELS];
	int leftSon[] = new int[JJSpringInterpolNode.LEVELS];
	int rightSon[] = new int[JJSpringInterpolNode.LEVELS];
	final static int STARTFRAME = -1;
	final static int ENDFRAME = -2;
	final static int INVALID = -3;

	int bufferRoot = INVALID;

	public JJTreeBufferSpringInterpol(final JJGraphAnimator b) {
		super(b);
	}

	int buildBufferTree(final int start, final int end, final int leftP, final int rightP) {
		final int middle = start + (end - start) / 2;
		leftParent[middle] = leftP;
		rightParent[middle] = rightP;

		if (start != end) {
			leftSon[middle] = buildBufferTree(start, middle - 1, leftP, middle);
			rightSon[middle] = buildBufferTree(middle + 1, end, middle, rightP);
		}

		return middle;
	}

	@Override
	public void initNodes(final JJPoint ac) {
		// Adding graph edges
		final int LEVELS = JJSpringInterpolNode.LEVELS;

		for (int k = 0; k < JJSpringInterpolNode.LEVELS; k++) {
			leftParent[k] = INVALID;
			rightParent[k] = INVALID;
			leftSon[k] = INVALID;
			rightSon[k] = INVALID;
		}

		bufferRoot = buildBufferTree(0, LEVELS - 1, STARTFRAME, ENDFRAME);

		// Some debug
		if (Debug.DEBUG) {
			Debug.println("" + leftParent);
			Debug.println("" + rightParent);
			Debug.println("" + leftSon);
			Debug.println("" + rightSon);
		}

		super.initNodes(ac);
	}

	@Override
	public void addInterPlaneEdges(final JJSpringInterpolNode node) {
	}

	@Override
	void springDo() {
		Debug.println("Springo do");
		if (springWindow != null)
			springWindow.setRedraw(false);

		computeSprings(bufferRoot);

		writeSpringPos();
		addFirstAndLastFrame();
		if (springWindow != null)
			springWindow.setRedraw(true);

		Debug.println("Spring done");
	}

	private int currentPlane = INVALID;
	private JJPoint shouldPos[];

	public void computeShouldPos() {
		if (shouldPos == null)
			shouldPos = new JJPoint[springNodes.length];

		JJPoint leftPos;
		JJPoint rightPos;

		for (int i = 0; i < springNodes.length; i++) {
			if (leftParent[currentPlane] == STARTFRAME)
				leftPos = springNodes[i].startPos;
			else
				leftPos = springNodes[i].pos[leftParent[currentPlane]];

			if (rightParent[currentPlane] == ENDFRAME)
				rightPos = springNodes[i].endPos;
			else
				rightPos = springNodes[i].pos[rightParent[currentPlane]];

			shouldPos[i] = JJPoint.plus(leftPos, rightPos).divA(2.0);
		}

	}

	private void computeSprings(final int plane) {
		if (plane == INVALID)
			return;

		Debug.println("Computing springs for plane " + plane);
		Debug.println(" with parents " + leftParent[plane] + " and " + rightParent[plane]);

		breakCounter = 0;
		startTime = System.currentTimeMillis();
		numRounds = 0;

		currentPlane = plane;
		computeShouldPos();

		while (nextSpringRound())
			;

		maxRealTime /= 2.0;

		computeSprings(leftSon[plane]);
		computeSprings(rightSon[plane]);

		maxRealTime *= 2.0;
	}

	@Override
	public boolean nextSpringRound() {
		Debug.println(
				"Temp: " + temperature + " time: " + (System.currentTimeMillis() - startTime) + " max: " + maxRealTime);

		if (((maxRealTime > 0) && (System.currentTimeMillis() - startTime) > maxRealTime))
			return false;

		if ((numRounds > minRounds)
				&& (((temperature > 0) && (temperature <= stopTemp)) || (numRounds++ >= maxRounds))) {
			return false;
		}
		final double oldTemp = temperature;

		for (int k = 0; k < springNodes.length; k++) { // deterministic schedule
			final int next = k;
			// if(fixedOrder != null)
			// next = fixedOrder[k];

			updatePos(springNodes[next], updateImpulse(springNodes[next], next), currentPlane);
		}

		if (oldTemp <= temperature)
			breakCounter += 2;
		else if (breakCounter > 0)
			breakCounter--;

		if (breakCounter > 50)
			return false;

		return true;
	}

	@Override
	JJPoint updateImpulse(final JJSpringInterpolNode v, final int shouldIndex) {
		final JJPoint i = new JJPoint();

		temperature -= v.heat[currentPlane];
		v.heat[currentPlane] = 0;

		double divider = 0;

		for (final Iterator<JJEdge> iter = v.middleNode[currentPlane].edgeIterator(); iter.hasNext();) {
			// computing forces

			final JJEdge tmpE = iter.next();

			final JJNode k = tmpE.opposite(v.middleNode[currentPlane]);
			final JJPoint kPos = getCoords(k);

			final JJPoint d = JJPoint.minus(v.pos[currentPlane], kPos);
			double dist = JJPoint.dist(v.pos[currentPlane], kPos);

			double attract = tmpE.getLength() - dist;
			attract /= 2.0; // Half force as every edge counts at two nodes!

			// Debug.println("Attract: " + attract);

			v.heat[currentPlane] += Math.abs(attract);

			if (dist == 0)
				dist = 1;

			i.plusA(d.divA(dist).multA(attract * tmpE.getWeight()));
			divider += tmpE.getWeight();
		}

		// Compute Attraction to middle node
		{
			final JJPoint kPos = shouldPos[shouldIndex];

			final JJPoint d = JJPoint.minus(kPos, v.pos[currentPlane]);
			final double attract = JJPoint.dist(kPos, v.pos[currentPlane]);

			v.heat[currentPlane] += attract;

			if (attract != 0) {
				i.plusA(d.multA(interEdgeWeight));
				divider += interEdgeWeight;
			}
		}

		if (divider != 0)
			i.divA(divider);

		temperature += v.heat[currentPlane];

		return i;
	}
}
