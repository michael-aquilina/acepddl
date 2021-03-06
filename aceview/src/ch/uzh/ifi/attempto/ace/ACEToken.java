package ch.uzh.ifi.attempto.ace;

import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;

import ch.uzh.ifi.attempto.ape.FunctionWords;

/**
 * <p>ACE token and its features.</p>
 * 
 * @author Kaarel Kaljurand
 */
public final class ACEToken {

	public static final ACEToken DOT = makeDot();

	// Some function words (in lowercase) additionally to those provided by Attempto Java Packages.
	// TODO: This list is probably not complete.
	// TODO: Some of these words could maybe be moved to AJP.
	private static final ImmutableSet ADDITIONAL_FUNCTIONWORDS =
		ImmutableSet.of("s", "at", "less", "least", "exactly", "thing", "things", "false", "does", "do", "he/she");

	private static final Pattern variablePattern = Pattern.compile("[A-Z][0-9]*");
	private static final Pattern wordPattern = Pattern.compile("[a-zA-Z$_-][a-zA-Z0-9$_-]*");

	// The token itself
	private String token;

	private boolean isBadToken = false;
	private boolean isBorderToken = false;
	private boolean isButToken = false;
	private boolean isQuestionMark = false;
	private boolean isNumber = false;
	private boolean isOrdinationWord = false;
	private boolean isSymbol = false;
	private boolean isQuotedString = false;
	private boolean isApos = false;
	private boolean isVariable = false;
	private boolean isFunctionWord = false;
	private boolean needsQuoting = false;

	private ACEToken() {}


	public static ACEToken newToken(String token) {
		ACEToken newToken = new ACEToken();
		newToken.token = token;

		String tokenLC = token.toLowerCase();

		if (tokenLC.equals("and")
				|| tokenLC.equals("or")
				|| tokenLC.equals("if")
				|| tokenLC.equals("then")) {
			newToken.isOrdinationWord = true;
			newToken.isFunctionWord = true;
		}
		else if (tokenLC.equals("but")) {
			newToken.isButToken = true;
			newToken.isFunctionWord = true;
		}
		else if (variablePattern.matcher(token).matches()) {
			newToken.isVariable = true;
			newToken.isFunctionWord = true;
		}
		else if (checkIsFunctionWord(tokenLC)) {
			newToken.isFunctionWord = true;
		}

		if (! wordPattern.matcher(token).matches()) {
			newToken.needsQuoting = true;
		}
		return newToken;
	}


	public static ACEToken newNumber(double number) {
		ACEToken newToken = new ACEToken();
		if (number == (int) number) {
			newToken.token = Integer.toString((int) number);
		}
		else {
			newToken.token = Double.toString(number);
		}
		newToken.isNumber = true;
		newToken.isFunctionWord = true;
		return newToken;
	}


	public static ACEToken newQuotedString(String str) {
		ACEToken newToken = new ACEToken();
		newToken.token = "\"" + str + "\"";
		newToken.isQuotedString = true;
		newToken.isFunctionWord = true;
		return newToken;
	}


	public static ACEToken newSymbol(char ch) {
		ACEToken newToken = new ACEToken();
		newToken.token = String.valueOf(ch);
		newToken.isSymbol = true;
		if (newToken.token.equals("'")) {
			newToken.isApos = true;
		}
		newToken.isFunctionWord = true;
		return newToken;
	}

	public static ACEToken newBorderToken(char ch) {
		ACEToken newToken = new ACEToken();
		newToken.token = String.valueOf(ch);
		newToken.isBorderToken = true;
		newToken.isSymbol = true;
		if (newToken.token.equals("?")) {
			newToken.isQuestionMark = true;			
		}
		newToken.isFunctionWord = true;
		return newToken;
	}


	public static ACEToken newBadToken(char ch) {
		ACEToken newToken = new ACEToken();
		newToken.token = String.valueOf(ch);
		newToken.isBadToken = true;
		newToken.isFunctionWord = true;
		return newToken;
	}


	public boolean isQuotedString() {
		return isQuotedString;
	}

	public boolean isBadToken() {
		return isBadToken;
	}

	public boolean isButToken() {
		return isButToken;
	}

	public boolean isNumber() {
		return isNumber;
	}

	public boolean isSymbol() {
		return isSymbol;
	}

	public boolean isBorderToken() {
		return isBorderToken;
	}

	public boolean isQuestionMark() {
		return isQuestionMark;
	}

	public boolean isOrdinationWord() {
		return isOrdinationWord;
	}

	public boolean isVariable() {
		return isVariable;
	}

	public boolean isApos() {
		return isApos;
	}

	public boolean isFunctionWord() {
		return isFunctionWord;
	}

	public boolean isContentWord() {
		return (! isFunctionWord());
	}


	public String getToken() {
		return token;
	}


	@Override
	public String toString() {
		if (needsQuoting) {
			return "`" + token + "`";
		}
		return token;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if ((obj == null) || (obj.getClass() != this.getClass())) return false;
		ACEToken t = (ACEToken) obj;
		return token.equals(t.getToken());
	}


	@Override
	public int hashCode() {
		return token.hashCode();
	}


	private static ACEToken makeDot() {
		ACEToken newToken = new ACEToken();
		newToken.token = String.valueOf('.');
		newToken.isBorderToken = true;
		newToken.isSymbol = true;
		newToken.isFunctionWord = true;
		return newToken;
	}


	private static boolean checkIsFunctionWord(String tokenLC) {
		if (FunctionWords.isFunctionWord(tokenLC) || ADDITIONAL_FUNCTIONWORDS.contains(tokenLC)) {
			return true;
		}

		return false;
	}
}