<!-- (c) https://github.com/MontiCore/monticore -->
<!-- Alpha: This is intended to become a MontiCore stable explanation. -->

This documentation is intended for **language engineers** using or extending the
OCL languages. 
A detailed documentation for **modelers** who use the languages of the object
constraint language (OCL) project is located 
**[here](../../../../../../README.md)**. 
We recommend that **language engineers** read this documentation for 
**modelers** before reading this documentation.

# Object Constraint Language for Programming (OCL/P)
The module described in this document defines a MontiCore language for OCL. 
OCL/P is an OCL variant that is suited e.g. for the modeling of tests and 
(in combination e.g. with class diagrams) for desired and unwanted behavior 
interactions. 
OCL/P is explained in detail by [Rum16], [Rum17].
                       

This module contains 
* four grammars, 
* context conditions (including typechecks), 
* a symbol table infrastructure including functionality for 
  creating symbol tables and (de-)serializing symbol tables, 
* pretty-printers, and
* a command-line interface (CLI). 


## An Example Model

The following model describes OCL constraints to check a model an online book 
store.

``` 
ocl Bookshop {
  context Shop s inv CustomerPaysBeforeNewOrder:      // invariant
    forall Customer c in s.customers:                 // quantifiers available
      c.allowedToOrder implies !exists Invoice i in s.invoices:
        i.customer == c && i.moneyPayed < i.invoiceAmount ;

  // Method specification for selling a book
  context Invoice Stock.sellBook(String iban, int discountPercent, Customer c) 
    let availableBooks =                              // set comprehension
          { book | Book book in booksInStock, book.iban == iban }
    pre:  !availableBooks.isEmpty &&                  // precondition
          c.allowedToOrder;
    post: let discount = (100 - discountPercent)/100; // postcondition, let
              b = result.soldBook                     // result variable 
          in                                        
              !(b isin booksInStock) &&
              booksInStock.size@pre == booksInStock.size + 1 &&  // @pre
              result.invoiceAmount == b.price * discount;  // result variable 
}
```

## Command Line Interface (CLI) Usage

The class [```OCLCLI```](../../../../java/de/monticore/ocl/OCLCLI.java) 
provides typical functionality used when processing models. 
To this effect, the class provides methods for parsing, pretty-printing, 
creating symbol tables, storing symbols, and loading symbols. 

The class provides a `main` method and can thus be used as a CLI. 
Building this gradle project yields the executable jar `OCLCLI.jar`, which can 
be found in the directory `target/libs`. 
The usage of the `OCLCLI` tool and detailed instructions for building the tool 
from the source files are described **[here](../../../../../../README.md)**. 

## Grammars

This module contains four grammars:
1. [OCL](./OCL.mc4),
1. [OCLExpressions](./OCLExpressions.mc4), 
1. [OptionalOperators](./OptionalOperators.mc4), and
1. [SetExpressions](./SetExpressions.mc4). 

### OCL
The grammar [OCL](./OCL.mc4) contains the basic constituents to define textual 
representations of OCL/P in an artifact. 
A detailed documentation of the grammar can be found in the 
[artifact defining the grammar](./OCL.mc4). 

The grammar [OCL](./OCL.mc4) defines the syntax for 
* OCL artifacts, 
* context definitions,
* pre- and postconditions, and
* invariants.
                            
The grammar [OCL](./OCL.mc4) extends the grammars
* [UMLStereotype][UMLStereotypeRef] for adding UMLStereotypes as possible extension 
  points to the grammar,
* [MCSimpleGenericTypes][MCSimpleGenericTypesRef] for using generic types within 
  OCL,
* [OCLExpressions](./OCLExpressions.mc4) for using expressions defined by OCL, 
* [OptionalOperators](./OptionalOperators.mc4) for easy access to optional 
  values,
* [SetExpressions](./SetExpressions.mc4) to be able to define and use sets in a
  math-like syntax, and
* [BitExpressions][BitExpressionsRef] for using binary expressions in OCL.

### OCLExpressions
The grammar [OCLExpressions](./OCLExpressions.mc4) defines the syntax for basic 
expressions defined by the OCL standard.

The grammar [OCLExpressions](./OCLExpressions.mc4) defines the syntax for
* forall and exists quantifiers,
* any-element-selection from collections,
* let-in local variable declarations,
* type-if expressions, 
* if-then-else expressions,
* implies and equivalent expressions,
* iterate expressons, 
* @pre and transitive closure qualifications, and
* type checks and casts.

The grammar [OCLExpressions](./OCLExpressions.mc4) extends the grammars
* [ExpressionsBasis][ExpressionsBasisRef] to reuse expressions defined by other 
  grammars,
