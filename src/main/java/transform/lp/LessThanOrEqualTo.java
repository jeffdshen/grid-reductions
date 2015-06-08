package transform.lp;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class LessThanOrEqualTo implements Constraint {
    public final Map<String, Double> a;
    public final double b;

    public LessThanOrEqualTo(Map<String, Double> lhs, double lhsC, Map<String, Double> rhs, double rhsC) {
        b = rhsC - lhsC;
        ImmutableMap.Builder<String, Double> builder = ImmutableMap.builder();
        for (String key : lhs.keySet()) {
            builder.put(key, lhs.get(key));
        }

        Function<String, Double> lhsDefault = Functions.forMap(lhs, 0.0);

        for (String key : rhs.keySet()) {
            //noinspection ConstantConditions
            builder.put(key, lhsDefault.apply(key) - rhs.get(key));
        }

        a = builder.build();
    }

    @Override
    public Map<String, Double> getA() {
        return a;
    }

    @Override
    public double getB() {
        return b;
    }
}
