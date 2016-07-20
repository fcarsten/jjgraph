/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJEdgeImpl;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphImpl;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJNode;

class PointInThreeTrianglesException extends Exception {
}

public class JJDelaunay {

	private JJGraphWindow window = null;
	private final JJGraph pls = new JJGraphImpl(JJTNode.class, JJEdgeImpl.class);
	// Point location structure
	private JJPoint p1, p2, p3;
	JJPoint points[];
	private JJTNode root;

	private JJPoint initPoints[] = null;

	public JJDelaunay(final JJGraphWindow theGraph) {
		window = theGraph;
		initialize();
		delaunay();
	}

	public JJDelaunay(final JJPoint pp[]) {
		initPoints = pp;
		initialize();
	}

	public Set<Edge> doDelaunay() {
		delaunay();
		Debug.println("Generated " + resultSet.size() + " new edges.");
		return resultSet;
	}

	public void doDelaunay(final Set<Edge> rs) {
		resultSet = rs;
		delaunay();
		Debug.println("Num edges: " + resultSet.size() + " new edges.");
	}

	private void splitIntoThree(final int i, final Triangle t) {
		final Edge p1i = new Edge(t.p1, i, this);
		final Edge p2i = new Edge(t.p2, i, this);
		final Edge p3i = new Edge(t.p3, i, this);

		final Edge p1p2 = t.findEdge(t.p1, t.p2);
		final Edge p2p3 = t.findEdge(t.p2, t.p3);
		final Edge p3p1 = t.findEdge(t.p3, t.p1);

		final JJTNode t1 = addNode(t.node, p1p2, p1i, p2i, t.p1, t.p2, i);
		final JJTNode t2 = addNode(t.node, p2p3, p2i, p3i, t.p2, t.p3, i);
		final JJTNode t3 = addNode(t.node, p3p1, p3i, p1i, t.p3, t.p1, i);

		legalizeEdge(i, p1p2);
		legalizeEdge(i, p2p3);
		legalizeEdge(i, p3p1);
	}

	public void legalizeEdge(final int pr, final Edge pipj) {
		// Debug.println("Testing edge "+pipj);

		if (!pipj.isLegal()) {

			Triangle tr = pipj.t1;
			Triangle tk = pipj.t2;

			if (!tr.contains(pr)) {
				final Triangle tmp = tr;
				tr = tk;
				tk = tmp;
			}

			if (!tr.contains(pr))
				throw new RuntimeException("Point " + pr + " not in triangle either side of " + pipj);

			// flip

			final int pj = pipj.a; // tr.next(pr);
			final int pk = tk.opposite(pipj); // tk.next(pj);
			final int pi = pipj.b; // tk.next(pk);

			final Edge prpk = new Edge(pr, pk, this);

			// Debug.println(" Flipping edge to " + prpk);

			final Edge prpj = tr.findEdge(pr, pj);
			final Edge pjpk = tk.findEdge(pj, pk);
			final Edge pkpi = tk.findEdge(pk, pi);
			final Edge pipr = tr.findEdge(pi, pr);

			final JJTNode t4 = addNode(tr.node, tk.node, prpj, pjpk, prpk, pr, pj, pk);
			final JJTNode t5 = addNode(tr.node, tk.node, pipr, prpk, pkpi, pr, pi, pk);
			// pls.addEdge(tk.node, t4);
			// pls.addEdge(tk.node, t5);

			legalizeEdge(pr, pkpi);
			legalizeEdge(pr, pjpk);

		}

	}

