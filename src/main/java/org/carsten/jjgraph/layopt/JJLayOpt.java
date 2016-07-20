/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.layopt;

/**
 * JJLayOpt.java
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

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraphWindowImpl;
import org.carsten.jjgraph.graph.JJGraphicEdge;
import org.carsten.jjgraph.graph.JJGraphicNode;
import org.carsten.jjgraph.graph.JJNode;
import org.carsten.jjgraph.util.Debug;
import org.carsten.jjgraph.util.GFDecomp;
import org.carsten.jjgraph.util.GFMatrix;
import org.carsten.jjgraph.util.JJArrayRegressCollection;
import org.carsten.jjgraph.util.JJLinAlgException;
import org.carsten.jjgraph.util.JJLinearRegress;
import org.carsten.jjgraph.util.JJPoint;

public class JJLayOpt {
	private final JJGraphWindowImpl fenster;

	private JJPoint centerNew;

	public JJPoint getCenterNew() {
		return new JJPoint(centerNew);
	}

	public void setCenterNew(final JJPoint v) {
		this.centerNew = new JJPoint(v);
	}

	private double m[];

	public void composeTransformation() {
		double a1 = getRotate() ? gfDecomp.angle1 : 0;
		double a2 = gfDecomp.angle2;
		double a3 = gfDecomp.angle1 + gfDecomp.skew;
		double a4 = gfDecomp.angle1 - gfDecomp.skew;

		if (getOrtho()) {
			a1 = 0;
			a2 = 90;
			a3 = 180;
			a4 = 270;
		}

		Debug.println("Angle: " + a1);
		Debug.println("Angle: " + a2);
		Debug.println("Angle: " + a3);
		Debug.println("Angle: " + a4);

		final boolean f = (getFlip() && gfDecomp.flip);
		final double sk = getSkew() ? gfDecomp.skew : 0;
		double sx = (getScale() || getScaleProp()) ? gfDecomp.xScale : 1;
		double sy = (getScale() || getScaleProp()) ? gfDecomp.yScale : 1;
		final double tx = getTranslate() ? gfDecomp.xTrans : 0;
		final double ty = getTranslate() ? gfDecomp.yTrans : 0;

		if (getScaleProp()) {
			sx = sy = (sx + sy) / 2;
		}

		m = GFMatrix.composeTrans(a1, sk, sx, sy, tx, ty, f, centerNew);

		if ((!getSkew() && getRotate()) || getOrtho()) {
			double qm = confidence(m);

			final double n[] = GFMatrix.composeTrans(a2, sk, sx, sy, tx, ty, f, centerNew);
			final double qn = confidence(n);

			if (qn < qm) {
				Debug.println("Using second angle");
				m = n;
				qm = qn;
			}

			final double o[] = GFMatrix.composeTrans(a3, sk, sx, sy, tx, ty, f, centerNew);
			final double qo = confidence(o);
			if (qo < qm) {
				Debug.println("Using third angle");
				m = o;
				qm = qo;
			}

			final double p[] = GFMatrix.composeTrans(a4, sk, sx, sy, tx, ty, f, centerNew);
			final double qp = confidence(p);
			if (qp < qm) {
				Debug.println("Using fourth angle");
				m = p;
				qm = qp;
			}

		}

	}

	public double confidence(final double m[]) {
		double conf = 0;

		for (final JJOptNode optNode : optNodes) {
			final JJPoint p = optNode.getNewPos();
			final JJPoint np = new JJPoint(m[4] + p.x * m[0] + p.y * m[2], m[5] + p.x * m[1] + p.y * m[3]);
			conf += np.minusA(optNode.getOldPos()).abs();
		}
		Debug.println("Quality: " + conf / optNodes.length);

		return conf / optNodes.length;
	}

	private HashMap<JJGraphicNode, JJOptNode> optNodeMap;
	private JJOptNode optNodes[];

	public void computeCenterNew() {
		if (fenster.getCenterMode() == JJGraphWindowImpl.BARRY_CENTER) {
			computeBarryCenterNew();
		} else {
			computeBoundingBoxCenterNew();
		}
	}

	public void computeBarryCenterNew() {
		centerNew = new JJPoint();
		int numOld = 0;

		for (final JJOptNode optNode : optNodes) {
			numOld++;
			centerNew.plusA(optNode.getNewPos());
		}
		if (numOld != 0)
			centerNew.divA(numOld);
	}

	public void computeBoundingBoxCenterNew() {
		JJPoint min = null;
		JJPoint max = null;

		for (final JJOptNode optNode : optNodes) {
			if (min == null || max == null) {
				min = new JJPoint(optNode.getNewPos());
				max = new JJPoint(optNode.getNewPos());
			} else {
				min.x = Math.min(min.x, optNode.getNewPos().x);
				min.y = Math.min(min.y, optNode.getNewPos().y);
				max.x = Math.max(max.x, optNode.getNewPos().x);
				max.y = Math.max(max.y, optNode.getNewPos().y);
			}
		}
		if (min != null && max != null)
			centerNew = new JJPoint((min.x + max.x) / 2.0, (min.y + max.y) / 2.0);
		else
			centerNew = new JJPoint();
	}

	public void openSubtask(final String s) {
		fenster.getGraph().getUndoManager().openSubtask(s);
	}

	public void closeSubtask(final String s) {
		fenster.getGraph().getUndoManager().closeSubtask(s);
	}

	public void setUndoRecording(final boolean b) {
		fenster.getGraph().getUndoManager().setUndoRecording(b);
	}

	public JJLayOpt(final JJGraphWindowImpl f2) {
		fenster = f2;
	}

	private String action;

	public void startAction(final String s) {
		action = s;
		openSubtask(action);
		// writeUndoInfo();
		// setUndoRecording(false);
	}

	public void finishAction() {
		// setUndoRecording(true);
		closeSubtask(action);
	}

	private void writePos() {
		for (final JJOptNode optNode : optNodes) {
			fenster.moveNodeTo(optNode.node, optNode.getOptPos());
		}
	}

	public void rewind() {
		startAction("Reset");
		for (final JJOptNode optNode : optNodes) {
			fenster.moveNodeTo(optNode.node, optNode.getOldPos());
		}
		finishAction();
	}

	public void play() {
		startAction("Play");
		for (final JJOptNode optNode : optNodes) {
			fenster.moveNodeTo(optNode.node, optNode.getNewPos());
		}
		finishAction();
	}

	private void initOptNodes() {
		if (optNodeMap == null)
			optNodeMap = new HashMap<>();
		else
			optNodeMap.clear();

		final int numNodes = fenster.getGraph().getNumNodes();
		optNodes = new JJOptNode[numNodes];

		int i = 0;

		for (final Iterator<JJNode> iter = fenster.getGraph().nodeIterator(); iter.hasNext();) {
			final JJNode tmpN = iter.next();
			final JJGraphicNode gn = tmpN.getGraphicNode(fenster);

			final JJOptNode on = new JJOptNode(gn.getCoords(), gn);
			optNodes[i++] = on;
			optNodeMap.put(gn, on);
		}
	}

	public void setEnd() {
		for (final Iterator<JJNode> iter = fenster.getGraph().nodeIterator(); iter.hasNext();) {
			final JJNode tmpN = iter.next();
			final JJGraphicNode gn = tmpN.getGraphicNode(fenster);
			final JJOptNode on = optNodeMap.get(gn);
			if (on != null) {
				on.setNewPos(gn.getCoords());
				on.setOptPos(gn.getCoords());
			}
		}

		try {
			computeCenterNew();
			m = JJLinearRegress.regress(new JJArrayRegressCollection(optNodes), centerNew);
			// computeLinearTransform();
			final double q[] = new double[6];
			final double s[] = new double[6];

			GFMatrix.polarDecompose(m, q, s, true);
			gfDecomp = new GFDecomp(m);
		} catch (final JJLinAlgException e) {
			fenster.printError("Couldn't optimize layout: " + e.getMessage());
		}

	}

	public void setStart() {
		initOptNodes();
	}

	private GFDecomp gfDecomp;

	public void optimize() {
		startAction("Optimize");
		composeTransformation();

		for (final JJOptNode optNode : optNodes) {
			final JJPoint p = optNode.getNewPos();
			final JJPoint np = new JJPoint(m[4] + p.x * m[0] + p.y * m[2], m[5] + p.x * m[1] + p.y * m[3]);
			optNode.setOptPos(np);

		}

		// Unfortunatly we have to move the edge bends here
		// and not in writePos as they are not in optNodes :-(.

		for (final Iterator<JJEdge> iter = fenster.getGraph().edgeIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			final JJGraphicEdge ge = tmpE.getGraphicEdge(fenster);
			if (ge.getBends() != null) {
				int k = 0;
				for (final Object element : ge.getBends()) {
					final JJPoint p = (JJPoint) element;

					fenster.moveBendTo(ge, k++, m[4] + p.x * m[0] + p.y * m[2], m[5] + p.x * m[1] + p.y * m[3]);
				}
			}
		}
		writePos();
		finishAction();
	}

	// private boolean translateP=true;

	// private boolean scaleP=true;
	// private boolean scalePropP=true;
	// private boolean orthoP=false;

	// private boolean skewP=false;

	// private boolean flipP=true;

	// private boolean rotateP=true;

	public final static int TRANSLATE = 1;
	public final static int SCALE = 2;
	public final static int SCALE_PROPORTIONAL = 4;
	public final static int ROTATE = 8;
	public final static int ROTATE_ORTHOGONAL = 16;
	public final static int SKEW = 32;
	public final static int FLIP = 64;

	private int optimizeFlag = TRANSLATE | SCALE_PROPORTIONAL | FLIP;

	/**
	 * Get the value of optimizeFlag.
	 *
	 * @return Value of optimizeFlag.
	 */
	public int getOptimizeFlag() {
		return optimizeFlag;
	}

	/**
	 * Set the value of optimizeFlag.
	 *
	 * @param v
	 *            Value to assign to optimizeFlag.
	 */
	public void setOptimizeFlag(final int v) {
		this.optimizeFlag = v;
	}

	public boolean getRotate() {
		return (optimizeFlag & ROTATE) != 0;
	}

	public void setRotate(final boolean v) {
		if (v)
			this.optimizeFlag |= ROTATE;
		else
			this.optimizeFlag &= ~ROTATE;
	}

	public boolean getFlip() {
		return (optimizeFlag & FLIP) != 0;
	}

	public void setFlip(final boolean v) {
		if (v)
			this.optimizeFlag |= FLIP;
		else
			this.optimizeFlag &= ~FLIP;
	}

	public boolean getSkew() {
		return (optimizeFlag & SKEW) != 0;
	}

	public void setSkew(final boolean v) {
		if (v)
			this.optimizeFlag |= SKEW;
		else
			this.optimizeFlag &= ~SKEW;
	}

	public boolean getScale() {
		return (optimizeFlag & SCALE) != 0;
	}

	public void setScale(final boolean v) {
		if (v)
			this.optimizeFlag |= SCALE;
		else
			this.optimizeFlag &= ~SCALE;
	}

	public boolean getScaleProp() {
		return (optimizeFlag & SCALE_PROPORTIONAL) != 0;
	}

	public void setScaleProp(final boolean v) {
		if (v)
			this.optimizeFlag |= SCALE_PROPORTIONAL;
		else
			this.optimizeFlag &= ~SCALE_PROPORTIONAL;
	}

	public boolean getOrtho() {
		return (optimizeFlag & ROTATE_ORTHOGONAL) != 0;
	}

	public void setOrtho(final boolean v) {
		if (v)
			this.optimizeFlag |= ROTATE_ORTHOGONAL;
		else
			this.optimizeFlag &= ~ROTATE_ORTHOGONAL;
	}

	public boolean getTranslate() {
		return (optimizeFlag & TRANSLATE) != 0;
	}

	public void setTranslate(final boolean v) {
		if (v)
			this.optimizeFlag |= TRANSLATE;
		else
			this.optimizeFlag &= ~TRANSLATE;
	}
}
