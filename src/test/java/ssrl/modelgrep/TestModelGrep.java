package ssrl.modelgrep;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import junit.framework.TestCase;

import org.yaml.snakeyaml.Yaml;


public class TestModelGrep extends TestCase {

	public void testTextToOutput () throws Exception {
		
		ModelGrep  s = new ModelGrep();
		
		List<String> params = new Vector<String>();
		params.add("DISTANCE   {distance}");
		s.setTemplate(params);
		s.compile();
		
		Map<String,Object> result = s.search("DISTANCE   350.0");
		assertEquals("350.0", result.get("distance") );
	}
	
	public void testTextToParams2 () throws Exception{
		
		ModelGrep  s = new ModelGrep();
		
		List<String> params = new Vector<String>();
		params.add("DISTANCE   {distance}");
		params.add("PHI   {phi}");		
		s.setTemplate(params);
		s.compile();
		
		Map<String, Object> result = s.search("DISTANCE   350.0\nPHI   100.0");
		
		assertEquals("350.0", result.get("distance") );
		assertEquals("100.0", result.get("phi") );		
	}
	
	public void testTextToParams3 () throws Exception{
		
		ModelGrep  s = new ModelGrep();
		
		List<String> params = new Vector<String>();
		params.add("DISTANCE   {distance}   PHI:   {phi}");
		s.setTemplate(params);
		s.compile();
		
		Map<String, Object> result = s.search("DISTANCE   350.0   PHI:   120.0");
		
		assertEquals("350.0", result.get("distance") );
		assertEquals("120.0", result.get("phi") );		
	}	
	
	
	public void testFindOutputs () throws Exception {
		
		ModelGrep  s = new ModelGrep();
		s.addDefaultTags();
		RegularExpressionWithModel regex = s.createRegex("DISTANCE   {distance}  {score} {phi:INT} {$fill}");
		
		List<RegularExpressionWithModel> model = regex.model;
		assertEquals(3, model.size());
		assertEquals("distance", model.get(0).variableName );
		assertEquals("score", model.get(1).variableName );
		assertEquals("phi", model.get(2).variableName );
		//assertNotEquals("$fill", outputs.get(3) );		
	}

	public void testCreateRegex () throws Exception{
		
		ModelGrep  s = new ModelGrep();
		s.addDefaultTags();
		RegularExpressionWithModel regexp = s.createRegex("DISTANCE {distance}");
		assertEquals("DISTANCE (\\S+)", regexp.unprotectedRegex );
	}
	
	public void testCreateRegexWhiteSpaceAbsorber () throws Exception{
		
		ModelGrep  s = new ModelGrep();
		s.setAbsorbWhiteSpaceSymbol(".");
		s.addDefaultTags();
		RegularExpressionWithModel regexp = s.createRegex("DISTANCE .{distance}");
		assertEquals("DISTANCE\\s+(\\S+)", regexp.unprotectedRegex );
	}
	
	public void testCreateRegexWhiteSpaceAbsorber2 () throws Exception{
		
		ModelGrep  s = new ModelGrep();
		s.setAbsorbWhiteSpaceSymbol(".");
		s.addDefaultTags();
		RegularExpressionWithModel regexp = s.createRegex("DISTANCE.{distance}");
		assertEquals("DISTANCE\\s*(\\S+)", regexp.unprotectedRegex );
	}	
	
	
	public void testCreateRegexBoundedByText () throws Exception{
		
		ModelGrep  s = new ModelGrep();
		s.getTags().put("DASHES","{/\\-*/}");
		s.addDefaultTags();
		RegularExpressionWithModel regexp = s.createRegex("---{:DASHES}{heading}{:DASHES}---");
		assertEquals("\\-\\-\\-\\-*(\\S+)\\-*\\-\\-\\-", regexp.unprotectedRegex );
	}
	
	public void testCreateRegexSTRING () throws Exception{
		
		ModelGrep  s = new ModelGrep();
		s.addDefaultTags();
		RegularExpressionWithModel regexp = s.createRegex("DISTANCE {distance} {:STRING} {:INT}");
		assertEquals("DISTANCE (\\S+) \\S+ [-+]?\\d+", regexp.unprotectedRegex );
	}	
	
