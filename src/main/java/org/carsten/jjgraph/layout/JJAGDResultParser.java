/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.layout;

/**
 * JJAGDResultParser.java
 *
 *
 * Created: Fri Feb 26 13:50:26 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.Iterator;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJGraphicEdge;
import org.carsten.jjgraph.graph.JJGraphicNode;
import org.carsten.jjgraph.graph.JJNode;
import org.carsten.jjgraph.util.Debug;
import org.carsten.jjgraph.util.JJPoint;

public class JJAGDResultParser extends StreamTokenizer {
	JJNode valToNode[];
	int edgeStatus[];
	final static int done = 1;
	final static int undone = 0;

	JJGraphWindow fenster;

	public JJAGDResultParser(final Reader r) {
		super(r);
		resetSyntax();
		whitespaceChars('\u0000', '\u0020');
		parseNumbers();
		wordChars('A', 'Z');
		wordChars('a', 'z');
		wordChars('[', '[');
		wordChars(']', ']');
		wordChars('_', '_');
		commentChar('#');
		quoteChar('\"');
	}

	public boolean parse(final JJGraphWindow theGraph, final JJNode na[]) {
		fenster = theGraph;
		valToNode = na;
		edgeStatus = new int[fenster.getGraph().getNumEdges()];
		int i = 0;

		for (final Iterator<JJEdge> iter = fenster.getGraph().edgeIterator(); iter.hasNext();) {
			final JJEdge tmpE = iter.next();
			tmpE.setValue(i);
			edgeStatus[i++] = undone;
		}

		try {
			while (true) {
				final int c = nextToken();
				switch (c) {
				case StreamTokenizer.TT_EOF:
					return true;
				case StreamTokenizer.TT_WORD:
					if (sval.equals("[")) {
						parseGraphInternals();
					} else if (sval.equals("AGD_error_file")) {
						nextToken();
						fenster.printError("AGD: " + sval);
						System.err.println("AGD: " + sval);
						return false;
					}
					break;
				default:
					break;
				}
			}
		} catch (final IOException e) {
			Debug.println("Error in line " + lineno());
		}
		return false;
	}

	public void parseGraphInternals() throws IOException {
		while (true) {
			final int c = nextToken();
			switch (c) {
			case StreamTokenizer.TT_EOF:
				throw new IOException("Unexpected EOF");
			case StreamTokenizer.TT_WORD:
				if (sval.equals("node")) {
					nextToken();
					parseNodeInternals();
				} else if (sval.equals("edge")) {
					nextToken();
					parseEdgeInternals();
				} else if (sval.equals("]")) {
					return;
				}

				break;
			default:
				break;
			}
		}
	}

	public void parseNodeGraphics(final JJNode theNode) throws IOException {
		double x = 0;
		double y = 0;
		double w = 0;
		double h = 0;

		while (true) {
			final int c = nextToken();
			switch (c) {
			case StreamTokenizer.TT_EOF:
				throw new IOException("Unexpected EOF");
			case StreamTokenizer.TT_WORD:
				if (sval.equals("]")) {
					// JJGraphWindow fenster = graph.getWindow();
					final JJGraphicNode gn = theNode.getGraphicNode(fenster);

					if (gn != null) {
						fenster.moveNodeTo(gn, new JJPoint(x, y));
					} else {
						Debug.println("No graphic context");
					}
					return;
				} else if (sval.equals("x")) {
					nextToken();
					x = nval;
				} else if (sval.equals("y")) {
					nextToken();
					y = nval;
				} else if (sval.equals("w")) {
					nextToken();
					w = nval;
				} else if (sval.equals("h")) {
					nextToken();
					h = nval;
				} else if (sval.equals("[")) {
					parseComment();
				}
				break;
			default:
			}
		}
	}

	public void parseEdgeBendList(final JJEdge theEdge) throws IOException {
		while (true) {
			final int c = nextToken();
			switch (c) {
			case StreamTokenizer.TT_EOF:
				throw new IOException("Unexpected EOF");
			case StreamTokenizer.TT_WORD:
				if (sval.equals("]")) {
					final JJGraphicEdge ge = theEdge.getGraphicEdge(fenster);
					ge.removeBendFirst();
					ge.removeBendLast();

					return;
				} else if (sval.equals("point")) {
					nextToken();
					parseEdgeBend(theEdge);
				}
				break;
			default:
			}
		}
	}

	public void parseEdgeBend(final JJEdge theEdge) throws IOException {
		double x = 0;
		double y = 0;

		while (true) {
			final int c = nextToken();
			switch (c) {
			case StreamTokenizer.TT_EOF:
				throw new IOException("Unexpected EOF");
			case StreamTokenizer.TT_WORD:
				if (sval.equals("]")) {
					final JJGraphicEdge ge = theEdge.getGraphicEdge(fenster);
					ge.addBendLast(new JJPoint(x, y));
					return;
				} else if (sval.equals("x")) {
					nextToken();
					x = nval;
				} else if (sval.equals("y")) {
					nextToken();
					y = nval;
				}
				break;
			default:
			}
		}
	}

	public void parseEdgeGraphics(final JJEdge theEdge) throws IOException {
		while (true) {
			final int c = nextToken();
			switch (c) {
			case StreamTokenizer.TT_EOF:
				throw new IOException("Unexpected EOF");
			case StreamTokenizer.TT_WORD:
				if (sval.equals("]")) {
					return;
				} else if (sval.equals("Line")) {
					nextToken();
					parseEdgeBendList(theEdge);
				} else if (sval.equals("[")) {
					parseComment();
				}
				break;
			default:
			}
		}
	}

	public void parseNodeInternals() throws IOException {
		JJNode theNode = null;

		while (true) {
			final int c = nextToken();
			switch (c) {
			case StreamTokenizer.TT_EOF:
				throw new IOException("Unexpected EOF");
			case StreamTokenizer.TT_WORD:
				if (sval.equals("]")) {
					return;
				} else if (sval.equals("id")) {
					nextToken();
					theNode = valToNode[(int) nval]; // .setValue( (int) nval);
				} else if (sval.equals("label")) {
					nextToken();
				} else if (sval.equals("graphics")) {
					nextToken();
					if (theNode == null)
						throw new IOException("Node id after coordinates");

					parseNodeGraphics(theNode);
				} else if (sval.equals("events")) {
				} else if (sval.equals("[")) {
					parseComment();
				} else {
					// Debug.println("Unknown node internal: " + sval);
				}
				break;
			default: {
			}
			}
		}
	}

	public void parseComment() throws IOException {
		while (true) {
			final int c = nextToken();
			switch (c) {
			case StreamTokenizer.TT_EOF:
				throw new IOException("Unexpected EOF");
			case StreamTokenizer.TT_WORD:
				if (sval.equals("]")) {
					return;
				} else if (sval.equals("[")) {
					parseComment();
				}
				break;
			default:
				break;
			}
		}
	}

	public void parseEdgeInternals() throws IOException {
		int source = -1;
		int target = -1;

		while (true) {
			final int c = nextToken();
			switch (c) {
			case StreamTokenizer.TT_EOF:
				throw new IOException("Unexpected EOF");
			case StreamTokenizer.TT_WORD:
				if (sval.equals("]")) {
					return;
				} else if (sval.equals("source")) {
					nextToken();
					source = (int) nval;
				} else if (sval.equals("target")) {
					nextToken();
					target = (int) nval;
				} else if (sval.equals("graphics")) {
					nextToken();
					final JJNode sn = valToNode[source];
					final JJNode tn = valToNode[target];
					if ((source < 0) || (target < 0))
						throw new IOException("Unknown edge source or target");

					for (final Iterator<JJEdge> iter = sn.edgeIterator(); iter.hasNext();) {
						final JJEdge tmpE = iter.next();
						if ((tn == tmpE.opposite(sn)) && (edgeStatus[tmpE.getValue()] == undone)) {
							parseEdgeGraphics(tmpE);
							edgeStatus[tmpE.getValue()] = done;
						}
					}
				} else if (sval.equals("[")) {
					parseComment();
				} else {
					// Debug.println("Unknown edge internal: " + sval);
				}
				break;
			default:
				break;
			}
		}
	}
}
