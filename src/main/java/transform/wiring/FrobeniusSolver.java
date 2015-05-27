package transform.wiring;

import com.google.common.base.Preconditions;
import com.google.common.math.IntMath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by kevin on 12/5/14.
 * Helper class for solving frobenius equations
 * (a1*x1 + a2*x2 + ... + an*xn = M for ai>0 and non-negative xi
 *
 * TODO - finds the minimum M such that there always exists a solution for given ai
 *
 * These are semi-naive implementations and can definitely be done faster
 */
public class FrobeniusSolver {
    private final int[] values;
    private int frobeniusNumber;
    private HashMap<Integer, int[]> coefficients;

    public FrobeniusSolver(int[] values) {
        Preconditions.checkNotNull(values);
        Preconditions.checkArgument(values.length > 0);
        for(int value: values){
            Preconditions.checkArgument(value > 0);
        }
        Preconditions.checkArgument(values.length != 1 || values[0] == 1,
            "Error: only 1 wire length provided and not of length 1");

        Preconditions.checkArgument(areRelativelyPrime(values),
            "Error: provided wire lengths are not relatively prime : " + Arrays.asList(values));

        this.values = values;
        coefficients = new HashMap<>();
        findFrobeniusNumber();
    }

    private static boolean areRelativelyPrime(int[] numbers){
        int currentGCD = numbers[0];
        for (int number : numbers) {
            currentGCD = IntMath.gcd(currentGCD, number);
            if (currentGCD == 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns an integer >= -1, above which all (nonnegative) numbers have a solution for the Frobenius Equation
     * This number has no solution
     */
    public int getSolvableCutoff(){
        return frobeniusNumber;
    }

    //returns null if failed
    public int[] getCoefficients(int total){
        if(coefficients.containsKey(total)){
            return coefficients.get(total);
        }
        if(total <= frobeniusNumber){
            return null;
        }
        for(int i = 0; i < values.length; i++){
            int[] subanswer = getCoefficients(total - values[i]).clone();
            if(subanswer!= null){
                subanswer[i] = subanswer[i] + 1;
                coefficients.put(total, subanswer);
                return subanswer;
            }
        }
        return null;
    }

    //finds the frobenius number. Also starts the DP
    private void findFrobeniusNumber() {
        int max = 0;
        int min = values[0];
        for(int value: values){
            max = Math.max(max, value);
            min = Math.min(min, value);
        }

        ArrayList<Integer> searchList = new ArrayList<>();
        searchList.add(0);
        coefficients.put(0, new int[values.length]);
        //iteratively generate numbers
        for(int i = 0; i < max; i++){
            ArrayList<Integer> newNums = new ArrayList<>();
            for(int val : searchList){
                for(int j = 0; j < values.length; j++){
                    int value = values[j];
                    if(!coefficients.containsKey(value + val)){
                        int[] coeff = coefficients.get(val).clone();
                        coeff[j] = coeff[j] + 1;
                        coefficients.put(value+val, coeff);
                        newNums.add(value + val);
                    }
                }
            }
            searchList = newNums;
        }
        int found = 0;
        for(int i = 0; i < max*min; i++){
            if (coefficients.containsKey(i)) {
                found++;
                if(found == min){
                    frobeniusNumber = i - min;
                    return;
                }
            } else {
                found = 0;
            }
        }

        throw new IllegalStateException("Derp. failed to find frobenius number");
    }
}
