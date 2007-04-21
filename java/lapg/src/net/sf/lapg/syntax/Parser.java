// Parser.java

package net.sf.lapg.syntax;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

import net.sf.lapg.lalr.IError;

public class Parser {
	
	private static final boolean DEBUG_SYNTAX = false;
	private static final int BITS = 32;
	
	byte[] buff = new byte[1025];
	int l, end;
	InputStream input;
	IError err;
	DescriptionCollector dc;
	
	int length;
	int[] rule = new int[128];
	
	String sourcename;
	
	private Parser( InputStream is, String sourceName, DescriptionCollector dc, IError err) {
		this.input = is;
		this.sourcename = sourceName;
		this.dc = dc;
		this.err = err;
	}
	
	static boolean parse( InputStream is, String sourceName, DescriptionCollector dc, IError err) {
		Parser p = new Parser(is, sourceName, dc, err);
		p.fillb();
		return p.parse();
	}
	
	String concat( String s1, String s2, String file, int line ) {
		if( s1 != null ) {
			if( line != -1 )
				return s1 + "\n#line "+line+" \""+file+"\"\n" + s2;
			else 
				return s1 + "\n" + s2;
		} else {
			return "#line "+line+" \""+file+"\"\n" + s2;
		}
	}
	
	void fillb() {
		l = 0;
		try {
			end = input.read( buff, 0, 1024 );
			if( end == -1 )
				end = 0;
		} catch( IOException ex ) {
			end = 0;
		}
		buff[end] = 0;
	}
	
	void error( String s ) {
		err.error(s);
	}

	public class lapg_place {
		public int line, column;

		public lapg_place( int line, int column ) {
			this.line = line;
			this.column = column;
		}
	};

	public class lapg_symbol {
		public Object sym;
		public int  lexem, state;
		public lapg_place pos;
	};

	private static final short[] lapg_char2no = new short[] {
		   0,   1,   1,   1,   1,   1,   1,   1,   1,   2,   3,   1,   1,   4,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   5,   1,   6,   7,   1,   8,   1,   9,  10,  11,   1,   1,  12,  13,  14,  15,
		  16,  17,  18,  19,  20,  21,  22,  23,  24,  25,  26,  27,  28,  29,  30,   1,
		   1,  31,  32,  33,  34,  35,  36,  37,  38,  39,  40,  41,  42,  43,  44,  45,
		  46,  47,  48,  49,  50,  51,  52,  53,  54,  55,  56,  57,  58,  59,   1,  60,
		   1,  61,  62,  63,  64,  65,  66,  67,  68,  69,  70,  71,  72,  73,  74,  75,
		  76,  77,  78,  79,  80,  81,  82,  83,  84,  85,  86,  87,  88,  89,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
	};

	private static final short[][] lapg_lexem = new short[][] {
		{  -2,  -1,   1,   2,   1,   1,   3,   4,  -1,   5,   6,  -1,   7,   8,   9,  10,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  12,  13,  14,  15,  -1,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  17,  18,  19,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  20,  21,  -1, },
		{ -12, -12,   1, -12,   1,   1, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, },
		{ -12, -12,  22, -12, -12, -12, -12, -12,  23, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, },
		{  -1,   3,   3,  -1,   3,   3,  24,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3,   3, },
		{ -12,   4,   4, -12,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4, },
		{  -1,  25,  25,  25,  25,  25,  25,  25,  25,  -1,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25, },
		{  -1,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  -1,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26, },
		{ -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, -21, },
		{  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  -1,  -1,  -1,  -1,  27,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1, },
		{ -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, },
		{  -1,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  28,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  29,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10, },
		{ -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, },
		{ -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18,  30, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, },
		{ -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, },
		{  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  31,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1, },
		{ -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, },
		{  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  -3,  -3,  -3,  -3,  -3,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  -3,  -3,  -3,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  -3,  -3,  -3, },
		{ -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, -22, },
		{  -5,  18,  18,  -5,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18, },
		{ -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, -23, },
		{  -1,  20,  20,  -1,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  -1,  20,  32, },
		{ -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, },
		{ -12, -12,  33, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, },
		{ -11, -11, -11, -11, -11, -11, -11, -11,  34, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, },
		{  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8, },
		{  -1,  25,  25,  25,  25,  25,  25,  25,  25,  35,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25, },
		{  -1,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  36,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26, },
		{ -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, },
		{  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4, },
		{  -1,  10,  10,  -1,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10, },
		{ -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19,  37, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, },
		{ -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, -24, },
		{  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7, },
		{  -6,  33,  33,  -6,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33,  33, },
		{  -2,  34,  34,  -2,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34,  34, },
		{  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3, },
		{  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9, },
		{ -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, },
	};

