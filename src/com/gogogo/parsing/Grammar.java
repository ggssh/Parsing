package com.gogogo.parsing;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 功能描述：
 *
 * @Author: ggssh
 * @Date: 2021/2/28 0028 11:47
 */
public class Grammar {
    ArrayList<String> VT = new ArrayList<>();//终结符
    ArrayList<String> VN = new ArrayList<>();//非终结符
    String S;//开始符号
    HashMap<String, ArrayList<String>> FIRST = new HashMap<>();//First集
    HashMap<String, ArrayList<String>> FOLLOW = new HashMap<>();//Follow集
    HashMap<String, ArrayList<String>> productions;//产生式集
}
