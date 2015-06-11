package transform.lp;

public interface LPSolver {
    /**
     * Solves the minimization problem c^T x, G x <= h, Ax = b, x >= 0
     * @param c a vector of coefficients
     * @param G a matrix of coefficients
     * @param h a vector of coefficients
     * @param A a matrix of coefficients
     * @param b a vector of coefficients
     * @return a minimal vector x satisfying the constraints
     */
    public double[] minimize(double[] c, double[][] G, double[] h, double[][] A, double[] b) throws Exception;
}
