/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;

import org.carsten.jjgraph.util.Debug;
import org.carsten.jjgraph.util.GFMatrix;
import org.carsten.jjgraph.util.JJArrayRegressCollection;
import org.carsten.jjgraph.util.JJInvertedRegressableCollection;
import org.carsten.jjgraph.util.JJLinAlgException;
import org.carsten.jjgraph.util.JJLinearRegress;
import org.carsten.jjgraph.util.JJPoint;
import org.carsten.jjgraph.util.JJRectangle;
import org.carsten.jjgraph.util.JJRegressableCollection;

public class JJInterpolator {

	private final JJGraphAnimator graphAnimator;
	private JJAnimationNode movingNodes[];
	private JJAnimationNode animationNodes[];

	private final double maxXDist = 0.0;
	private final double maxYDist = 0.0;
	private double currentX;
	private double currentY;

	private JJPoint tmpXDir[];
	private JJPoint tmpYDir[];

	private JJPoint tmpDir[];
	private JJPoint tmpPos[];

	public final static int DIRECT_INTERPOLATION = 0;
	public final static int ORTHO_INTERPOLATION = 1;
	public final static int GEM_INTERPOLATION = 2;
	public final static int SPRING_INTERPOLATION = 3;
	public final static int PQ_INTERPOLATION = 4;
	public final static int CLUSTER_INTERPOLATION = 5;

	private int interpolateMode = DIRECT_INTERPOLATION;
	private boolean springNeedsInit = true;

	private final JJMatrixInterpol matInter;
	private final JJClusterTransform clusterInter;
	private final JJClusterTransformDelaunay clusterInterDelaunay;
	private final JJForceInterpolator gem;
	JJSpringInterpol springInter; // Mike version

	private final double q[] = new double[6];
	private final double s[] = new double[6];

	private final double qBack[] = new double[6];
	private final double sBack[] = new double[6];
	private Rectangle canvasRect;
	private JJRectangle bbox;
	protected JJPoint avgCenter;

	public void draw(final Graphics2D g, final JJAnimationCanvas canvas) {
		if (interpolateMode == PQ_INTERPOLATION) {
			g.setColor(canvas.getBackground());
			g.fillRect(canvasRect.x, canvasRect.y, canvasRect.width, canvasRect.height);

			final JJPoint a = matInter.transform(new JJPoint(bbox.x, bbox.y));

			final JJPoint b = matInter.transform(new JJPoint(bbox.x + bbox.width, bbox.y));

			final JJPoint c = matInter.transform(new JJPoint(bbox.x + bbox.width, bbox.y + bbox.height));

			final JJPoint d = matInter.transform(new JJPoint(bbox.x, bbox.y + bbox.height));

			a.multA(graphAnimator.getZoom());
			b.multA(graphAnimator.getZoom());
			c.multA(graphAnimator.getZoom());
			d.multA(graphAnimator.getZoom());

			a.plusA(5000, 5000);
			b.plusA(5000, 5000);
			c.plusA(5000, 5000);
			d.plusA(5000, 5000);

			final int x[] = { (int) a.x, (int) b.x, (int) c.x, (int) d.x };
			final int y[] = { (int) a.y, (int) b.y, (int) c.y, (int) d.y };
			final Polygon poly = new Polygon(x, y, 4);
			g.setColor(new Color(250, 250, 250));
			g.fill(poly);

			g.setColor(Color.black);
			g.draw(poly);

		}
	}

	public void initEndPos(final JJGraphAnimationNode an[], final JJGraphAnimationNode mn[], final JJPoint ac) {
		avgCenter = ac;

		// Compute lineaer part now. Does not cost much anyway
		movingNodes = mn;
		animationNodes = an;

		final long startTime = System.currentTimeMillis();

		try {
			final JJRegressableCollection rc = new JJArrayRegressCollection(mn);
			final JJRegressableCollection rcBack = new JJInvertedRegressableCollection(mn);

			final double matrix[] = JJLinearRegress.regress(rc, avgCenter);
			final double matrixBack[] = JJLinearRegress.regress(rcBack, avgCenter);
			JJLinearRegress.computeR2(rc, matrix, avgCenter);
			JJLinearRegress.computeR2(rcBack, matrixBack, avgCenter);

			// Debug.println("" + new GFDecomp(matrix));
			// Debug.println("" + new GFDecomp(matrixBack));

			GFMatrix.polarDecompose(matrix, q, s, true);
			GFMatrix.polarDecompose(matrixBack, qBack, sBack, true);

			s[4] = matrix[4];
			s[5] = matrix[5];

			sBack[4] = matrixBack[4];
			sBack[5] = matrixBack[5];

		} catch (final JJLinAlgException e) {
			Debug.println(e.getMessage());
		}

		final double m1[][] = { { 1, 0, 0, 1, 0, 0 }, { 1, 0, 0, 1, 0, 0 }, { 1, 0, 0, 1, 0, 0 },
				{ 1, 0, 0, 1, 0, 0 } };
		final double m2[][] = { q, s, qBack, sBack };

		long time2 = System.currentTimeMillis();

		matInter.setSourceMat(m1);
		matInter.setTargetMat(m2);
		matInter.setCenter(avgCenter);

		matInter.init(movingNodes);
		pqInit = System.currentTimeMillis();

		springInterInit(avgCenter);// movingNodes, q, s, qBack, sBack);
		springInit = System.currentTimeMillis();

		initInterpolate();
		naivInit = System.currentTimeMillis();

		gem.init(movingNodes);
		forceInit = System.currentTimeMillis();

		clusterInter.init(movingNodes);
		clusterInit = System.currentTimeMillis();

		// clusterInterDelaunay.init(movingNodes);

		if (Debug.DEBUG) {
			clusterInit -= forceInit;
			forceInit -= naivInit;
			naivInit -= springInit;
			springInit -= pqInit;
			pqInit -= time2;
			time2 -= startTime;
			clusterInit += time2;
			springInit += time2;
			pqInit += time2;
			// Debug.println("Naive init time: " + naivInit);
			// Debug.println("PQ init time: " + pqInit);
			// Debug.println("Force init time: " + forceInit);
			// Debug.println("Spring init time: " + springInit);
			// Debug.println("Cluster init time: " + clusterInit);
		}

	}

