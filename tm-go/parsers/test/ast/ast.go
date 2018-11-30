// generated by Textmapper; DO NOT EDIT

package ast

import (
	"github.com/inspirer/textmapper/tm-go/parsers/test"
	"github.com/inspirer/textmapper/tm-go/parsers/test/selector"
)

type Node interface {
	Type() test.NodeType
	Offset() int
	Endoffset() int
	// Child returns the first child node that matches the selector.
	Child(sel selector.Selector) Node
	Children(sel selector.Selector) []Node
	// Next returns the first element among the following siblings that matches the selector.
	Next(sel selector.Selector) Node
	// NextAll returns all following siblings of the node that match the selector.
	NextAll(sel selector.Selector) []Node
	Text() string
	IsValid() bool
}

// Interfaces.

type TestNode interface {
	TestNode() Node
}

type Token struct {
	Node
}

type NilNode struct{}

var nilInstance = &NilNode{}

// All types implement TestNode.
func (n Block) TestNode() Node    { return n.Node }
func (n Decl1) TestNode() Node    { return n.Node }
func (n Decl2) TestNode() Node    { return n.Node }
func (n Int) TestNode() Node      { return n.Node }
func (n Negation) TestNode() Node { return n.Node }
func (n Test) TestNode() Node     { return n.Node }
func (n Token) TestNode() Node    { return n.Node }
func (NilNode) TestNode() Node    { return nil }

type Declaration interface {
	TestNode
	declarationNode()
}

// declarationNode() ensures that only the following types can be
// assigned to Declaration.
//
func (Block) declarationNode()   {}
func (Decl1) declarationNode()   {}
func (Decl2) declarationNode()   {}
func (Int) declarationNode()     {}
func (NilNode) declarationNode() {}

// Types.

type Block struct {
	Node
}

func (n Block) Negation() (Negation, bool) {
	field := Negation{n.Child(selector.Negation)}
	return field, field.IsValid()
}

func (n Block) Declaration() []Declaration {
	nodes := n.Children(selector.Declaration)
	var ret = make([]Declaration, 0, len(nodes))
	for _, node := range nodes {
		ret = append(ret, ToTestNode(node).(Declaration))
	}
	return ret
}

type Decl1 struct {
	Node
}

func (n Decl1) Identifier() []Token {
	nodes := n.Children(selector.Identifier)
	var ret = make([]Token, 0, len(nodes))
	for _, node := range nodes {
		ret = append(ret, Token{node})
	}
	return ret
}

type Decl2 struct {
	Node
}

type Int struct {
	Node
}

type Negation struct {
	Node
}

type Test struct {
	Node
}

func (n Test) Declaration() []Declaration {
	nodes := n.Children(selector.Declaration)
	var ret = make([]Declaration, 0, len(nodes))
	for _, node := range nodes {
		ret = append(ret, ToTestNode(node).(Declaration))
	}
	return ret
}
