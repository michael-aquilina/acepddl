/* Generated By:JavaCC: Do not edit this line. PDDL21ParserConstants.java */
package edu.asu.sapa.parsing;

public interface PDDL21ParserConstants {

	int EOF = 0;
	int SINGLE_LINE_COMMENT = 6;
	int DEFINE = 7;
	int DOMAIN = 8;
	int FUNCTION = 9;
	int REQUIREMENTS = 10;
	int TYPES = 11;
	int CONSTANTS = 12;
	int PREDICATES = 13;
	int ACTION = 14;
	int DURATIVE_ACTION = 15;
	int VARS = 16;
	int PARAMETERS = 17;
	int COST = 18;
	int REQUIREMENT = 19;
	int CONDITION = 20;
	int PRECONDITION = 21;
	int EFFECT = 22;
	int ASSIGN = 23;
	int SCALEUP = 24;
	int SCALEDOWN = 25;
	int INCREASE = 26;
	int DECREASE = 27;
	int DURATION = 28;
	int DURATION_VAR = 29;
	int AT = 30;
	int START = 31;
	int END = 32;
	int OVER = 33;
	int ALL = 34;
	int LOCALTIME = 35;
	int AND = 36;
	int OR = 37;
	int NOT = 38;
	int FORALL = 39;
	int WHEN = 40;
	int EXISTS = 41;
	int EITHER = 42;
	int PROBLEM = 43;
	int DOMAIN_TAG = 44;
	int OBJECT = 45;
	int GOAL = 46;
	int INIT = 47;
	int METRIC = 48;
	int MAXIMIZE = 49;
	int MINIMIZE = 50;
	int LENGTH = 51;
	int SERIAL = 52;
	int PARALLEL = 53;
	int A_NUMBER = 54;
	int ASSIGN_MATH = 55;
	int EQUAL = 56;
	int PLUS = 57;
	int MINUS = 58;
	int MUL = 59;
	int DIVIDE = 60;
	int COMPARISON = 61;
	int VAR = 62;
	int NAME = 63;
	int OPENBRACE = 64;
	int CLOSEBRACE = 65;

	int DEFAULT = 0;

	String[] tokenImage = { "<EOF>", "\" \"", "\"\\t\"", "\"\\n\"", "\"\\r\"", "\"\\f\"", "<SINGLE_LINE_COMMENT>",
			"\"define\"", "\"domain\"", "\":functions\"", "\":requirements\"", "\":types\"", "\":constants\"",
			"\":predicates\"", "\":action\"", "\":durative-action\"", "\":vars\"", "\":parameters\"", "\":cost\"",
			"<REQUIREMENT>", "\":condition\"", "\":precondition\"", "\":effect\"", "\"assign\"", "\"scale-up\"",
			"\"scale-down\"", "\"increase\"", "\"decrease\"", "\":duration\"", "\"?duration\"", "\"at\"", "\"start\"",
			"\"end\"", "\"over\"", "\"all\"", "\"#t\"", "\"and\"", "\"or\"", "\"not\"", "\"forall\"", "\"when\"",
			"\"exists\"", "\"either\"", "\"problem\"", "\":domain\"", "\":objects\"", "\":goal\"", "\":init\"",
			"\":metric\"", "\"maximize\"", "\"minimize\"", "\":length\"", "\":serial\"", "\":parallel\"", "<A_NUMBER>",
			"<ASSIGN_MATH>", "\"=\"", "\"+\"", "\"-\"", "\"*\"", "\"/\"", "<COMPARISON>", "<VAR>", "<NAME>", "\"(\"",
			"\")\"", };

}
