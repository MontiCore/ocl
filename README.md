[![Maintainability](https://api.codeclimate.com/v1/badges/c79dfbc30be9a027ced2/maintainability)](https://codeclimate.com/github/MontiCore/OCL/maintainability)
[![Build Status](https://travis-ci.org/MontiCore/OCL.svg?branch=master)](https://travis-ci.org/MontiCore/OCL)
 [![Java 8](https://img.shields.io/badge/java-8-blue.svg)](http://java.oracle.com)
 [![PPTX-Docu](https://img.shields.io/badge/PPTX--Docu-2018--05--22-brightgreen.svg)](https://github.com/EmbeddedMontiArc/Documentation/blob/master/reposlides/18.05.22.Docu.OCL.pdf)


# OCL

A DSL parsing OCL expressions

Also see [Documentation](https://git.rwth-aachen.de/monticore/languages/OCL/tree/master/documentation) for further information about type checking and grammar.

# Download
* Please download the latest jar from: http://nexus.se.rwth-aachen.de/#browse/search=keyword%3Docl%20AND%20group.raw%3Dde.monticore.lang
  * you need to log-in with your credentials
  * please download `de/monticore/lang/ocl/${version}/ocl-${long-version}-cli.jar`

# How to use the CLI

[![](/uploads/c313faf5fac17218ae7ec740de12b35a/image.png)](http://www.youtube.com/watch?v=qjpyTzGyvEM "")


```
java -jar ocl-${version}-cli.jar -path "C:\Path\to\Project" -ocl example.ocl.Rule
```
-path defines the path to the project folder, can be relative

-ocl defines the qualified name to the OCL model within the project folder

# Features
The CLI will first try to load the OCL model

E.g. example.ocl.Rule :
```
package example.ocl;

import example.cd.AuctionCD;
import example.cd.DefaultTypes;

context Auction a inv:
	a.participants.size > 0

```

It will try to load all imports as classdiagramms in the project folder

E.g. example.cd.AuctionCD :
```
package example.cd;

classdiagram AuctionCD {

  class Auction;
  class Person;
  
  association participants [*] Auction <-> Person [*];

}
```

And then will verify that the OCL models type-integrety holds according to the classdiagramms.

E.g. `a.participants.size` can be resolved according to `AuctionCD` and `DefaultTypes` and yields an Integer value.

It is important to import the DefaultTypes to support basic types like Integer, Double, Set, List, etc. 
