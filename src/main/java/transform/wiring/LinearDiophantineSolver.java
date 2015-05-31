package transform.wiring;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;
import utils.MathUtils;

import java.util.List;

public class LinearDiophantineSolver {
    private final int min;
    private final List<Integer> values;
    private final List<List<Integer>> coefficients;
    private final int minIndex;

    public LinearDiophantineSolver(List<Integer> values) {
        Preconditions.checkArgument(MathUtils.relativelyPrime(values),
            "Error: provided wire lengths are not relatively prime : " + values);

        this.values = values;
        this.minIndex = minIndex(values);
        this.min = values.get(minIndex);
        this.coefficients = solve(values);
    }

    public List<Integer> getCoefficients(int sum) {
        if (sum >= min || sum < 0) {
            int index = sum % min;
            int minCoeff = 0;
            if (sum % min < 0) {
                index += min;
                minCoeff -= 1;
            }
            List<Integer> c = coefficients.get(index);
            List<Integer> list = Lists.newArrayList(c);
            list.set(minIndex, c.get(minIndex) + sum / min + minCoeff);
            return ImmutableList.copyOf(list);
        }

        return coefficients.get(sum);
    }

    private static List<List<Integer>> solve(List<Integer> values) {
        if (values.isEmpty()) {
            return ImmutableList.of();
        }

        int index = minIndex(values);
        int min = values.get(index);

        @SuppressWarnings("unchecked")
        Iterable<Integer>[] coefficients = new Iterable[min];
        coefficients[0] = ImmutableList.of();

        // j times, basically like breadth first search
        for (int j = 0; j < min; j++) {
            // apply every ith element of value
            for (int i = 0; i < values.size(); i++) {
                if (i == index) {
                    continue;
                }
                int value = values.get(i);

                // update all coefficients
                for (int k = 0; k < coefficients.length; k++) {
                    if (coefficients[k] != null && coefficients[(k + value) % min] == null) {
                        coefficients[(k + value) % min] = Iterables.concat(coefficients[k], ImmutableList.of(i));
                    }
                }
            }
        }

        ImmutableList.Builder<List<Integer>> builder = ImmutableList.builder();
        for (int k = 0; k < coefficients.length; k++) {
            int sum = 0;
            int[] counts = new int[values.size()];
            for (int i : coefficients[k]) {
                int value = values.get(i);
                counts[i]++;
                sum += value;
            }

            counts[index] = (k - sum) / min;
            List<Integer> x = ImmutableList.copyOf(Ints.asList(counts));
            builder.add(x);
        }
        return builder.build();
    }

    private static int minIndex(List<Integer> values) {
        int min = values.get(0);
        int minIndex = 0;

        int index = 0;
        for (Integer value : values) {
            if (value < min) {
                min = value;
                minIndex = index;
            }
            index++;
        }
        return minIndex;
    }

    public List<Integer> getValues() {
        return values;
    }
}
