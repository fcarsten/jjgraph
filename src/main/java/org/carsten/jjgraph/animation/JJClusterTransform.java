/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.animation;

/**
 * JJClusterTransform.java
 *
 *
 * Created: Wed Feb 16 16:32:29 2000
 *
 * @author Carsten Friedrich
 * @version
 *
 */
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.util.GFTransformMatrix;
import org.carsten.jjgraph.util.JJArrayRegressCollection;
import org.carsten.jjgraph.util.JJLinAlgException;
import org.carsten.jjgraph.util.JJLinearRegress;
import org.carsten.jjgraph.util.JJPoint;
import org.carsten.jjgraph.util.JJRegressPointPair;
import org.carsten.jjgraph.util.JJRegressableCollection;

public class JJClusterTransform {
	protected JJAnimationNode animationNodes[];
	protected JJGraphAnimator graphAnimator;

	protected JJPoint startFramePoints[];
	protected JJPoint endFramePoints[];
	protected JJClusterTransformNode transformNodes[];

	protected JJGraphWindow fenster;

	final static int CLUSTERS = 10;
	final static int MAXROUNDS = 1000;

	public JJClusterTransform(final JJGraphAnimator b) {
		graphAnimator = b;
	}

	void computeDataSet(final Set<JJClusterDatum> datumSet) {
		final JJRegressPointPair regressTriangle[] = new JJRegressPointPair[1];
		for (int i = 0; i < 1; i++)
			regressTriangle[i] = new JJRegressPointPair();

		// for(int i=0; i<5; i++){
		for (int j = 0; j < startFramePoints.length; j++) {

			regressTriangle[0].p1 = startFramePoints[j];
			regressTriangle[0].p2 = endFramePoints[j];

			final JJClusterDatum m = new JJClusterDatum(regressTriangle);

			datumSet.add(m);
		}
		// }
	}

	//
	// Remove all matrices which are not supported by more than one triangle
	//

	// void stripMatrixSet(Set matrixSet)
	// {
	// for(Iterator iter = matrixSet.iterator(); iter.hasNext();){
	// if ( ((JJClusterTransformMatrix)iter.next()).value <= 1)
	// iter.remove();
	// }
	// }

	// void displayMatrixSet(Set matrixSet)
	// {
	// List l = new LinkedList();
	// List ldtrans = new LinkedList();

	// for(Iterator iter = matrixSet.iterator(); iter.hasNext();){
	// JJClusterTransformMatrix m = (JJClusterTransformMatrix) iter.next();
	// JJPoint p1 = m.transform(10,0);
	// JJPoint p2 = m.transform(0,10);

	// JJPoint d1 = m.dtransform(10,0);
	// JJPoint d2 = m.dtransform(0,10);

	// for(int i=0; i< m.value; i++){
	// Line2D.Double line = new Line2D.Double();
	// // line.setLine(0,0, p2.x -p1.x, p2.y -p1.y);
	// line.setLine(p1.x, p1.y , p2.x, p2.y);
	// l.add(new JJRedGreenLine(line));

	// Line2D.Double dline = new Line2D.Double();
	// dline.setLine(d1.x, d1.y , d2.x, d2.y);
	// ldtrans.add(new JJRedGreenLine(dline));

	// }
	// }
	// lplotter = new JJLinePlotter(l);
	// ldplotter = new JJLinePlotter(ldtrans);

	// }
	// JJLinePlotter lplotter, ldplotter;

	// void displayMatrixSet(Set matrixSet)
	// {
	// List l = new LinkedList();

	// for(Iterator iter = matrixSet.iterator(); iter.hasNext();){
	// JJClusterTransformMatrix m = (JJClusterTransformMatrix) iter.next();
	// double k[] = new double[7];
	// for(int ii=0;ii<6;ii++){
	// k[ii] = m.m[ii];
	// }
	// k[6] = m.getAngle();

	// //for(int i=0; i< m.value; i++){
	// l.add(new JJArrayObject(k));
	// //}
	// }
	// lplotter = new JJArrayPlotter(l);
	// }

	// void displayDataSet(Set dataSet)
	// {
	// List l = new LinkedList();

	// for(Iterator iter = dataSet.iterator(); iter.hasNext();){
	// JJClusterDatum m = (JJClusterDatum) iter.next();
	// double k[] = m.getMatrix(); // new double[7];
	// // for(int ii=0;ii<6;ii++){
	// // k[ii] = m.getMatrixEntryAt(ii);
	// // }
	// // k[6] = m.getAngle();

