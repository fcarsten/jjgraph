/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
/* Generated By:JavaCC: Do not edit this line. MatrixMarketParser.java */
package org.carsten.jjgraph.graph;

import org.carsten.jjgraph.util.Debug;

public class MatrixMarketParser implements JJGraphParser, MatrixMarketParserConstants {
	JJNode nodeArray[];
	JJGraph graph;
	JJWindowList fenster;

	@Override
	public boolean parse(final JJGraph g) {
		graph = g;
		fenster = g.getWindows();

		try {
			parse();
		} catch (final ParseException e) {
			fenster.printError("Parse error: " + e.getMessage());
			return false;
		}

		return true;
	}

	static String trimString(final String s) {
		return s.substring(1, s.length() - 1);
	}

	final public void parse() throws ParseException, ParseException {
		Debug.println("Parsing matrix market format");
		firstLine();
		size();
		label_1: while (true) {
			entry();
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case NUM:
				;
				break;
			default:
				jj_la1[0] = jj_gen;
				break label_1;
			}
		}
		jj_consume_token(0);
	}

	final public void firstLine() throws ParseException, ParseException {
		Token kind, format, type, symmetry;
		jj_consume_token(FIRSTLINE);
		kind = jj_consume_token(WORD);
		format = jj_consume_token(WORD);
		type = jj_consume_token(WORD);
		symmetry = jj_consume_token(WORD);
		if (!(format.image.equals("coordinate") && type.image.equals("real") && kind.image.equals("matrix"))) {
			{
				if (true)
					throw new ParseException("Parse error: unsupported sub-format " + format + " " + type);
			}
		}
		if (symmetry.image.equals("general")) {
			graph.setDirected(true);
		} else {
			graph.setDirected(false);
		}
	}

	final public void entry() throws ParseException {
		Token xs, ys, ws;
		xs = jj_consume_token(NUM);
		ys = jj_consume_token(NUM);
		ws = jj_consume_token(FLOAT);
		final int x = Integer.parseInt(xs.image) - 1;
		final int y = Integer.parseInt(ys.image) - 1;
		final double w = Double.parseDouble(ws.image);
		final JJEdge e = graph.addEdge(nodeArray[x], nodeArray[y]);
		e.setWeight(w);
	}

	final public void size() throws ParseException, ParseException {
		Token xs, ys, es;
		xs = jj_consume_token(NUM);
		ys = jj_consume_token(NUM);
		es = jj_consume_token(NUM);
		final int x = Integer.parseInt(xs.image);
		final int y = Integer.parseInt(ys.image);
		final int e = Integer.parseInt(es.image);
		if (x != y) {
			{
				if (true)
					throw new ParseException("Matrix not square");
			}
		}
		Debug.println("Creating " + x + " nodes");
		nodeArray = new JJNode[x];
		for (int i = 0; i < x; i++) {
			nodeArray[i] = graph.addNode();
		}
	}

	public MatrixMarketParserTokenManager token_source;
	SimpleCharStream jj_input_stream;
	public Token token, jj_nt;
	private int jj_ntk;
	private int jj_gen;
	final private int[] jj_la1 = new int[1];
	final private int[] jj_la1_0 = { 0x40, };

	public MatrixMarketParser(final java.io.InputStream stream) {
		jj_input_stream = new SimpleCharStream(stream, 1, 1);
		token_source = new MatrixMarketParserTokenManager(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 1; i++)
			jj_la1[i] = -1;
	}

	public void ReInit(final java.io.InputStream stream) {
		jj_input_stream.ReInit(stream, 1, 1);
		token_source.ReInit(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 1; i++)
			jj_la1[i] = -1;
	}

	public MatrixMarketParser(final java.io.Reader stream) {
		jj_input_stream = new SimpleCharStream(stream, 1, 1);
		token_source = new MatrixMarketParserTokenManager(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 1; i++)
			jj_la1[i] = -1;
	}

	public void ReInit(final java.io.Reader stream) {
		jj_input_stream.ReInit(stream, 1, 1);
		token_source.ReInit(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 1; i++)
			jj_la1[i] = -1;
	}

	public MatrixMarketParser(final MatrixMarketParserTokenManager tm) {
		token_source = tm;
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 1; i++)
			jj_la1[i] = -1;
	}

	public void ReInit(final MatrixMarketParserTokenManager tm) {
		token_source = tm;
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 1; i++)
			jj_la1[i] = -1;
	}

	final private Token jj_consume_token(final int kind) throws ParseException {
		Token oldToken;
		if ((oldToken = token).next != null)
			token = token.next;
		else
			token = token.next = token_source.getNextToken();
		jj_ntk = -1;
		if (token.kind == kind) {
			jj_gen++;
			return token;
		}
		token = oldToken;
		jj_kind = kind;
		throw generateParseException();
	}

	final public Token getNextToken() {
		if (token.next != null)
			token = token.next;
		else
			token = token.next = token_source.getNextToken();
		jj_ntk = -1;
		jj_gen++;
		return token;
	}

	final public Token getToken(final int index) {
		Token t = token;
		for (int i = 0; i < index; i++) {
			if (t.next != null)
				t = t.next;
			else
				t = t.next = token_source.getNextToken();
		}
		return t;
	}

	final private int jj_ntk() {
		if ((jj_nt = token.next) == null)
			return (jj_ntk = (token.next = token_source.getNextToken()).kind);
		else
			return (jj_ntk = jj_nt.kind);
	}

	private final java.util.Vector<int[]> jj_expentries = new java.util.Vector<>();
	private int[] jj_expentry;
	private int jj_kind = -1;

	final public ParseException generateParseException() {
		jj_expentries.removeAllElements();
		final boolean[] la1tokens = new boolean[10];
		for (int i = 0; i < 10; i++) {
			la1tokens[i] = false;
		}
		if (jj_kind >= 0) {
			la1tokens[jj_kind] = true;
			jj_kind = -1;
		}
		for (int i = 0; i < 1; i++) {
			if (jj_la1[i] == jj_gen) {
				for (int j = 0; j < 32; j++) {
					if ((jj_la1_0[i] & (1 << j)) != 0) {
						la1tokens[j] = true;
					}
				}
			}
		}
		for (int i = 0; i < 10; i++) {
			if (la1tokens[i]) {
				jj_expentry = new int[1];
				jj_expentry[0] = i;
				jj_expentries.addElement(jj_expentry);
			}
		}
		final int[][] exptokseq = new int[jj_expentries.size()][];
		for (int i = 0; i < jj_expentries.size(); i++) {
			exptokseq[i] = jj_expentries.elementAt(i);
		}
		return new ParseException(token, exptokseq, tokenImage);
	}

	final public void enable_tracing() {
	}

	final public void disable_tracing() {
	}

}