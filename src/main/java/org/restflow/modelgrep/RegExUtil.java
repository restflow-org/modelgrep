package org.restflow.modelgrep;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExUtil {
	public static final String S_WHITE_SPACE_REGEX = "\\s*";
	public static final String S_SOME_WHITE_SPACE = "{/\\s+/}";	
	
	static public String escapeRegExpressionChars(String text) {		
		String regex= Matcher.quoteReplacement(text);
		
/*		String regex;
		regex = text.replaceAll("\\\\","\\\\\\\\");
		regex = regex.replaceAll("\\$","\\\\\\$");
*/
		regex = regex.replaceAll("\\[","\\\\\\[");
		regex = regex.replaceAll("\\]","\\\\\\]");	
		regex = regex.replaceAll("\\.","\\\\\\.");	
		regex = regex.replaceAll("\\?","\\\\\\?");	
		regex = regex.replaceAll("\\^","\\\\\\^");
		regex = regex.replaceAll("\\(","\\\\\\(");
		regex = regex.replaceAll("\\)","\\\\\\)");		
		regex = regex.replaceAll("\\+","\\\\\\+");
		regex = regex.replaceAll("\\*","\\\\\\*");
		regex = regex.replaceAll("\\:","\\\\\\:");
		regex = regex.replaceAll("\\{","\\\\\\{");
		regex = regex.replaceAll("\\}","\\\\\\}");		
		regex = regex.replaceAll("\\~","\\\\\\~");
		regex = regex.replaceAll("\\-","\\\\\\-");
		regex = regex.replaceAll("\\!","\\\\\\!");		
		regex = regex.replaceAll("\\@","\\\\\\@");				
		regex = regex.replaceAll("\\%","\\\\\\%");		
		regex = regex.replaceAll("\\#","\\\\\\#");
		regex = regex.replaceAll("\\&","\\\\\\&");				
		regex = regex.replaceAll("\\=","\\\\\\=");				
		regex = regex.replaceAll("\\\"","\\\\\\\"");
		regex = regex.replaceAll("\\|","\\\\\\|");		
		regex = regex.replaceAll("\\<","\\\\\\<");
		regex = regex.replaceAll("\\>","\\\\\\>");
		//regex = regex.replaceAll("\n","\\s+");		
		
		return regex;
	}

	
	static public String escapeRegExpressionCharsAndAbsorbWhitespace(String text, String absorbWhiteSpaceSymbol) {

		if (absorbWhiteSpaceSymbol == null ) {
			return RegExUtil.escapeRegExpressionChars(text);
		} else {
			Pattern p = Pattern.compile( S_WHITE_SPACE_REGEX + RegExUtil.escapeRegExpressionChars(absorbWhiteSpaceSymbol) +S_WHITE_SPACE_REGEX );
			Matcher matcher = p.matcher(text);
			StringBuffer sb = new StringBuffer();
			while (matcher.find() ) {
				
				StringBuffer unmatchedText = new StringBuffer();
				matcher.appendReplacement(unmatchedText, "");
				sb.append( RegExUtil.escapeRegExpressionChars( unmatchedText.toString()) );
				if (matcher.group().length() > 1 ) {
					sb.append( S_SOME_WHITE_SPACE) ;
				} else {
					sb.append( ModelGrep.S_WHITE_SPACE) ;
				}
			}
			StringBuffer moreText = new StringBuffer();
			matcher.appendTail(moreText );
			String moreTextStr = moreText.toString(); 
			moreTextStr=RegExUtil.escapeRegExpressionChars(moreText.toString());
			sb.append(moreTextStr);
			return sb.toString();
		}
		
	}
	
	
	static public String unprotectRegex(String protectedRegex) {
		Pattern p;
		Matcher matcher;
		StringBuffer sb;
		
		StringBuffer patternStrBuf = new StringBuffer();
		patternStrBuf.append( ModelGrep.S_PROTECTED_REGEX );
		
		p = Pattern.compile(patternStrBuf.toString());
		
		matcher = p.matcher(protectedRegex);
		sb = new StringBuffer();
		while (matcher.find() ) {
			String regex = matcher.group(1);
			matcher.appendReplacement(sb, "");
			sb.append( regex );
		}
		matcher.appendTail(sb);
		return sb.toString();
	}
	
}
