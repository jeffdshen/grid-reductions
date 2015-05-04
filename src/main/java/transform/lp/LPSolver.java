package transform.lp;

public interface LPSolver {
    /**
     * Solves the minimization problem c^T x, A y <= b, x >= 0
     * @param c a vector of coefficients
     * @param A a matrix of coefficients
     * @param b a vector of coefficients
     * @return a minimal vector x satisfying the constraints
     */
    public double[] minimize(double[] c, double[][] A, double[] b) throws Exception;
}
