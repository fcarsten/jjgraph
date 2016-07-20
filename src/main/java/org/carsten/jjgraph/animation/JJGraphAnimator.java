/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
/**
 * JJBlender.java
 *
 *
 * Created: Wed Feb 16 16:32:29 2000
 *
 * @author $Author: carsten $
 * @version $Revision: 1.6 $ $Date: 2003/05/21 08:11:27 $
 *
 * $Log: JJGraphAnimator.java,v $
 * Revision 1.6  2003/05/21 08:11:27  carsten
 * Removed some useless stuff
 *
 * Revision 1.5  2003/02/09 05:46:40  carsten
 * Fix bug: Nodes which are visible at start but not at end would not be
 * moved back to their initial positions on rewind
 *
 * Revision 1.4  2003/02/04 00:22:34  carsten
 * Changed _isVisible to isVisible
 *
 * Revision 1.3  2002/08/14 05:55:45  carsten
 * Multiple fade in/out bug fixed
 *
 * Revision 1.2  2002/07/31 01:15:06  carsten
 * Fixed changing colours of edges during movement
 *
 *
 */
import java.util.Iterator;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJGraphicEdge;
import org.carsten.jjgraph.graph.JJGraphicNode;
import org.carsten.jjgraph.graph.JJPreDrawer;
import org.carsten.jjgraph.util.JJPoint;

public class JJGraphAnimator implements JJAnimator, JJPreDrawer {
	private JJAnimationCanvas canvas;
	private final static int frames = 100;

	private final static int START = 0;
	private final static int FADE_OUT = 1;
	private final static int MOTION_PHASE = 3;
	private final static int FADE_IN = 5;
	private final static int FINISH = 6;
	private final static int END = 99;
	final static double SLEEP_TIME = 20;
	private int speed = 5;
	private float fadeAlpha;
	private final float deltaAlpha = 0.1f;

	public final static int CONSTANT = 0;
	public final static int ACCEL_DECEL = 1;
	public final static int ACCEL = 2;
	public final static int DECEL = 3;
	// private boolean didInitOnce = false;

	private int speedFkt = CONSTANT;

	private int mode = END;

	private final JJSceneContainer sceneContainer;

	// private JJGraphAnimationNode animationNodes[][];
	// private JJInterpolator interpolator[];
	// protected JJPoint center[];

	private double images = 0;
	private double imageCounter = 0;
	private boolean measure = false;
	private int currentScene = 0;

	private final float usedMemory = 0;
	private final Runtime rt = Runtime.getRuntime();

	public Color getBackground() {
		return canvas.getBackground();
	}

	/**
	 * Get the value of currentScene.
	 *
	 * @return value of currentScene.
	 */
	public int getCurrentScene() {
		return currentScene;
	}

	/**
	 * Set the value of currentScene.
	 *
	 * @param v
	 *            Value to assign to currentScene.
	 */
	public void setCurrentScene(final int v) {
		this.currentScene = v;
	}

	/**
	 * Get the value of measure.
	 *
	 * @return Value of measure.
	 */
	public boolean getMeasure() {
		return measure;
	}

	/**
	 * Set the value of measure.
	 *
	 * @param v
	 *            Value to assign to measure.
	 */
	public void setMeasure(final boolean v) {
		this.measure = v;
	}

	/**
	 * Get the value of speedFkt.
	 *
	 * @return Value of speedFkt.
	 */
	public int getSpeedFkt() {
		return speedFkt;
	}

	/**
	 * Set the value of speedFkt.
	 *
	 * @param v
	 *            Value to assign to speedFkt.
	 */
	public void setSpeedFkt(final int v) {
		this.speedFkt = v;
	}

	public double speedControl(final double d) {
		switch (speedFkt) {
		case CONSTANT:
			return d;
		case ACCEL:
			return 1 - Math.cos(Math.PI * (d / 2));
		case DECEL:
			return Math.sin(Math.PI * (d / 2));
		case ACCEL_DECEL:
			return (1 - Math.cos(Math.PI * d)) / 2.0;
		default:
			return d;
		}
	}

	public void setInterEdgeWeight(final double d) {
		JJSpringInterpol.setInterEdgeWeight(d);
	}

	public void setDelaunayEdgeWeight(final double d) {
		JJSpringInterpol.setDelaunayEdgeWeight(d);
	}

