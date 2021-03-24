<!-- (c) https://github.com/MontiCore/monticore -->

# Object Constraint Language (OCL)

This documentation is intended for  **modelers** who use the object OCL 
languages.
A detailed documentation for **language engineers** using or extending the 
OCL language is 
located **[here](src/main/grammars/de/monticore/ocl/OCL.md)**.
We recommend that **language engineers** read this documentation before reading 
the detailed documentation.

# An Example Model

``` 
ocl Bookshop {
  context Shop s inv CustomerPaysBeforeNewOrder:      // invariant
    forall Customer c in s.customers:                 // quantifiers available
      c.allowedToOrder implies !exists Invoice i in s.invoices:
        i.buyer == c && i.moneyPayed < i.invoiceAmount ;

  // Method specification for selling a book
  context Invoice Stock.sellBook(String iban, int discountPercent, Customer c)
    let availableBooks =                              // set comprehension
          { book | Book book in booksInStock, book.iban == iban }
    pre:  !availableBooks.isEmpty() &&                  // precondition
          c.allowedToOrder;
    post: let discount = (100 - discountPercent)/100; // postcondition, let
              b = result.soldBook                     // result variable
          in
              !(b isin booksInStock) &&
              booksInStock.size@pre == booksInStock.size + 1 &&  // @pre
              result.invoiceAmount == b.price * discount;
}
```

The textual OCL representation mostly relies on the definition in [Rum16, 
Rum17]. 
OCL is used to check the correctness of other models.
Here, the `Book` and `Customer` types are, for example, defined by a 
class diagram:

```
classdiagram Bookshop {
  class Shop;

  class Book {
    String name;
    String iban;
    double cost;
    double price;
  }

  class Stock {
    void addBook(Book b);
    Invoice sellBook(Book bookToSell, int discountPercent, Customer buyer);
  }

  class Customer {
    String name;
    String phoneNumber;
    int customerNumber;
    boolean allowedToOrder;
    void payInvoice(double moneyWired, Invoice invoice);
  }

  class Invoice {
    int invoiceNumber;
    double invoiceAmount;
    double moneyPayed;
  }

  association [1] Stock -> (booksInStock) Book [*];
  association [1] Shop -> (invoices) Invoice [*];
  association [1] Shop -> (customers) Customer [*];
  association [1] Invoice <-> (buyer) Customer [1];
  association [1] Invoice <-> (soldBook) Book [1];
}
```


# Command Line Interface (CLI)

This section describes the CLI tool of the OCL language. 
The CLI tool provides typical functionality used when
processing models. To this effect, it provides funcionality
for 
* parsing, 
* coco-checking, 
* pretty-printing, 
* creating symbol tables, 
* storing symbols in symbol files, and  
* loading symbols from symbol files.  

The requirements for building and using the OCL CLI tool are that (at least) 
JDK 8 (JDK 11 and JDK 14 are also officially supported by us), Git, and Gradle 
are installed and available for use in Bash. 

The following subsection describes how to download the CLI tool.
Then, this document describes how to build the CLI tool from the source files.
Afterwards, this document contains a tutorial for using the CLI tool.  

