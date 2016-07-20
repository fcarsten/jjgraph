/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

/**
 * JJProgressMonitor.java
 *
 *
 * Created: Tue Mar 25 13:07:26 2003
 *
 * @author <a href="mailto:carsten@it.usyd.edu.au"></a>
 * @version
 */
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

public class JJProgressMonitor extends Thread {
	int val;
	int max;
	int min;

	JFrame fenster;
	JProgressBar progressBar;

	int smtp = 0;
	int smtdp = 0;
	long startTime;

	public JJProgressMonitor(final JComponent c, final String s, final String s2, final int mi, final int ma,
			final int _smtp, final int _smtdp) {
		smtp = _smtp;
		smtdp = _smtdp;
		max = ma;
		min = mi;
		val = mi;

		start();
	}

	public boolean isCanceled() {
		return false;
	}

	@Override
	public void run() {
		startTime = System.currentTimeMillis();

		try {
			sleep(smtdp);
		} catch (final InterruptedException e) { // !!! Should restart if time
													// not up
		}

		double tmp = (System.currentTimeMillis() - startTime);

		tmp /= (val - min);
		tmp *= (max - val);
		if (tmp >= smtp) {
			fenster = new JFrame("Progress...");
			progressBar = new JProgressBar(min, max);
			fenster.getContentPane().add(progressBar);
			progressBar.setValue(val);
			fenster.pack();
			fenster.setVisible(true);

			while ((!isCanceled()) && (val < max)) {
				try {
					sleep(500);
					if (progressBar.getValue() != val) {
						progressBar.setValue(val);
						repaint();
					}
				} catch (final InterruptedException e) {
				}

			}
			fenster.dispose();

		}

	}

	public void repaint() {
		if (fenster != null) {
			// fenster.repaint(500);
			fenster.getContentPane().repaint(50);
		}

	}

	public void setProgress(final int i) {
		val = i;
	}

}// JJProgressMonitor
