package com.ceiec.router;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * desc:
 *
 * @author : caokunliang
 * creat_date: 2019/9/5 0005
 * creat_time: 11:06
 **/
public class Test {
    public static void main(String[] args) {
        String s = "http://192.168.19.89:8080/CAD_WebService";
        Pattern pattern = Pattern.compile("http://([0-9.]+):([0-9]{2,5})(/.*)");
        Matcher matcher = pattern.matcher(s);

        matcher.find();

        System.out.println(matcher.group(1));
        System.out.println(matcher.group(2));

    }
}
