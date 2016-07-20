/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

import javax.swing.JPanel;

/**
 * JJInspectable.java
 *
 *
 * Created: Thu May 04 10:10:18 2000
 *
 * @author
 * @version
 */

public interface JJInspectable {

	JPanel createTab();

	String getTabName();

} // JJInspectable