	// //for(int i=0; i< m.value; i++){
	// l.add(new JJArrayObject(k));
	// //}
	// }
	// lplotter = new JJArrayPlotter(l);
	// }

	// JJArrayPlotter lplotter;

	JJAnimationCluster[] clusterDataSet(final Set<JJClusterDatum> dataSet) {
		final int numClusters = Math.min(CLUSTERS, (dataSet.size() / 20) + 1);

		final JJAnimationCluster clusters[] = new JJAnimationCluster[numClusters];

		for (int i = 0; i < clusters.length; i++) {
			clusters[i] = new JJAnimationCluster();
		}
		int i = 0;

		for (final JJClusterDatum element : dataSet) {
			final JJClusterDatum data = element;
			clusters[i].add(data);
			dataSet.add(data);
			data.setCurrentCluster(i);
			i = (i + 1) % clusters.length;
		}

		for (i = 0; i < MAXROUNDS; i++) {
			boolean converged = true;

			for (final JJAnimationCluster cluster : clusters) {
				cluster.recomputeCenter();
			}

			for (final JJClusterDatum element : dataSet) {
				final JJClusterDatum data = element;
				final int k = findBestCluster(data, clusters);
				if ((k > -1) && (k != data.getCurrentCluster())) {
					clusters[data.getCurrentCluster()].remove(data);
					clusters[k].add(data);
					data.setCurrentCluster(k);
					converged = false;
				}
			}
			if (converged) {
				// Debug.println("Converged after " + i + " rounds");
				break;
			}

		}

		return clusters;
	}

	int findBestCluster(final JJClusterDatum m, final JJAnimationCluster clusters[]) {
		double currentDist = 0;
		int currentCluster = -1;

		for (int i = 0; i < clusters.length; i++) {
			if (clusters[i].valid) {
				final double tmp = clusters[i].dist(m);

				if ((currentCluster != -1) && (currentDist <= tmp))
					continue;
				currentDist = tmp;
				currentCluster = i;
			}
		}
		return currentCluster;
	}

	int findBestCluster(final JJClusterTransformNode m, final JJAnimationCluster clusters[]) {
		double currentDist = 0;
		int currentCluster = -1;

		for (int i = 0; i < clusters.length; i++) {
			if (!clusters[i].valid)
				continue;

			final JJPoint p = clusters[i].center.transform(m.startPoint);
			final double tmp = JJPoint.dist(p, m.endPoint);

			if ((currentCluster != -1) && (currentDist <= tmp))
				continue;
			currentDist = tmp;
			currentCluster = i;
		}

		return currentCluster;
	}

	void initMatrices() {
		final Set<JJClusterDatum> dataSet = new HashSet<>();

		for (int i = 0; i < animationNodes.length; i++) {
			startFramePoints[i] = animationNodes[i].getStartPosition();
			endFramePoints[i] = animationNodes[i].getEndPosition();
			transformNodes[i] = new JJClusterTransformNode(startFramePoints[i], endFramePoints[i], this,
					animationNodes[i]);
		}

		computeDataSet(dataSet);
		// displayMatrixSet(matrixSet);

		final JJAnimationCluster clusters[] = clusterDataSet(dataSet);

		// stripMatrixSet(matrixSet);

		// Now we find the best matrix for each point
		//
		// We also make sure that each valid cluster has at least 3 nodes
		// associated.

		final List<JJClusterTransformNode> clusterNodes[] = assignNodesToClusters(clusters);

		computeMatrixForNewCentersAndAssignMatrix(clusterNodes, clusters);
	}

	List<JJClusterTransformNode>[] assignNodesToClusters(final JJAnimationCluster clusters[]) {
		@SuppressWarnings("unchecked")
		final List<JJClusterTransformNode> clusterNodes[] = new List[clusters.length];
		for (int i = 0; i < clusterNodes.length; i++) {
			clusterNodes[i] = new LinkedList<>();
		}

		final List<JJClusterTransformNode> toDo = new LinkedList<>();
		for (final JJClusterTransformNode transformNode : transformNodes) {
			toDo.add(transformNode);
		}

		while (!toDo.isEmpty()) {
			while (!toDo.isEmpty()) {
				// Debug.println("" + transformNodes[i]);

				final JJClusterTransformNode tn = toDo.remove(0);
				final int tm = findBestCluster(tn, clusters);

				if (tm >= 0)
					clusterNodes[tm].add(tn);
				else {
					// Debug.println("Found node with no suitable cluster: " +
					// tn.animationNode);
					tn.setTransformMatrix(null);
				}
			}
			stripClusters(clusterNodes, clusters, toDo);
		}
		return clusterNodes;
	}

