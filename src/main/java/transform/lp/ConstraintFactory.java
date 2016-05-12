package transform.lp;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Static methods for constructing various constraints.
 */
public class ConstraintFactory {
    public static Constraint lessThanOrEqualTo(
        Map<String, ? extends Number> lhs, Number lhsC, Map<String, ? extends Number> rhs, Number rhsC
    ) {
        return new LessThanOrEqualTo(lhs, lhsC.doubleValue(), rhs, rhsC.doubleValue());
    }

    public static Constraint lessThanOrEqualTo(Map<String, ? extends Number> lhs, Number rhsC) {
        return lessThanOrEqualTo(lhs, 0.0, ImmutableMap.<String, Double>of(), rhsC);
    }

    public static Constraint atMost(
        Map<String, ? extends Number> lhs, Number lhsC, Map<String, ? extends Number> rhs, Number rhsC
    ) {
        return lessThanOrEqualTo(lhs, lhsC, rhs, rhsC);
    }

    public static Constraint atMost(Map<String, ? extends Number> lhs, Number rhsC) {
        return lessThanOrEqualTo(lhs, rhsC);
    }

    public static Constraint atMost(
        Map<String, ? extends Number> lhs, Number lhsC, Map<String, ? extends Number> rhs
    ) {
        return lessThanOrEqualTo(lhs, lhsC, rhs, 0.0);
    }


    public static Constraint atMost(Map<String, ? extends Number> lhs, Map<String, ? extends Number> rhs) {
        return lessThanOrEqualTo(lhs, 0.0, rhs, 0.0);
    }

    /**
     * For constraints with at most a single variable on each side
     */
    public static Constraint atMost(String lhs, Number lhsC, String rhs, Number rhsC) {
        return atMost(ImmutableMap.of(lhs, 1), lhsC, ImmutableMap.of(rhs, 1), rhsC);
    }

    public static Constraint atMost(String lhs, Number rhsC) {
        return atMost(ImmutableMap.of(lhs, 1), rhsC);
    }

    public static Constraint atMost(String lhs, Number lhsC, String rhs) {
        return atMost(ImmutableMap.of(lhs, 1), lhsC, ImmutableMap.of(rhs, 1));
    }

    public static Constraint atMost(String lhs, String rhs) {
        return atMost(ImmutableMap.of(lhs, 1), ImmutableMap.of(rhs, 1));
    }

    public static Constraint greaterThanOrEqualTo(
        Map<String, ? extends Number> lhs, Number lhsC, Map<String, ? extends Number> rhs, Number rhsC
    ) {
        return lessThanOrEqualTo(rhs, rhsC, lhs, lhsC);
    }

    public static Constraint greaterThanOrEqualTo(Map<String, ? extends Number> lhs, Number rhsC) {
        return greaterThanOrEqualTo(lhs, 0.0, ImmutableMap.<String, Double>of(), rhsC);
    }

    public static Constraint atLeast(
        Map<String, ? extends Number> lhs, Number lhsC, Map<String, ? extends Number> rhs, Number rhsC
    ) {
        return greaterThanOrEqualTo(lhs, lhsC, rhs, rhsC);
    }

    public static Constraint atLeast(
        Map<String, ? extends Number> lhs, Number lhsC, Map<String, ? extends Number> rhs
    ) {
        return greaterThanOrEqualTo(lhs, lhsC, rhs, 0.0);
    }

    public static Constraint atLeast(
        Map<String, ? extends Number> lhs, Map<String, ? extends Number> rhs
    ) {
        return greaterThanOrEqualTo(lhs, 0.0, rhs, 0.0);
    }

    public static Constraint atLeast(
        Map<String, ? extends Number> lhs, Map<String, ? extends Number> rhs, Number rhsC
    ) {
        return greaterThanOrEqualTo(lhs, 0.0, rhs, rhsC);
    }

    public static Constraint atLeast(Map<String, ? extends Number> lhs, Number rhsC) {
        return greaterThanOrEqualTo(lhs, rhsC);
    }

    /**
     * For constraints with at most a single variable on each side
     */
    public static Constraint atLeast(String lhs, Number lhsC, String rhs, Number rhsC) {
        return atLeast(ImmutableMap.of(lhs, 1), lhsC, ImmutableMap.of(rhs, 1), rhsC);
    }

    public static Constraint atLeast(String lhs, Number rhsC) {
        return atLeast(ImmutableMap.of(lhs, 1), rhsC);
    }