	public void testFindOutputsVarInt () throws Exception {
		
		ModelGrep  s = new ModelGrep();
		
	
		RegularExpressionWithModel regex = s.createRegex("SIZE_X {size_x:STRING}");
		List<RegularExpressionWithModel> model = regex.model;
		assertEquals("size_x", model.get(0).variableName );
		//assertEquals("phi:FLOAT", outputs.get(2) );				
	}
	
	
	public void testCreateRegexVarInt () throws Exception{
		
		ModelGrep  s = new ModelGrep();
		
		RegularExpressionWithModel regexp = s.createRegex("SIZE_X {size_x:INT}");
		assertEquals("SIZE_X ([-+]?\\d+)", regexp.unprotectedRegex );
	}	
	
	

	public void testFindOutputsWithTokens () throws Exception{
		
		ModelGrep  s = new ModelGrep();
		
		List<String> params = new Vector<String>();
		params.add("DISTANCE   {distance}  {:STRING} {:INT}");
		s.setTemplate(params);
		s.compile();
		
		Map<String, Object> result = s.search("DISTANCE   350.0  Hello 5");
		assertEquals("350.0", result.get("distance") );
		//assertEquals("Hello", result.get(1) );		
		//assertEquals("2", result.get(2) );				
	}

	public void testDontFindOutputsWithTokens () throws Exception{
		
		ModelGrep  s = new ModelGrep();
		
		List<String> params = new Vector<String>();
		params.add("DISTANCE {distance} {:STRING} {:INT}");
		s.setTemplate(params);
		s.compile();
		
		Map<String, Object> result = s.search("DISTANCE   350.0  Hello k");
		assertEquals( 0, result.size());				
	}

	public void testFindOutputsWithFile () throws Exception {
		
		ModelGrep  s = new ModelGrep();
		
		List<String> params = new Vector<String>();
		params.add("DISTANCE              {distance}");
		params.add("DIM                   {:INT}\n" +
				   "BYTE_ORDER            {endian}" );		
		s.setTemplate(params);
		s.compile();

		String filePath = "src/test/resources/samples/imageHeader.txt";
		File in = new File(filePath);
		String text = new String(getFileAsBytes(in));
		
		Map<String, Object> result = s.search(text);
		assertEquals("172.008397", result.get("distance") );
		assertEquals("little_endian", result.get("endian") );		
		//assertEquals("Hello", result.get(1) );		
		//assertEquals("2", result.get(2) );				
	}
	
	
	public void testFindOutputsWithSpecialCharacters () throws Exception {
		
		ModelGrep  s = new ModelGrep();
		
		List<String> params = new Vector<String>();
		params.add("Ignore slash \\w \\s \\S \\   {ok_slash} Ignore slash");
		params.add("Ignore dollars   $hello   {ok_dollar} Ignore dollars");
		params.add("Ignore brackets   [{PID}]   ok_brackets Ignore brackets");
		params.add("Ignore period   . {ok_period}   ok_period Ignore period");
		params.add("Ignore question  {ok_question}  Ignore question");
		params.add("Ignore exp  {ok_exp}  Ignore exp");
		params.add("Ignore paren  {ok_paren}  Ignore paren");		
		params.add("Ignore plus  {ok_plus}  Ignore plus");				
		params.add("Ignore star  {ok_star}  Ignore star");						
		params.add("Ignore colon {ok_colon} Ignore colon");						
		params.add("Ignore dash  {ok_dash}  Ignore dash");	
		params.add("Ignore excl {ok_excl} Ignore excl");
		params.add("Ignore stuff  {ok_stuff} ignore stuff");
		params.add("----------{ok_no_whitespace}----------");
		params.add("-----{header1}------------{header2}---");		
				
		s.setTemplate(params);
		s.compile();
		
		String filePath = "src/test/resources/samples/specialChar.txt";
		File in = new File(filePath);
		String text = new String(getFileAsBytes(in));
		
		Map<String, Object> result = s.search(text);
		assertEquals("ok_slash", result.get("ok_slash") );
		assertEquals("ok_dollar", result.get("ok_dollar") );		
		assertEquals("12345", result.get("PID") );		
		assertEquals("HELLO.", result.get("ok_period") );				
		assertEquals("Hello?", result.get("ok_question") );						
		assertEquals("Hello^", result.get("ok_exp") );								
		assertEquals("(Hello)", result.get("ok_paren") );								
		assertEquals("Hello+", result.get("ok_plus") );								
		assertEquals("Hello*", result.get("ok_star") );
		assertEquals("Hello:", result.get("ok_colon") );	
		assertEquals("[A-Z]", result.get("ok_dash") );	
		assertEquals("@#&=\"|", result.get("ok_stuff") );
		assertEquals("MyBoundedHello", result.get("ok_no_whitespace") );
		assertEquals("X", result.get("header1") );
		assertEquals("Y", result.get("header2") );		
	}
	
	
	
