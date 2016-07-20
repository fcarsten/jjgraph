/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.graph;

import java.awt.Color;
/**
 * JJGMLParser.java
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
import java.util.HashMap;

import org.carsten.jjgraph.util.Debug;
import org.carsten.jjgraph.util.JJDirector;
import org.carsten.jjgraph.util.JJEvent;
import org.carsten.jjgraph.util.JJPoint;

public class JJGMLParser extends StreamTokenizer implements JJGraphParser {
	static int UNDEF_MODE = 1;
	static int GRAPH_MODE = 1;
	static int NODE_MODE = 1;
	static int EDGE_MODE = 2;
	JJNode currentNode = null;
	JJEdge currentEdge = null;
	HashMap<Integer, JJNode> valToNode;
	JJGraph graph;
	// FastFileReader myReader;
	int updateCounter = 0;

	JJDirector director;

	public JJGMLParser(final Reader r) {
		super(r);
		// myReader = (Reader) r;

		resetSyntax();
		whitespaceChars('\u0000', '\u0020');
		parseNumbers();
		// eolIsSignificant(true);
		wordChars('A', 'Z');
		wordChars('a', 'z');
		wordChars('[', '[');
		wordChars(']', ']');
		wordChars('_', '_');
		commentChar('#');
		quoteChar('\"');
		valToNode = new HashMap<>();

	}

	@Override
	public boolean parse(final JJGraph theGraph) {
		graph = theGraph;
		director = (JJDirector) graph.getTool("JJDirector");
		if (director != null)
			Debug.println("Found director");

		try {
			while (true) {
				final int c = nextToken();
				switch (c) {
				case StreamTokenizer.TT_EOF:
					return true;
				case StreamTokenizer.TT_WORD:
					if (sval.equals("[")) {
						parseGraphInternals();
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
					parseNodeInternals(graph.addNode());
				} else if (sval.equals("edge")) {
					nextToken();
					parseEdgeInternals();
				} else if (sval.equals("[")) {
					parseComment();
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
					final JJWindowList fenster = graph.getWindows();
					for (final JJGraphWindow jjGraphWindow : fenster) {
						final JJGraphWindow window = jjGraphWindow;
						final JJGraphicNode gn = theNode.getGraphicNode(window);
						window.moveNodeTo(gn, x - w / 2, y - h / 2);
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
				} else if (sval.equals("fill")) {
					if (nextToken() == StreamTokenizer.TT_NUMBER) {
						final int cRed = (int) nval;
						nextToken();
						final int cGreen = (int) nval;
						nextToken();
						final int cBlue = (int) nval;
						final JJWindowList fenster = graph.getWindows();
						for (final Object element : fenster) {
							final JJGraphWindow window = (JJGraphWindow) element;
							final JJGraphicNode gn = theNode.getGraphicNode(window);
							gn.setColor(new Color(cRed, cGreen, cBlue));
						}
					} else { // Color in "0xJJJJJJ" format
						// Debug.println("Fill node with " + sval);
						final String col = sval;

						// Debug.println("Red :" + col.substring(1,3));
						// Debug.println("Green :" + col.substring(3,5));
						// Debug.println("Blue :" + col.substring(5,7));

						final int red = Integer.parseInt(col.substring(1, 3), 16);
						final int green = Integer.parseInt(col.substring(3, 5), 16);
						final int blue = Integer.parseInt(col.substring(5, 7), 16);
						final JJWindowList fenster = graph.getWindows();
						for (final Object element : fenster) {
							final JJGraphWindow window = (JJGraphWindow) element;
							final JJGraphicNode gn = theNode.getGraphicNode(window);
							gn.setColor(new Color(red, green, blue));
						}
					}
				} else if (sval.equals("[")) {
					parseComment();
				}
				break;
			default:
			}
		}
	}

	public void parseNodeEvents(final JJNode theNode) throws IOException {
		while (true) {
			final int c = nextToken();
			switch (c) {
			case StreamTokenizer.TT_EOF:
				throw new IOException("Unexpected EOF");
			case StreamTokenizer.TT_WORD:
				if (sval.equals("]")) {
					return;
				} else if (sval.equals("add")) {
					if (nextToken() == StreamTokenizer.TT_NUMBER) {
						final int time = (int) nval;
						if (director != null)
							director.addEvent(theNode, JJEvent.ADD, time);
					}
				} else if (sval.equals("remove")) {
					if (nextToken() == StreamTokenizer.TT_NUMBER) {
						final int time = (int) nval;
						if (director != null)
							director.addEvent(theNode, JJEvent.REMOVE, time);
					}
				} else if (sval.equals("only")) {
					if (nextToken() == StreamTokenizer.TT_NUMBER) {
						final int time = (int) nval;
						if (director != null) {
							director.addEvent(theNode, JJEvent.ADD, time);
							director.addEvent(theNode, JJEvent.REMOVE, time + 1);
						}
					}
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
					final JJWindowList fenster = graph.getWindows();
					for (final Object element : fenster) {
						final JJGraphWindow window = (JJGraphWindow) element;
						final JJGraphicEdge ge = theEdge.getGraphicEdge(window);
						ge.addBendLast(new JJPoint(x, y));
						return;
					}
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
				} else if (sval.equals("fill")) {
					if (nextToken() == StreamTokenizer.TT_NUMBER) {
						final int cRed = (int) nval;

						nextToken();
						final int cGreen = (int) nval;

						nextToken();
						final int cBlue = (int) nval;
						if (theEdge != null) {
							final JJWindowList fenster = graph.getWindows();
							for (final Object element : fenster) {
								final JJGraphWindow window = (JJGraphWindow) element;
								final JJGraphicEdge ge = theEdge.getGraphicEdge(window);
								ge.setColor(new Color(cRed, cGreen, cBlue));
							}
						}
					} else { // Color in "0xJJJJJJ" format
						final String col = sval;
						final int red = Integer.parseInt(col.substring(2, 3), 16);
						final int green = Integer.parseInt(col.substring(4, 5), 16);
						final int blue = Integer.parseInt(col.substring(6, 7), 16);
						if (theEdge != null) {
							final JJWindowList fenster = graph.getWindows();
							for (final Object element : fenster) {
								final JJGraphWindow window = (JJGraphWindow) element;
								final JJGraphicEdge ge = theEdge.getGraphicEdge(window);
								ge.setColor(new Color(red, green, blue));
							}
						}
					}
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

	public void parseNodeInternals(final JJNode theNode) throws IOException {
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
					theNode.setValue((int) nval);
					valToNode.put(new Integer((int) nval), theNode);
				} else if (sval.equals("label")) {
					nextToken();
					theNode.setName(new String(sval));
				} else if (sval.equals("graphics")) {
					nextToken();
					parseNodeGraphics(theNode);

				} else if (sval.equals("events")) {
					nextToken();
					parseNodeEvents(theNode);

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
		JJEdge theEdge = null;

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
					if ((source >= 0) && (target >= 0)) {
						final JJNode tmpS = valToNode.get(new Integer(source));
						final JJNode tmpT = valToNode.get(new Integer(target));
						if ((tmpS != null) && (tmpT != null)) {
							Debug.println("Parsing node internals");

							theEdge = graph.addEdge(tmpS, tmpT);
						}
					}
				} else if (sval.equals("target")) {
					nextToken();
					target = (int) nval;
					if ((source >= 0) && (target >= 0)) {
						final JJNode tmpS = valToNode.get(new Integer(source));
						final JJNode tmpT = valToNode.get(new Integer(target));
						if ((tmpS != null) && (tmpT != null)) {
							theEdge = graph.addEdge(tmpS, tmpT);
						}
					}
				} else if (sval.equals("graphics")) {
					nextToken();
					parseEdgeGraphics(theEdge);
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
