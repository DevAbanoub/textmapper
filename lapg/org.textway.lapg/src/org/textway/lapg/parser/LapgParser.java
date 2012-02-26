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
package org.textway.lapg.parser;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.ArrayList;
import org.textway.lapg.api.Lexem;
import org.textway.lapg.parser.LapgLexer.ErrorReporter;
import org.textway.lapg.parser.LapgLexer.Lexems;
import org.textway.lapg.parser.LapgTree.TextSource;
import org.textway.lapg.parser.ast.*;
import org.textway.lapg.parser.LapgLexer.LapgSymbol;

public class LapgParser {

	public static class ParseException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParseException() {
		}
	}

	private final ErrorReporter reporter;

	public LapgParser(ErrorReporter reporter) {
		this.reporter = reporter;
	}


	private static final boolean DEBUG_SYNTAX = false;
	TextSource source;
	private static final int lapg_action[] = {
		-1, -1, -3, 133, -1, -1, 4, -11, -17, 29, 7, -53, 108, 109, -95, 110,
		111, 112, -1, -135, 119, -1, 49, -1, 5, -141, -1, 35, -1, -177, -1, -1,
		-187, 30, -195, 51, 56, -1, -229, 100, 31, 3, 120, -257, -1, -263, -1, 28,
		33, 6, 50, 32, 2, 18, -1, 20, 21, 16, 17, -291, 14, 15, 19, 22,
		24, 23, -1, 13, -341, -1, -1, 57, 58, 59, -1, -379, 104, -1, 8, -411,
		52, 53, -417, 101, -1, 118, -1, -423, -1, 129, 10, -429, -1, 11, 12, -479,
		9, -521, -1, 62, 67, -1, -1, -529, -1, -1, 121, 125, 126, 124, -1, -1,
		115, 27, 38, -569, 65, 66, 61, -1, 60, 68, -1, -609, -1, -1, 132, 87,
		-1, 69, -661, -699, -739, -1, -1, -789, -819, 72, 88, 77, 76, -847, 122, -1,
		-1, 40, -887, 63, 103, -1, 80, -1, 131, -925, -1, -977, -1, -1, -1015, -1043,
		54, -1083, 75, -1123, 83, 74, 92, 93, 91, -1, -1173, 85, 78, -1225, -1, -1,
		45, 46, 47, 48, -1, 42, 43, 86, 130, 106, -1, -1, 89, -1253, 70, 73,
		-1305, -1, 79, 55, 123, 44, -1, 105, -1355, 84, 107, -1, -1, -2, -2
	};

	private static final short lapg_lalr[] = {
		11, -1, 16, 8, 19, 8, -1, -2, 19, -1, 16, 34, -1, -2, 1, -1,
		41, -1, 40, -1, 39, -1, 38, -1, 37, -1, 36, -1, 35, -1, 34, -1,
		33, -1, 32, -1, 31, -1, 2, -1, 6, -1, 17, -1, 28, -1, 0, 0,
		-1, -2, 0, 9, 1, 9, 2, 9, 15, 9, 17, 9, 18, 9, 20, 9,
		31, 9, 32, 9, 33, 9, 34, 9, 35, 9, 36, 9, 37, 9, 38, 9,
		39, 9, 40, 9, 41, 9, 14, 128, 19, 128, -1, -2, 1, -1, 41, -1,
		40, -1, 39, -1, 38, -1, 37, -1, 36, -1, 35, -1, 34, -1, 33, -1,
		32, -1, 31, -1, 2, -1, 4, -1, 5, -1, 17, -1, 29, -1, 30, -1,
		18, 116, -1, -2, 14, -1, 19, 127, -1, -2, 1, -1, 41, -1, 40, -1,
		39, -1, 38, -1, 37, -1, 36, -1, 35, -1, 34, -1, 33, -1, 32, -1,
		31, -1, 2, -1, 6, -1, 17, -1, 28, -1, 0, 0, -1, -2, 11, -1,
		9, 8, 16, 8, 19, 8, -1, -2, 19, -1, 9, 34, 16, 34, -1, -2,
		1, -1, 41, -1, 40, -1, 39, -1, 38, -1, 37, -1, 36, -1, 35, -1,
		34, -1, 33, -1, 32, -1, 31, -1, 2, -1, 6, -1, 28, -1, 0, 1,
		-1, -2, 28, -1, 1, 99, 31, 99, 32, 99, 33, 99, 34, 99, 35, 99,
		36, 99, 37, 99, 38, 99, 39, 99, 40, 99, 41, 99, -1, -2, 15, -1,
		18, 117, -1, -2, 1, -1, 41, -1, 40, -1, 39, -1, 38, -1, 37, -1,
		36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 20, 113, -1, -2,
		1, -1, 41, -1, 40, -1, 39, -1, 38, -1, 37, -1, 36, -1, 35, -1,
		34, -1, 33, -1, 32, -1, 31, -1, 14, -1, 15, -1, 17, -1, 18, -1,
		19, -1, 21, -1, 22, -1, 23, -1, 25, -1, 27, -1, 28, -1, 20, 25,
		-1, -2, 3, -1, 0, 36, 1, 36, 2, 36, 6, 36, 17, 36, 28, 36,
		31, 36, 32, 36, 33, 36, 34, 36, 35, 36, 36, 36, 37, 36, 38, 36,
		39, 36, 40, 36, 41, 36, -1, -2, 19, -1, 1, 102, 16, 102, 28, 102,
		31, 102, 32, 102, 33, 102, 34, 102, 35, 102, 36, 102, 37, 102, 38, 102,
		39, 102, 40, 102, 41, 102, -1, -2, 19, -1, 9, 34, -1, -2, 19, -1,
		9, 34, -1, -2, 15, -1, 20, 114, -1, -2, 1, -1, 41, -1, 40, -1,
		39, -1, 38, -1, 37, -1, 36, -1, 35, -1, 34, -1, 33, -1, 32, -1,
		31, -1, 14, -1, 15, -1, 17, -1, 18, -1, 19, -1, 21, -1, 22, -1,
		23, -1, 25, -1, 27, -1, 28, -1, 20, 26, -1, -2, 5, -1, 0, 37,
		1, 37, 2, 37, 6, 37, 17, 37, 19, 37, 28, 37, 31, 37, 32, 37,
		33, 37, 34, 37, 35, 37, 36, 37, 37, 37, 38, 37, 39, 37, 40, 37,
		41, 37, 43, 37, -1, -2, 37, -1, 13, 64, 15, 64, -1, -2, 1, -1,
		41, -1, 40, -1, 39, -1, 38, -1, 37, -1, 36, -1, 35, -1, 34, -1,
		33, -1, 32, -1, 31, -1, 2, -1, 6, -1, 19, -1, 28, -1, 43, -1,
		10, 71, 13, 71, -1, -2, 19, -1, 0, 39, 1, 39, 2, 39, 6, 39,
		17, 39, 28, 39, 31, 39, 32, 39, 33, 39, 34, 39, 35, 39, 36, 39,
		37, 39, 38, 39, 39, 39, 40, 39, 41, 39, 43, 39, -1, -2, 11, -1,
		16, -1, 1, 9, 2, 9, 6, 9, 10, 9, 13, 9, 19, 9, 23, 9,
		24, 9, 25, 9, 27, 9, 28, 9, 31, 9, 32, 9, 33, 9, 34, 9,
		35, 9, 36, 9, 37, 9, 38, 9, 39, 9, 40, 9, 41, 9, 43, 9,
		-1, -2, 1, -1, 41, -1, 40, -1, 39, -1, 38, -1, 37, -1, 36, -1,
		35, -1, 34, -1, 33, -1, 32, -1, 31, -1, 6, -1, 19, -1, 28, -1,
		43, -1, 10, 71, 13, 71, -1, -2, 1, -1, 41, -1, 40, -1, 39, -1,
		38, -1, 37, -1, 36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1,
		2, -1, 6, -1, 19, -1, 28, -1, 43, -1, 10, 71, 13, 71, -1, -2,
		23, -1, 24, -1, 25, -1, 27, -1, 1, 81, 2, 81, 6, 81, 10, 81,
		13, 81, 19, 81, 20, 81, 28, 81, 31, 81, 32, 81, 33, 81, 34, 81,
		35, 81, 36, 81, 37, 81, 38, 81, 39, 81, 40, 81, 41, 81, 43, 81,
		-1, -2, 28, -1, 1, 96, 31, 96, 32, 96, 33, 96, 34, 96, 35, 96,
		36, 96, 37, 96, 38, 96, 39, 96, 40, 96, 41, 96, 16, 99, -1, -2,
		28, -1, 1, 98, 31, 98, 32, 98, 33, 98, 34, 98, 35, 98, 36, 98,
		37, 98, 38, 98, 39, 98, 40, 98, 41, 98, -1, -2, 1, -1, 41, -1,
		40, -1, 39, -1, 38, -1, 37, -1, 36, -1, 35, -1, 34, -1, 33, -1,
		32, -1, 31, -1, 2, -1, 6, -1, 19, -1, 28, -1, 43, -1, 10, 71,
		13, 71, -1, -2, 43, -1, 0, 41, 1, 41, 2, 41, 6, 41, 17, 41,
		28, 41, 31, 41, 32, 41, 33, 41, 34, 41, 35, 41, 36, 41, 37, 41,
		38, 41, 39, 41, 40, 41, 41, 41, -1, -2, 11, -1, 1, 9, 2, 9,
		6, 9, 10, 9, 13, 9, 19, 9, 20, 9, 23, 9, 24, 9, 25, 9,
		27, 9, 28, 9, 31, 9, 32, 9, 33, 9, 34, 9, 35, 9, 36, 9,
		37, 9, 38, 9, 39, 9, 40, 9, 41, 9, 43, 9, -1, -2, 1, -1,
		41, -1, 40, -1, 39, -1, 38, -1, 37, -1, 36, -1, 35, -1, 34, -1,
		33, -1, 32, -1, 31, -1, 2, -1, 19, -1, 28, -1, 43, -1, 10, 94,
		20, 94, -1, -2, 28, -1, 1, 96, 31, 96, 32, 96, 33, 96, 34, 96,
		35, 96, 36, 96, 37, 96, 38, 96, 39, 96, 40, 96, 41, 96, -1, -2,
		1, -1, 41, -1, 40, -1, 39, -1, 38, -1, 37, -1, 36, -1, 35, -1,
		34, -1, 33, -1, 32, -1, 31, -1, 2, -1, 6, -1, 19, -1, 28, -1,
		43, -1, 10, 71, 13, 71, -1, -2, 1, -1, 41, -1, 40, -1, 39, -1,
		38, -1, 37, -1, 36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1,
		2, -1, 6, -1, 19, -1, 28, -1, 43, -1, 10, 71, 13, 71, -1, -2,
		23, -1, 24, -1, 25, -1, 27, -1, 1, 82, 2, 82, 6, 82, 10, 82,
		13, 82, 19, 82, 20, 82, 28, 82, 31, 82, 32, 82, 33, 82, 34, 82,
		35, 82, 36, 82, 37, 82, 38, 82, 39, 82, 40, 82, 41, 82, 43, 82,
		-1, -2, 11, -1, 16, -1, 1, 9, 2, 9, 6, 9, 10, 9, 13, 9,
		19, 9, 23, 9, 24, 9, 25, 9, 27, 9, 28, 9, 31, 9, 32, 9,
		33, 9, 34, 9, 35, 9, 36, 9, 37, 9, 38, 9, 39, 9, 40, 9,
		41, 9, 43, 9, -1, -2, 28, -1, 1, 97, 31, 97, 32, 97, 33, 97,
		34, 97, 35, 97, 36, 97, 37, 97, 38, 97, 39, 97, 40, 97, 41, 97,
		-1, -2, 11, -1, 1, 9, 2, 9, 6, 9, 10, 9, 13, 9, 19, 9,
		20, 9, 23, 9, 24, 9, 25, 9, 27, 9, 28, 9, 31, 9, 32, 9,
		33, 9, 34, 9, 35, 9, 36, 9, 37, 9, 38, 9, 39, 9, 40, 9,
		41, 9, 43, 9, -1, -2, 23, -1, 24, -1, 25, -1, 27, 90, 1, 90,
		2, 90, 6, 90, 10, 90, 13, 90, 19, 90, 20, 90, 28, 90, 31, 90,
		32, 90, 33, 90, 34, 90, 35, 90, 36, 90, 37, 90, 38, 90, 39, 90,
		40, 90, 41, 90, 43, 90, -1, -2, 1, -1, 41, -1, 40, -1, 39, -1,
		38, -1, 37, -1, 36, -1, 35, -1, 34, -1, 33, -1, 32, -1, 31, -1,
		2, -1, 19, -1, 28, -1, 43, -1, 10, 95, 20, 95, -1, -2
	};

	private static final short lapg_sym_goto[] = {
		0, 2, 45, 65, 68, 76, 86, 95, 95, 95, 98, 102, 110, 112, 116, 121,
		128, 135, 150, 156, 178, 186, 190, 194, 201, 204, 211, 212, 219, 242, 249, 256,
		300, 344, 388, 432, 476, 520, 564, 608, 652, 696, 740, 740, 752, 753, 754, 756,
		762, 791, 795, 797, 801, 804, 806, 810, 811, 812, 813, 815, 818, 819, 822, 823,
		825, 826, 828, 831, 834, 840, 851, 852, 863, 869, 884, 903, 914, 915, 922, 923,
		924, 926, 933, 940, 946, 958, 978, 980, 981, 985, 986, 987, 988, 989, 995, 996,
		997
	};

	private static final short lapg_sym_from[] = {
		203, 204, 0, 1, 5, 8, 14, 21, 25, 26, 31, 34, 37, 45, 46, 59,
		66, 70, 74, 84, 91, 101, 102, 103, 110, 111, 119, 125, 130, 131, 133, 141,
		149, 151, 154, 155, 157, 159, 161, 169, 175, 187, 193, 198, 200, 0, 1, 5,
		8, 14, 21, 25, 31, 34, 84, 102, 103, 110, 131, 141, 155, 159, 161, 175,
		200, 21, 68, 69, 1, 14, 21, 26, 84, 102, 110, 175, 1, 4, 14, 21,
		23, 84, 95, 102, 110, 175, 8, 25, 34, 103, 130, 131, 141, 159, 161, 77,
		104, 105, 128, 156, 174, 186, 2, 29, 86, 123, 143, 153, 170, 189, 86, 143,
		98, 101, 128, 174, 19, 26, 59, 66, 91, 26, 43, 59, 66, 87, 91, 98,
		28, 77, 86, 123, 134, 143, 170, 0, 1, 5, 8, 14, 21, 25, 26, 59,
		66, 84, 91, 102, 110, 175, 23, 26, 44, 59, 66, 91, 7, 18, 26, 32,
		59, 66, 75, 79, 82, 91, 103, 115, 125, 130, 131, 141, 155, 159, 161, 169,
		187, 200, 54, 66, 88, 92, 122, 156, 180, 186, 26, 59, 66, 91, 26, 59,
		66, 91, 26, 59, 66, 91, 132, 163, 192, 132, 163, 192, 26, 59, 66, 91,
		132, 163, 192, 125, 26, 59, 66, 91, 132, 163, 192, 8, 25, 26, 34, 38,
		59, 66, 91, 103, 125, 130, 131, 135, 136, 141, 155, 158, 159, 161, 169, 173,
		187, 200, 1, 14, 21, 84, 102, 110, 175, 1, 14, 21, 84, 102, 110, 175,
		0, 1, 5, 8, 14, 21, 25, 26, 31, 34, 37, 45, 46, 59, 66, 70,
		74, 84, 91, 101, 102, 103, 110, 111, 119, 124, 125, 130, 131, 133, 141, 149,
		151, 154, 155, 157, 159, 161, 169, 175, 187, 193, 198, 200, 0, 1, 5, 8,
		14, 21, 25, 26, 31, 34, 37, 45, 46, 59, 66, 70, 74, 84, 91, 101,
		102, 103, 110, 111, 119, 124, 125, 130, 131, 133, 141, 149, 151, 154, 155, 157,
		159, 161, 169, 175, 187, 193, 198, 200, 0, 1, 5, 8, 14, 21, 25, 26,
		30, 31, 34, 37, 45, 46, 59, 66, 70, 74, 84, 91, 101, 102, 103, 110,
		111, 119, 125, 130, 131, 133, 141, 149, 151, 154, 155, 157, 159, 161, 169, 175,
		187, 193, 198, 200, 0, 1, 5, 8, 14, 21, 25, 26, 30, 31, 34, 37,
		45, 46, 59, 66, 70, 74, 84, 91, 101, 102, 103, 110, 111, 119, 125, 130,
		131, 133, 141, 149, 151, 154, 155, 157, 159, 161, 169, 175, 187, 193, 198, 200,
		0, 1, 5, 8, 14, 21, 25, 26, 30, 31, 34, 37, 45, 46, 59, 66,
		70, 74, 84, 91, 101, 102, 103, 110, 111, 119, 125, 130, 131, 133, 141, 149,
		151, 154, 155, 157, 159, 161, 169, 175, 187, 193, 198, 200, 0, 1, 5, 8,
		14, 21, 25, 26, 30, 31, 34, 37, 45, 46, 59, 66, 70, 74, 84, 91,
		101, 102, 103, 110, 111, 119, 125, 130, 131, 133, 141, 149, 151, 154, 155, 157,
		159, 161, 169, 175, 187, 193, 198, 200, 0, 1, 5, 8, 14, 21, 25, 26,
		31, 34, 37, 45, 46, 59, 66, 70, 74, 84, 91, 97, 101, 102, 103, 110,
		111, 119, 125, 130, 131, 133, 141, 149, 151, 154, 155, 157, 159, 161, 169, 175,
		187, 193, 198, 200, 0, 1, 5, 8, 14, 21, 25, 26, 31, 34, 37, 45,
		46, 59, 66, 70, 74, 84, 91, 101, 102, 103, 110, 111, 119, 125, 130, 131,
		133, 141, 144, 149, 151, 154, 155, 157, 159, 161, 169, 175, 187, 193, 198, 200,
		0, 1, 5, 8, 14, 21, 25, 26, 31, 34, 37, 45, 46, 59, 66, 70,
		74, 84, 91, 101, 102, 103, 110, 111, 119, 125, 130, 131, 133, 141, 144, 149,
		151, 154, 155, 157, 159, 161, 169, 175, 187, 193, 198, 200, 0, 1, 5, 8,
		14, 21, 25, 26, 31, 34, 37, 45, 46, 59, 66, 70, 74, 84, 91, 101,
		102, 103, 110, 111, 119, 125, 130, 131, 133, 141, 144, 149, 151, 154, 155, 157,
		159, 161, 169, 175, 187, 193, 198, 200, 0, 1, 5, 8, 14, 21, 25, 26,
		31, 34, 37, 45, 46, 59, 66, 70, 74, 84, 91, 101, 102, 103, 110, 111,
		119, 125, 130, 131, 133, 141, 144, 149, 151, 154, 155, 157, 159, 161, 169, 175,
		187, 193, 198, 200, 103, 125, 130, 131, 141, 146, 155, 159, 161, 169, 187, 200,
		0, 0, 0, 5, 0, 5, 8, 25, 34, 37, 1, 14, 21, 70, 74, 84,
		101, 102, 103, 110, 119, 125, 130, 131, 133, 141, 149, 151, 154, 155, 157, 159,
		161, 169, 175, 187, 193, 198, 200, 7, 32, 79, 82, 26, 59, 26, 59, 66,
		91, 21, 68, 69, 0, 5, 0, 5, 8, 25, 115, 144, 4, 8, 25, 8,
		25, 34, 30, 8, 25, 34, 70, 70, 119, 74, 103, 141, 103, 141, 159, 103,
		141, 159, 103, 125, 130, 141, 159, 187, 103, 125, 130, 131, 141, 155, 159, 161,
		169, 187, 200, 125, 103, 125, 130, 131, 141, 155, 159, 161, 169, 187, 200, 8,
		25, 34, 103, 141, 159, 8, 25, 34, 103, 125, 130, 131, 136, 141, 155, 159,
		161, 169, 187, 200, 8, 25, 34, 38, 103, 125, 130, 131, 135, 136, 141, 155,
		158, 159, 161, 169, 173, 187, 200, 103, 125, 130, 131, 141, 155, 159, 161, 169,
		187, 200, 154, 1, 14, 21, 84, 102, 110, 175, 14, 45, 86, 143, 1, 14,
		21, 84, 102, 110, 175, 1, 14, 21, 84, 102, 110, 175, 103, 130, 131, 141,
		159, 161, 103, 125, 130, 131, 141, 146, 155, 159, 161, 169, 187, 200, 0, 1,
		5, 8, 14, 21, 25, 31, 34, 84, 102, 103, 110, 131, 141, 155, 159, 161,
		175, 200, 8, 25, 59, 7, 32, 79, 82, 95, 115, 146, 97, 103, 130, 131,
		141, 159, 161, 45, 14
	};

	private static final short lapg_sym_to[] = {
		205, 206, 2, 11, 2, 29, 11, 11, 29, 53, 75, 78, 78, 86, 89, 53,
		53, 96, 96, 11, 53, 96, 11, 123, 11, 143, 96, 153, 153, 153, 170, 123,
		96, 96, 96, 153, 189, 123, 153, 153, 11, 153, 96, 96, 153, 3, 3, 3,
		3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
		3, 47, 47, 47, 12, 12, 12, 54, 12, 12, 12, 12, 13, 22, 13, 13,
		50, 13, 114, 13, 13, 13, 30, 30, 30, 124, 124, 124, 124, 124, 124, 103,
		103, 141, 159, 187, 159, 198, 21, 69, 107, 149, 107, 149, 193, 193, 108, 108,
		118, 120, 160, 195, 46, 55, 55, 55, 55, 56, 84, 56, 56, 111, 56, 119,
		68, 68, 109, 150, 172, 109, 194, 4, 14, 4, 4, 14, 14, 4, 57, 57,
		57, 14, 57, 14, 14, 14, 51, 58, 85, 58, 58, 58, 26, 45, 59, 26,
		59, 59, 102, 26, 26, 59, 125, 144, 125, 125, 125, 125, 125, 125, 125, 125,
		125, 125, 90, 93, 112, 113, 148, 188, 197, 199, 60, 60, 60, 60, 61, 61,
		61, 61, 62, 62, 62, 62, 166, 166, 166, 167, 167, 167, 63, 63, 63, 63,
		168, 168, 168, 154, 64, 64, 64, 64, 169, 169, 169, 31, 31, 65, 31, 31,
		65, 65, 65, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31,
		31, 31, 15, 15, 15, 15, 15, 15, 15, 16, 16, 16, 16, 16, 16, 16,
		2, 11, 2, 29, 11, 11, 29, 53, 75, 78, 78, 86, 89, 53, 53, 96,
		96, 11, 53, 96, 11, 123, 11, 143, 96, 151, 153, 153, 153, 170, 123, 96,
		96, 96, 153, 189, 123, 153, 153, 11, 153, 96, 96, 153, 2, 11, 2, 29,
		11, 11, 29, 53, 75, 78, 78, 86, 89, 53, 53, 96, 96, 11, 53, 96,
		11, 123, 11, 143, 96, 152, 153, 153, 153, 170, 123, 96, 96, 96, 153, 189,
		123, 153, 153, 11, 153, 96, 96, 153, 2, 11, 2, 29, 11, 11, 29, 53,
		70, 75, 78, 78, 86, 89, 53, 53, 96, 96, 11, 53, 96, 11, 123, 11,
		143, 96, 153, 153, 153, 170, 123, 96, 96, 96, 153, 189, 123, 153, 153, 11,
		153, 96, 96, 153, 2, 11, 2, 29, 11, 11, 29, 53, 71, 75, 78, 78,
		86, 89, 53, 53, 96, 96, 11, 53, 96, 11, 123, 11, 143, 96, 153, 153,
		153, 170, 123, 96, 96, 96, 153, 189, 123, 153, 153, 11, 153, 96, 96, 153,
		2, 11, 2, 29, 11, 11, 29, 53, 72, 75, 78, 78, 86, 89, 53, 53,
		96, 96, 11, 53, 96, 11, 123, 11, 143, 96, 153, 153, 153, 170, 123, 96,
		96, 96, 153, 189, 123, 153, 153, 11, 153, 96, 96, 153, 2, 11, 2, 29,
		11, 11, 29, 53, 73, 75, 78, 78, 86, 89, 53, 53, 96, 96, 11, 53,
		96, 11, 123, 11, 143, 96, 153, 153, 153, 170, 123, 96, 96, 96, 153, 189,
		123, 153, 153, 11, 153, 96, 96, 153, 2, 11, 2, 29, 11, 11, 29, 53,
		75, 78, 78, 86, 89, 53, 53, 96, 96, 11, 53, 116, 96, 11, 123, 11,
		143, 96, 153, 153, 153, 170, 123, 96, 96, 96, 153, 189, 123, 153, 153, 11,
		153, 96, 96, 153, 2, 11, 2, 29, 11, 11, 29, 53, 75, 78, 78, 86,
		89, 53, 53, 96, 96, 11, 53, 96, 11, 123, 11, 143, 96, 153, 153, 153,
		170, 123, 176, 96, 96, 96, 153, 189, 123, 153, 153, 11, 153, 96, 96, 153,
		2, 11, 2, 29, 11, 11, 29, 53, 75, 78, 78, 86, 89, 53, 53, 96,
		96, 11, 53, 96, 11, 123, 11, 143, 96, 153, 153, 153, 170, 123, 177, 96,
		96, 96, 153, 189, 123, 153, 153, 11, 153, 96, 96, 153, 2, 11, 2, 29,
		11, 11, 29, 53, 75, 78, 78, 86, 89, 53, 53, 96, 96, 11, 53, 96,
		11, 123, 11, 143, 96, 153, 153, 153, 170, 123, 178, 96, 96, 96, 153, 189,
		123, 153, 153, 11, 153, 96, 96, 153, 2, 11, 2, 29, 11, 11, 29, 53,
		75, 78, 78, 86, 89, 53, 53, 96, 96, 11, 53, 96, 11, 123, 11, 143,
		96, 153, 153, 153, 170, 123, 179, 96, 96, 96, 153, 189, 123, 153, 153, 11,
		153, 96, 96, 153, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126,
		203, 5, 6, 24, 7, 7, 32, 32, 79, 82, 17, 17, 17, 97, 100, 17,
		121, 17, 127, 17, 97, 127, 127, 127, 171, 127, 183, 184, 185, 127, 171, 127,
		127, 127, 17, 127, 201, 202, 127, 27, 27, 27, 27, 66, 91, 67, 67, 94,
		94, 48, 95, 48, 8, 25, 9, 9, 33, 33, 145, 180, 23, 34, 34, 35,
		35, 80, 74, 36, 36, 36, 98, 99, 147, 101, 128, 174, 129, 129, 190, 130,
		130, 130, 131, 155, 161, 131, 131, 200, 132, 132, 132, 163, 132, 163, 132, 163,
		192, 132, 163, 156, 133, 157, 157, 157, 133, 157, 133, 157, 157, 157, 157, 37,
		37, 37, 134, 134, 134, 38, 38, 38, 135, 158, 158, 158, 173, 135, 158, 135,
		158, 158, 158, 158, 39, 39, 39, 83, 39, 39, 39, 39, 83, 39, 39, 39,
		83, 39, 39, 39, 83, 39, 39, 136, 136, 136, 136, 136, 136, 136, 136, 136,
		136, 136, 186, 204, 42, 49, 106, 122, 142, 196, 43, 87, 110, 175, 18, 18,
		18, 18, 18, 18, 18, 19, 19, 19, 19, 19, 19, 19, 137, 137, 137, 137,
		137, 137, 138, 138, 138, 138, 138, 181, 138, 138, 138, 138, 138, 138, 10, 20,
		10, 40, 20, 20, 40, 76, 81, 20, 20, 139, 20, 164, 139, 164, 139, 164,
		20, 164, 41, 52, 92, 28, 77, 104, 105, 115, 146, 182, 117, 140, 162, 165,
		140, 140, 191, 88, 44
	};

	private static final short lapg_rlen[] = {
		0, 1, 3, 2, 1, 2, 3, 1, 1, 1, 3, 3, 2, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 3, 1, 1, 2, 2,
		3, 3, 0, 1, 3, 0, 1, 0, 1, 0, 1, 7, 3, 1, 1, 1,
		1, 1, 2, 1, 2, 2, 5, 6, 1, 1, 1, 1, 4, 4, 1, 3,
		0, 1, 2, 1, 2, 1, 3, 0, 1, 3, 2, 2, 1, 1, 2, 3,
		2, 1, 2, 2, 4, 2, 3, 1, 1, 3, 3, 2, 2, 2, 1, 3,
		1, 2, 1, 1, 1, 2, 2, 5, 2, 4, 1, 3, 1, 1, 1, 1,
		1, 0, 1, 4, 0, 1, 3, 1, 1, 3, 3, 5, 1, 1, 1, 1,
		1, 3, 3, 2, 1, 1
	};

	private static final short lapg_rlex[] = {
		86, 86, 44, 44, 45, 45, 46, 46, 47, 48, 49, 49, 50, 50, 51, 51,
		51, 51, 51, 51, 51, 51, 51, 51, 51, 87, 87, 51, 52, 53, 53, 53,
		54, 54, 88, 88, 54, 89, 89, 90, 90, 91, 91, 54, 55, 56, 56, 56,
		56, 57, 57, 58, 58, 58, 59, 59, 59, 60, 60, 60, 61, 61, 62, 62,
		92, 92, 63, 64, 64, 65, 65, 93, 93, 66, 66, 66, 66, 66, 67, 67,
		67, 68, 68, 68, 69, 69, 69, 69, 69, 69, 69, 69, 69, 69, 70, 70,
		71, 71, 71, 72, 73, 73, 74, 74, 74, 75, 76, 76, 77, 77, 77, 77,
		77, 94, 94, 77, 95, 95, 77, 77, 78, 78, 79, 79, 80, 80, 80, 81,
		82, 82, 83, 83, 84, 85
	};

	protected static final String[] lapg_syms = new String[] {
		"eoi",
		"identifier",
		"error",
		"regexp",
		"scon",
		"icon",
		"'%'",
		"_skip",
		"_skip_comment",
		"'::='",
		"'|'",
		"'='",
		"'=>'",
		"';'",
		"'.'",
		"','",
		"':'",
		"'['",
		"']'",
		"'('",
		"')'",
		"'<'",
		"'>'",
		"'*'",
		"'+'",
		"'?'",
		"'?!'",
		"'&'",
		"'@'",
		"Ltrue",
		"Lfalse",
		"Lprio",
		"Lshift",
		"Linput",
		"Lleft",
		"Lright",
		"Lnonassoc",
		"Lnoeoi",
		"Lsoft",
		"Lclass",
		"Lspace",
		"Llayout",
		"Lreduce",
		"code",
		"input",
		"options",
		"option",
		"symbol",
		"reference",
		"type",
		"type_part_list",
		"type_part",
		"pattern",
		"lexer_parts",
		"lexer_part",
		"lexem_attrs",
		"lexem_attribute",
		"icon_list",
		"grammar_parts",
		"grammar_part",
		"priority_kw",
		"directive",
		"inputs",
		"inputref",
		"references",
		"rules",
		"rule0",
		"ruleprefix",
		"ruleparts",
		"rulepart",
		"ruleparts_choice",
		"ruleannotations",
		"annotations",
		"annotation_list",
		"annotation",
		"negative_la",
		"negative_la_clause",
		"expression",
		"expression_list",
		"map_entries",
		"map_separator",
		"name",
		"qualified_id",
		"rule_attrs",
		"command",
		"syntax_problem",
		"grammar_partsopt",
		"type_part_listopt",
		"typeopt",
		"iconopt",
		"lexem_attrsopt",
		"commandopt",
		"Lnoeoiopt",
		"rule_attrsopt",
		"map_entriesopt",
		"expression_listopt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 44;
		public static final int options = 45;
		public static final int option = 46;
		public static final int symbol = 47;
		public static final int reference = 48;
		public static final int type = 49;
		public static final int type_part_list = 50;
		public static final int type_part = 51;
		public static final int pattern = 52;
		public static final int lexer_parts = 53;
		public static final int lexer_part = 54;
		public static final int lexem_attrs = 55;
		public static final int lexem_attribute = 56;
		public static final int icon_list = 57;
		public static final int grammar_parts = 58;
		public static final int grammar_part = 59;
		public static final int priority_kw = 60;
		public static final int directive = 61;
		public static final int inputs = 62;
		public static final int inputref = 63;
		public static final int references = 64;
		public static final int rules = 65;
		public static final int rule0 = 66;
		public static final int ruleprefix = 67;
		public static final int ruleparts = 68;
		public static final int rulepart = 69;
		public static final int ruleparts_choice = 70;
		public static final int ruleannotations = 71;
		public static final int annotations = 72;
		public static final int annotation_list = 73;
		public static final int annotation = 74;
		public static final int negative_la = 75;
		public static final int negative_la_clause = 76;
		public static final int expression = 77;
		public static final int expression_list = 78;
		public static final int map_entries = 79;
		public static final int map_separator = 80;
		public static final int name = 81;
		public static final int qualified_id = 82;
		public static final int rule_attrs = 83;
		public static final int command = 84;
		public static final int syntax_problem = 85;
		public static final int grammar_partsopt = 86;
		public static final int type_part_listopt = 87;
		public static final int typeopt = 88;
		public static final int iconopt = 89;
		public static final int lexem_attrsopt = 90;
		public static final int commandopt = 91;
		public static final int Lnoeoiopt = 92;
		public static final int rule_attrsopt = 93;
		public static final int map_entriesopt = 94;
		public static final int expression_listopt = 95;
	}

	public interface Rules {
		public static final int lexer_part_group_selector = 32;  // lexer_part ::= '[' icon_list ']'
		public static final int lexer_part_alias = 33;  // lexer_part ::= identifier '=' pattern
		public static final int grammar_part_directive = 56;  // grammar_part ::= directive
	}

	protected final int lapg_next(int state) {
		int p;
		if (lapg_action[state] < -2) {
			for (p = -lapg_action[state] - 3; lapg_lalr[p] >= 0; p += 2) {
				if (lapg_lalr[p] == lapg_n.lexem) {
					break;
				}
			}
			return lapg_lalr[p + 1];
		}
		return lapg_action[state];
	}

	protected final int lapg_state_sym(int state, int symbol) {
		int min = lapg_sym_goto[symbol], max = lapg_sym_goto[symbol + 1] - 1;
		int i, e;

		while (min <= max) {
			e = (min + max) >> 1;
			i = lapg_sym_from[e];
			if (i == state) {
				return lapg_sym_to[e];
			} else if (i < state) {
				min = e + 1;
			} else {
				max = e - 1;
			}
		}
		return -1;
	}

	protected int lapg_head;
	protected LapgSymbol[] lapg_m;
	protected LapgSymbol lapg_n;
	protected LapgLexer lapg_lexer;

	private Object parse(LapgLexer lexer, int initialState, int finalState) throws IOException, ParseException {

		lapg_lexer = lexer;
		lapg_m = new LapgSymbol[1024];
		lapg_head = 0;
		int lapg_symbols_ok = 4;

		lapg_m[0] = new LapgSymbol();
		lapg_m[0].state = initialState;
		lapg_n = lapg_lexer.next();

		while (lapg_m[lapg_head].state != finalState) {
			int lapg_i = lapg_next(lapg_m[lapg_head].state);

			if (lapg_i >= 0) {
				reduce(lapg_i);
			} else if (lapg_i == -1) {
				shift();
				lapg_symbols_ok++;
			}

			if (lapg_i == -2 || lapg_m[lapg_head].state == -1) {
				if (restore()) {
					if (lapg_symbols_ok >= 4) {
						reporter.error(lapg_n.offset, lapg_n.endoffset, lapg_n.line,
								MessageFormat.format("syntax error before line {0}", lapg_lexer.getTokenLine()));
					}
					if (lapg_symbols_ok <= 1) {
						lapg_n = lapg_lexer.next();
					}
					lapg_symbols_ok = 0;
					continue;
				}
				if (lapg_head < 0) {
					lapg_head = 0;
					lapg_m[0] = new LapgSymbol();
					lapg_m[0].state = initialState;
				}
				break;
			}
		}

		if (lapg_m[lapg_head].state != finalState) {
			if (lapg_symbols_ok >= 4) {
				reporter.error(lapg_n.offset, lapg_n.endoffset, lapg_n.line,
						MessageFormat.format("syntax error before line {0}",
								lapg_lexer.getTokenLine()));
			}
			throw new ParseException();
		}
		return lapg_m[lapg_head - 1].sym;
	}

	protected boolean restore() {
		if (lapg_n.lexem == 0) {
			return false;
		}
		while (lapg_head >= 0 && lapg_state_sym(lapg_m[lapg_head].state, 2) == -1) {
			dispose(lapg_m[lapg_head]);
			lapg_m[lapg_head] = null;
			lapg_head--;
		}
		if (lapg_head >= 0) {
			lapg_m[++lapg_head] = new LapgSymbol();
			lapg_m[lapg_head].lexem = 2;
			lapg_m[lapg_head].sym = null;
			lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, 2);
			lapg_m[lapg_head].line = lapg_n.line;
			lapg_m[lapg_head].offset = lapg_n.offset;
			lapg_m[lapg_head].endoffset = lapg_n.endoffset;
			return true;
		}
		return false;
	}

	protected void shift() throws IOException {
		lapg_m[++lapg_head] = lapg_n;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, lapg_n.lexem);
		if (DEBUG_SYNTAX) {
			System.out.println(MessageFormat.format("shift: {0} ({1})", lapg_syms[lapg_n.lexem], lapg_lexer.current()));
		}
		if (lapg_m[lapg_head].state != -1 && lapg_n.lexem != 0) {
			lapg_n = lapg_lexer.next();
		}
	}

	protected void reduce(int rule) {
		LapgSymbol lapg_gg = new LapgSymbol();
		lapg_gg.sym = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head + 1 - lapg_rlen[rule]].sym : null;
		lapg_gg.lexem = lapg_rlex[rule];
		lapg_gg.state = 0;
		if (DEBUG_SYNTAX) {
			System.out.println("reduce to " + lapg_syms[lapg_rlex[rule]]);
		}
		LapgSymbol startsym = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head + 1 - lapg_rlen[rule]] : lapg_n;
		lapg_gg.line = startsym.line;
		lapg_gg.offset = startsym.offset;
		lapg_gg.endoffset = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head].endoffset : lapg_n.offset;
		applyRule(lapg_gg, rule, lapg_rlen[rule]);
		for (int e = lapg_rlen[rule]; e > 0; e--) {
			cleanup(lapg_m[lapg_head]);
			lapg_m[lapg_head--] = null;
		}
		lapg_m[++lapg_head] = lapg_gg;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, lapg_gg.lexem);
	}

	@SuppressWarnings("unchecked")
	protected void applyRule(LapgSymbol lapg_gg, int rule, int ruleLength) {
		switch (rule) {
			case 2:  // input ::= options lexer_parts grammar_partsopt
				  lapg_gg.sym = new AstRoot(((List<AstOptionPart>)lapg_m[lapg_head - 2].sym), ((List<AstLexerPart>)lapg_m[lapg_head - 1].sym), ((List<AstGrammarPart>)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 3:  // input ::= lexer_parts grammar_partsopt
				  lapg_gg.sym = new AstRoot(null, ((List<AstLexerPart>)lapg_m[lapg_head - 1].sym), ((List<AstGrammarPart>)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 4:  // options ::= option
				 lapg_gg.sym = new ArrayList<AstOptionPart>(16); ((List<AstOptionPart>)lapg_gg.sym).add(((AstOptionPart)lapg_m[lapg_head].sym)); 
				break;
			case 5:  // options ::= options option
				 ((List<AstOptionPart>)lapg_m[lapg_head - 1].sym).add(((AstOptionPart)lapg_m[lapg_head].sym)); 
				break;
			case 6:  // option ::= identifier '=' expression
				 lapg_gg.sym = new AstOption(((String)lapg_m[lapg_head - 2].sym), ((AstExpression)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 8:  // symbol ::= identifier
				 lapg_gg.sym = new AstIdentifier(((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 9:  // reference ::= identifier
				 lapg_gg.sym = new AstReference(((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 10:  // type ::= '(' scon ')'
				 lapg_gg.sym = ((String)lapg_m[lapg_head - 1].sym); 
				break;
			case 11:  // type ::= '(' type_part_list ')'
				 lapg_gg.sym = source.getText(lapg_m[lapg_head - 2].offset+1, lapg_m[lapg_head].endoffset-1); 
				break;
			case 28:  // pattern ::= regexp
				 lapg_gg.sym = new AstRegexp(((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 29:  // lexer_parts ::= lexer_part
				 lapg_gg.sym = new ArrayList<AstLexerPart>(64); ((List<AstLexerPart>)lapg_gg.sym).add(((AstLexerPart)lapg_m[lapg_head].sym)); 
				break;
			case 30:  // lexer_parts ::= lexer_parts lexer_part
				 ((List<AstLexerPart>)lapg_m[lapg_head - 1].sym).add(((AstLexerPart)lapg_m[lapg_head].sym)); 
				break;
			case 31:  // lexer_parts ::= lexer_parts syntax_problem
				 ((List<AstLexerPart>)lapg_m[lapg_head - 1].sym).add(((AstError)lapg_m[lapg_head].sym)); 
				break;
			case 32:  // lexer_part ::= '[' icon_list ']'
				 lapg_gg.sym = new AstGroupsSelector(((List<Integer>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 33:  // lexer_part ::= identifier '=' pattern
				 lapg_gg.sym = new AstNamedPattern(((String)lapg_m[lapg_head - 2].sym), ((AstRegexp)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 36:  // lexer_part ::= symbol typeopt ':'
				 lapg_gg.sym = new AstLexeme(((AstIdentifier)lapg_m[lapg_head - 2].sym), ((String)lapg_m[lapg_head - 1].sym), null, null, null, null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 43:  // lexer_part ::= symbol typeopt ':' pattern iconopt lexem_attrsopt commandopt
				 lapg_gg.sym = new AstLexeme(((AstIdentifier)lapg_m[lapg_head - 6].sym), ((String)lapg_m[lapg_head - 5].sym), ((AstRegexp)lapg_m[lapg_head - 3].sym), ((Integer)lapg_m[lapg_head - 2].sym), ((AstLexemAttrs)lapg_m[lapg_head - 1].sym), ((AstCode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 44:  // lexem_attrs ::= '(' lexem_attribute ')'
				 lapg_gg.sym = ((AstLexemAttrs)lapg_m[lapg_head - 1].sym); 
				break;
			case 45:  // lexem_attribute ::= Lsoft
				 lapg_gg.sym = new AstLexemAttrs(Lexem.KIND_SOFT, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 46:  // lexem_attribute ::= Lclass
				 lapg_gg.sym = new AstLexemAttrs(Lexem.KIND_CLASS, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 47:  // lexem_attribute ::= Lspace
				 lapg_gg.sym = new AstLexemAttrs(Lexem.KIND_SPACE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 48:  // lexem_attribute ::= Llayout
				 lapg_gg.sym = new AstLexemAttrs(Lexem.KIND_LAYOUT, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 49:  // icon_list ::= icon
				 lapg_gg.sym = new ArrayList<Integer>(4); ((List<Integer>)lapg_gg.sym).add(((Integer)lapg_m[lapg_head].sym)); 
				break;
			case 50:  // icon_list ::= icon_list icon
				 ((List<Integer>)lapg_m[lapg_head - 1].sym).add(((Integer)lapg_m[lapg_head].sym)); 
				break;
			case 51:  // grammar_parts ::= grammar_part
				 lapg_gg.sym = new ArrayList<AstGrammarPart>(64); ((List<AstGrammarPart>)lapg_gg.sym).add(((AstGrammarPart)lapg_m[lapg_head].sym)); 
				break;
			case 52:  // grammar_parts ::= grammar_parts grammar_part
				 ((List<AstGrammarPart>)lapg_m[lapg_head - 1].sym).add(((AstGrammarPart)lapg_m[lapg_head].sym)); 
				break;
			case 53:  // grammar_parts ::= grammar_parts syntax_problem
				 ((List<AstGrammarPart>)lapg_m[lapg_head - 1].sym).add(((AstError)lapg_m[lapg_head].sym)); 
				break;
			case 54:  // grammar_part ::= symbol typeopt '::=' rules ';'
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head - 4].sym), ((String)lapg_m[lapg_head - 3].sym), ((List<AstRule>)lapg_m[lapg_head - 1].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 55:  // grammar_part ::= annotations symbol typeopt '::=' rules ';'
				 lapg_gg.sym = new AstNonTerm(((AstIdentifier)lapg_m[lapg_head - 4].sym), ((String)lapg_m[lapg_head - 3].sym), ((List<AstRule>)lapg_m[lapg_head - 1].sym), ((AstAnnotations)lapg_m[lapg_head - 5].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 56:  // grammar_part ::= directive
				 lapg_gg.sym = lapg_m[lapg_head].sym; 
				break;
			case 60:  // directive ::= '%' priority_kw references ';'
				 lapg_gg.sym = new AstDirective(((String)lapg_m[lapg_head - 2].sym), ((List<AstReference>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 61:  // directive ::= '%' Linput inputs ';'
				 lapg_gg.sym = new AstInputDirective(((List<AstInputRef>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 62:  // inputs ::= inputref
				 lapg_gg.sym = new ArrayList<AstInputRef>(); ((List<AstInputRef>)lapg_gg.sym).add(((AstInputRef)lapg_m[lapg_head].sym)); 
				break;
			case 63:  // inputs ::= inputs ',' inputref
				 ((List<AstInputRef>)lapg_m[lapg_head - 2].sym).add(((AstInputRef)lapg_m[lapg_head].sym)); 
				break;
			case 66:  // inputref ::= reference Lnoeoiopt
				 lapg_gg.sym = new AstInputRef(((AstReference)lapg_m[lapg_head - 1].sym), ((String)lapg_m[lapg_head].sym) != null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 67:  // references ::= reference
				 lapg_gg.sym = new ArrayList<AstReference>(); ((List<AstReference>)lapg_gg.sym).add(((AstReference)lapg_m[lapg_head].sym)); 
				break;
			case 68:  // references ::= references reference
				 ((List<AstReference>)lapg_m[lapg_head - 1].sym).add(((AstReference)lapg_m[lapg_head].sym)); 
				break;
			case 69:  // rules ::= rule0
				 lapg_gg.sym = new ArrayList<AstRule>(); ((List<AstRule>)lapg_gg.sym).add(((AstRule)lapg_m[lapg_head].sym)); 
				break;
			case 70:  // rules ::= rules '|' rule0
				 ((List<AstRule>)lapg_m[lapg_head - 2].sym).add(((AstRule)lapg_m[lapg_head].sym)); 
				break;
			case 73:  // rule0 ::= ruleprefix ruleparts rule_attrsopt
				 lapg_gg.sym = new AstRule(((AstRulePrefix)lapg_m[lapg_head - 2].sym), ((List<AstRulePart>)lapg_m[lapg_head - 1].sym), ((AstRuleAttribute)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 74:  // rule0 ::= ruleparts rule_attrsopt
				 lapg_gg.sym = new AstRule(null, ((List<AstRulePart>)lapg_m[lapg_head - 1].sym), ((AstRuleAttribute)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 75:  // rule0 ::= ruleprefix rule_attrsopt
				 lapg_gg.sym = new AstRule(((AstRulePrefix)lapg_m[lapg_head - 1].sym), null, ((AstRuleAttribute)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 76:  // rule0 ::= rule_attrsopt
				 lapg_gg.sym = new AstRule(null, null, ((AstRuleAttribute)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 77:  // rule0 ::= syntax_problem
				 lapg_gg.sym = new AstRule(((AstError)lapg_m[lapg_head].sym)); 
				break;
			case 78:  // ruleprefix ::= annotations ':'
				 lapg_gg.sym = new AstRulePrefix(((AstAnnotations)lapg_m[lapg_head - 1].sym), null); 
				break;
			case 79:  // ruleprefix ::= ruleannotations identifier ':'
				 lapg_gg.sym = new AstRulePrefix(((AstRuleAnnotations)lapg_m[lapg_head - 2].sym), ((String)lapg_m[lapg_head - 1].sym)); 
				break;
			case 80:  // ruleprefix ::= identifier ':'
				 lapg_gg.sym = new AstRulePrefix(null, ((String)lapg_m[lapg_head - 1].sym)); 
				break;
			case 81:  // ruleparts ::= rulepart
				 lapg_gg.sym = new ArrayList<AstRulePart>(); ((List<AstRulePart>)lapg_gg.sym).add(((AstRulePart)lapg_m[lapg_head].sym)); 
				break;
			case 82:  // ruleparts ::= ruleparts rulepart
				 ((List<AstRulePart>)lapg_m[lapg_head - 1].sym).add(((AstRulePart)lapg_m[lapg_head].sym)); 
				break;
			case 83:  // ruleparts ::= ruleparts syntax_problem
				 ((List<AstRulePart>)lapg_m[lapg_head - 1].sym).add(((AstError)lapg_m[lapg_head].sym)); 
				break;
			case 84:  // rulepart ::= ruleannotations identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(((String)lapg_m[lapg_head - 2].sym), ((AstReference)lapg_m[lapg_head].sym), ((AstRuleAnnotations)lapg_m[lapg_head - 3].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 85:  // rulepart ::= ruleannotations reference
				 lapg_gg.sym = new AstRuleSymbol(null, ((AstReference)lapg_m[lapg_head].sym), ((AstRuleAnnotations)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 86:  // rulepart ::= identifier '=' reference
				 lapg_gg.sym = new AstRuleSymbol(((String)lapg_m[lapg_head - 2].sym), ((AstReference)lapg_m[lapg_head].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 87:  // rulepart ::= reference
				 lapg_gg.sym = new AstRuleSymbol(null, ((AstReference)lapg_m[lapg_head].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 89:  // rulepart ::= '(' ruleparts_choice ')'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 90:  // rulepart ::= rulepart '&' rulepart
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 91:  // rulepart ::= rulepart '?'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 92:  // rulepart ::= rulepart '*'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 93:  // rulepart ::= rulepart '+'
				 reporter.error(lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line, "unsupported, TODO"); 
				break;
			case 96:  // ruleannotations ::= annotation_list
				 lapg_gg.sym = new AstRuleAnnotations(null, ((List<AstNamedEntry>)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 97:  // ruleannotations ::= negative_la annotation_list
				 lapg_gg.sym = new AstRuleAnnotations(((AstNegativeLA)lapg_m[lapg_head - 1].sym), ((List<AstNamedEntry>)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 98:  // ruleannotations ::= negative_la
				 lapg_gg.sym = new AstRuleAnnotations(((AstNegativeLA)lapg_m[lapg_head].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 99:  // annotations ::= annotation_list
				 lapg_gg.sym = new AstAnnotations(((List<AstNamedEntry>)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 100:  // annotation_list ::= annotation
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(((AstNamedEntry)lapg_m[lapg_head].sym)); 
				break;
			case 101:  // annotation_list ::= annotation_list annotation
				 ((List<AstNamedEntry>)lapg_gg.sym).add(((AstNamedEntry)lapg_m[lapg_head].sym)); 
				break;
			case 102:  // annotation ::= '@' identifier
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head].sym), null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 103:  // annotation ::= '@' identifier '(' expression ')'
				 lapg_gg.sym = new AstNamedEntry(((String)lapg_m[lapg_head - 3].sym), ((AstExpression)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 104:  // annotation ::= '@' syntax_problem
				 lapg_gg.sym = new AstNamedEntry(((AstError)lapg_m[lapg_head].sym)); 
				break;
			case 105:  // negative_la ::= '(' '?!' negative_la_clause ')'
				 lapg_gg.sym = new AstNegativeLA(((List<AstReference>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 106:  // negative_la_clause ::= reference
				 lapg_gg.sym = new ArrayList<AstReference>(); ((List<AstReference>)lapg_gg.sym).add(((AstReference)lapg_m[lapg_head].sym)); 
				break;
			case 107:  // negative_la_clause ::= negative_la_clause '|' reference
				 ((List<AstReference>)lapg_gg.sym).add(((AstReference)lapg_m[lapg_head].sym)); 
				break;
			case 108:  // expression ::= scon
				 lapg_gg.sym = new AstLiteralExpression(((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 109:  // expression ::= icon
				 lapg_gg.sym = new AstLiteralExpression(((Integer)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 110:  // expression ::= Ltrue
				 lapg_gg.sym = new AstLiteralExpression(Boolean.TRUE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 111:  // expression ::= Lfalse
				 lapg_gg.sym = new AstLiteralExpression(Boolean.FALSE, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 115:  // expression ::= name '(' map_entriesopt ')'
				 lapg_gg.sym = new AstInstance(((AstName)lapg_m[lapg_head - 3].sym), ((List<AstNamedEntry>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 118:  // expression ::= '[' expression_listopt ']'
				 lapg_gg.sym = new AstArray(((List<AstExpression>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 120:  // expression_list ::= expression
				 lapg_gg.sym = new ArrayList(); ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head].sym)); 
				break;
			case 121:  // expression_list ::= expression_list ',' expression
				 ((List<AstExpression>)lapg_gg.sym).add(((AstExpression)lapg_m[lapg_head].sym)); 
				break;
			case 122:  // map_entries ::= identifier map_separator expression
				 lapg_gg.sym = new ArrayList<AstNamedEntry>(); ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head - 2].sym), ((AstExpression)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset)); 
				break;
			case 123:  // map_entries ::= map_entries ',' identifier map_separator expression
				 ((List<AstNamedEntry>)lapg_gg.sym).add(new AstNamedEntry(((String)lapg_m[lapg_head - 2].sym), ((AstExpression)lapg_m[lapg_head].sym), source, lapg_m[lapg_head - 2].offset, lapg_m[lapg_head].endoffset)); 
				break;
			case 127:  // name ::= qualified_id
				 lapg_gg.sym = new AstName(((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 129:  // qualified_id ::= qualified_id '.' identifier
				 lapg_gg.sym = ((String)lapg_m[lapg_head - 2].sym) + "." + ((String)lapg_m[lapg_head].sym); 
				break;
			case 130:  // rule_attrs ::= '%' Lprio reference
				 lapg_gg.sym = new AstPrioClause(((AstReference)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 131:  // rule_attrs ::= '%' Lshift
				 lapg_gg.sym = new AstShiftClause(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 132:  // command ::= code
				 lapg_gg.sym = new AstCode(source, lapg_m[lapg_head].offset+1, lapg_m[lapg_head].endoffset-1); 
				break;
			case 133:  // syntax_problem ::= error
				 lapg_gg.sym = new AstError(source, lapg_m[lapg_head].offset, lapg_m[lapg_head].endoffset); 
				break;
		}
	}

	/**
	 * disposes symbol dropped by error recovery mechanism
	 */
	protected void dispose(LapgSymbol sym) {
	}

	/**
	 * cleans node removed from the stack
	 */
	protected void cleanup(LapgSymbol sym) {
	}

	public AstRoot parseInput(LapgLexer lexer) throws IOException, ParseException {
		return (AstRoot) parse(lexer, 0, 205);
	}

	public AstExpression parseExpression(LapgLexer lexer) throws IOException, ParseException {
		return (AstExpression) parse(lexer, 1, 206);
	}
}
