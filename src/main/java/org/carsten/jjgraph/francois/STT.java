/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.francois;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.carsten.jjgraph.util.Debug;

import lp.LpConstants;
import lp.LpRec;
import lp.Solve;

class Layering {
	Digraph dg;

	// digraph MUST be acyclic
	Layering(final Digraph g) {
		dg = g;
	}

	Map<Object, Integer> number = new HashMap<>();

	void setVarNumber(final Object o, final int num) {
		number.put(o, new Integer(num));
	}

	int getVarNumber(final Object o) {
		return number.get(o).intValue();
	}

	void compute() {
		// create lp system

		final Solve sol = new Solve();
		final LpRec lp = new LpRec(0, dg.numberOfNodes());

		// the variable i that represnets a node is in the order of the
		// list node, starting with 1
		{
			int i = 1;
			for (final Object element : dg.nodes) {
				final Node n = (Node) element;
				setVarNumber(n, i);
				i++;
			}
		}

		final double[] T = new double[dg.numberOfNodes() + 2];

		// first set the opt function T[i]=indeg(i)-outdeg(i)
		for (final Object element : dg.nodes) {
			final Node n = (Node) element;
			final int i = getVarNumber(n);
			T[i] = dg.inDegree(n) - dg.outDegree(n);
		}
		sol.set_obj_fn(lp, T);

		// add edge constraints

		for (int i = 1; i <= dg.numberOfNodes(); i++)
			T[i] = 0;
		for (final Object element : dg.edges) {
			final Edge e = (Edge) element;

			final int s = getVarNumber(e.source);
			final int t = getVarNumber(e.target);

			// add edge constr
			T[s] = 1;
			T[t] = -1;
			sol.add_constraint(lp, T, LpConstants.LE, -1);

			T[s] = 0;
			T[t] = 0;
		}

		final int res = sol.solve(lp);

		// if (res==0)
		// Debug.print(".");
		// else
		// Debug.print("#");

		// get the value of the minimum layer
		int min_layer = Integer.MAX_VALUE;
		for (final Object element : dg.nodes) {
			final Node n = (Node) element;
			min_layer = Math.min(min_layer, (int) sol.best_solution(lp, getVarNumber(n)));
		}

		nbLayers = 0;
		// assign to layers 1 --- k:
		// Layer 1 is at the bottom
		for (final Object element : dg.nodes) {
			final Node n = (Node) element;
			n.layer = (int) sol.best_solution(lp, getVarNumber(n)) - min_layer;
			nbLayers = Math.max(nbLayers, n.layer + 1);
		}
		for (final Object element : dg.nodes) {
			final Node n = (Node) element;
			n.layer = nbLayers - n.layer;
		}
	}

	List<Node>[] Tlayer = null;
	int nbLayers;

	@SuppressWarnings("unchecked")
	void assignToLayers() {
		Tlayer = new List[nbLayers + 1];

		// sol.print_solution(lp);
		for (int i = 1; i <= nbLayers; i++)
			Tlayer[i] = new ArrayList<>();

		// Debug.print("layers");
		for (final Node element : dg.nodes) {
			// Debug.println(" "+element.layer);
			Tlayer[element.layer].add(element);
		}
	}

}

class CrossMinimisation {

	Layering la;
	Digraph dg;

	CrossMinimisation(final Digraph g, final Layering l) {
		dg = g;
		la = l;
	}

	void sweep(final int m) {
		// Debug.println("m="+m);

		int a_nb = 0;
		for (int i = Math.min(m, la.nbLayers - 1); i >= 1; i--) {
			a_nb += minimiseCrossings(i, +1);
		}

		int nb = 0;
		for (int i = Math.max(m, 2); i <= la.nbLayers; i++) {
			nb += minimiseCrossings(i, -1);
		}

		Debug.println("number of crossings=" + (nb + a_nb));
	}

	void layerByLayerSweep() {

		// set default positions in layers
		// for (int i=1;i<=la.nbLayers;i++) {
		// initPosition(i);
		// }
		initBarycenter();

		for (int step = 0; step < 5; step++) {
			// sweep
			sweep(1);
			sweep(la.nbLayers);
		}

	}

	void barycenter(final int lj, final int dir) {

		final List<Node> Li = la.Tlayer[lj + dir];
		final List<Node> Lj = la.Tlayer[lj];

		for (final Node node : Lj) {
			final Node u = node;
			List<Node> Lu = dg.outNodes(u);
			if (dir == 1) {
				Lu = dg.inNodes(u);
			}

			int nb = 1;
			for (final Object element : Lu) {
				final Node v = (Node) element;
				u.xpos += v.xpos;
				nb++;
			}
			u.xpos /= nb;
		}
	}

