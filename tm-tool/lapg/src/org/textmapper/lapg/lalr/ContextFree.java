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
package org.textmapper.lapg.lalr;

import org.textmapper.lapg.api.*;
import org.textmapper.lapg.api.rule.RhsSymbol;

import java.util.Arrays;

abstract class ContextFree {

	// log

	protected final ProcessingStatus status;

	// grammar information

	protected final int nsyms, nterms, eoi, errorn;
	protected final int[] inputs;
	protected final boolean[] noEoiInput;
	protected final int rules, situations;
	protected final Symbol[] sym;
	protected final Rule[] wrules;
	protected final int[] priorul;
	protected final int[] rleft, rindex, rright, rprio;
	protected final boolean[] sym_empty;
	protected final int[] classterm; /* index of a class term, or 0 if none, or -1 for class term */
	protected final int[] softterms; /* a -1 terminated list of soft terms for a class */

	protected final int[] nla_situations; /* list of situations with nla */
	protected final int[] nla_rules; /* list of rules, starting with nla */
	protected final int[] sit_nla; /* set index for each situation */
	protected final IntegerSets nla;

	protected ContextFree(Grammar g, ProcessingStatus status) {
		this.status = status;

		this.sym = g.getSymbols();
		this.wrules = g.getRules();

		this.nsyms = g.getGrammarSymbols();
		this.rules = wrules.length;
		this.nterms = g.getTerminals();

		InputRef[] startRefs = g.getInput();
		this.noEoiInput = new boolean[startRefs.length];
		this.inputs = new int[startRefs.length];
		for (int i = 0; i < startRefs.length; i++) {
			this.inputs[i] = startRefs[i].getTarget().getIndex();
			this.noEoiInput[i] = !startRefs[i].hasEoi();
		}

		this.eoi = g.getEoi().getIndex();
		this.errorn = g.getError() == null ? -1 : g.getError().getIndex();

		this.situations = getSituations(wrules);

		this.priorul = getPriorityRules(g.getPriorities());

		this.classterm = new int[nterms];
		this.softterms = new int[nterms];
		Arrays.fill(this.softterms, -1);
		Arrays.fill(this.classterm, 0);
		for (int i = 0; i < nterms; i++) {
			Terminal term = (Terminal) this.sym[i];
			if (term.isSoft()) {
				int classindex = term.getSoftClass().getIndex();
				assert classindex < nterms && this.sym[classindex] instanceof Terminal && !((Terminal)this.sym[classindex]).isSoft();
				this.classterm[i] = classindex;
				this.classterm[classindex] = -1;
				this.softterms[i] = this.softterms[classindex];
				this.softterms[classindex] = i;
			}
		}

		this.rleft = new int[rules];
		this.rprio = new int[rules];
		this.rindex = new int[rules];
		this.rright = new int[situations];
		this.sym_empty = new boolean[nsyms];

		int curr_rindex = 0;
		int nla_count = 0;
		int nla_rcount = 0;
		for (int i = 0; i < wrules.length; i++) {
			Rule r = wrules[i];
			this.rleft[i] = r.getLeft().getIndex();
			this.rprio[i] = r.getPriority();
			this.rindex[i] = curr_rindex;
			RhsSymbol[] wright = r.getRight();
			for (RhsSymbol element : wright) {
				if (element.getNegativeLA() != null) {
					nla_count++;
					if (element == wright[0]) {
						nla_rcount++;
					}
				}
				this.rright[curr_rindex++] = element.getTarget().getIndex();
			}
			this.rright[curr_rindex++] = -1 - i;
			if (wright.length == 0) {
				sym_empty[rleft[i]] = true;
			}
		}

		if (nla_count > 0) {
			sit_nla = new int[situations];
			Arrays.fill(sit_nla, -1);
			nla = new IntegerSets();
			nla_situations = new int[nla_count];
			nla_rules = new int[nla_rcount];
			int nla_sit_index = 0, nla_rule_index = 0;
			for (int i = 0; i < wrules.length; i++) {
				int sit = rindex[i];
				RhsSymbol[] wright = wrules[i].getRight();
				for (RhsSymbol element : wright) {
					NegativeLookahead nl = element.getNegativeLA();
					if (nl != null) {
						sit_nla[sit] = nla.storeSet(toSortedIds(nl.getUnwantedSet()));
						nla_situations[nla_sit_index++] = sit;
						if (element == wright[0]) {
							nla_rules[nla_rule_index++] = i;
						}
					}
					sit++;
				}
			}
			assert nla_count == nla_sit_index;
			assert nla_rcount == nla_rule_index;
		} else {
			sit_nla = null;
			nla = null;
			nla_situations = null;
			nla_rules = null;
		}

		assert situations == curr_rindex;
	}

	private static int getSituations(Rule[] rules) {
		int counter = 0;
		for (Rule rule : rules) {
			counter += rule.getRight().length + 1;
		}
		return counter;
	}

	private static int[] getPriorityRules(Prio[] prios) {
		int counter = 0;
		for (Prio prio : prios) {
			counter += prio.getSymbols().length + 1;
		}

		int[] result = new int[counter];

		int curr_prio = 0;
		for (Prio prio : prios) {
			result[curr_prio++] = -prio.getPrio();
			Symbol[] list = prio.getSymbols();
			for (Symbol element : list) {
				result[curr_prio++] = element.getIndex();
			}
		}

		assert curr_prio == counter;
		return result;
	}

	private static int[] toSortedIds(Terminal[] list) {
		int[] result = new int[list.length];
		for (int i = 0; i < list.length; i++) {
			result[i] = list[i].getIndex();
		}
		Arrays.sort(result);
		return result;
	}

	protected int ruleIndex(int situation) {
		while (rright[situation] >= 0) situation++;
		return -rright[situation] - 1;
	}

	// info

	protected void print_situation(int situation) {
		int rulenum, i;

		for (i = situation; rright[i] >= 0; i++) {
		}
		rulenum = -rright[i] - 1;

		// left part of the rule
		status.debug("  " + sym[rleft[rulenum]].getName() + " ::=");

		for (i = rindex[rulenum]; rright[i] >= 0; i++) {
			if (i == situation) {
				status.debug(" _");
			}
			status.debug(" " + sym[rright[i]].getName());
		}
		if (i == situation) {
			status.debug(" _");
		}
		status.debug("\n");
	}
}
