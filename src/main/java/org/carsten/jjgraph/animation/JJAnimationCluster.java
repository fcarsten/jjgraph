/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

import java.util.LinkedList;
import java.util.List;

import org.carsten.jjgraph.util.JJLinAlgException;

public class JJAnimationCluster {
	List<JJClusterDatum> nodes = new LinkedList<>();
	JJClusterCenter center;
	boolean valid = true;

	public void add(final JJClusterDatum m) {
		nodes.add(m);
	}

	public void remove(final JJClusterDatum m) {
		nodes.remove(m);
	}

	public void recomputeCenter() {
		if (nodes.size() <= 2) {
			valid = false;
			return;
		}

		center = new JJClusterCenter(); // new double[6], null, new JJPoint());
		// JJPoint centerPoint = new JJPoint();

		final List<JJClusterDatum> dataList = new LinkedList<>();

		for (final Object element : nodes) {
			final JJClusterDatum tmpM = (JJClusterDatum) element;
			dataList.add(tmpM);
			// centerPoint.plusA(tmpM.getCenter());
		}
		try {
			center.recompute(dataList);
			// centerPoint.divA(nodes.size());
			// center.setCenter(centerPoint);
		} catch (final JJLinAlgException e) {
			valid = false;
		}

	}

	public double dist(final JJClusterDatum m) {
		return center.dist(m);
	}

} // JJAnimationCluster
