package transform.lp;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;

import java.util.*;

/**
 * A linear program with variables names
 */
public class LinearProgram {
    private final BiMap<String, Integer> vars;
    private final Map<String, Double> objective;
    private final List<Constraint> constraints;
    private final double[] c;
    private final double[] b;
    private final double[][] a;

    private LinearProgram(BiMap<String, Integer> vars, Map<String, Double> objective, List<Constraint> constraints) {
        this.vars = vars;
        this.objective = objective;
        this.constraints = constraints;
        this.c = new double[vars.size()];
        this.b = new double[constraints.size()];
        this.a = new double[constraints.size()][];

        for (String key : objective.keySet()) {
            c[vars.get(key)] = objective.get(key);
        }

        for (int i = 0; i < constraints.size(); i++) {
            Constraint c = constraints.get(i);
            b[i] = c.getB();
            a[i] = new double[vars.size()];

            Map<String, Double> A = c.getA();
            for (String key : A.keySet()) {
                a[i][vars.get(key)] = A.get(key);
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public double[] getC() {
        return c;
    }

    public double[][] getA() {
        return a;
    }

    public double[] getB() {
        return b;
    }

    public Map<String, Double> getSolution(double[] solution) {
        BiMap<Integer, String> inverse = vars.inverse();

        ImmutableMap.Builder<String, Double> builder = ImmutableMap.builder();
        for (int i = 0; i < solution.length; i++) {
            builder.put(inverse.get(i), solution[i]);
        }

        return builder.build();
    }

    public Map<String, Double> getSolution(LPSolver solver) throws Exception {
        return getSolution(solver.minimize(c, a, b));
    }

    public static class Builder {
        private final ArrayList<Constraint> constraints;
        private Map<String, Double> objective;

        private Builder() {
            constraints = new ArrayList<>();
        }

        public void setObjective(Map<String, Double> objective) {
            Preconditions.checkState(this.objective == null);
            this.objective = objective;
        }

        public void addConstraint(Constraint c) {
            constraints.add(c);
        }

        public LinearProgram build() {
            Set<String> vars = new HashSet<>();
            for (String key : objective.keySet()) {
                vars.add(key);
            }

            for (Constraint c : constraints) {
                for (String key : c.getA().keySet()) {
                    vars.add(key);
                }
            }

            ImmutableBiMap.Builder<String, Integer> index = ImmutableBiMap.builder();

            int i = 0;
            for (String s : vars) {
                index.put(s, i);
                i++;
            }

            return new LinearProgram(index.build(), ImmutableMap.copyOf(objective), ImmutableList.copyOf(constraints));
        }
    }
}
