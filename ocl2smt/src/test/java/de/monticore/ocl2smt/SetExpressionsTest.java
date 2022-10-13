package de.monticore.ocl2smt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class SetExpressionsTest extends ExpressionAbstractTest {
    @BeforeEach
    public void setup() throws IOException {
        parse("setExpressions/Set.cd", "setExpressions/Set.ocl");
    }

    @Test
    public void test_isin_set() {
        testInv("All_Person_in_All_Auctions");
    }

    @Test
    public void test_notin_set() {
        testInv("One_Person_in_Any_Auctions");
    }

    @Test
    public void test_transitive_closure() {
        testInv("One_Person_in_Any_Auctions");
    }

}
