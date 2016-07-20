/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.layout;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJGraphicEdge;
import org.carsten.jjgraph.graph.JJGraphicNode;
import org.carsten.jjgraph.graph.JJNode;
import org.carsten.jjgraph.layopt.JJLayOpt;
import org.carsten.jjgraph.util.JJPoint;

class JJSpringNodes {

	JJNode node;
	int attrib;
	JJPoint coord;
	boolean move;
	LinkedList<Integer> conNodes; // nur Knotennummern
	LinkedList<JJEdge> toSucc; // Kanten zu Vorgaenger und Nachfolger
	LinkedList<JJEdge> fromPred;

	JJSpringNodes() {
		// succ = new HashSet();
		// pred = new HashSet();
		conNodes = new LinkedList<>();
		toSucc = new LinkedList<>();
		fromPred = new LinkedList<>();
		coord = new JJPoint();
	}

}

public class JJSpring implements Runnable, JJLayout {
	boolean suspended = false;

	boolean animation = false;
	boolean useEdgeWeight = true;
	int maxIteration = 1000;
	int animationIntervals = 10;
	int dimension = 2;
	int quality = 2;
	boolean stopBit = false;
	double optDistance = 100;
	JJPoint offset;
	JJSpringNodes[] nodes;
	JJGraphWindow fenster;

	boolean layoutInvisible = false;

	/**
	 * Get the value of layoutInvisible.
	 *
	 * @return Value of layoutInvisible.
	 */
	public boolean getLayoutInvisible() {
		return layoutInvisible;
	}

	/**
	 * Set the value of layoutInvisible.
	 *
	 * @param v
	 *            Value to assign to layoutInvisible.
	 */
	public void setLayoutInvisible(final boolean v) {
		this.layoutInvisible = v;
	}

	public JJSpring(final JJGraphWindow f) {
		offset = new JJPoint(100, 100);
		fenster = f;
		// nodes = new JJSpringNodes[graph.getNumNodes()];
	}

	boolean getAnimation() {
		return (animation);
	}

	@Override
	public int allowsOptimize() {
		return JJLayOpt.TRANSLATE | JJLayOpt.SCALE_PROPORTIONAL | JJLayOpt.FLIP | JJLayOpt.ROTATE;
	}

	void setAnimation(final boolean value) {
		animation = value;
	}

	boolean getUseEdgeWeight() {
		return (useEdgeWeight);
	}

	void setUseEdgeWeight(final boolean value) {
		useEdgeWeight = value;
	}

	int getMaxIteration() {
		return (maxIteration);
	}

	void setMaxIteration(final int value) throws Exception {
		if (value <= 0)
			throw new Exception("Max Iteration must be > 0");
		else
			maxIteration = value;
	}

	int getAnimationIntervals() {
		return (animationIntervals);
	}

	void setAnimationIntervals(final int value) throws Exception {
		if ((value <= 0) || (value > maxIteration))
			throw new Exception("Animation interval must be between 0 and " + maxIteration);
		else
			animationIntervals = value;
	}

	int getQuality() {
		return (quality);
	}

	// void stop() {
	// stopBit = true;
	// }

	void setQuality(final int value) throws Exception {
		if ((value < 0) || (value > 10))
			throw new Exception("Quality must be between 1 and 10");
		else
			quality = value;
	}

	double getOptDistance() {
		return (optDistance);
	}

	void setOptDistance(final double value) throws Exception {
		if (value <= 0)
			throw new Exception("Optimal distance ust be > 0");
		else
			optDistance = value;
	}

	double getxoff() {
		return (offset.x);
	}

	void setxoff(final double value) {
		offset.x = (int) value;
	}

	double getyoff() {
		return (offset.y);
	}

	void setyoff(final double value) {
		offset.y = (int) value;
	}

	int getDimension() {
		return (dimension);
	}

	void setDimension(final int value) throws Exception {
		if (value == 2 || value == 3)
			dimension = value;
		else
			throw new Exception("Dimension must be 2 or 3");
	}

