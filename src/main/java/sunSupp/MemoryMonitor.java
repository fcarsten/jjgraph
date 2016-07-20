package sunSupp;
/*
 * @(#)MemoryMonitor.java	1.24 98/09/13
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
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

/**
 * Tracks Memory allocated & used, displayed in graph form.
 */
public class MemoryMonitor extends JPanel {

	public MonitorComponent mc;

	public MemoryMonitor() {
		setLayout(new BorderLayout());
		setBorder(new TitledBorder(new EtchedBorder(), "Memory Monitor"));
		mc = new MonitorComponent();
		add(mc);
	}

	public class MonitorComponent extends JPanel implements Runnable {
		public Thread thread;
		private int w, h;
		private BufferedImage bimg;
		private Graphics2D big;
		private final Font font = new Font("Times New Roman", Font.PLAIN, 11);
		private final Runtime r = Runtime.getRuntime();
		private int columnInc;
		private int pts[];
		private int ptNum;
		private int ascent, descent;
		private float freeMemory, totalMemory;
		private final Rectangle graphOutlineRect = new Rectangle();
		private final Rectangle2D mfRect = new Rectangle2D.Float();
		private final Rectangle2D muRect = new Rectangle2D.Float();
		private final Line2D graphLine = new Line2D.Float();
		private final Color graphColor = new Color(46, 139, 87);
		private final Color mfColor = new Color(0, 100, 0);

		public MonitorComponent() {
			this.setBackground(Color.black);
			this.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(final MouseEvent e) {
					if (thread == null)
						start();
					else
						stop();
				}
			});
			start();
		}

		@Override
		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		@Override
		public Dimension getMaximumSize() {
			return getPreferredSize();
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(140, 80);
		}

		@Override
		public void paint(final Graphics g) {

			if (big == null) {
				return;
			}

			big.setBackground(this.getBackground());
			big.clearRect(0, 0, w, h);

			r.gc();
			final float freeMemory = r.freeMemory();
			final float totalMemory = r.totalMemory();

			// .. Draw allocated and used strings ..
			big.setColor(Color.green);
			big.drawString(String.valueOf((int) totalMemory / 1024) + "K allocated", 4.0f, ascent + 0.5f);
			big.drawString(String.valueOf(((int) (totalMemory - freeMemory)) / 1024) + "K used", 4, h - descent);

			// Calculate remaining size
			final float ssH = ascent + descent;
			final float remainingHeight = h - (ssH * 2) - 0.5f;
			final float blockHeight = remainingHeight / 10;
			final float blockWidth = 20.0f;
			final float remainingWidth = w - blockWidth - 10;

			// .. Memory Free ..
			big.setColor(mfColor);
			final int MemUsage = (int) ((freeMemory / totalMemory) * 10);
			int i = 0;
			for (; i < MemUsage; i++) {
				mfRect.setRect(5, ssH + i * blockHeight, blockWidth, blockHeight - 1);
				big.fill(mfRect);
			}

			// .. Memory Used ..
			big.setColor(Color.green);
			for (; i < 10; i++) {
				muRect.setRect(5, ssH + i * blockHeight, blockWidth, blockHeight - 1);
				big.fill(muRect);
			}

			// .. Draw History Graph ..
			big.setColor(graphColor);
			final int graphX = 30;
			final int graphY = (int) ssH;
			final int graphW = w - graphX - 5;
			final int graphH = (int) remainingHeight;
			graphOutlineRect.setRect(graphX, graphY, graphW, graphH);
			big.draw(graphOutlineRect);

			final int graphRow = graphH / 10;

			// .. Draw row ..
			for (int j = graphY; j <= graphH + graphY; j += graphRow) {
				graphLine.setLine(graphX, j, graphX + graphW, j);
				big.draw(graphLine);
			}

			// .. Draw animated column movement ..
			final int graphColumn = graphW / 15;

			if (columnInc == 0) {
				columnInc = graphColumn;
			}

			for (int j = graphX + columnInc; j < graphW + graphX; j += graphColumn) {
				graphLine.setLine(j, graphY, j, graphY + graphH);
				big.draw(graphLine);
			}

			--columnInc;

			if (pts == null) {
				pts = new int[graphW];
				ptNum = 0;
			} else if (pts.length != graphW) {
				int tmp[] = null;
				if (ptNum < graphW) {
					tmp = new int[ptNum];
					System.arraycopy(pts, 0, tmp, 0, tmp.length);
				} else {
					tmp = new int[graphW];
					System.arraycopy(pts, pts.length - tmp.length, tmp, 0, tmp.length);
					ptNum = tmp.length - 2;
				}
				pts = new int[graphW];
				System.arraycopy(tmp, 0, pts, 0, tmp.length);
			} else {
				big.setColor(Color.yellow);
				pts[ptNum] = (int) (graphY + graphH * (freeMemory / totalMemory));
				for (int j = graphX + graphW - ptNum, k = 0; k < ptNum; k++, j++) {
					if (k != 0) {
						if (pts[k] != pts[k - 1]) {
							big.drawLine(j - 1, pts[k - 1], j, pts[k]);
						} else {
							big.fillRect(j, pts[k], 1, 1);
						}
					}
				}
				if (ptNum + 2 == pts.length) {
					// throw out oldest point
					for (int j = 1; j < ptNum; j++) {
						pts[j - 1] = pts[j];
					}
					--ptNum;
				} else {
					ptNum++;
				}
			}
			g.drawImage(bimg, 0, 0, this);
		}

		public void start() {
			thread = new Thread(this);
			thread.setPriority(Thread.MIN_PRIORITY);
			thread.setName("MemoryMonitor");
			thread.start();
		}

		public synchronized void stop() {
			thread = null;
			this.notify();
		}

		@Override
		public void run() {

			final Thread me = Thread.currentThread();

			while (thread == me && !this.isShowing() || this.getSize().width == 0) {
				try {
					Thread.sleep(500);
				} catch (final InterruptedException e) {
					thread = null;
					return;
				}
			}

			while (thread == me && this.isShowing()) {
				final Dimension d = this.getSize();
				if (d.width != w || d.height != h) {
					w = d.width;
					h = d.height;
					bimg = (BufferedImage) this.createImage(w, h);
					big = bimg.createGraphics();
					big.setFont(font);
					final FontMetrics fm = big.getFontMetrics(font);
					ascent = fm.getAscent();
					descent = fm.getDescent();
				}
				this.repaint();
				try {
					Thread.sleep(999);
				} catch (final InterruptedException e) {
					break;
				}
			}
			thread = null;
		}
	}

	public static void main(final String s[]) {
		final MemoryMonitor demo = new MemoryMonitor();
		final WindowListener l = new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				System.exit(0);
			}

			@Override
			public void windowDeiconified(final WindowEvent e) {
				demo.mc.start();
			}

			@Override
			public void windowIconified(final WindowEvent e) {
				demo.mc.stop();
			}
		};
		final Frame f = new Frame("Java2D Demo - MemoryMonitor");
		f.addWindowListener(l);
		f.add("Center", demo);
		f.pack();
		f.setSize(new Dimension(200, 200));
		f.setVisible(true);
	}
}
