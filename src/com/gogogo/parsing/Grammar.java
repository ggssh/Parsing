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
            File file = new File(filepath);
            RandomAccessFile randomfile = new RandomAccessFile(file, "r");

            String line;
            while((line = randomfile.readLine()) != null) {
                String left = line.split("->")[0].trim();
                if(S.isEmpty()){
                    S = new String(left);
                }
                if (!this.VN.contains(left)) {
                    this.VN.add(left);
                }
            }

            randomfile.close();
        } catch (Exception var6) {
            var6.printStackTrace();
        }

    }

    public ArrayList<String> getVN() {
        return VN;
    }

    //读取终结符
    public void setVT() {
        new ArrayList();

        for(int i = 0; i < this.VN.size(); ++i) {
            String left = (String)this.VN.get(i);
            ArrayList<String> rights = (ArrayList)this.productions.get(left);
            Iterator var4 = rights.iterator();

            while(var4.hasNext()) {
                String right = (String)var4.next();
                String[] production = right.split(" ");

                for(int j = 0; j < production.length; ++j) {
                    if (!this.VN.contains(production[j])  && !this.VT.contains(production[j])) {
                        this.VT.add(production[j]);
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
        for(int i = 0; i < this.VN.size(); ++i) {
            String left = new String((String)this.VN.get(i));
            ArrayList<String> production = (ArrayList)this.productions.get(left);
            new String();

            String a;
            while(!(a = this.longestCommonPrefix(production)).isEmpty()) {
                String left_Epr;
                for(left_Epr = new String(left); this.VN.contains(left_Epr); left_Epr = left_Epr + "'") {
                }

                String newNT = new String(left_Epr);
                this.VN.add(this.VN.indexOf(left) + 1, newNT);
                ArrayList<String> newRight = new ArrayList();
                ArrayList<String> newRight_Epr = new ArrayList();
                Iterator var9 = production.iterator();

                while(true) {
                    while(var9.hasNext()) {
                        String right = (String)var9.next();
                        new String();
                        if (!right.contains(a)) {
                            newRight.add(right);
                        } else {
                            String newright;
                            if (right.indexOf(a) + a.length() != right.length() && right.indexOf(a) == 0) {
                                newright = right.substring(right.indexOf(a) + a.length() + 1);
                                newRight_Epr.add(newright);
                            } else if (right.indexOf(a) + a.length() != right.length() && right.indexOf(a) != 0) {
                                newRight.add(right);
                            } else {
                                newright = "$";
                                newRight_Epr.add(newright);
                            }
                        }
                    }

                    newRight.add(new String(a + " " + newNT));
                    this.productions.put(left, newRight);
                    this.productions.put(newNT, newRight_Epr);
                    production = new ArrayList((Collection)this.productions.get(left));
                    break;
                }
            }
        }

        System.out.println(this.productions);
    }

    //最大的前缀
    public String longestCommonPrefix(ArrayList<String> strs) {
        if (strs != null && strs.size() != 0) {
            int count = strs.size();
            String ans = new String();

            for(int i = 0; i < count; ++i) {
                for(int j = i + 1; j < count; ++j) {
                    String temp = this.longestCommonPrefix((String)strs.get(i), (String)strs.get(j));
                    if (temp.length() != 0 && ans.length() < temp.length()) {
                        ans = new String(temp);
                    }
                }
            }

            return ans.trim();
        } else {
            return "";
        }
    }
    public String longestCommonPrefix(String str1, String str2) {
        int length = Math.min(str1.length(), str2.length());

        int index;
        for(index = 0; index < length && str1.charAt(index) == str2.charAt(index); ++index) {
        }

        return str1.substring(0, index).trim();
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

    public void getFirst(){
//      first集合
        Iterator<String> it = VN.iterator();
//        遍历每一个非终结符号
        while(it.hasNext()){
//            存放单个非终结符号的FIRST
            HashSet<String> firstCell = new HashSet<>();
//            读取非终结符号的所有产生式
            String key = it.next();
            ArrayList<String> list = productions.get(key);
//            遍历非终结符号的所有产生式
            for(int i =0;i<list.size();i++){
//                将每个产生式读取进去字符串数组,方便后续的操作
                String[] listCell = list.get(i).split(" ");
//                如果第一个字符是终结符号,就直接加入
                if(VT.contains(listCell[0])){
                    firstCell.add(listCell[0]);
                }
//                如果不是终结符号,就进行一系列的逻辑处理
                else{
                    //标记是否有定义为空,如果有就检查下一个字符
                    boolean[] isVn = new boolean[listCell.length];
                    //第一个符号肯定是非终结符号,所以先检查第一个
                    int p =0;
                    isVn[p]=true;
//                    从第一个开始检查
                    while(isVn[p]){
//                        如果p指的位置出现了终结符号,那么就直接加入FIRST集合,并且直接跳出循环
                        if(VT.contains(listCell[p])){
                            firstCell.add(listCell[p]);
                            break;
                        }
//                        走到这一步代表这个符号是非终结符号了,有点好奇为什么加入栈
                        String vnGo = listCell[p];
                        Stack<String> stack = new Stack<>();
                        stack.push(vnGo);
                        while(!stack.empty()){
                            //拿到这些非终结符号的产生式(就是栈顶元素的)
                            ArrayList<String> listGo = productions.get(stack.pop());
//                            遍历这个非终结符号的每一个产生式
                            for(int k =0;k<listGo.size();k++){
//                                先拿到第一个产生式
                                String[] listGoCell = listGo.get(k).split(" ");
//                                如果该非终结符号的产生式的第一个符号是终结符号的话
                                if(VT.contains(listGoCell[0])){
//                                    如果该终结符号是$的话
                                    if(listGoCell[0].equals("$")){
//                                    开始符号不能推出空
                                        if(!key.equals(S)){
                                            firstCell.add(listGoCell[0]);
                                        }
                                        if(p+1<isVn.length){
                                            isVn[p+1]=true;
                                        }
                                    }
//                                    如果终结符号不是$的话
                                    else{
                                        firstCell.add(listGoCell[0]);
                                    }
                                }
//                                如果该非终结符号的产生式的第一个符号不是终结符号的话,入栈继续判断
                                else{
                                    stack.push(listGoCell[0]);
                                }
                            }
                        }
                        //进行下一次循环
                        p++;
                        if(p>isVn.length-1){
                            break;
                        }
                    }
                }
            }
//            将firstCell放进去FIRST集合
            FIRST.put(key,firstCell);
        }
        System.out.println(FIRST+"FIRST");
    }
}
