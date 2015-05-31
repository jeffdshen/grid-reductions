package utils;

import com.google.common.math.IntMath;
import com.google.common.primitives.Ints;

import java.util.List;

public class MathUtils {
    public static boolean relativelyPrime(int[] nums) {
        return relativelyPrime(Ints.asList(nums));
    }

    public static boolean relativelyPrime(List<Integer> nums) {
        int currentGCD = nums.get(0);
        for (int number : nums) {
            currentGCD = IntMath.gcd(currentGCD, number);
            if (currentGCD == 1) {
                return true;
            }
        }
        return false;
    }
}
