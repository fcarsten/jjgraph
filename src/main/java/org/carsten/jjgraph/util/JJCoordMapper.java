/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

/**
 * JJCoordMapper.java
 *
 *
 * Created: Thu Sep 05 11:50:39 2002
 *
 * @author <a href="mailto:carsten@it.usyd.edu.au"></a>
 * @version
 */

public interface JJCoordMapper {
	JJPoint transform(JJPoint p);

	double getX(JJPoint p);

	double getY(JJPoint p);

	double getZ(JJPoint p);
}// JJCoordMapper
