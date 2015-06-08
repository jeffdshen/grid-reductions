package transform.lp;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Static methods for constructing various constraints.
 */
public class ConstraintFactory {
    public static Constraint lessThanOrEqualTo(
        Map<String, Double> lhs, double lhsC, Map<String, Double> rhs, double rhsC
    ) {
        return new LessThanOrEqualTo(lhs, lhsC, rhs, rhsC);
    }

    public static Constraint atMost(Map<String, Double> lhs, double lhsC, Map<String, Double> rhs, double rhsC) {
        return lessThanOrEqualTo(lhs, lhsC, rhs, rhsC);
    }

    public static Constraint greaterThanOrEqualTo(
        Map<String, Double> lhs, double lhsC, Map<String, Double> rhs, double rhsC
    ) {
        return lessThanOrEqualTo(rhs, rhsC, lhs, lhsC);
    }

    public static Constraint atLeast(Map<String, Double> lhs, double lhsC, Map<String, Double> rhs, double rhsC) {
        return greaterThanOrEqualTo(lhs, lhsC, rhs, rhsC);
    }

    public static Constraint atLeast(Map<String, Double> lhs, double lhsC, Map<String, Double> rhs) {
        return greaterThanOrEqualTo(lhs, lhsC, rhs, 0.0);
    }

    public static Constraint atLeast(Map<String, Double> lhs, Map<String, Double> rhs, double rhsC) {
        return greaterThanOrEqualTo(lhs, 0.0, rhs, rhsC);
    }

    public static Constraint lessThanOrEqualTo(Map<String, Double> lhs, double rhsC) {
        return lessThanOrEqualTo(lhs, 0.0, ImmutableMap.<String, Double>of(), rhsC);
    }

    public static Constraint atMost(Map<String, Double> lhs, double rhsC) {
        return lessThanOrEqualTo(lhs, rhsC);
    }

    public static Constraint greaterThanOrEqualTo(Map<String, Double> lhs, double rhsC) {
        return greaterThanOrEqualTo(lhs, 0.0, ImmutableMap.<String, Double>of(), rhsC);
    }

    public static Constraint atLeast(Map<String, Double> lhs, double rhsC) {
        return greaterThanOrEqualTo(lhs, rhsC);
    }
}
