/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

import java.awt.Color;
import java.util.BitSet;

import org.carsten.jjgraph.util.Debug;
import org.carsten.jjgraph.util.JJPoint;
import org.carsten.jjgraph.util.JJRegressable;

public class JJGraphAnimationNode implements JJRegressable, JJAnimationNode {
	private JJPoint oldPos; // The initial position of the node for the
							// animation
	private BitSet oldVisible; // is the node visible at the beginning of the
								// animation
	private boolean movingShape = false;
	private Color startColor;
	private Object userData;

	/**
	 * Get the value of userData.
	 *
	 * @return value of userData.
	 */
	@Override
	public Object getUserData() {
		return userData;
	}

	/**
	 * Set the value of userData.
	 *
	 * @param v
	 *            Value to assign to userData.
	 */
	@Override
	public void setUserData(final Object v) {
		this.userData = v;
	}

	@Override
	public Color getStartColor() {
		return startColor;
	}

	public void setStartColor(final Color v) {
		this.startColor = v;
	}

	@Override
	public int getFadePolicy() {
		return knoten.getFadePolicy();
	}

	@Override
	public void setFadePolicy(final int d) {
		knoten.setFadePolicy(d);
	}

	@Override
	public JJPoint getPos1() {
		return getStartPosition();
	}

	@Override
	public JJPoint getPos2() {
		return getEndPosition();
	}

	@Override
	public JJPoint getStartPosition() {
		return oldPos;
	}

	@Override
	public BitSet getStartVisible() {
		return oldVisible;
	}

	public boolean isStartVisible() {
		return oldVisible.isEmpty();
	}

	public boolean isMoving() {
		return movingShape && getStartVisible().isEmpty() && getEndVisible().isEmpty();
	}

	public void setStartVisible(final BitSet v) {
		this.oldVisible = new BitSet();
		this.oldVisible.or(v);
	}

	// private JJPoint newPos; // The temporary target position. Differs from
	// the target position
	// if the target position has been moved to avoid unneccessary ndoe movement

	private BitSet newVisible; // is the node visible at the final layout?

	private JJPoint blendPos; // the current position of the node
	private JJPoint targetPos; // The final position of the node
	private JJPoint customPos; // Everyone is free to use

	/**
	 * Get the value of customPos.
	 *
	 * @return Value of customPos.
	 */
	@Override
	public JJPoint getCustomPos() {
		return customPos;
	}

	/**
	 * Set the value of customPos.
	 *
	 * @param v
	 *            Value to assign to customPos.
	 */
	@Override
	public void setCustomPos(final JJPoint v) {
		this.customPos = v;
	}

	private Color endColor; // The final colour of the node

	private final JJAnimatedShape knoten; // Reference to the node in the graph

	@Override
	public JJAnimatedShape getAnimatedShape() {
		return knoten;
	}

	public void setMNI(final int i) {
		if (movingShape) {
			((JJMovingShape) knoten).setMNI(i);
		}
	}

	@Override
	public JJMovingShape getMovingShape() {
		if (movingShape)
			return (JJMovingShape) knoten;
		return null;

	}

	public BitSet getVisible() {
		return knoten.getVisible();
	}

	@Override
	public boolean isVisible() {
		return knoten.isVisible();
	}

	@Override
	public JJPoint getEndPosition() {
		return new JJPoint(targetPos);
	}

	public void setEndPosition(final JJPoint p) {
		targetPos = new JJPoint(p);
	}

	@Override
	public JJPoint getPosition() {
		return blendPos;
	}

	@Override
	public double getX() {
		return blendPos.x;
	}

	@Override
	public double getY() {
		return blendPos.y;
	}

	@Override
	public void setPosition(final JJPoint p) {
		blendPos = new JJPoint(p);
	}

	public void setEndVisible(final BitSet b) {
		newVisible = new BitSet();
		newVisible.or(b);
	}