	/////////////////////////////////////////////////////////////////////////////
	// //
	// void layout () //
	// //
	// Funktionalitaet: //
	// - der betreffende Graph wird mittels des springembedder-Algorithmus //
	// layouted //
	// //
	// Bemerkungen: //
	// - Veraendert die x und y Koordinaten der Knoten //
	// - Veraendert value der Knoten und der Kanten //
	// - Stuetzpunkte der Kanten werden entfernt //
	// //
	// Vorbedingungen: //
	// - es muss ein Graph mit setGraph() uebergeben wurden sein //
	// //
	// Nachbedingungen: //
	// - die Koordinaten der Knoten sind auf die entsprechenden Werte gesetzt,
	///////////////////////////////////////////////////////////////////////////// //
	// value von Knoten und Kanten enthaelt andere Werte //
	// //
	/////////////////////////////////////////////////////////////////////////////

	@Override
	public void layout() {
		stopBit = false;
		springembedder();
	}

	@Override
	public void layout(final Collection<JJNode> c) {
		stopBit = false;
		springembedder();
	}

	Iterator<JJNode> nodeIterator() {
		return fenster.getGraph().nodeIterator();
	}

	void initJJSpringNodes() {

		///////////////////////////////////////////////////////////////////
		// //
		// Initialisierung von JJSpringNodes. Zuordnung der Array- //
		// Elemente zu Knoten erfolgt ueber die temporaere Knotennummer //
		// (Wert in "value" des Knotens). //
		// //
		///////////////////////////////////////////////////////////////////
		int value = -1;

		// for(iter1=graph.nodeSet.begin(); iter1.atEnd() == false;
		// iter1.advance()) {
		for (final Iterator<JJNode> iter1 = nodeIterator(); iter1.hasNext();) {
			// JJNode tmpN = (JJNode) iter1.get();
			// JJGraphicNode gn = tmpN.getGraphicNode();
			final JJGraphicNode gn = iter1.next().getGraphicNode(fenster);
			if (gn != null && gn.isVisible()) {
				// Falls Knoten bereiTs Koordinaten (also Graphikrepr.) hat.
				value = gn.getNode().getValue();
				nodes[value].node = gn.getNode();
				nodes[value].attrib = 0;

				// Testen, ob Knoten festgehalten werden soll.
				if (gn.isSelected())
					nodes[value].move = false;
				else
					nodes[value].move = true;

				nodes[value].coord.x = gn.getX();
				nodes[value].coord.y = gn.getY();

				Iterator<JJEdge> edgeIter = null;

				for (edgeIter = gn.getNode().edgeIterator(); edgeIter.hasNext();) {
					final JJEdge tmpE = edgeIter.next();

					if (tmpE.getTarget().getGraphicNode(fenster).isVisible()
							&& tmpE.getSource().getGraphicNode(fenster).isVisible()) {
						nodes[value].conNodes.add(new Integer(tmpE.getTarget().getValue()));
						nodes[value].toSucc.add(tmpE);
					}

				}
			} else {
				// falls keine Grafikrepraesentation des Knotens
				nodes[value].coord.x = 0.0;
				nodes[value].coord.y = 0.0;
			}
		}

		// Ausgangswerte moeglichst nicht auf einem Punkt (einer Linie),
		// sondern weinigstens ein Knoten im Raum verteilt.

		if ((value != -1) && nodes[value].move) {
			nodes[value].coord.x = nodes[value].coord.y + 1;
		}
	}

	void initForces(final JJPoint[] forces, final int anzN) {
		for (int i = 1; i <= anzN; i++)
			forces[i] = new JJPoint(0.0, 0.0);
	}

