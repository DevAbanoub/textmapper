// generated by Textmapper; DO NOT EDIT

package js

import (
	"strings"
	"unicode/utf8"
)

// Lexer states.
const (
	StateInitial        = 0
	StateDiv            = 1
	StateTemplate       = 2
	StateTemplateDiv    = 3
	StateJsxTemplate    = 4
	StateJsxTemplateDiv = 5
	StateJsxTag         = 6
	StateJsxClosingTag  = 7
	StateJsxText        = 8
)

// ErrorHandler is called every time a lexer or parser is unable to process
// some part of the input.
type ErrorHandler func(line, offset, len int, msg string)

// IgnoreErrorsHandler is a no-op error handler.
func IgnoreErrorsHandler(line, offset, len int, msg string) {}

// Lexer uses a generated DFA to scan through a utf-8 encoded input string. If
// the string starts with a BOM character, it gets skipped.
type Lexer struct {
	source string
	err    ErrorHandler

	ch          rune // current character, -1 means EOI
	offset      int  // character offset
	tokenOffset int  // last token offset
	line        int  // current line number (1-based)
	tokenLine   int  // last token line
	lineOffset  int  // current line offset
	scanOffset  int  // scanning offset
	value       interface{}

	State int // lexer state, modifiable

	token  Token // last token
	Stack  []int // stack of JSX states, non-empty for StateJsx*
	Opened []int // number of opened curly braces per jsxTemplate* state
}

const bom = 0xfeff // byte order mark, permitted as a first character only
var bomSeq = "\xef\xbb\xbf"

// Init prepares the lexer l to tokenize source by performing the full reset
// of the internal state.
//
// Note that Init may call err once if there is an error in the
// first few characters of the text.
func (l *Lexer) Init(source string, err ErrorHandler) {
	l.source = source
	l.err = err

	l.ch = 0
	l.offset = 0
	l.tokenOffset = 0
	l.line = 1
	l.tokenLine = 1
	l.lineOffset = 0
	l.scanOffset = 0
	l.State = 0
	l.token = UNAVAILABLE
	l.Stack = nil
	l.Opened = nil

	if strings.HasPrefix(source, bomSeq) {
		l.scanOffset += len(bomSeq)
	}

	l.offset = l.scanOffset
	if l.offset < len(l.source) {
		r, w := rune(l.source[l.offset]), 1
		if r >= 0x80 {
			// not ASCII
			r, w = utf8.DecodeRuneInString(l.source[l.offset:])
			if r == utf8.RuneError && w == 1 || r == bom {
				l.invalidRune(r, w)
			}
		}
		l.scanOffset += w
		l.ch = r
	} else {
		l.ch = -1 // EOI
	}
}