	private static final int[] lapg_action = new int[] {
		  -1,  -1,  -3,   7,  -1,  -1, -11,   6,  -1,  10,   8,   9,  14,  15,  -1, -17,
		  -1,  -1,  -1,  12,  17,  16, -25,  -1, -33,  30, -39,  -1, -49,  33,  -1,  38,
		  13,  -1, -57,  -1,  28,  -1,  58, -65,  34, -69,  -1,  -1, -85, -93,  31,  29,
		  -1,   1,   2,   3,  -1,   4,-107,  -1,  40,  52,-127,  -1,-147,  -1,  -1,-165,
		  20,-179,  45,  -1,  47,   5,  51,-191,  35,-207,  56,  42,-225,  36,  37,-243,
		-255,  46,  44,  39,-267,  -1,  41,-285,  55,  -1,  -2,
	};

	private static final short[] lapg_lalr = new short[] {
		  15,  -1,  20,  -1,   1,  11,  -1,  -2,  20,  -1,   1,  11,  -1,  -2,   1,  -1,
		   8,  -1,  21,  27,  -1,  -2,  18,  -1,  19,  32,  21,  32,  -1,  -2,  19,  -1,
		  21,  26,  -1,  -2,   7,  -1,  12,  57,  17,  57,  20,  57,  -1,  -2,   1,  -1,
		   9,  -1,   0,   0,  -1,  -2,   2,  -1,   1,  18,  20,  18,  -1,  -2,   1,  43,
		  -1,  -2,   3,  -1,   4,  -1,   5,  -1,  22,  -1,  13,  48,  14,  48,   1,  54,
		  -1,  -2,   2,  -1,   1,  19,  20,  19,  -1,  -2,   8,  -1,   1,  21,   3,  21,
		   4,  21,   5,  21,  20,  21,  -1,  -2,   3,  -1,   4,  -1,   5,  -1,  22,  -1,
		  11,  48,  13,  48,  14,  48,  21,  48,   1,  53,  -1,  -2,  22,  -1,   1,  48,
		   3,  48,   4,  48,   5,  48,  11,  48,  13,  48,  14,  48,  21,  48,  -1,  -2,
		   3,  -1,   4,  -1,   5,  -1,  22,  -1,  11,  48,  14,  48,  21,  48,   1,  54,
		  -1,  -2,   8,  -1,   1,  21,   3,  21,   4,  21,   5,  21,  20,  21,  -1,  -2,
		   3,  -1,   4,  -1,   5,  -1,   1,  23,  20,  23,  -1,  -2,   3,  -1,   4,  -1,
		   5,  -1,  22,  -1,  13,  48,  14,  48,   1,  54,  -1,  -2,   3,  -1,   4,  -1,
		   5,  -1,  11,  50,  13,  50,  14,  50,  21,  50,   1,  54,  -1,  -2,   3,  -1,
		   4,  -1,   5,  -1,  22,  -1,  11,  48,  14,  48,  21,  48,   1,  54,  -1,  -2,
		   3,  -1,   4,  -1,   5,  -1,   1,  25,  20,  25,  -1,  -2,   3,  -1,   4,  -1,
		   5,  -1,   1,  22,  20,  22,  -1,  -2,   3,  -1,   4,  -1,   5,  -1,  11,  49,
		  13,  49,  14,  49,  21,  49,   1,  53,  -1,  -2,   3,  -1,   4,  -1,   5,  -1,
		   1,  24,  20,  24,  -1,  -2,
	};

	private static final short[] lapg_sym_goto = new short[] {
		   0,   1,  15,  17,  28,  39,  50,  51,  53,  59,  61,  61,  65,  66,  67,  70,
		  72,  74,  75,  76,  77,  80,  83,  89,  90,  91,  92,  93,  94,  95, 106, 113,
		 115, 117, 119, 121, 124, 126, 128, 129, 130, 132, 134, 136, 137, 139, 141, 145,
		 146, 152, 156, 161,
	};

