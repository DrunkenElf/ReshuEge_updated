package com.reshuege.utils;

import java.util.ArrayList;
import java.util.TreeMap;

public class Categories {
    public ArrayList<String> names = new ArrayList<>();
    public ArrayList<String> hrefs = new ArrayList<>();
    public ArrayList<TreeMap<String, String>> children = new ArrayList<>();

    public Categories(ArrayList<String> names, ArrayList<String> hrefs, ArrayList<TreeMap<String, String>> children){
        this.names = names;
        this.hrefs = hrefs;
        this.children = children;
    }

}
