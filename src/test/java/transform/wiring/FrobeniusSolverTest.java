package transform.wiring;

import com.google.common.collect.ImmutableList;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class FrobeniusSolverTest {

    @Test
    public void testGetCoefficients() throws Exception {
        FrobeniusSolver frobSolver = new FrobeniusSolver(new int[]{30,105,70,42});
        assertEquals(frobSolver.getLargestUnsolvable(), 383);

        int[] test = frobSolver.getCoefficients(4673);
        assertEquals(test[0]*30 + test[1]*105 + test[2]*70 + test[3]*42, 4673);
        test = frobSolver.getCoefficients(134652);
        assertEquals(test[0]*30 + test[1]*105 + test[2]*70 + test[3]*42, 134652);

        int start = 129845;
        int end = start + 200;
        for (int i = start; i < end; i++) {
            test = frobSolver.getCoefficients(i);
            assertEquals(test[0]*30 + test[1]*105 + test[2]*70 + test[3]*42, i);
        }

        start = 384;
        end = start + 200;
        for (int i = start; i < end; i++) {
            test = frobSolver.getCoefficients(i);
            assertEquals(test[0]*30 + test[1]*105 + test[2]*70 + test[3]*42, i);
        }
    }

    @Test(expectedExceptions = {Exception.class})
    public void testSingleWireException() {
        FrobeniusSolver frobSolver = new FrobeniusSolver(ImmutableList.of(2));
    }

    @Test(expectedExceptions = {Exception.class})
    public void testMultipleWireException() {
        FrobeniusSolver frobSolver = new FrobeniusSolver(ImmutableList.of(6, 3, 24));
    }

    @Test
    public void testSingleCoefficient() throws Exception {
        FrobeniusSolver solver = new FrobeniusSolver(new int[]{1});
        assertEquals(solver.getLargestUnsolvable(), -1);
        assertEquals(solver.getCoefficients(3), new int[]{3});
    }
}