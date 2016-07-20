/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionListener;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * JJFontSelectorBox.java
 *
 *
 * Created: Fri Feb 26 18:35:41 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

public class JJFontSelectorBox extends JPanel implements ActionListener, ListSelectionListener {
	JDialog dia;
	Font selFont = null;
	Font returnFont = null;
	JCheckBox bold, italic;

	JTextField testString = new JTextField(
			"\nABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz\n01234567890!@#$%^&*A()_+-=`~\\|{}[]:\";'<>?,./\n");

	JTextField sizeField = null;
	JList<String> fontList;

	public JJFontSelectorBox(final Font f, final Frame c) {
		setLayout(new BorderLayout());
		selFont = f;
		returnFont = f;

		dia = new JDialog(c, "JJFontSelector", true);
		setPreferredSize(new Dimension(400, 300));
		dia.setSize(new Dimension(400, 300));
		// setSize(new Dimension(400,00));
		createComponents();
		dia.getContentPane().add(this);
		dia.pack();
		if (selFont != null)
			fontList.setSelectedValue(selFont.getName(), true);
		dia.setVisible(true);
		;

	}

	public void createComponents() {
		// add(new JLabel(f.getName()), BorderLayout.WEST);
		final String fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

		fontList = new JList<>(fonts);
		fontList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fontList.addListSelectionListener(this);

		final JScrollPane sp = new JScrollPane(fontList);
		add(sp, BorderLayout.CENTER);
		if (selFont != null) {
			testString.setFont(selFont);
		}

		final JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());

		bottomPanel.add(testString, BorderLayout.CENTER);
		sizeField = new JJNumberField("10", 3);
		sizeField.addActionListener(this);
		if (selFont != null)
			sizeField.setText("" + selFont.getSize());

		final JPanel bottomTop = new JPanel();
		bottomTop.add(new JLabel("Size: "));
		bottomTop.add(sizeField);

		final URLClassLoader cl = (URLClassLoader) this.getClass().getClassLoader();
		URL url = cl.findResource("sunSupp/toolbarButtonGraphics/text/Bold16.gif");
		bold = new JCheckBox(new ImageIcon(url));
		url = cl.findResource("sunSupp/toolbarButtonGraphics/text/BoldSel16.gif");
		bold.setSelectedIcon(new ImageIcon(url));
		bold.setToolTipText("Bold");
		bold.addActionListener(this);

		bottomTop.add(bold);

		url = cl.findResource("sunSupp/toolbarButtonGraphics/text/Italic16.gif");
		italic = new JCheckBox(new ImageIcon(url));
		url = cl.findResource("sunSupp/toolbarButtonGraphics/text/ItalicSel16.gif");
		italic.setSelectedIcon(new ImageIcon(url));
		italic.setToolTipText("Italic");
		italic.addActionListener(this);
		bottomTop.add(italic);

		if (selFont != null) {
			bold.setSelected(selFont.isBold());
			italic.setSelected(selFont.isItalic());
		}

		bottomPanel.add(bottomTop, BorderLayout.NORTH);

		final JPanel bottomBottom = new JPanel();
		bottomBottom.setLayout(new BorderLayout());
		final JButton okB = new JButton("OK");
		final JButton cancelB = new JButton("Cancel");
		okB.addActionListener(this);
		cancelB.addActionListener(this);

		bottomBottom.add(okB, BorderLayout.WEST);
		bottomBottom.add(cancelB, BorderLayout.EAST);

		bottomPanel.add(bottomBottom, BorderLayout.SOUTH);

		add(bottomPanel, BorderLayout.SOUTH);

		// add(rightPanel, BorderLayout.EAST);
	}

	static public Font showDialog(final Font f, final Frame c) {
		final JJFontSelectorBox sel = new JJFontSelectorBox(f, c);

		return sel.returnFont;
	}

	public void redraw() {
		final int size = Integer.parseInt(sizeField.getText());
		final String name = fontList.getSelectedValue();
		int style = Font.PLAIN;
		if (bold.isSelected())
			style |= Font.BOLD;

		if (italic.isSelected())
			style |= Font.ITALIC;
		if (size > 0) {
			selFont = new Font(name, style, size);
			testString.setFont(selFont);
			testString.setEnabled(true);
		} else {
			testString.setEnabled(false);
		}

	}

	@Override
	public void actionPerformed(final java.awt.event.ActionEvent e) {
		if (e.getActionCommand().equals("OK")) {
			redraw();
			returnFont = selFont;
			dia.dispose();
		} else if (e.getActionCommand().equals("Cancel")) {
			dia.dispose();
		}

		redraw();
	}

	@Override
	public void valueChanged(final javax.swing.event.ListSelectionEvent e) {
		redraw();
	}

}
