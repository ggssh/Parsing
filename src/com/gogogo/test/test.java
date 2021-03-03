package com.gogogo.test;

import com.gogogo.parsing.Grammar;
import org.junit.jupiter.api.Test;

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
        grammar.allLeftRecursive();
        //System.out.println(grammar.getProductions());
        System.out.println(grammar.getVN());
        System.out.println(grammar.getVT());
        grammar.getFirst();
        grammar.grammarOutput();
    }
}
