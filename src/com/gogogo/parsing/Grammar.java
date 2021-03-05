package com.gogogo.parsing;

import com.sun.istack.internal.NotNull;
import org.omg.CORBA.IRObject;

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
    String START;//开始符号
    HashMap<String, HashSet<String>> FIRST;//First集
    HashMap<String, HashSet<String>> FOLLOW;//Follow集
    HashMap<String, ArrayList<String>> productions;//产生式集
    public static final char SINGLE_ANGLE_QUOTE = '\'';//用来替换非终结符
    public static final String EPSILON = "$";//用来代替ε

    public Grammar() {
        VT = new ArrayList<>();
        VN = new ArrayList<>();
        START = "";
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
                if (productions.get(left) == null) {
                    productions.put(left, right);
                } else {
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
            while ((line = randomfile.readLine()) != null) {
                String left = line.split("->")[0].trim();
                if (START.isEmpty()) {
                    START = new String(left);
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

        for (int i = 0; i < this.VN.size(); ++i) {
            String left = (String) this.VN.get(i);
            ArrayList<String> rights = (ArrayList) this.productions.get(left);
            Iterator var4 = rights.iterator();

            while (var4.hasNext()) {
                String right = (String) var4.next();
                String[] production = right.split(" ");

                for (int j = 0; j < production.length; ++j) {
                    if (!this.VN.contains(production[j]) && !this.VT.contains(production[j])) {
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
    private void directLeftRecursive(String left, @NotNull ArrayList<ArrayList<String>> right) {
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
                s += " ";
            }
            if (stringArrayList.indexOf(left) == 0) {

                r2.append(s.substring(s.indexOf(left) + left.length())).append(repl).append("|");
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

    //将产生式右部转化为ArrayList<ArrayList<String>>
    private ArrayList<ArrayList<String>> right2Array(ArrayList<String> arrayList) {
        ArrayList<ArrayList<String>> arrayLists = new ArrayList<>();
        for (String s : arrayList) {
            List<String> list = Arrays.asList(s.split(" "));
            ArrayList<String> tempList = new ArrayList<>(list);
            arrayLists.add(tempList);
        }
        return arrayLists;
    }

    //提取左公因子
    public void leftFactoring() {
        for (int i = 0; i < this.VN.size(); ++i) {
            String left = new String((String) this.VN.get(i));
            ArrayList<String> production = (ArrayList) this.productions.get(left);
            new String();

            String a;
            while (!(a = this.longestCommonPrefix(production)).isEmpty()) {
                String left_Epr;
                for (left_Epr = new String(left); this.VN.contains(left_Epr); left_Epr = left_Epr + "'") {
                }

                String newNT = new String(left_Epr);
                this.VN.add(this.VN.indexOf(left) + 1, newNT);
                ArrayList<String> newRight = new ArrayList();
                ArrayList<String> newRight_Epr = new ArrayList();
                Iterator var9 = production.iterator();

                while (true) {
                    while (var9.hasNext()) {
                        String right = (String) var9.next();
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
                    production = new ArrayList((Collection) this.productions.get(left));
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

            for (int i = 0; i < count; ++i) {
                for (int j = i + 1; j < count; ++j) {
                    String temp = this.longestCommonPrefix((String) strs.get(i), (String) strs.get(j));
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
        for (index = 0; index < length && str1.charAt(index) == str2.charAt(index); ++index) {
        }

        return str1.substring(0, index).trim();
    }

    //输出经过处理之后的文法
    public void grammarOutput() {
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
                if (i < get.size() - 1) {
                    str.append("|");
                }
            }
            System.out.println(value + "->" + str);
        }
    }

    //获得First集
    public void getFirst() {
        //first集合
        Iterator<String> it = VN.iterator();
        //遍历每一个非终结符号
        while (it.hasNext()) {
            //存放单个非终结符号的FIRST
            HashSet<String> firstCell = new HashSet<>();
            //读取非终结符号的所有产生式
            String key = it.next();
            ArrayList<String> list = productions.get(key);
            //遍历非终结符号的所有产生式
            for (String s : list) {
                //将每个产生式读取进去字符串数组,方便后续的操作
                String[] listCell = s.split(" ");
                //如果第一个字符是终结符号,就直接加入
                if (VT.contains(listCell[0])) {
                    firstCell.add(listCell[0]);
                }
                //如果不是终结符号,就进行一系列的逻辑处理
                else {
                    //标记是否有定义为空,如果有就检查下一个字符
                    boolean[] isVn = new boolean[listCell.length];
                    //第一个符号肯定是非终结符号,所以先检查第一个
                    int p = 0;
                    isVn[p] = true;
                    //从第一个开始检查
                    while (isVn[p]) {
                        //如果p指的位置出现了终结符号,那么就直接加入FIRST集合,并且直接跳出循环
                        if (VT.contains(listCell[p])) {
                            firstCell.add(listCell[p]);
                            break;
                        }
                        //走到这一步代表这个符号是非终结符号了,有点好奇为什么加入栈
                        String vnGo = listCell[p];
                        Stack<String> stack = new Stack<>();
                        stack.push(vnGo);
                        while (!stack.empty()) {
                            //拿到这些非终结符号的产生式(就是栈顶元素的)
                            ArrayList<String> listGo = productions.get(stack.pop());
                            //遍历这个非终结符号的每一个产生式
                            for (String go : listGo) {
                                //先拿到第一个产生式
                                String[] listGoCell = go.split(" ");
                                //如果该非终结符号的产生式的第一个符号是终结符号的话
                                if (VT.contains(listGoCell[0])) {
                                    //如果该终结符号是$的话
                                    if (listGoCell[0].equals(EPSILON)) {
                                        //开始符号不能推出空
                                        if (!key.equals(START)) {
                                            firstCell.add(listGoCell[0]);
                                        }
                                        if (p + 1 < isVn.length) {
                                            isVn[p + 1] = true;
                                        }
                                    }
                                    //如果终结符号不是$的话
                                    else {
                                        firstCell.add(listGoCell[0]);
                                    }
                                }
                                //如果该非终结符号的产生式的第一个符号不是终结符号的话,入栈继续判断
                                else {
                                    stack.push(listGoCell[0]);
                                }
                            }
                        }
                        //进行下一次循环
                        p++;
                        if (p > isVn.length - 1) {
                            break;
                        }
                    }
                }
            }
            //将firstCell放进去FIRST集合
            FIRST.put(key, firstCell);
        }
    }

    public HashMap<String, HashSet<String>> getFIRST() {
        return FIRST;
    }

    //获得Follow集
    public void getFollow() {
        //迭代器,方便对容器中的元素进行遍历
        Iterator<String> it = VN.iterator();
        HashMap<String, HashSet<String>> keyFollow = new HashMap<>();
        // 用于存放/A->...B 或者 A->...Bε的组合
        ArrayList<HashMap<String, String>> VnList = new ArrayList<>();
        HashSet<String> VnListLeft = new HashSet<>();
        HashSet<String> VnListRight = new HashSet<>();
        //开始符号加入#
        //在创建hashset的同时进行add操作
        keyFollow.put(START, new HashSet<String>() {
            private static final long serialVersionUID = 1L;

            {
                add(new String("#"));
            }
        });

        while (it.hasNext()) {
            String key = it.next();
            //将产生式的右部变换为二维数组类型
            ArrayList<ArrayList<String>> list = right2Array(productions.get(key));
            ArrayList<String> listCell;

            //先把每个VN作为keyFollow的key,之后再查找添加其Follow元素
            if (!keyFollow.containsKey(key)) {
                keyFollow.put(key, new HashSet<>());
            }
            keyFollow.toString();

            for (ArrayList<String> arrayList : list) {
                listCell = arrayList;

                //1.直接找非终结符号后面跟着终结符号
                for (int i = 1; i < listCell.size(); i++) {
                    HashSet<String> set = new HashSet<>();
                    if (VT.contains(listCell.get(i))) {
                        set.add(listCell.get(i));
                        //如果keyFollow中键包含当前终结符前面的那个非终结符时,将该键所对应的set重新加入至当前set集合,进行更新
                        if (keyFollow.containsKey(listCell.get(i - 1))) {
                            set.addAll(keyFollow.get(listCell.get(i - 1)));
                        }
                        keyFollow.put(listCell.get(i - 1), set);
                    }
                }
                //2.找...VnVn...组合
                for (int i = 0; i < listCell.size() - 1; i++) {
                    HashSet<String> set = new HashSet<>();
                    //如果当前是一个非终结符而且之后一个仍然是非终结符
                    if (VN.contains(listCell.get(i)) && VN.contains(listCell.get(i + 1))) {
                        set.addAll(FIRST.get(listCell.get(i + 1)));
                        set.remove(EPSILON);

                        if (keyFollow.containsKey(listCell.get(i))) {
                            set.addAll(keyFollow.get(listCell.get(i)));
                        }
                        keyFollow.put(listCell.get(i), set);
                    }
                }
                //3.A->...B或者A->...B$(可以有n个$)的组合存起来
                for (int i = 0; i < listCell.size(); i++) {
                    HashMap<String, String> newVn;
                    // 是VN且A不等于B(产生式左部的非终结符)
                    if (VN.contains(listCell.get(i)) && !listCell.get(i).equals(key)) {
                        //标记VN后是否为空
                        boolean isAllNull = false;
                        //即A->...B$(可以有多个$)
                        if (i + 1 < listCell.size()) {
                            for (int j = i + 1; j < listCell.size(); j++) {
                                //如果其后面的都是VN且其FIRST中包含EPSILON
                                if ((FIRST.containsKey(listCell.get(j)) ? FIRST.get(listCell.get(j)).contains(EPSILON)
                                        : false)) {
                                    isAllNull = true;
                                } else {
                                    isAllNull = false;
                                    break;
                                }
                            }
                        }
                        //如果是最后一个为VN,即A->...B
                        if (i == listCell.size() - 1) {
                            isAllNull = true;
                        }
                        if (isAllNull) {
                            VnListLeft.add(key);
                            VnListRight.add(listCell.get(i));

                            //往VnList中添加,分存在和不存在两种情况
                            boolean isHaveAdd = false;
                            for (int x = 0; x < VnList.size(); x++) {
                                HashMap<String, String> VnListCell = VnList.get(x);
                                if (!VnListCell.containsKey(key)) {
                                    VnListCell.put(key, listCell.get(i));
                                    VnList.set(x, VnListCell);
                                    isHaveAdd = true;
                                    break;
                                } else {
                                    //去重
                                    if (VnListCell.get(key).equals(listCell.get(i))) {
                                        isHaveAdd = true;
                                        break;
                                    }
                                    continue;
                                }
                            }
                            //如果没有添加,表示是新的组合
                            if (!isHaveAdd) {
                                newVn = new HashMap<>();
                                newVn.put(key, listCell.get(i));
                                VnList.add(newVn);
                            }
                        }
                    }
                }
            }
        }

        keyFollow.toString();

        //4.VnListLeft减去VnListRight,剩下的就是入口产生式
        VnListLeft.removeAll(VnListRight);
        //用栈或者队列都可以
        Queue<String> keyQueue = new LinkedList<>();
        Iterator<String> itVnVn = VnListLeft.iterator();
        while (itVnVn.hasNext()) {
            keyQueue.add(itVnVn.next());
        }
        while (!keyQueue.isEmpty()) {
            String keyLeft = keyQueue.poll();
            for (int t = 0; t < VnList.size(); t++) {
                HashMap<String, String> VnListCell = VnList.get(t);
                if (VnListCell.containsKey(keyLeft)) {
                    HashSet<String> set = new HashSet<>();
                    //原来的FOLLOW加上左边的FOLLOW
                    if (keyFollow.containsKey(keyLeft)) {
                        set.addAll(keyFollow.get(keyLeft));
                    }
                    if (keyFollow.containsKey(VnListCell.get(keyLeft))) {
                        set.addAll(keyFollow.get(VnListCell.get(keyLeft)));
                    }
                    keyFollow.put(VnListCell.get(keyLeft), set);
                    keyQueue.add(VnListCell.get(keyLeft));

                    //移除已处理的组合
                    VnListCell.remove(keyLeft);
                    VnList.set(t, VnListCell);
                }
            }
        }
        //莫名会出现$,目前没有发现问题出在哪
        for (int cnt =0;cnt<VN.size();cnt++){
            keyFollow.get(VN.get(cnt)).remove(EPSILON);
        }
        HashMap<String,HashSet<String>> res = new HashMap<>();
        for (String s:VN){
            res.put(s,keyFollow.get(s));
        }
        //此时keyFollow为完整的FOLLOW集
        FOLLOW = res;
    }

    public HashMap<String, HashSet<String>> getFOLLOW() {
        return FOLLOW;
    }
}
