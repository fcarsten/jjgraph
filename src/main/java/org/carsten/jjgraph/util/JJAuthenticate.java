/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

/**
 * JJAuthenticate.java
 *
 *
 * Created: Mon May 13 17:24:36 2002
 *
 * @author <a href="mailto:friedric@cs.newcastle.edu.au"></a>
 * @version
 */
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class JJAuthenticate extends JDialog implements ActionListener {
	boolean id = false;
	JButton ok, can;
	JTextField userField;
	private String userName;

	JPasswordField pwField;
	private String password;

	/**
	 * Get the value of password.
	 *
	 * @return value of password.
	 */
	public String getPassword() {
		return password;
	}

	public String getUserName() {
		return userName;
	}

	JJAuthenticate() {
		super((Frame) null, "CRC Password", true);
		getContentPane().setLayout(new GridLayout(0, 1));
		userField = new JTextField(15);
		pwField = new JPasswordField(15);
		pwField.setEchoChar('*');

		final JPanel userPane = new JPanel();
		userPane.setLayout(new FlowLayout());
		userPane.add(new JLabel("Username: "));
		userPane.add(userField);

		final JPanel passPane = new JPanel();
		passPane.setLayout(new FlowLayout());
		passPane.add(new JLabel("Password :"));
		passPane.add(pwField);

		getContentPane().add(userPane);
		getContentPane().add(passPane);

		addOKCancelPanel();
		createFrame();
		pack();
		setVisible(true);
	}

	void addOKCancelPanel() {
		final JPanel p = new JPanel();
		p.setLayout(new FlowLayout());
		createButtons(p);
		getContentPane().add(p);
	}

	void createButtons(final JPanel p) {
		p.add(ok = new JButton("OK"));
		ok.addActionListener(this);
		p.add(can = new JButton("Cancel"));
		can.addActionListener(this);
	}

	void createFrame() {
		final Dimension d = getToolkit().getScreenSize();
		setLocation(d.width / 4, d.height / 3);
	}

	@Override
	public void actionPerformed(final ActionEvent ae) {
		if (ae.getSource() == ok) {
			id = true;
			password = new String(pwField.getPassword());
			userName = userField.getText();
			dispose();
		} else if (ae.getSource() == can) {
			id = false;
			dispose();
		}
	}
}// JJAuthenticate
