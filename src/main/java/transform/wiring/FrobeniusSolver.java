package transform.wiring;

import com.google.common.base.Preconditions;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import utils.MathUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kevin on 12/5/14.
 * Helper class for solving frobenius equations
 * (a1*x1 + a2*x2 + ... + an*xn = M for ai>0 and non-negative xi
 *
 * These are semi-naive implementations and can definitely be done faster
 */
public class FrobeniusSolver {
    private final List<Integer> values;
    private int frobeniusNumber;
    private HashMap<Integer, int[]> coefficients;
    private final int min;
    private final int indexMin;

    /**
     * Precondition: values must be unmodified while the solver available
     */
    public FrobeniusSolver(List<Integer> values) {
        Preconditions.checkNotNull(values);
        Preconditions.checkArgument(values.size() > 0);
        for(int value : values){
            Preconditions.checkArgument(value > 0);
        }

        Preconditions.checkArgument(MathUtils.relativelyPrime(values),
            "Error: provided wire lengths are not relatively prime : " + values);

        this.values = values;
        this.indexMin = MathUtils.indexMin(values);
        this.min = values.get(indexMin);
        coefficients = new HashMap<>();
        findFrobeniusNumber();

    }

    /**
     * Precondition: values must be unmodified while the solver available
     */
    public FrobeniusSolver(int[] values) {
        this(Ints.asList(values));
    }

    public int getWireLength(int index) {
        return values.get(index);
    }

    /**
     * Returns an integer x >= 0, such that the equation with x has a solution.
     */
    public int getSolvableCutoff(){
        return frobeniusNumber + 1;
    }

    /**
     * Returns the largest unsolvable integer x (the frobenius number)
     */
    public int getLargestUnsolvable() {
        return frobeniusNumber;
    }

    /**
     * Find the coefficients to make the sum equal a given total
     * @param total the number for the linear combination to sum to
     * @return the coefficients of the ai in the equation. null if no solution exists
     */
    public int[] getCoefficients(int total){
        if(coefficients.containsKey(total)){
            return coefficients.get(total);
        }

        if(total <= frobeniusNumber){
            return null;
        }

        // subtract min until reaching a known solution
        int diff =  (total - (frobeniusNumber + 1)) / min;
        int known = frobeniusNumber + 1 + (total - (frobeniusNumber + 1)) % min;
        int[] answer = coefficients.get(known).clone();
        answer[indexMin] += diff;
        return answer;
    }

    //finds the frobenius number. Also starts the DP
    private void findFrobeniusNumber() {
        int max = Ordering.natural().max(values);
        int min = Ordering.natural().min(values);

        ArrayList<Integer> searchList = new ArrayList<>();
        searchList.add(0);
        coefficients.put(0, new int[values.size()]);
        //iteratively generate numbers
        for(int i = 0; i < max; i++){
            ArrayList<Integer> newNums = new ArrayList<>();
            for(int val : searchList){
                for(int j = 0; j < values.size(); j++){
                    int value = values.get(j);
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

        // check for min consecutive solutions
        int found = 0;
        for(int i = 0; i < max*min; i++){
            if (coefficients.containsKey(i)) {
                found++;
                if (found == min){
                    // frobenius number guaranteed to have min solutions afterwards that all work
                    frobeniusNumber = i - min;
                    return;
                }
            } else {
                found = 0;
            }
        }

        throw new IllegalStateException("Unexpected programming error: did not find the frobenius number");
    }
}
