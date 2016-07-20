/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.layout;

import java.util.HashSet;
import java.util.Iterator;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJGraphicNode;
import org.carsten.jjgraph.graph.JJNode;
import org.carsten.jjgraph.util.JJPoint;

public class JJSpring3D extends JJSpring implements Runnable {

	public JJSpring3D(final JJGraphWindow f) {
		super(f);
		dimension = 3;
	}

	void initJJSpringNodes(final HashSet<JJNode> locNodes) {

		///////////////////////////////////////////////////////////////////
		// //
		// Initialisierung von JJSpringNodes. Zuordnung der Array- //
		// Elemente zu Knoten erfolgt ueber die temporaere Knotennummer //
		// (Wert in "value" des Knotens). //
		// //
		///////////////////////////////////////////////////////////////////
		int value = -1;

		for (final Iterator<JJNode> iter1 = nodeIterator(); iter1.hasNext();) {
			final JJGraphicNode gn = iter1.next().getGraphicNode(fenster);
			if (gn != null && gn.isVisible()) {
				// Falls Knoten bereiTs Koordinaten (also Graphikrepr.) hat.
				value = gn.getNode().getValue();
				nodes[value].node = gn.getNode();
				nodes[value].attrib = 0;

				// Testen, ob Knoten festgehalten werden soll.
				// if (locNodes.contains(gn.getNode()))
				if (gn.isSelected())
					nodes[value].move = false;
				else
					nodes[value].move = true;

				nodes[value].coord.x = gn.getX();
				nodes[value].coord.y = gn.getY();
				nodes[value].coord.z = gn.getZ();

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
				nodes[value].coord.z = 0.0;
			}
		}

		if ((value != -1) && nodes[value].move) {
			nodes[value].coord.y = nodes[value].coord.y + 1;
			nodes[value].coord.z = nodes[value].coord.z + 1;
		}
	}

	@Override
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

		point = new JJPoint(1.0, 1.0, 1.0);

