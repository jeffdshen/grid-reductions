package transform;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class FrobeniusSolverTest {

    @Test
    public void testGetCoefficients() throws Exception {
        FrobeniusSolver frobSolver = new FrobeniusSolver(new int[]{30,105,70,42});
        assertEquals(frobSolver.getSolvableCutoff(), 383);
        int[] test = frobSolver.getCoefficients(385);
        assertEquals(test[0]*30 + test[1]*105 + test[2]*70 + test[3]*42, 385);
        test = frobSolver.getCoefficients(4673);
        assertEquals(test[0]*30 + test[1]*105 + test[2]*70 + test[3]*42, 4673);
        test = frobSolver.getCoefficients(134652);
        assertEquals(test[0]*30 + test[1]*105 + test[2]*70 + test[3]*42, 134652);
    }
}