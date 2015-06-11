package transform.lp;

import com.google.common.collect.ImmutableMap;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;
import static transform.lp.ConstraintFactory.atLeast;
import static transform.lp.ConstraintFactory.atMost;
import static com.google.common.collect.ImmutableMap.of;
import static transform.lp.ConstraintFactory.equalTo;

public class LinearProgramTest {
    @Test
    public void testCorrectness() throws Exception {
        // Minimize 20x + 67y, subject to -2x - y <= -55, 29 + 3y >= x, x>=0, y>=0
        // minimize c * x
        LinearProgram.Builder builder = LinearProgram.builder();
        builder.setObjective(ImmutableMap.of("x", 20, "y", 67));
        builder.addConstraint(atMost(of("x", -2, "y", -1), -55));
        builder.addConstraint(atLeast(of("y", -3), 29, of("x", 1)));
        builder.addConstraint(equalTo(of("z", 1), 5, of("x", 3, "y", 5)));
        LinearProgram lp = builder.build();

        LPSolver solver = new JOptimizerSolver();
        Map<String, Double> solution = lp.getSolution(solver);
        assertEquals(solution.get("x"), 55./2, 0.1);
        assertEquals(solution.get("y"), 0, 0.1);
        assertEquals(solution.get("z"), -5 + 55./2 * 3 + 0, 0.1);
    }
}
