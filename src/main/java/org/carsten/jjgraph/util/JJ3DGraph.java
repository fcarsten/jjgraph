/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

import java.awt.BorderLayout;
import java.awt.DisplayMode;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Iterator;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.media.j3d.Group;
import javax.media.j3d.Light;
import javax.media.j3d.Material;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.View;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphEvent;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJGraphWindowListener;
import org.carsten.jjgraph.graph.JJGraphicEdge;
import org.carsten.jjgraph.graph.JJGraphicNode;
import org.carsten.jjgraph.graph.JJNode;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.PlatformGeometry;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;

public class JJ3DGraph implements ActionListener, ItemListener, JJGraphWindowListener {

	// private JJGraphWindow graphWindow = null;
	private final JJGraph graph;

	protected HashMap<JJGraphicNode, TransformGroup> nodeTo3D = new HashMap<>();
	protected HashMap<JJGraphicEdge, TransformGroup> edgeTo3D = new HashMap<>();
	protected float xOff = 0f;
	protected float yOff = 0f;
	protected float zOff = 0f;
	protected Group graphScene;
	protected float scale = 200.0f;

	private JFrame frame;
	private Canvas3D canvas;

	private final boolean headlightOnOff = true;

	private DirectionalLight headlight = null;
	protected boolean fullScreen = false;

	protected TransformGroup viewTransform = null;
	protected TransformGroup sceneTransform = null;
	private JCheckBoxMenuItem headlightMenuItem = null;

