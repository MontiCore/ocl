<!-- (c) https://github.com/MontiCore/monticore -->
<!-- Alpha: This is intended to become a MontiCore stable explanation. -->

This documentation is intended for **language engineers** using or extending the
OCL languages. 
A detailed documentation for *modelers* who use the languages of the object
constraint language (OCL) project is located 
**[here](../../../../../../README.md)**. 
We recommend that language engineers read this documentation for 
modelers before reading this documentation.

# Object Constraint Language for Programming (OCL/P)

This document describe the MontiCore language **OCL/P**. 
OCL/P is an OCL variant that is suited e.g. for the modeling of 
invariants and method pre/post conditions.
It can be used during development, but also for modelling tests or 
for desired or unwanted situations. 
OCL/P is explained in detail by [Rum16], [Rum17].
                       
This OCL language component contains 
* one grammar, 
* context conditions (including typechecks), 
* a symbol table infrastructure including functionality for 
  creating symbol tables and (de-)serializing symbol tables, 
* pretty-printers, and
* a command-line tool. 

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

## Command Line Interface Usage

The class [```OCLTool```](../../../../java/de/monticore/ocl/ocl/OCLTool.java) 
provides typical functionality used when processing models. 
To this effect, the class provides methods for parsing, pretty-printing, 
creating symbol tables, storing symbols, and loading symbols. 

The class provides a `main` method and can thus be used from the command line. 
Building this gradle project yields the executable jar `MCOCL.jar`, which can 
be found in the directory `target/libs`. 
The usage of the `MCOCL` tool and detailed instructions for building the tool 
from the source files are described **[here](../../../../../../README.md)**. 

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
  math-like syntax, as well as list as enumeration of values, and
* [BitExpressions][BitExpressionsRef] for using binary expressions in OCL.

## Context Conditions
This section lists the context conditions for the OCL language.

* [```ConstructorNameReferencesType```](../../../../java/de/monticore/ocl/ocl/_cocos/ConstructorNameReferencesType.java)  
ensures that the type referenced by a constructor exists.

* [```ContextHasOnlyOneType```](../../../../java/de/monticore/ocl/ocl/_cocos/ContextHasOnlyOneType.java)  
that context definitions do not reference more than one type without assigning 
the local variable to be instantiated from that type an explicit name. 

* [```ContextVariableNamesAreUnique```](../../../../java/de/monticore/ocl/ocl/_cocos/ContextVariableNamesAreUnique.java)  
checks that variable names in context declarations are not declared more than 
once.

* [```ExpressionHasNoSideEffect```](../../../../java/de/monticore/ocl/ocl/_cocos/ExpressionHasNoSideEffect.java)  
checks that the expressions used in OCL artifacts are free of side effects.
This is needed, because we deliberately choose to use the same 
expression sublanguage as e.g. Java does and thus rule out side effects
via this condition.

* [```IterateExpressionVariableUsageIsCorrect```](../../../../java/de/monticore/ocl/oclexpressions/_cocos/IterateExpressionVariableUsageIsCorrect.java)  
checks the variable introduced by an iterate expression is used in the accumulator part of the expression.

* [```ParameterNamesUnique```](../../../../java/de/monticore/ocl/ocl/_cocos/ParameterNamesUnique.java)  
checks the names of parameters are not already declared. 

* [```PreAndPostConditionsAreBooleanType```](../../../../java/de/monticore/ocl/ocl/_cocos/PreAndPostConditionsAreBooleanType.java)  
checks the expressions used in pre- and postconditions return booleans.

* [```UnnamedInvariantDoesNotHaveParameters```](../../../../java/de/monticore/ocl/ocl/_cocos/UnnamedInvariantDoesNotHaveParameters.java)  
checks unnamed invariants do not have parameters.

* [```ValidTypes```](../../../../java/de/monticore/ocl/ocl/_cocos/ExpressionValidCoCo.java)  
performs all type checks on expressions used in OCL artifacts.


### Naming Conventions

* [```ConstructorNameStartsWithCapitalLetter```](../../../../java/de/monticore/ocl/ocl/_cocos/ConstructorNameStartsWithCapitalLetter.java)  
that name of a constructor starts with an uppercase letter.

* [```InvariantNameStartsWithCapitalLetter```](../../../../java/de/monticore/ocl/ocl/_cocos/InvariantNameStartsWithCapitalLetter.java)  
checks that names of invariants start with an uppercase letter.

* [```MethSignatureStartsWithLowerCaseLetter```](../../../../java/de/monticore/ocl/ocl/_cocos/MethSignatureStartsWithLowerCaseLetter.java)  
checks that method names start with a lowercase letter.


## Symbol Table

The OCL uses the built-in symbol types 
[```VariableSymbol```][BasicSymbolsRef] and 
[```Diagram```][BasicSymbolsRef] as well as the [type symbol 
types][TypeSymbolsRef] of MontiCore. 

