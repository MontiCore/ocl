package de.monticore.oclrefadaptation;

import de.monticore.refadaptation.Binding;
import org.junit.jupiter.api.Test;

public class BindingCastTest {

  @Test
  void test() {
    Foo refF = new Foo();
    Foo fInc = new Foo();

    Binding<Foo> binding = Binding.createStrict(refF, fInc);

    Binding<SpcialFoo> casted = binding.cast(); // should fail
    SpcialFoo special = casted.getReferenceElement();
  }

  @Test
  void testSpecial() {
    Foo refF = new SpcialFoo();
    Foo fInc = new SpcialFoo();

    Binding<SpcialFoo> binding = Binding.createStrict(refF, fInc).cast();

    Binding<Foo> fooBinding = binding.cast();
    System.out.println(fooBinding);

    Binding<SpcialFoo> specialCased = binding.cast();
  }

  class Foo {

  }

  class SpcialFoo extends Foo {

  }
}