	public void testFillTemplate () throws Exception {
		
		ModelGrep  s = new ModelGrep();
		
		List<String> params = new Vector<String>();
		params.add("DISTANCE              {distance}");
		params.add("{$byte_order_var}            {endian}" );		
		s.setTemplate(params);
		
		Map<String,String> fillModel = new HashMap<String, String>();
		fillModel.put("byte_order_var","BYTE_ORDER");
		
		s.setFillModel(fillModel);
		s.compile();

		String filePath = "src/test/resources/samples/imageHeader.txt";
		File in = new File(filePath);
		String text = new String(getFileAsBytes(in));
		
		Map<String, Object> result = s.search(text);
		assertEquals("172.008397", result.get("distance") );
		assertEquals("little_endian", result.get("endian") );		
		//assertEquals("Hello", result.get(1) );		
		//assertEquals("2", result.get(2) );				
	}
	
	public void testMakeRepeatTag() throws Exception {

		//first add a tag to the default tags
		ModelGrep  s = new ModelGrep();
		s.getTags().put("REPEAT_ABC", "{/(?:ABC_)+/}");
		
		//now define a search for a number
		List<String> params = new Vector<String>();
		params.add("{:REPEAT_ABC}{myNumber:INT}{:REPEAT_ABC}");
		s.setTemplate(params);
		s.compile();

		//String filePath = "src/test/resources/ssrl/workflow/beans/repeat.txt";
		//File in = new File(filePath);
		//String text = new String(getFileAsBytes(in));
		String text = "ABC_ABC_ABC_ABC_1234ABC_ABC_ABC_ABC_\n";
		Map<String, Object> result = s.search(text);

		assertEquals("1234", result.get("myNumber") );
	}
	
	public void testNewTagWithTags() throws Exception {

		//first add a tag to the default tags
		ModelGrep  s = new ModelGrep();
		s.getTags().put("FOUR_INTS", "{:INT} {:INT} {:INT} {:INT}");
		
		//now define a search for a number
		List<String> params = new Vector<String>();
		params.add("{:FOUR_INTS} {greeting:STRING} {:FOUR_INTS}");
		s.setTemplate(params);
		s.compile();

		String text = "1 2 3 4 HELLO 1 2 3 4";
		Map<String, Object> result = s.search(text);

		assertEquals("HELLO", result.get("greeting") );
	}
	
	
	public void testFindOutputsWithVariableTags () throws Exception {
		
		ModelGrep  s = new ModelGrep();
		s.getTags().put("FOUR_INT_VARS", "{var1:INT} {var2:INT} {var3:INT} {var4:INT}");
		
		RegularExpressionWithModel regex = s.createRegex("{numbers:FOUR_INT_VARS}" );
		List<RegularExpressionWithModel> model = regex.model;
		RegularExpressionWithModel numbers = model.get(0);
		assertEquals("numbers", numbers.variableName );
		
		List<RegularExpressionWithModel> variables = numbers.model;

//		property="~";
		assertEquals(4, variables.size());
		assertEquals("var1", variables.get(0).variableName );
		assertEquals("var2", variables.get(1).variableName );
		assertEquals("var3", variables.get(2).variableName );
		assertEquals("var4", variables.get(3).variableName );
	}
	
	public void testNewTagWithVariableTags() throws Exception {

		//first add a tag to the default tags
		ModelGrep  s = new ModelGrep();
		s.getTags().put("FOUR_INT_VARS", "{var1:INT} {var2:INT} {var3:INT} {var4:INT}");
		
		//now define a search for a number
		List<String> params = new Vector<String>();
		params.add("{table1:FOUR_INT_VARS} {greeting:STRING} {table2:FOUR_INT_VARS}");
		s.setTemplate(params);
		s.compile();

		String text = "1 2 3 4 HELLO 5 6 7 8";
		Map<String,Object> result = s.search(text);

		assertEquals("HELLO", result.get("greeting") );
		Map<String,Object> table1 = (Map<String,Object>)result.get("table1");
		assertEquals("3", table1.get("var3"));		
		
		Map<String,Object> table2 = (Map<String,Object>)result.get("table2");		
		assertEquals("7", table2.get("var3") );				
	}
/*
	public void testFindOutputsWithArrayVariableTags () {
		
		ModelGrep  s = new ModelGrep();
		s.getTags().put("FOUR_INT_VARS", "{var1:INT} {var2:INT} {var3:INT} {var4:INT}");
		
		Map<String,List<String>> model = s.findModel("My four numbers: {numbers[]:FOUR_INT_VARS}");

		property="numbers";
		List<String> outputs = model.get("numbers");
		assertEquals("var1", outputs.get(0) );
		assertEquals("var2", outputs.get(1) );
		assertEquals("var3", outputs.get(2) );
		assertEquals("var4", outputs.get(3) );
	}
	
*/