* [MCBasicTypes][MCBasicTypesRef] for referencing types, and
* [BasicSymbols][BasicSymbolsRef] for importing type symbols.

### OptionalOperators
The grammar [OCLExpressions](./OCLExpressions.mc4) defines expressions for 
accessing optional values (i.e. potentially absent values).
These operators are also known as [Elvis operators][ElvisRef]

The grammar [OptionalOperators](./OptionalOperators.mc4) defines the syntax for
* accessing optionals in a possibly chained manner("?:"),
* equality and inequality checks on optionals ("?<=", "?==", etc.), and
* type checks on optionals ("?~~", "?!~"), 

The grammar [OptionalOperators](./OptionalOperators.mc4) extends the grammars
* [CommonExpressions][CommonExpressionsRef] for declaring optional operators as
  `InfixExpression`.

### SetExpressions
The grammar [SetExpressions](./SetExpressions.mc4) defines the syntax for 
defining and combining sets in a math-like syntax.

The grammar [SetExpressions](./SetExpressions.mc4) defines the syntax for
* set comprehension (also known as set-builder notation),
* checking set membership,
* set-theoretic union, intersection, and
* logical AND and OR predicates on all elements of a set.

The grammar [SetExpressions](./SetExpressions.mc4) extends the grammars
* [ExpressionsBasis][ExpressionsBasisRef] to reuse expressions defined by other 
  grammars,
* [MCBasicTypes][MCBasicTypesRef] for referencing types, and
* [BasicSymbols][BasicSymbolsRef] for importing type symbols.


## Context Conditions
This section lists the context conditions for the OCL module. 

* The context condition [```ConstructorNameReferencesType```](../../../../java/de/monticore/ocl/ocl/_cocos/ConstructorNameReferencesType.java)  
that the type referenced by a constructor exists.

* The context condition [```ContextHasOnlyOneType```](../../../../java/de/monticore/ocl/ocl/_cocos/ContextHasOnlyOneType.java)  
that context definitions do not reference more than one type without assigning 
the local variable to be instantiated from that type an explicit name. 

* The context condition [```ContextVariableNamesAreUnique```](../../../../java/de/monticore/ocl/ocl/_cocos/ContextVariableNamesAreUnique.java)  
checks that variable names in context declarations are not declared more than 
once.

* The context condition [```ExpressionHasNoSideEffect```](../../../../java/de/monticore/ocl/ocl/_cocos/ExpressionHasNoSideEffect.java)  
checks that the expressions used in OCL artifacts are free of side effects.

* The context condition [```IterateExpressionVariableUsageIsCorrect```](../../../../java/de/monticore/ocl/ocl/_cocos/IterateExpressionVariableUsageIsCorrect.java)  
checks the variable introduced by an iterate expression is used in the accumulator part of the expression.

* The context condition [```ParameterNamesUnique```](../../../../java/de/monticore/ocl/ocl/_cocos/ParameterNamesUnique.java)  
checks the names of parameters are not already declared. 

* The context condition [```PreAndPostConditionsAreBooleanType```](../../../../java/de/monticore/ocl/ocl/_cocos/PreAndPostConditionsAreBooleanType.java)  
checks the expressions used in pre- and postconditions return booleans.

* The context condition [```SetComprehensionHasGenerator```](../../../../java/de/monticore/ocl/ocl/_cocos/SetComprehensionHasGenerator.java)  
checks the set comprehensions always have a generator part.

* The context condition [```UnnamedInvariantDoesNotHaveParameters```](../../../../java/de/monticore/ocl/ocl/_cocos/UnnamedInvariantDoesNotHaveParameters.java)  
checks unnamed invariants do not have parameters.

* The context condition [```ValidTypes```](../../../../java/de/monticore/ocl/ocl/_cocos/ValidTypes.java)  
performs all type checks on expressions used in OCL artifacts.


### Naming Conventions

* The context condition [```ConstructorNameStartsWithCapitalLetter```](../../../../java/de/monticore/ocl/ocl/_cocos/ConstructorNameStartsWithCapitalLetter.java)  
that name of a constructor starts with an uppercase letter.

* The context condition [```InvariantNameStartsWithCapitalLetter```](../../../../java/de/monticore/ocl/ocl/_cocos/InvariantNameStartsWithCapitalLetter.java)  
checks that names of invariants start with an uppercase letter.

* The context condition [```MethSignatureStartsWithLowerCaseLetter```](../../../../java/de/monticore/ocl/ocl/_cocos/MethSignatureStartsWithLowerCaseLetter.java)  
checks that method names start with a lowercase letter.


## Symbol Table

