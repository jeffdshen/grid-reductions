package transform.wiring;

import com.google.common.collect.ImmutableList;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;

public class LinearDiophantineSolverTest {
    @Test
    public void test1() throws Exception {
        test(ImmutableList.of(1));
    }

    @Test
    public void test2() throws Exception {
        test(ImmutableList.of(3, 5));
        test(ImmutableList.of(2, 3));
        test(ImmutableList.of(7, 20));
    }

    @Test
    public void test3() throws Exception {
        test(ImmutableList.of(3, 6, 10));
    }

    public void test(List<Integer> list) {
        LinearDiophantineSolver solver = new LinearDiophantineSolver(list);
        for (int i = -5; i <= 5; i++) {
            List<Integer> coefficients = solver.getCoefficients(i);
            int sum = 0;
            for (int j = 0; j < coefficients.size(); j++) {
                sum += list.get(j) * coefficients.get(j);
            }

            assertEquals(sum, i);
        }
    }
}