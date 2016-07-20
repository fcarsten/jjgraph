/**
 * Copyright 1999-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com)
 * Contributions by Falk Schreiber, Francois Bertault, Damian Merrick, and probably others
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten.jjgraph.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;

import org.carsten.jjgraph.graph.JJGraphWindowImpl;

class cfHour {
	String hour;
	String altHour;
}

class cfMin {
	String min;
	int offset;
}

public class JJClock implements ActionListener {
	private final JJGraphWindowImpl fenster;
	final static int DEUTSCH = 0;
	final static int BAYERISCH = 1;
	final static int ENGLISCH = 2;
	final static int SCHWEDISCH = 3;
	final static int SAECHSISCH = 4;
	final static int DAENISCH = 5;
	final static int FRANZOESISCH = 6;
	final static int SPANISCH = 7;

	private final cfHour stunde[] = new cfHour[13];
	private final cfMin minute[] = new cfMin[13];
	private final String rest[] = new String[4];
	private String uhr;
	private int language;
	private final javax.swing.Timer timer;

	public JJClock(final JJGraphWindowImpl f, final String l) {
		fenster = f;

		for (int i = 0; i < stunde.length; i++) {
			stunde[i] = new cfHour();
		}

		for (int i = 0; i < minute.length; i++) {
			minute[i] = new cfMin();
		}

		setLanguage(l);

		timer = new javax.swing.Timer(10 * 1000, this);
		timer.setInitialDelay(10 * 1000);
		timer.start();
	}

	public void stop() {
		timer.stop();
	}

	public void restart() {
		timer.restart();
	}

	public void setLanguage(final String l) {
		if (l.equals("deutsch"))
			initDeutsch();
		else if (l.equals("dansk"))
			initDaenisch();
		else if (l.equals("svenska"))
			initSchwedisch();
		else if (l.equals("espanol"))
			initSpanisch();
		else if (l.equals("english"))
			initEnglish();
		else if (l.equals("francais"))
			initFranz();
		else if (l.equals("boarisch"))
			initBayer();
		else if (l.equals("seggssch"))
			initSachs();
		else {
			Debug.println("Language " + l + " currently not supported.");
		}
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final Date now = new Date();
		final Calendar c = Calendar.getInstance();
		c.setTime(now);

		int min_i = c.get(Calendar.MINUTE);
		int hour_i = c.get(Calendar.HOUR);
		String other = "";
		String tmpUhr = "";
		String hour = "";

		if (min_i % 5 != 0)
			if (min_i % 5 < 3)
				other = rest[1];
			else
				other = rest[0];

		min_i = (min_i + 2) / 5;

		final String min = minute[min_i].min;
		final int offset = minute[min_i].offset;

		hour_i += offset;

		if (min_i == 0) {
			tmpUhr = uhr;
			hour = stunde[hour_i].altHour;
		} else {
			hour = stunde[hour_i].hour;
		}

		String zeit = "";

		if (language <= ENGLISCH)
			zeit = other + min + hour + tmpUhr;
		else if (language <= DAENISCH)
			zeit = other + min + tmpUhr + hour;
		else if (language <= SPANISCH)
			zeit = other + hour + min;

		fenster.setTime(zeit);
	}

	void initDeutsch() {
		language = DEUTSCH;

		stunde[0].altHour = stunde[0].hour = " zw�lf";
		stunde[1].altHour = " ein";
		stunde[1].hour = " eins";
		stunde[2].altHour = stunde[2].hour = " zwei";
		stunde[3].altHour = stunde[3].hour = " drei";
		stunde[4].altHour = stunde[4].hour = " vier";
		stunde[5].altHour = stunde[5].hour = " f�nf";
		stunde[6].altHour = stunde[6].hour = " sechs";
		stunde[7].altHour = stunde[7].hour = " sieben";
		stunde[8].altHour = stunde[8].hour = " acht";
		stunde[9].altHour = stunde[9].hour = " neun";
		stunde[10].altHour = stunde[10].hour = " zehn";
		stunde[11].altHour = stunde[11].hour = " elf";
		stunde[12].altHour = stunde[12].hour = " zw�lf";

		minute[0].min = "";
		minute[0].offset = 0;
		minute[1].min = " f�nf nach";
		minute[1].offset = 0;
		minute[2].min = " zehn nach";
		minute[2].offset = 0;
		minute[3].min = " viertel nach";
		minute[3].offset = 0;
		minute[4].min = " zehn vor halb";
		minute[4].offset = 1;
		minute[5].min = " f�nf vor halb";
		minute[5].offset = 1;
		minute[6].min = " halb";
		minute[6].offset = 1;
		minute[7].min = " f�nf nach halb";
		minute[7].offset = 1;
		minute[8].min = " zehn nach halb";
		minute[8].offset = 1;
		minute[9].min = " dreiviertel";
		minute[9].offset = 1;
		minute[10].min = " zehn vor";
		minute[10].offset = 1;
		minute[11].min = " f�nf vor";
		minute[11].offset = 1;
		minute[12].min = "";
		minute[12].offset = 1;

		rest[0] = " gleich";
		rest[1] = " kurz nach";
		uhr = " uhr";

		return;
	}

	void initSachs() {
		language = SAECHSISCH;

		stunde[0].altHour = stunde[0].hour = " zwelfe";
		stunde[1].altHour = stunde[1].hour = " eens";
		stunde[2].altHour = stunde[2].hour = " zweje";
		stunde[3].altHour = stunde[3].hour = " dreie";
		stunde[4].altHour = stunde[4].hour = " viere";
		stunde[5].altHour = stunde[5].hour = " f�mfe";
		stunde[6].altHour = stunde[6].hour = " seggse";
		stunde[7].altHour = stunde[7].hour = " siehme";
		stunde[8].altHour = stunde[8].hour = " achde";
		stunde[9].altHour = stunde[9].hour = " neune";
		stunde[10].altHour = stunde[10].hour = " zehne";
		stunde[11].altHour = stunde[11].hour = " elfe";
		stunde[12].altHour = stunde[12].hour = " zwelfe";

		minute[0].min = "";
		minute[0].offset = 0;
		minute[1].min = " f�nf nach um";
		minute[1].offset = 0;
		minute[2].min = " zehn nach um";
		minute[2].offset = 0;
		minute[3].min = " vierdel";
		minute[3].offset = 1;
		minute[4].min = " zehn vor halb";
		minute[4].offset = 1;
		minute[5].min = " f�nf vor halb";
		minute[5].offset = 1;
		minute[6].min = " halb";
		minute[6].offset = 1;
		minute[7].min = " f�nf nach halb";
		minute[7].offset = 1;
		minute[8].min = " zehn nach halb";
		minute[8].offset = 1;
		minute[9].min = " dreivierdel";
		minute[9].offset = 1;
		minute[10].min = " zehn vor um";
		minute[10].offset = 1;
		minute[11].min = " f�nf vor um";
		minute[11].offset = 1;
		minute[12].min = "";
		minute[12].offset = 1;

		rest[0] = " glei";
		rest[1] = " bissel nach";
		uhr = " um";

		return;
	}

	void initBayer() {
		language = BAYERISCH;

		stunde[0].altHour = stunde[0].hour = " zweife";
		stunde[1].altHour = stunde[1].hour = " oans";
		stunde[2].altHour = stunde[2].hour = " zwoa";
		stunde[3].altHour = stunde[3].hour = " drei";
		stunde[4].altHour = stunde[4].hour = " viere";
		stunde[5].altHour = stunde[5].hour = " fimfe";
		stunde[6].altHour = stunde[6].hour = " seckse";
		stunde[7].altHour = stunde[7].hour = " sieme";
		stunde[8].altHour = stunde[8].hour = " oachdde";
		stunde[9].altHour = stunde[9].hour = " neine";
		stunde[10].altHour = stunde[10].hour = " zehne";
		stunde[11].altHour = stunde[11].hour = " �ife";
		stunde[12].altHour = stunde[12].hour = " zweife";

		minute[0].min = "";
		minute[0].offset = 0;
		minute[1].min = " fimpf noch";
		minute[1].offset = 0;
		minute[2].min = " fimpf aaf viatl";
		minute[2].offset = 1;
		minute[3].min = " viatl";
		minute[3].offset = 1;
		minute[4].min = " fimpf noch viatl";
		minute[4].offset = 1;
		minute[5].min = " fimpf aaf holwe";
		minute[5].offset = 1;
		minute[6].min = " holwe";
		minute[6].offset = 1;
		minute[7].min = " fimpf noch holwe";
		minute[7].offset = 1;
		minute[8].min = " fimpf aaf dreiviatl";
		minute[8].offset = 1;
		minute[9].min = " dreiviatl";
		minute[9].offset = 1;
		minute[10].min = " fimpf noch dreiviatl";
		minute[10].offset = 1;
		minute[11].min = " fimpf aaf";
		minute[11].offset = 1;
		minute[12].min = "";
		minute[12].offset = 1;

		rest[0] = " a wengl aaf";
		rest[1] = " a wengl noch";
		uhr = ", grodaus";

		return;
	}

	void initDaenisch() {
		language = DAENISCH;

		stunde[0].altHour = stunde[0].hour = " tolv";
		stunde[1].altHour = stunde[1].hour = " �t";
		stunde[2].altHour = stunde[2].hour = " to";
		stunde[3].altHour = stunde[3].hour = " tre";
		stunde[4].altHour = stunde[4].hour = " fire";
		stunde[5].altHour = stunde[5].hour = " fem";
		stunde[6].altHour = stunde[6].hour = " seks";
		stunde[7].altHour = stunde[7].hour = " syv";
		stunde[8].altHour = stunde[8].hour = " otte";
		stunde[9].altHour = stunde[9].hour = " ni";
		stunde[10].altHour = stunde[10].hour = " ti";
		stunde[11].altHour = stunde[11].hour = " elleve";
		stunde[12].altHour = stunde[12].hour = " tolv";

		minute[0].min = "";
		minute[0].offset = 0;
		minute[1].min = " fem over";
		minute[1].offset = 0;
		minute[2].min = " ti over";
		minute[2].offset = 0;
		minute[3].min = " kvarter over";
		minute[3].offset = 0;
		minute[4].min = " tyve over";
		minute[4].offset = 0;
		minute[5].min = " fem i halv";
		minute[5].offset = 1;
		minute[6].min = " halv";
		minute[6].offset = 1;
		minute[7].min = " fem over halv";
		minute[7].offset = 1;
		minute[8].min = " tyve i";
		minute[8].offset = 1;
		minute[9].min = " kvarter i";
		minute[9].offset = 1;
		minute[10].min = " ti i";
		minute[10].offset = 1;
		minute[11].min = " fem i";
		minute[11].offset = 1;
		minute[12].min = "";
		minute[12].offset = 1;

		rest[0] = " lidt inden";
		rest[1] = " lidt efter";
		uhr = " klokken";

		return;
	}

	void initSchwedisch() {
		language = SCHWEDISCH;

		stunde[0].altHour = stunde[0].hour = " tolv";
		stunde[1].altHour = stunde[1].hour = " ett";
		stunde[2].altHour = stunde[2].hour = " tr�";
		stunde[3].altHour = stunde[3].hour = " tre";
		stunde[4].altHour = stunde[4].hour = " fyra";
		stunde[5].altHour = stunde[5].hour = " fem";
		stunde[6].altHour = stunde[6].hour = " sex";
		stunde[7].altHour = stunde[7].hour = " sju";
		stunde[8].altHour = stunde[8].hour = " otta";
		stunde[9].altHour = stunde[9].hour = " nio";
		stunde[10].altHour = stunde[10].hour = " tio";
		stunde[11].altHour = stunde[11].hour = " elva";
		stunde[12].altHour = stunde[12].hour = " tolv";

		minute[0].min = "";
		minute[0].offset = 0;
		minute[1].min = " fem �ver";
		minute[1].offset = 0;
		minute[2].min = " tio �ver";
		minute[2].offset = 0;
		minute[3].min = " kvart �ver";
		minute[3].offset = 0;
		minute[4].min = " tjugo �ver";
		minute[4].offset = 0;
		minute[5].min = " fem i halv";
		minute[5].offset = 1;
		minute[6].min = " halv";
		minute[6].offset = 1;
		minute[7].min = " fem �ver halv";
		minute[7].offset = 1;
		minute[8].min = " tjugo i";
		minute[8].offset = 1;
		minute[9].min = " kvart i";
		minute[9].offset = 1;
		minute[10].min = " tio i";
		minute[10].offset = 1;
		minute[11].min = " fem i";
		minute[11].offset = 1;
		minute[12].min = "";
		minute[12].offset = 1;

		rest[0] = " n�stan";
		rest[1] = " lite efter";
		uhr = " klockan";

		return;
	}

	void initFranz() {
		language = FRANZOESISCH;

		stunde[0].altHour = stunde[0].hour = " douce heures";
		stunde[1].altHour = stunde[1].hour = " une heures";
		stunde[2].altHour = stunde[2].hour = " deux heures";
		stunde[3].altHour = stunde[3].hour = " trois heures";
		stunde[4].altHour = stunde[4].hour = " quatre heures";
		stunde[5].altHour = stunde[5].hour = " cinq heures";
		stunde[6].altHour = stunde[6].hour = " six heures";
		stunde[7].altHour = stunde[7].hour = " sept heures";
		stunde[8].altHour = stunde[8].hour = " huit heures";
		stunde[9].altHour = stunde[9].hour = " neuf heures";
		stunde[10].altHour = stunde[10].hour = " dix heures";
		stunde[11].altHour = stunde[11].hour = " once heures";
		stunde[12].altHour = stunde[12].hour = " douce heures";

		minute[0].min = "";
		minute[0].offset = 0;
		minute[1].min = " cinq";
		minute[1].offset = 0;
		minute[2].min = " dix";
		minute[2].offset = 0;
		minute[3].min = " et quart";
		minute[3].offset = 0;
		minute[4].min = " vingt";
		minute[4].offset = 0;
		minute[5].min = " vingt-cinq";
		minute[5].offset = 0;
		minute[6].min = " et demi";
		minute[6].offset = 0;
		minute[7].min = " moins svingt-cinq";
		minute[7].offset = 1;
		minute[8].min = " moins vingt";
		minute[8].offset = 1;
		minute[9].min = " moins le quart";
		minute[9].offset = 1;
		minute[10].min = " moins dix";
		minute[10].offset = 1;
		minute[11].min = " moins cinq";
		minute[11].offset = 1;
		minute[12].min = "";
		minute[12].offset = 1;

		rest[0] = " presque";
		rest[1] = "";
		uhr = "";

		return;
	}

	void initSpanisch() {
		language = SPANISCH;

		stunde[0].altHour = stunde[0].hour = " las doce";
		stunde[1].altHour = stunde[1].hour = " la una";
		stunde[2].altHour = stunde[2].hour = " las dos";
		stunde[3].altHour = stunde[3].hour = " las tres";
		stunde[4].altHour = stunde[4].hour = " las cuatro";
		stunde[5].altHour = stunde[5].hour = " las cinco";
		stunde[6].altHour = stunde[6].hour = " las seis";
		stunde[7].altHour = stunde[7].hour = " las siete";
		stunde[8].altHour = stunde[8].hour = " las ocho";
		stunde[9].altHour = stunde[9].hour = " las nueve";
		stunde[10].altHour = stunde[10].hour = " las diez";
		stunde[11].altHour = stunde[11].hour = " las once";
		stunde[12].altHour = stunde[12].hour = " las doce";

		minute[0].min = "";
		minute[0].offset = 0;
		minute[1].min = " y cinco";
		minute[1].offset = 0;
		minute[2].min = " y diez";
		minute[2].offset = 0;
		minute[3].min = " y cuarto";
		minute[3].offset = 0;
		minute[4].min = " y veinte";
		minute[4].offset = 0;
		minute[5].min = " y veinticinco";
		minute[5].offset = 0;
		minute[6].min = " y media";
		minute[6].offset = 0;
		minute[7].min = " menos veinticinco";
		minute[7].offset = 1;
		minute[8].min = " menos veinte";
		minute[8].offset = 1;
		minute[9].min = " menos cuarto";
		minute[9].offset = 1;
		minute[10].min = " menos diez";
		minute[10].offset = 1;
		minute[11].min = " menos cinco";
		minute[11].offset = 1;
		minute[12].min = "";
		minute[12].offset = 1;

		rest[0] = " un poco antes de";
		rest[1] = " un poco despu�s de";
		uhr = "";

		return;
	}

	void initEnglish() {
		language = ENGLISCH;

		stunde[0].altHour = stunde[0].hour = " twelve";
		stunde[1].hour = stunde[1].altHour = " one";
		stunde[2].altHour = stunde[2].hour = " two";
		stunde[3].altHour = stunde[3].hour = " three";
		stunde[4].altHour = stunde[4].hour = " four";
		stunde[5].altHour = stunde[5].hour = " five";
		stunde[6].altHour = stunde[6].hour = " six";
		stunde[7].altHour = stunde[7].hour = " seven";
		stunde[8].altHour = stunde[8].hour = " eight";
		stunde[9].altHour = stunde[9].hour = " nine";
		stunde[10].altHour = stunde[10].hour = " ten";
		stunde[11].altHour = stunde[11].hour = " eleven";
		stunde[12].altHour = stunde[12].hour = " twelve";

		minute[0].min = "";
		minute[0].offset = 0;
		minute[1].min = " five past";
		minute[1].offset = 0;
		minute[2].min = " ten past";
		minute[2].offset = 0;
		minute[3].min = " a quarter past";
		minute[3].offset = 0;
		minute[4].min = " twenty minutes past";
		minute[4].offset = 0;
		minute[5].min = " five minutes to half past";
		minute[5].offset = 0;
		minute[6].min = " half past";
		minute[6].offset = 0;
		minute[7].min = " five minutes after half past";
		minute[7].offset = 0;
		minute[8].min = " ten minutes after half past";
		minute[8].offset = 0;
		minute[9].min = " a quarter to";
		minute[9].offset = 1;
		minute[10].min = " ten minutes to";
		minute[10].offset = 1;
		minute[11].min = " five minutes to";
		minute[11].offset = 1;
		minute[12].min = "";
		minute[12].offset = 1;

		rest[0] = " nearly";
		rest[1] = " a little bit past";
		uhr = " o'clock";

		return;
	}

} // JJClock
