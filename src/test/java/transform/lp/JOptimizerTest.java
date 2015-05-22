package transform.lp;

import com.joptimizer.optimizers.LPOptimizationRequest;
import com.joptimizer.optimizers.LPPrimalDualMethod;
import com.joptimizer.optimizers.OptimizationResponse;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class JOptimizerTest {
    @Test
    public void testCorrectness() throws Exception {
        // Minimize 20x + 67y, subject to -2x - y <= -55, x - 3y <= 29, x>=0, y>=0
        // minimize c * x
        double[] c = new double[] { 20, 67 };

        // Gx <= h
        double[][] G = new double[][] {{-2., -1.}, {1., -3.}};
        double[] h = new double[] {-55., 29.};

        // x >= 0
        double[] lb = {0, 0};

        // problem
        LPOptimizationRequest or = new LPOptimizationRequest();
        or.setC(c);
        or.setG(G);
        or.setH(h);
        or.setLb(lb);
        or.setDumpProblem(true);

        // optimizer
        LPPrimalDualMethod opt = new LPPrimalDualMethod();

        opt.setLPOptimizationRequest(or);
        int returnCode = opt.optimize();
        assertTrue(returnCode != OptimizationResponse.FAILED);
        double[] sol = opt.getOptimizationResponse().getSolution();
        assertEquals(sol[0], 55./2, 0.1);
        assertEquals(sol[1], 0, 0.1);
    }
}