	void computeForces(final double optimalDistance, final JJPoint[] forces, final int anzN) {

		///////////////////////////////////////////////////////////////////
		// //
		// Berechnet die Kraefte, die an den Knoten des Graphen wirken. //
		// Dabei werden die Koordinaten der Knoten in der Struktur //
		// "nodes" uebergeben. Das Ergebnis wird im Array "forces" //
		// zurueckgegeben. //
		// Die Vorgaenger und Nachfolger werden ueber die entsprechenden //
		// Knotenmengen bestimmt. Kantengewichte finden hierbei keine //
		// Beruecksichtigung. //
		// //
		///////////////////////////////////////////////////////////////////
		int k, nodeIndex1, nodeIndex2;
		JJPoint attractiveForce, repulsiveForce, force = null, delta, point;
		double attractiveFactor, repulsiveFactor;
		final double sqrOptimalDistance = optimalDistance * optimalDistance;

		// Falls zweidimensionales Layout
		point = new JJPoint(1.0, 1.0);

		for (nodeIndex1 = 1; nodeIndex1 < anzN; nodeIndex1++) {
			// Fuer alle Vorgaenger und Nachfolger
			final Iterator<Integer> conIter = nodes[nodeIndex1].conNodes.iterator();

			while (conIter.hasNext()) {
				final int tmpCon = conIter.next().intValue();

				if (nodeIndex1 < tmpCon) {
					nodeIndex2 = tmpCon;
					delta = new JJPoint(nodes[nodeIndex2].coord.x - nodes[nodeIndex1].coord.x,
							nodes[nodeIndex2].coord.y - nodes[nodeIndex1].coord.y);

					if ((delta.x == 0.0) && (delta.y == 0.0))
						delta.setLocation(point);

					attractiveFactor = (Math.sqrt(delta.x * delta.x + delta.y * delta.y)) / optimalDistance;

					attractiveForce = new JJPoint(delta.x * attractiveFactor, delta.y * attractiveFactor);

					repulsiveFactor = sqrOptimalDistance / (delta.x * delta.x + delta.y * delta.y);

					repulsiveForce = new JJPoint(delta.x * repulsiveFactor, delta.y * repulsiveFactor);

					force = new JJPoint(attractiveForce.x - repulsiveForce.x, attractiveForce.y - repulsiveForce.y);

					forces[nodeIndex1].x += force.x;
					forces[nodeIndex1].y += force.y;

					forces[nodeIndex2].x -= force.x;
					forces[nodeIndex2].y -= force.y;
					nodes[nodeIndex2].attrib = nodeIndex1;
				}
			}

			for (k = nodeIndex1 + 1; k <= anzN; k++) {
				if (nodes[k].attrib != nodeIndex1) {
					nodeIndex2 = k;
					delta = new JJPoint(nodes[nodeIndex2].coord.x - nodes[nodeIndex1].coord.x,
							nodes[nodeIndex2].coord.y - nodes[nodeIndex1].coord.y);

					if ((delta.x == 0.0) && (delta.y == 0.0))
						delta.setLocation(point);

					repulsiveFactor = sqrOptimalDistance / (delta.x * delta.x + delta.y * delta.y);

					repulsiveForce = new JJPoint(delta.x * repulsiveFactor, delta.y * repulsiveFactor);

					if (force != null) {
						forces[nodeIndex1].x -= repulsiveForce.x;
						forces[nodeIndex1].y -= repulsiveForce.y;

						forces[nodeIndex2].x += repulsiveForce.x;
						forces[nodeIndex2].y += repulsiveForce.y;
					}
				}
			}
		}
	}