// Next finds and returns the next token in l.source. The source end is
// indicated by Token.EOI.
//
// The token text can be retrieved later by calling the Text() method.
func (l *Lexer) Next() Token {
	prevLine := l.tokenLine
restart:
	l.tokenLine = l.line
	l.tokenOffset = l.offset

	state := tmStateMap[l.State]
	hash := uint32(0)
	for state >= 0 {
		var ch int
		switch {
		case l.ch < 0:
			state = int(tmLexerAction[state*tmNumClasses])
			if state == -1 {
				return INVALID_TOKEN // Unexpected end of input reached
			}
			continue
		case int(l.ch) < tmRuneClassLen:
			ch = int(tmRuneClass[l.ch])
		default:
			ch = mapRune(l.ch)
		}
		state = int(tmLexerAction[state*tmNumClasses+ch])
		if state < -1 {
			break
		}
		hash = hash*uint32(31) + uint32(l.ch)

		if l.ch == '\n' {
			l.line++
			l.lineOffset = l.offset
		}

		// Scan the next character.
		// Note: the following code is inlined to avoid performance implications.
		l.offset = l.scanOffset
		if l.offset < len(l.source) {
			r, w := rune(l.source[l.offset]), 1
			if r >= 0x80 {
				// not ASCII
				r, w = utf8.DecodeRuneInString(l.source[l.offset:])
				if r == utf8.RuneError && w == 1 || r == bom {
					l.invalidRune(r, w)
				}
			}
			l.scanOffset += w
			l.ch = r
		} else {
			l.ch = -1 // EOI
		}
	}
	if state >= -2 {
		if state == -1 {
			return INVALID_TOKEN
		}
		if state == -2 {
			return EOI
		}
	}

	token := Token(-state - 3)
	switch token {
	case IDENTIFIER:
		hh := hash & 63
		switch hh {
		case 1:
			if hash == 0x5c13d641 && "default" == l.source[l.tokenOffset:l.offset] {
				token = DEFAULT
				break
			}
			if hash == 0x2f9501 && "enum" == l.source[l.tokenOffset:l.offset] {
				token = ENUM
				break
			}
		case 3:
			if hash == 0xcd244983 && "finally" == l.source[l.tokenOffset:l.offset] {
				token = FINALLY
				break
			}
		case 6:
			if hash == 0x37b0c6 && "with" == l.source[l.tokenOffset:l.offset] {
				token = WITH
				break
			}
		case 7:
			if hash == 0x33c587 && "null" == l.source[l.tokenOffset:l.offset] {
				token = NULL
				break
			}
		case 9:
			if hash == 0x18cc9 && "for" == l.source[l.tokenOffset:l.offset] {
				token = FOR
				break
			}
		case 11:
			if hash == 0xc8b && "do" == l.source[l.tokenOffset:l.offset] {
				token = DO
				break
			}
		case 13:
			if hash == 0x6da5f8d && "yield" == l.source[l.tokenOffset:l.offset] {
				token = YIELD
				break
			}
		case 14:
			if hash == 0x36758e && "true" == l.source[l.tokenOffset:l.offset] {
				token = TRUE
				break
			}
		case 17:
			if hash == 0xcb7e7191 && "target" == l.source[l.tokenOffset:l.offset] {
				token = TARGET
				break
			}
			if hash == 0xcccfb691 && "typeof" == l.source[l.tokenOffset:l.offset] {
				token = TYPEOF
				break
			}
		case 20:
			if hash == 0x375194 && "void" == l.source[l.tokenOffset:l.offset] {
				token = VOID
				break
			}
		case 22:
			if hash == 0x58e7956 && "await" == l.source[l.tokenOffset:l.offset] {
				token = AWAIT
				break
			}
			if hash == 0x18f56 && "get" == l.source[l.tokenOffset:l.offset] {
				token = GET
				break
			}
		case 23:
			if hash == 0xdd7 && "of" == l.source[l.tokenOffset:l.offset] {
				token = OF
				break
			}
		case 24:
			if hash == 0x524f73d8 && "function" == l.source[l.tokenOffset:l.offset] {
				token = FUNCTION
				break
			}
		case 25:
			if hash == 0xb22d2499 && "extends" == l.source[l.tokenOffset:l.offset] {
				token = EXTENDS
				break
			}
		case 27:
			if hash == 0x1a21b && "let" == l.source[l.tokenOffset:l.offset] {
				token = LET
				break
			}
		case 29:
			if hash == 0xd1d && "if" == l.source[l.tokenOffset:l.offset] {
				token = IF
				break
			}
		case 30:
			if hash == 0x364e9e && "this" == l.source[l.tokenOffset:l.offset] {
				token = THIS
				break
			}
		case 32:
			if hash == 0x1a9a0 && "new" == l.source[l.tokenOffset:l.offset] {
				token = NEW
				break
			}
		case 33:
			if hash == 0x20a6f421 && "debugger" == l.source[l.tokenOffset:l.offset] {
				token = DEBUGGER
				break
			}
		case 34:
			if hash == 0x1bc62 && "set" == l.source[l.tokenOffset:l.offset] {
				token = SET
				break
			}
		case 35:
			if hash == 0x5a73763 && "const" == l.source[l.tokenOffset:l.offset] {
				token = CONST
				break
			}
			if hash == 0x5cb1923 && "false" == l.source[l.tokenOffset:l.offset] {
				token = FALSE
				break
			}
		case 37:
			if hash == 0xb96173a5 && "import" == l.source[l.tokenOffset:l.offset] {
				token = IMPORT
				break
			}
			if hash == 0xd25 && "in" == l.source[l.tokenOffset:l.offset] {
				token = IN
				break
			}
		case 38:
			if hash == 0x693a6e6 && "throw" == l.source[l.tokenOffset:l.offset] {
				token = THROW
				break
			}
		case 39:
			if hash == 0xde312ca7 && "continue" == l.source[l.tokenOffset:l.offset] {
				token = CONTINUE
				break
			}
			if hash == 0x1c727 && "var" == l.source[l.tokenOffset:l.offset] {
				token = VAR
				break
			}
		case 42:
			if hash == 0x3017aa && "from" == l.source[l.tokenOffset:l.offset] {
				token = FROM
				break
			}
		case 43:
			if hash == 0xb06685ab && "delete" == l.source[l.tokenOffset:l.offset] {
				token = DELETE
				break
			}
		case 44:
			if hash == 0x35c3d12c && "instanceof" == l.source[l.tokenOffset:l.offset] {
				token = INSTANCEOF
				break
			}
		case 46:
			if hash == 0xcacdce6e && "static" == l.source[l.tokenOffset:l.offset] {
				token = STATIC
				break
			}
		case 48:
			if hash == 0x2e7b30 && "case" == l.source[l.tokenOffset:l.offset] {
				token = CASE
				break
			}
			if hash == 0xc84e3d30 && "return" == l.source[l.tokenOffset:l.offset] {
				token = RETURN
				break
			}
		case 49:
			if hash == 0x6bdcb31 && "while" == l.source[l.tokenOffset:l.offset] {
				token = WHILE
				break
			}
		case 50:
			if hash == 0xc32 && "as" == l.source[l.tokenOffset:l.offset] {
				token = AS
				break
			}
		case 52:
			if hash == 0xb32913b4 && "export" == l.source[l.tokenOffset:l.offset] {
				token = EXPORT
				break
			}
			if hash == 0xcafbb734 && "switch" == l.source[l.tokenOffset:l.offset] {
				token = SWITCH
				break
			}
		case 56:
			if hash == 0x5a5a978 && "class" == l.source[l.tokenOffset:l.offset] {
				token = CLASS
				break
			}
		case 57:
			if hash == 0x2f8d39 && "else" == l.source[l.tokenOffset:l.offset] {
				token = ELSE
				break
			}
		case 59:
			if hash == 0x5a0eebb && "catch" == l.source[l.tokenOffset:l.offset] {
				token = CATCH
				break
			}
			if hash == 0x68b6f7b && "super" == l.source[l.tokenOffset:l.offset] {
				token = SUPER
				break
			}
			if hash == 0x1c1bb && "try" == l.source[l.tokenOffset:l.offset] {
				token = TRY
				break
			}
		case 63:
			if hash == 0x59a58ff && "break" == l.source[l.tokenOffset:l.offset] {
				token = BREAK
				break
			}
		}
	}

	switch token {
	case 1:
		goto restart
	}

	// There is an ambiguity in the language that a slash can either represent
	// a division operator, or start a regular expression literal. This gets
	// disambiguated at the grammar level - division always follows an
	// expression, while regex literals are expressions themselves. Here we use
	// some knowledge about the grammar to decide whether the next token can be
	// a regular expression literal.
	//
	// See the following thread for more details:
	// http://stackoverflow.com/questions/5519596/when-parsing-javascript-what

	if l.State <= StateJsxTemplateDiv {
		// The lowest bit of "l.State" determines how to interpret a forward
		// slash if it happens to be the next character.
		//   unset: start of a regular expression literal
		//   set:   start of a division operator (/ or /=)
		switch token {
		case NEW, DELETE, VOID, TYPEOF, INSTANCEOF, IN, DO, RETURN, CASE, THROW, ELSE:
			l.State &^= 1
		case TEMPLATEHEAD, TEMPLATEMIDDLE:
			l.State = StateTemplate
		case TEMPLATETAIL:
			l.State = StateDiv
		case RPAREN, RBRACK:
			// TODO support if (...) /aaaa/;
			l.State |= 1
		case PLUSPLUS, MINUSMINUS:
			if prevLine != l.tokenLine {
				// This is a pre-increment/decrement, so we expect a regular expression.
				l.State &^= 1
			}
			// Otherwise: if we were expecting a regular expression literal before this
			// token, this is a pre-increment/decrement, otherwise, this is a post. We
			// can just propagate the previous value of the lowest bit of the state.
		case LT:
			if l.State&1 == 0 {
				// Start a new JSX tag.
				l.Stack = append(l.Stack, l.State|1)
				l.State = StateJsxTag
			} else {
				l.State &^= 1
			}
		case LBRACE:
			if l.State >= StateJsxTemplate {
				l.Opened[len(l.Opened)-1]++
			}
			l.State &^= 1
		case RBRACE:
			if l.State >= StateJsxTemplate {
				last := len(l.Opened) - 1
				l.Opened[last]--
				if l.Opened[last] == 0 {
					l.Opened = l.Opened[:last]
					l.State = l.Stack[len(l.Stack)-1]
					l.Stack = l.Stack[:len(l.Stack)-1]
					break
				}
			}
			l.State &^= 1
		case SINGLELINECOMMENT, MULTILINECOMMENT:
			break
		default:
			if token >= punctuationStart && token < punctuationEnd {
				l.State &^= 1
			} else {
				l.State |= 1
			}
		}
	} else {
		// Handling JSX states.
		switch token {
		case DIV:
			if l.State == StateJsxTag && l.token == LT && l.Stack[len(l.Stack)-1] == StateJsxText {
				l.State = StateJsxClosingTag
				l.Stack = l.Stack[:len(l.Stack)-1]
			}
		case GT:
			if l.State == StateJsxClosingTag || l.token == DIV {
				l.State = l.Stack[len(l.Stack)-1]
				l.Stack = l.Stack[:len(l.Stack)-1]
			} else {
				l.State = StateJsxText
			}
		case LBRACE:
			l.Opened = append(l.Opened, 1)
			l.Stack = append(l.Stack, l.State)
			l.State = StateJsxTemplate
		case LT:
			// Start a new JSX tag.
			l.Stack = append(l.Stack, l.State)
			l.State = StateJsxTag
		}
	}
	l.token = token
	return token
}

func (l *Lexer) invalidRune(r rune, w int) {
	switch r {
	case utf8.RuneError:
		l.err(l.line, l.offset, w, "illegal UTF-8 encoding")
	case bom:
		l.err(l.line, l.offset, w, "illegal byte order mark")
	}
}

// Pos returns the start and end positions of the last token returned by Next().
func (l *Lexer) Pos() (start, end int) {
	start = l.tokenOffset
	end = l.offset
	return
}

// Line returns the line number of the last token returned by Next().
func (l *Lexer) Line() int {
	return l.tokenLine
}

// Text returns the substring of the input corresponding to the last token.
func (l *Lexer) Text() string {
	return l.source[l.tokenOffset:l.offset]
}

// Value returns the value associated with the last returned token.
func (l *Lexer) Value() interface{} {
	return l.value
}
