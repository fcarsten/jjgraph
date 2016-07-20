/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.layout;

/**
 * JJRandomLayout.java
 *
 *
 * Created: Wed Dec  8 11:15:52 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJGraphicEdge;
import org.carsten.jjgraph.graph.JJGraphicNode;
import org.carsten.jjgraph.graph.JJNode;
import org.carsten.jjgraph.layopt.JJLayOpt;
import org.carsten.jjgraph.util.Debug;
import org.carsten.jjgraph.util.JJPoint;

class OrthoComp implements Comparator<JJNode> {
	@Override
	public int compare(final JJNode n1, final JJNode n2) {
		return ((n1.getCoord() >= n2.getCoord()) ? ((n1.getCoord() == n2.getCoord()) ? 0 : -1) : 1);
	}

}

class InOutStruct {
	boolean o = false, u = false, l = false, r = false;
}

public class JJOrtho implements JJLayout {
	final static int RAND = 5;
	final static int ORTHO_X_OFFSET = 100;
	final static int ORTHO_Y_OFFSET = 100;

	// Groesse der Nachbarschaft in der ich nach potentiellen Tausch-
	// kandidaten suche
	final static int WSIZE = 9;

	// Schwellwert ab dem ich eine Position zur neuen Wunschposition mache
	final static int RES_VAL = 500;

	private double aktuX;
	private double aktuY;
	private double xOffSet;
	private double yOffSet;

	private boolean threeD = false;
	private boolean anim = true;
	private boolean doSwapping = false;
	private boolean doSwapBadies = false;
	private boolean doEdgeAdjustment = true;
	private String initPlacement = "adjust"; // "random"

	private int tiIndex;

	private final JJGraph graph;
	private final JJGraphWindow fenster;

	// Fuer Swap nodes
	private int count = 0; // Anzahl der Knoten mit graphischer Rep.
	private JJNode nodes[]; // Array der Knoten mit graphischer Rep.
	private int anzX = 0; // Anzahl der Knoten pro Reihe in nodes
	private int anzY = 0; // Anzahl der Knoten pro Spalte in nodes

	private final double distArray[] = new double[WSIZE];
	private final int wunschPos[] = new int[WSIZE];
	private final JJPoint wunschP[] = new JJPoint[WSIZE];

	@Override
	public String getName() {
		return "Orthogonal";
	}

	public JJOrtho(final JJGraphWindow f) {
		xOffSet = ORTHO_X_OFFSET;
		yOffSet = ORTHO_Y_OFFSET;

		fenster = f;
		graph = f.getGraph();
	}

	// void init(JJGraph g)
	// {
	// // inspector= new JJOrthoInsp(this);
	// }

	@Override
	public int allowsOptimize() {
		return JJLayOpt.TRANSLATE | JJLayOpt.SCALE_PROPORTIONAL | JJLayOpt.FLIP | JJLayOpt.ROTATE_ORTHOGONAL;
	}

	double distSum(final JJNode node, final JJPoint pos) {
		return distSum(node, pos, null, new JJPoint(0, 0));
	}

	double distSum(final JJNode node, final JJPoint pos, final JJNode partner, final JJPoint partnerPos) {
		final JJGraphicNode gn = node.getGraphicNode(fenster);

		if (gn == null)
			return Double.MAX_VALUE;

		double dist = 0.0;

		// FFSet<FFEdge>& edgeTo = node.transToSucc();
		// FFSet<FFEdge>& edgeFrom = node.transFromPred();

		for (final Iterator<JJEdge> i = node.outIterator(); i.hasNext();) {
			final JJNode tmpN = i.next().getTarget();

			if (tmpN != node) // Reflexive Kanten nicht mitzaehlen !
			{
				if (tmpN != partner) {
					final JJGraphicNode tmpGN = tmpN.getGraphicNode(fenster);
					dist += pos.distance(tmpGN.getCoords());
					// dist += pos.brieftraeger(tmpGN.getCoords());
				} else {
					dist += pos.distance(partnerPos);
					// dist += pos.brieftraeger(partnerPos);
				}
			}
		}

		for (final Iterator<JJEdge> i = node.inIterator(); i.hasNext();) {
			final JJNode tmpN = i.next().getSource();
			if (tmpN != node) // Reflexive Kanten nicht mitzaehlen !
			{
				if (tmpN != partner) {
					final JJGraphicNode tmpGN = tmpN.getGraphicNode(fenster);
					dist += pos.distance(tmpGN.getCoords());
					// dist += pos.brieftraeger(tmpGN.getCoords());
				} else {
					dist += pos.distance(partnerPos);
					// dist += pos.brieftraeger(partnerPos);
				}
			}
		}

		if (dist == 0.0)
			return Double.MAX_VALUE;

		// Tie Breaker
		//
		// Ein freies Feld nach rechts oben wird immer genommen
		//

		if (partner == null)
			if (pos.getX() > gn.getCoords().getX())
				dist--;
			else if (pos.getY() > gn.getCoords().getY())
				dist--;

		return dist;
	}

	double distSum(final JJNode node) {
		final JJGraphicNode gn = node.getGraphicNode(fenster);

		if (gn != null)
			return (distSum(node, gn.getCoords()));

		return Double.MAX_VALUE;
	}

	int ffMinArray(final double tmpA[]) {
		int result = 0;
		for (int k = 1; k < tmpA.length; k++) {
			if (tmpA[k] < tmpA[result])
				result = k;
		}

		return result;
	}

	int testWunschPos(final JJNode knoten, final double schwellWert) {
		// Rueckgabewert: Eine prinzipiell gute Position, deren Swappartner
		// aber zu teuer ist. Ziel . neue Suche in dessen
		// Umgebung.

		return testWunschPos(knoten, schwellWert, -1);
	}

	int testWunschPos(final JJNode knoten, final double schwellWert, final int bad) {
		final int myPos = knoten.getValue();
		final JJPoint istP = knoten.getGraphicNode(fenster).getCoords();
		final double oldValue1 = knoten.getCoord();

		int result = -1;

		if (WSIZE >= 5) {
			wunschPos[1] = wunschPos[0] - anzX; // �ber Wunschposition
			wunschPos[2] = wunschPos[0] - 1; // links von Wunschposition
			wunschPos[3] = wunschPos[0] + 1; // rechts von Wunschposition
			wunschPos[4] = wunschPos[0] + anzX; // unter Wunschposition
		}

		if (WSIZE >= 9) {

			wunschPos[5] = wunschPos[1] - 1; // Jetzt mit der 3x3 Matrix um
			wunschPos[6] = wunschPos[1] + 1; // den Punkt
			wunschPos[7] = wunschPos[4] - 1;
			wunschPos[8] = wunschPos[4] + 1;
		}

		for (int k = 0; k < WSIZE; k++) {

			if ((wunschPos[k] <= count) && (wunschPos[k] >= 0) && (wunschPos[k] != myPos) && (wunschPos[k] != bad)) {
				if (nodes[wunschPos[k]] != null) {
					wunschP[k] = nodes[wunschPos[k]].getGraphicNode(fenster).getCoords();

					final double newValue1 = distSum(knoten, wunschP[k], nodes[wunschPos[k]], istP);

					final double oldValue2 = nodes[wunschPos[k]].getCoord();

					final double newValue2 = distSum(nodes[wunschPos[k]], istP, knoten, wunschP[k]);
					distArray[k] = (newValue1 + newValue2) - (oldValue1 + oldValue2);

					if ((distArray[k] >= 0) && (k > 0)
							&& (((distSum(knoten, wunschP[k]) + xOffSet) - oldValue1) < -schwellWert)) {
						// cerr << "Fand einen potentielle neue WunschPosition:
						// " << k
						// << " mit alt: " << oldValue1 << ", neu: "
						// << distSum(knoten, wunschP[k])+xOffSet << endl;
						result = k;
					}
				} else {
					wunschP[k] = new JJPoint((wunschPos[k] % anzX) * xOffSet, (wunschPos[k] / anzX) * yOffSet);
					final double newValue1 = distSum(knoten, wunschP[k]);
					distArray[k] = newValue1 - oldValue1;
				}
			} else
				distArray[k] = Double.MAX_VALUE;
		}
		return result;
	}

	void updateDists(final JJNode node) {
		node.setCoord(distSum(node));

		// JJSet<JJEdge>& edgeS1 = node.transToSucc();

		for (final Iterator<JJEdge> j = node.outIterator(); j.hasNext();) {
			final JJNode tmpN = j.next().getTarget();
			tmpN.setCoord(distSum(tmpN));
		}

		// JJSet<JJEdge>& edgeS2 = node.transFromPred();
		for (final Iterator<JJEdge> j = node.inIterator(); j.hasNext();) {
			final JJNode tmpN = j.next().getSource();
			tmpN.setCoord(distSum(tmpN));
		}
	}

	//
	// eins: Erster Knoten
	// zwei: Zweiter Knoten
	// dest1: Ziel fuer Knoten 1, falls am Platz von Knoten 2 kein Knoten
	// existiert
	//
	// Rueckgabe: true wenn wirklich eine Verbesserung eingtreten ist. Da beim
	// abstand links - oben bevorzugt wird terminiert der Algorithmus
	// sonst nicht.
	//

	boolean doSwap(final int eins, final int zwei, final JJPoint dest1) {
		final JJNode node1 = nodes[eins];
		final JJNode node2 = nodes[zwei];

		final JJGraphicNode tmpGN1 = node1.getGraphicNode(fenster);
		final JJPoint p1 = tmpGN1.getCoords();

		if (node2 != null) {
			final JJGraphicNode tmpGN2 = node2.getGraphicNode(fenster);
			fenster.moveNodeTo(tmpGN2, p1);

			nodes[eins] = node2;
			node2.setValue(eins);

			// cerr << "Moving " << zwei << " to " << p1() << endl;
		} else {
			// cerr << "Breche aus Gitter aus !!!" << endl;
			nodes[eins] = null;
		}

		fenster.moveNodeTo(tmpGN1, dest1);
		nodes[zwei] = node1;
		nodes[zwei].setValue(zwei);

		// cerr << "Moving " << eins << " to " << dest1() << endl;

		// Neue Distanzen anpassen
		final double oldDist = node1.getCoord();

		updateDists(node1);

		if (node2 != null) {
			updateDists(node2);
			return true;
		}

		if (oldDist > node1.getCoord())
			return true;

		return false;
	}

	boolean swapBadies() {
		boolean didSwap = false;
		final int anzNodes = graph.getNumNodes();
		JJNode knoten[] = new JJNode[anzNodes];

		// JJSet<JJNode> knotenS = graph.allNodes();
		int k = 0;

		for (final Iterator<JJNode> i = graph.nodeIterator(); i.hasNext();) {
			knoten[k++] = i.next();
		}

		// qsort(knoten, anzNodes, sizeof(JJNodeClass *),
		// (int (*)( void *, void *)) ffOrthoCmp);

		Arrays.sort(knoten, new OrthoComp());

		// int minSwap= min(100, anzNodes);
		// int topTen= max(anzNodes/2, minSwap);
		final int topTen = anzNodes;

		for (k = 0; k < topTen; k++) {
			double oldValue1 = knoten[k].getCoord();
			JJGraphicNode gn1 = knoten[k].getGraphicNode(fenster);

			if (gn1 != null)
				for (int j = k + 1; j < topTen; j++) {
					final JJGraphicNode gn2 = knoten[j].getGraphicNode(fenster);
					if (gn2 != null) {
						final double oldValue2 = knoten[j].getCoord();

						final double newValue1 = distSum(knoten[k], gn2.getCoords(), knoten[j], gn1.getCoords());

						final double newValue2 = distSum(knoten[j], gn1.getCoords(), knoten[k], gn2.getCoords());
						if (((newValue1 + newValue2) - (oldValue1 + oldValue2)) < 0) {
							// cerr << "Knoten " << knoten[k].getName() << " von
							// "
							// << knoten[k].getValue() << " nach " <<
							// knoten[j].getValue()
							// << endl;

							didSwap |= doSwap(knoten[k].getValue(), knoten[j].getValue(), gn2.getCoords());
							oldValue1 = knoten[k].getCoord();
							gn1 = knoten[k].getGraphicNode(fenster);
						}
					}
				}
		}

		// delete [] knoten;
		knoten = null;

		return didSwap;
	}

	boolean swapNodes() {
		boolean didSwap = false;

		// JJSet<JJNode> nodeS = graph.allNodes();
		for (final Iterator<JJNode> i = graph.nodeIterator(); i.hasNext();) {
			final JJPoint med = new JJPoint(0, 0);
			final JJNode tmpN = i.next();
			final JJGraphicNode tmpGN = tmpN.getGraphicNode(fenster);

			if (tmpGN != null && (tmpN.deg() != 0)) {
				// JJSet<JJEdge>& edgeS1 = tmpN.transToSucc();

				for (final Iterator<JJEdge> j = tmpN.outIterator(); j.hasNext();)
					med.plusA(j.next().getTarget().getGraphicNode(fenster).getCoords());

				// JJSet<JJEdge>& edgeS2 = tmpN.transFromPred();
				for (final Iterator<JJEdge> j = tmpN.inIterator(); j.hasNext();)
					med.plusA(j.next().getSource().getGraphicNode(fenster).getCoords());

				med.divA(tmpN.deg()); // = med / (tmpN.indeg() + tmpN.outdeg());

				final int myPos = tmpN.getValue();

				final JJPoint istP = tmpGN.getCoords();

				final int xFak = (int) ((med.getX() + (xOffSet / 2)) / xOffSet);
				final int yFak = (int) ((med.getY() + (yOffSet / 2)) / yOffSet);

				wunschPos[0] = yFak * anzX + xFak; // an Wunschpoisition

				double schwellWert = RES_VAL;
				int rerun = testWunschPos(tmpN, schwellWert);
				int mini = ffMinArray(distArray);

				while ((distArray[mini] > 0.0) && (rerun > 0)) {
					// cerr << "Ich starte einen neuen Versuch" << endl;
					final int ignoreValue = wunschPos[0];
					wunschPos[0] = wunschPos[rerun];
					rerun = testWunschPos(tmpN, schwellWert, ignoreValue);
					mini = ffMinArray(distArray);
					schwellWert += 100.0;
				}
				// cerr << endl;

				if (distArray[mini] < 0.0) {
					// cerr << "Knoten " << tmpN.getName() << " von " << myPos
					// << " nach "
					// << wunschPos[mini] << endl;
					didSwap |= doSwap(myPos, wunschPos[mini], wunschP[mini]);
					// WIN_UPD();
				} else {
				}
			}
		}
		return didSwap;
	}

	// void printEdgeSum(ostream &strom)
	// {
	// double edgeSum = 0.0;
	// JJSet<JJNode>& tmpNS=graph.allNodes();

	// for(Iterator i= tmpNS.first(); i != null; tmpNS.next(i))
	// edgeSum += tmpNS(i).getCoord();
	// strom << edgeSum/2;
	// }

	double getEdgeSum() {
		double edgeSum = 0.0;
		// JJSet<JJNode>& tmpNS=graph.allNodes();

		for (final Iterator<JJNode> i = graph.nodeIterator(); i.hasNext();)
			edgeSum += i.next().getCoord();

		return edgeSum / 2;
	}

	// #define PES(a); cerr << a;\
	// printEdgeSum(cerr);\
	// cerr << endl;

	boolean adjustPlacement() {
		final int randOff = RAND;
		final int numNodes = graph.getNumNodes();
		// JJSet<JJNode>& tmpNS = graph.allNodes();
		boolean returnVal = true;

		// Knoten normieren d.h. an minimale Koordinaten verschieben
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;

		double maxX = -Double.MAX_VALUE;
		double maxY = -Double.MAX_VALUE;

		for (final Iterator<JJNode> i = graph.nodeIterator(); i.hasNext();) {
			final JJNode tmpN = i.next();

			final JJGraphicNode tmpGN = tmpN.getGraphicNode(fenster);
			if (tmpGN != null) {
				final JJPoint tmpP = tmpGN.getCoords();
				minX = Math.min(minX, tmpP.getX());
				minY = Math.min(minY, tmpP.getY());
				maxX = Math.max(maxX, tmpP.getX());
				maxY = Math.max(maxY, tmpP.getY());
			}
		}

		if ((maxX < minX) || (maxY < minY)) {
			Debug.println("There is something wrong");
			return false;
		}

		final double xShift = xOffSet * randOff - minX;
		final double yShift = yOffSet * randOff - minY;

		maxX += xShift;
		maxY += yShift;

		anzX = (int) ((maxX + xOffSet / 2) / xOffSet) + randOff + 1;
		anzY = (int) ((maxY + yOffSet / 2) / yOffSet) + randOff + 1;
		count = anzX * anzY;

		// cerr << "Breite: " << anzX << " L�nge: " << count/anzX << endl;

		nodes = new JJNode[count];
		for (int k = 0; k < count; k++)
			nodes[k] = null;

		for (final Iterator<JJNode> i = graph.nodeIterator(); i.hasNext();) {
			final JJNode tmpN = i.next();
			final JJGraphicNode tmpGN = tmpN.getGraphicNode(fenster);
			if (tmpGN != null) {
				JJPoint tmpP = tmpGN.getCoords();
				tmpP.plusA(xShift, yShift);
				final int tmpX = (int) ((tmpP.getX() + xOffSet / 2) / xOffSet);
				final int tmpY = (int) ((tmpP.getY() + yOffSet / 2) / yOffSet);

				wunschPos[0] = tmpY * anzX + tmpX;

				if (WSIZE >= 5) {
					wunschPos[1] = wunschPos[0] - anzX; // �ber Wunschposition
					wunschPos[2] = wunschPos[0] - 1; // links von Wunschposition
					wunschPos[3] = wunschPos[0] + 1; // rechts von
														// Wunschposition
					wunschPos[4] = wunschPos[0] + anzX; // unter Wunschposition
				}

				if (WSIZE >= 9) {
					wunschPos[5] = wunschPos[1] - 1; // Jetzt mit der 3x3 Matrix
														// um
					wunschPos[6] = wunschPos[1] + 1; // den Punkt
					wunschPos[7] = wunschPos[4] - 1;
					wunschPos[8] = wunschPos[4] + 1;
				}

				int j = 0;

				for (; j < WSIZE; j++) {
					if ((wunschPos[j] <= count) && (wunschPos[j] >= 0) && (nodes[wunschPos[j]] == null)) {
						nodes[wunschPos[j]] = tmpN;
						tmpN.setValue(wunschPos[j]);

						tmpP = new JJPoint((wunschPos[j] % anzX) * xOffSet, (wunschPos[j] / anzX) * yOffSet);
						break;
					}
				}

				if (j == WSIZE) {
					returnVal = false;
				}

				fenster.moveNodeTo(tmpGN, tmpP);
			}
		}
		return returnVal;
	}

	void randomPlacement() {
		final int placeFak = 2; // Wieviel Raum soll zwischen 2 Knoten sein
		final int randOff = RAND;
		final int numNodes = graph.getNumNodes();
		// JJSet<JJNode>& tmpNS = graph.allNodes();

		anzX = (int) Math.ceil(Math.sqrt(numNodes));
		anzY = (int) Math.ceil(((float) numNodes) / anzX);

		anzX = (anzX - 1) * placeFak + 1;
		anzY = (anzY - 1) * placeFak + 1;

		anzX += 2 * randOff;
		anzY += 2 * randOff;

		count = anzX * anzY;

		// cerr << "Breite: " << anzX << " L�nge: " << count/anzX << endl;

		nodes = new JJNode[count];

		for (int k = 0; k < count; k++)
			nodes[k] = null;

		// Startpositionen der Knoten setzen

		int tmpPos = randOff * anzX + randOff;
		final double tmpMaxX = (anzX - (randOff + 1)) * xOffSet;
		// cerr << "Max X: " << tmpMaxX << endl;

		for (final Iterator<JJNode> i = graph.nodeIterator(); i.hasNext();) {
			final JJNode tmpN = i.next();
			final JJGraphicNode gn = tmpN.getGraphicNode(fenster);

			if (gn != null) {
				nodes[tmpPos] = tmpN;
				tmpN.setValue(tmpPos);
				fenster.moveNodeTo(gn, aktuX, aktuY);

				aktuX += placeFak * xOffSet;
				tmpPos += placeFak;

				if (aktuX > tmpMaxX) {
					aktuX = randOff * xOffSet;
					aktuY += placeFak * yOffSet;
					tmpPos += (2 * randOff) - 1;
					tmpPos += (placeFak - 1) * anzX;
				}
			}
		}
	}

	@Override
	public void layout(final Collection<JJNode> c) {
		layout();
	}

	@Override
	public void layout() {
		final int randOff = RAND;

		aktuX = randOff * xOffSet;
		aktuY = randOff * yOffSet;
		count = 0;

		final boolean tmpUpdate = true;
		// if (graph.getWindow ())
		// {
		// graph.getWindow().setBusy ();
		// tmpUpdate = graph.getWindow().setUpdate(false);
		// }

		// JJSet<JJNode>& tmpNS = graph.allNodes();
		fenster.removeBends();

		if (initPlacement == "random")
			randomPlacement();
		else
			while (adjustPlacement() == false) {
				// cerr << "Reducing grid space and retry" << endl;
				setxoff(getxoff() / 2);
				setyoff(getyoff() / 2);
				// if(inspector != null)
				// inspector.inspect();
			}

		// Initialisiere Entfernungen
		// WIN_UPD();

		for (final Iterator<JJNode> i = graph.nodeIterator(); i.hasNext();) {

			final JJNode tmpN = i.next();
			// if(tmpN.getGraphicNode() != null)
			tmpN.setCoord(distSum(tmpN));
			// else
			// tmpN.setCoord(Double.MAX_VALUE);
		}

		final double oldSum = getEdgeSum() / 2;

		// PES("Nach Knoten positionieren: ");

		// Versuche Knoten an optimale Positionen zu setzen

		if (doSwapping) {
			while (swapNodes()) {
				if (anim) {
					// PES("Nach Swappen: ");

					// if (graph.getWindow ())
					// graph.getWindow().update();

					// WIN_UPD(3);
				}
			}

			final double newSum = getEdgeSum();
			// cerr << "Swapping reduced edge length to " << (newSum/oldSum)*100
			// << "%."
			// << endl;
		}

		if (doSwapBadies) {
			while (swapBadies()) {
				if (anim) {
					// PES("Nach Badies: ");

					// if (graph.getWindow ())
					// graph.getWindow().update();
				}
			}

			final double newSum = getEdgeSum();
			// cerr << "Swapping badies reduced edge length to " <<
			// (newSum/oldSum)*100
			// << "%." << endl;
		}

		// Kantenpositionen
		if (doEdgeAdjustment) {
			if (get3D())
				edgePos3D();
			else
				edgePos2D();
		}

		// if (graph.getWindow ())
		// {
		// graph.getWindow().unsetBusy ();
		// graph.getWindow().setUpdate(tmpUpdate);
		// graph.getWindow().adjustWindowSize();
		// }
		// cerr << "Ready" << endl;

		// delete [] nodes;
		nodes = null;
	}

	void edgePos3D() {
		final int randOff = RAND;
		// JJSet<JJNode>& tmpNS= graph.allNodes();

		// cerr << "Doing 3D edge adjustment" << endl;

		for (final Iterator<JJNode> i = graph.nodeIterator(); i.hasNext();) {
			final Iterator<JJEdge> j = i.next().outIterator();
			while (j.hasNext()) {
				final JJEdge tmpEdge = j.next();
				final JJNode tmpSource = tmpEdge.getSource();
				final JJNode tmpTarget = tmpEdge.getTarget();
				final JJGraphicNode tmpGS = tmpSource.getGraphicNode(fenster);
				final JJGraphicNode tmpGT = tmpTarget.getGraphicNode(fenster);
				final JJGraphicEdge tmpGE = tmpEdge.getGraphicEdge(fenster);

				if ((tmpGS != null) && (tmpGT != null) && (tmpGE != null)) {
					final JJPoint srcP = tmpGS.getCoords();
					final JJPoint tgtP = tmpGT.getCoords();

					final double z = Math.sqrt((srcP.getX() * srcP.getY() + tgtP.getX() * tgtP.getY()) / 2);

					final JJPoint tmpP1 = JJPoint.plus(srcP, 0, 0, z);
					final JJPoint tmpP3 = JJPoint.plus(tgtP, 0, 0, z);
					final JJPoint tmpP2 = new JJPoint(tmpP3.getX(), tmpP1.getY(), z);

					tmpGE.addBendLast(tmpP1);
					tmpGE.addBendLast(tmpP2);
					tmpGE.addBendLast(tmpP3);
				}
			}
		}
	}

	boolean testNeighbor(final JJPoint p1, final JJPoint p2) {
		// int row1 = pos1 / anzX;
		// int row2 = pos2 / anzX;

		// int col1 = pos1 % anzX;
		// int col2 = pos2 % anzX;

		final int row1 = (int) (p1.getY() / yOffSet);
		final int row2 = (int) (p2.getY() / yOffSet);

		final int col1 = (int) (p1.getX() / xOffSet);
		final int col2 = (int) (p2.getX() / xOffSet);

		// cerr << "Testing " << p1()<< " (" << row1 << "," << col1 << ")" <<
		// endl;
		// cerr << " to " << p2()<< " (" << row2 << "," << col2 << ")" << endl;

		if (col1 == col2) {
			final int start = Math.min(row1, row2) * anzX + col1;
			final int ziel = Math.max(row1, row2) * anzX + col1;
			boolean frei = true;

			for (int k = start + anzX; k < ziel; k += anzX) {
				// cerr << " ... pos: " << k << endl;

				if ((k < count) && (nodes[k] != null))
					frei = false;
			}
			return frei;
		} else if (row1 == row2) {
			final int start = Math.min(col1, col2) + row1 * anzX;
			final int ziel = Math.max(col1, col2) + row2 * anzX;
			boolean frei = true;

			for (int k = start + 1; k < ziel; k++) {
				// cerr << " ... pos: " << k << endl;
				if ((k < count) && (nodes[k] != null))
					frei = false;
			}
			return frei;
		}

		return false;
	}

	void edgePos2D() {
		final int randOff = RAND;

		final InOutStruct inOut[] = new InOutStruct[graph.getNumNodes()];
		for (int i = 0; i < inOut.length; i++) {
			inOut[i] = new InOutStruct();
		}

		// memset(inOut, 0, sizeof(inOutStruct) * graph.getNumNodes());

		final HashSet<JJEdge> nachbarn = new HashSet<>();

		// JJSet<JJEdge> nachbarn;

		// JJSet<JJNode>& tmpNS= graph.allNodes();

		int k = 0;
		for (final Iterator<JJNode> i = graph.nodeIterator(); i.hasNext();) {
			i.next().setValue(k++);
		}

		for (final Iterator<JJNode> i = graph.nodeIterator(); i.hasNext();) {
			final JJNode tmpSource = i.next();
			final JJGraphicNode tmpGS = tmpSource.getGraphicNode(fenster);

			// JJSet<JJEdge>& tmpES = tmpNS(i).transToSucc();
			for (final Iterator<JJEdge> j = tmpSource.outIterator(); j.hasNext();) {
				final JJEdge tmpEdge = j.next();
				final JJNode tmpTarget = tmpEdge.getTarget();
				final JJGraphicNode tmpGT = tmpTarget.getGraphicNode(fenster);
				final JJGraphicEdge tmpGE = tmpEdge.getGraphicEdge(fenster);

				if ((tmpGS != null) && (tmpGT != null) && (tmpGE != null)) {
					final JJPoint srcP = tmpGS.getCoords();
					final JJPoint tgtP = tmpGT.getCoords();

					if (testNeighbor(srcP, tgtP)) {
						nachbarn.add(tmpEdge);
						// cerr << tmpSource.getName() << " und " <<
						// tmpTarget.getName()
						// << " sind Nachbarn." << endl;
					} else {
						adjustEdge(tmpGS, tmpGT, inOut, tmpGE);
						// cerr << tmpSource.getName() << " und " <<
						// tmpTarget.getName()
						// << " sind keine Nachbarn." << endl;
					}
				}
			}
		}

		boolean repeat = false;

		final HashSet<JJEdge> delSet = new HashSet<>();

		// JJSet<JJEdge> delSet;

		do {
			for (final JJEdge jjEdge : delSet) {
				nachbarn.remove(jjEdge);
			}
			delSet.clear();
			repeat = false;

			for (final Object element : nachbarn) {
				final JJEdge tmpEdge = (JJEdge) element;

				final JJNode tmpTN = tmpEdge.getTarget();
				final JJPoint tmpTP = tmpTN.getGraphicNode(fenster).getCoords();

				final JJNode tmpSN = tmpEdge.getSource();
				final JJPoint tmpSP = tmpSN.getGraphicNode(fenster).getCoords();

				if (((tmpTP.getX() < tmpSP.getX()) && (inOut[tmpSN.getValue()].l == false))
						|| ((tmpTP.getX() > tmpSP.getX()) && (inOut[tmpSN.getValue()].r == false))
						|| ((tmpTP.getY() < tmpSP.getY()) && (inOut[tmpTN.getValue()].o == false))
						|| ((tmpTP.getY() > tmpSP.getY()) && (inOut[tmpTN.getValue()].u == false))) {
				} else {
					adjustEdge(tmpSN.getGraphicNode(fenster), tmpTN.getGraphicNode(fenster), inOut,
							tmpEdge.getGraphicEdge(fenster));
					delSet.add(tmpEdge);
					repeat = true;
				}
			}
		} while (repeat == true);
	}

	void adjustEdge(final JJGraphicNode tmpGS, final JJGraphicNode tmpGT, final InOutStruct inOut[],
			final JJGraphicEdge tmpGE) {
		final int randOff = RAND;
		final JJPoint srcP = tmpGS.getCoords();
		final JJPoint tgtP = tmpGT.getCoords();
		final JJPoint tmpP1 = srcP;
		final JJPoint tmpP3 = tgtP;
		final JJPoint tmpP2 = new JJPoint(srcP.getX(), tgtP.getY());
		final double tmpFakX = (xOffSet / 2.0) / anzX;
		final double tmpFakY = (yOffSet / 2.0) / anzY;

		final int tmpPos = (int) ((tmpP2.getY() / yOffSet) * anzX + tmpP2.getX() / xOffSet);

		if ((nodes[tmpPos] == null) && testNeighbor(srcP, tmpP2) && testNeighbor(tgtP, tmpP2)) {
			tmpGE.addBendFirst(tmpP2); // PointAt(1,tmpP2);
		} else {
			if (srcP.getY() > tgtP.getY()) {
				tmpP1.setY(tmpP1.getY() - (getWidth(tmpGS) + // getRange().getX()
																// +
						(srcP.getX() / xOffSet - randOff) * tmpFakX));
			} else {
				tmpP1.setY(tmpP1.getY() + (getWidth(tmpGS) + // Range().getX() +
						(srcP.getX() / xOffSet - randOff) * tmpFakX));
			}
			if (tgtP.getX() > srcP.getX()) {
				tmpP3.setX(tmpP3.getX() - (getWidth(tmpGT) + // Range().getX() +
						(tgtP.getY() / yOffSet - randOff) * tmpFakY));
			} else {
				tmpP3.setX(tmpP3.getX() + (getWidth(tmpGT) + // Range().getX() +
						(tgtP.getY() / yOffSet - randOff) * tmpFakY));
			}

			tmpP2.setX(tmpP3.getX());
			tmpP2.setY(tmpP1.getY());

			tmpGE.addBendLast(tmpP1); // PointAt(1,tmpP1);
			tmpGE.addBendLast(tmpP2); // PointAt(2,tmpP2);
			tmpGE.addBendLast(tmpP3); // PointAt(3,tmpP3);
		}

		if (srcP.getY() > tgtP.getY())
			inOut[tmpGS.getNode().getValue()].u = true;
		else
			inOut[tmpGS.getNode().getValue()].o = true;

		if (tgtP.getX() > srcP.getX())
			inOut[tmpGT.getNode().getValue()].l = true;
		else
			inOut[tmpGT.getNode().getValue()].r = true;
	}

	public double getWidth(final JJGraphicNode n) {
		return n.getWidth();

		// JJGraphWindow window = n.getNode().getGraph().getWindow();
		// if(window == null)
		// return 10;
		// return window.getWidth(n);
	}

	public boolean get3D() {
		return threeD;
	}

	public void set3D(final boolean b) {
		threeD = b;
	}

	public boolean getSwapping() {
		return doSwapping;
	}

	public void setSwapping(final boolean b) {
		doSwapping = b;
	}

	public boolean getSwapBadies() {
		return doSwapBadies;
	}

	public void setSwapBadies(final boolean b) {
		doSwapBadies = b;
	}

	public boolean getEdgeAdjust() {
		return doEdgeAdjustment;
	}

	public void setEdgeAdjust(final boolean b) {
		doEdgeAdjustment = b;
	}

	public String getInitPlacement() {
		return initPlacement;
	}

	public void setInitPlacement(final String b) {
		initPlacement = b;
	}

	public boolean getAnim() {
		return anim;
	}

	public void setAnim(final boolean b) {
		anim = b;
	}

	public void setxoff(final double x) {
		xOffSet = x;
	}

	public void setyoff(final double y) {
		yOffSet = y;
	}

	public double getxoff() {
		return xOffSet;
	}

	public double getyoff() {
		return yOffSet;
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
