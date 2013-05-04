
modelgrep
===========

Utility to grab structured values from a text file in a known format by using
a template. Commonly, templates are used to render files by applying
a data model to a template. With modelgrep, the reverse process is used
to extract a model from a rendered text file by applying the template. This
can be especially useful in parsing conversational output from older programs
that were not written to generate machine-readable text.

Modelgrep reads the template definition and generates regular expressions, which
are use to extract the place-holder values defined in the template.

### Build

    mvn package

### Develop from the command line

The maven build process will create a jar-with-dependencies which can be used to develop parsers from the command line. The command line has two options, -d for the parser definition and -r for the file to be parsed.

Command line options are as follows:

     Option                                  Description                            
     ------                                  -----------                            
     -?, -h                                  show help                              
     -f <file>                               file to search                         
     -t <file>                               template in yaml format                
     -v, --verbose                           verbose  


Example file to be parsed (available in src/test/resources/samples/sqlTable.txt):

    Author: Roger Zelazny
    
    +-----------+-------------------------+------------------+
    |  ID       |  title                  | publisher        | 
    +-----------+-------------------------+------------------+
    |   1       |   Lord of light         |           Eos    |
    |   2       |   Doors of his face     |  IBooks, Inc.    |
    +-----------+-------------------------+------------------+

Example template to parse the above text: (available in src/test/resources/templates/sqlTable.yaml)

    absorbWhiteSpaceSymbol: "~"
    template:
    -  "Author: {first} {last}"
    -  "{books[]:BOOK}"
    tags:
      BOOK: "| ~ {id:INT} ~ | ~ {title:TEXT_BLOCK} ~ | ~ {publisher:TEXT_BLOCK} ~ |"


To it from the root modelgrep directory with the test resources provided:

    java -jar target/ModelGrep-2.1-jar-with-dependencies.jar -d src/test/resources/templates/sqlTable.yaml -r src/test/resources/samples/sqlTable.txt

The parser will dump the model in yaml format.

    books:
    - {id: '1', title: Lord of light, publisher: Eos}
    - {id: '2', title: Doors of his face, publisher: 'IBooks, Inc.'}
    last: Zelazny
    first: Roger

##Template format
 When parsing arbitrary text, it can become very frustrating to parse characters that are used by the parsing language itself. Most parsing languages solve this problem by requiring that these spacial character be escaped. In modelgrep, the special characters can be defined strategically to avoid escaping.

###Flexible characters to avoid escaping
The special characters include:

* absorbWhiteSpaceSymbol: This symbol can be set to indicate that white space is absorbed around it. This allows for the definition of flexible boundaries as shown in the parsing of the decorated sql table dump example above.

* _tokenMatcherPrefix: indicates beginning of a token to be parsed and extracted from the text. default is the curly brace {

* _tokenMatcherSuffix: indicates end of a token to be parsed and extracted from the text. default is  the curly brace }

For example, if the text to be parsed had curly braces instead of pipes, the template author could switch to a different template definition:


In the following modified file, curly braces have replaced the pipe symbol to demonstrate:

    Author: Roger Zelazny
    
    +-----------+-------------------------+------------------+
    {  ID       |  title                  | publisher        }
    +-----------+-------------------------+------------------+
    {   1       |   Lord of light         |           Eos    }
    {   2       |   Doors of his face     |  IBooks, Inc.    }
    +-----------+-------------------------+------------------+


We create a new parser and redefine the open and closing symbols used to indicate a variable to be extracted. In this example two characters are used to open `(-` and two characters are used to close `-)`. The curly braces are now free to be used in the template to anchor the text to be extracted:

    absorbWhiteSpaceSymbol: "~"
    _tokenMatcherPrefix: "(-"
    _tokenMatcherSuffix: "-)"
    template:
    -  "Author: (-first-) (-last-)"
    -  "(-books[]:BOOK-)"
    tags:
      BOOK: "{ ~ (-id:INT-) ~ | ~ (-title:TEXT_BLOCK-) ~ | ~ (-publisher:TEXT_BLOCK-) ~ }"

Within the token matching symbols, a variable name is given followed by a tag name that indicates what type of value should be matched at this point.


###Tags
Tag names can be defined in the tags section or one of the built-in tags can be used:

 * INT (an integer)
 * TEXT_BLOCK  (a non-greedy block of text that will absorb whitespace)
 * FLOAT  (a floating point number)
 * STRING (a string of text surrounded by white space)

A variable name can be followed by square brackets [] to indicate that more than one match can be expected. This will generate a list of items. In the above example a list of 'books' is parsed using the 'BOOK' tag, which is defined in the 'tags' section. The BOOK tag uses the built-in tags and the whitespace absorber to handle flexible content.




## License

MIT