	void computeForcesEdges(final double optimalDistance, final JJPoint[] forces, final int anzN) {

		///////////////////////////////////////////////////////////////////
		// //
		// Berechnet die Kraefte, die an den Knoten des Graphen wirken. //
		// Dabei werden die Koordinaten der Knoten in der Struktur //
		// "nodes" uebergeben. Das Ergebnis wird im Array "forces" //
		// zurueckgegeben. //
		// Die Bestimmung der Vorgaenger- und Nachfolgerknoten erfolgt //
		// ueber die ein- und ausgehenden Kanten. Hier werden Kantenge- //
		// wichte beruecksichtigt. //
		// //
		///////////////////////////////////////////////////////////////////
		JJPoint attractiveForce, repulsiveForce, force = null, delta, point;
		double attractiveFactor, repulsiveFactor, weight;
		int k, nodeIndex1, nodeIndex2;

		// Falls zweidimensionales Layout

		point = new JJPoint(1.0, 1.0);

		for (nodeIndex1 = 1; nodeIndex1 < anzN; nodeIndex1++) {
			// alle ein/aus-gehenden Kanten betrachten

			Iterator<JJEdge> edgeIter;

			for (int inOut = 1; inOut <= 2; inOut++) {
				if (inOut == 1)
					edgeIter = nodes[nodeIndex1].toSucc.iterator();
				else
					edgeIter = nodes[nodeIndex1].fromPred.iterator();

				while (edgeIter.hasNext()) {

					final JJEdge tmpE = edgeIter.next();
					JJNode tmpN = null;

					if (inOut == 1) {
						tmpN = tmpE.getTarget();
					} else {
						tmpN = tmpE.getSource();
					}

					if (!tmpN.getGraphicNode(fenster).isVisible())
						continue;

					nodeIndex2 = tmpN.getValue();

					if ((nodeIndex1 < nodeIndex2) && (tmpE.getWeight() != 0.0)) {
						weight = tmpE.getWeight();
						delta = new JJPoint(nodes[nodeIndex2].coord.x - nodes[nodeIndex1].coord.x,
								nodes[nodeIndex2].coord.y - nodes[nodeIndex1].coord.y);

						if ((delta.x == 0.0) && (delta.y == 0.0))
							delta.setLocation(point);

						attractiveFactor = (Math.sqrt(delta.x * delta.x + delta.y * delta.y)) / optimalDistance;

						attractiveForce = new JJPoint(delta.x * attractiveFactor * weight,
								delta.y * attractiveFactor * weight);

						repulsiveFactor = (optimalDistance * optimalDistance) / (delta.x * delta.x + delta.y * delta.y);

						repulsiveForce = new JJPoint((delta.x * repulsiveFactor) / weight,
								(delta.y * repulsiveFactor) / weight);

						force = new JJPoint(attractiveForce.x - repulsiveForce.x, attractiveForce.y - repulsiveForce.y);

						forces[nodeIndex1].x += force.x;
						forces[nodeIndex1].y += force.y;

						forces[nodeIndex2].x -= force.x;
						forces[nodeIndex2].y -= force.y;
						nodes[nodeIndex2].attrib = nodeIndex1;
					}
				}
			}

			for (k = nodeIndex1 + 1; k <= anzN; k++) {
				if (nodes[k].attrib != nodeIndex1) {
					nodeIndex2 = k;
					delta = new JJPoint(nodes[nodeIndex2].coord.x - nodes[nodeIndex1].coord.x,
							nodes[nodeIndex2].coord.y - nodes[nodeIndex1].coord.y);

					if ((delta.x == 0.0) && (delta.y == 0.0))
						delta.setLocation(point);

					repulsiveFactor = (optimalDistance * optimalDistance) / (delta.x * delta.x + delta.y * delta.y);

					repulsiveForce = new JJPoint(delta.x * repulsiveFactor, delta.y * repulsiveFactor);

					forces[nodeIndex1].x -= repulsiveForce.x;
					forces[nodeIndex1].y -= repulsiveForce.y;

					forces[nodeIndex2].x += repulsiveForce.x;
					forces[nodeIndex2].y += repulsiveForce.y;
				}
			}
		}
	}

	double computeMaxForce(final JJPoint[] forces, final int anzN, final double[] absForces) {

		///////////////////////////////////////////////////////////////////
		// //
		// Bestimmt die groesste Kraft aus dem Array "forces". //
		// //
		///////////////////////////////////////////////////////////////////
		double maxAbsForce = 0.0;

		for (int i = 1; i <= anzN; i++) {
			absForces[i] = forces[i].distance(0, 0);

			if (absForces[i] > maxAbsForce)
				maxAbsForce = absForces[i];
		}
		return (maxAbsForce);
	}

	void computeNodePosition(final JJPoint[] forces, final int anzN, final double temperature,
			final double[] absForces) {

		///////////////////////////////////////////////////////////////////
		// //
		// Berechnet entsprechend der an den Knoten wirkenden Kraefte //
		// ("forces") und der maximal zulaessigen Verschiebung der //
		// Knoten ("temperature") die neuen Knotenpositionen. //
		// //
		///////////////////////////////////////////////////////////////////

		final JJPoint shift = new JJPoint();
		int i;

		for (i = 1; i <= anzN; i++) {
			// Falls Knoten nicht festgehalten werden soll
			if (nodes[i].move) {
				if (absForces[i] < temperature) {
					shift.x = forces[i].x;
					shift.y = forces[i].y;
				} else {
					shift.x = forces[i].x / absForces[i] * temperature;
					shift.y = forces[i].y / absForces[i] * temperature;
				}

				nodes[i].coord.x += shift.x;
				nodes[i].coord.y += shift.y;
			}
		}
	}

