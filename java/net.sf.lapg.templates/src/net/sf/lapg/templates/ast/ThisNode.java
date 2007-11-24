package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.IEvaluationEnvironment;

public class ThisNode extends ExpressionNode {

	protected ThisNode(int line) {
		super(line);
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationEnvironment env) {
		return context.getThisObject();
	}

	@Override
	public void toString(StringBuffer sb) {
		sb.append("this");
	}
}