	void initBarycenter() {
		for (int step = 0; step < 20; step++) {
			for (int i = la.nbLayers - 1; i >= 1; i--) {
				barycenter(i, +1);
				Collections.sort(la.Tlayer[i], new CompCoordX());
				initOrder(i);
			}

			for (int i = 2; i <= la.nbLayers; i++) {
				barycenter(i, -1);
				Collections.sort(la.Tlayer[i], new CompCoordX());
				initOrder(i);
			}

		}

	}

	void initPosition(final int i) {
		// magic secret
		final int nb = dg.numberOfNodes();
		for (final Object element : la.Tlayer[i]) {
			final Node n = (Node) element;
			n.xpos = nb * Math.max(dg.inDegree(n), dg.outDegree(n)) + dg.inDegree(n) + dg.outDegree(n);
			n.xpos = 1000 - (dg.inDegree(n) + dg.outDegree(n));
		}
		Collections.sort(la.Tlayer[i], new CompCoordX());

		int bo = 1;
		for (final Object element : la.Tlayer[i]) {
			final Node n = (Node) element;
			n.xpos = bo * n.xpos;
			bo = -bo;
		}
		Collections.sort(la.Tlayer[i], new CompCoordX());

		initOrder(i);

	}

	void initOrder(final int i) {
		int x = 0;
		for (final Object element : la.Tlayer[i]) {
			final Node n = (Node) element;
			n.xpos = Math.max(x + 10, n.xpos);
			x = n.xpos;
		}

	}

	class CompCoordX implements Comparator<Node> {
		@Override
		public int compare(final Node o1, final Node o2) {
			return o2.xpos - o1.xpos;
		}
	}