	public void testNewTagWithVariableArrayTags() throws Exception {

		//first add a tag to the default tags
		ModelGrep  s = new ModelGrep();
		s.getTags().put("FOUR_INT_VARS", "{var1:INT} {var2:INT} {var3:INT} {var4:INT}");
		
		//now define a search for a number
		List<String> params = new Vector<String>();
		params.add("{row[]:FOUR_INT_VARS}");
		s.setTemplate(params);
		s.compile();

		String text = "1 2 3 4\n5 6 7 8\n9 10 11 12";
		Map<String,Object> result = (Map<String,Object>)s.search(text);
		assertEquals(1, result.size());		

		List<Map<String,Object>> table = (List<Map<String,Object>>) result.get("row");
		
		assertNotNull("no rows found",table);
		
		Map<String,Object> row = table.get(0);		
		assertEquals("1", row.get("var1"));		
		assertEquals("2", row.get("var2"));		
		assertEquals("3", row.get("var3"));		
		assertEquals("4", row.get("var4"));		

		Map<String,Object> row2 = table.get(1);		
		assertEquals("5", row2.get("var1"));		
		assertEquals("6", row2.get("var2"));		
		assertEquals("7", row2.get("var3"));		
		assertEquals("8", row2.get("var4"));
		
		Map<String,Object> row3 = table.get(2);		
		assertEquals("9", row3.get("var1"));		
		assertEquals("10", row3.get("var2"));		
		assertEquals("11", row3.get("var3"));		
		assertEquals("12", row3.get("var4"));
	}
	
/*	public void testNewTagWithMapArrayTags() throws Exception {

		//first add a tag to the default tags
		ModelGrep  s = new ModelGrep();
		s.getTags().put("FOUR_INT_VARS", "{var1:INT} {var2:INT} {var3:INT} {var4:INT}");
		
		//now define a search for a number
		List<String> params = new Vector<String>();
		params.add("{map[var1]:FOUR_INT_VARS}");
		s.setTemplate(params);
		s.compile();

		String text = "1 2 3 4\n5 6 7 8";
		Map<String, Object> result = s.search(text).getProperties();

		assertEquals("HELLO", result.get("greeting") );
		assertEquals("3", result.get("map['1'].var3") );		
		assertEquals("7", result.get("map['5'].var3") );				
	}*/
	
	public void testUnprotectRegularExpression() throws Exception {
		//first add a tag to the default tags
		//ModelGrep  s = new ModelGrep();
		assertEquals("\\d+\\d+",RegExUtil.unprotectRegex("{/\\d+/}{/\\d+/}"));
	}
	
	public void testLoadTable () throws Exception {
		
		ModelGrep  s = new ModelGrep();
		s.setAbsorbWhiteSpaceSymbol(".");
		
		List<String> params = new Vector<String>();
		params.add("{books[]:BOOK}");
		params.add("Author: {first} {last}");
		s.setTemplate(params);

		s.getTags().put("BOOK", "|. {id:INT} .|. {title:TEXT_BLOCK} .|. {publisher:TEXT_BLOCK} .|\n");
		
		s.compile();

		String filePath = "src/test/resources/samples/sqlTable.txt";
		File in = new File(filePath);
		String text = new String(getFileAsBytes(in));
		
		Map<String,Object> result = s.search(text);
		assertTrue("should have at least one match", result.size() > 0);
		assertEquals("Roger", result.get("first") );
		assertEquals("Zelazny", result.get("last") );	
		
		List<Map<String,Object>> books = (List<Map<String,Object>>)result.get("books");
		assertNotNull("should have at least one book", books);
		assertEquals("Lord of light", books.get(0).get("title"));
		assertEquals("Doors of his face", books.get(1).get("title"));

		assertEquals("Eos", books.get(0).get("publisher"));
		assertEquals("IBooks, Inc.", books.get(1).get("publisher"));
		
		Yaml yaml = new Yaml();
		String resultStr = yaml.dump(result);
		System.out.println(resultStr);
		
	}	
	
	
	