	double cool(final double maxForce, final double vibration, double coolMaxForce1, double coolMaxForce2,
			double coolMaxForce3, double coolMaxForce4, int coolPhase) {

		///////////////////////////////////////////////////////////////////
		// //
		// Berechnet abhaengig von "maxForce" die fuer diesen Schleichen-//
		// durchlauf maximal zulaessige Verschiebung ("temperature") der //
		// Knoten. Nachdem der Graph zu schwingen angefangen hat, wird //
		// die maximale zulaessige Verschiebung erheblich niedriger ge- //
		// setzt, so dass eine Schwingung nicht mehr auftritt (else- //
		// Zweig). //
		// //
		///////////////////////////////////////////////////////////////////
		double temperature;

		coolMaxForce4 = coolMaxForce3;
		coolMaxForce3 = coolMaxForce2;
		coolMaxForce2 = coolMaxForce1;
		coolMaxForce1 = maxForce;

		// Test, ob Graph schwingt
		if ((coolMaxForce4 != 0.0) && (Math.abs((coolMaxForce1 - coolMaxForce3) / coolMaxForce1) < vibration)
				&& (Math.abs((coolMaxForce2 - coolMaxForce4) / coolMaxForce2) < vibration))
			coolPhase = 2;

		if (coolPhase == 1)
			temperature = Math.sqrt(maxForce);
		else
			temperature = Math.sqrt(maxForce) / 15;

		return (temperature);
	}

	void writeNodePosition(final int anzN) {

		///////////////////////////////////////////////////////////////////
		// //
		// Uebertraegt die neuen Koordinaten der Knoten in den //
		// urspruenglich uebergebenen Graphen. //
		// //
		///////////////////////////////////////////////////////////////////
		JJGraphicNode gn;
		double xOff = 0, yOff = 0;

		for (int i = 1; i <= anzN; i++) {
			gn = nodes[i].node.getGraphicNode(fenster);
			if (gn != null) {
				nodes[i].coord.x = nodes[i].coord.x / 2;
				nodes[i].coord.y = nodes[i].coord.y / 2;

				if (nodes[i].coord.x < xOff)
					xOff = nodes[i].coord.x;

				if (nodes[i].coord.y < yOff)
					yOff = nodes[i].coord.y;

				// gn.moveTo (nodes[i].coord);
			}

		}

		for (int i = 1; i <= anzN; i++) {
			gn = nodes[i].node.getGraphicNode(fenster);
			if (gn != null) {
				// Debug.println("Moving to " + nodes[i].coord);
				nodes[i].coord.x += (Math.abs(xOff) + 50);
				nodes[i].coord.y += (Math.abs(yOff) + 50);
				fenster.moveNodeTo(gn, nodes[i].coord);
			}

		}

	}

	void changeOptDist(double optimalDistance) {

		///////////////////////////////////////////////////////////////////
		// //
		// Passt "optimalDistance" an den Wert der Kantengewichte an. //
		// Da sich "optimalDistance" auf ein Kantengewicht von 1.0 //
		// bezieht, wuerden hohe Kantengewichte den Graphen sehr eng um //
		// einen Punkt ziehen. Dies wird durch eine Anpassung von //
		// "optimalDistance" verhindert. //
		// //
		///////////////////////////////////////////////////////////////////

		double maxWeight = 0;
		// HashSet allEdges = graph.edgeSet;
		// Collection allEdges = fenster.getVisibleEdges();

		for (final Iterator<JJEdge> edgeIter = fenster.getGraph().edgeIterator(); edgeIter.hasNext();) {
			final JJGraphicEdge tmpE = edgeIter.next().getGraphicEdge(fenster);
			if ((tmpE.isVisible()) && (tmpE.getEdge().getWeight() > maxWeight))
				maxWeight = tmpE.getEdge().getWeight();
		}

		if (maxWeight == 0.0)
			maxWeight = 1.0;
		if ((optimalDistance / maxWeight) < 5.0)
			optimalDistance = (Math.sqrt(maxWeight * maxWeight)) * 5.0;
	}

