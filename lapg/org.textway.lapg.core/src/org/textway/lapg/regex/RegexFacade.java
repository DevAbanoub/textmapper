/**
 * Copyright 2002-2012 Evgeny Gryaznov
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
package org.textway.lapg.regex;

import org.textway.lapg.api.regex.RegexContext;
import org.textway.lapg.api.regex.RegexPart;
import org.textway.lapg.lex.RegexpParseException;
import org.textway.lapg.regex.RegexDefTree.RegexDefProblem;
import org.textway.lapg.regex.RegexDefTree.TextSource;

import java.util.Map;

/**
 * evgeny, 6/11/12
 */
public class RegexFacade {

	public static RegexContext createContext(final Map<String, RegexPart> map) {
		return new RegexContext() {
			@Override
			public RegexPart resolvePattern(String name) {
				return map.get(name);
			}
		};
	}

	public static RegexPart parse(String alias, String regex) throws RegexpParseException {
		if (regex.length() == 0) {
			throw new RegexpParseException("regexp is empty", 0);
		}

		RegexDefTree<RegexAstPart> result = RegexDefTree.parse(new TextSource(alias, regex.toCharArray(), 1));
		if (result.hasErrors()) {
			RegexDefProblem problem = result.getErrors().get(0);
			String message = problem.getMessage();
			if (message.startsWith("syntax error") || message.startsWith("invalid lexem at")) {
				if (problem.getOffset() >= regex.length()) {
					message = "regexp is incomplete";
				} else {
					message = "regexp has syntax error near `" + regex.substring(problem.getOffset()) + "'";
				}
			} else if (message.equals("Unexpected end of input reached")) {
				message = "unfinished regexp";
			}
			throw new RegexpParseException(message, problem.getOffset());
		}
		return result.getRoot();
	}
}