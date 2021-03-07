package com.gogogo.test;

import com.gogogo.parsing.Grammar;
import org.junit.jupiter.api.Test;

import java.io.RandomAccessFile;

/**
 * 功能描述：
 *
 * @Author: ggssh
 * @Date: 2021/2/28 0028 15:22
 */
public class test {
    @Test
    public void test(){
        Grammar grammar = new Grammar();
        grammar.setProductions("grammar_input.txt");
        grammar.setVN("grammar_input.txt");
        grammar.setVT();
        System.out.println("开始符号为 : "+ grammar.getVN().get(0));
        grammar.eliminateLeftRecursion();
        //消除左递归
        //提取左公共因子
        grammar.leftFactoring();
        //获得First集
        grammar.getFirst();
        //获得Follow集
        grammar.getFollow();
        //System.out.println(grammar.getProductions());
        System.out.println("VN "+grammar.getVN());
        System.out.println("VT "+grammar.getVT());
        //System.out.println("First "+grammar.getFIRST());
        //System.out.println("Follow "+ grammar.getFOLLOW());
        grammar.grammarOutput();
        System.out.println(grammar.isLL1());
        grammar.preForm();
    }
}
