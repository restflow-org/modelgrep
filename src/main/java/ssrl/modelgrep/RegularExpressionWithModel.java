package ssrl.modelgrep;

import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;


public class RegularExpressionWithModel {
	public String variableName;
	public String searchText;
	public String protectedRegex;
	public String unprotectedRegex;		
	public Pattern pattern;
	public List<RegularExpressionWithModel> model = new Vector<RegularExpressionWithModel>();
	public boolean hasEmbeddedTags() {
		return model.size() > 0;
	}
}