	public void testAbsorbWhitespace () throws Exception {
		
		ModelGrep  s = new ModelGrep();
		s.setAbsorbWhiteSpaceSymbol(".");
		
		List<String> params = new Vector<String>();
		params.add("<id>{id:INT}<id>\n.\n<author>.<first>{first}</first>.<last>{last}</last>.<author/>.");
		s.setTemplate(params);
		s.compile();

		String text = "<id>1<id>\n    \n<author>\n<first>Roger</first>\n<last>Zelazny</last>\n<author/>\n\n\n\n";
		
		Map<String,Object> result = s.search(text);
		assertTrue("should have at least one match", result.size() > 0);
		assertEquals("Roger", result.get("first") );
		assertEquals("Zelazny", result.get("last") );	
		
		Yaml yaml = new Yaml();
		String resultStr = yaml.dump(result);
		System.out.println(resultStr);
		
	}		
	
	
	public void testRepeatRegex () throws Exception {
		
		ModelGrep  s = new ModelGrep();
		s.setAbsorbWhiteSpaceSymbol(".");
		
		List<String> params = new Vector<String>();
		params.add("{wordArray[]:WORD} . ");
		s.setTemplate(params);
		s.getTags().put("WORD", "{/\\w+/}");

		s.compile();

		String text = "word1 word2 word3 ";
		
		Map<String,Object> result = s.search(text);
		assertTrue("should have at least one match", result.size() > 0);
		List<Object> wordArray = (List<Object>)result.get("wordArray");
		assertTrue("should have 3 words", wordArray.size() ==  3);

		assertEquals("word1", wordArray.get(0) );
		assertEquals("word2", wordArray.get(1) );
		assertEquals("word3", wordArray.get(2) );
		
		Yaml yaml = new Yaml();
		String resultStr = yaml.dump(result);
		System.out.println(resultStr);
	}		

	public void testFlattenTagsWithNoVariablesNoArray () throws Exception {
		
		ModelGrep  s = new ModelGrep();
		s.setAbsorbWhiteSpaceSymbol(".");
		
		List<String> params = new Vector<String>();
		params.add("{properties:PROPERTIES}");
		s.setTemplate(params);
		s.getTags().put("PROPERTIES", "{:PROPERTY}");
		s.getTags().put("PROPERTY", "{name:STRING}={value:STRING}");		

		s.compile();

		String text = "first=Roger";
		
		Map<String,Object> result = s.search(text);
		assertTrue("should have at least one match", result.size() > 0);

		Map<String,Object> props = (Map<String,Object>)result.get("properties"); 
		assertEquals("first", props.get("name") );
		assertEquals("Roger", props.get("value") );		

		
		Yaml yaml = new Yaml();
		String resultStr = yaml.dump(result);
		System.out.println(resultStr);
	}		
	
	public void testFlattenTagsWithNoVariables () throws Exception {
		
		ModelGrep  s = new ModelGrep();
		s.setAbsorbWhiteSpaceSymbol(".");
		
		List<String> params = new Vector<String>();
		params.add("{properties[]:PROPERTIES}.");
		s.setTemplate(params);
		s.getTags().put("PROPERTIES", "{:PROPERTY}");
		s.getTags().put("PROPERTY", "{name:STRING}={value:STRING}");		

		s.compile();

		String text = "first=Roger\nlast=Zelazny";
		
		Map<String,Object> result = s.search(text);
		assertTrue("should have at least one match", result.size() > 0);

		List<Map<String,Object>> props = (List<Map<String,Object>>)result.get("properties");
		Map<String,Object> prop1 = (Map<String,Object>)props.get(0);
		Map<String,Object> prop2 = (Map<String,Object>)props.get(1);		
		
		assertEquals("first", prop1.get("name") );
		assertEquals("Roger", prop1.get("value") );		

		assertEquals("last", prop2.get("name") );
		assertEquals("Zelazny", prop2.get("value") );		
		
		Yaml yaml = new Yaml();
		String resultStr = yaml.dump(result);
		System.out.println(resultStr);
	}		
	
