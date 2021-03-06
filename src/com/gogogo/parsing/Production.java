package com.gogogo.parsing;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ggssh
 * 该类为产生式结构的定义
 */
public class Production {

    String left;//产生式左部
    ArrayList<String> right;//产生式右部

    public Production(String left, ArrayList<String> right) {
        this.left = left;
        this.right = right;
    }

    public String getLeft() {
        return left;
    }

    public List<String> getRight() {
        return right;
    }
}