    public static Constraint atLeast(String lhs, Number lhsC, String rhs) {
        return atLeast(ImmutableMap.of(lhs, 1), lhsC, ImmutableMap.of(rhs, 1));
    }

    public static Constraint atLeast(String lhs, String rhs) {
        return atLeast(ImmutableMap.of(lhs, 1), ImmutableMap.of(rhs, 1));
    }

    public static Constraint atLeast(String lhs, String rhs, Number rhsC) {
        return atLeast(ImmutableMap.of(lhs, 1), ImmutableMap.of(rhs, 1), rhsC);
    }

    /**
     * WARNING: Gives a constraint where lhs + 1 <= rhs
     */
    public static Constraint lessThan(Map<String, Integer> lhs, int lhsC, Map<String, Integer> rhs, int rhsC) {
        return lessThanOrEqualTo(lhs, lhsC + 1, rhs, rhsC);
    }

    public static Constraint lessThan(Map<String, Integer> lhs, Map<String, Integer> rhs, int rhsC) {
        return lessThanOrEqualTo(lhs, 1, rhs, rhsC);
    }

    public static Constraint lessThan(Map<String, Integer> lhs, int lhsC, Map<String, Integer> rhs) {
        return lessThanOrEqualTo(lhs, lhsC + 1, rhs, 0);
    }

    public static Constraint lessThan(Map<String, Integer> lhs, Map<String, Integer> rhs) {
        return lessThanOrEqualTo(lhs, 1, rhs, 0);
    }

    /**
     * WARNING: Gives a constraint where lhs >= rhs + 1
     */
    public static Constraint greaterThan(Map<String, Integer> lhs, int lhsC, Map<String, Integer> rhs, int rhsC) {
        return greaterThanOrEqualTo(lhs, lhsC, rhs, rhsC + 1);
    }

    public static Constraint greaterThan(Map<String, Integer> lhs, Map<String, Integer> rhs, int rhsC) {
        return greaterThanOrEqualTo(lhs, 0, rhs, rhsC + 1);
    }

    public static Constraint greaterThan(Map<String, Integer> lhs, int lhsC, Map<String, Integer> rhs) {
        return greaterThanOrEqualTo(lhs, lhsC, rhs, 1);
    }

    public static Constraint greaterThan(Map<String, Integer> lhs, Map<String, Integer> rhs) {
        return greaterThanOrEqualTo(lhs, 0, rhs, 1);
    }

    public static Constraint equalTo(
        Map<String, ? extends Number> lhs, Number lhsC, Map<String, ? extends Number> rhs, Number rhsC
    ) {
        return new EqualTo(lhs, lhsC.doubleValue(), rhs, rhsC.doubleValue());
    }

    public static Constraint equalTo(
        Map<String, ? extends Number> lhs, Map<String, ? extends Number> rhs, Number rhsC
    ) {
        return equalTo(lhs, 0., rhs, rhsC.doubleValue());
    }

    public static Constraint equalTo(
        Map<String, ? extends Number> lhs, Number lhsC, Map<String, ? extends Number> rhs
    ) {
        return equalTo(lhs, lhsC.doubleValue(), rhs, 0.);
    }

    public static Constraint equalTo(
        Map<String, ? extends Number> lhs, Map<String, ? extends Number> rhs
    ) {
        return equalTo(lhs, 0., rhs, 0.);
    }

    public static Constraint equalTo(Map<String, ? extends Number> lhs, Number rhsC) {
        return equalTo(lhs, 0., ImmutableMap.<String, Number>of(), rhsC);
    }

    /**
     * For constraints with at most a single variable on each side
     */
    public static Constraint equalTo(String lhs, Number lhsC, String rhs, Number rhsC) {
        return equalTo(ImmutableMap.of(lhs, 1), lhsC, ImmutableMap.of(rhs, 1), rhsC);
    }

    public static Constraint equalTo(String lhs, Number rhsC) {
        return equalTo(ImmutableMap.of(lhs, 1), rhsC);
    }

    public static Constraint equalTo(String lhs, Number lhsC, String rhs) {
        return equalTo(ImmutableMap.of(lhs, 1), lhsC, ImmutableMap.of(rhs, 1));
    }

    public static Constraint equalTo(String lhs, String rhs) {
        return equalTo(ImmutableMap.of(lhs, 1), ImmutableMap.of(rhs, 1));
    }

    public static Constraint equalTo(String lhs, String rhs, Number rhsC) {
        return equalTo(ImmutableMap.of(lhs, 1), ImmutableMap.of(rhs, 1), rhsC);
    }
}
