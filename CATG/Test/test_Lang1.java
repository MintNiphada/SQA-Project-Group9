package tests;

import catg.CATG;
import org.apache.commons.lang3.math.NumberUtils;

public class test_Lang1 {

    public static void main(String[] args) {

        String input = CATG.readString("0x80000000");

        try {
            Number result = NumberUtils.createNumber(input);
            System.out.println("Input  : " + input);
            System.out.println("Result : " + result);
            System.out.println("Type   : " + result.getClass().getSimpleName());
        } catch (NumberFormatException e) {
            System.out.println("Input  : " + input);
            System.out.println("ERROR  : NumberFormatException");
        }
    }
}
