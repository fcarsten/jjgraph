/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;
/*
 * @(#)JJHistogram.java	1.24 98/09/13
 *
 * Copyright 1998 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

/**
 * Tracks Memory allocated & used, displayed in graph form.
 */
public class JJHistogram extends JPanel implements WindowListener {

	public HistoComponent mc;

	public void setMultiSelection(final boolean b) {
		mc.setMultiSelect(b);
	}

	@Override
	public void windowActivated(final WindowEvent e) {
	}

	@Override
	public void windowClosed(final WindowEvent e) {
	}

	@Override
	public void windowClosing(final WindowEvent e) {
		if (mc != null)
			mc.setSelectedRegion(Double.NaN, Double.NaN);
		e.getWindow().dispose();

	}

	@Override
	public void windowDeactivated(final WindowEvent e) {
	}

	@Override
	public void windowDeiconified(final WindowEvent e) {
	}

	@Override
	public void windowIconified(final WindowEvent e) {
	}

	@Override
	public void windowOpened(final WindowEvent e) {
	}

	public JJHistogram(final double pArray[], final double pi) {
		setLayout(new BorderLayout());
		setBorder(new TitledBorder(new EtchedBorder(), "Histogram"));
		mc = new HistoComponent(pArray, pi);
		add(mc);
	}

	public void addHistogramListener(final JJHistogramListener l) {
		mc.addHistogramListener(l);
	}

	public void removeHistogramListener(final JJHistogramListener l) {
		mc.removeHistogramListener(l);
	}

	public class HistoComponent extends JPanel implements MouseListener {
		private BufferedImage bimg;
		int rightLabels = 1;

		// private Graphics2D big;
		private final Font font = new Font("Times New Roman", Font.PLAIN, 11);
		private final double pts[];
		private double pivot = Double.NaN;

		private double columns[];
		private double columnsMax;
		private int numColumns = 10;
		private boolean selectedColumns[] = null;

		private final static double xOffset = 5;

		private final Rectangle graphOutlineRect = new Rectangle();
		private final Line2D graphLine = new Line2D.Float();
		private final Color graphColor = new Color(46, 139, 87);
		private final Color mfColor = new Color(0, 100, 0);

		private final Color histoColor = new Color(200, 200, 100);
		private final Color histoSelectColor = new Color(250, 250, 100);

		Set<JJHistogramListener> listeners = new HashSet<>();

		double min, max; // Store max and min and are changed for pretty
							// printing
		double absMin, absMax; // Store max and min without and don't get
								// changed

		double exponent = 1;

		private double rangeDivider = 0.0;
		private boolean multiSelect = false;

		/**
		 * Get the value of multiSelect.
		 *
		 * @return value of multiSelect.
		 */
		public boolean isMultiSelect() {
			return multiSelect;
		}

		/**
		 * Set the value of multiSelect.
		 *
		 * @param v
		 *            Value to assign to multiSelect.
		 */
		public void setMultiSelect(final boolean v) {
			this.multiSelect = v;
		}

		/**
		 * Get the value of rangeDivider.
		 *
		 * @return value of rangeDivider.
		 */
		public double getRangeDivider() {
			return rangeDivider;
		}

		/**
		 * Set the value of rangeDivider.
		 *
		 * @param v
		 *            Value to assign to rangeDivider.
		 */
		public void setRangeDivider(final double v) {
			this.rangeDivider = v * 10 - 1;

			setMinMax();
			computeColumns();
			this.repaint();
		}

		void setMinMax() {
			final double dist = Math.abs(pivot - absMin);
			final double unit = dist / 10000;

			min = pivot - (10000 - rangeDivider) * unit;
			max = pivot + (10000 - rangeDivider) * unit;
			// Debug.println("Min: " +min);
			// Debug.println("Max: " +max);

		}

		void setColumns(final int c) {
			if (c > 0 && (c * 2 != numColumns)) {
				numColumns = c * 2;
				computeColumns();
				this.repaint();
			}
		}

		public HistoComponent(final double pArray[], final double pi) {
			this.setBackground(Color.black);
			pivot = pi;

			int counter = 0;
			for (final double element : pArray) {
				if (JJPoint.isValid(element))
					counter++;
			}

			pts = new double[counter];
			int k = 0;

			for (final double element : pArray) {
				if (JJPoint.isValid(element))
					pts[k++] = element;
			}

			// System.arraycopy(pArray, 0, pts, 0, pArray.length);

			normalizePoints();
			computeColumns();
			this.addMouseListener(this);

		}

