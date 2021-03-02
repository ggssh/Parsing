package com.gogogo.parsing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EliminateLeftRecursive {
    //特殊符号
    public static final char SINGLE_ANGLE_QUOTE = '\'';

    public static final String RIGHT_INFER_QUOTE = " -> ";

    public static final String REGEX = "\\|";

    public static final char EPSILON = '$';

    //非终结符
    public char[] Vn;

    //终结符
    public char[] Vt;

    //产生式
    public Map<String,String> P;

    //开始符号
    public char S;

    public EliminateLeftRecursive(){
        super();
    }

    public EliminateLeftRecursive(char[] Vn,char[] Vt,Map<String,String> P,char S){
        this.Vn = Vn;
        this.Vt = Vt;
        this.P = P;
        this.S = S;
    }

    /**
     * 消除直接左递归，把直接左递归改写为右递归
     *
     * @param left 产生式的左部  一个非终结符A
     * @param right 产生式的右部  形如Aa1 | Aa2 | ... |Aam | b1 | b2 | ... |bn
     *              结果会转换为 A → b1A＇ | b2A＇| ... | bnA＇
     *                          A＇→ a1A＇ | a2A＇| ... | amA＇
     */
    private void eliminateDirectLeftRecursive(String left,String right){
        //用于替换的非终结符(如A＇)
        String repl = left + SINGLE_ANGLE_QUOTE;
        //以"|"为分隔符对产生式右部进行字符串分隔
        String[] strings = right.split(REGEX);
        //用于拼接A的右部
        StringBuilder r1 = new StringBuilder();
        //用户拼接A＇的右部
        StringBuilder r2 = new StringBuilder();
        for(String s : strings){
            if (s.indexOf(left) == 0){//形如Aa1
                r2.append(s.substring(1)).append(repl).append("|");
            }else{//形如b1
                r1.append("|").append(s).append(repl);
            }
        }
        //改为右递归后再加入到产生式P中
        P.put(left,r1.substring(1));
        P.put(repl,r2.append(EPSILON).toString());
    }

    /**
     * 消除文法中一切左递归
     */
    private void eliminateAllLeftRecursive(){
        if (P == null || Vn == null)
            throw new RuntimeException("未初始化相关参数，无法执行！");

        //1、将文法的所有非终结符按字典序升序
        Arrays.sort(Vn);

        //2、循环遍历
        for (int i = 0;i<Vn.length;i++){
            //根据非终结符获取产生式的右部
            String Ai = P.get(Vn[i]+"");
            if (Ai == null)
                throw new RuntimeException("产生式有误");
            StringBuilder sb = new StringBuilder();
            //用于标记当前产生式的右部是否包含左递归
            boolean flag = false;
            for (int j = 0;j<i;j++){
                if(Ai.indexOf(Vn[j]) == 0){
                    //即该产生式包含左递归，可以替换
                    flag = true;
                    String Aj = P.get(Vn[j]+"");
                    if (Aj == null)
                        throw new RuntimeException("产生式有误");
                    //以"|"为分隔符对产生式右部进行字符串分隔
                    String[] Si = Ai.split(REGEX);
                    String[] Sj = Aj.split(REGEX);
                    int k = 0;
                    for(;k<Si.length;k++){
                        if (Si[k].indexOf(Vn[j]) == 0){
                            String ts = Si[k].substring(1);
                            for (String sj : Sj){
                                sb.append(sj).append(ts).append("|");
                            }
                        }else {
                            break;
                        }
                    }
                    for(;k<Si.length;k++) sb.append(Si[k]).append("|");
                    Ai = sb.substring(0,sb.length()-1);
                    sb.setLength(0);
                }

            }
            //消除Ai中的一切直接左递归
            if (flag || Ai.indexOf(Vn[i]) == 0) eliminateDirectLeftRecursive(Vn[i]+"",Ai);
        }

        //3、去掉无用产生式

    }

    /**
     * 格式输入并初始化产生式P与非终结符Vn
     */
    public void inputAndInitialize() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("输入如下文法的产生式：");
        String line = "";
        StringBuilder sb = new StringBuilder();
        P = new HashMap<>();
        while (!"".equals(line = br.readLine())){
            String[] strings = line.trim().split(RIGHT_INFER_QUOTE);
            sb.append(strings[0]);
            if (P.containsKey(strings[0])){
                P.put(strings[0],P.get(strings[0])+"|"+strings[1]);
            }else {
                P.put(strings[0],strings[1]);
            }
        }
        Vn = sb.toString().toCharArray();
    }


    /**
     * 执行消除文法中一切左递归的算法
     */
    public void run(){
        if (P == null || Vn == null) return;
        eliminateAllLeftRecursive();
    }

    /**
     * 输出消除一切左递归后的文法产生式
     */
    public void output(){
        if (P == null) return;
        System.out.println("消除左递归后的文法为：");
        for (Map.Entry<String,String> entry : P.entrySet()){
            System.out.println(entry.getKey()+RIGHT_INFER_QUOTE+entry.getValue());
        }
    }

    /**
     * 测试
     * @param args
     */
    public static void main(String[] args) {
//        用例1
//        S → Qc|c
//        Q → Rb|b
//        R → Sa|a
//        char[] Vn = {'S','Q','R'},Vt = {'a','b','c'};
//        Map<String,String> P = new HashMap<>();
//        P.put("S","Qc|c");P.put("Q","Rb|b");P.put("R","Sa|a");


        /*用例2
        A → aB
        A → Bb
        B → Ac
        B → d
        */
        char[] Vn = {'A','B'},Vt = {'a','b','c','d'};
        Map<String,String> P = new HashMap<>();
        P.put("A","Bb|aB");P.put("B","Ac|d");


//        char[] Vn = {'S'},Vt = {'a','b'};
//        Map<String,String> P = new HashMap<>();
//        P.put("S","Sa|b");

        EliminateLeftRecursive eliminateLeftRecursive = new EliminateLeftRecursive(Vn, Vt, P,'S');
        eliminateLeftRecursive.eliminateAllLeftRecursive();
        Map<String,String> rs = eliminateLeftRecursive.P;
        for (Map.Entry<String,String> entry : rs.entrySet()){
            System.out.println(entry.getKey()+eliminateLeftRecursive.RIGHT_INFER_QUOTE+entry.getValue());
        }
    }
}
