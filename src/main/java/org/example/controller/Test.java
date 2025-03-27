package org.example.controller;

public class Test {

    public static void main(String[] args) {
        String num1 = "1234567890129763112312312312312312312312313123131231312313";
        String num2 = "7129371962938769186298638655917823698167986976981236912867";
        String result = addStrings(num1, num2);
        System.out.println(result);
    }

    public static String addStrings(String num1, String num2) {
        String reversedNum1 = reverse(num1);
        String reversedNum2 = reverse(num2);

        int carry = 0;

        StringBuilder result = new StringBuilder();

        int maxLength = Math.max(reversedNum1.length(), reversedNum2.length());
        for (int i = 0; i < maxLength; i++) {
            int digit1 = i < reversedNum1.length() ? reversedNum1.charAt(i) - '0' : 0;
            int digit2 = i < reversedNum2.length() ? reversedNum2.charAt(i) - '0' : 0;

            int sum = digit1 + digit2 + carry;
            int currentDigit = sum % 10;
            carry = sum / 10;

            result.append(currentDigit);
        }

        if (carry > 0) {
            result.append(carry);
        }

        System.gc();
        return reverse(result.toString());
    }

    private static String reverse(String s) {
        char[] chars = s.toCharArray();
        int left = 0, right = chars.length - 1;
        while (left < right) {
            char temp = chars[left];
            chars[left] = chars[right];
            chars[right] = temp;
            left++;
            right--;
        }
        return new String(chars);
    }
}
