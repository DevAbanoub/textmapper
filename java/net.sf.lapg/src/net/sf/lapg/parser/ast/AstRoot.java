package net.sf.lapg.parser.ast;

import java.util.List;

import net.sf.lapg.parser.LapgTree.TextSource;

public class AstRoot extends AstNode {

	private final List<AstOption> options;
	private final List<AstLexerPart> lexer;
	private final List<AstGrammarPart> grammar;
	private int templatesStart = -1;

	public AstRoot(List<AstOption> options, List<AstLexerPart> lexer, List<AstGrammarPart> grammar, TextSource source,
			int offset, int endoffset) {
		super(source, offset, endoffset);
		this.options = options;
		this.lexer = lexer;
		this.grammar = grammar;
	}

	public List<AstOption> getOptions() {
		return options;
	}

	public List<AstLexerPart> getLexer() {
		return lexer;
	}

	public List<AstGrammarPart> getGrammar() {
		return grammar;
	}

	@Override
	public void accept(AbstractVisitor v) {
		if(v.visit(this)) {
			if(options != null) {
				for(AstOption o : options) {
					o.accept(v);
				}
			}
		}
	}

	public int getTemplatesStart() {
		return templatesStart;
	}
	
	public void setTemplatesStart(int templatesStart) {
		this.templatesStart = templatesStart;
	}
}