	public void testFlattenTagsWithNoVariablesMoreComplex () throws Exception {
		
		ModelGrep  s = new ModelGrep();
		s.setAbsorbWhiteSpaceSymbol(".");
		
		List<String> params = new Vector<String>();
		params.add("{authors[]:AUTHORS}.");
		s.setTemplate(params);
		s.getTags().put("AUTHORS", "{:NAME} {age:INT}");
		s.getTags().put("NAME", "{first:STRING} {last:STRING}");		

		s.compile();

		String text = "Roger Zelazny 65\nIsaac Asimov 78";
		
		Map<String,Object> result = s.search(text);
		assertTrue("should have at least one match", result.size() > 0);

		List<Map<String,Object>> props = (List<Map<String,Object>>)result.get("authors");
		Map<String,Object> author1 = (Map<String,Object>)props.get(0);
		Map<String,Object> author2 = (Map<String,Object>)props.get(1);		
		
		assertEquals("Roger", author1.get("first") );
		assertEquals("Zelazny", author1.get("last") );		
		assertEquals("65", author1.get("age") );		

		
		assertEquals("Isaac", author2.get("first") );
		assertEquals("Asimov", author2.get("last") );		
		assertEquals("78", author2.get("age") );		

		
		Yaml yaml = new Yaml();
		String resultStr = yaml.dump(result);
		System.out.println(resultStr);
	}
	
	public void testFlattenMultipleLayersOfTags () throws Exception {
		
		ModelGrep  s = new ModelGrep();
		s.setAbsorbWhiteSpaceSymbol(".");
		
		List<String> params = new Vector<String>();
		params.add("{authors[]:AUTHORS}.");
		s.setTemplate(params);
		s.getTags().put("AUTHORS", "{:NAME} {age:INT}");
		s.getTags().put("NAME", "{:SALUTATION} {first:STRING} {last:STRING}");		
		s.getTags().put("SALUTATION", "{salutation}");		
		
		s.compile();

		String text = "Mr. Roger Zelazny 65\nDr. Isaac Asimov 78";
		
		Map<String,Object> result = s.search(text);
		assertTrue("should have at least one match", result.size() > 0);

		List<Map<String,Object>> props = (List<Map<String,Object>>)result.get("authors");
		Map<String,Object> author1 = (Map<String,Object>)props.get(0);
		Map<String,Object> author2 = (Map<String,Object>)props.get(1);		
		
		assertEquals("Roger", author1.get("first") );
		assertEquals("Zelazny", author1.get("last") );		
		assertEquals("65", author1.get("age") );		
		assertEquals("Mr.", author1.get("salutation") );		
		
		assertEquals("Isaac", author2.get("first") );
		assertEquals("Asimov", author2.get("last") );		
		assertEquals("78", author2.get("age") );		
		assertEquals("Dr.", author2.get("salutation") );				

		
		Yaml yaml = new Yaml();
		String resultStr = yaml.dump(result);
		System.out.println(resultStr);
	}	
	
	
	
