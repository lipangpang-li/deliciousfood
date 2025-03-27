package org.example.test;

public class ThirdTest {

    public static void main(String[] args) {

        String stringBuilder = new String("begin");
        System.out.println(stringBuilder);
        String test = test(stringBuilder);

        System.out.println(stringBuilder);
        System.out.println(test);
    }

    public static String test(String string) {

        string = "end";
        return string;
    }
}