	private void splitIntoTwo(final int pr, final Triangle t[]) {
		// Debug.println("split into two " + t[0] + "," + t[1]);

		// Find splitting edge

		Edge pipj = t[0].edges[0];
		if (t[0].area2(points[pipj.a], points[pipj.b], points[pr]) != 0)
			pipj = t[0].edges[1];
		if (t[0].area2(points[pipj.a], points[pipj.b], points[pr]) != 0)
			pipj = t[0].edges[2];
		if (t[0].area2(points[pipj.a], points[pipj.b], points[pr]) != 0)
			throw new RuntimeException("Splint into two: couldn't find edge to split");

		final Triangle tk = t[0];
		final Triangle tl = t[1];

		final int pi = pipj.a;
		final int pj = pipj.b;
		final int pk = tk.opposite(pipj);
		final int pl = tl.opposite(pipj);

		final Edge pipr = new Edge(pi, pr, this);
		final Edge pjpr = new Edge(pj, pr, this);
		final Edge pkpr = new Edge(pk, pr, this);
		final Edge plpr = new Edge(pl, pr, this);

		final Edge plpi = tl.findEdge(pl, pi);
		final Edge plpj = tl.findEdge(pl, pj);
		final Edge pkpi = tk.findEdge(pk, pi);
		final Edge pkpj = tk.findEdge(pk, pj);

		final JJNode tril = addNode(tl.node, pipr, plpr, plpi, pr, pi, pl);
		final JJNode trjl = addNode(tl.node, pjpr, plpr, plpj, pr, pj, pl);
		final JJNode trik = addNode(tk.node, pipr, pkpr, pkpi, pr, pi, pk);
		final JJNode trjk = addNode(tk.node, pjpr, pkpr, pkpj, pr, pj, pk);

		legalizeEdge(pr, plpi);
		legalizeEdge(pr, plpj);
		legalizeEdge(pr, pkpi);
		legalizeEdge(pr, pkpj);
	}

	private void splitIntoTwoOld(final int i, final Triangle t[]) {
		Debug.println("split into two " + t[0] + "," + t[1]);

		final JJTNode t1[] = new JJTNode[2];
		final JJTNode t2[] = new JJTNode[2];
		final JJTNode t3[] = new JJTNode[2];

		final Edge p1i[] = new Edge[2];
		final Edge p2i[] = new Edge[2];
		final Edge p3i[] = new Edge[2];

		for (int k = 0; k < 2; k++) {
			p1i[k] = new Edge(t[k].p1, i, this);
			p2i[k] = new Edge(t[k].p2, i, this);
			p3i[k] = new Edge(t[k].p3, i, this);
			final Edge p1p2 = t[k].findEdge(t[k].p1, t[k].p2);
			final Edge p2p3 = t[k].findEdge(t[k].p2, t[k].p3);
			final Edge p3p1 = t[k].findEdge(t[k].p3, t[k].p1);

			if (t[k].area2(points[t[k].p1], points[t[k].p2], points[i]) > 0) {
				t1[k] = addNode(t[k].node, p1p2, p1i[k], p2i[k], t[k].p1, t[k].p2, i);
			}
			if (t[k].area2(points[t[k].p2], points[t[k].p3], points[i]) > 0) {
				t2[k] = addNode(t[k].node, p2p3, p2i[k], p3i[k], t[k].p2, t[k].p3, i);
			}
			if (t[k].area2(points[t[k].p3], points[t[k].p1], points[i]) > 0) {
				t3[k] = addNode(t[k].node, p3p1, p3i[k], p1i[k], t[k].p3, t[k].p1, i);
			}
		}

		for (int k = 0; k < 2; k++) {
			for (int j = 0; j < 3; j++) {
				legalizeEdge(i, t[k].edges[j]);
			}
		}
	}

	// private boolean isInterior(Triangle t, int i)
	// {
	// return
	// area2( points[t.p1], points[t.p2], points[i]) > 0 &&
	// area2( points[t.p2], points[t.p3], points[i]) > 0 &&
	// area2( points[t.p3], points[t.p1], points[i]) > 0;
	// }