	private static final short[] lapg_sym_from = new short[] {
		  89,   1,   8,  15,  16,  17,  27,  28,  35,  37,  48,  52,  59,  67,  85,  34,
		  44,  41,  54,  60,  65,  71,  73,  76,  79,  80,  84,  87,  41,  54,  60,  65,
		  71,  73,  76,  79,  80,  84,  87,  41,  54,  60,  65,  71,  73,  76,  79,  80,
		  84,  87,   4,  18,  26,   4,   5,  14,  15,  45,  63,  16,  28,  42,  43,  61,
		  62,  30,  55,  55,  61,  67,   0,   2,  18,  33,  30,  22,  24,   2,   6,  30,
		  14,  23,  62,  41,  54,  58,  60,  71,  76,  48,   0,   0,   2,   6,  16,  41,
		  54,  60,  65,  71,  73,  76,  79,  80,  84,  87,  41,  60,  65,  71,  73,  76,
		  79,   0,   2,   2,   6,   2,   6,   8,  17,   5,  14,  15,   5,  15,  45,  63,
		  15,  15,  15,  37,  16,  28,  16,  28,  41,  42,  43,  16,  28,  41,  60,  71,
		  76,  39,  41,  54,  58,  60,  71,  76,  41,  60,  71,  76,  41,  60,  71,  73,
		  76,
	};

	private static final short[] lapg_sym_to = new short[] {
		  90,   4,  18,  22,  26,  18,  39,  26,  46,  22,  66,  68,  74,  81,  88,  45,
		  63,  49,  49,  49,  49,  49,  49,  49,  49,  49,  49,  49,  50,  50,  50,  50,
		  50,  50,  50,  50,  50,  50,  50,  51,  51,  51,  51,  51,  51,  51,  51,  51,
		  51,  51,  10,  33,  38,  11,  12,  12,  12,  64,  64,  27,  27,  60,  60,  76,
		  76,  41,  71,  72,  77,  82,   1,   1,  34,  44,  42,  35,  37,   5,  15,  43,
		  20,  36,  78,  52,  52,  52,  52,  52,  52,  67,  89,   2,   6,  16,  28,  53,
		  69,  53,  53,  53,  53,  53,  53,  69,  69,  69,  54,  54,  80,  54,  84,  54,
		  87,   3,   7,   8,  17,   9,   9,  19,  32,  13,  21,  13,  14,  14,  65,  79,
		  23,  24,  25,  47,  29,  40,  30,  30,  55,  61,  62,  31,  31,  56,  75,  83,
		  86,  48,  57,  70,  73,  57,  57,  57,  58,  58,  58,  58,  59,  59,  59,  85,
		  59,
	};

	private static final short[] lapg_rlen = new short[] {
		   4,   1,   1,   1,   1,   2,   2,   1,   3,   3,   1,   0,   2,   3,   1,   1,
		   2,   3,   2,   3,   1,   0,   5,   4,   6,   5,   1,   0,   3,   3,   1,   3,
		   1,   1,   2,   4,   4,   4,   1,   3,   1,   3,   2,   0,   5,   1,   2,   2,
		   0,   3,   2,   2,   1,   1,   0,   4,   2,   1,   2,
	};

	private static final short[] lapg_rlex = new short[] {
		  24,  29,  29,  29,  30,  30,  25,  25,  31,  31,  32,  32,  26,  26,  35,  36,
		  36,  33,  34,  34,  37,  37,  34,  34,  34,  34,  38,  38,  27,  39,  39,  40,
		  40,  28,  28,  41,  41,  41,  41,  43,  43,  44,  44,  47,  45,  23,  23,  48,
		  48,  46,  46,  46,  46,  50,  50,  49,  49,  42,  42,
	};

	private static final String[] lapg_syms = new String[] {
		"eoi",
		"id",
		"regexp",
		"cmd1",
		"cmd2",
		"cmd3",
		"str",
		"type",
		"Int",
		"'%'",
		"_skip",
		"'='",
		"'::='",
		"'|'",
		"';'",
		"'.'",
		"':'",
		"'::'",
		"'->'",
		"','",
		"'['",
		"']'",
		"'<<'",
		"idlist",
		"input",
		"directives",
		"lexical_part",
		"magazine_def",
		"grammar_part",
		"cmdx",
		"cmd",
		"directive",
		"newtypelexemopt",
		"newtypelexem",
		"lexemdef",
		"int32",
		"int32_list",
		"Intopt",
		"magazine_def_listopt",
		"magazine_def_list",
		"attrib",
		"ruledef",
		"def_rule_for",
		"rules_or",
		"rules_eq",
		"prioritydef",
		"stdrule",
		"{}",
		"rule_priority",
		"ids",
		"cmdopt",
	};