		@Override
		public void mouseClicked(final java.awt.event.MouseEvent e) {
			// Debug.println("X: " + e.getX());
			// Debug.println("Y: " + e.getY());
			int selectedColumnNum = -1;

			double tmpMax = max;
			double currentVal = min;

			if (e.getButton() == 1) {
				final double graphW = getWidth() - xOffset - rightLabels;
				final double diff = Math.abs(max - min);

				if (graphW == 0) {
					Debug.println("Window size too small");
					return;
				}

				final double x = (e.getX() - xOffset) * diff / graphW + min;
				// Debug.println("Mouse X: " + x);

				if (x < min || x > max)
					return;

				selectedColumnNum = 0;

				if (diff != 0) {
					final double step = diff / numColumns;

					while (x < currentVal || x > currentVal + step) {
						currentVal += step;
						selectedColumnNum++;
					}
					tmpMax = currentVal + step;
				}
				// Debug.println("Selected Column: " + selectedColumn);
				//
				// Adjust for bad computation precision
				//

				if (selectedColumnNum == numColumns - 1)
					tmpMax++;

				if (!(e.isControlDown() && multiSelect)) {
					for (int i = 0; i < selectedColumns.length; i++) {
						selectedColumns[i] = false;
					}
					setSelectedRegion(currentVal, tmpMax);
					selectedColumns[selectedColumnNum] = true;
				} else {
					if (selectedColumns[selectedColumnNum]) {
						clearSelectedRegion(currentVal, tmpMax);
						selectedColumns[selectedColumnNum] = false;
					} else {
						addSelectedRegion(currentVal, tmpMax);
						selectedColumns[selectedColumnNum] = true;
					}
				}
			} else {
				for (int i = 0; i < selectedColumns.length; i++)
					selectedColumns[i] = false;

				setSelectedRegion(Double.NaN, Double.NaN);
			}

			this.repaint();

			//
			// Call listeners
			//

			Debug.println("Select region " + currentVal + " to " + tmpMax);

		}

		public void setSelectedRegion(final double a, final double b) {
			for (final Object element : listeners) {
				final JJHistogramListener l = (JJHistogramListener) element;
				l.setSelectedRegion(a, b);
			}
		}

		public void clearSelectedRegion(final double a, final double b) {
			for (final Object element : listeners) {
				final JJHistogramListener l = (JJHistogramListener) element;
				l.clearSelectedRegion(a, b);
			}
		}

		public void addSelectedRegion(final double a, final double b) {
			for (final Object element : listeners) {
				final JJHistogramListener l = (JJHistogramListener) element;
				l.addSelectedRegion(a, b);
			}
		}

		public void addHistogramListener(final JJHistogramListener l) {
			listeners.add(l);
		}

		public void removeHistogramListener(final JJHistogramListener l) {
			listeners.remove(l);
		}

		@Override
		public void mousePressed(final java.awt.event.MouseEvent e) {
		}

		@Override
		public void mouseReleased(final java.awt.event.MouseEvent e) {
		}

		@Override
		public void mouseEntered(final java.awt.event.MouseEvent e) {
		}

		@Override
		public void mouseExited(final java.awt.event.MouseEvent e) {
		}

		void computeColumns() {
			columns = new double[numColumns];

			boolean hasSelection = false;

			if (selectedColumns != null) {
				for (final boolean selectedColumn : selectedColumns) {
					if (selectedColumn) {
						hasSelection = true;
					}
				}

				if (hasSelection)
					setSelectedRegion(Double.NaN, Double.NaN);
			}

			selectedColumns = new boolean[numColumns];

			for (int i = 0; i < selectedColumns.length; i++) {
				selectedColumns[i] = false;
			}

			if (hasSelection)
				setSelectedRegion(Double.NaN, Double.NaN);

			for (final double pt : pts) {
				int index = 0;
				if (max - min != 0)
					index = (int) ((pt - min) * numColumns / Math.abs(max - min)); // What
																					// column
																					// are
																					// we
																					// in
																					// ?
				// Debug.println("Index: " + index);
				// Debug.println("pts[i]: " + pts[i]);

				if (index >= columns.length) // If we are at the right end we
												// have to use the column
												// before.
					continue;
				if (index < 0)
					continue;

				columns[index]++;
			}
			columnsMax = columns[0];

			for (int i = 1; i < columns.length; i++) {
				columnsMax = Math.max(columnsMax, columns[i]);
			}

			if (columnsMax > 0) // If we have any data at all, normalize the
								// column sizes to 0 to 1.0
				for (int i = 0; i < columns.length; i++) {
					columns[i] /= columnsMax;
				}
			// Debug.println("Max column size: " + columnsMax);
		}

