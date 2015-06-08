package transform.lp;

import java.util.Map;

/**
 * A linear program constraint, that represents A x <= b
 */
public interface Constraint {
    public Map<String, Double> getA();
    public double getB();
}
