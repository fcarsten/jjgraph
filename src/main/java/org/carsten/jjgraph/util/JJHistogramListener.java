/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

/**
 * JJHistogramListener.java
 *
 *
 * Created: Wed Feb 28 18:05:09 2001
 *
 * @author
 * @version
 */

public interface JJHistogramListener {
	void setSelectedRegion(double min, double max);

	void clearSelectedRegion(double min, double max);

	void addSelectedRegion(double min, double max);

} // JJHistogramListener
