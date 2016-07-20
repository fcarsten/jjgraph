/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;

/**
 * JJNodeProp.java
 *
 *
 * Created: Mon Nov 15 11:12:12 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindowImpl;
import org.carsten.jjgraph.graph.JJGraphicNodeCircle;
import org.carsten.jjgraph.graph.JJGraphicNodeDefault;
import org.carsten.jjgraph.graph.JJGraphicNodeImage;
import org.carsten.jjgraph.graph.JJGraphicNodeImageP;
import org.carsten.jjgraph.graph.JJGraphicNodeSquare;
import org.carsten.jjgraph.graph.JJInspectable;

public class JJNodeProp extends JPanel implements JJInspectable, ActionListener, DropTargetListener {
	private final static int PIMAGES = 15;

	private final JJGraph graph;
	private final JJGraphWindowImpl fenster;
	private JComboBox<String> shapeBox;
	private JButton colourButton;
	static private Image defImage = null;
	static private URL defaultURL = null;
	static private Image defImageP[] = new Image[PIMAGES];
	private Image image = null;

	private String imageDir = "";
	private final boolean dropping = false;
	private URL imageURL = null;

	public final static int RECTANGLE = 0;
	public final static int SQUARE = 1;
	public final static int CIRCLE = 2;
	public final static int IMAGE = 3;

	@Override
	public void actionPerformed(final java.awt.event.ActionEvent e) {
		final boolean tmpRedraw = fenster.setRedraw(false);

		if (e.getSource() == shapeBox) {
			switch (shapeBox.getSelectedIndex()) {
			case RECTANGLE: {
				// Debug.println("Switching to rectangle nodes");
				fenster.setGraphicNodeAppearanceClass(JJGraphicNodeDefault.class);
				break;
			}
			case SQUARE: {
				// Debug.println("Switching to square nodes");
				fenster.setGraphicNodeAppearanceClass(JJGraphicNodeSquare.class);
				break;
			}
			case CIRCLE: {
				// Debug.println("Switching to circle nodes");
				fenster.setGraphicNodeAppearanceClass(JJGraphicNodeCircle.class);
				break;
			}
			case IMAGE: {
				// Debug.println("Switching to image nodes");
				fenster.setMasterImage(image);
				fenster.setMasterImageURL(imageURL);

				Toolkit.getDefaultToolkit().prepareImage(image, -1, -1, this);

				if (image == defImage) {
					for (int i = 0; i < PIMAGES; i++) {
						Toolkit.getDefaultToolkit().prepareImage(defImageP[i], -1, -1, this);
					}

					fenster.setGraphicNodeAppearanceClass(JJGraphicNodeImageP.class);
				} else
					fenster.setGraphicNodeAppearanceClass(JJGraphicNodeImage.class);
				break;
			}
			default: {
			}
			}
			repaint();
		} else if (e.getSource() == colourButton) {
			final Color tmpC = JColorChooser.showDialog(this, "Choose Node Color", fenster.getNodeColor());
			if (tmpC != null) {
				colourButton.setBackground(tmpC);
				fenster.setNodeColor(tmpC);
			}
		}
		fenster.setRedraw(tmpRedraw);
	}

	void selectImageFile() {
		JFileChooser chooser;
		if (imageDir.equals("")) {
			try {
				chooser = new JFileChooser(System.getProperty("user.dir"));
			} catch (final Exception e) {
				Debug.println("Couldn't access home directory: " + e + "" + e.getClass());
				return;
			}
		} else {
			chooser = new JFileChooser(imageDir);
		}

		// chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(false);
		chooser.setAcceptAllFileFilterUsed(false);

		// Note: source for ExampleFileFilter can be found in FileChooserDemo,
		// under the demo/jfc directory in the Java 2 SDK, Standard Edition.

		final JJImageFileFilter filter = new JJImageFileFilter();
		filter.addExtension("jpg");
		filter.addExtension("jpeg");
		filter.addExtension("gif");
		filter.addExtension("png");
		filter.setDescription("Images");
		chooser.setFileFilter(filter);
		final int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			final File imageFile = chooser.getSelectedFile();
			try {
				imageURL = imageFile.toURI().toURL();
			} catch (final MalformedURLException e) {
				imageURL = null;
			}

			imageDir = imageFile.getAbsoluteFile().getParent();
			image = Toolkit.getDefaultToolkit().createImage(imageFile.getAbsolutePath());
		}
	}

	@Override
	public JPanel createTab() {
		final JPanel arg = new JPanel();
		arg.setLayout(new BorderLayout());
		arg.add(new JLabel("Set node properties"), BorderLayout.NORTH);

		final String shapeList[] = { "Rectangle", "Square", "Circle", "Image" };
		shapeBox = new JComboBox<>(shapeList);
		shapeBox.setSelectedIndex(0);
		shapeBox.addActionListener(this);

		final JPanel controls = new JPanel();

		controls.add(new JLabel("Shape: "));
		controls.add(shapeBox);

		controls.add(new JLabel("Colour: "));
		colourButton = new JButton("   ");
		colourButton.setBackground(fenster.getNodeColor());
		colourButton.addActionListener(this);
		controls.add(colourButton);

		arg.add(controls, BorderLayout.CENTER);

		final JPanel p = new JPanel();
		// p.setLayout(new BorderLayout());
		// p.add(arg, BorderLayout.NORTH);

		// return p;
		setLayout(new BorderLayout());
		add(arg, BorderLayout.NORTH);
		final JJPreviewPanel pp = new JJPreviewPanel();

		add(pp, BorderLayout.CENTER);

		pp.setDropTarget(new DropTarget(pp, this));
		pp.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() == 2) {
					selectImageFile();
					repaint();
				}
			}
		});

		return this;
	}

	class JJPreviewPanel extends JPanel {
		@Override
		public void paintComponent(final Graphics g) {
			super.paintComponent(g);
			final double w = super.getWidth();
			final double h = super.getHeight();

			g.setColor(Color.white);

			g.fillRect(0, 0, (int) w, (int) h);

			if (image != null) {
				double iw = image.getWidth(this);
				double ih = image.getHeight(this);

				if ((iw > 0) && (ih > 0)) {
					if ((iw > w) || (ih > h)) {
						final double wr = w / iw;
						final double hr = h / ih;
						final double s = Math.min(wr, hr);
						iw *= s;
						ih *= s;
					}

					if (shapeBox.getSelectedIndex() == IMAGE) {
						g.drawImage(image, (int) (w / 2 - iw / 2), (int) (h / 2 - ih / 2), (int) iw, (int) ih, this);

						g.setColor(Color.black);

						g.drawRect((int) (w / 2 - iw / 2), (int) (h / 2 - ih / 2), (int) iw, (int) ih);
					}
				}
			}
		}

		@Override
		public boolean imageUpdate(final Image img, final int infoflags, final int x, final int y, final int width,
				final int height) {
			if (image != img)
				return false;

			return super.imageUpdate(img, infoflags, x, y, width, height);
		}

	}

	static {
		final URLClassLoader cl = (URLClassLoader) JJNodeProp.class.getClassLoader();

		URL url = cl.findResource("org/carsten/jjgraph/icons/default.gif");
		defImage = Toolkit.getDefaultToolkit().createImage(url);
		defaultURL = url;

		for (int i = 0; i < PIMAGES; i++) {
			url = cl.findResource("org/carsten/jjgraph/icons/default_p" + i + ".gif");
			defImageP[i] = Toolkit.getDefaultToolkit().createImage(url);
			// Debug.println("Loaded default Images");
		}
	}

	public JJNodeProp(final JJGraphWindowImpl f) {
		fenster = f;
		graph = fenster.getGraph();

		image = defImage;
		imageURL = defaultURL;
	}

	@Override
	public String getTabName() {
		return "Node Properties";
	}

	@Override
	public void drop(final java.awt.dnd.DropTargetDropEvent e) {
		// Debug.println("drop image");
		final DropTargetContext targetContext = e.getDropTargetContext();

		boolean outcome = false;

		if ((e.getSourceActions() & DnDConstants.ACTION_COPY) != 0)
			e.acceptDrop(DnDConstants.ACTION_COPY);
		else {
			e.rejectDrop();
			return;
		}

		final DataFlavor[] dataFlavors = e.getCurrentDataFlavors();
		DataFlavor transferDataFlavor = null;

		// Debug.println(DataFlavor.javaFileListFlavor.getMimeType());

		for (final DataFlavor dataFlavor : dataFlavors) {
			// Debug.println(dataFlavors[i].getMimeType());
			if (DataFlavor.javaFileListFlavor.equals(dataFlavor)) {
				// Debug.println("matched");
				transferDataFlavor = dataFlavor;
				break;
			}
		}

		if (transferDataFlavor != null) {
			final Transferable t = e.getTransferable();
			// InputStream is = null;
			java.util.List<File> fl = null;

			try {
				// Debug.println("get list");
				fl = (java.util.List<File>) t.getTransferData(transferDataFlavor);
			} catch (final IOException ioe) {
				// ioe.printStackTrace();
				Debug.println(ioe.getMessage());
				targetContext.dropComplete(false);

				return;
			} catch (final UnsupportedFlavorException ufe) {
				// ufe.printStackTrace();
				Debug.println(ufe.getMessage());
				targetContext.dropComplete(false);

				return;
			}

			if (fl != null) {
				outcome = true;
				final JJImageFileFilter ff = new JJImageFileFilter();

				for (final Iterator<File> iter = fl.listIterator(); iter.hasNext();) {
					final File tmpF = iter.next();
					// Debug.println(tmpF.getAbsolutePath());
					if (ff.accept(tmpF)) {
						final File imageFile = tmpF;
						try {
							imageURL = tmpF.toURI().toURL();
						} catch (final MalformedURLException e2) {
							imageURL = null;
						}

						imageDir = imageFile.getAbsoluteFile().getParent();
						image = Toolkit.getDefaultToolkit().createImage(imageFile.getAbsolutePath());
						break;
					}
				}
			}
		}

		shapeBox.setSelectedIndex(IMAGE);

		targetContext.dropComplete(outcome);
		repaint();

	}

	public static Image getPImage() {
		return defImageP[(int) (Math.random() * PIMAGES)];
	}

	class JJImageFileFilter extends sunSupp.ExampleFileFilter {
		public JJImageFileFilter() {
			addExtension("jpg");
			addExtension("jpeg");
			addExtension("gif");
			addExtension("png");
			setDescription("Images");
		}
	}

	@Override
	public void dropActionChanged(final java.awt.dnd.DropTargetDragEvent e) {
	}

	@Override
	public void dragOver(final java.awt.dnd.DropTargetDragEvent e) {
	}

	@Override
	public void dragEnter(final java.awt.dnd.DropTargetDragEvent e) {
	}

	@Override
	public void dragExit(final java.awt.dnd.DropTargetEvent e) {
	}

} // JJNodeProp