	// what would be good: looking for connected component and solve
	// the problem on each component (and then we get smaller LP problems)...
	// permute nodes on layer j
	int minimiseCrossings(final int lj, final int dir) {

		List<Node> Li = la.Tlayer[lj + dir];
		if (dir == 0) {
			Li = new ArrayList<>();
			if (lj > 1)
				Li.addAll(la.Tlayer[lj - 1]);
			if (lj < la.nbLayers)
				Li.addAll(la.Tlayer[lj + 1]);
		}

		final List<Node> Lj = la.Tlayer[lj];

		final int nb_i = Li.size();
		final int nb_j = Lj.size();

		// Debug.println("put number 0--nb_j on each node of Lj");
		int num = 0;
		for (final Object element : Lj) {
			final Node u = (Node) element;
			u.number = num++;
		}

		//
		// Debug.println("compute cuv for all pair of nodes of Lj");
		final int[][] cuv = new int[nb_j][nb_j];
		for (final Object element : Lj) {
			final Node u = (Node) element;
			for (final Object element2 : Lj) {
				final Node v = (Node) element2;
				cuv[u.number][v.number] = 0;
				if (u == v) {
				} else {
					List<Node> Lu = dg.outNodes(u);
					List<Node> Lv = dg.outNodes(v);
					if (dir == 1) {
						Lu = dg.inNodes(u);
						Lv = dg.inNodes(v);
					} else if (dir == 0) {
						Lu = new ArrayList<>();
						Lv = new ArrayList<>();
						Lu.addAll(dg.inNodes(u));
						Lu.addAll(dg.outNodes(u));
						Lv.addAll(dg.inNodes(v));
						Lv.addAll(dg.outNodes(v));
					}

					for (final Object element3 : Lu) {
						final Node w = (Node) element3;
						for (final Object element4 : Lv) {
							final Node z = (Node) element4;
							if (z.xpos < w.xpos)
								cuv[u.number][v.number]++;
						}
					}

					// Debug.println("cuv["+u.number+"]["+v.number+"]="+(cuv[u.number][v.number]));

				}
			}
		}

		// count nbr of crossings
		int nb_crossings = 0;
		for (final Object element : Lj) {
			final Node u = (Node) element;
			for (final Object element2 : Lj) {
				final Node v = (Node) element2;
				if (u.xpos < v.xpos) {
					nb_crossings += cuv[u.number][v.number];
				}
			}
		}
		// Debug.println("previous nbr of crossings"+nb_crossings);

		// Debug.println("assign a variable number for every pair (u,v) with
		// u.number<v.number");
		// very stupid method, but I am in a hurry, I have to pick up
		// someone at the airport, I have been sick all week and it is my dad's
		// birsday on the weekend
		int nb_var = 0; // ok, could compute directly...
		final int[][] var_uv = new int[nb_j][nb_j]; // ok, it's too big

		for (final Object element : Lj) {
			final Node u = (Node) element;
			for (final Object element2 : Lj) {
				final Node v = (Node) element2;
				if (u.number < v.number) {
					nb_var++;
					var_uv[u.number][v.number] = nb_var;
				}
			}
		}

		// Debug.println("create ilp problem"+nb_var);
		final Solve sol = new Solve();
		final LpRec lp = new LpRec(0, nb_var);

		// Debug.println("ok, the objective function");

		final double[] T = new double[nb_var + 2];

		for (final Object element : Lj) {
			final Node u = (Node) element;
			for (final Object element2 : Lj) {
				final Node v = (Node) element2;
				if (u.number < v.number) {
					// the obj func
					T[var_uv[u.number][v.number]] = cuv[u.number][v.number] - cuv[v.number][u.number];
				}
			}
		}
		sol.set_obj_fn(lp, T);

		// Debug.println("add constraints 2");
		for (final Object element : Lj) {
			final Node u = (Node) element;
			for (final Object element2 : Lj) {
				final Node v = (Node) element2;
				if (u.number < v.number) {
					// ilp
					sol.set_int(lp, var_uv[u.number][v.number], LpConstants.TRUE);

					// 0<=xuv<=1
					sol.set_lowbo(lp, var_uv[u.number][v.number], 0);
					sol.set_upbo(lp, var_uv[u.number][v.number], 1);
				}
			}
		}

		// Debug.println("add constraints 1");
		for (int i = 1; i <= nb_var; i++)
			T[i] = 0;
		for (final Object element : Lj) {
			final Node u = (Node) element;
			for (final Object element2 : Lj) {
				final Node v = (Node) element2;
				for (final Object element3 : Lj) {
					final Node w = (Node) element3;
					if (u.number < v.number && v.number < w.number) {
						T[var_uv[u.number][v.number]] = 1;
						T[var_uv[v.number][w.number]] = 1;
						T[var_uv[u.number][w.number]] = -1;
						sol.add_constraint(lp, T, LpConstants.GE, 0);
						sol.add_constraint(lp, T, LpConstants.LE, 1);
						T[var_uv[u.number][v.number]] = 0;
						T[var_uv[v.number][w.number]] = 0;
						T[var_uv[u.number][w.number]] = 0;
					}
				}
			}
		}

		// ok, that's it
		final int res = sol.solve(lp);

		// if (res==0)
		// Debug.print(".");
		// else
		// Debug.print("#");

		// sol.print_lp(lp);

		// sol.print_solution(lp);

		// now, set positions
		for (final Object element : Lj) {
			final Node u = (Node) element;
			u.xpos = 0;
		}

		int new_nb_crossings;

		{
			new_nb_crossings = (int) sol.obj_fn_value(lp);

			for (final Object element : Lj) {
				final Node u = (Node) element;
				for (final Object element2 : Lj) {
					final Node v = (Node) element2;
					if (u.number < v.number) {
						new_nb_crossings += cuv[v.number][u.number];
						v.xpos += 10 * ((int) sol.best_solution(lp, var_uv[u.number][v.number]));
						u.xpos += 10 * (1 - (int) sol.best_solution(lp, var_uv[u.number][v.number]));

					}
				}
			}
		}
		// Debug.println("new crossings:"+new_nb_crossings);
		Collections.sort(la.Tlayer[lj], new CompCoordX());
		return new_nb_crossings;
	}

}

public class STT {
	Digraph dg;

	// digraph MUST be acyclic
	STT(final Digraph g) {
		dg = g;
	}

	static int vertical_dist = 100;
	static int horizontal_dist = 100;

	void verticalPositions(final Layering la) {
		for (final Object element : dg.nodes) {
			final Node n = (Node) element;
			// n.ypos=(la.nbLayers-n.layer)*vertical_dist;
			n.ypos = (n.layer) * vertical_dist;
		}
	}

	Map<Object, Integer> number = new HashMap<>();

	void setVarNumber(final Object o, final int num) {
		number.put(o, new Integer(num));
	}

	int getVarNumber(final Object o) {
		return number.get(o).intValue();
	}

