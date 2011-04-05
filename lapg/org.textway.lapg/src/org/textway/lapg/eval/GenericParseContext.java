/**
 * Copyright 2002-2010 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.textway.lapg.eval;

import org.textway.lapg.api.Grammar;
import org.textway.lapg.eval.GenericLexer.ErrorReporter;
import org.textway.lapg.eval.GenericParser.ParseException;
import org.textway.lapg.lalr.ParserTables;
import org.textway.lapg.lex.LexerTables;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Gryaznov Evgeny, 3/17/11
 */
public class GenericParseContext {

	private final Grammar grammar;
	private final ParserTables parserTables;
	private final LexerTables lexerTables;

	public GenericParseContext(Grammar grammar, ParserTables parserTables, LexerTables lexerTables) {
		this.grammar = grammar;
		this.parserTables = parserTables;
		this.lexerTables = lexerTables;
	}

	public Result parse(String text, int inputIndex) {
		return parse(new TextSource("input", text.toCharArray(), 1), inputIndex);
	}

	public Result parse(TextSource source, int inputIndex) {
		final List<ParseProblem> list = new ArrayList<ParseProblem>();
		ErrorReporter reporter = new ErrorReporter() {
			public void error(int start, int end, int line, String s) {
				list.add(new ParseProblem(KIND_ERROR, start, end, s, null));
			}
		};

		try {
			GenericLexer lexer = createLexer(source, reporter);
			lexer.setLine(source.getInitialLine());

			GenericParser parser = createParser(source, reporter);
			parser.source = source;
			Object result = parser.parse(lexer, inputIndex, parserTables.final_states[inputIndex], !grammar.getInput()[inputIndex].hasEoi());

			return new Result(source, result, list);
		} catch (ParseException ex) {
			/* not parsed */
		} catch (IOException ex) {
			list.add(new ParseProblem(KIND_FATAL, 0, 0, "I/O problem: " + ex.getMessage(), ex));
		}
		return new Result(source, null, list);
	}

	private GenericParser createParser(TextSource source, ErrorReporter reporter) {
		return new GenericParser(reporter, parserTables, grammar, false);
	}

	protected GenericLexer createLexer(TextSource source, ErrorReporter reporter) throws IOException {
		return new GenericLexer(source.getStream(), reporter, lexerTables, grammar);
	}

	public static class Result {
		private final TextSource source;
		private final Object root;
		private final List<ParseProblem> errors;

		public Result(TextSource source, Object root, List<ParseProblem> errors) {
			this.source = source;
			this.root = root;
			this.errors = errors;
		}

		public TextSource getSource() {
			return source;
		}

		public Object getRoot() {
			return root;
		}

		public List<ParseProblem> getErrors() {
			return errors;
		}
	}

	public static final int KIND_FATAL = 0;
	public static final int KIND_ERROR = 1;
	public static final int KIND_WARN = 2;

	public static final String PARSER_SOURCE = "parser";

	public static class ParseProblem extends Exception {
		private static final long serialVersionUID = 1L;

		private final int kind;
		private final int offset;
		private final int endoffset;

		public ParseProblem(int kind, int offset, int endoffset, String message, Throwable cause) {
			super(message, cause);
			this.kind = kind;
			this.offset = offset;
			this.endoffset = endoffset;
		}

		public int getKind() {
			return kind;
		}

		public int getOffset() {
			return offset;
		}

		public int getEndOffset() {
			return endoffset;
		}

		public String getSource() {
			return PARSER_SOURCE;
		}
	}

	public static class TextSource {

		private final String file;
		private final int initialLine;
		private final char[] contents;
		private int[] lineoffset;

		public TextSource(String file, char[] contents, int initialLine) {
			this.file = file;
			this.initialLine = initialLine;
			this.contents = contents;
		}

		public String getFile() {
			return file;
		}

		public int getInitialLine() {
			return initialLine;
		}

		public Reader getStream() {
			return new CharArrayReader(contents);
		}

		public String getLocation(int offset) {
			return file + "," + lineForOffset(offset);
		}

		public String getText(int start, int end) {
			if (start < 0 || start > contents.length || end > contents.length || start > end) {
				return "";
			}
			return new String(contents, start, end - start);
		}

		public int lineForOffset(int offset) {
			if (lineoffset == null) {
				lineoffset = getLineOffsets(contents);
			}
			int line = Arrays.binarySearch(lineoffset, offset);
			return initialLine + (line >= 0 ? line : -line - 2);
		}

		public char[] getContents() {
			return contents;
		}
	}

	private static int[] getLineOffsets(char[] contents) {
		int size = 1;
		for (int i = 0; i < contents.length; i++) {
			if (contents[i] == '\n') {
				size++;
			} else if (contents[i] == '\r') {
				if (i + 1 < contents.length && contents[i + 1] == '\n') {
					i++;
				}
				size++;
			}
		}
		int[] result = new int[size];
		result[0] = 0;
		int e = 1;
		for (int i = 0; i < contents.length; i++) {
			if (contents[i] == '\n') {
				result[e++] = i + 1;
			} else if (contents[i] == '\r') {
				if (i + 1 < contents.length && contents[i + 1] == '\n') {
					i++;
				}
				result[e++] = i + 1;
			}
		}
		if (e != size) {
			throw new IllegalStateException();
		}
		return result;
	}
}