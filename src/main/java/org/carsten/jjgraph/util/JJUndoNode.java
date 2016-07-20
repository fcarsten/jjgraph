/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

/**
 * JJUndoNode.java
 *
 *
 * Created: Wed Apr 12 19:00:16 2000
 *
 * @author Carsten Friedrich
 * @version
 */
import java.lang.reflect.Method;

public class JJUndoNode {
	private final String name;
	private final Object target;
	private final Method method;
	private final Object[] parameters;

	public JJUndoNode(final String s, final Object t, final Method m, final Object[] p) {
		name = s;
		target = t;
		method = m;
		parameters = p;
	}

	public Object getTarget() {
		return target;
	}

	@Override
	public String toString() {
		return name;
	}

	public Object execute() {
		// Debug.println("Calling " + method + " on object " + target);

		try {
			return method.invoke(target, parameters);
		} catch (final java.lang.reflect.InvocationTargetException e) {
			Debug.println(e.getMessage());
		} catch (final java.lang.IllegalAccessException e) {
			Debug.println(e.getMessage());
		}

		return null;
	}

} // JJUndoNode