	public enum Tokens {
		eoi,
		id,
		regexp,
		cmd1,
		cmd2,
		cmd3,
		str,
		type,
		Int,
		PERC,
		_skip,
		EQ,
		COLONCOLONEQ,
		OR,
		SEMICOLON,
		DOT,
		COLON,
		COLONCOLON,
		MINUSGREATER,
		COMMA,
		LBRACKET,
		RBRACKET,
		LESSLESS,
		idlist,
		input,
		directives,
		lexical_part,
		magazine_def,
		grammar_part,
		cmdx,
		cmd,
		directive,
		newtypelexemopt,
		newtypelexem,
		lexemdef,
		int32,
		int32_list,
		Intopt,
		magazine_def_listopt,
		magazine_def_list,
		attrib,
		ruledef,
		def_rule_for,
		rules_or,
		rules_eq,
		prioritydef,
		stdrule,
		_sym47,
		rule_priority,
		ids,
		cmdopt,
	}

	private static int lapg_next( int state, int symbol ) {
		int p;
		if( lapg_action[state] < -2 ) {
			for( p = - lapg_action[state] - 3; lapg_lalr[p] >= 0; p += 2 )
				if( lapg_lalr[p] == symbol ) break;
			return lapg_lalr[p+1];
		}
		return lapg_action[state];
	}

	private static int lapg_state_sym( int state, int symbol ) {
		int min = lapg_sym_goto[symbol], max = lapg_sym_goto[symbol+1]-1;
		int i, e;

		while( min <= max ) {
			e = (min + max) >> 1;
			i = lapg_sym_from[e];
			if( i == state )
				return lapg_sym_to[e];
			else if( i < state )
				min = e + 1;
			else
				max = e - 1;
		}
		return -1;
	}