## Downloading the Latest Version of the CLI Tool
A ready to use version of the CLI tool can be downloaded in the form of an 
executable JAR file.
You can use [**this download link**](https://nexus.se.rwth-aachen.de/service/rest/v1/search/assets/download?sort=version&repository=monticore-snapshots&maven.groupId=de.monticore.lang&maven.artifactId=ocl&maven.extension=jar&maven.classifier=cli) 
for downloading the CLI tool. 

Alternatively, you can download the CLI tool using `wget`.
The following command downloads the latest version of the CLI tool and saves it 
under the name `OCLCLI.jar` in your working directory:
```
wget "https://nexus.se.rwth-aachen.de/service/rest/v1/search/assets/download?sort=version&repository=monticore-snapshots&maven.groupId=de.monticore.lang&maven.artifactId=ocl&maven.extension=jar&maven.classifier=cli" -O OCLCLI.jar
``` 
** //TODO: Nach CLI Release auf monticore.de Link ersetzen **


## Building the CLI Tool from the Sources
 
It is possible to build an executable JAR of the CLI tool from the source files 
located in GitHub.
The following describes the process for building the CLI tool from the source 
files using Bash.
For building an executable Jar of the CLI with Bash from the source files 
available in GitHub, execute the following commands.

First, clone the repository:
```
git clone git@git.rwth-aachen.de:monticore/languages/OCL.git
```
**//TODO: Nach GitHub Release Link ersetzen**

Change the directory to the root directory of the cloned sources:
```
cd OCL
```

Afterwards, build the source files with gradle (if `./gradlew.bat` is not 
recognized as a command in your shell, then use `./gradlew`):
```
./gradlew.bat build
```
Congratulations! You can now find the executable JAR file `OCLCLI.jar` in
 the directory `target/libs` (accessible via `cd target/libs`).

## Tutorial: Getting Started Using the OCL CLI Tool
The previous sections describe how to obtain an executable JAR file
(OCL CLI tool). This section provides a tutorial for
using the OCL CLI tool. The following examples assume
that you locally named the CLI tool `OCLCLI`.
If you build the CLI tool from the sources or used the `wget`
command above, then you are fine. If you manually downloaded 
the CLI tool, then you should consider renaming the downloaded JAR.   

### First Steps
Executing the Jar file without any options prints usage information of the CLI 
tool to the console:
```
$ java -jar OCLCLI.jar
usage: OCLCLI
 -c,--coco <arg>              Checks the CoCos for the input. Optional arguments
                              are:
                              -c intra to check only the intra-model CoCos,
                              -c inter checks also inter-model CoCos,
                              -c type (default) checks all CoCos.
 -cd4c,--cd4code              Load symbol types from cd4c. Shortcut for loading
                              CDTypeSymbol as TypeSymbol,
                              CDMethodSignatureSymbol as FunctionSymbol, and
                              FieldSymbol as VariableSymbol. Furthermore,
                              warnings about not deserializing
                              CDAssociationSymbol and CDRoleSymbol will be
                              ignored.
 -d,--dev                     Specifies whether developer level logging should
                              be used (default is false)
 -fs,--functionSymbol <fqn>   Takes the fully qualified name of one or more
                              symbol kind(s) that should be treated as
                              FunctionSymbol when deserializing symbol files.
 -h,--help                    Prints this help dialog
 -i,--input <file>            Processes the list of OCL input artifacts.
                              Argument list is space separated. CoCos are not
                              checked automatically (see -c).
 -is,--ignoreSymKind <fqn>    Takes the fully qualified name of one or more
                              symbol kind(s) for which no warnings about not
                              being able to deserialize them shall be printed.
                              Allows cleaner CLI outputs.
 -p,--path <directory>        Sets the artifact path for imported
                              symbols.Directory will be searched recursively for
                              files with the ending ".sym". Defaults to the
                              current folder.
 -pp,--prettyprint <file>     Prints the OCL-AST to stdout or the specified file
                              (optional)
 -s,--symboltable <file>      Stores the symbol tables of the input OCL
                              artifacts in the specified files. The n-th input
                              OCL (-i option) is stored in the file as specified
                              by the n-th argument of this option. Default is
                              'target/symbols/{packageName}/{artifactName}.sdsym
                              '.
 -so,--syntaxobjects <file>   Prints an object diagram of the OCL-AST to stdout
                              or the specified file (optional)
 -ts,--typeSymbol <fqn>       Takes the fully qualified name of one or more
                              symbol kind(s) that should be treated as
                              TypeSymbol when deserializing symbol files.
 -vs,--variableSymbol <fqn>   Takes the fully qualified name of one or more
                              symbol kind(s) that should be treated as
                              VariableSymbol when deserializing symbol files.
```
To work properly, the CLI tool needs the mandatory argument `-i,--input <arg>`, 
which takes the file paths of at least one input file containing SD models.
If no other arguments are specified, the CLI tool solely parses the model(s).

For trying this out, copy the `OCLCLI.jar` into a directory of your 
choice. 
Afterwards, create  containing the following simple OCL :
```
ocl Example {
}
```

Save the text file as `Example.ocl` in the directory where `OCLCLI.jar` is 
located. 

Now execute the following command:
```
java -jar OCLCLI.jar -i Example.ocl
```

You may notice that the CLI tool prints no output to the console.
This means that the tool has parsed the file `Example.ocl` successfully.

### Step 2: Pretty-Printing
The CLI tool provides a pretty-printer for the OCL language.
A pretty-printer can be used, e.g., to fix the formatting of files containing 
OCL.
To execute the pretty-printer, the `-pp,--prettyprint` option can be used.
Using the option without any arguments pretty-prints the models contained in the
input files to the console.

Execute the following command for trying this out:
```
java -jar OCLCLI.jar -i Example.ocl -pp
```
The command prints the pretty-printed model contained in the input file to the 
console:
```
ocl Example {
}
```

It is possible to pretty-print the models contained in the input files to output
files.
For this task, it is possible to provide the names of output files as arguments 
to the `-pp,--prettyprint` option.
If arguments for output files are provided, then the number of output files must
be equal to the number of input files.
The i-th input file is pretty-printed into the i-th output file.

Execute the following command for trying this out:
```
java -jar OCLCLI.jar -i Example.ocl -pp Output.ocl
```
The command prints the pretty-printed model contained in the input file into the 
file `Output.ocl`.

### Step 3: Checking Context Conditions
For checking context conditions, the `-c,--coco <arg>` option can be used. 
Using this option without any arguments checks whether the model satisfies all 
context conditions. 

If you are only interested in checking whether a model only satisfies a subset 
of the context conditions or want to explicate that all context conditions 
should be checked, you can do this by additionally providing one of the three
arguments `intra`, `inter`, and `type`.
* Using the argument `intra` only executes context conditions concerning 
  violations of intra-model context conditions.
  These context conditions, for example, check naming conventions. 
* Using the argument `inter` executes all intra-model context conditions and 
  additionally checks whether type names in constructor signatures are defined.
* Using the argument `type` executes all context coniditions. 
  These context conditions include checking whether used types and methods 
  exist. 
  The behavior when using the argument `type` is the equal to the default 
  behavior when using no arguments. 

Execute the following command for trying out a simple example:
```
java -jar OCLCLI.jar -i Example.ocl -c -cd4c
```
You may notice that the CLI prints nothing to the console when executing this 
command.
This means that the model satisfies all context condtions. 

Let us now consider a more complex example.
Recall the OCL `Bookshop` from the `An Example Model` section above.
For continuing, copy the textual representation of the OCL `Bookshop` and 
save it in a file `Bookshop.ocl` in the directory where the file `OCLCLI.jar` 
is located. 
For this you will need a symbol file containing the symbols of a class diagram
corresponding to the `Bookshop.ocl`. 
This will be explained in more detail in the following section. 
For now, just add `-p src/test/resources/docs/Bookshop/ -cd4c` to the command to use tell 
the CLI where to find the symbol file prepared for this example and how to 
process it.

You can check the different kinds of context conditions, using the 
`-c,--coco <arg>` option:
```
java -jar OCLCLI.jar -i Bookshop.ocl -p src/test/resources/docs/Bookshop/ -cd4c -c intra
```
```
java -jar OCLCLI.jar -i Bookshop.ocl -p src/test/resources/docs/Bookshop/ -cd4c -c inter
```
```
java -jar OCLCLI.jar -i Bookshop.ocl -p src/test/resources/docs/Bookshop/ -cd4c -c type
```
None of these commands should produce output. 

To see an error lets add a mistake to the model. 
Replace this line in `Bookshop.ocl`
```
{ book | Book book in booksInStock, book.iban == iban }
```
by 
```
{ book2 | Book book in booksInStock, book.iban == iban }
```
As `book2` is undefined, the CLI should now print an Error message when 
checking the cocos:

```
$ java -jar OCLCLI.jar  -i Bookshop.ocl -p src/test/resources/docs/Bookshop/ -cd4c -c
[INFO]  DeriveSymTypeOfExpression package suspected
[ERROR] 0xA0309 Bookshop.ocl:<13,12> Could not calculate type of expression "book2" on the left side of SetComprehension
```

Please remember to undo the "mistake". 

# TODO AB HIER VON SEQUENCE DIAGRAM ÜBERNOMMEN ; ANPASSEN FÜR OCL

### Step 4: Using the Model Path to Resolve Symbols

In this section we make use of the model path and provide the CLI tool with
a symbol file (stored symbol table) of another model, which contains the 
necessary type information.

Create a new directory `mytypes` in the directory where the CLI tool 
`OCLCLI.jar` is located.
The symbol file `Types.typesym` of a model, which provides all necessary type 
information, can be found [here](doc/Types.typesym).
Download the file, name it `Types.typesym`, and move it into the directory 
`mytypes`.

The contents of the symbol file are of minor importance for you as a language 
user. 
In case you are curious and had a look into the symbol file: 
The symbol file contains a JSON representation of the symbols defined in a 
model.
In this case, the symbol file contains information about defined types. 
Usually, the CLI tools of MontiCore languages automatically generate the 
contents of these files and you, as a language user, must not be concerned with 
their contents. 
  
The path containing the directory structure that contains the symbol file is 
called the "Model Path".
If we provide the model path to the tool, it will search for symbols in symbol 
files, which are stored in directories contained in the model path.
So, if we want the tool to find our symbol file, we have to provide the model 
path to the tool via the `-path <arg>` option:
```
java -jar OCLCLI.jar -i Bid.ocl -c type -path <MODELPATH>
```
where `<MODELPATH>` is the path where you stored the downloaded symbol file.
In our example, in case you stored the model in the directory `mytypes`,
execute the following command:
```
java -jar OCLCLI.jar -i Bid.ocl -c type -path mytypes
```

Well, executing the above command still produces the same error message.
This is because the symbol file needs to be imported first, just like in Java.
Therefore, we add the following import statement to the beginning of the 
contents contained in the file `Bid.ocl` containing the SD `Bid`:
```
import Types.*;

ocl Bid {
  ...
}
```
The added import statement means that the file containing the SD imports all 
symbols that are stored in the symbol file `Types`. 
Note that you may have to change the name here, depending on how you named the 
symbol file from above.
The concrete file ending `.typesym` is not important 
in this case. However, the file ending of the symbol file must end with `sym`, 
i.e., the name of the symbol file must be compatible to the pattern `*.*sym`.
If you strictly followed the instructions of this tutorial, then you are fine.

If we now execute the command again, the CLI tool will print no output. 
This means that it processed the model successfully without any context 
condition violations. Great! 

### Step 5: Storing Symbols
The previous section describes how to load symbols from an existing symbol file.
Now, we will use the CLI tool to store a symbol file for our `Bid.ocl` model.
The stored symbol file will contain information about the objects defined in the 
SD.
It can be imported by other models for using the symbols introduced by these 
object definitions,
similar to how we changed the file `Bid.ocl` for importing the symbols contained 
in the symbol file `Types.typessym`.

Using the `-s,-symboltable <arg>` option builds the symbol tables of the input 
models and stores them in the file paths given as arguments.
Either no file paths must be provided or exactly one file path has to be 
provided for each input model.
The symbol file for the i-th input model is stored in the file defined by the 
i-th file path. 
If you do not provide any file paths, the CLI tool stores the symbol table of 
each input model in the symbol file 
`target/symbols/{packageName}/{fileName}.oclsym` where `packageName` is the name 
of the package as specified in the file containing the model and `fileName` is 
the name of the file containing the model. 
The file is stored relative to the working directory, i.e., the directory in 
which you execute the command for storing the symbol files.
Furthermore, please notice that in order to store the symbols properly, the 
model has to be well-formed in all regards, and therefore all context conditions 
are checked beforehand.

For storing the symbol file of `Bid.ocl`, execute the following command 
(the implicit context condition checks require using the model path option):
```
java -jar OCLCLI.jar -i Bid.ocl -path mytypes -s
```
The CLI tool produces the file `target/symbols/Bid.oclsym`, which can now be 
imported by other models, e.g., by models that need to use some of the objects 
defined in the SD `Bid`.

For storing the symbol file of `Bid.ocl` in the file `syms/BidSyms.oclsym`, 
for example, execute the following command
(again, the implicit context condition checks require using the model path 
option):
```
java -jar OCLCLI.jar -i Bid.ocl -path mytypes -s syms/BidSyms.oclsym
```

Congratulations, you have just finished the tutorial about saving SD symbol 
files!

## Further Information

* [Project root: MontiCore @github](https://github.com/MontiCore/monticore)
* [MontiCore documentation](http://www.monticore.de/)
* [**List of languages**](https://github.com/MontiCore/monticore/blob/dev/docs/Languages.md)
* [**MontiCore Core Grammar Library**](https://github.com/MontiCore/monticore/blob/dev/monticore-grammar/src/main/grammars/de/monticore/Grammars.md)
* [Best Practices](https://github.com/MontiCore/monticore/blob/dev/docs/BestPractices.md)
* [Publications about MBSE and MontiCore](https://www.se-rwth.de/publications/)
* [Licence definition](https://github.com/MontiCore/monticore/blob/master/00.org/Licenses/LICENSE-MONTICORE-3-LEVEL.md)

