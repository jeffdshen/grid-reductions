package transform.lp;

import com.joptimizer.optimizers.LPOptimizationRequest;
import com.joptimizer.optimizers.LPPrimalDualMethod;
import com.joptimizer.optimizers.OptimizationResponse;

public class JOptimizerSolver implements LPSolver {
    @Override
    public double[] minimize(double[] c, double[][] G, double[] h, double[][] A, double[] b) throws Exception {
        int n = c.length;

        // x >= 0
        double[] lb = new double[n];

        // problem
        LPOptimizationRequest or = new LPOptimizationRequest();
        or.setC(c);
        or.setG(G);
        or.setH(h);
        or.setA(A);
        or.setB(b);
        or.setLb(lb);
        or.setDumpProblem(true);

        // optimizer
        LPPrimalDualMethod opt = new LPPrimalDualMethod();

        opt.setLPOptimizationRequest(or);
        int returnCode = opt.optimize();
        if (returnCode == OptimizationResponse.FAILED) {
            throw new Exception("Failed optimization response");
        }
        return opt.getOptimizationResponse().getSolution();
    }
}
