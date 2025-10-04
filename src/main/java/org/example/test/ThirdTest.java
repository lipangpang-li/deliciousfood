package org.example.test;

import lombok.Data;

public class ThirdTest {

    public static void main(String[] args) {
        Usr usr = new Usr();
        usr.setName("start");

        Usr a = usr;
        a.setName("end");

        System.out.println(usr);

    }

    public static String test(String string) {

        string = "end";
        return string;
    }
}

@Data
class Usr{
    private String name;
}