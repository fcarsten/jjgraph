/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

import java.awt.Color;
/**
 * JJHTMLManager.java
 *
 *
 * Created: Fri Feb 26 13:37:31 1999
 *
 * @author Carsten Friedrich
 * @version
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.JOptionPane;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.ElementIterator;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

import org.carsten.jjgraph.graph.JJGraph;
import org.carsten.jjgraph.graph.JJGraphWindow;
import org.carsten.jjgraph.graph.JJGraphicNode;
import org.carsten.jjgraph.graph.JJNode;

public class JJHTMLManager {
	JJNode knoten;
	JJNode knotenHost;

	JJGraphWindow window;

	HashMap<String, Integer> hostMap;
	int hostCounter;

	HashMap<URL, JJNode> urlMap;
	HashMap<Integer, Color> colorMap;
	HashMap<Integer, JJNode> clusterToNode;

	public JJHTMLManager(final JJGraphWindow g) {
		hostMap = new HashMap<>();
		urlMap = new HashMap<>();
		colorMap = new HashMap<>();
		clusterToNode = new HashMap<>();
		window = g;
	}

	private JJGraphWindow getOtherWindow() {
		return window.getFrame().getClusterHandler().getOtherWindow();
	}

	public URL adjustUrl(final URL url) {
		try {
			final URL newUrl = new URL(url, url.getFile() + "/");
			// Debug.println("Trying " + newUrl.toString());

			try {
				final HttpURLConnection con = (HttpURLConnection) newUrl.openConnection();
				// con.disconnect();
				final InputStream os = con.getInputStream();
				final int code = con.getResponseCode();
				con.disconnect();

				if ((code >= 200) && (code < 300)) {
					// Debug.println("Adding / to url");
					return newUrl;
				}
			} catch (final java.lang.ClassCastException e) {
				return url;
			} catch (final IOException e) {
				window.printError(e.getMessage());
			}
		} catch (final MalformedURLException e) {
			window.printError(e.getMessage());
		}
		return url;
	}

	public JJNode addNode(URL url) {
		// Debug.println("Adding url " + url.toString() + " .... ");

		if (!url.getFile().endsWith("/"))
			url = adjustUrl(url);

		JJNode newN = urlMap.get(url);

		if (newN != null) {
			// Debug.println("done");
			return newN;
		}

		newN = window.getGraph().addNode();
		final JJGraphicNode newG = newN.getGraphicNode(window);

		urlMap.put(url, newN);
		newN.setUrl(url);

		String name = url.getFile();

		if (name.equals("/"))
			name = url.getHost();

		if (name.endsWith("/"))
			name = name.substring(0, name.length() - 1);
		else if (name.endsWith(".html"))
			name = name.substring(0, name.length() - 5);
		else if (name.endsWith(".htm"))
			name = name.substring(0, name.length() - 4);
		else if (name.endsWith(".shtml"))
			name = name.substring(0, name.length() - 6);

		if (url.getRef() != null)
			name += "#" + url.getRef();

		newN.setName(name);

		String theHost = url.getHost();
		if (theHost.equals(""))
			theHost = url.getFile();

		final Integer cluster = hostMap.get(theHost);

		if (cluster == null) {
			hostCounter++;
			hostMap.put(theHost, new Integer(hostCounter));
			final Color newColor = new Color((float) (Math.random() / 2.0 + 0.5), (float) (Math.random() / 2.0 + 0.5),
					(float) (Math.random() / 2.0 + 0.5));
			colorMap.put(new Integer(hostCounter), newColor);

			if ((window != null) && (getOtherWindow() != null)) {
				final JJNode tmpN = getOtherWindow().getGraph().addNode();
				tmpN.setName(theHost);

				// if(url.getHost().equals(""))
				// tmpN.setName(url.getFile().substring(url.getFile().indexOf('@')));

				tmpN.getGraphicNode(getOtherWindow()).setColor(newColor);
				tmpN.setCluster(hostCounter);
				clusterToNode.put(new Integer(hostCounter), tmpN);

			}

			newN.setCluster(hostCounter);
			newG.setColor(newColor);
			// graph.getWindow().showCluster(hostCounter);

		} else {
			newN.setCluster(cluster.intValue());
			newG.setColor(colorMap.get(cluster));
		}

		// if(newN.getGraphicNode() != null)
		// graph.getWindow().repaint(newN.getGraphicNode().getBounds());
		final JJGraphicNode gn = newN.getGraphicNode(window);

		if (gn != null)
			window.repaint(gn);

		// Debug.println("done");
		return newN;
	}

	public void newHref(final String urlStr, final URL contextUrl) {
		try {
			final URL url = new URL(contextUrl, urlStr);

			final JJNode tmpN = addNode(url);
			final JJGraphicNode kn = knoten.getGraphicNode(window);

			if (kn != null)
				window.moveNodeTo(tmpN.getGraphicNode(window), kn.getCoords());

			knoten.getGraph().addEdge(knoten, tmpN);

			if ((knoten.getCluster() != tmpN.getCluster()) && (getOtherWindow() != null)) {
				final JJGraph otherGraph = getOtherWindow().getGraph();

				otherGraph.addEdge(knotenHost, clusterToNode.get(new Integer(tmpN.getCluster())));
			}

		} catch (final MalformedURLException e) {
			window.printError(e.getMessage());
		}
	}

	public void expandNode(final JJNode n) {
		knoten = n;
		knotenHost = clusterToNode.get(new Integer(knoten.getCluster()));

		Debug.println("Expanding " + knoten.getUrl().toString());

		final EditorKit kit = new HTMLEditorKit();
		final Document doc = kit.createDefaultDocument();

		// The Document class does not yet
		// handle charset's properly.
		doc.putProperty("IgnoreCharsetDirective", Boolean.TRUE);
		try {

			// Create a reader on the HTML content.
			if (knoten.getUrl() == null)
				return;

			final Reader rd = getReader(knoten.getUrl());

			// Parse the HTML.
			kit.read(rd, doc, 0);

			// Iterate through the elements
			// of the HTML document.
			final ElementIterator it = new ElementIterator(doc);
			javax.swing.text.Element elem;
			while ((elem = it.next()) != null) {
				final SimpleAttributeSet s = (SimpleAttributeSet) elem.getAttributes().getAttribute(HTML.Tag.A);
				if ((s != null) && (s.getAttribute(HTML.Attribute.HREF) != null)) {
					newHref(s.getAttribute(HTML.Attribute.HREF).toString(), knoten.getUrl());
				}
			}
		} catch (final java.io.IOException e) {
			window.printError("IOException: " + e.getMessage());
			// e.printStackTrace();
		} catch (final javax.swing.text.BadLocationException e) {
			window.printError("Bad Location Exception: " + e.getMessage());
			// e.printStackTrace();
		}
	}

	static String auth = null;
	static boolean proxySet = false;

	static void setProxy() {
		proxySet = true;
		String proxy = JOptionPane.showInputDialog(null, "Are you using a proxy (name:port)?", "IOException",
				JOptionPane.QUESTION_MESSAGE);

		if (proxy != null) {
			proxy = proxy.trim();
			final int index = proxy.indexOf(':');
			if (index == -1)
				return;

			final String host = proxy.substring(0, index);
			final String port = proxy.substring(index + 1, proxy.length());

			System.getProperties().put("proxySet", "true");
			System.getProperties().put("proxyHost", host);
			System.getProperties().put("proxyPort", port);

			// Microsoft VM
			// propSystem.put("firewallSet", "true");
			// propSystem.put("firewallHost", "myProxyServer.com");
			// propSystem.put("firewallPort", "80");
			// propSystem.put("http.proxyHost", "myProxyServer.com");
			// propSystem.put("http.proxyPort", "80");

			final String namePass = JOptionPane.showInputDialog(null, "Do you have to login (name:password)? ",
					"Authentification", JOptionPane.QUESTION_MESSAGE);

			if (namePass != null) {
				auth = Base64.getEncoder().encodeToString(namePass.getBytes());
			}
		}
	}

	public void disableCertificateCheck() {
		// Create a trust manager that does not validate certificate chains
		final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(final java.security.cert.X509Certificate[] certs, final String authType) {
			}

			@Override
			public void checkServerTrusted(final java.security.cert.X509Certificate[] certs, final String authType) {
			}
		} };

		// Install the all-trusting trust manager
		try {
			final SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (final Exception e) {
		}
	}

	static Reader getReader(final URL url) throws IOException {

		System.setProperty("javax.net.ssl.trustStore", "c:\\Documents and settings\\carsten\\.keystore");
		InputStreamReader r = null;
		try {
			URLConnection conn = url.openConnection();
			if (conn instanceof HttpURLConnection) {
				HttpURLConnection httpCon = (HttpURLConnection) conn;

				if (auth != null) {
					conn.setRequestProperty("Proxy-Authorization", auth);
				}

				final int code = httpCon.getResponseCode();
				if (code == 401) {
					httpCon.disconnect();
					conn = url.openConnection();
					httpCon = (HttpURLConnection) conn;

					final JJAuthenticate auth = new JJAuthenticate();

					final String userName = auth.getUserName();
					final String password = auth.getPassword();
					if ((userName == null) || (password == null))
						throw new IOException("Can't access password proteced site");

					conn.setRequestProperty("Authorization", "Basic "
							+ Base64.getEncoder().encodeToString((userName.trim() + ":" + password.trim()).getBytes()));
				}
			}

			r = new InputStreamReader(conn.getInputStream());
		} catch (final IOException e) {
			if (!proxySet) {
				setProxy();
				return getReader(url);
			} else
				throw e;
		}

		return r;
	}
} // JJHTMLManager