	void horizontalPositions(final Layering la) {
		int nb_var = 0;
		for (final Edge element : dg.edges) {
			final Edge e = element;
			nb_var++;
			setVarNumber(e, nb_var);
		}

		for (final Object element : dg.nodes) {
			final Node n = (Node) element;
			nb_var++;
			setVarNumber(n, nb_var);
		}

		final Solve sol = new Solve();
		final LpRec lp = new LpRec(0, nb_var);

		final double[] T = new double[nb_var + 1];
		for (int i = 1; i <= nb_var; i++)
			T[i] = 0;
		{ // obj function : i.e. min sum y_ij (sum |xi-xj|)
			for (final Object element : dg.edges) {
				final Edge e = (Edge) element;
				T[getVarNumber(e)] = 1;
			}

			sol.set_obj_fn(lp, T);
		}

		for (int i = 1; i <= nb_var; i++)
			T[i] = 0;
		// Debug.println(" create lp : cnstrs (1)");

		for (int li = 1; li <= la.nbLayers; li++) { // xi+unit<xj, xj+unit<xk

			Node a = null;
			for (final Object element : la.Tlayer[li]) {
				final Node b = (Node) element;
				if (a != null) {
					T[getVarNumber(a)] = -1;
					T[getVarNumber(b)] = 1;
					int unit = horizontal_dist;
					if (a.dummy || b.dummy) // less space when a dummy
						unit /= 2;
					sol.add_constraint(lp, T, LpConstants.GE, unit);
					T[getVarNumber(a)] = 0;
					T[getVarNumber(b)] = 0;
				}
				a = b;
			}
		}

		// Debug.println(" create lp : cnstrs (2)");
		{ // y_ij=|x_i-y_j}
			for (final Object element : dg.edges) {
				final Edge e = (Edge) element;
				final Node a = e.source;
				final Node b = e.target;

				T[getVarNumber(e)] = 1;
				T[getVarNumber(a)] = 1;
				T[getVarNumber(b)] = -1;
				sol.add_constraint(lp, T, LpConstants.GE, 0);

				T[getVarNumber(a)] = -1;
				T[getVarNumber(b)] = 1;
				sol.add_constraint(lp, T, LpConstants.GE, 0);

				T[getVarNumber(e)] = 0;
				T[getVarNumber(a)] = 0;
				T[getVarNumber(b)] = 0;
			}
		}

		final int res = sol.solve(lp);
		// if (res==0)
		// Debug.print(".");
		// else
		// Debug.print("#");
		{
			for (final Object element : dg.nodes) {
				final Node b = (Node) element;
				b.xpos = (int) sol.best_solution(lp, getVarNumber(b));
			}
		}

	}

	void addDummyNodes() {
		// no concurent modifciton of the lists of nodes or edges
		final List<Edge> le = new ArrayList<>();
		final List<Node> ln = new ArrayList<>();
		final List<Edge> del_le = new ArrayList<>();

		for (final Edge element : dg.edges) {
			final Edge e = element;

			final int ks = e.source.layer;
			final int kt = e.target.layer;
			// Debug.println(ks+":"+kt);
			if (ks - kt <= 1) {
			} else {
				final boolean bo = e.reverse;

				del_le.add(e);

				final int n = ks - kt;

				Node a = e.source;
				Edge ee = null;
				for (int j = 1; j < n; j++) {
					final Node b = new Node(dg.nb_id++);
					ln.add(b);
					e.dummies.add(b);

					b.layer = ks - j;

					b.dummy = true;

					ee = new Edge(a, b);
					ee.reverse = bo;
					le.add(ee);
					a = b;
				}

				ee = new Edge(a, e.target);
				ee.reverse = bo;

				le.add(ee);
			}
		}

		Debug.println("number of dummy nodes=" + ln.size());

		for (final Object element : ln) {
			final Node n = (Node) element;
			dg.addNode(n);
		}
		for (final Object element : le) {
			final Edge e = (Edge) element;
			dg.addEdge(e);
		}
		for (final Object element : del_le) {
			final Edge e = (Edge) element;
			dg.removeEdge(e);
		}

	}

	public void layout() {
		Debug.println("Computing layering");

		final Layering la = new Layering(dg);
		la.compute();

		Debug.println("Adding dummy nodes");

		addDummyNodes();

		la.assignToLayers();

		Debug.println("Computing y coords");

		verticalPositions(la);

		Debug.println("Minimize crossings");

		final CrossMinimisation cm = new CrossMinimisation(dg, la);
		cm.layerByLayerSweep();

		Debug.println("Computing x coors");

		horizontalPositions(la);
	}
}
