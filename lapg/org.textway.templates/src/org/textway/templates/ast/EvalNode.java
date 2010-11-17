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
package org.textway.templates.ast;

import org.textway.templates.api.EvaluationContext;
import org.textway.templates.api.EvaluationException;
import org.textway.templates.api.IEvaluationStrategy;
import org.textway.templates.api.ILocatedEntity;
import org.textway.templates.ast.AstTree.TextSource;

public class EvalNode extends Node {

	private final ExpressionNode templateExpr;
	private final ExpressionNode templateLocation;

	public EvalNode(ExpressionNode expr, ExpressionNode templateId, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.templateExpr = expr;
		this.templateLocation = templateId;
	}

	@Override
	protected void emit(StringBuilder sb, EvaluationContext context, IEvaluationStrategy env) {
		try {
			Object toEvaluate = env.evaluate(templateExpr, context, false);
			String id;
			if( templateLocation != null ) {
				id = env.toString(env.evaluate(templateLocation, context, false), templateLocation);
			} else {
				id = toEvaluate instanceof ILocatedEntity ? ((ILocatedEntity)toEvaluate).getLocation() : null;
			}
			String templateCode = env.toString(toEvaluate, templateExpr);
			sb.append(env.eval(this, templateCode, id, context, templateExpr.getLine()));
		} catch (EvaluationException ex) {
		}
	}
}