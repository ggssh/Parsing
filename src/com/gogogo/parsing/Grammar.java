package com.gogogo.parsing;

import com.sun.istack.internal.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

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
    HashMap<String, HashSet<String>> FIRST;//First集
    HashMap<String, HashSet<String>> FOLLOW;//Follow集
    HashMap<String, ArrayList<String>> productions;//产生式集
    public static final char SINGLE_ANGLE_QUOTE = '\'';//用来替换非终结符
    public static final char EPSILON = '$';//用来代替ε

    public Grammar() {
        VT = new ArrayList<>();
        VN = new ArrayList<>();
        S = "";
        FIRST = new HashMap<>();
        FOLLOW = new HashMap<>();
        productions = new HashMap<>();
    }

    //读取产生式
    public void setProductions(String filePath) {
        try {
            File file = new File(filePath);
            RandomAccessFile randomFile = new RandomAccessFile(file, "r");
            String line;
            while ((line = randomFile.readLine()) != null) {
                String left = line.split("->")[0].trim();
                String tempRight = line.split("->")[1].trim();
                List<String> temp = Arrays.asList(tempRight.split("\\|"));
                ArrayList<String> right = new ArrayList<>(temp);
                if(productions.get(left)==null){
                    productions.put(left, right);
                }else{
                    productions.get(left).addAll(right);
                }
            }
            randomFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, ArrayList<String>> getProductions() {
        return productions;
    }

    //读取非终结符号集
    public void setVN(String filepath) {
        try {
            //读取文件，然后一行一行地进行读取
            File file = new File(filepath);
            RandomAccessFile randomfile = new RandomAccessFile(file, "r");

            String line;
            String left;
            while ((line = randomfile.readLine()) != null) {
                left = line.split("->")[0].trim();
                //如果非终结符号集不包含，就加入
                if (!this.VN.contains(left)) {
                    this.VN.add(left);
                }
            }
            //关闭文件读写
            randomfile.close();
        } catch (Exception var5) {
            var5.printStackTrace();
        }
    }

    public ArrayList<String> getVN() {
        return VN;
    }

    //读取终结符
    public void setVT() {
        ArrayList<String> rights = new ArrayList<>();
        for (int i = 0; i < VN.size(); i++) {
            String left = VN.get(i);
            //rights代表的是非终结符号右边的所有产生式
            rights = productions.get(left);
            //取出每一条产生式
            for (String right : rights) {
                //判断产生式的每一个符号,这边利用空格分割
                String[] production = right.split(" ");
                for (int j = 0; j < production.length; j++) {
                    //如果非终结符号不可以找到并且也不是”$“，就是终结符号
                    if (!VN.contains(production[j]) && !production[j].equals("$") && !VT.contains(production[j])) {
                        VT.add(production[j]);
                    }
                }
            }
        }
    }

    public ArrayList<String> getVT() {
        return VT;
    }

    //消除左递归
    public void allLeftRecursive() {
        if (productions == null || VN == null) {
            throw new RuntimeException("ERROR");
        }
        String res = "";
        //新建一个二维数组
        HashMap<String, ArrayList<String>> tempProduction = new HashMap<>(productions);
        HashMap<String, ArrayList<ArrayList<String>>> temp = new HashMap<>();
        for (int count = 0; count < tempProduction.size(); count++) {
            ArrayList<ArrayList<String>> temp2ArrayList = new ArrayList<>();
            for (int l = 0; l < tempProduction.get(VN.get(count)).size(); l++) {
                List<String> tempList =
                        Arrays.asList(tempProduction.get(VN.get(count)).get(l).split(" "));
                ArrayList<String> tempArrayList = new ArrayList<>(tempList);
                temp2ArrayList.add(tempArrayList);
            }
            temp.put(VN.get(count), temp2ArrayList);
        }
        for (int i = 0; i < VN.size(); i++) {
            //根据非终结符获取产生式的右部
            ArrayList<ArrayList<String>> Ai = temp.get(VN.get(i));
            //ArrayList<String> Ai = productions.get(VN.get(i));
            if (Ai == null) {
                throw new RuntimeException("ERROR");
            }
            StringBuilder builder = new StringBuilder();
            //用于标记当前产生式的右部是否包含左递归
            boolean flag = false;
            for (int j = 0; j < i; j++) {
                if (Ai.get(0).indexOf(VN.get(j)) == 0) {
                    //说明该产生式包含左递归，可以替换
                    flag = true;
                    ArrayList<ArrayList<String>> Aj = temp.get(VN.get(j));
                    if (Aj == null) {
                        throw new RuntimeException("ERROR");
                    }
                    int k = 0;
                    for (; k < Ai.size(); k++) {
                        if (Ai.get(k).indexOf(VN.get(j)) == 0) {
                            StringBuilder ts = new StringBuilder();
                            for (int m = 1; m < Ai.get(k).size(); m++) {
                                //每次append之后都要加入一个空格
                                ts.append(Ai.get(k).get(m)).append(" ");
                            }
                            for (ArrayList<String> aj : Aj) {
                                StringBuilder tempAj = new StringBuilder();
                                for (String s : aj) {
                                    tempAj.append(s).append(" ");
                                }
                                builder.append(tempAj).append(ts).append("|");
                            }
                        } else {
                            break;
                        }
                    }
                    for (; k < Ai.size(); k++) {
                        StringBuilder tempAi = new StringBuilder();
                        for (String s : Ai.get(k)) {
                            tempAi.append(s).append(" ");
                        }
                        builder.append(tempAi).append("|");
                    }
                    ArrayList<ArrayList<String>> resDoubleArrayList = new ArrayList<>();
                    List<String> resList = Arrays.asList(builder.toString().split("\\|"));
                    ArrayList<String> resArrayList = new ArrayList<>(resList);
                    for (String tempS : resArrayList) {
                        List<String> list = Arrays.asList(tempS.split(" "));
                        resDoubleArrayList.add(new ArrayList<>(list));
                    }
                    Ai = resDoubleArrayList;
                    builder.setLength(0);
                }
            }
            //消除Ai中的一切直接左递归
            if (flag || Ai.get(0).indexOf(VN.get(i)) == 0) {
                directLeftRecursive(VN.get(i), Ai);
            }
        }
        //重新设置非终结符集
        VN = new ArrayList<String>(productions.keySet());
    }

    //消除直接左递归
    private void directLeftRecursive(String left,@NotNull ArrayList<ArrayList<String>> right) {
        //用于替换的非终结符(如A')
        String repl = left + SINGLE_ANGLE_QUOTE;
        //用于拼接A的右部
        StringBuilder r1 = new StringBuilder();
        //用于拼接A'的右部
        StringBuilder r2 = new StringBuilder();
        for (ArrayList<String> stringArrayList : right) {
            String s = "";
            for (String str : stringArrayList) {
                s += str;
                s+=" ";
            }
            if (stringArrayList.indexOf(left) == 0) {

                r2.append(s.substring(s.indexOf(left)+left.length())).append(repl).append("|");
            } else {
                r1.append("|").append(s).append(repl);
            }
        }
        //改为右递归后再加入到产生式P中
        productions.put(left, str2Array(r1.substring(1)));
        productions.put(repl, str2Array(r2.append(EPSILON).toString()));
    }

    //将产生式根据 | 进行分割
    private ArrayList<String> str2Array(String str) {
        List<String> strings = Arrays.asList(str.split("\\|"));
        ArrayList<String> stringArrayList = new ArrayList<>(strings);
        return stringArrayList;
    }

    //提取左公因子
    public void leftFactoring() {
        for (int i = 0; i < VN.size(); i++) {
            String left = new String(VN.get(i));
            String a = maxPrefix(left);

            if (!a.isEmpty()) {
                String left_Epr = new String(left);
                while (VN.contains(left_Epr)) {
                    left_Epr += "'";
                }
                String newNT = left_Epr;
                VN.add(VN.indexOf(left), newNT);
                ArrayList<String> newRight1 = new ArrayList<String>();
                ArrayList<String> newRight2 = new ArrayList<String>();
                for (int j = 0; j < productions.get(left).size(); j++) {
                    String right = productions.get(left).get(j);
                    String newRight = new String();

                    if (!right.contains(a)) {
                        newRight1.add(right);
                    } else {
                        if (right.indexOf(a) + a.length() != right.length()) {
                            newRight = right.substring(right.indexOf(a) + a.length() + 1);
                        }
//                        a后面是空的
                        else {
                            newRight = "$";
                        }
                        newRight2.add(newRight);
                    }
                }
                //A->aA'
                newRight1.add(new String(a + " " + newNT));
                productions.put(left, newRight1);
                productions.put(newNT, newRight2);
            }
        }
        System.out.println(productions);
    }

    //最大的前缀
    private String maxPrefix(String left) {
//        用来储存返回的结果
        String prefix = new String();
//        拿到P的右边的全部产生式
        Vector<String> P = new Vector<String>(productions.get(left));
//        拿到第一个产生式
        String firstP = new String(P.get(0));
//        把firstP根据空格分开

        firstP += " ";

        while (firstP.indexOf(' ') != firstP.length() - 1) {
            String temp = firstP.substring(0, firstP.indexOf(" "));
            //System.out.println("第一个产生中的第一个token: "+temp);

            boolean flag = false;
            //遍历left的其他产生式
            for (int i = 1; i < P.size(); i++) {
                String right = new String(P.get(i));
                right += " ";

                String cmp = right.substring(0, right.indexOf(" "));

                if (!cmp.equals(temp)) {
                    break;
                } else {
                    if (flag == false) {
                        prefix += (temp + " ");
                    }
                    P.set(i, right.substring(right.indexOf(" ") + 1));
                    flag = true;
                }
            }
            firstP = firstP.substring(firstP.indexOf(" ") + 1);
        }
        if (prefix.length() > 0) {
            return prefix.trim();
        }
        return prefix;
    }

    //输出经过处理之后的文法
    public void grammarOutput(){
        for (String value : VN) {
            int count = 0;
            StringBuilder str = new StringBuilder();
            ArrayList<String> get = productions.get(value);
            for (int i = 0, getSize = get.size(); i < getSize; i++) {
                String s = get.get(i);
                List<String> list = Arrays.asList(s.trim().split("\\s+"));
                ArrayList<String> arrayList = new ArrayList<>(list);
                for (int j = 0; j < arrayList.size(); j++) {
                    str.append(arrayList.get(j));
                    if (j < arrayList.size() - 1) {
                        str.append(" ");
                    }
                }
                if (i<get.size()-1){
                    str.append("|");
                }
            }
            System.out.println(value+"->"+str);
        }
    }
}
