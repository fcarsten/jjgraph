/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;

/**
 * JJBlenderWindow.java
 *
 *
 * Created: Mon Nov 15 11:12:12 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphEvent;
import org.carsten.jjgraph.graph.JJGraphListener;
import org.carsten.jjgraph.graph.JJGraphWindowImpl;
import org.carsten.jjgraph.graph.JJInspectable;
import org.carsten.jjgraph.graph.JJNode;
import org.carsten.jjgraph.util.Debug;

public class JJGraphAnimationWindow extends JJAnimationControls implements JJInspectable, JJGraphListener {
	private JJSceneContainer sceneContainer;

	private JButton startPos;
	private JButton endPos;

	private final JJGraph graph;
	private final JJGraphWindowImpl graphWindow;
	protected JCheckBox saveFrames;
	protected JCheckBox measure;
	protected JCheckBox saveGraphs;
	protected JTextField filePath;

	private boolean dataInvalid = false;

	@Override
	public String getTabName() {
		return "Marey";
	}

	@Override
	public void disableWidgets() {
		super.disableWidgets();
		endPos.setEnabled(false);
		startPos.setEnabled(false);
	}

	@Override
	public void enableWidgets() {
		super.enableWidgets();
		endPos.setEnabled(true);
		startPos.setEnabled(true);
	}

	@Override
	public void graphStructureChanged(final JJGraphEvent e) {
		if (e.getNode() != null) {
			dataInvalid = true;
		}
	}

	public void graphAppearanceChanged(final JJGraphEvent e) {
	}

	public JJGraphAnimationWindow(final JJGraphWindowImpl g) {
		graph = g.getGraph();
		graphWindow = g;

		graph.addStructureListener(this);
		graphAnimator = new JJGraphAnimator(new JJAnimatedCanvas(g));
	}

	@Override
	public void rewind() {
		graphWindow.openSubtask("Rewind");
		super.rewind();
		graphWindow.closeSubtask("Rewind");
	}

	private int frameCounter = 0;

	private void saveImage() {
		graphWindow.saveImage(filePath.getText() + (frameCounter / 1000) + "" + ((frameCounter % 1000) / 100) + ""
				+ ((frameCounter % 100) / 10) + "" + (frameCounter % 10) + "" + ".jpg");
		frameCounter++;
	}

	private void saveGraph() {
		graphWindow.savePovray(filePath.getText() + (frameCounter / 1000) + "" + ((frameCounter % 1000) / 100) + ""
				+ ((frameCounter % 100) / 10) + "" + (frameCounter % 10) + "" + ".pov");
		frameCounter++;
	}

	@Override
	public boolean next() {
		final boolean undo = graph.getUndoRecording();
		graph.setUndoRecording(false);

		final boolean tmpR = graphWindow.setRedraw(false);

		final boolean tmp2 = super.next();
		if (saveFrames.isSelected()) {
			saveImage();
		}
		if (saveGraphs.isSelected()) {
			saveGraph();
		}

		graphWindow.setRedraw(tmpR);

		graph.setUndoRecording(undo);
		return tmp2;

	}

	@Override
	public void stopAnimation() {
		super.stopAnimation();
		if (saveFrames.isSelected()) {
			saveImage();
		}
		if (saveGraphs.isSelected()) {
			saveGraph();
		}

		graphWindow.closeSubtask("Blending");
	}

	@Override
	public void startAnimation() {
		graphWindow.openSubtask("Blending");
		if (saveFrames.isSelected()) {
			saveImage();
		}
		if (saveGraphs.isSelected()) {
			saveGraph();
		}

		super.startAnimation();

	}

	JTextField interEdgeWeight;
	JTextField intraEdgeWeight;
	JTextField delaunayEdgeWeight;
	JTextField centerEdgeWeight;

	@Override
	protected JPanel createButtonRow() {
		final JPanel buttonPane = super.createButtonRow();
		final URLClassLoader cl = (URLClassLoader) this.getClass().getClassLoader();

		URL url = cl.findResource("sunSupp/toolbarButtonGraphics/media/StartPoint16.gif");
		startPos = new JButton(new ImageIcon(url));
		startPos.setToolTipText("Set current layout as first frame");
		startPos.addActionListener(this);

		url = cl.findResource("sunSupp/toolbarButtonGraphics/media/EndPoint16.gif");
		endPos = new JButton(new ImageIcon(url));
		endPos.setToolTipText("Set current layout as last frame");
		endPos.addActionListener(this);

		buttonPane.add(startPos);
		buttonPane.add(endPos);

		disableWidgets();
		startPos.setEnabled(true);

		final JPanel p = new JPanel();
		p.setLayout(new BorderLayout());

		p.add(buttonPane, BorderLayout.SOUTH);
		// p.add(edgeW, BorderLayout.NORTH);

		return p;
	}

	//
	// Abused for spring controls
	//

	protected JPanel createRelPosPanel() {
		final JPanel edgeW = new JPanel();
		edgeW.setLayout(new GridLayout(3, 2));
		edgeW.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2),
				BorderFactory.createEtchedBorder()));

		interEdgeWeight = new JTextField("" + JJSpringInterpol.getInterEdgeWeight(), 4);
		interEdgeWeight.addActionListener(this);

		intraEdgeWeight = new JTextField("" + JJSpringInterpol.getIntraEdgeWeight(), 4);
		intraEdgeWeight.addActionListener(this);

		delaunayEdgeWeight = new JTextField("" + JJSpringInterpol.getDelaunayEdgeWeight(), 4);
		delaunayEdgeWeight.addActionListener(this);

		centerEdgeWeight = new JTextField("" + JJSpringInterpol.getCenterEdgeWeight(), 4);
		centerEdgeWeight.addActionListener(this);

		edgeW.add(new JLabel("Spring forces:"));
		edgeW.add(new JLabel("  "));
		edgeW.add(new JLabel("  "));
		edgeW.add(new JLabel("  "));
		edgeW.add(new JLabel(" Inter plane: "));
		edgeW.add(interEdgeWeight);
		edgeW.add(new JLabel(" Intra plane: "));
		edgeW.add(intraEdgeWeight);
		edgeW.add(new JLabel(" Delaunay edges: "));
		edgeW.add(delaunayEdgeWeight);
		edgeW.add(new JLabel(" Edges to center: "));
		edgeW.add(centerEdgeWeight);

		return edgeW;
	}

	public void setEndPos() {
		// Debug.println("Setting end positions for scene: " + (
		// sceneContainer.getNumScenes()));

		final JJGraphAnimationNode animationNodes[] = sceneContainer.getAnimationNodes(sceneContainer.getNumScenes());

		if (animationNodes == null) {
			Debug.println("Couldn't find animationNodes for scene :" + (sceneContainer.getNumScenes()));
			return;
		}

		for (final JJGraphAnimationNode animationNode : animationNodes) {
			final JJAnimatedShape gn = animationNode.getAnimatedShape();
			if (animationNode.getAnimatedShape() instanceof JJMovingShape) {
				animationNode.setEndPosition(animationNode.getMovingShape().getCoords());
			}

			animationNode.setEndVisible(gn.getVisible());
			animationNode.setEndColor(gn.getColor());
			animationNode.getAnimatedShape().setAnimationEndData(animationNode.getUserData());
		}
		graphAnimator.addScene(graphWindow.getGraphCenter());
		enableWidgets();
	}

	public void setStartPos() {
		final JJAnimatedShape shapes[] = new JJAnimatedShape[graph.getNumNodes() + graph.getNumEdges()];
		int i = 0;

		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			shapes[i++] = iter.next().getGraphicNode(graphWindow);
		}

		for (final Iterator<JJEdge> iter = graph.edgeIterator(); iter.hasNext();) {
			shapes[i++] = iter.next().getGraphicEdge(graphWindow);
		}

		sceneContainer = graphAnimator.initStartPositions(shapes, graphWindow.getGraphCenter());

		endPos.setEnabled(true);
		dataInvalid = false;
	}

	@Override
	public void actionPerformed(final java.awt.event.ActionEvent e) {
		// Debug.println("Should blend now");
		// if(dataInvalid && (e.getSource()!= startPos))
		// updateData();

		if (e.getSource() == startPos) {
			setStartPos();
		} else if (e.getSource() == endPos) {
			setEndPos();
		} else if (e.getSource() == interEdgeWeight) {
			final double val = Double.parseDouble(interEdgeWeight.getText());
			graphAnimator.setInterEdgeWeight(val);
		} else if (e.getSource() == centerEdgeWeight) {
			final double val = Double.parseDouble(centerEdgeWeight.getText());
			graphAnimator.setCenterEdgeWeight(val);
		} else if (e.getSource() == intraEdgeWeight) {
			final double val = Double.parseDouble(intraEdgeWeight.getText());

			graphAnimator.setIntraEdgeWeight(val);
		} else if (e.getSource() == delaunayEdgeWeight) {
			final double val = Double.parseDouble(delaunayEdgeWeight.getText());

			graphAnimator.setDelaunayEdgeWeight(val);
		} else if (e.getSource() == measure) {
			graphAnimator.setMeasure(measure.isSelected());
		} else if ((e.getSource() == saveGraphs) || (e.getSource() == saveFrames)) {
		} else {
			super.actionPerformed(e);
		}
	}

	@Override
	public JPanel createCustomPanel() {
		final JPanel p = new JPanel();
		p.setLayout(new BorderLayout());

		final JPanel sw = new JPanel();
		saveFrames = new JCheckBox("Save Frames", false);
		saveFrames.addActionListener(this);
		saveGraphs = new JCheckBox("Save Povray", false);
		saveGraphs.addActionListener(this);
		measure = new JCheckBox("Measure", false);
		measure.addActionListener(this);

		final String fs = System.getProperty("file.separator");
		filePath = new JTextField("." + fs + "frame", 20);
		sw.add(saveFrames);
		sw.add(saveGraphs);
		sw.add(measure);

		p.add(createRelPosPanel(), BorderLayout.NORTH);
		p.add(sw, BorderLayout.CENTER);
		p.add(filePath, BorderLayout.SOUTH);

		return p;
	}

	// protected void updateData()
	// {
	// dataInvalid = false;
	// if(animationNodes == null)
	// return;

	// Set nodes = new HashSet(graph.getNodes());
	// JJGraphAnimationNode animationNodes2[] =
	// new JJGraphAnimationNode[nodes.size()];

	// // Copy the still valid nodes.
	// int index=0;

	// for(int i=0; i< animationNodes.length; i++){
	// JJNode n =
	// ((JJGraphicNode)animationNodes[i].getAnimatedShape()).getNode();

	// if(nodes.contains(n)){
	// animationNodes2[index++] = animationNodes[i];
	// nodes.remove(n);
	// }
	// }

	// // Now we have to add the new ones
	// for(Iterator iter = nodes.iterator(); iter.hasNext();){
	// JJMovingShape as = ((JJNode)iter.next()).getGraphicNode();
	// animationNodes2[index] = new JJGraphAnimationNode(as);
	// animationNodes2[index].setTargetPosition(as.getCoords());
	// animationNodes2[index].setEndVisible(as.isVisible());
	// animationNodes2[index].setEndColor(as.getColor());
	// animationNodes2[index].setStartVisible(false);

	// index++;
	// }

	// animationNodes = animationNodes2;
	// graphAnimator.setAnimationNodes(animationNodes,
	// graph.getWindow().getGraphCenter());
	// }

} // JJGraphAnimatorWindow
