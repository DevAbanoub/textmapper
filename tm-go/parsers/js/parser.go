package js

func (p *Parser) Parse(lexer *Lexer) bool {
	return p.parse(0, 2628, lexer)
}

func (p *Parser) applyRule(rule int32, node *node, rhs []node) {
	nt := ruleNodeType[rule]
	if nt == 0 {
		return
	}
	p.listener.Node(nt, node.sym.offset, node.sym.endoffset)
}
