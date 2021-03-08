package com.reshuege.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class Masker {

    public static String setMasked(String s) {
        String a = s;
        String b = "945916";
        StringBuilder sb = new StringBuilder();
        a = Integer.toBinaryString(Integer.parseInt(a));
        b = Integer.toBinaryString(Integer.parseInt(b));

        System.out.println(a);
        System.out.println(b);
        LinkedList<String> aar = new LinkedList<>(Arrays.asList(a.split("")));
        LinkedList<String> bar = new LinkedList<>(Arrays.asList(b.split("")));
        ArrayList<Integer> list = new ArrayList<>(Math.max(a.length(), b.length()) + 1);

        //System.out.println(list);
        int k1;
        int k2;
        int temp = -99;
       aar.remove("");
       bar.remove("");

        while (aar.size() > 0 || bar.size() > 0) {
            temp = -99;
            //System.out.println("Size:"+aar.size()+" "+bar.size());
            if (aar.size() == 0 & bar.size() != 0) {
                String w = bar.pollLast();
                //Log.d("firs:", w);
                if (w != null & !w.equals("")) {
                    temp = 0 ^ Integer.parseInt(w);
                }
            } else if (aar.size() != 0 && bar.size() == 0) {
                String w = aar.pollLast();
                //Log.d("sec:", w);
                if (w != null & !w.equals("")) {
                    temp = 0 ^ Integer.parseInt(w);
                }
            } else {
                String w = aar.pollLast();
                String q = bar.pollLast();
                if (w != null && q != null && !w.equals("") && !q.equals("")) {
                    //Log.d("third:",  w + "%" + q);
                    temp = Integer.parseInt(w) ^ Integer.parseInt(q);
                } else
                    temp = -99;
            }
            if (temp != -99)
                sb.append(temp);
            //System.out.print(temp);
        }
        //System.out.println(sb);
        //System.out.println(sb.reverse());
        return String.valueOf(Integer.parseInt(sb.reverse().toString(), 2));
    }
}
