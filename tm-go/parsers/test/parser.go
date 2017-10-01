// generated by Textmapper; DO NOT EDIT

package test

import (
	"fmt"
)

// Parser is a table-driven LALR parser for test.
type Parser struct {
	listener Listener

	stack         []stackEntry
	next          symbol
	ignoredTokens []symbol // to be reported with the next shift
}

type SyntaxError struct {
	Offset    int
	Endoffset int
}

func (e SyntaxError) Error() string {
	return "syntax error"
}

type symbol struct {
	symbol    int32
	offset    int
	endoffset int
}

type stackEntry struct {
	sym   symbol
	state int8
	value interface{}
}

func (p *Parser) Init(l Listener) {
	p.listener = l
}

const (
	startStackSize       = 512
	startTokenBufferSize = 16
	noToken              = int32(UNAVAILABLE)
	eoiToken             = int32(EOI)
	debugSyntax          = false
)

func (p *Parser) Parse(lexer *Lexer) error {
	return p.parse(0, 30, lexer)
}

func (p *Parser) parse(start, end int8, lexer *Lexer) error {
	if cap(p.stack) < startStackSize {
		p.stack = make([]stackEntry, 0, startStackSize)
	}
	if cap(p.ignoredTokens) < startTokenBufferSize {
		p.ignoredTokens = make([]symbol, 0, startTokenBufferSize)
	} else {
		p.ignoredTokens = p.ignoredTokens[:0]
	}
	state := start

	p.stack = append(p.stack[:0], stackEntry{state: state})
	p.fetchNext(lexer)

	for state != end {
		action := tmAction[state]
		if action < -2 {
			// Lookahead is needed.
			if p.next.symbol == noToken {
				p.fetchNext(lexer)
			}
			action = lalr(action, p.next.symbol)
		}

		if action >= 0 {
			// Reduce.
			rule := action
			ln := int(tmRuleLen[rule])

			var entry stackEntry
			entry.sym.symbol = tmRuleSymbol[rule]
			rhs := p.stack[len(p.stack)-ln:]
			p.stack = p.stack[:len(p.stack)-ln]
			if ln == 0 {
				entry.sym.offset, _ = lexer.Pos()
				entry.sym.endoffset = entry.sym.offset
			} else {
				entry.sym.offset = rhs[0].sym.offset
				entry.sym.endoffset = rhs[ln-1].sym.endoffset
			}
			p.applyRule(rule, &entry, rhs, lexer)
			if debugSyntax {
				fmt.Printf("reduced to: %v\n", Symbol(entry.sym.symbol))
			}
			state = gotoState(p.stack[len(p.stack)-1].state, entry.sym.symbol)
			entry.state = state
			p.stack = append(p.stack, entry)

		} else if action == -1 {
			// Shift.
			if p.next.symbol == noToken {
				p.fetchNext(lexer)
			}
			state = gotoState(state, p.next.symbol)
			p.stack = append(p.stack, stackEntry{
				sym:   p.next,
				state: state,
				value: lexer.Value(),
			})
			if debugSyntax {
				fmt.Printf("shift: %v (%s)\n", Symbol(p.next.symbol), lexer.Text())
			}
			if len(p.ignoredTokens) > 0 {
				p.reportIgnoredTokens()
			}
			switch Token(p.next.symbol) {
			case IDENTIFIER:
				p.listener(Identifier, p.next.offset, p.next.endoffset)
			}
			if state != -1 && p.next.symbol != eoiToken {
				p.next.symbol = noToken
			}
		}

		if action == -2 || state == -1 {
			break
		}
	}

	if state != end {
		offset, endoffset := lexer.Pos()
		err := SyntaxError{
			Offset:    offset,
			Endoffset: endoffset,
		}
		return err
	}

	return nil
}

func lalr(action, next int32) int32 {
	a := -action - 3
	for ; tmLalr[a] >= 0; a += 2 {
		if tmLalr[a] == next {
			break
		}
	}
	return tmLalr[a+1]
}

func gotoState(state int8, symbol int32) int8 {
	min := tmGoto[symbol]
	max := tmGoto[symbol+1]

	if max-min < 32 {
		for i := min; i < max; i += 2 {
			if tmFromTo[i] == state {
				return tmFromTo[i+1]
			}
		}
	} else {
		for min < max {
			e := (min + max) >> 1 &^ int32(1)
			i := tmFromTo[e]
			if i == state {
				return tmFromTo[e+1]
			} else if i < state {
				min = e + 2
			} else {
				max = e
			}
		}
	}
	return -1
}

func (p *Parser) fetchNext(lexer *Lexer) {
restart:
	tok := lexer.Next()
	switch tok {
	case MULTILINECOMMENT, SINGLELINECOMMENT, INVALID_TOKEN:
		s, e := lexer.Pos()
		p.ignoredTokens = append(p.ignoredTokens, symbol{int32(tok), s, e})
		goto restart
	}
	p.next.symbol = int32(tok)
	p.next.offset, p.next.endoffset = lexer.Pos()
}

func (p *Parser) applyRule(rule int32, lhs *stackEntry, rhs []stackEntry, lexer *Lexer) {
	switch rule {
	case 5: // Declaration : '{' '-' '-' Declaration_list '}'
		p.listener(Negation, rhs[1].sym.offset, rhs[2].sym.endoffset)
	case 6: // Declaration : '{' '-' '-' '}'
		p.listener(Negation, rhs[1].sym.offset, rhs[2].sym.endoffset)
	case 7: // Declaration : '{' '-' Declaration_list '}'
		p.listener(Negation, rhs[1].sym.offset, rhs[1].sym.endoffset)
	case 8: // Declaration : '{' '-' '}'
		p.listener(Negation, rhs[1].sym.offset, rhs[1].sym.endoffset)
	case 11: // Declaration : IntegerConstant '[' ']'
		nn0, _ := rhs[0].value.(int)
		{
			switch nn0 {
			case 7:
				p.listener(Int7, rhs[0].sym.offset, rhs[2].sym.endoffset)
			case 9:
				p.listener(Int9, rhs[0].sym.offset, rhs[2].sym.endoffset)
			}
		}
	case 12: // Declaration : IntegerConstant
		nn0, _ := rhs[0].value.(int)
		{
			switch nn0 {
			case 7:
				p.listener(Int7, rhs[0].sym.offset, rhs[0].sym.endoffset)
			case 9:
				p.listener(Int9, rhs[0].sym.offset, rhs[0].sym.endoffset)
			}
		}
	}
	nt := ruleNodeType[rule]
	if nt == 0 {
		return
	}
	p.listener(nt, lhs.sym.offset, lhs.sym.endoffset)
}

func (p *Parser) reportIgnoredTokens() {
	for _, c := range p.ignoredTokens {
		var t NodeType
		switch Token(c.symbol) {
		case MULTILINECOMMENT:
			t = MultiLineComment
		case SINGLELINECOMMENT:
			t = SingleLineComment
		case INVALID_TOKEN:
			t = InvalidToken
		default:
			continue
		}
		p.listener(t, c.offset, c.endoffset)
	}
	p.ignoredTokens = p.ignoredTokens[:0]
}
