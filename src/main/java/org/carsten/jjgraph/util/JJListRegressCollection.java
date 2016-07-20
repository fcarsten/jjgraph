/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

import java.util.List;

public class JJListRegressCollection implements JJRegressableCollection {
	protected List<JJRegressable> reg;

	public JJListRegressCollection(final List<JJRegressable> l) {
		reg = l;
	}

	@Override
	public JJPoint getPos1(final int k) {
		return reg.get(k).getPos1();
	}

	@Override
	public JJPoint getPos2(final int k) {
		return reg.get(k).getPos2();
	}

	@Override
	public int size() {
		return reg.size();
	}

} // JJRegressable
