/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

import java.awt.Color;

import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJGraphicNode;
import org.carsten.jjgraph.graph.JJNode;
import org.carsten.jjgraph.util.GFMatrix;
import org.carsten.jjgraph.util.JJPoint;

public class JJSpringInterpolNode {
	final public static int LEVELS = 7;
	final public static double startZ = -100;
	final public static double endZ = 100;

	private final JJAnimationNode realNode;
	JJNode startNode;
	JJNode endNode;

	JJNode middleNode[];
	JJPoint pos[];
	double heat[]; // local temperature
	JJPoint startPos;
	JJPoint endPos;

	public void setRealPosition(final JJPoint p) {
		if (realNode != null)
			realNode.setPosition(p);
	}

	public String getRealName() {
		return ((JJGraphicNode) (realNode.getAnimatedShape())).getNode().getName();
	}

	public JJPoint getPos(final int index) {
		return new JJPoint(pos[index]);
	}

	public void addFirstAndLastFrame() {
		final JJPoint tmpPos[] = new JJPoint[LEVELS + 2];
		for (int i = 0; i < pos.length; i++) {
			tmpPos[i + 1] = pos[i];
		}

		tmpPos[0] = startPos;
		tmpPos[LEVELS + 1] = endPos;
		pos = tmpPos;

	}

	private final double ctm[][];
	private final double ctmBack[][];

	public JJSpringInterpolNode(final JJGraph springGraph, final JJAnimationNode rNode, final double mat[][],
			final double matBack[][], final JJGraphWindow springWindow) {
		realNode = rNode;
		ctm = mat;
		ctmBack = matBack;

		init(springGraph, realNode.getStartPosition(), realNode.getEndPosition(), springWindow);
	}

	public JJSpringInterpolNode(final JJGraph springGraph, final JJPoint startP, final JJPoint endP,
			final double mat[][], final double matBack[][], final JJGraphWindow springWindow) {
		realNode = null;
		ctm = mat;
		ctmBack = matBack;

		init(springGraph, startP, endP, springWindow);
	}

	public void init(final JJGraph springGraph, final JJPoint startP, final JJPoint endP,
			final JJGraphWindow springWindow) {
		final double sx = startP.x;
		final double sy = startP.y;
		final double ex = endP.x;
		final double ey = endP.y;

		startPos = new JJPoint(sx, sy, startZ);
		endPos = new JJPoint(ex, ey, endZ);

		startNode = springGraph.addNode();
		endNode = springGraph.addNode();

		if (springWindow != null) {
			final JJGraphicNode sn = startNode.getGraphicNode(springWindow);

			springWindow.moveNodeTo(sn, startPos);
			sn.setColor(new Color(100, 250, 100));

			final JJGraphicNode en = endNode.getGraphicNode(springWindow);
			springWindow.moveNodeTo(en, endPos);
			en.setColor(new Color(100, 100, 250));
		}

		middleNode = new JJNode[LEVELS];
		pos = new JJPoint[LEVELS];

		heat = new double[LEVELS];

		for (int i = 0; i < LEVELS; i++) {
			middleNode[i] = springGraph.addNode();
			if (springWindow != null) {
				middleNode[i].getGraphicNode(springWindow)
						.setColor(new Color(100, 250 - (150 / (LEVELS + 2)) * i, 100 + (150 / (LEVELS + 2)) * i));
			}

			// Direct interpolation version

			// pos[i] = new JJPoint(sx + (ex-sx)*(i+1)/(LEVELS+1),
			// sy + (ey-sy)*(i+1)/(LEVELS+1),
			// startZ +
			// ((Math.abs(endZ-startZ)/(LEVELS+1)) * (i+1)) );

			// Matrix transformation version
			final JJPoint sTrans = GFMatrix.transform(sx, sy, ctm[i]);
			final JJPoint eTrans = GFMatrix.transform(ex, ey, ctmBack[(LEVELS - 1) - i]);
			eTrans.minusA(sTrans);
			eTrans.multA(((double) i) / ((double) (LEVELS - 1)));

			pos[i] = sTrans.plusA(eTrans);
			pos[i].z = startZ + ((Math.abs(endZ - startZ) / (LEVELS + 1)) * (i + 1));

			if (springWindow != null)
				springWindow.moveNodeTo(middleNode[i].getGraphicNode(springWindow), pos[i]);
			heat[i] = 0;
		}

		// tmpE.setName(""+tmpE.getLength());
	}

} // JJSpringInterpolNode
