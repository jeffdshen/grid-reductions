package transform.lp;

import java.util.Map;

/**
 * A linear program constraint, that represents A x <= b or Ax = b
 */
public interface Constraint {
    public boolean isEquality();
    public Map<String, Double> getA();
    public double getB();
}
