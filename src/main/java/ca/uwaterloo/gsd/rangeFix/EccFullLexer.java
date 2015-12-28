package ca.uwaterloo.gsd.rangeFix;
import java.io.*;
import java.util.*;

interface IEccFullInternalLexer extends EccFullParser.Lexer {
	public void setLocation(int line, int column);

}
	
public class EccFullLexer implements EccFullParser.Lexer {
	private EccFullInternalLexer lexer;
	public EccFullLexer(EccFullInternalLexer l) {
		this.lexer = l;
		curlexer = lexer;
	}
	
	public EccFullLexer(Reader r) {
		this.lexer = new EccFullInternalLexer(r);
		curlexer = lexer;
	}	
	
    @Override 
	public Position getStartPos() {
		if (lastToken == null) return new Position(-1, -1);
		return lastToken.getLoc().getBegin();
	}

    @Override 
	public Position getEndPos() { 
		if (lastToken == null) return new Position(-1, -1);
		return lastToken.getLoc().getEnd(); 
	}

    @Override 
	public Object getLVal() { return lastToken; }

	private EccFullParser.Lexer curlexer;
	private int next = 0;
	private Token nextToken = null;
	private Token lastToken = null;
	// private boolean prefetched = false;
	private int previous;
	
	@Override 
	public int yylex() throws java.io.IOException {
		int result;
		// if (prefetched){
			// result = next;
			// lastToken = nextToken;
			// prefetched = false;
		// }
		// else {
			// result = curlexer.yylex();
			// lastToken = (Token)curlexer.getLVal();
		// }
		result = curlexer.yylex();
		lastToken = (Token)curlexer.getLVal();
		if (result == EccFullParser.EOF && curlexer != lexer)
		{
			result = next;
			lastToken = nextToken;
			curlexer = lexer;
		}
		if (result == -1 && curlexer != lexer)
		{
			lexer.yyerror(new Location(curlexer.getStartPos(), curlexer.getEndPos()), "unparsable text");
		}
		if (result == EccFullParser.LINETEXT && curlexer == lexer) {
			next = curlexer.yylex();
			nextToken = (Token)curlexer.getLVal();
			IEccFullInternalLexer newLexer;
			if (previous == EccFullParser.DEFAULT) {
				if (next == EccFullParser.CALCULATIONINDICATOR || next == EccFullParser.FOLLOWINDICATOR){
					newLexer = new EccFullInternalLexer(new StringReader(lastToken.getText()));
				}
				else {
					newLexer = new EccFullValuesLexer(new StringReader(lastToken.getText()));
				}
			}
			else {
				newLexer = new EccFullValuesLexer(new StringReader(lastToken.getText()));
			}
			Position pos = lastToken.getLoc().getBegin();
			newLexer.setLocation(pos.getLine() - 1, pos.getColumn() - 1);
			curlexer = newLexer;
			return yylex();
		}
		// if (result == EccFullParser.DEFAULT && curlexer == lexer) {
			// int followingDefaultState = curlexer.yylex();
			// Token followingDefaultToken = (Token)curlexer.getLVal();
			// if (followingDefaultState != EccFullParser.LINETEXT){
				// prefetched = true;
			// }
			// else {
				// next = curlexer.yylex();
				// nextToken = (Token)curlexer.getLVal();
				// if (next == EccFullParser.CALCULATIONINDICATOR || next == EccFullParser.FOLLOWINDICATOR){
					// EccFullInternalLexer newLexer = new EccFullInternalLexer(new StringReader(followingDefaultToken .getText()));
					// Position pos = followingDefaultToken.getLoc().getBegin();
					// newLexer.setLocation(pos.getLine() - 1, pos.getColumn() - 1);
					// curlexer = newLexer;
				// }
				// else {
					// EccFullValuesLexer newLexer = new EccFullValuesLexer(new StringReader(followingDefaultToken.getText()));
					// Position pos = followingDefaultToken.getLoc().getBegin();
					// newLexer.setLocation(pos.getLine() - 1, pos.getColumn() - 1);
					// curlexer = newLexer;
				// }
			// }
		// }
		previous = result;
		if (result == EccFullParser.CALCULATIONINDICATOR || result == EccFullParser.FOLLOWINDICATOR) return yylex();
		return result;
	}

    public void yyerror(EccFullParser.Location loc, String s) {
		lexer.yyerror(loc, s);
	}
	
	public Collection<CompileError> getAllErrors() { return lexer._allErrors; }

}