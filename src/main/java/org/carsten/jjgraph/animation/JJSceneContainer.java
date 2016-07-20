/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

/**
 * JJSceneContainer.java
 *
 *
 * Created: Wed Jun 05 15:22:43 2002
 *
 * @author $Author: carsten $
 * @version $Revision: 1.3 $ $Date: 2002/08/14 05:55:45 $
 *
 * $Log: JJSceneContainer.java,v $
 * Revision 1.3  2002/08/14 05:55:45  carsten
 * Multiple fade in/out bug fixed
 *
 * Revision 1.2  2002/07/31 01:15:06  carsten
 * Fixed changing colours of edges during movement
 *
 */
import java.util.ArrayList;

import org.carsten.jjgraph.util.Debug;
import org.carsten.jjgraph.util.JJPoint;

public class JJSceneContainer {
	protected java.util.List<JJGraphAnimationNode[]> animationNodes = new ArrayList<>();
	protected java.util.List<JJInterpolator> interpolator = new ArrayList<>();
	protected java.util.List<JJPoint> center = new ArrayList<>();
	protected java.util.List<JJGraphAnimationNode[]> movingNodes = new ArrayList<>();
	protected JJGraphAnimator animator;
	private int numScenes = 0;

	public int getNumScenes() {
		return numScenes;
	}

	public JJPoint getCenter(final int i) {
		try {
			return center.get(i);
		} catch (final IndexOutOfBoundsException e) {
		}
		return null;
	}

	public JJGraphAnimationNode[] getMovingNodes(final int i) {
		try {
			return movingNodes.get(i);
		} catch (final IndexOutOfBoundsException e) {
		}
		return null;

	}

	public JJGraphAnimationNode[] getAnimationNodes(final int i) {
		try {
			return animationNodes.get(i);
		} catch (final IndexOutOfBoundsException e) {
		}
		return null;
	}

	public JJInterpolator getInterpolator(final int i) {
		try {
			return interpolator.get(i);
		} catch (final IndexOutOfBoundsException e) {
			Debug.println("Couldn't find interpolator for scene " + i);
		}
		return null;
	}

	public JJSceneContainer(final JJGraphAnimator ga) {
		animator = ga;
	}

	public void initFirstScene(final JJAnimatedShape[] shapes, final JJPoint co) {
		// Debug.println("Init first scene");

		animationNodes.clear();
		movingNodes.clear();
		interpolator.clear();
		center.clear();
		numScenes = 0;

		if (co != null)
			center.add(new JJPoint(co));
		else
			center.add(null);

		final int numNodes = shapes.length;
		final JJGraphAnimationNode an[] = new JJGraphAnimationNode[numNodes];
		for (int i = 0; i < numNodes; i++) {
			an[i] = new JJGraphAnimationNode(shapes[i], animator.getBackground());
		}
		animationNodes.add(an);
	}

	public void initNextScene(final JJGraphAnimationNode[] anOld, final JJPoint co) {
		if (co != null)
			center.add(new JJPoint(co));
		else
			center.add(null);

		final int numNodes = anOld.length;
		final JJGraphAnimationNode an[] = new JJGraphAnimationNode[numNodes];
		for (int i = 0; i < numNodes; i++) {
			an[i] = new JJGraphAnimationNode(anOld[i].getAnimatedShape(), animator.getBackground());
		}
		animationNodes.add(an);
	}

	public void closeScene(final JJPoint p) {
		// Debug.println("Closing scene : " + numScenes);

		numScenes++;

		JJPoint centerPoint = getCenter(numScenes - 1);
		if (p != null) {
			if (centerPoint == null)
				centerPoint = new JJPoint(p);
			else {
				centerPoint.x = (centerPoint.x + p.x) / 2.0;
				centerPoint.y = (centerPoint.y + p.y) / 2.0;
			}
		}

		final JJInterpolator inter = new JJInterpolator(animator);

		final JJGraphAnimationNode mn[] = buildMovingNodes(numScenes - 1);
		movingNodes.add(mn);

		interpolator.add(inter);
		inter.initEndPos(getAnimationNodes(numScenes - 1), mn, centerPoint);
	}

	public int addScene(final JJPoint p) {
		closeScene(p);

		// Debug.println("Init scene : " + numScenes);
		initNextScene(getAnimationNodes(numScenes - 1), p);

		return numScenes - 1;
	}

	public void setInterpolateMode(final int i) {
		for (int k = 0; k < interpolator.size(); k++) {
			getInterpolator(k).setInterpolateMode(i);
		}
	}

	public void switchToScene(final int d) {
		final JJGraphAnimationNode animationNodes[] = getAnimationNodes(d);

		if (animationNodes != null) {
			for (final JJGraphAnimationNode animationNode : animationNodes) {
				animationNode.frame(0);
				animationNode.setVisible(animationNode.getStartVisible());
				animationNode.setColor(animationNode.getStartColor());

				// if(animationNodes[i].isMoving())
				if (animationNode.getStartPosition() != null)
					animationNode.setPosition(new JJPoint(animationNode.getStartPosition()));
			}
		}
	}

	public void rewind(final int d) {
		switchToScene(d);

		for (int i = d; i < interpolator.size(); i++)
			getInterpolator(i).reset();
	}

	public void rewind() {
		rewind(0);
	}

	protected JJGraphAnimationNode[] buildMovingNodes(final int scene) {
		int counter = 0;
		final JJGraphAnimationNode animationNodes[] = getAnimationNodes(scene);
		JJGraphAnimationNode movingNodes[] = null;

		for (final JJGraphAnimationNode animationNode : animationNodes) {
			if (animationNode.isMoving())
				counter++;
		}

		if (counter > 0) {
			movingNodes = new JJGraphAnimationNode[counter];
			counter = 0;

			for (final JJGraphAnimationNode animationNode : animationNodes) {
				if (animationNode.getAnimatedShape() instanceof JJMovingShape) {
					if (animationNode.isMoving()) {
						movingNodes[counter] = animationNode;
						counter++;
					}
				}
			}

		}

		return movingNodes;
	}

}// JJSceneContainer
