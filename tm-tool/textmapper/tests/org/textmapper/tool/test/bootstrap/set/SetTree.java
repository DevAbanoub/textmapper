package org.textmapper.tool.test.bootstrap.set;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.textmapper.tool.test.bootstrap.set.SetLexer.ErrorReporter;
import org.textmapper.tool.test.bootstrap.set.SetParser.ParseException;

public class SetTree<T> {

	private final TextSource source;
	private final T root;
	private final List<SetProblem> errors;

	public SetTree(TextSource source, T root, List<SetProblem> errors) {
		this.source = source;
		this.root = root;
		this.errors = errors;
	}

	public TextSource getSource() {
		return source;
	}

	public T getRoot() {
		return root;
	}

	public List<SetProblem> getErrors() {
		return errors;
	}

	public boolean hasErrors() {
		return errors.size() > 0;
	}


	public static SetTree<Object> parse(TextSource source) {
		final List<SetProblem> list = new ArrayList<SetProblem>();
		ErrorReporter reporter = new ErrorReporter() {
			public void error(String message, int line, int offset, int endoffset) {
				list.add(new SetProblem(KIND_ERROR, message, line, offset, endoffset, null));
			}
		};

		try {
			SetLexer lexer = new SetLexer(source.getStream(), reporter);
			lexer.setLine(source.getInitialLine());

			SetParser parser = new SetParser(reporter);
			Object result = parser.parse(lexer);

			return new SetTree<Object>(source, result, list);
		} catch (ParseException ex) {
			/* not parsed */
		} catch (IOException ex) {
			list.add(new SetProblem(KIND_FATAL, "I/O problem: " + ex.getMessage(), 0, 0, 0, ex));
		}
		return new SetTree<Object>(source, null, list);
	}


	public static final int KIND_FATAL = 0;
	public static final int KIND_ERROR = 1;
	public static final int KIND_WARN = 2;

	public static final String PARSER_SOURCE = "parser";

	public static class SetProblem extends Exception {
		private static final long serialVersionUID = 1L;

		private final int kind;
		private final int line;
		private final int offset;
		private final int endoffset;

		public SetProblem(int kind, String message, int line, int offset, int endoffset, Throwable cause) {
			super(message, cause);
			this.kind = kind;
			this.line = line;
			this.offset = offset;
			this.endoffset = endoffset;
		}

		public int getKind() {
			return kind;
		}

		public int getLine() {
			return line;
		}

		public int getOffset() {
			return offset;
		}

		public int getEndoffset() {
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

		public int columnForOffset(int offset) {
			if (lineoffset == null) {
				lineoffset = getLineOffsets(contents);
			}
			int line = Arrays.binarySearch(lineoffset, offset);
			return offset >= 0 ? offset - lineoffset[line >= 0 ? line : -line - 2] : 0;
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
