/*
 * Copyright 2017 Albert Tregnaghi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 */
package de.jcup.basheditor.script.parser;

import java.util.ArrayList;
import java.util.List;

class ParseContext {

	char[] chars;
	int pos;
	StringBuilder sb;
	List<ParseToken> tokens = new ArrayList<ParseToken>();
	private ParseToken currentToken;
	private ParserState parserState = ParserState.INIT;
	private ParserState stateBeforeString;
	private VariableContext variableContext;

	ParseContext() {
		currentToken = createToken();
	}

	public class VariableContext {
		private VariableState variableState = VariableState.NO_ARRAY;
		private int variableOpenCurlyBraces;
		private int variableCloseCurlyBraces;

		public void incrementVariableOpenCurlyBraces() {
			variableOpenCurlyBraces++;

		}

		public void incrementVariableCloseCurlyBraces() {
			variableCloseCurlyBraces++;

		}

		public boolean areVariableCurlyBracesBalanced() {
			return variableOpenCurlyBraces == variableCloseCurlyBraces;
		}

		public void variableArrayOpened() {
			variableState = VariableState.ARRAY_OPENED;
		}

		public void variableArrayClosed() {
			variableState = VariableState.ARRAY_CLOSED;
		}

		public boolean isInsideVariableArray() {
			boolean isInside = inState(ParserState.VARIABLE);
			isInside = isInside && VariableState.ARRAY_OPENED.equals(variableState);
			return isInside;
		}
	}

	void addTokenAndResetText() {
		if (moveCurrentPosWhenEmptyText()) {
			return;
		}

		currentToken.text = sb.toString();
		currentToken.end = pos;
		tokens.add(currentToken);

		/* new token on next position */
		currentToken = createToken();
		currentToken.start = pos + 1;

		resetText();
	}

	void appendCharToText() {
		getSb().append(getCharAtPos());
	}

	char getCharAtPos() {
		return chars[pos];
	}

	char getCharBefore() {
		int posBefore = pos - 1;
		if (posBefore >= 0) {
			if (chars.length > 0) {
				return chars[posBefore];
			}
		}
		return 0;
	}

	boolean insideString() {
		boolean inString = false;
		inString = inString || inState(ParserState.INSIDE_DOUBLE_STRING);
		inString = inString || inState(ParserState.INSIDE_DOUBLE_TICKED);
		inString = inString || inState(ParserState.INSIDE_SINGLE_STRING);
		return inString;
	}

	boolean inState(ParserState parserState) {
		return getState().equals(parserState);
	}

	boolean moveCurrentPosWhenEmptyText() {
		if (getSb().length() == 0) {
			currentToken.start++;
			return true;
		}
		return false;
	}

	void restoreStateBeforeString() {
		switchTo(stateBeforeString);
	}

	void switchTo(ParserState parserState) {
		this.parserState = parserState;
		if (ParserState.VARIABLE.equals(parserState)) {
			getVariableContext().variableState = VariableState.NO_ARRAY;
		} else {
			variableContext = null;
		}
	}

	void switchToStringState(ParserState newStringState) {
		this.stateBeforeString = getState();
		switchTo(newStringState);
	}

	private ParseToken createToken() {
		ParseToken token = new ParseToken();
		token.start = pos;
		return token;
	}

	private StringBuilder getSb() {
		if (sb == null) {
			sb = new StringBuilder();
		}
		return sb;
	}

	private ParserState getState() {
		if (parserState == null) {
			parserState = ParserState.UNKNOWN;
		}
		return parserState;
	}

	private void resetText() {
		sb = null;
	}

	public VariableContext getVariableContext() {
		if (variableContext == null) {
			variableContext = new VariableContext();
		}
		return variableContext;
	}

	@Override
	public String toString() {
		return "ParseContext:" + getSb().toString() + "\nTokens:" + tokens;
	}

}