	SimpleUniverse universe;
	BranchGroup sceneRoot;
	JCheckBoxMenuItem stereoButton;
	DisplayMode oldMode;
	GraphicsDevice myDevice;
	BoundingSphere allBounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 1000000000.0);

	public void initialize() {
		frame = new JFrame();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				Debug.println("Releasing locales");

				universe.removeAllLocales();
			}
		});
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		frame.setTitle("JJGraph 3D : " + graph.getName());
		frame.getContentPane().setLayout(new BorderLayout());

		myDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

		oldMode = myDevice.getDisplayMode();
		final GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();
		// template.setStereo(template.PREFERRED);

		// Get the GraphicsConfiguration that best fits our needs.
		final GraphicsConfiguration gcfg = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getBestConfiguration(template);
		canvas = new Canvas3D(gcfg);

		canvas.setSize(oldMode.getWidth(), oldMode.getHeight());
		canvas.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(final java.awt.event.KeyEvent e) {
				if (e.getKeyChar() == 'q') {
					Debug.println("Closing 3D Window");
					frame.dispose();
					if (fullScreen) {
						myDevice.setDisplayMode(oldMode);
						myDevice.setFullScreenWindow(null);
					}
				}
			}

		});

		frame.getContentPane().add("Center", canvas);

		// Debug.println(""+canvas.queryProperties());
		buildMenuBar(frame);
		if (fullScreen) {
			try {
				frame.setUndecorated(true);
				final DisplayMode dm = new DisplayMode(800, 600, 32, 100);

				frame.setSize(dm.getWidth(), dm.getHeight());
				frame.setResizable(false);
				myDevice.setFullScreenWindow(frame);
				myDevice.setDisplayMode(dm);
				frame.validate();

			} catch (final Exception e) {
				Debug.println("Fullscreen exception: " + e);
				myDevice.setDisplayMode(oldMode);
				myDevice.setFullScreenWindow(null);
				fullScreen = false;
			}
		}
	}

	public JJ3DGraph(final JJGraphWindow theGraphWindow) {
		graph = theGraphWindow.getGraph();

		initialize();
		buildUniverse(theGraphWindow);
		if (!fullScreen) {
			frame.pack();
			frame.setVisible(true);
		}
	}

	@Override
	public void actionPerformed(final ActionEvent event) {
		final String arg = event.getActionCommand();
		if (arg.equals("Reset view"))
			reset();
		else if (arg.equals("stereoView")) {
			if (stereoButton.isSelected())
				canvas.setStereoEnable(true);
			else
				canvas.setStereoEnable(false);
		}
	}

	private void buildMenuBar(final JFrame f) {
		// Build the menubar
		javax.swing.JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		final JMenuBar menuBar = new JMenuBar();
		f.setJMenuBar(menuBar);

		// View menu
		final JMenu m = new JMenu("View");
		m.getAccessibleContext().setAccessibleDescription("3D menu");
		menuBar.add(m);

		JMenuItem tmpI;

		tmpI = new JMenuItem("Reset view");
		tmpI.setActionCommand("Reset view");
		tmpI.addActionListener(this);
		m.add(tmpI);

		stereoButton = new JCheckBoxMenuItem("Stereo view");
		stereoButton.setActionCommand("stereoView");
		stereoButton.addActionListener(this);
		m.add(stereoButton);

		if (!canvas.getStereoAvailable()) {
			stereoButton.setEnabled(false);
			Debug.println("Stereo vision not supported by hardware");
		}

		m.addSeparator();

		headlightMenuItem = new JCheckBoxMenuItem("Headlight on/off");
		headlightMenuItem.addItemListener(this);
		headlightMenuItem.setState(headlightOnOff);
		m.add(headlightMenuItem);

		menuBar.add(m);
	}

	/**
	 * Handles on/off checkbox items on a standard menu.
	 *
	 * @param event
	 *            an ItemEvent indicating what requires handling
	 */

	@Override
	public void itemStateChanged(final ItemEvent event) {
		final Object src = event.getSource();
		boolean state;
		if ((src != null) && (src == headlightMenuItem)) {
			state = headlightMenuItem.getState();
			if (headlight != null)
				headlight.setEnable(state);
		}
	}

	// These methods will be replaced
	// Set the view position and direction
	// public void setViewpoint( Point3f position, Vector3f direction )
	// {
	// Transform3D t = new Transform3D( );
	// t.set( new Vector3f( position ) );
	// viewTransform.setTransform( t );
	// // how to set direction?
	// }

	// Reset transforms
	public void reset() {
		final Transform3D trans = new Transform3D();
		sceneTransform.setTransform(trans);
		trans.set(new Vector3f(0.0f, 0.0f, 10.0f));
		viewTransform.setTransform(trans);
		// setNavigationType( navigationType );
	}

	// --------------------------------------------------------------
	// SCENE CONTENT
	// --------------------------------------------------------------

	/**
	 * Builds the 3D universe by constructing a virtual universe (via
	 * SimpleUniverse), a view platform (via SimpleUniverse), and a view (via
	 * SimpleUniverse). A headlight is added and a set of behaviors initialized
	 * to handle navigation types.
	 */
	protected void buildUniverse(final JJGraphWindow graphWindow) {
		//
		// Create a SimpleUniverse object, which builds:
		//
		// - a Locale using the given hi-res coordinate origin
		//
		// - a ViewingPlatform which in turn builds:
		// - a MultiTransformGroup with which to move the
		// the ViewPlatform about
		//
		// - a ViewPlatform to hold the view
		//
		// - a BranchGroup to hold avatar geometry (if any)
		//
		// - a BranchGroup to hold view platform
		// geometry (if any)
		//
		// - a Viewer which in turn builds:
		// - a PhysicalBody which characterizes the user's
		// viewing preferences and abilities
		//
		// - a PhysicalEnvironment which characterizes the
		// user's rendering hardware and software
		//
		// - a JavaSoundMixer which initializes sound
		// support within the 3D environment
		//
		// - a View which renders the scene into a Canvas3D
		//
		// All of these actions could be done explicitly, but
		// using the SimpleUniverse utilities simplifies the code.
		//
		universe = new SimpleUniverse(canvas); // Canvas3D into which to draw
		universe.getViewer().getView().setTransparencySortingPolicy(View.TRANSPARENCY_SORT_GEOMETRY);
		universe.getViewer().getView().setDepthBufferFreezeTransparent(false);

		//
		// Get the viewing platform created by SimpleUniverse.
		// From that platform, get the inner-most TransformGroup
		// in the MultiTransformGroup. That inner-most group
		// contains the ViewPlatform. It is this inner-most
		// TransformGroup we need in order to:
		//
		// - change the viewing direction in a "walk" style
		//
		// The inner-most TransformGroup's transform will be
		// changed by the walk behavior (when enabled).
		//
		final ViewingPlatform viewingPlatform = universe.getViewingPlatform();
		viewTransform = viewingPlatform.getViewPlatformTransform();

		final PlatformGeometry pg = new PlatformGeometry();

		headlight = new DirectionalLight();
		headlight.setColor(White);
		headlight.setDirection(new Vector3f(0.0f, 0.0f, -1.0f));
		headlight.setInfluencingBounds(allBounds);
		headlight.setCapability(Light.ALLOW_STATE_WRITE);
		pg.addChild(headlight);
		viewingPlatform.setPlatformGeometry(pg);

		sceneRoot = new BranchGroup();

		// GraphBehavior graphBehavior = new GraphBehavior();
		final BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 10000000000000.0);
		// graphBehavior.setSchedulingBounds(bounds);
		// sceneRoot.addChild(graphBehavior);

		createScene(sceneRoot, graphWindow);

		//
		// Compile the scene branch group and add it to the
		// SimpleUniverse.
		//

		sceneRoot.compile();
		universe.addBranchGraph(sceneRoot);

		reset();

	}

	public void createScene(final BranchGroup sr, final JJGraphWindow graphWindow) {
		//
		// Create the 3D content BranchGroup, containing:
		//
		// - a TransformGroup who's transform the examine behavior
		// will change (when enabled).
		//
		// - 3D geometry to view
		//
		// Build the scene root

		// BranchGroup backBranch = createBackground();

		// BoundingSphere allBounds = new BoundingSphere(new Point3d( 0.0, 0.0,
		// 0.0 ),
		// 100000.0 );
		final Background back = new Background();
		// Color col = graphWindow.getBackground();

		back.setColor(new Color3f(0, 0, 0));
		back.setApplicationBounds(allBounds);

		sr.addChild(back);

		// Build a transform that we can modify
		sceneTransform = new TransformGroup();
		sceneTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		sceneTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		sceneTransform.setCapability(Group.ALLOW_CHILDREN_EXTEND);

		// Create bounds for the background and lights
		// BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0),
		// 10000.0f);

		final AmbientLight lightA = new AmbientLight();
		lightA.setInfluencingBounds(allBounds);
		sr.addChild(lightA);

		// Add mouse behaviors
		// Create the rotate behavior node
		final MouseRotate rotBehavior = new MouseRotate();
		rotBehavior.setTransformGroup(sceneTransform);
		sceneTransform.addChild(rotBehavior);
		rotBehavior.setSchedulingBounds(allBounds);

		// Create the zoom behavior node
		final MouseZoom zoomBehavior = new MouseZoom();
		zoomBehavior.setTransformGroup(sceneTransform);
		sceneTransform.addChild(zoomBehavior);
		zoomBehavior.setSchedulingBounds(allBounds);

		// Create the translate behavior node
		final MouseTranslate transBehavior = new MouseTranslate();
		transBehavior.setTransformGroup(sceneTransform);
		sceneTransform.addChild(transBehavior);
		transBehavior.setSchedulingBounds(allBounds);

		//
		// Build the scene, add it to the transform, and add
		// the transform to the scene root
		//
		buildGraph(graphWindow);
		sceneTransform.addChild(graphScene);
		sr.addChild(sceneTransform);
	}

	/**
	 * Builds the scene. Example application subclasses should replace this
	 * method with their own method to build 3D content.
	 *
	 * @return a Group containing 3D content to display
	 */

	public void updateGraph() {
	}

	public void buildGraph(final JJGraphWindow graphWindow) {
		// Build the scene group containing nothing

		graphScene = new BranchGroup();

		graphScene.setCapability(Group.ALLOW_CHILDREN_EXTEND);

		float numNodes = 0; // (float)graph.getNumNodes();

		// The barry center of the graph should be ay 0,0,0

		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			final JJNode tmpN = iter.next();
			final JJGraphicNode gn = tmpN.getGraphicNode(graphWindow);

			if (gn.isVisible()) {
				numNodes++;
				xOff -= ((float) gn.getX() / scale);
				yOff -= ((float) gn.getY() / scale);
				zOff -= ((float) gn.getZ() / scale);
			}
		}
		xOff /= numNodes;
		yOff /= numNodes;
		zOff /= numNodes;
		nodeTo3D.clear();

		// Add all nodes
		// if((window==null)||window.getShowNodes())
		for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
			final JJNode tmpN = iter.next();
			final JJGraphicNode gn = tmpN.getGraphicNode(graphWindow);
			createNode(gn);

		}

		// add all edges
		// if((window==null)||window.getShowEdges())
		for (final Iterator<JJEdge> iter = graph.edgeIterator(); iter.hasNext();) {

			final JJEdge tmpE = iter.next();
			final JJGraphicEdge ge = tmpE.getGraphicEdge(graphWindow);
			// if((window==null)||window.isDrawn(ge)){
			createEdge(ge);
			// }
		}
		// return scene;
	}

	void createNode(final JJGraphicNode gn) {
		if (gn.isVisible()) {
			// Debug.println("Creating node");
			final Transform3D t3dKnot = new Transform3D();
			t3dKnot.set(new Vector3f((float) gn.getX() / scale + xOff, (float) gn.getY() / scale + yOff,
					(float) gn.getZ() / scale + zOff));
			final TransformGroup transformGroup = new TransformGroup(t3dKnot);
			transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
			transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
			transformGroup.setCapability(Group.ALLOW_CHILDREN_READ);
			nodeTo3D.put(gn, transformGroup);

			final Appearance ap = createAppearance(gn.getColor().getRed() / 255.0f, gn.getColor().getGreen() / 255.0f,
					gn.getColor().getBlue() / 255.0f, 50.0f);

			// if(gn.getColor().getAlpha() != 255){
			final TransparencyAttributes ta = new TransparencyAttributes();
			ta.setTransparency(1 - gn.getColor().getAlpha() / 255.0f);
			ta.setTransparencyMode(TransparencyAttributes.NICEST);
			ta.setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);

			ap.setTransparencyAttributes(ta);
			// }

			final Sphere sphere = new Sphere(0.05f, ap);
			sphere.getShape().setCapability(Shape3D.ALLOW_APPEARANCE_READ);

			transformGroup.addChild(sphere);

			final BranchGroup bg = new BranchGroup();
			bg.addChild(transformGroup);
			graphScene.addChild(bg);
			// scene.addChild(transformGroup);
		}
	}

	void createEdge(final JJGraphicEdge ge) {
		// Debug.println("Creating edge");
		final JJGraphWindow graphWindow = ge.getWindow();

		final JJEdge tmpE = ge.getEdge();

		final JJGraphicNode gs = tmpE.getSource().getGraphicNode(graphWindow);
		final JJGraphicNode gt = tmpE.getTarget().getGraphicNode(graphWindow);

		final double sx = gs.getX() / scale + xOff;
		final double sy = gs.getY() / scale + yOff;
		final double sz = gs.getZ() / scale + zOff;

		final double tx = gt.getX() / scale + xOff;
		final double ty = gt.getY() / scale + yOff;
		final double tz = gt.getZ() / scale + zOff;

		final float dx = (float) (tx - sx);
		final float dy = (float) (ty - sy);
		final float dz = (float) (tz - sz);
		final double length = Math.sqrt(dx * dx + dy * dy + dz * dz);

		final Vector3f tmpV = new Vector3f(dx, dy, dz);

		final double theta = tmpV.angle(new Vector3f(0f, 01f, 0f));

		final Transform3D t3dKnot1 = new Transform3D();
		t3dKnot1.setScale(new Vector3d(1.0, length, 1.0));

		t3dKnot1.setRotation(new AxisAngle4f(-dz, 0f, dx, (float) -theta));

		final Transform3D trans = t3dKnot1;
		trans.setTranslation(new Vector3f((float) sx + dx / 2f, (float) sy + dy / 2f, (float) sz + dz / 2f));

		final TransformGroup transformGroup = new TransformGroup(trans);
		transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		transformGroup.setCapability(Group.ALLOW_CHILDREN_READ);

		final Appearance ap = createAppearance(ge.getColor().getRed() / 255.0f, ge.getColor().getGreen() / 255.0f,
				ge.getColor().getBlue() / 255.0f, 50.0f);

		// if(ge.getColor().getAlpha() != 255){
		final TransparencyAttributes ta = new TransparencyAttributes();
		ta.setTransparency(1 - ge.getColor().getAlpha() / 255.0f);
		ta.setTransparencyMode(TransparencyAttributes.NICEST);
		ta.setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);
		ap.setTransparencyAttributes(ta);
		// }

		final Cylinder cyl = new Cylinder((float) (tmpE.getWeight() / 100.0), 1.0f, ap); // (float)(tmpE.getWeight()/100.0f),

		cyl.getShape(Cylinder.BODY).setCapability(Shape3D.ALLOW_APPEARANCE_READ);

		// (float)length, ap);
		cyl.setCapability(Primitive.ENABLE_APPEARANCE_MODIFY);

		transformGroup.addChild(cyl);

		edgeTo3D.put(ge, transformGroup);
		final BranchGroup bg = new BranchGroup();
		bg.addChild(transformGroup);

		graphScene.addChild(bg);
	}

	Appearance createAppearance(final float r, final float g, final float b, final float s) {
		final Appearance appear = new Appearance();
		appear.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
		appear.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
		appear.setCapability(Appearance.ALLOW_MATERIAL_READ);
		appear.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_READ);

		final Material material = new Material();
		material.setDiffuseColor(r, g, b);
		material.setAmbientColor(r / 4.0f, g / 4.0f, b / 4.0f);
		material.setShininess(s);
		material.setCapability(Material.ALLOW_COMPONENT_READ);
		material.setCapability(Material.ALLOW_COMPONENT_WRITE);

		appear.setMaterial(material);

		return appear;
	}

	public void nodeAppearanceChanged(final JJGraphEvent e) {
		final JJGraphicNode gn = (JJGraphicNode) e.getSource();
		nodeAppearanceChanged(gn);
	}

	public void nodeAppearanceChanged(final JJGraphicNode gn) {
		final TransformGroup tg = nodeTo3D.get(gn);
		if ((tg == null) && gn.isVisible()) {

			Debug.println("Don't know the node yet");
			createNode(gn);
		} else if (tg != null) {
			Debug.println("Moving node");
			// switch(e.getType()){
			// case JJGraphEvent.NODE_MOVE_EVENT:
			// {
			final Transform3D t3dKnot = new Transform3D();
			t3dKnot.set(new Vector3f((float) gn.getX() / scale + xOff, (float) gn.getY() / scale + yOff,
					(float) gn.getZ() / scale + zOff));
			tg.setTransform(t3dKnot);
			// break;
			// }
			// case JJGraphEvent.NODE_COLOUR_EVENT:
			// {
			// Debug.println("Changing colour");
			final Primitive node = (Primitive) tg.getChild(0);
			final Appearance ap = node.getAppearance();

			final float r = gn.getColor().getRed() / 255.0f;
			final float g = gn.getColor().getGreen() / 255.0f;
			final float b = gn.getColor().getBlue() / 255.0f;

			final Material material = ap.getMaterial();// new Material();
			material.setDiffuseColor(r, g, b);
			material.setAmbientColor(r / 4.0f, g / 4.0f, b / 4.0f);
			material.setShininess(50.0f);
			ap.setMaterial(material);

			// if(gn.getColor().getAlpha() != 255){
			final TransparencyAttributes ta = ap.getTransparencyAttributes();// new
																				// TransparencyAttributes(
																				// );
			ta.setTransparency(1 - gn.getColor().getAlpha() / 255.0f);
			// ta.setTransparencyMode( TransparencyAttributes.NICEST );
			ap.setTransparencyAttributes(ta);
			// }
			// node.setAppearance(ap);
			// break;

			// }
			// default:
			// {
			// }
			// }
		}
	}

	public void edgeAppearanceChanged(final JJGraphEvent e) {
		final JJGraphicEdge ge = (JJGraphicEdge) e.getSource();
		edgeAppearanceChanged(ge);
	}

	public void edgeAppearanceChanged(final JJGraphicEdge ge) {
		final TransformGroup tg = edgeTo3D.get(ge);
		final JJGraphWindow graphWindow = ge.getWindow();

		if ((tg == null))// && graphWindow.isDrawn(ge))
		{
			createEdge(ge);
		} else {
			// if(graphWindow.isDrawn(ge))
			// {
			// switch(e.getType()){
			// case JJGraphEvent.EDGE_MOVE_EVENT:
			// {
			Debug.println("Adjusting edge: " + ge.getEdge().getName());

			final JJEdge tmpE = ge.getEdge();

			final JJGraphicNode gs = tmpE.getSource().getGraphicNode(graphWindow);
			final JJGraphicNode gt = tmpE.getTarget().getGraphicNode(graphWindow);

			final double sx = gs.getX() / scale + xOff;
			final double sy = gs.getY() / scale + yOff;
			final double sz = gs.getZ() / scale + zOff;

			final double tx = gt.getX() / scale + xOff;
			final double ty = gt.getY() / scale + yOff;
			final double tz = gt.getZ() / scale + zOff;

			final float dx = (float) (tx - sx);
			final float dy = (float) (ty - sy);
			final float dz = (float) (tz - sz);
			final double length = Math.sqrt(dx * dx + dy * dy + dz * dz);

			final Vector3f tmpV = new Vector3f(dx, dy, dz);

			final double theta = tmpV.angle(new Vector3f(0f, 01f, 0f));

			final Transform3D t3dKnot1 = new Transform3D();
			t3dKnot1.setScale(new Vector3d(1.0, length, 1.0));
			t3dKnot1.setRotation(new AxisAngle4f(-dz, 0f, dx, (float) -theta));

			final Transform3D trans = t3dKnot1;
			trans.setTranslation(new Vector3f((float) sx + dx / 2f, (float) sy + dy / 2f, (float) sz + dz / 2f));

			// Cylinder cyl = (Cylinder)tg.getChild(0);

			tg.setTransform(trans);
			// break;
			// }
			// case JJGraphEvent.EDGE_COLOUR_EVENT:
			// {
			// Debug.println("Changing colour");
			final Primitive node = (Primitive) tg.getChild(0);
			final Appearance ap = node.getAppearance();

			final float r = ge.getColor().getRed() / 255.0f;
			final float g = ge.getColor().getGreen() / 255.0f;
			final float b = ge.getColor().getBlue() / 255.0f;

			final Material material = ap.getMaterial();// new Material();
			material.setDiffuseColor(r, g, b);
			material.setAmbientColor(r / 4.0f, g / 4.0f, b / 4.0f);
			material.setShininess(50.0f);
			ap.setMaterial(material);

			// if(ge.getColor().getAlpha() != 255){
			float alpha = 1.0f - ge.getColor().getAlpha() / 255.0f;
			if (!ge.isVisible())
				alpha = 1;

			final TransparencyAttributes ta = ap.getTransparencyAttributes();// new
																				// TransparencyAttributes(
																				// );
			ta.setTransparency(alpha);
			// ta.setTransparencyMode( TransparencyAttributes.NICEST );
			ap.setTransparencyAttributes(ta);
			// }
			// node.setAppearance(ap);
			// break;
			// }
			// default:
			// {
			// }
			// }

			// }
		}
	}

	@Override
	public void graphAppearanceChanged(final JJGraphEvent theEvent) {
		if (theEvent.getSource() instanceof JJGraphicNode) {
			nodeAppearanceChanged(theEvent);
		} else if (theEvent.getSource() instanceof JJGraphicEdge) {
			edgeAppearanceChanged(theEvent);
		} else if (theEvent.getType() == JJGraphEvent.DISABLE_REDRAW) {
			canvas.stopRenderer();
		} else if (theEvent.getType() == JJGraphEvent.ENABLE_REDRAW) {
			canvas.startRenderer();
		}

		// graphBehavior.graphChanged(theEvent);
	}

	// Well known colors, positions, and directions
	public final static Color3f White = new Color3f(1.0f, 1.0f, 1.0f);
}
