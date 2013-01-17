package org.restflow.modelgrep;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ModelGrep  {
    protected final Log logger = LogFactory.getLog(getClass());
    
	public static final String S_PROTECTED_REGEX = "\\{/([^/]+)/\\}";
	public static final String S_TOKEN_PREFIX = "{";	
	public static final String S_TOKEN_SUFFIX = "}";

	public static final String S_TOKEN_REG_EX_PREFIX = "{/";	
	public static final String S_TOKEN_REG_EX_SUFFIX = "/}";	
	
	public static final String S_VAR_TYPE_SEPARATOR = ":";	
	
	public static final String S_STRING_REGEX = "\\S+";
	public static final String S_INT_REGEX = "[-+]?\\d+";
	
	public static final String S_STRING_TEXT_BLOCK_REGEX = "(?s).+?";
	
	public static final String S_STRING_FLOAT_REGEX = "[-+]?[0-9]*\\.?[0-9]+(?:[eE][-+]?[0-9]+)?";


	public static final String S_WHITE_SPACE = "{/\\s*/}";


	public String _tokenMatcherPrefix = "{";
	public String _tokenMatcherSuffix = "}";
	
	public static final String S_VARIABLE_TYPE_MATCHER ="(\\w*(?:\\[\\])*)\\:(\\w+)";
	public static final String S_VARIABLE_MATCHER ="(\\w+)";
	
	public static final String S_TEMPLATE_FILL_TOKEN = "\\{\\$(\\w+)\\}";
	public String absorbWhiteSpaceSymbol = null;

	//set by the user
	private List<String> template;
	private Map<String,String> tags = new HashMap<String,String>();
	private Map<String,String> fillModel;

	private List<RegularExpressionWithModel> compiledTemplate;

	/**
	 * Constructor automatically adds default tags
	 */
	public ModelGrep() {
		super();
		addDefaultTags();
	}

	public void addDefaultTags() {
		tags.put("STRING",  S_TOKEN_REG_EX_PREFIX + S_STRING_REGEX + S_TOKEN_REG_EX_SUFFIX);
		tags.put("INT", S_TOKEN_REG_EX_PREFIX + S_INT_REGEX + S_TOKEN_REG_EX_SUFFIX);		
		tags.put("TEXT_BLOCK", S_TOKEN_REG_EX_PREFIX + S_STRING_TEXT_BLOCK_REGEX + S_TOKEN_REG_EX_SUFFIX);				
		tags.put("FLOAT", S_TOKEN_REG_EX_PREFIX + S_STRING_FLOAT_REGEX + S_TOKEN_REG_EX_SUFFIX);						
		tags.put("_", S_WHITE_SPACE );
	}

	public void compile() throws Exception {
		addDefaultTags();
		Map<String,String> tags = getTags();
		for (String key: tags.keySet()) {
			tags.put(key, tags.get(key).trim());
		}
		
		compiledTemplate = new Vector<RegularExpressionWithModel>();
		
		for (String e: template ) {
			RegularExpressionWithModel p2m = createRegex(e);
			compiledTemplate.add(p2m);
			logger.debug(p2m.unprotectedRegex);
		}
	}
	
	public Map<String,Object> search(String text) throws Exception {
		Map<String,Object> model = new HashMap<String,Object>();
		return search (model, text);
	}
	
	public Map<String,Object> search(Map<String,Object> bean, String text) throws Exception {

		for (RegularExpressionWithModel p2m : compiledTemplate) {
			Matcher matcher = p2m.pattern.matcher(text);

			int index = 0;
			while (matcher.find()) {
				assignMatchesToBean(matcher, 0, bean, p2m );
			}
		}
		return bean;
	}
	
	public int assignMatchesToBean(Matcher matcher, int startIndex,
			Map<String,Object> bean, RegularExpressionWithModel model) throws Exception {
		
		List<RegularExpressionWithModel> innerModelList = model.model;

		int matchIndex = startIndex;

		logMatchedGroups(model, matcher);
		
		for (RegularExpressionWithModel innerModel : innerModelList) {
			matchIndex = assignMatchToBean(matcher, matchIndex, innerModel, bean );
		}
		
		return matchIndex;
	}

	private int assignMatchToBean(Matcher matcher, int matchIndex, RegularExpressionWithModel innerModel, Map<String,Object> bean ) throws Exception {
		if (innerModel.variableName.endsWith("[]")) {
			String variableName = innerModel.variableName.substring(0,innerModel.variableName.length()-2);
			
			if ( !innerModel.hasEmbeddedTags() ) {
				List<String> innerList = (List<String>)bean.get(variableName);
				if (innerList == null) {
					innerList= new Vector<String>();					
				}
				
				bean.put(variableName, innerList);
				return searchAndAddStringMatchesToList(innerList, matchIndex, innerModel,matcher.group(0));				
			} else {
				//looking for a repeated match without a tag or variable
				List<Map<String,Object>> innerList = (List<Map<String,Object>>)bean.get(variableName);
				if (innerList == null) {
					innerList= new Vector<Map<String,Object>>();					
				}

				bean.put(variableName, innerList);
				return searchAndAddMatchesToBeanList(innerList, matchIndex, innerModel,matcher.group(0));
			}
		} else {

			if ( !innerModel.hasEmbeddedTags() ) {
				String matchContents = matcher.group( 1 + matchIndex);
				bean.put(innerModel.variableName, matchContents);
				matchIndex++;
				return matchIndex;
			}
			
			if (innerModel.variableName.equals("..") ) {
				//matchIndex = searchAndAddMatchesToBeanProperties(bean, 0, innerModel, matcher.group(0));
				matchIndex = assignMatchesToBean(matcher, matchIndex, bean, innerModel);
				//matchIndex++;
				return matchIndex;
			}
			
			Map<String,Object> innerBean = new HashMap<String,Object>();
			bean.put(innerModel.variableName, innerBean);

			return assignMatchesToBean(matcher, matchIndex + 1,
					innerBean, innerModel);
		}
	}
	
	private void logMatchedGroups(RegularExpressionWithModel model, Matcher matcher) {
		List<String> groups = new Vector<String>();
		for (int cnt =0; cnt <matcher.groupCount(); cnt++) {
			groups.add(matcher.group(cnt));
		}
		
		logger.debug(model.variableName +":" + groups);
		
	}
	
	public int searchAndAddMatchesToBeanList(List<Map<String,Object>> beanList,int outerMatchIndex, RegularExpressionWithModel model, String text ) throws Exception {
		int innerMatchIndex = 0;
		int innerMatchNum= 0;
		
		Matcher matcher = model.pattern.matcher(text);

		while (matcher.find()) {
			Map<String,Object> bean = new HashMap<String,Object>();
			
			innerMatchNum = assignMatchesToBean(matcher, 0, bean, model );
			innerMatchIndex += innerMatchNum;
			beanList.add(bean);
		}
		return outerMatchIndex + innerMatchNum;
	}

	public int searchAndAddStringMatchesToList(List<String> beanList,int outerMatchIndex, RegularExpressionWithModel model, String text ) throws Exception {
		int innerMatchIndex = 0;
		
		Matcher matcher = model.pattern.matcher(text);
				
		while (matcher.find()) {
			innerMatchIndex ++;
			beanList.add(matcher.group());
		}
		return outerMatchIndex + innerMatchIndex;
	}

	public RegularExpressionWithModel createRegex(String text) throws Exception {
		RegularExpressionWithModel regex = new RegularExpressionWithModel();
		regex.searchText = text;
		buildProtectedRegex(regex);
		regex.unprotectedRegex = RegExUtil.unprotectRegex(regex.protectedRegex);
		regex.pattern = Pattern.compile(regex.unprotectedRegex);
		return regex;
	}
	
	
	public RegularExpressionWithModel buildProtectedRegex(RegularExpressionWithModel regex) throws Exception {
		List<RegularExpressionWithModel> models = new Vector<RegularExpressionWithModel>();
		regex.model = models;
		
		String text = regex.searchText;
		Pattern p;
		Matcher matcher;
		StringBuffer sb;
	
		int index;
		
		StringBuffer patternStrBuf = new StringBuffer();
		
		

		patternStrBuf.append(RegExUtil.escapeRegExpressionChars(_tokenMatcherPrefix));
		patternStrBuf.append(S_VARIABLE_TYPE_MATCHER );
		patternStrBuf.append(RegExUtil.escapeRegExpressionChars(_tokenMatcherSuffix));

		patternStrBuf.append("|");

		patternStrBuf.append(RegExUtil.escapeRegExpressionChars(_tokenMatcherPrefix));
		patternStrBuf.append(S_VARIABLE_MATCHER );
		patternStrBuf.append(RegExUtil.escapeRegExpressionChars(_tokenMatcherSuffix));
		

		patternStrBuf.append("|");
		patternStrBuf.append(S_TEMPLATE_FILL_TOKEN);
		patternStrBuf.append("|");
		patternStrBuf.append(S_PROTECTED_REGEX);
			
		p = Pattern.compile(patternStrBuf.toString());
		
		
		matcher = p.matcher(text);
		index = 0;
		sb = new StringBuffer();
		while (matcher.find() ) {
			String matchText= matcher.group(0);
			String varText = matcher.group(1);
			String typeText = matcher.group(2);			
			String varOnlyText = matcher.group(3);
			String fillModelText = matcher.group(4);
			String protectedRegex = matcher.group(5);
			
			//skip processing if we have found a protected regular expression
			if (protectedRegex != null && !protectedRegex.equals("")) {
				matcher.appendReplacement(sb, "" );
				sb.append(matchText);
				continue;
			}
			
			int start = matcher.start();
			int end = matcher.end();	

			String boundedLeftChar ="";
			String boundedRightChar ="";


			StringBuffer moreText = new StringBuffer();
			matcher.appendReplacement(moreText, "" );

			String moreTextStr = moreText.toString(); 
			
			moreTextStr=RegExUtil.escapeRegExpressionCharsAndAbsorbWhitespace(moreText.toString(),getAbsorbWhiteSpaceSymbol());
			sb.append(moreTextStr);
			
	
			if (fillModelText != null && !fillModelText.equals("") && fillModel != null ) {
				String fillText = fillModel.get( fillModelText );

				if (fillText != null ) {
					sb.append( fillText);
				}
			} else {
				if ( typeText == null || typeText.equals("")) {
					typeText = "STRING";
				}
				
				String typeExpression =  tags.get(typeText);
				if ( typeExpression == null ) throw new Exception ("could not find regular expression for tag "+ typeText );
				
				sb.append(boundedLeftChar);
				
				String variableName = null;
				if ( varText != null && !varText.equals("") ) {
					variableName = varText;
				}
				if ( varOnlyText != null && !varOnlyText.equals("") ) {
					variableName = varOnlyText;
				}
				
				if ( variableName == null ) {
					RegularExpressionWithModel innerRegex = new RegularExpressionWithModel();
					innerRegex.searchText = typeExpression;
					buildProtectedRegex(innerRegex);
					//TODO
					if ( innerRegex.hasEmbeddedTags() ) {
						//reference to tag without variable...resolves to tag with embedded variables
						innerRegex.unprotectedRegex=RegExUtil.unprotectRegex(innerRegex.protectedRegex);
						innerRegex.pattern= Pattern.compile(innerRegex.unprotectedRegex);
						innerRegex.variableName = "..";
						regex.model.add(innerRegex);
					}
					sb.append( innerRegex.protectedRegex );					
				} else {
					if (!variableName.endsWith("[]")) {
						sb.append("(");
						RegularExpressionWithModel innerRegex = new RegularExpressionWithModel();
						innerRegex.searchText = typeExpression;
						buildProtectedRegex(innerRegex);
						sb.append(innerRegex.protectedRegex);
						sb.append(")");

						innerRegex.variableName = variableName;
						regex.model.add(innerRegex);
					} else {
						sb.append("(");
						RegularExpressionWithModel innerRegex = new RegularExpressionWithModel();
						innerRegex.searchText = typeExpression;
						buildProtectedRegex(innerRegex);
						innerRegex.unprotectedRegex=RegExUtil.unprotectRegex(innerRegex.protectedRegex);
						innerRegex.pattern= Pattern.compile(innerRegex.unprotectedRegex);
						sb.append(innerRegex.protectedRegex);
						sb.append("){/+/}"); //add repeat
						innerRegex.variableName = variableName;
						regex.model.add(innerRegex);						
					}
				}
				sb.append(boundedRightChar);
			}
			index++;
		}

		StringBuffer moreText = new StringBuffer();
		matcher.appendTail(moreText );
		String moreTextStr = moreText.toString(); 
		moreTextStr=RegExUtil.escapeRegExpressionCharsAndAbsorbWhitespace(moreText.toString(),getAbsorbWhiteSpaceSymbol());

		sb.append(moreTextStr);

		regex.protectedRegex = sb.toString();
		return regex;
	}
	



	
	
	public List<String> getTemplate() {
		return template;
	}

	public void setTemplate(List<String> template) {
		this.template = template;
	}

	public Map<String, String> getFillModel() {
		return fillModel;
	}

	public void setFillModel(Map<String, String> fillModel) {
		this.fillModel = fillModel;
	}

	public Map<String, String> getTags() {
		return tags;
	}

	public void setTags(Map<String, String> tags) {
		this.tags = tags;
	}

	public String getAbsorbWhiteSpaceSymbol() {
		return absorbWhiteSpaceSymbol;
	}

	public void setAbsorbWhiteSpaceSymbol(String absorbWhiteSpaceSymbol) {
		this.absorbWhiteSpaceSymbol = absorbWhiteSpaceSymbol;
	}

	public String getTokenMatcherPrefix() {
		return _tokenMatcherPrefix;
	}

	public void setTokenMatcherPrefix(String tokenMatcherPrefix) {
		_tokenMatcherPrefix = tokenMatcherPrefix;
	}

	public String getTokenMatcherSuffix() {
		return _tokenMatcherSuffix;
	}

	public void setTokenMatcherSuffix(String tokenMatcherSuffix) {
		_tokenMatcherSuffix = tokenMatcherSuffix;
	}

	
}