	private void findTrianglesContaining(final JJTNode n, final int i, final Triangle t[])
			throws PointInThreeTrianglesException {
		if ((n.outdeg() == 0) && (n.triangle != t[0])) {
			if (t[0] != null) {
				if ((t[1] != null) && (n.triangle != t[1])) {
					Debug.println("Bad error: found point in > 2 triangles");
					Debug.println("" + t[0]);
					Debug.println("" + t[1]);
					Debug.println("" + n.triangle);
					throw new PointInThreeTrianglesException();

				}
				t[1] = (Triangle) n.triangle;
			} else
				t[0] = (Triangle) n.triangle;
		} else
			for (final Iterator<JJEdge> iter = n.outIterator(); iter.hasNext();) {
				final JJEdge tmpE = iter.next();
				final JJTNode tmpN = (JJTNode) tmpE.getTarget();
				if (((Triangle) tmpN.triangle).insideTriangle(points[i])) {
					findTrianglesContaining(tmpN, i, t);
				}
			}
	}

	private void findTrianglesContaining(final int i, final Triangle t[]) throws PointInThreeTrianglesException {
		findTrianglesContaining(root, i, t);
	}

	private void delaunay() {
		int i = 3;

		try {
			for (; i < points.length; i++) {
				final Triangle t[] = new Triangle[2];
				try {
					findTrianglesContaining(i, t);
				} catch (final PointInThreeTrianglesException e) {
					continue;
				}

				if (t[1] == null) {
					// Debug.println("Adding " + i + " to " + t[0]);

					splitIntoThree(i, t[0]);
				} else {
					splitIntoTwo(i, t);
				}
			}
		} catch (final Exception e) {
			Debug.println(e.getMessage());
			e.printStackTrace();
			displayResult(i);
			return;
		}

		// displayResult(i-1);
		finishUp();
	}

	public void displayResult(final int numNodes) {
		final JJGraph resultG = new JJGraphImpl();
		resultG.setDirected(false);
		final JJGraphWindow resWindow = resultG.createGraphic();

		final JJNode nodes[] = new JJNode[points.length];
		for (int k = 0; k < numNodes + 1; k++) {
			nodes[k] = resultG.addNode();
			nodes[k].setName("" + k);

			resWindow.moveNodeTo(nodes[k].getGraphicNode(resWindow), points[k]);
		}
		for (final Iterator<JJNode> iter = pls.nodeIterator(); iter.hasNext();) {
			final JJTNode tmpN = (JJTNode) iter.next();
			if (tmpN.outdeg() == 0) {
				final Triangle t = (Triangle) tmpN.triangle;
				for (int i = 0; i < 3; i++) {
					if (t.edges[i] != null)
						resultG.addEdge(nodes[t.edges[i].a], nodes[t.edges[i].b]);
					else {
						Debug.println("Triangle without 3 edges");
					}
				}
			}
		}
	}

	public JJGraph getResultGraph() {
		final JJGraph resultG = new JJGraphImpl();
		resultG.setDirected(false);

		final JJNode nodes[] = new JJNode[points.length];
		for (int k = 3; k < points.length; k++) {
			nodes[k] = resultG.addNode();
			nodes[k].setValue(k - 3);

			// resultG.getWindow().moveNodeTo(nodes[k].getGraphicNode(),
			// points[k]);
		}
		for (final Iterator<JJNode> iter = pls.nodeIterator(); iter.hasNext();) {
			final JJTNode tmpN = (JJTNode) iter.next();
			if (tmpN.outdeg() == 0) {
				final Triangle t = (Triangle) tmpN.triangle;
				for (int i = 0; i < 3; i++) {
					if ((t.edges[i] != null) && (t.edges[i].a > 2) && (t.edges[i].b > 2))
						resultG.addEdge(nodes[t.edges[i].a], nodes[t.edges[i].b]);
				}
			}
		}
		resultG.removeParallelEdges();

		return resultG;
	}

	public JJPoint[] getPoints() {
		final JJPoint res[] = new JJPoint[points.length - 3];
		System.arraycopy(points, 3, res, 0, res.length);

		return res;
	}

	private Set<Edge> resultSet = null;
	private Set<Triangle> triangleSet = null;

