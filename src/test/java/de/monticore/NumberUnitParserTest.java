/**
 * ******************************************************************************
 *  MontiCAR Modeling Family, www.se-rwth.de
 *  Copyright (c) 2017, Software Engineering Group at RWTH Aachen,
 *  All rights reserved.
 *
 *  This project is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 3.0 of the License, or (at your option) any later version.
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * *******************************************************************************
 */
package de.monticore;

import de.monticore.numberunit._ast.ASTNumberWithUnit;
import de.monticore.numberunit._parser.NumberUnitParser;
import de.se_rwth.commons.logging.Log;
import org.junit.*;

import java.io.IOException;

import static org.junit.Assert.*;

  /**
   * Created by Michael von Wenckstern on 10.02.2017.
   */
  public class NumberUnitParserTest {

    static boolean failQuick;

    @BeforeClass
    public static void startUp() {
      failQuick = Log.isFailQuickEnabled();
      Log.enableFailQuick(false);
    }

    @AfterClass
    public static void tearDown() {
      Log.enableFailQuick(failQuick);
    }

    @Before
    public void clear() {
      Log.getFindings().clear();
    }

    @Test
    public void testDegree20() throws IOException {
      NumberUnitParser parser = new NumberUnitParser();
      ASTNumberWithUnit ast = parser.parse_String("7°").orElse(null);
      assertNotNull(ast);
      ast = parser.parse_String("-9°C").orElse(null);
      assertNotNull(ast);
     // assertEquals(Rational.valueOf(-9,1), ast.getUnitNumber().get().getNumber().get());
     // assertEquals(Unit.valueOf("°C"), ast.getUnitNumber().get().getUnit().get());
    }

    @Test
    public void testM2() throws IOException {
      NumberUnitParser parser = new NumberUnitParser();
      ASTNumberWithUnit ast = parser.parse_String("-0.9").orElse(null);
      assertNotNull(ast);

      // assertEquals(Rational.valueOf(-9, 10), ast.getNumber().get());
      // assertEquals(Unit.ONE, ast.getUnit().get());
    }

    @Test
    public void testM1() throws IOException {
      NumberUnitParser parser = new NumberUnitParser();
      ASTNumberWithUnit ast = parser.parse_String("-0.5 kg*m^2/s^3").orElse(null);
      assertNotNull(ast);

      // assertEquals(Rational.valueOf(-1, 2), ast.getNumber().get());
      // assertEquals(Unit.valueOf("kg*m^2/s^3"), ast.getUnit().get());
    }

    @Test
    public void test0() throws IOException {
      NumberUnitParser parser = new NumberUnitParser();
      ASTNumberWithUnit ast = parser.parse_String("8/3 kg*m^2/s^3").orElse(null);
      assertNotNull(ast);

//      assertEquals(Rational.valueOf(8, 3), ast.getNumber().get());
//      assertEquals(Unit.valueOf("kg*m^2/s^3"), ast.getUnit().get());
    }

    @Test
    public void test1() throws IOException {
      NumberUnitParser parser = new NumberUnitParser();
      ASTNumberWithUnit ast = parser.parse_String("8 kg*m^2/s^3").orElse(null);
      assertNotNull(ast);

//      assertEquals(Rational.valueOf(8, 1), ast.getNumber().get());
//      assertEquals(Unit.valueOf("kg*m^2/s^3"), ast.getUnit().get());
    }

    @Test
    public void test2() throws IOException {
      NumberUnitParser parser = new NumberUnitParser();
      ASTNumberWithUnit ast = parser.parse_String("0.3e-7").orElse(null);
      assertNotNull(ast);

      //      assertEquals(Rational.valueOf(8, 1), ast.getNumber().get());
      //      assertEquals(Unit.valueOf("kg*m^2/s^3"), ast.getUnit().get());
    }

    @Test
    public void test3() throws IOException {
      Log.enableFailQuick(false);
      NumberUnitParser parser = new NumberUnitParser();
      ASTNumberWithUnit ast = parser.parse_String("-7.3 +0.5j").orElse(null);
      assertNull(ast);
    }


    @Test
    public void test4() throws IOException {
      NumberUnitParser parser = new NumberUnitParser();
      ASTNumberWithUnit ast = parser.parse_String("-7.3 +0.5i").orElse(null);
      assertNotNull(ast);

      assertTrue(ast.complexNumberIsPresent());
      assertTrue(ast.complexNumberIsPresent());
      assertEquals(-7.3, ast.getComplexNumber().getRealNumber(), 0.0001);
      assertEquals(0.5, ast.getComplexNumber().getImagineNumber(), 0.0001);
    }

    @Test
    public void test5() throws IOException {
      NumberUnitParser parser = new NumberUnitParser();
      ASTNumberWithUnit ast = parser.parse_String("1-2i").orElse(null);
      assertNotNull(ast);

      assertTrue(ast.complexNumberIsPresent());
      assertEquals(1, ast.getComplexNumber().getRealNumber(), 0.0001);
      assertEquals(-2, ast.getComplexNumber().getImagineNumber(), 0.0001);
    }

    @Test
    public void test6() throws IOException {
      NumberUnitParser parser = new NumberUnitParser();
      ASTNumberWithUnit ast = parser.parse_String("1 -2i").orElse(null);
      assertNotNull(ast);

      assertTrue(ast.complexNumberIsPresent());
      assertEquals(1, ast.getComplexNumber().getRealNumber(), 0.0001);
      assertEquals(-2, ast.getComplexNumber().getImagineNumber(), 0.0001);
    }

    @Test
    public void test7() throws IOException {
      NumberUnitParser parser = new NumberUnitParser();
      ASTNumberWithUnit ast = parser.parse_String("1  -  2i").orElse(null);
      assertNotNull(ast);

      assertTrue(ast.complexNumberIsPresent());
      assertEquals(1, ast.getComplexNumber().getRealNumber(), 0.0001);
      assertEquals(-2, ast.getComplexNumber().getImagineNumber(), 0.0001);
    }


    @Test
    public void test9() throws IOException {
      NumberUnitParser parser = new NumberUnitParser();
      ASTNumberWithUnit ast = parser.parse_String("-0.5-0.5i").orElse(null);
      assertNotNull(ast);

      assertTrue(ast.complexNumberIsPresent());
      assertEquals(-0.5, ast.getComplexNumber().getRealNumber(), 0.0001);
      assertEquals(-0.5, ast.getComplexNumber().getImagineNumber(), 0.0001);
    }

    @Test
    public void testImperialUnit()  throws IOException {
      NumberUnitParser parser = new NumberUnitParser();
      ASTNumberWithUnit ast = parser.parse_String("7 th").orElse(null);
      assertNotNull(ast);
    }

    @Test
    public void testInfinite() throws IOException {
      NumberUnitParser parser = new NumberUnitParser();
      ASTNumberWithUnit ast = parser.parse_String("-oo km/h").orElse(null);
      assertNotNull(ast);
//      System.out.println(ast);
//      System.out.println(ast.getUnitNumber().get().tUnitInfIsPresent());
//      System.out.println(ast.getUnitNumber().get().getUnit());
    }

    @Test
    public void testDegree() throws IOException{
      NumberUnitParser parser = new NumberUnitParser();
      ASTNumberWithUnit ast = parser.parse_String("-90 °").orElse(null);
      assertNotNull(ast);
//      System.out.println(ast);

    }

    @Test
    public void testDegree2() throws IOException{
      NumberUnitParser parser = new NumberUnitParser();
      ASTNumberWithUnit ast = parser.parse_String("-90 deg").orElse(null);
      assertNotNull(ast);
//      System.out.println(ast);

    }
}
