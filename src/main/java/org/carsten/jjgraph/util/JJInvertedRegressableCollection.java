/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

import java.util.List;

public class JJInvertedRegressableCollection implements JJRegressableCollection {

	JJRegressableCollection reg;

	public JJInvertedRegressableCollection(final JJRegressable r[]) {
		reg = new JJArrayRegressCollection(r);
	}

	public JJInvertedRegressableCollection(final List<JJRegressable> r) {
		reg = new JJListRegressCollection(r);
	}

	@Override
	public JJPoint getPos1(final int k) {
		return reg.getPos2(k);
	}

	@Override
	public JJPoint getPos2(final int k) {
		return reg.getPos1(k);
	}

	@Override
	public int size() {
		return reg.size();
	}

} // JJRegressable