	public void testLabelit () throws Exception {
		
		ModelGrep  s = new ModelGrep();
		s.setAbsorbWhiteSpaceSymbol(".");
		
		List<String> params = new Vector<String>();
		params.add("Solution . Metric . fit . rmsd . #spots . crystal_system . unit_cell . volume\n"+
        "{indexResults[]:INDEX_RESULTS}");

		params.add("MOSFLM Integration results:\n"+
        "Solution . SpaceGroup . Beam x . y . distance . Resolution . Mosaicity . RMS\n"+
        "{integrationResults[]:INTEGRATION_RESULTS}");

		s.setTemplate(params);
		
		//s.getTags().put("INDEX_RESULTS", ":) . {id:INT} . {metric} . {fit} . {rmsd} . {spots} . {crystalSystem:TWO_WORDS} . {unitCell:UNIT_CELL} . {volume} .");
		s.getTags().put("INDEX_RESULTS", "{face} . {id:INT} . {metric} . {fit} . {rmsd} . {spots} . {crystalSystem:TWO_WORDS} . {unitCell:UNIT_CELL} . {volume} .");

		s.getTags().put("UNIT_CELL", "{a} . {b} . {c} . {alpha} . {beta} . {gamma}");		
		s.getTags().put("INTEGRATION_RESULTS", ":) . {id} . {spaceGroup} . {beamX} . {beamY} . {distance} . {resolution} . {mosaicity} . {rms}");		
		s.getTags().put("TWO_WORDS", "{/\\S+\\s+\\S+/}");		
		
		s.compile();

		String text = "/data/blstaff/PILATUS/BL9-1/MYO/E15000_TH75_MG_5_0001.cbf\n"
				+ "/data/blstaff/PILATUS/BL9-1/MYO/E15000_TH75_MG_5_0900.cbf\n"
				+ "Correcting beam by 2.0 mm (to 219.2 212.3) and reindexing\n"
				+ "\n"
				+ "LABELIT Indexing results:\n"
				+ "Beam center x  219.21mm, y  212.37mm, distance  299.82mm ; 80% mosaicity=0.40 deg.\n"
				+ "\n"
				+ "Solution  Metric fit  rmsd  #spots  crystal_system   unit_cell                                  volume\n"
				+ ":)  12     0.0622 dg 0.125    245     hexagonal hP   90.22  90.22  45.25  90.00  90.00 120.00   318987\n"
				+ ";(  11     0.0622 dg 0.195    246  orthorhombic oC   90.22 156.27  45.25  90.00  90.00  90.00   637967\n"
				+ ";(  10     0.0622 dg 0.196    246    monoclinic mC  156.26  90.22  45.25  90.00  89.97  90.00   637975\n"
				+ ":)   9     0.0508 dg 0.124    244  orthorhombic oC   90.19 156.33  45.25  90.00  90.00  90.00   638001\n"
				+ ";(   8     0.0543 dg 0.213    246  orthorhombic oC   90.25 156.19  45.25  90.00  90.00  90.00   637865\n"
				+ ":)   7     0.0508 dg 0.124    245    monoclinic mC  156.33  90.19  45.25  90.00  90.02  90.00   638038\n"
				+ ";(   6     0.0543 dg 0.213    246    monoclinic mC   90.25 156.20  45.25  90.00  89.99  90.00   637910\n"
				+ ";(   5     0.0578 dg 0.192    247    monoclinic mC   90.22 156.26  45.25  90.00  89.96  90.00   637975\n"
				+ ":)   4     0.0483 dg 0.123    245    monoclinic mP   90.19  45.25  90.22  90.00 119.97  90.00   318986\n"
				+ ";(   3     0.0266 dg 0.208    247    monoclinic mC  156.21  90.25  45.25  90.00  89.95  90.00   637974\n"
				+ ":)   2     0.0296 dg 0.119    244    monoclinic mC   90.18 156.32  45.25  90.00  89.96  90.00   637974\n"
				+ ":)   1     0.0000 dg 0.114    245     triclinic aP   45.25  90.18  90.22  60.04  89.96  89.96   -318987\n"
				+ "tetragonal MOSFLM logfile declares FATAL ERROR"
				+ "orthorhombic MOSFLM logfile declares FATAL ERROR"
				+ "monoclinic MOSFLM logfile declares FATAL ERROR"
				+ "monoclinic MOSFLM logfile declares FATAL ERROR"
				+ "\n"
				+ "MOSFLM Integration results:\n"
				+ "Solution  SpaceGroup Beam x   y  distance  Resolution Mosaicity RMS\n"
				+ ":)  12           P3 219.25 212.91  299.88       1.60    0.400000    0.075\n"
				+ "     1           P1 219.23 212.58  299.87       1.60    0.400000    0.087\n";
		
		
		
		Map<String,Object> result = s.search(text);
		
		Yaml yaml = new Yaml();
		String resultStr = yaml.dump("Hello\n'World'");
		System.out.println(resultStr);

		assertEquals("wrong number of matches", 2, result.size());

		List<Map<String,Object>> integrationResults = (List<Map<String,Object>>)result.get("integrationResults");
		Map<String,Object> result1 = (Map<String,Object>)integrationResults.get(0);	
		
		assertEquals("299.88", result1.get("distance") );
		assertEquals("0.400000", result1.get("mosaicity") );

		
		List<Map<String,Object>> indexResults = (List<Map<String,Object>>)result.get("indexResults");
		assertEquals(12, indexResults.size());
		
		Map<String,Object> result2 = (Map<String,Object>)indexResults.get(0);	

		assertEquals(9, result2.size());

	}	
	
	
	
