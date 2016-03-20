package transform.lp;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.Map;

public class LessThanOrEqualTo implements Constraint {
    public final Map<String, Double> a;
    public final double b;

    public LessThanOrEqualTo(
        Map<String, ? extends Number> lhs, double lhsC, Map<String, ? extends Number> rhs, double rhsC
    ) {
        b = rhsC - lhsC;
        ImmutableMap.Builder<String, Double> builder = ImmutableMap.builder();
        for (String key : lhs.keySet()) {
            builder.put(key, lhs.get(key).doubleValue());
        }

        Function<String, Number> lhsDefault = Functions.forMap(lhs, 0.0);

        for (String key : rhs.keySet()) {
            //noinspection ConstantConditions
            builder.put(key, lhsDefault.apply(key).doubleValue() - rhs.get(key).doubleValue());
        }

        a = builder.build();
    }

    @Override
    public boolean isEquality() {
        return false;
    }

    @Override
    public Map<String, Double> getA() {
        return a;
    }

    @Override
    public double getB() {
        return b;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Joiner.on(" + ").appendTo(builder, Iterables.transform(a.keySet(), new Function<String, Object>() {
            @Nullable
            @Override
            public Object apply(@Nullable String s) {
                return a.get(s) + " * " + s;
            }
        }));
        builder.append(" <= ").append(b);
        return builder.toString();
    }
}
