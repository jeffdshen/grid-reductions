package transform.lp;

import java.util.Map;

/**
 * A linear program constraint, that represents A x <= b or Ax = b
 */
public interface Constraint {
    boolean isEquality();
    Map<String, Double> getA();
    double getB();
}