	@Override
	public BitSet getEndVisible() {
		return newVisible;
	}

	public boolean isEndVisible() {
		return newVisible.isEmpty();
	}

	// public void setTargetPosition(JJPoint p)
	// {
	// targetPos = p;
	// }

	// public JJPoint getTargetPosition()
	// {
	// return targetPos;
	// }

	public void setEndColor(final Color c) {
		endColor = c;
	}

	@Override
	public Color getEndColor() {
		return endColor;
	}

	@Override
	public Color getColor() {
		return knoten.getColor();
	}

	public void setColor(final Color c) {
		knoten.setColor(c);
	}

	public void setVisible(final int v, final boolean b) {
		knoten.setVisible(v, b);
	}

	public void setVisible(final BitSet b) {
		knoten.getVisible().clear();
		if (b != null)
			knoten.getVisible().or(b);

		// knoten.setVisible(b);
	}

	private final Color background;

	public JJGraphAnimationNode(final JJAnimatedShape n, final Color bg) {
		knoten = n;
		if (bg == null)
			throw new NullPointerException();

		background = bg;

		if (n instanceof JJMovingShape) {
			movingShape = true;
			oldPos = new JJPoint(((JJMovingShape) n).getCoords());
			blendPos = new JJPoint(oldPos);
			targetPos = null; // new JJPoint(oldPos);
		}
		setStartVisible(n.getVisible());
		setEndVisible(n.getVisible());
		startColor = n.getColor();
		setUserData(n.getAnimationData());
		n.setAnimationStartData(getUserData());

	}

	@Override
	public String toString() {
		return knoten.toString();
	}

	@Override
	public void frame(final double d) {
		Color sc = background;
		Color ec = background;

		if (isStartVisible())
			sc = getStartColor();
		if (isEndVisible())
			ec = getEndColor();

		if (sc == null) {
			Debug.println("Incorrect start color!");
			sc = background;
		}

		if (ec == null) {
			Debug.println("Incorrect end color!");
			ec = background;
		}

		// if(getAnimatedShape().getName().equals("During")){
		// Debug.println("During");

		// }

		if (!sc.equals(ec)) {
			final int red = (int) (sc.getRed() + (ec.getRed() - sc.getRed()) * d);
			final int blue = (int) (sc.getBlue() + (ec.getBlue() - sc.getBlue()) * d);
			final int green = (int) (sc.getGreen() + (ec.getGreen() - sc.getGreen()) * d);

			if ((!isVisible()) && getFadePolicy() == JJAnimatedShape.DURING)
				setVisible(null);

			if (isStartVisible() && isEndVisible())
				setColor(new Color(red, green, blue));
			else {
				if (!isStartVisible()) {
					setColor(new Color(red, green, blue, (int) (d * 255)));
					// if(getAnimatedShape().getName().equals("During")){
					// Debug.println("start Color("+
					// sc.getRed()+","+sc.getBlue()+","+
					// sc.getGreen()+")");
					// Debug.println("end Color("+ ec.getRed()+","+
					// ec.getBlue()+","+
					// ec.getGreen()+")");
					// Debug.println("setColor(new Color("+
					// red+","+blue+","+green+","
					// + ((1-d)*255) + ")");
					// }
				} else if (!isEndVisible()) {
					setColor(new Color(red, green, blue, (int) ((1 - d) * 255)));
					// if(getAnimatedShape().getName().equals("During")){
					// Debug.println("start Color("+
					// sc.getRed()+","+sc.getBlue()+","+
					// sc.getGreen()+")");
					// Debug.println("end Color("+ ec.getRed()+","+
					// ec.getBlue()+","+
					// ec.getGreen()+")");
					// Debug.println("setColor(new Color("+
					// red+","+blue+","+green+","
					// + (d*255) + ")");
					// }
				}
			}
		}

		getAnimatedShape().frame(getUserData(), d);

	}

} // JJGraphAnimationNode