	public Set<Triangle> getTriangles() {
		return triangleSet;
	}

	public void finishUp() {
		if (resultSet == null)
			resultSet = new HashSet<>();

		if (triangleSet == null)
			triangleSet = new HashSet<>();

		for (final Iterator<JJNode> iter = pls.nodeIterator(); iter.hasNext();) {
			final JJTNode tmpN = (JJTNode) iter.next();
			if (tmpN.outdeg() == 0) {
				final Triangle t = (Triangle) tmpN.triangle;
				t._normalize();

				for (int i = 0; i < 3; i++)
					if (t.edges[i].a >= 0) {
						resultSet.add(t.edges[i]);
					}

				if (t.isValid()) {
					triangleSet.add(t);
				}
			}
		}

		if (window != null) {
			final JJGraph graph = window.getGraph();

			for (final Object element : resultSet) {
				final Edge e = (Edge) element;
				graph.addEdge(origNodes[e.a], origNodes[e.b]);
			}
		}
	}

	private JJNode origNodes[];

	private void initialize() {
		pls.clear();
		int k = 3;
		double maxCoord = 0;

		if (window != null) {
			final JJGraph graph = window.getGraph();

			points = new JJPoint[graph.getNumNodes() + 3];
			origNodes = new JJNode[graph.getNumNodes()];

			for (final Iterator<JJNode> iter = graph.nodeIterator(); iter.hasNext();) {
				final JJNode tmpN = iter.next();
				points[k] = tmpN.getGraphicNode(window).getCoords();
				maxCoord = Math.max(maxCoord, Math.abs(points[k].x));
				maxCoord = Math.max(maxCoord, Math.abs(points[k].y));
				origNodes[k - 3] = tmpN;
				k++;
			}
		} else {
			points = new JJPoint[initPoints.length + 3];

			for (final JJPoint initPoint : initPoints) {
				points[k] = initPoint;
				maxCoord = Math.max(maxCoord, Math.abs(points[k].x));
				maxCoord = Math.max(maxCoord, Math.abs(points[k].y));
				k++;
			}
		}

		p1 = new JJPoint(3 * maxCoord, 0);
		p2 = new JJPoint(0, 3 * maxCoord);
		p3 = new JJPoint(-3 * maxCoord, -3 * maxCoord);

		points[0] = p1;
		points[1] = p2;
		points[2] = p3;

		final Edge e1 = new Edge(0, 1, this);
		final Edge e2 = new Edge(1, 2, this);
		final Edge e3 = new Edge(2, 0, this);

		root = addNode(null, e1, e2, e3, 0, 1, 2);
	}

	private JJTNode addNode(final JJTNode source, final Edge ea, final Edge eb, final Edge ec, final int a, final int b,
			final int c) {
		Triangle oldFace = null;
		if (source != null)
			oldFace = (Triangle) source.triangle;

		final Triangle t = new Triangle(ea, eb, ec, a, b, c, oldFace, this);
		final JJTNode tmpN = (JJTNode) pls.addNode();

		if (source != null)
			pls.addEdge(source, tmpN);

		t.node = tmpN;
		tmpN.triangle = t;
		return tmpN;
	}

	private JJTNode addNode(final JJTNode source1, final JJTNode source2, final Edge ea, final Edge eb, final Edge ec,
			final int a, final int b, final int c) {
		Triangle oldFace1 = null;
		Triangle oldFace2 = null;
		if (source1 != null)
			oldFace1 = (Triangle) source1.triangle;
		if (source2 != null)
			oldFace2 = (Triangle) source2.triangle;

		final Triangle t = new Triangle(ea, eb, ec, a, b, c, oldFace1, oldFace2, this);
		final JJTNode tmpN = (JJTNode) pls.addNode();

		if (source1 != null)
			pls.addEdge(source1, tmpN);

		if (source2 != null)
			pls.addEdge(source2, tmpN);

		t.node = tmpN;
		tmpN.triangle = t;
		return tmpN;
	}

}
