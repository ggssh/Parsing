package com.gogogo.parsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 * 功能描述：
 *
 * @Author: ggssh
 * @Date: 2021/2/28 0028 11:47
 */
public class Grammar {
    ArrayList<String> VT;//终结符
    ArrayList<String> VN;//非终结符
    String S;//开始符号
    HashMap<String,ArrayList<String>> FIRST;//First集
    HashMap<String,ArrayList<String>> FOLLOW;//Follow集
    ArrayList<Production> productions;//产生式集
}
