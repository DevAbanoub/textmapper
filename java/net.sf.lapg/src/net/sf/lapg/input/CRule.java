package net.sf.lapg.input;

import java.util.List;

import net.sf.lapg.api.Rule;
import net.sf.lapg.templates.api.ILocatedEntity;

public class CRule implements ILocatedEntity, Rule {

	private CSymbol left;
	private final CSymbol[] right;
	private final CAction action;
	private final CSymbol priority;
	private final int line;
	int index;

	public CRule(List<CSymbol> right, CAction action, CSymbol priority, int line) {
		this.right = right != null ? right.toArray(new CSymbol[right.size()]) : new CSymbol[0];
		this.action = action;
		this.priority = priority;
		this.line = line;
		this.index = -1;
	}

	void setLeft(CSymbol sym) {
		this.left = sym;
	}

	public String getLocation() {
		return "line:" + line;
	}

	public CSymbol getLeft() {
		return left;
	}

	public CSymbol[] getRight() {
		return right;
	}

	public String getAction() {
		return action != null ? action.getContents() : null;
	}

	public int getPriority() {
		if( priority != null ) {
			return priority.getIndex();
		}
		for( int i = right.length-1; i >= 0; i --) {
			if( right[i].isTerm()) {
				return right[i].getIndex();
			}
		}
		return -1;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if( left.getName() == null ) {
			sb.append("<noname>");
		} else {
			sb.append(left.getName());
		}
		sb.append(" ::=");
		for( CSymbol s : right ) {
			sb.append(" ");
			if( s.getName() == null ) {
				sb.append("{}");
			} else {
				sb.append(s.getName());
			}
		}
		if( action != null ) {
			sb.append(" {}");
		}
		if( priority != null ) {
			sb.append(" << ");
			sb.append(priority.getName());
		}
		return sb.toString();
	}

	public int getLine() {
		return line;
	}
}