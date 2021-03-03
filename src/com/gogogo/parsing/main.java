package com.gogogo.parsing;

/**
 * 功能描述：
 *
 * @Author: ggssh
 * @Date: 2021/2/24 0024 20:34
 */
public class main {
    public static void main(String[] args) {
        Grammar grammar = new Grammar();
        grammar.setProductions("grammar_input.txt");
        grammar.setVN("grammar_input.txt");
        grammar.setVT();
        grammar.leftFactoring();
    }
}