The OCL uses the build-in
symbol types [```VariableSymbol```][BasicSymbolsRef]
and [```Diagram```][BasicSymbolsRef]
as well as the [type symbol types][TypeSymbolsRef] of MontiCore. 

OCL introduces symbols for all named invariants.
Locally, OCL further introduces symbols for
* local variable declarations ([```VariableSymbol```][BasicSymbolsRef]),
* parameters declarations ([```VariableSymbol```][BasicSymbolsRef]),
* an implicit read-only `result` variable ([```VariableSymbol```][BasicSymbolsRef]) 
  in constraints using a method as context,
* an implicit read-only `this` variable ([```VariableSymbol```][BasicSymbolsRef]) 
  in invariants using a type as context,
* an implicit read-only `super` variable ([```VariableSymbol```][BasicSymbolsRef]) 
  in invariants using a type with a supertype as context, and
* an implicit read-only variable with the name of the referenced type starting 
  with a lowercase letter ([```VariableSymbol```][BasicSymbolsRef]) 
  in invariants using a type with as context.


## Symbol kinds used by the OCL (importable or subclassed):
The OCL uses symbols of kind [```TypeSymbol```][BasicSymbolsRef] and 
[```MethodSymbol```][OOSymbolsRef] to reference types and methods in 
constraints.

// TODO: MethodSymbol wird aktuell nicht wirklich importiert. OCL.mc4 müsste dazu auch OOTypeSymbols extenden

## Symbol kinds defined by the OCL (exported):
* OCL exports  an `InvariantSymbol` for every named invariant.

## Serialization and De-serialization of Symbol Tables

The OCL uses the DeSer implementations as generated by MontiCore
without any handwritten extensions. 
Local varibles and paramters are not exported (they are only known in the 
respective expressions that define them.
Therefore, the corresponding variable symbols are not serialized, i.e., they
are not part of the corresponding symbol file. 
For example, the following depicts an excerpt of the symbol file obtained from 
serializing the symbol table instance depicted in "An Example Model":

```json
{ 
  //TODO: waits for CD4A Symbol Table fixes
}
```

## Further Information

* [Project root: MontiCore @github](https://github.com/MontiCore/monticore)
* [MontiCore documentation](http://www.monticore.de/)
* [**List of languages**](https://github.com/MontiCore/monticore/blob/dev/docs/Languages.md)
* [**MontiCore Core Grammar Library**](https://github.com/MontiCore/monticore/blob/dev/monticore-grammar/src/main/grammars/de/monticore/Grammars.md)
* [Best Practices](https://github.com/MontiCore/monticore/blob/dev/docs/BestPractices.md)
* [Publications about MBSE and MontiCore](https://www.se-rwth.de/publications/)
* [Licence definition](https://github.com/MontiCore/monticore/blob/master/00.org/Licenses/LICENSE-MONTICORE-3-LEVEL.md)

[BasicSymbolsRef]:https://github.com/MontiCore/monticore/blob/dev/monticore-grammar/src/main/grammars/de/monticore/symbols/BasicSymbols.mc4
[TypeSymbolsRef]:https://github.com/MontiCore/monticore/tree/dev/monticore-grammar/src/main/grammars/de/monticore/types
[MCBasicTypesRef]:https://github.com/MontiCore/monticore/blob/dev/monticore-grammar/src/main/grammars/de/monticore/types/MCBasicTypes.mc4
[OOSymbolsRef]:https://github.com/MontiCore/monticore/blob/dev/monticore-grammar/src/main/grammars/de/monticore/symbols/OOSymbols.mc4
[ExpressionsBasisRef]:https://github.com/MontiCore/monticore/blob/dev/monticore-grammar/src/main/grammars/de/monticore/expressions/ExpressionsBasis.mc4
[UMLStereotypeRef]:https://github.com/MontiCore/monticore/blob/dev/monticore-grammar/src/main/grammars/de/monticore/UMLStereotype.mc4
[MCCommonLiteralsRef]:https://github.com/MontiCore/monticore/blob/dev/monticore-grammar/src/main/grammars/de/monticore/literals/MCCommonLiterals.mc4
[CommonExpressionsRef]:https://github.com/MontiCore/monticore/blob/dev/monticore-grammar/src/main/grammars/de/monticore/expressions/CommonExpressions.mc4
[MCSimpleGenericTypesRef]:https://github.com/MontiCore/monticore/blob/dev/monticore-grammar/src/main/grammars/de/monticore/types/MCSimpleGenericTypes.mc4
[BitExpressionsRef]:https://github.com/MontiCore/monticore/blob/dev/monticore-grammar/src/main/grammars/de/monticore/expressions/BitExpressions.mc4
[ElvisRef]:https://en.wikipedia.org/wiki/Elvis_operator
