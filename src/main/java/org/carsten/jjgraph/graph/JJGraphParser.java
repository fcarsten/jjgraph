/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

/**
 * JJGraphParser.java
 *
 *
 * Created: Fri Feb 26 13:37:31 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import java.io.IOException;

public interface JJGraphParser {
	boolean parse(JJGraph g) throws IOException;
} // JJGraphParser