	public void testDirectoryParsing () throws Exception {
		
		ModelGrep  s = new ModelGrep();
		s.setAbsorbWhiteSpaceSymbol("~");
		
		List<String> params = new Vector<String>();
		params.add("{root}/{name}_{num:INT}.{ext}");
		s.setTemplate(params);
		s.compile();

		String text = "/data/blstaff/PILATUS/BL9-1/MYO/E15000_TH75_MG_5_0001.cbf\n";
		
		Map<String,Object> result = s.search(text);

		assertEquals("wrong number of matches", 4, result.size());

		assertEquals("/data/blstaff/PILATUS/BL9-1/MYO", result.get("root"));		
		assertEquals("0001", result.get("num") );
		assertEquals("E15000_TH75_MG_5", result.get("name") );
		assertEquals("cbf", result.get("ext"));
	}	
	
	
	
	public void testXtriageParsing () throws Exception {
		
/*		ModelGrep s = new ModelGrep();
		s.setAbsorbWhiteSpaceSymbol("~");
		
		List<String> params = new Vector<String>();
		
		params.add("z ~ Acentric_observed ~ Acentric_untwinned ~ Centric_observed ~ Centric_untwinned ~ $$~\n"
					+ "$$~\n"
					+ "{nztest[]:NZ_TEST}"
					+ "$$~\n"
					+ "~\n"
					+ "~\n"
					+ "$TABLE:~L~test,acentric~data:~\n");
		

      	params.add("|l| ~ Observed ~ Acentric_theory ~ Acentric_theory,_perfect_twin ~ $$~\n"
      				+ "$$~\n"
      				+ "{ltest:TEXT_BLOCK}\n"
      				+ "$$~\n"
      				+ "~\n"
      				+ "---------------------------------------------\n");
		
      	s.setTemplate(params);
      	
      	s.getTags().put("NZ_TEST", "{z} ~ {acob} ~ {acuntw} ~ {cenob} ~ {cenuntw}\n");
      	*/
		ModelGrep s = (ModelGrep)new Yaml().load(new FileInputStream("src/test/resources/templates/xtriageTemplate.yaml"));
		s.addDefaultTags();
		s.compile();
		
		String filePath = "src/test/resources/samples/xtriage.log";
		File in = new File(filePath);
		String text = new String(getFileAsBytes(in));
		
		Map<String,Object> result = s.search(text);
		
		String header = (String)result.get("header");
		assertNotNull("did not find the header",header);
		
		List nztest = (List)result.get("nztest");
		assertNotNull("did not find the nztest table",nztest);

		assertEquals (11, nztest.size());
		assertEquals("0.632100",((Map)nztest.get(10)).get("acuntw")  );
		
		String l_header = (String)result.get("l_header");
		assertNotNull("did not find the l header",l_header);

		List ltest = (List)result.get("ltest");
		assertNotNull("did not find the ltest table",ltest);
	
		assertEquals (50, ltest.size());
		assertEquals("0.999404",((Map)ltest.get(49)).get("acthtw")  );
		

	}
	
	public void testEqualsParsing () throws Exception {
		
		ModelGrep  s = new ModelGrep();
		s.setAbsorbWhiteSpaceSymbol("~");
		
		List<String> params = new Vector<String>();
		params.add("{equals1:EQUALS_ROW} ~ {myText} ~ {equals2:EQUALS_ROW}");
		s.getTags().put("EQUALS_ROW","{/=+/}");
		s.setTemplate(params);
		s.compile();
		
		String text = "==============================\nMyText\n=========================\n";
		
		Map<String,Object> result = s.search(text);

		assertEquals("wrong number of matches", 3, result.size());

		assertEquals("MyText", result.get("myText"));		
		assertEquals("==============================", result.get("equals1"));		
		assertEquals("=========================", result.get("equals2"));		
		
	}		
	
	
	private final byte[] getFileAsBytes(final File file) throws IOException {
		final BufferedInputStream bis = new BufferedInputStream(
				new FileInputStream(file));
		final byte[] bytes = new byte[(int) file.length()];
		bis.read(bytes);
		bis.close();
		return bytes;
	}

	
	public class AuthorModel {
		private String first;
		private String last;		
		private List<BookModel> books;
		public String getFirst() {
			return first;
		}
		public void setFirst(String first) {
			this.first = first;
		}
		public String getLast() {
			return last;
		}
		public void setLast(String last) {
			this.last = last;
		}
		public List<BookModel> getBooks() {
			return books;
		}
		public void setBooks(List<BookModel> books) {
			this.books = books;
		}
	}

	public class BookModel {
		private String title;
		private String publisher;
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String getPublisher() {
			return publisher;
		}
		public void setPublisher(String publisher) {
			this.publisher = publisher;
		}
	}

	
}
