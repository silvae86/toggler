package test_utils;

import java.util.HashSet;

// Adapted from https://www.geeksforgeeks.org/java-program-to-print-all-permutations-of-a-given-string/
// Java program to print all permutations of a
// given string.
public class Permutation {

    public static HashSet<String> permute(String str) {
        int n = str.length();
        return Permutation.permute(str, new HashSet<>(), 0, n - 1);
    }

    /**
     * permutation function
     *
     * @param str string to calculate permutation for
     * @param l   starting index
     * @param r   end index
     */
    private static HashSet<String> permute(String str, HashSet<String> allPermutations, int l, int r) {
        if (l == r)
            return allPermutations;
        else {
            for (int i = l; i <= r; i++) {
                allPermutations.add(swap(str, l, i));
                allPermutations.addAll(permute(str, allPermutations, l + 1, r));
            }
            return allPermutations;
        }
    }

    /**
     * Swap Characters at position
     *
     * @param a string defaultValue
     * @param i position 1
     * @param j position 2
     * @return swapped string
     */
    private static String swap(String a, int i, int j) {
        char temp;
        char[] charArray = a.toCharArray();
        temp = charArray[i];
        charArray[i] = charArray[j];
        charArray[j] = temp;
        return String.valueOf(charArray);
    }
}