	void springembedderLayout() {
		///////////////////////////////////////////////////////////////////
		// //
		// Hauptprozedur des Algorithmus //
		// //
		///////////////////////////////////////////////////////////////////

		JJPoint[] forces;
		double[] absForces;

		double actMaxForce, temperature, maxForce;
		final double optimalDistance = optDistance, coolMaxForce1 = 0.0, coolMaxForce2 = 0.0, coolMaxForce3 = 0.0,
				coolMaxForce4 = 0.0;

		int anzN, maxIter, iterationCount = 0, iteration = 0;
		final int coolPhase = 1;

		// anzN = graph.getNumNodes();
		fenster.removeBends();

		anzN = fenster.getGraph().getNumNodes();

		forces = new JJPoint[anzN + 1];
		for (int i = 0; i < anzN + 1; i++)
			forces[i] = new JJPoint(0, 0);

		absForces = new double[anzN + 1];
		nodes = new JJSpringNodes[anzN + 1];
		for (int i = 0; i < anzN + 1; i++)
			nodes[i] = new JJSpringNodes();

		// Bestimme entsprechend Qualitaet und Graphgroesse "maxForce"
		// und "maxIter"
		if (quality > 0) {
			maxForce = 50.5 - (5.0 * quality);
			maxIter = (5000 * quality) / anzN;
			if (maxIter < 30)
				maxIter = 30;
		} else if (quality == 0) {
			maxForce = 0.5;
			maxIter = 0;
			// cerr << "Doing infinite ...." << endl;
		} else {
			maxForce = 3.0;
			maxIter = maxIteration;
		}

		// Initialisierungen
		// stopBit = false;
		changeOptDist(optimalDistance);
		initForces(forces, anzN);
		initJJSpringNodes();

		// Berechnung ...
		if (useEdgeWeight)
			computeForcesEdges(optimalDistance, forces, anzN);
		else
			computeForces(optimalDistance, forces, anzN);

		actMaxForce = computeMaxForce(forces, anzN, absForces);

		// ... solange wiederholen, bis gute Verteilung oder Abbruchkriterium
		// erreicht
		while ((((actMaxForce > maxForce) && (iterationCount < maxIter)) || (maxIter == 0)) && (stopBit == false)) {
			initForces(forces, anzN);
			if (useEdgeWeight)
				computeForcesEdges(optimalDistance, forces, anzN);
			else
				computeForces(optimalDistance, forces, anzN);

			actMaxForce = computeMaxForce(forces, anzN, absForces);
			temperature = cool(actMaxForce, 0.3, coolMaxForce1, coolMaxForce2, coolMaxForce3, coolMaxForce4, coolPhase);

			computeNodePosition(forces, anzN, temperature, absForces);
			iterationCount++;
			iteration++;

			// Falls Animation waehrend der Berechnung,
			// nach bestimmten Berechnungsdurchlaeufen
			// die Koordinaten der Knoten im Graphikfenster aktualisieren.
			if ((animation && (iteration == animationIntervals)) || ((maxIter == 0) && (iteration == 100))) {
				writeNodePosition(anzN);
				iteration = 0;
			}
		}

		// Ergebnis als Koordinaten fuer Knoten im Graphen uebertragen.
		writeNodePosition(anzN);

		nodes = null;
	}

	void numberNodes(final int i) {
		int val = i;

		for (final Iterator<JJNode> iter = nodeIterator(); iter.hasNext();) {
			final JJGraphicNode tmpN = iter.next().getGraphicNode(fenster);
			if (tmpN.isVisible())
				tmpN.getNode().setValue(++val);
			else
				tmpN.getNode().setValue(-1);
		}
	}

	void springembedder() {

		///////////////////////////////////////////////////////////////////
		// //
		// Der Graph wird fuer den Algorithmus vorbereitet (Entfernen //
		// reflexiver und paralleler Kanten). Das eigentliche Layout //
		// des Graphen (der Teilgraphen) erfolgt in der Prozedur //
		// "springembedder_layout". //
		// //
		///////////////////////////////////////////////////////////////////

		numberNodes(0);
		springembedderLayout();
	}

	@Override
	public void run() {

		// while (!suspended) {
		layout();

		// double newStressSum = relax();
		if (fenster != null) {
			// fenster.repaint();
			fenster.setBusy(false);
		}

		// try {
		// Thread.sleep(100);
		// } catch (InterruptedException e) {
		// break;
		// }
		// }
	}

	public synchronized void start() {
		if (fenster != null) {
			fenster.setBusy(true);
		}

		final Thread relaxer = new Thread(this);
		relaxer.setPriority(Thread.MIN_PRIORITY);
		relaxer.setName("Springembedder layout");
		relaxer.start();
	}

	public synchronized void stop() {
		suspended = true;
	}

	@Override
	public String getName() {
		return "Spring embedder";
	}

	@Override
	public void apply(final String s) {
		layout();
	}

	@Override
	public boolean canDo(final String s) {
		return s.equals(getName());
	}
}