		for (nodeIndex1 = 1; nodeIndex1 < anzN; nodeIndex1++) {
			// Fuer alle Vorgaenger- und Nachfolgerknoten

			final Iterator<Integer> conIter = nodes[nodeIndex1].conNodes.iterator();

			while (conIter.hasNext()) {
				final int tmpCon = conIter.next().intValue();

				if (nodeIndex1 < tmpCon) {
					nodeIndex2 = tmpCon;

					delta = JJPoint.minus(nodes[nodeIndex2].coord, nodes[nodeIndex1].coord);

					if ((delta.x == 0.0) && (delta.y == 0.0) && (delta.z == 0.0))
						delta.setLocation(point);

					attractiveFactor = delta.abs() / optimalDistance;
					attractiveForce = JJPoint.mult(delta, attractiveFactor);

					repulsiveFactor = sqrOptimalDistance / (delta.x * delta.x + delta.y * delta.y + delta.z * delta.z);
					repulsiveForce = JJPoint.mult(delta, repulsiveFactor);

					force = JJPoint.minus(attractiveForce, repulsiveForce);
					forces[nodeIndex1].plusA(force);
					forces[nodeIndex2].minusA(force);
					nodes[nodeIndex2].attrib = nodeIndex1;
				}
			}

			for (k = nodeIndex1 + 1; k <= anzN; k++) {
				if (nodes[k].attrib != nodeIndex1) {
					nodeIndex2 = k;
					delta = JJPoint.minus(nodes[nodeIndex2].coord, nodes[nodeIndex1].coord);

					if ((delta.x == 0.0) && (delta.y == 0.0) && (delta.z == 0.0))
						delta.setLocation(point);

					repulsiveFactor = sqrOptimalDistance / (delta.x * delta.x + delta.y * delta.y + delta.z * delta.z);
					repulsiveForce = JJPoint.mult(delta, repulsiveFactor);
					forces[nodeIndex1].minusA(repulsiveForce);
					forces[nodeIndex2].plusA(repulsiveForce);
				}
			}
		}
	}

	@Override
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

		point = new JJPoint(1.0, 1.0, 1.0);
		Iterator<JJEdge> edgeIter;

		for (nodeIndex1 = 1; nodeIndex1 < anzN; nodeIndex1++) {
			// alle ausgehenden Kanten betrachten
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
						delta = JJPoint.minus(nodes[nodeIndex2].coord, nodes[nodeIndex1].coord);

						if ((delta.x == 0.0) && (delta.y == 0.0) && (delta.z == 0.0))
							delta.setLocation(point);

						attractiveFactor = delta.abs() / (optimalDistance / weight);
						attractiveForce = JJPoint.mult(delta, attractiveFactor);

						repulsiveFactor = ((optimalDistance / weight) * (optimalDistance / weight))
								/ (delta.x * delta.x + delta.y * delta.y + delta.z * delta.z);

						repulsiveForce = JJPoint.mult(delta, repulsiveFactor);
						force = JJPoint.mult(JJPoint.minus(attractiveForce, repulsiveForce), weight);
						forces[nodeIndex1].plusA(force);
						forces[nodeIndex2].minusA(force);
						nodes[nodeIndex2].attrib = nodeIndex1;
					}
				}
			}

			for (k = nodeIndex1 + 1; k <= anzN; k++) {
				if (nodes[k].attrib != nodeIndex1) {
					nodeIndex2 = k;
					delta = JJPoint.minus(nodes[nodeIndex2].coord, nodes[nodeIndex1].coord);

					if ((delta.x == 0.0) && (delta.y == 0.0) && (delta.z == 0.0))
						delta.setLocation(point);

					repulsiveFactor = (optimalDistance * optimalDistance)
							/ (delta.x * delta.x + delta.y * delta.y + delta.z * delta.z);
					repulsiveForce = JJPoint.mult(delta, repulsiveFactor);
					forces[nodeIndex1].minusA(repulsiveForce);
					forces[nodeIndex2].plusA(repulsiveForce);
				}
			}
		}
	}

	@Override
	double computeMaxForce(final JJPoint[] forces, final int anzN, final double[] absForces) {

		///////////////////////////////////////////////////////////////////
		// //
		// Bestimmt die groesste Kraft aus dem Array "forces". //
		// //
		///////////////////////////////////////////////////////////////////
		double maxAbsForce = 0.0;

		for (int i = 1; i <= anzN; i++) {
			absForces[i] = forces[i].abs();

			if (absForces[i] > maxAbsForce)
				maxAbsForce = absForces[i];
		}
		return (maxAbsForce);
	}

	@Override
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
					shift.z = forces[i].z;
				} else {
					shift.x = forces[i].x / absForces[i] * temperature;
					shift.y = forces[i].y / absForces[i] * temperature;
					shift.z = forces[i].z / absForces[i] * temperature;
				}

				nodes[i].coord.x += shift.x;
				nodes[i].coord.y += shift.y;
				nodes[i].coord.z += shift.z;

			}
		}
	}

	@Override
	void writeNodePosition(final int anzN) {

		///////////////////////////////////////////////////////////////////
		// //
		// Uebertraegt die neuen Koordinaten der Knoten in den //
		// urspruenglich uebergebenen Graphen. //
		// //
		///////////////////////////////////////////////////////////////////
		JJGraphicNode gn;
		double xOff = 0, yOff = 0, zOff = 0;

		for (int i = 1; i <= anzN; i++) {
			gn = nodes[i].node.getGraphicNode(fenster);
			if (gn != null) {
				nodes[i].coord.x = nodes[i].coord.x / 2;
				nodes[i].coord.y = nodes[i].coord.y / 2;
				nodes[i].coord.z = nodes[i].coord.z / 2;

				if (nodes[i].coord.x < xOff)
					xOff = nodes[i].coord.x;

				if (nodes[i].coord.y < yOff)
					yOff = nodes[i].coord.y;

				if (nodes[i].coord.y < yOff)
					zOff = nodes[i].coord.z;
			}
		}

		for (int i = 1; i <= anzN; i++) {
			gn = nodes[i].node.getGraphicNode(fenster);
			if (gn != null) {
				// Debug.println("Moving to " + nodes[i].coord);
				nodes[i].coord.x += (Math.abs(xOff) + 50);
				nodes[i].coord.y += (Math.abs(yOff) + 50);
				nodes[i].coord.z += (Math.abs(zOff) + 50);
				// Debug.println("Moving node " + i + " to (" + nodes[i].coord.x
				// +
				// "," + nodes[i].coord.y +
				// "," + nodes[i].coord.z + ")");

				fenster.moveNodeTo(gn, nodes[i].coord);
			}
		}
	}

	@Override
	public String getName() {
		return "Springembedder 3D";
	}

}
