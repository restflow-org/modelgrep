package org.restflow.modelgrep;

import static java.util.Arrays.asList;

import java.io.FileInputStream;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;


public class Main {
	public static void main(String[] args) throws Exception {
		OptionParser parser = parseOptions();

		String def = null;
		String resource = null;		
		
		try {
			OptionSet options = parser.parse(args);

			if (options.has("h")) {
				parser.printHelpOn(System.out);
				return;
			}
			
			if (options.has("d")) {
				def = (String)options.valueOf("d");
			}
			
			if (options.has("r")) {
				resource = (String)options.valueOf("r");
			}

			
		} catch (OptionException e) {
			System.err.print("Error in command-line options: ");
			System.err.println(e.getLocalizedMessage());
			parser.printHelpOn(System.err);
			return;
		}

		if (def == null) {
			System.err.print("must set d");
			return;
		}

		if (resource == null) {
			System.err.print("must set resource to search");
			return;
		}

		Constructor c = new Constructor("org.restflow.modelgrep.ModelGrep");
		Yaml yaml = new Yaml(c);
		
		FileInputStream fis = new FileInputStream(def);
		ModelGrep mg = (ModelGrep)yaml.load(IOUtils.toString(fis));
		mg.compile();
		
		FileInputStream textstream  = new FileInputStream(resource);
		String text = IOUtils.toString(textstream);

		String dump = yaml.dump(mg.search(text));
		System.out.println(dump);
		
	}
	
	private static OptionParser parseOptions() {
		
		OptionParser parser = null;
		
		try {
			parser = new OptionParser() {
			{
				acceptsAll(asList("h", "?"), "show help");
				acceptsAll(asList("t", "enable-trace"), "enable trace");
				acceptsAll(asList("d"), "yaml definition")
				.withRequiredArg().ofType(String.class)
				.describedAs("file");
				acceptsAll(asList("r"), "resource to search")				
				.withRequiredArg().ofType(String.class)
				.describedAs("file");
				}
			};
			
		} catch (OptionException e) {
			System.err.print("Option contains illegal character: ");
			System.err.println(e.getLocalizedMessage());
			System.exit(0);
		}
			
		return parser;
	}	
}
