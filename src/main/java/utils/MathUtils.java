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


    public static int indexMin(List<Integer> values) {
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

    /**
     * Gives the index of the minimum element
     */
    public static int indexMin(int[] values) {
        return indexMin(Ints.asList(values));
    }
}
