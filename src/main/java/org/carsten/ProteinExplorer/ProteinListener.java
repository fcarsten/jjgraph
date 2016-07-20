/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.ProteinExplorer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
/**
 * ProteinListener.java
 *
 *
 * Created: Wed Dec 04 10:55:22 2002
 *
 * @author <a href="mailto:carsten@it.usyd.edu.au"></a>
 * @version
 */
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.carsten.jjgraph.graph.JJEdge;
import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJNode;
import org.carsten.jjgraph.util.Debug;

public class ProteinListener implements Runnable {
	ProteinWindow window = null;

	public ProteinListener(final ProteinWindow w) {
		window = w;
	}

	public String readInput(final BufferedReader in) throws IOException {
		Debug.println("Reading input");

		String lengthS = "";
		int c;
		while (true) {
			c = in.read();
			if (c == -1)
				throw new IOException("Unexpected end of stream");
			if (c == ':')
				break;
			lengthS += (char) c;
			// Debug.println(lengthS);
		}

		if (lengthS.length() == 0)
			throw new IOException("Header corrupt");

		final int length = Integer.parseInt(lengthS);

		final char buffer[] = new char[length];

		int numRead = 0;
		String resS = "";// new String(buffer);

		while (numRead < length) {
			final int res = in.read(buffer, 0, length);
			resS += new String(buffer);
			numRead += res;
		}

		if (numRead > length)
			Debug.println("Read " + numRead + " instead of expected " + length + " characters");
		return resS;
	}

	public void sendOutput(final PrintWriter out, final String msg) {
		out.print("" + msg.length() + ":" + msg);
		out.flush();
	}

	@Override
	public void run() {
		try (ServerSocket serverSocket = new ServerSocket(4567)) {

			while (true) {
				Socket clientSocket = null;
				try {
					final JJProteinInspector insp = window.getProteinInspector();
					clientSocket = serverSocket.accept();
					try {
						final PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
						final BufferedReader in = new BufferedReader(
								new InputStreamReader(clientSocket.getInputStream()));
						try {
							final String msg = "Welcome to Protein Explorer";

							sendOutput(out, msg);

							String query = readInput(in);
							query = query.toUpperCase();

							Debug.println("Received: " + query);
							if (query.equals("GET LIST")) {
								String res = "";
								if (insp != null) {
									final Collection<String> ac = insp.getActionSet();
									for (final Object element : ac) {
										final String tmpS = (String) element;
										if (!tmpS.equals(""))
											res += tmpS + "\n";
									}
								}
								Debug.println("Sending:\n" + res);
								sendOutput(out, res);
							} else if (query.startsWith("GET LIST ")) {
								final String nodeName = query.substring(9);
								String response = "";
								Debug.println("Looking for node with name \"" + nodeName + "\"");

								final Collection<String> ac = new HashSet<>();
								for (final Iterator<JJNode> iter = window.getGraph().nodeIterator(); iter.hasNext();) {
									final JJNode node = iter.next();
									if (node.getName().compareToIgnoreCase(nodeName) == 0) {
										for (final Iterator<JJEdge> edgeIter = node.edgeIterator(); edgeIter
												.hasNext();) {
											final JJEdge edge = edgeIter.next();
											ac.add(edge.getName());
										}
										break;
									}
								}
								for (final Object element : ac) {
									final String tmpS = (String) element;
									if (!tmpS.equals("")) {
										response += tmpS + "\n";
									}
								}

								Debug.println("Sending:\n\"" + response + "\"");
								sendOutput(out, response);
							} else if (query.startsWith("GET SUBGRAPH ")) {
								final String interaction = query.substring(13);
								final HashSet<JJNode> nodes = new HashSet<>();
								final HashSet<JJEdge> edges = new HashSet<>();
								final JJGraph graph = window.getGraph();
								for (final Iterator<JJEdge> iter = graph.edgeIterator(); iter.hasNext();) {
									final JJEdge tmpE = iter.next();
									if (tmpE.getName().compareToIgnoreCase(interaction) == 0) {
										edges.add(tmpE);
										nodes.add(tmpE.getSource());
										nodes.add(tmpE.getTarget());
									}
								}
								final String res = graph.saveGMLToString(window, nodes, edges);
								Debug.println("Sending:\n" + res);
								sendOutput(out, res);
							} else if (query.startsWith("SELECT ")) {
								final String interaction = query.substring(7);
								Debug.println("Selecting ppi: " + interaction);

								if (insp != null) {
									final boolean tmpB = window.setRedraw(false);
									insp.selectInteraction(interaction);
									window.setRedraw(tmpB);
								}

							}
						} catch (final IOException e) {
							Debug.println("Network communication failed: " + e.getMessage());
						}

						out.close();
						in.close();
					} catch (final IOException e) {
						Debug.println("Network stream creation failed: " + e.getMessage());
					}

					try {
						clientSocket.close();
					} catch (final IOException e) {
						Debug.println("Could not close client socket");
					}
				} catch (final IOException e) {
					Debug.println("Accept failed: 4567");
				}
			}
		} catch (final IOException e) {
			Debug.println("Could not listen on port: 4567");
			return;
		}

		/*
		 * try{ serverSocket.close(); } catch(IOException e){
		 * Debug.println("Could not close server socket"); return; }
		 */
	}

}// ProteinListener