	public void setCenterEdgeWeight(final double d) {
		JJSpringInterpol.setCenterEdgeWeight(d);
	}

	public void setIntraEdgeWeight(final double d) {
		JJSpringInterpol.setIntraEdgeWeight(d);
	}

	// public JJSpringInterpol getSpringInterpolator()
	// {
	// JJInterpolator inter = sceneContainer.getInterpolator(0);
	// if(inter != null)
	// return inter.springInter;

	// return null;
	// }

	@Override
	public void setCanvas(final JJAnimationCanvas c) {
		canvas = c;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(final int v) {
		this.speed = v;
	}

	@Override
	public boolean hasNext() {
		return mode != END;
	}

	// public void setAnimationNodes(JJGraphAnimationNode[] an, JJPoint c)
	// {
	// animationNodes = an;

	// // If all other nodes already have an end-position, we have to
	// // re-init the whole thing.
	// buildMovingNodes();

	// if(didInitOnce)
	// initEndPos(c);
	// }

	@Override
	public JJSceneContainer initStartPositions(final JJAnimatedShape[] shapes, final JJPoint co) {
		if ((mode != END) && (mode != START))
			fastForward();
		sceneContainer.initFirstScene(shapes, co);

		return sceneContainer;
	}

	public void addScene(final JJPoint coN) {
		final int i = sceneContainer.addScene(coN);

		mode = END;
	}

	// double errors[];

	// void showError(JJRegressableCollection rc, double m[], JJPoint c)
	// {
	// errors = new double[rc.size()];
	// for(int i=0; i< rc.size(); i++){
	// JJPoint pos1 = JJPoint.minus(rc.getPos1(i), c);
	// JJPoint trans = GFMatrix.transform(pos1.x, pos1.y, m);
	// trans.plusA(c);

	// Debug.println("");
	// Debug.println("Pos1 " + pos1);
	// Debug.println("trans " + trans);
	// Debug.println("Pos2 " + rc.getPos2(i));
	// errors[i] = JJPoint.dist(trans, rc.getPos2(i));
	// }

	// JJHistogramWindow hw= new JJHistogramWindow(errors);
	// hw.addHistogramListener(this);
	// }

	// Precondition:
	// - order of 'errors' is the same as 'animationNodes'

	// public void selectedRegion(double min, double max)
	// {
	// canvas.unselectAll();
	// if(animationNodes == null)
	// return;

	// //Debug.println("Select interval: " + min + " to " + max);

	// for(int i=0; i< errors.length; i++){
	// //Debug.print("Error: " + errors[i]);

	// if(errors[i] >= min && errors[i] <= max){
	// //Debug.println(" ... ok");
	// canvas.select(animationNodes[0][i].getAnimatedShape());
	// // !!! maybe have to change animationNodes[i] to movingNodes[i]
	// }
	// else
	// { //Debug.println(" ... no");
	// }
	// }

	// }

	@Override
	public void draw(final Graphics2D g) {
		if (mode == MOTION_PHASE)
			getInterpolator(getCurrentScene()).draw(g, canvas);
	}

	@Override
	public void rewind() {
		// initBlenderNodes();
		sceneContainer.rewind();

		setCurrentScene(0);
		writePos(getAnimationNodes(0));
		mode = START;
	}

	public void fastForward() {
		finishUp(sceneContainer.getNumScenes() - 1);
		mode = END;
		writePos(getMovingNodes(sceneContainer.getNumScenes() - 1));
	}

	public void writeUndoInfo(final JJGraphAnimationNode[] movingNodes) {
		for (final JJGraphAnimationNode movingNode : movingNodes) {
			movingNode.getMovingShape().createUndoMoveEventTo(movingNode.getMovingShape().getCoords());
		}
	}

	public JJGraphAnimator(final JJAnimationCanvas f2) {
		setCanvas(f2);
		// Debug.println("Creating new scene container");

		sceneContainer = new JJSceneContainer(this);

	}

	@Override
	public boolean next() {
		final JJGraphAnimationNode movingNodes[] = getMovingNodes(getCurrentScene());

		if (mode == START) {
			mode = FADE_OUT;
			// Debug.println("Init scene and init fade out");

			initScene(currentScene);

			initFadeOut();
			images = Math.ceil(frames / getSpeed());
			imageCounter = 0;
			if (measure)
				initMeasures();
		}

		if (mode == FADE_OUT) {
			// Debug.println("fade out");
			if (!fadeOut(currentScene)) {
				// Debug.println("start up");
				startUp(currentScene);
				mode = MOTION_PHASE;
				getInterpolator(currentScene).setNumImages((int) (images - imageCounter));
			}
		}

		if (mode == MOTION_PHASE) {
			if (imageCounter > images) {
				// Debug.println("Init fade in");
				initFadeIn(currentScene);
				mode = FADE_IN;
			} else {
				// Debug.println("Motion frame");
				getInterpolator(currentScene).frame(speedControl(imageCounter / images));
				// fadeTo((float)speedControl(imageCounter/images),
				// currentScene);

				imageCounter++;
			}
		}

		if (mode == FADE_IN) {
			// Debug.println("Fade in");
			if (!fadeIn(currentScene))
				mode = FINISH;
		}

		if (mode == FINISH) {
			// Debug.println("Finish up");
			finishUp(currentScene);
			if (currentScene < sceneContainer.getNumScenes() - 1) {
				currentScene++;
				mode = START;
			} else {
				mode = END;
				// canvas.setPredrawer(null);
				if (measure) {
					finalizeMeasures();
					printMeasures();
				}
			}
		} else if (measure)
			updateMeasures(speedControl((imageCounter - 1) / images), movingNodes);

		writePos(movingNodes);

		// rt.gc();
		// float tmpM = rt.totalMemory() - rt.freeMemory();
		// Debug.println("Lost " + (tmpM - usedMemory) + " somewhere");
		// usedMemory = tmpM;
		return hasNext();
	}

	private double sec, unm, cel, tec, npl, uni;

	void updateMeasures(final double d, final JJGraphAnimationNode movingNodes[]) {
		if (!measure)
			return;

		if (canvas instanceof JJAnimatedCanvas) {
			final JJGraphWindow w = ((JJAnimatedCanvas) canvas).graphWindow;

			countEdgeMeasures(w, d, movingNodes);
			countNodeMeasures(w.getGraph(), d, movingNodes);
		}
	}

	void countEdgeMeasures(final JJGraphWindow window, final double d, final JJGraphAnimationNode movingNodes[]) {
		final JJGraph graph = window.getGraph();

		for (int i = 0; i < edgeArray.length; i++) {
			final JJEdge e1 = edgeArray[i];
			final JJGraphicNode gs1 = e1.getSource().getGraphicNode(window);
			final JJGraphicNode gt1 = e1.getTarget().getGraphicNode(window);

			final JJGraphAnimationNode gas1 = movingNodes[gs1.getMNI()];
			final JJGraphAnimationNode gat1 = movingNodes[gt1.getMNI()];

			final double tmp = dist(gas1, gat1) - ((1 - d) * startDist(gas1, gat1) + d * endDist(gas1, gat1));

			cel += tmp * tmp;

			final double ax1 = gas1.getX();
			final double ay1 = gas1.getY();
			final double ax2 = gat1.getX();
			final double ay2 = gat1.getY();

			final double x1 = gs1.getX();
			final double y1 = gs1.getY();
			final double x2 = gt1.getX();
			final double y2 = gt1.getY();

			for (int k = i + 1; k < edgeArray.length; k++) {
				final JJEdge e2 = edgeArray[k];
				final JJGraphicNode gs2 = e2.getSource().getGraphicNode(window);
				final JJGraphicNode gt2 = e2.getTarget().getGraphicNode(window);

				if ((gs1 == gs2) || (gs1 == gt2) || (gt1 == gs2) || (gt1 == gt2))
					continue;

				final JJGraphAnimationNode gas2 = movingNodes[gs2.getMNI()];
				final JJGraphAnimationNode gat2 = movingNodes[gt2.getMNI()];

				if (Line2D.linesIntersect(ax1, ay1, ax2, ay2, gas2.getX(), gas2.getY(), gat2.getX(), gat2.getY())) {
					sec++;
					if (!Line2D.linesIntersect(x1, y1, x2, y2, gs2.getX(), gs2.getY(), gt2.getX(), gt2.getY()))
						tec++;
				}
			}
		}
	}

	void countNodeMeasures(final JJGraph graph, final double d, final JJGraphAnimationNode movingNodes[]) {
		for (int i = 0; i < movingNodes.length; i++) {
			final JJGraphAnimationNode ani = movingNodes[i];
			final JJMovingShape asi = ani.getMovingShape();
			npl += JJPoint.dist(ani.getPosition(), asi.getCoords());

			for (int k = i + 1; k < movingNodes.length; k++) {
				final JJGraphAnimationNode ank = movingNodes[k];

				final double dist = dist(ani, ank);
				final double distS = startDist(ani, ank);
				final double distE = endDist(ani, ank);

				final double tmp = dist - ((1 - d) * distS + d * distE);

				unm += tmp * tmp;

				if ((distS > 10) && (distE > 10) && (dist < 10))
					uni++;
			}
		}
	}

	double dist(final JJGraphAnimationNode a, final JJGraphAnimationNode b) {
		return Math.sqrt((a.getX() - b.getX()) * (a.getX() - b.getX()) + (a.getY() - b.getY()) * (a.getY() - b.getY()));
	}

	double startDist(final JJGraphAnimationNode a, final JJGraphAnimationNode b) {
		return Math.sqrt((a.getPos1().x - b.getPos1().x) * (a.getPos1().x - b.getPos1().x)
				+ (a.getPos1().y - b.getPos1().y) * (a.getPos1().y - b.getPos1().y));
	}

	double endDist(final JJGraphAnimationNode a, final JJGraphAnimationNode b) {
		return Math.sqrt((a.getPos2().x - b.getPos2().x) * (a.getPos2().x - b.getPos2().x)
				+ (a.getPos2().y - b.getPos2().y) * (a.getPos2().y - b.getPos2().y));
	}

	void finalizeMeasures() {
		if (!measure)
			return;

		sec /= imageCounter;
		unm /= imageCounter;
		cel /= imageCounter;

	}

	JJEdge edgeArray[];

	void initMeasures() {
		if (!measure)
			return;

		JJGraphWindow window = null;
		if (canvas instanceof JJAnimatedCanvas) {
			window = ((JJAnimatedCanvas) canvas).graphWindow;
		} else
			return;
		final JJGraph graph = window.getGraph();

		int counter = 0;
		for (final Iterator<JJEdge> iter = graph.edgeIterator(); iter.hasNext();) {
			final JJEdge e = iter.next();
			if ((e.getSource().getGraphicNode(window).getMNI() != -1)
					&& (e.getTarget().getGraphicNode(window).getMNI() != -1))
				counter++;
		}

		edgeArray = new JJEdge[counter];
		counter = 0;
		for (final Iterator<JJEdge> iter = graph.edgeIterator(); iter.hasNext();) {
			final JJEdge e = iter.next();
			if ((e.getSource().getGraphicNode(window).getMNI() != -1)
					&& (e.getTarget().getGraphicNode(window).getMNI() != -1))
				edgeArray[counter++] = e;
		}

		sec = 0;
		cel = 0;
		unm = 0;
		tec = 0;
		npl = 0;
		uni = 0;
	}

	void printMeasures() {
		// Debug.println("Number of frames: " + imageCounter);
		// Debug.println("Animation time: " +
		// getInterpolator(currentScene).animationTime);
		// Debug.println("sec : " + sec);
		// Debug.println("unm : " + unm);
		// Debug.println("cel : " + cel);
		// Debug.println("tec : " + tec);
		// Debug.println("npl : " + npl);
		// Debug.println("uni : " + uni);
	}

	void initFadeOut() {
		fadeAlpha = 1;
	}

	boolean fadeOut(final int scene) {
		fadeAlpha -= deltaAlpha;
		fadeAlpha = Math.max(0, fadeAlpha);
		final Color targetColor = canvas.getBackground();

		final JJGraphAnimationNode animationNodes[] = sceneContainer.getAnimationNodes(scene);

		for (int i = 0; i < animationNodes.length; i++) {
			if ((!animationNodes[i].getEndVisible().isEmpty()) && (animationNodes[i].getStartVisible().isEmpty())
					&& (animationNodes[i].getFadePolicy() == JJAnimatedShape.SEPERATE)) {
				animationNodes[i].setColor(colorFraction(fadeAlpha, animationNodes[i].getStartColor(), targetColor));
			}
		}

		if (fadeAlpha == 0) {
			for (int i = 0; i < animationNodes.length; i++) {
				if ((!animationNodes[i].getEndVisible().isEmpty()) && (animationNodes[i].getStartVisible().isEmpty())
						&& (animationNodes[i].getFadePolicy() == JJAnimatedShape.SEPERATE)) {
					animationNodes[i].setVisible(animationNodes[i].getEndVisible());
				}
			}
			return false;
		}

		return true;
	}

	// Color colorFraction(float alpha, Color c)
	// {
	// float rgb[] = c.getRGBComponents(null);
	// rgb[0] = rgb[0] + (1-rgb[0])*(1-alpha);
	// rgb[1] = rgb[1] + (1-rgb[1])*(1-alpha);
	// rgb[2] = rgb[2] + (1-rgb[2])*(1-alpha);
	// rgb[3] = alpha;
	// return new Color( rgb[0], rgb[1], rgb[2], rgb[3]);
	// }

	static Color colorFraction(final float alpha, final Color c, final Color tc) {
		final float rgb[] = c.getRGBComponents(null);
		final float target[] = tc.getRGBComponents(null);
		rgb[0] = rgb[0] + (target[0] - rgb[0]) * (1 - alpha);
		rgb[1] = rgb[1] + (target[1] - rgb[1]) * (1 - alpha);
		rgb[2] = rgb[2] + (target[2] - rgb[2]) * (1 - alpha);
		rgb[3] = alpha;
		return new Color(rgb[0], rgb[1], rgb[2], rgb[3]);
	}

	// Color darker(Color c, Color tc)
	// {
	// float rgb[] = c.getRGBColorComponents(null);
	// float rgbTarget[] = tc.getRGBColorComponents(null);
	// rgb[0] = (float)Math.max(rgbTarget[0], rgb[0]- 0.02);
	// rgb[1] = (float)Math.max(rgbTarget[1], rgb[1]- 0.02);
	// rgb[2] = (float)Math.max(rgbTarget[2], rgb[2]- 0.02);
	// return new Color( rgb[0], rgb[1], rgb[2]);
	// }

	void initFadeIn(final int scene) {
		fadeAlpha = 0;
		final Color tc = canvas.getBackground();

		final JJGraphAnimationNode animationNodes[] = sceneContainer.getAnimationNodes(scene);

		for (final JJGraphAnimationNode animationNode : animationNodes) {
			final JJGraphAnimationNode currentNode = animationNode;

			if ((!currentNode.getStartVisible().isEmpty()) && (currentNode.getEndVisible().isEmpty())
					&& (currentNode.getFadePolicy() == JJAnimatedShape.SEPERATE)) {
				currentNode.setColor(colorFraction(fadeAlpha, currentNode.getEndColor(), tc));
				currentNode.setVisible(currentNode.getEndVisible());
				if (currentNode.getAnimatedShape() instanceof JJMovingShape) {
					currentNode.setPosition(currentNode.getEndPosition());
					canvas.setPosition(currentNode.getAnimatedShape(), currentNode.getPosition());
				}

			}
		}
	}

	boolean fadeIn(final int scene) {
		fadeAlpha += deltaAlpha;
		fadeAlpha = Math.min(1f, fadeAlpha);
		final Color tc = canvas.getBackground();
		final JJGraphAnimationNode animationNodes[] = sceneContainer.getAnimationNodes(scene);

		for (int i = 0; i < animationNodes.length; i++) {
			if ((!animationNodes[i].getStartVisible().isEmpty()) && (animationNodes[i].getEndVisible().isEmpty())
					&& (animationNodes[i].getFadePolicy() == JJAnimatedShape.SEPERATE)) {
				animationNodes[i].setColor(colorFraction(fadeAlpha, animationNodes[i].getEndColor(), tc));
				// if(animationNodes[i].getAnimatedShape().getName().equals("SKY"))
				// Debug.println("Position of sky is now: " +
				// animationNodes[i].getPosition());

			}
		}
		return (fadeAlpha != 1f);
	}

	// void fadeTo(float fadeAlpha, int scene)
	// {
	// Color tc = canvas.getBackground();
	// JJGraphAnimationNode animationNodes[] =
	// sceneContainer.getAnimationNodes(scene);

	// for(int i=0; i< animationNodes.length; i++){
	// if(animationNodes[i].getFadePolicy() == JJAnimatedShape.DURING){
	// if((!animationNodes[i].getStartVisible().isEmpty()) &&
	// animationNodes[i].getEndVisible().isEmpty())
	// {
	// animationNodes[i].
	// setColor(colorFraction(fadeAlpha,animationNodes[i].getEndColor(),tc));
	// if(! animationNodes[i]._isVisible())
	// animationNodes[i].setVisible(animationNodes[i].getEndVisible());
	// }
	// if((!animationNodes[i].getEndVisible().isEmpty()) &&
	// animationNodes[i].getStartVisible().isEmpty())
	// {
	// animationNodes[i].
	// setColor(colorFraction(1-fadeAlpha, animationNodes[i].getEndColor(),
	// tc));
	// }
	// }
	// }
	// }

	void finishUp(final int scene) {
		final JJGraphAnimationNode animationNodes[] = sceneContainer.getAnimationNodes(scene);

		for (final JJGraphAnimationNode animationNode : animationNodes) {
			animationNode.frame(1);
			animationNode.setVisible(animationNode.getEndVisible());
			if (animationNode.getAnimatedShape() instanceof JJMovingShape)
				animationNode.setPosition(animationNode.getEndPosition());
			animationNode.setColor(animationNode.getEndColor());

		}
	}

	void startUp(final int scene) {
		final JJGraphAnimationNode animationNodes[] = sceneContainer.getAnimationNodes(scene);

		// for(int i=0; i< animationNodes.length; i++){
		// if(animationNodes[i].getAnimatedShape().getFadePolicy() ==
		// JJAnimatedShape.DURING){
		// animationNodes[i].setVisible(null);
		// }
		// }
	}

	void writePos(final JJGraphAnimationNode movingNodes[]) {
		if (movingNodes != null)
			for (final JJGraphAnimationNode movingNode : movingNodes) {
				// if( (movingNodes[k].getStartVisible() ||
				// movingNodes[k].getEndVisible())){
				if (movingNode.isVisible()) {
					canvas.setPosition(movingNode.getAnimatedShape(), movingNode.getPosition());
				}
			}
		canvas.refresh();
	}

	public JJInterpolator getInterpolator(final int i) {
		return sceneContainer.getInterpolator(i);
	}

	public JJPoint getCenter(final int i) {
		return sceneContainer.getCenter(i);
	}

	public JJGraphAnimationNode[] getMovingNodes(final int i) {
		return sceneContainer.getMovingNodes(i);
	}

	public JJGraphAnimationNode[] getAnimationNodes(final int i) {
		return sceneContainer.getAnimationNodes(i);
	}

	public double getZoom() {
		return canvas.getZoom();
	}

	public void setInterpolateMode(final int i) {
		sceneContainer.setInterpolateMode(i);
	}

	public void initScene(final int i) {
		final JJGraphAnimationNode movingNodes[] = getMovingNodes(i);
		final JJGraphAnimationNode animationNodes[] = getAnimationNodes(i);

		for (final JJGraphAnimationNode animationNode : animationNodes) {
			animationNode.setMNI(-1);
		}
		if (movingNodes != null) {
			for (int k = 0; k < movingNodes.length; k++) {
				movingNodes[k].setMNI(k);
			}
		}

		for (final JJGraphAnimationNode animationNode : animationNodes) {
			if (animationNode.getAnimatedShape() instanceof JJGraphicEdge) {
				final JJGraphicEdge ge = (JJGraphicEdge) animationNode.getAnimatedShape();
				final JJGraphWindow window = ge.getWindow();

				if ((ge.getEdge().getSource().getGraphicNode(window).getMNI() == -1)
						|| (ge.getEdge().getTarget().getGraphicNode(window).getMNI() == -1)) {
					ge.setFadePolicy(JJAnimatedShape.SEPERATE);
					// ge.getEdge().setName("Seperate");
				} else {
					ge.setFadePolicy(JJAnimatedShape.DURING);
					// ge.getEdge().setName("During");
				}
			}
		}
		sceneContainer.switchToScene(i);

	}

}