		void normalizePoints() {
			if (pts == null || pts.length == 0)
				return;
			absMin = absMax = pts[0];
			exponent = 1;

			for (int i = 1; i < pts.length; i++) {
				absMin = Math.min(absMin, pts[i]);
				absMax = Math.max(absMax, pts[i]);
			}
			Debug.println("absMin: " + absMin);
			Debug.println("absMax: " + absMax);

			if (!JJPoint.isValid(pivot)) {
				pivot = (absMax + absMin) / 2.0;
				Debug.println("Setting pivot to " + pivot);
			}

			Debug.println("absMin: " + absMin);
			Debug.println("absMax: " + absMax);
			final double pivotDist = Math.max(Math.abs(pivot - absMin), Math.abs(pivot - absMax));
			absMin = pivot - pivotDist;
			absMax = pivot + pivotDist;

			setMinMax();
		}

		// public Dimension getMinimumSize() {
		// return getPreferredSize();
		// }

		// public Dimension getMaximumSize() {
		// return getPreferredSize();
		// }

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(320, 200);
		}

		@Override
		public void paintComponent(final Graphics g) {
			// Debug.println("Repainting");

			final Dimension d = this.getSize();
			final int w = getWidth();
			final int h = getHeight();

			if (bimg == null || bimg.getWidth() != w || bimg.getHeight() != h) {
				bimg = (BufferedImage) this.createImage(w, h);
			}

			final Graphics2D big = bimg.createGraphics();
			big.setFont(font);

			final FontMetrics fm = big.getFontMetrics(font);
			final int ascent = fm.getAscent();
			final int descent = fm.getDescent();

			rightLabels = fm.stringWidth("  " + columnsMax) + 5;

			big.setBackground(this.getBackground());
			big.clearRect(0, 0, w, h);

			// .. Draw min/max strings ..
			big.setColor(Color.green);
			if (exponent == 1)
				big.drawString("Samples: " + pts.length + ", Maximum: " + columnsMax, 4.0f, ascent + 0.5f);
			else
				big.drawString("Samples: " + pts.length + ", Maximum: " + columnsMax + ", Range magnitue: " + exponent,
						4.0f, ascent + 0.5f);

			final float ssH = ascent + descent;
			final float remainingHeight = h - (ssH * 2) - 0.5f;

			// .. Draw Grid ..
			big.setColor(graphColor);
			final double graphX = xOffset;
			final double graphY = ssH;
			final double graphW = w - graphX - rightLabels;
			final double graphH = remainingHeight;
			graphOutlineRect.setRect(graphX, graphY, graphW, graphH);
			big.draw(graphOutlineRect);

			final double graphRow = Math.max(graphH / columnsMax, 3);

			// .. Draw row ..

			double nextValidY = graphY;
			int k = 0;

			for (double j = graphY; j <= graphH + graphY; j += graphRow) {
				graphLine.setLine((int) graphX, (int) j, (int) (graphX + graphW), (int) j);

				if (j >= nextValidY) {
					big.setColor(Color.green);
					final String label = " " + (int) (columnsMax - ((j - graphY) * (columnsMax / graphH)));

					big.drawString(label, (int) (graphX + graphW) + 5, (int) j);
					nextValidY += ssH + 2;
				} else
					big.setColor(graphColor);

				k++;
				big.draw(graphLine);
			}

			// .. Draw animated column movement ..

			final double columnInc = graphW / numColumns;

			k = 0;

			final NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(2);

			double nextValidX = graphX;

			for (double j = graphX; j <= graphW + graphX; j += columnInc) {
				graphLine.setLine((int) j, (int) graphY, (int) j, (int) (graphY + graphH));

				if (j >= nextValidX) {
					big.setColor(Color.green);
					final String label = "" + nf.format(min + k * (max - min) / numColumns);

					big.drawString(label, (int) j, h - descent);
					nextValidX = j + fm.stringWidth(label) + 5;

				} else
					big.setColor(graphColor);

				if (Math.abs(min + k * (max - min) / numColumns - pivot) <= 0.001) // 0
																					// line
					big.setColor(Color.red);

				k++;
				big.draw(graphLine);

			}

			// Draw histogram

			k = 0;

			for (double j = graphX; j < graphW + graphX; j += columnInc) {
				if (k < columns.length) {
					// Debug.println("Columns["+k+"]: " + columns[k]);

					final double startY = (int) (graphY + ((1.0 - columns[k]) * graphH));
					// Debug.println("StartY: " + startY);

					if (selectedColumns[k])
						big.setColor(histoSelectColor);
					else
						big.setColor(histoColor);

					big.fillRect((int) (j + 1), (int) (startY + 1), (int) (Math.max(columnInc - 1, 1)),
							(int) (graphY + graphH - (1 + startY)));
					k++;
				}

			}
			g.drawImage(bimg, 0, 0, this);
		}
	}
}