	void stripClusters(final List<JJClusterTransformNode> clusterNodes[], final JJAnimationCluster clusters[],
			final List<JJClusterTransformNode> toDo) {
		for (int i = 0; i < clusterNodes.length; i++) {
			if ((clusterNodes[i].size() < 4) && clusters[i].valid) {
				clusters[i].valid = false;
				if (clusterNodes[i].size() > 0) {
					toDo.addAll(clusterNodes[i]);
					return;
				}
			}
		}
	}

	public void computeMatrixForNewCentersAndAssignMatrix(final List<JJClusterTransformNode> clusterNodes[],
			final JJAnimationCluster clusters[]) {
		for (int i = 0; i < clusterNodes.length; i++) {
			if (!clusters[i].valid)
				continue;

			// Debug.println("Recomputing cluster " + i + ", " +
			// clusters[i].center);
			// Debug.println(" size: " + clusterNodes[i].size());

			// if(Debug.DEBUG && (lplotter != null)){
			// double k[] = new double[6];
			// for(int l=0;l<6;l++){
			// k[l] = clusters[i].center.getMatrixEntryAt(l);
			// }
			// // k[6] = clusters[i].center.getAngle();
			// lplotter.add(new JJRedArrayObject(k));
			// }

			final List<JJClusterTransformNode> nodeList = clusterNodes[i];

			JJRegressPointPair regressPoints[];
			JJRegressableCollection rc;
			final JJPoint centre = new JJPoint();

			regressPoints = new JJRegressPointPair[nodeList.size()];
			rc = new JJArrayRegressCollection(regressPoints);
			int counter = 0;

			for (final Object element : nodeList) {
				final JJClusterTransformNode ctn = (JJClusterTransformNode) element;
				regressPoints[counter] = new JJRegressPointPair();
				regressPoints[counter].p1 = ctn.startPoint;
				regressPoints[counter].p2 = ctn.endPoint;
				centre.plusA(regressPoints[counter].p1);
				centre.plusA(regressPoints[counter].p2);
				counter++;
			}
			centre.divA(nodeList.size() * 2);
			// Debug.println(" New center: " + centre);

			GFTransformMatrix newM = null;

			try {
				final double matrix[] = JJLinearRegress.regress(rc, centre);
				JJLinearRegress.computeR2(rc, matrix, centre);
				newM = new GFTransformMatrix(matrix);// , null, centre);

				// if(Debug.DEBUG && (lplotter != null)){
				// double k[] = new double[6];
				// for(int l=0;l<6;l++){
				// k[l] = newM.m[l];
				// }
				// // k[6] = newM.getAngle();
				// lplotter.add(new JJGreenArrayObject(k));
				// }

			} catch (final JJLinAlgException e) {
				// Debug.println("Couldn't compute adjusted linear
				// transformation");
				// Debug.println(" points : " );

				for (final Object element : nodeList) {
					final JJClusterTransformNode ctn = (JJClusterTransformNode) element;
					// Debug.println(" " + ctn.animationNode + ": " +
					// ctn.startPoint + " " + ctn.endPoint);
				}
			}

			// Debug.println(" now: " + newM + "" + (new GFDecomp(newM.m)));
			// Debug.println("");

			for (final Object element : nodeList) {
				final JJClusterTransformNode ctn = (JJClusterTransformNode) element;
				ctn.setCenter(centre);
				ctn.setTransformMatrix(newM);
			}
		}
	}

	public void init(final JJAnimationNode bn[]) {
		animationNodes = bn;
		if (bn == null)
			return;

		transformNodes = new JJClusterTransformNode[animationNodes.length];
		startFramePoints = new JJPoint[animationNodes.length];
		endFramePoints = new JJPoint[animationNodes.length];
		initMatrices();

		reset();
	}

	void reset() {
	}

	public void frame(final double d) {
		for (final JJClusterTransformNode transformNode : transformNodes) {
			transformNode.frame(d);
		}
	}

}