OCL introduces symbols for
* named invariants ([```OCLInvariantSymbol```](./OCL.mc4)).
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


## Symbol kinds used by the OCL (subclassed; not importable):

The OCL uses symbols of kind [```VariableSymbol```][BasicSymbolsRef]
for declaring parameters and local variables.
As these are only valid in their local scopes, they cannot be imported.

## Symbol kinds used by the OCL (importable):

The OCL uses symbols of kind [```TypeSymbol```][BasicSymbolsRef], 
[```VariableSymbol```][BasicSymbolsRef] and 
[```FunctionSymbol```][BasicSymbolsRef] to reference types, fields 
and methods in constraints.

## Symbol kinds defined by the OCL (exported):

* OCL exports  an `OCLInvariantSymbol` for every named invariant.
* Other than that: 
  OCL does not define TypeSymbols on its own and VariableSymbols and 
  FunctionSymbols only locally.

## Serialization and De-Serialization of Symbol Tables

The OCL uses the DeSer implementations as generated by MontiCore
without any handwritten extensions. 
Local variables and parameters are not exported (they are only known in the
respective expressions that define them.
Therefore, no variable symbols are serialized, i.e., they
are not part of the corresponding symbol file. 
For example, the following the symbol file obtained from 
serializing the symbol table instance depicted in "An Example Model":

```json
{
  "generated-using": "www.MontiCore.de technology",
  "name": "Bookshop",
  "package": "docs",
  "symbols": [
    {
      "kind": "de.monticore.ocl.ocl._symboltable.OCLInvariantSymbol",
      "name": "CustomerPaysBeforeNewOrder",
      "spannedScope": {
        "isShadowingScope": false,
        "symbols": [
          {
            "kind": "de.monticore.symbols.basicsymbols._symboltable.VariableSymbol",
            "name": "s",
            "type": {
              "kind": "de.monticore.types.check.SymTypeOfObject",
              "objName": "Shop"
            },
            "isReadOnly": true
          }
        ]
      }
    },
    {
      "kind": "de.monticore.symbols.basicsymbols._symboltable.DiagramSymbol",
      "name": "Bookshop"
    }
  ]
}
```

It contains the invariant `CustomerPaysBeforeNewOrder` computer with its 
signature, namely the `Shop s` object.

## Further Information

* [Project root: MontiCore @github](https://github.com/MontiCore/monticore)
* [MontiCore documentation](http://www.monticore.de/)
* [**List of languages**](https://github.com/MontiCore/monticore/blob/opendev/docs/Languages.md)
* [**MontiCore Core Grammar Library**](https://github.com/MontiCore/monticore/blob/opendev/monticore-grammar/src/main/grammars/de/monticore/Grammars.md)
* [Best Practices](https://github.com/MontiCore/monticore/blob/opendev/docs/BestPractices.md)
* [Publications about MBSE and MontiCore](https://www.se-rwth.de/publications/)
* [Licence definition](https://github.com/MontiCore/monticore/blob/master/00.org/Licenses/LICENSE-MONTICORE-3-LEVEL.md)

[BasicSymbolsRef]:https://github.com/MontiCore/monticore/blob/opendev/monticore-grammar/src/main/grammars/de/monticore/symbols/BasicSymbols.mc4
[TypeSymbolsRef]:https://github.com/MontiCore/monticore/tree/opendev/monticore-grammar/src/main/grammars/de/monticore/types
[MCBasicTypesRef]:https://github.com/MontiCore/monticore/blob/opendev/monticore-grammar/src/main/grammars/de/monticore/types/MCBasicTypes.mc4
[OOSymbolsRef]:https://github.com/MontiCore/monticore/blob/opendev/monticore-grammar/src/main/grammars/de/monticore/symbols/OOSymbols.mc4
[ExpressionsBasisRef]:https://github.com/MontiCore/monticore/blob/opendev/monticore-grammar/src/main/grammars/de/monticore/expressions/ExpressionsBasis.mc4
[UMLStereotypeRef]:https://github.com/MontiCore/monticore/blob/opendev/monticore-grammar/src/main/grammars/de/monticore/UMLStereotype.mc4
[MCCommonLiteralsRef]:https://github.com/MontiCore/monticore/blob/opendev/monticore-grammar/src/main/grammars/de/monticore/literals/MCCommonLiterals.mc4
[CommonExpressionsRef]:https://github.com/MontiCore/monticore/blob/opendev/monticore-grammar/src/main/grammars/de/monticore/expressions/CommonExpressions.mc4
[MCSimpleGenericTypesRef]:https://github.com/MontiCore/monticore/blob/opendev/monticore-grammar/src/main/grammars/de/monticore/types/MCSimpleGenericTypes.mc4
[BitExpressionsRef]:https://github.com/MontiCore/monticore/blob/opendev/monticore-grammar/src/main/grammars/de/monticore/expressions/BitExpressions.mc4
[ElvisRef]:https://en.wikipedia.org/wiki/Elvis_operator