	long naivInit = 0;
	long springInit = 0;
	long forceInit = 0;
	long pqInit = 0;
	long clusterInit = 0;
	long animationTime = 0;

	public void springInterInit(final JJPoint ac) {
		if ((interpolateMode == SPRING_INTERPOLATION) && (movingNodes != null)) {
			springInter.init(movingNodes, q, s, qBack, sBack, ac);
			springNeedsInit = false;
		} else
			springNeedsInit = true;
	}

	public int getInterpolateMode() {
		return interpolateMode;
	}

	public void setInterpolateMode(final int v) {
		this.interpolateMode = v;

		if ((interpolateMode == SPRING_INTERPOLATION) && springNeedsInit && (movingNodes != null)) {
			springInter.init(movingNodes, q, s, qBack, sBack, avgCenter);
			springNeedsInit = false;
		}

	}

	public JJInterpolator(final JJGraphAnimator b) {
		graphAnimator = b;
		springInter = new JJSpringInterpol(b);
		matInter = new JJTwoWayPolarInterpol(b);
		clusterInter = new JJClusterTransform(b);
		clusterInterDelaunay = new JJClusterTransformDelaunayDecompose(b);
		gem = new JJForceInterpolator(b);
		// clusterInter = new JJClusterTransformDelaunay(b);
		// matInter= new JJMatrixInterpol(b);
	}

	public void frame(final double d) {
		final long startTime = System.currentTimeMillis();
		nodesFrame(d);

		if (movingNodes != null) {
			switch (getInterpolateMode()) {
			case DIRECT_INTERPOLATION: {
				interpolateFrame(d);
				break;
			}
			case ORTHO_INTERPOLATION: {
				interpolateOrthoFrame(d);
				break;
			}
			case GEM_INTERPOLATION: {
				gem.next();
				break;
			}
			case SPRING_INTERPOLATION: {
				springInter.frame(d);
				break;
			}
			case PQ_INTERPOLATION: {
				matInter.frame(d);
				break;
			}
			case CLUSTER_INTERPOLATION: {
				clusterInter.frame(d);
				break;
			}
			default: {
			}
			}
		}
		animationTime += (System.currentTimeMillis() - startTime);
	}

	public void reset() {
		matInter.reset();
		clusterInter.reset();
		clusterInterDelaunay.reset();

		currentX = 0;
		currentY = 0;
	}

	void setNumImages(final int i) {
		animationTime = 0;

		gem.setNumImages(i);
	}

	void initInterpolate() {
		if (movingNodes == null)
			return;

		tmpDir = new JJPoint[movingNodes.length];
		tmpPos = new JJPoint[movingNodes.length];

		for (int i = 0; i < movingNodes.length; i++) {
			if (movingNodes[i].isVisible()) {
				tmpPos[i] = new JJPoint(movingNodes[i].getPosition());

				tmpDir[i] = JJPoint.minus(movingNodes[i].getEndPosition(), movingNodes[i].getPosition());
			}
		}
	}

	void interpolateFrame(final double d) {
		if (movingNodes == null)
			return;

		for (int j = 0; j < movingNodes.length; j++) {
			if (movingNodes[j].isVisible()) {
				final JJPoint tmpP = new JJPoint(tmpDir[j]);
				tmpP.multA(d);
				tmpP.plusA(tmpPos[j]);
				movingNodes[j].setPosition(tmpP);
			}
		}
	}

	void nodesFrame(final double d) {
		if (animationNodes == null)
			return;

		for (final JJAnimationNode animationNode : animationNodes) {
			// if(animationNodes[j]._isVisible())
			// {
			animationNode.frame(d);
			// }
		}
	}

	void interpolateOrthoFrame(double d) {
		if (movingNodes == null)
			return;
		int ortho = 1;

		if (d > 0.5) {
			d = (d - 0.5) * 2;
		} else {
			ortho = 0;
			d *= 2;
		}

		for (int j = 0; j < movingNodes.length; j++) {
			if (movingNodes[j].isVisible()) {
				final JJPoint tmpP = new JJPoint(tmpDir[j]);
				if (ortho == 0) {
					tmpP.x = 0;
				}
				tmpP.multA(d);

				if (ortho == 1) {
					tmpP.y = tmpDir[j].y;
				}

				tmpP.plusA(tmpPos[j]);
				movingNodes[j].setPosition(tmpP);
			}
		}
	}

} // JJTranslator
