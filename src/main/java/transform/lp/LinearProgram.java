package transform.lp;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;

import java.util.*;

/**
 * A linear program with variables names
 */
public class LinearProgram {
    private final BiMap<String, Integer> vars;
    private final Map<String, Double> objective;
    private final List<Constraint> equal;
    private final List<Constraint> unequal;
    private final double[] c;
    private final double[] b;
    private final double[][] a;
    private final double[] h;
    private final double[][] g;

    private LinearProgram(
        BiMap<String, Integer> vars, Map<String, Double> objective, List<Constraint> equal, List<Constraint> unequal
    ) {
        this.vars = vars;
        this.objective = objective;
        this.equal = equal;
        this.unequal = unequal;
        this.c = new double[vars.size()];
        this.b = new double[equal.size()];
        this.a = new double[equal.size()][];
        this.h = new double[unequal.size()];
        this.g = new double[unequal.size()][];

        // make objective
        for (String key : objective.keySet()) {
            c[vars.get(key)] = objective.get(key);
        }

        // make equalities
        for (int i = 0; i < equal.size(); i++) {
            Constraint c = equal.get(i);
            b[i] = c.getB();
            a[i] = new double[vars.size()];

            Map<String, Double> A = c.getA();
            for (String key : A.keySet()) {
                a[i][vars.get(key)] = A.get(key);
            }
        }

        // make inequalities
        for (int i = 0; i < unequal.size(); i++) {
            Constraint c = unequal.get(i);
            h[i] = c.getB();
            g[i] = new double[vars.size()];

            Map<String, Double> A = c.getA();
            for (String key : A.keySet()) {
                g[i][vars.get(key)] = A.get(key);
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

    public double[][] getG() {
        return g;
    }

    public double[] getH() {
        return h;
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
        return getSolution(solver.minimize(c, g, h, a, b));
    }

    public static class Builder {
        private final ArrayList<Constraint> equality;
        private final ArrayList<Constraint> inequality;
        private Map<String, ? extends Number> objective;
        private final Set<String> vars;

        private Builder() {
            equality = new ArrayList<>();
            inequality = new ArrayList<>();
            vars = new HashSet<>();
        }

        public void setObjective(Map<String, ? extends Number> objective) {
            Preconditions.checkState(this.objective == null);
            this.objective = objective;
        }

        public void addConstraint(Constraint c) {
            if (c.isEquality()) {
                equality.add(c);
            } else {
                inequality.add(c);
            }

            for (String key : c.getA().keySet()) {
                vars.add(key);
            }
        }

        public LinearProgram build() {
            for (String key : objective.keySet()) {
                vars.add(key);
            }

            ImmutableBiMap.Builder<String, Integer> index = ImmutableBiMap.builder();

            int i = 0;
            for (String s : vars) {
                index.put(s, i);
                i++;
            }

            Map<String, Double> obj = Maps.transformValues(ImmutableMap.copyOf(objective),
                new Function<Number, Double>() {
                    @Override
                    public Double apply(Number input) {
                        return input.doubleValue();
                    }
                }
            );

            return new LinearProgram(
                index.build(), obj, ImmutableList.copyOf(equality), ImmutableList.copyOf(inequality)
            );
        }
    }
}