	public boolean parse() {

		byte[]        token = new byte[4096];
		int           lapg_head = 0, group = 0, lapg_i, lapg_size, chr;
		lapg_symbol[] lapg_m = new lapg_symbol[512];
		lapg_symbol   lapg_n;
		int           lapg_current_line = 1, lapg_current_column = 1;

		lapg_m[0] = new lapg_symbol();
		lapg_m[0].state = 0;
		chr = buff[l++];if( l == end ) fillb();

		do {
			lapg_n = new lapg_symbol();
			lapg_n.pos = new lapg_place( lapg_current_line, lapg_current_column );
			for( lapg_size = 0, lapg_i = group; lapg_i >= 0; ) {
				if( lapg_size < 4096-1 ) token[lapg_size++] = (byte)chr;
				lapg_i = lapg_lexem[lapg_i][lapg_char2no[chr]];
				if( lapg_i >= -1 && chr != 0 ) { 
					lapg_current_column++;
					if( chr == '\n' ) { lapg_current_column = 1; lapg_current_line++; }
					chr = buff[l++];if( l == end ) fillb();
				}
			}

			if( lapg_i == -1 ) {
				error( MessageFormat.format( "invalid lexem at line {0}, column {1}: `{2}`, skipped", lapg_n.pos.line, lapg_n.pos.column, new String(token,0,lapg_size) ) );
				lapg_n.lexem = -1;
				continue;
			}

			lapg_size--;
			lapg_n.lexem = -lapg_i-2;
			lapg_n.sym = null;

			switch( lapg_n.lexem ) {
				case 1:
					 lapg_n.sym = new String(token,0,lapg_size); break; 
				case 2:
					 lapg_n.sym = new String(token,1,lapg_size-2); break; 
				case 3:
					 lapg_n.sym = new String(token,1,lapg_size-1); break; 
				case 4:
					 lapg_n.sym = new String(token,3,lapg_size-3); break; 
				case 5:
					 lapg_n.sym = new String(token,1,lapg_size-2); break; 
				case 6:
					 lapg_n.sym = new String(token,1,lapg_size-2); break; 
				case 7:
					 lapg_n.sym = new String(token,1,lapg_size-2); break; 
				case 8:
					 lapg_n.sym = Integer.parseInt(new String(token,0,lapg_size)); break; 
				case 10:
					 continue; 
			}


			do {
				lapg_i = lapg_next( lapg_m[lapg_head].state, lapg_n.lexem );

				if( lapg_i >= 0 ) {
					lapg_symbol lapg_gg = new lapg_symbol();
					lapg_gg.sym = (lapg_rlen[lapg_i]!=0)?lapg_m[lapg_head+1-lapg_rlen[lapg_i]].sym:null;
					lapg_gg.lexem = lapg_rlex[lapg_i];
					lapg_gg.state = 0;
					if( DEBUG_SYNTAX )
						System.out.println( "reduce to " + lapg_syms[lapg_rlex[lapg_i]] );
					lapg_gg.pos = (lapg_rlen[lapg_i]!=0)?lapg_m[lapg_head+1-lapg_rlen[lapg_i]].pos:lapg_n.pos;
					switch( lapg_i ) {
						case 2:
							 lapg_gg.pos.line++; 
							break;
						case 4:
							lapg_gg.sym = concat( null, ((String)lapg_m[lapg_head-0].sym), sourcename, lapg_m[lapg_head-0].pos.line );
							break;
						case 5:
							lapg_gg.sym = concat( ((String)lapg_gg.sym), ((String)lapg_m[lapg_head-0].sym), sourcename, (lapg_m[lapg_head-1].pos.line+1!=lapg_m[lapg_head-0].pos.line)?lapg_m[lapg_head-0].pos.line:-1 );
							lapg_gg.pos = lapg_m[lapg_head-0].pos;
							break;
						case 8:
							dc.process_directive( ((String)lapg_m[lapg_head-1].sym), ((String)lapg_m[lapg_head-0].sym), lapg_m[lapg_head-2].pos.line, lapg_m[lapg_head-2].pos.column );
							break;
						case 9:
							dc.process_directive( ((String)lapg_m[lapg_head-1].sym), ((Integer)lapg_m[lapg_head-0].sym), lapg_m[lapg_head-2].pos.line, lapg_m[lapg_head-2].pos.column );
							break;
						case 14:
							 if( ((Integer)lapg_m[lapg_head-0].sym) < 0 || ((Integer)lapg_m[lapg_head-0].sym) >= BITS ) lapg_gg.sym = 0; else lapg_gg.sym = 1 << ((Integer)lapg_m[lapg_head-0].sym); 
							break;
						case 16:
							 lapg_gg.sym = ((Integer)lapg_gg.sym) | ((Integer)lapg_m[lapg_head-0].sym); 
							break;
						case 17:
							 dc.currentgroups = ((Integer)lapg_m[lapg_head-1].sym); 
							break;
						case 18:
							dc.terminal(((String)lapg_m[lapg_head-1].sym), null);
							break;
						case 19:
							dc.terminal(((String)lapg_m[lapg_head-2].sym),((String)lapg_m[lapg_head-1].sym));
							break;
						case 22:
							dc.lexem( dc.terminal(((String)lapg_m[lapg_head-4].sym), null), ((String)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-0].sym), ((Integer)lapg_m[lapg_head-1].sym) );
							break;
						case 23:
							dc.lexem( dc.terminal(((String)lapg_m[lapg_head-3].sym), null), ((String)lapg_m[lapg_head-1].sym), ((String)lapg_m[lapg_head-3].sym), null, ((Integer)lapg_m[lapg_head-0].sym) );
							break;
						case 24:
							dc.lexem( dc.terminal(((String)lapg_m[lapg_head-5].sym), ((String)lapg_m[lapg_head-4].sym)), ((String)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head-5].sym), ((String)lapg_m[lapg_head-0].sym), ((Integer)lapg_m[lapg_head-1].sym) );
							break;
						case 25:
							dc.lexem( dc.terminal(((String)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-3].sym)), ((String)lapg_m[lapg_head-1].sym), ((String)lapg_m[lapg_head-4].sym), null, ((Integer)lapg_m[lapg_head-0].sym) );
							break;
						case 31:
							dc.nonterm( ((String)lapg_m[lapg_head-0].sym), 0, null, dc.nonterm( ((String)lapg_m[lapg_head-2].sym), 0 ) );
							break;
						case 32:
							dc.nonterm( ((String)lapg_m[lapg_head-0].sym), 0, null, -1 );
							break;
						case 43:
							if( ((String)lapg_m[lapg_head-0].sym).equals("left")) lapg_gg.sym = 1;
							else if( ((String)lapg_m[lapg_head-0].sym).equals("right")) lapg_gg.sym = 2;
							else if( ((String)lapg_m[lapg_head-0].sym).equals("nonassoc")) lapg_gg.sym = 3;
							else { error("wrong priority declaration: %" + ((String)lapg_m[lapg_head-0].sym));lapg_gg.sym = 0; }
							break;
						case 45:
							 dc.addprio(((String)lapg_m[lapg_head-0].sym),((Integer)lapg_m[lapg_head-1].sym),false); 
							break;
						case 46:
							 dc.addprio(((String)lapg_m[lapg_head-0].sym),((Integer)lapg_m[lapg_head-2].sym),true); 
							break;
						case 47:
							 lapg_gg.sym = dc.nonterm( ((String)lapg_m[lapg_head-0].sym), 0 ); 
							break;
						case 48:
							 lapg_gg.sym = -1; 
							break;
						case 49:
							dc.rule( length, ((Integer)lapg_m[lapg_head-1].sym), ((String)lapg_m[lapg_head-0].sym), rule, lapg_gg.pos.line );
							break;
						case 50:
							dc.rule( length, ((Integer)lapg_m[lapg_head-0].sym), null, rule, lapg_gg.pos.line );
							break;
						case 51:
							dc.rule( 0, ((Integer)lapg_m[lapg_head-0].sym), ((String)lapg_m[lapg_head-1].sym), rule, lapg_gg.pos.line );
							break;
						case 52:
							dc.rule( 0, ((Integer)lapg_m[lapg_head-0].sym), null, rule, lapg_gg.pos.line );
							break;
						case 55:
							if( ((String)lapg_m[lapg_head-1].sym) != null ) {
								length += 2;
								rule[length] = dc.nonterm( ((String)lapg_m[lapg_head-0].sym), 0 );
								rule[length-1] = dc.nonterm( ((String)lapg_m[lapg_head-0].sym), 2, null, rule[length] );
								dc.rule( 0, -1, ((String)lapg_m[lapg_head-1].sym), new int[]{ rule[length-1] }, lapg_m[lapg_head-2].pos.line );
							} else rule[++length] = dc.nonterm( ((String)lapg_m[lapg_head-0].sym), 0 );
							break;
						case 56:
							length = 0;
							if( ((String)lapg_m[lapg_head-1].sym) != null) {
								length += 2;
								rule[length] = dc.nonterm( ((String)lapg_m[lapg_head-0].sym), 0 );
								rule[length-1] = dc.nonterm( ((String)lapg_m[lapg_head-0].sym), 2, null, rule[length] );
								dc.rule( 0, -1, ((String)lapg_m[lapg_head-1].sym), new int[]{ rule[length-1] }, lapg_m[lapg_head-1].pos.line );
							} else rule[++length] = dc.nonterm( ((String)lapg_m[lapg_head-0].sym), 0 );
							break;
						case 57:
							rule[0] = dc.nonterm( ((String)lapg_m[lapg_head-0].sym), 1 );
							break;
						case 58:
							rule[0] = dc.nonterm( ((String)lapg_m[lapg_head-1].sym), 1, ((String)lapg_m[lapg_head-0].sym), -2 );
							break;
					}
					for( int e = lapg_rlen[lapg_i]; e > 0; e-- ) 
						lapg_m[lapg_head--] = null;
					lapg_m[++lapg_head] = lapg_gg;
					lapg_m[lapg_head].state = lapg_state_sym( lapg_m[lapg_head-1].state, lapg_gg.lexem );
				} else if( lapg_i == -1 ) {
					lapg_m[++lapg_head] = lapg_n;
					lapg_m[lapg_head].state = lapg_state_sym( lapg_m[lapg_head-1].state, lapg_n.lexem );
					if( DEBUG_SYNTAX )
						System.out.println( MessageFormat.format( "shift: {0} ({1})", lapg_syms[lapg_n.lexem], new String(token,0,lapg_size) ) );
				}

			} while( lapg_i >= 0 && lapg_m[lapg_head].state != -1 );

			if( (lapg_i == -2 || lapg_m[lapg_head].state == -1) && lapg_n.lexem != 0 ) {
				break;
			}

		} while( lapg_n.lexem != 0 );

		if( lapg_m[lapg_head].state != 91-1 ) {
			error( MessageFormat.format( "syntax error before line {0}, column {1}", lapg_n.pos.line, lapg_n.pos.column ) );
			return false;
		};
		return true;
	}
}
