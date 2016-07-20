/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

public class JJArrayRegressCollection implements JJRegressableCollection {
	protected JJRegressable reg[];

	public JJArrayRegressCollection(final JJRegressable u[]) {
		reg = u;
	}

	@Override
	public JJPoint getPos1(final int k) {
		return reg[k].getPos1();
	}

	@Override
	public JJPoint getPos2(final int k) {
		return reg[k].getPos2();
	}

	@Override
	public int size() {
		if (reg == null)
			return 0;

		return reg.length;
	}

} // JJRegressable
