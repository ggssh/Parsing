//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.gogogo.parsing;

import com.sun.istack.internal.NotNull;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Grammar {
    ArrayList<String> VT = new ArrayList();
    ArrayList<String> VN = new ArrayList();
    String S = "";
    HashMap<String, Set<String>> FIRST = new HashMap();
    HashMap<String, Set<String>> FOLLOW = new HashMap();
    HashMap<String, ArrayList<String>> productions = new HashMap();
    public static final char SINGLE_ANGLE_QUOTE = '\'';
    public static final char EPSILON = '$';

    public Grammar() {
    }

    public void setProductions(String filePath) {
        try {
            File file = new File(filePath);
            RandomAccessFile randomFile = new RandomAccessFile(file, "r");

            String line;
            while((line = randomFile.readLine()) != null) {
                String left = line.split("->")[0].trim();
                String tempRight = line.split("->")[1].trim();
                List<String> temp = Arrays.asList(tempRight.split("\\|"));
                ArrayList<String> right = new ArrayList(temp);
                if (this.productions.get(left) == null) {
                    this.productions.put(left, right);
                } else {
                    ((ArrayList)this.productions.get(left)).addAll(right);
                }
            }

            randomFile.close();
        } catch (IOException var9) {
            var9.printStackTrace();
        }

    }

    public HashMap<String, ArrayList<String>> getProductions() {
        return this.productions;
    }

    public void setVN(String filepath) {
        try {
            File file = new File(filepath);
            RandomAccessFile randomfile = new RandomAccessFile(file, "r");

            String line;
            while((line = randomfile.readLine()) != null) {
                String left = line.split("->")[0].trim();
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
        return this.VN;
    }

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
                    if (!this.VN.contains(production[j]) && !production[j].equals("$") && !this.VT.contains(production[j])) {
                        this.VT.add(production[j]);
                    }
                }
            }
        }

    }

    public ArrayList<String> getVT() {
        return this.VT;
    }

    public void allLeftRecursive() {
        if (this.productions != null && this.VN != null) {
            String res = "";
            HashMap<String, ArrayList<String>> tempProduction = new HashMap(this.productions);
            HashMap<String, ArrayList<ArrayList<String>>> temp = new HashMap();

            int i;
            ArrayList Ai;
            for(i = 0; i < tempProduction.size(); ++i) {
                Ai = new ArrayList();

                for(int l = 0; l < ((ArrayList)tempProduction.get(this.VN.get(i))).size(); ++l) {
                    List<String> tempList = Arrays.asList(((String)((ArrayList)tempProduction.get(this.VN.get(i))).get(l)).split(" "));
                    ArrayList<String> tempArrayList = new ArrayList(tempList);
                    Ai.add(tempArrayList);
                }

                temp.put(this.VN.get(i), Ai);
            }

            for(i = 0; i < this.VN.size(); ++i) {
                Ai = (ArrayList)temp.get(this.VN.get(i));
                if (Ai == null) {
                    throw new RuntimeException("ERROR");
                }

                StringBuilder builder = new StringBuilder();
                boolean flag = false;

                for(int j = 0; j < i; ++j) {
                    if (((ArrayList)Ai.get(0)).indexOf(this.VN.get(j)) == 0) {
                        flag = true;
                        ArrayList<ArrayList<String>> Aj = (ArrayList)temp.get(this.VN.get(j));
                        if (Aj == null) {
                            throw new RuntimeException("ERROR");
                        }

                        int k;
                        StringBuilder ts;
                        ArrayList aj;
                        Iterator var21;
                        for(k = 0; k < Ai.size() && ((ArrayList)Ai.get(k)).indexOf(this.VN.get(j)) == 0; ++k) {
                            ts = new StringBuilder();

                            for(int m = 1; m < ((ArrayList)Ai.get(k)).size(); ++m) {
                                ts.append((String)((ArrayList)Ai.get(k)).get(m)).append(" ");
                            }

                            var21 = Aj.iterator();

                            while(var21.hasNext()) {
                                aj = (ArrayList)var21.next();
                                StringBuilder tempAj = new StringBuilder();
                                Iterator var15 = aj.iterator();

                                while(var15.hasNext()) {
                                    String s = (String)var15.next();
                                    tempAj.append(s).append(" ");
                                }

                                builder.append(tempAj).append(ts).append("|");
                            }
                        }

                        while(k < Ai.size()) {
                            ts = new StringBuilder();
                            var21 = ((ArrayList)Ai.get(k)).iterator();

                            while(var21.hasNext()) {
                                String s = (String)var21.next();
                                ts.append(s).append(" ");
                            }

                            builder.append(ts).append("|");
                            ++k;
                        }

                        ArrayList<ArrayList<String>> resDoubleArrayList = new ArrayList();
                        List<String> resList = Arrays.asList(builder.toString().split("\\|"));
                        aj = new ArrayList(resList);
                        Iterator var24 = aj.iterator();

                        while(var24.hasNext()) {
                            String tempS = (String)var24.next();
                            List<String> list = Arrays.asList(tempS.split(" "));
                            resDoubleArrayList.add(new ArrayList(list));
                        }

                        Ai = resDoubleArrayList;
                        builder.setLength(0);
                    }
                }

                if (flag || ((ArrayList)Ai.get(0)).indexOf(this.VN.get(i)) == 0) {
                    this.directLeftRecursive((String)this.VN.get(i), Ai);
                }
            }

            this.VN = new ArrayList(this.productions.keySet());
        } else {
            throw new RuntimeException("ERROR");
        }
    }

    private void directLeftRecursive(String left, @NotNull ArrayList<ArrayList<String>> right) {
        String repl = left + '\'';
        StringBuilder r1 = new StringBuilder();
        StringBuilder r2 = new StringBuilder();
        Iterator var6 = right.iterator();

        while(var6.hasNext()) {
            ArrayList<String> stringArrayList = (ArrayList)var6.next();
            String s = "";

            for(Iterator var9 = stringArrayList.iterator(); var9.hasNext(); s = s + " ") {
                String str = (String)var9.next();
                s = s + str;
            }

            if (stringArrayList.indexOf(left) == 0) {
                r2.append(s.substring(s.indexOf(left) + left.length())).append(repl).append("|");
            } else {
                r1.append("|").append(s).append(repl);
            }
        }

        this.productions.put(left, this.str2Array(r1.substring(1)));
        this.productions.put(repl, this.str2Array(r2.append('$').toString()));
    }

    private ArrayList<String> str2Array(String str) {
        List<String> strings = Arrays.asList(str.split("\\|"));
        ArrayList<String> stringArrayList = new ArrayList(strings);
        return stringArrayList;
    }

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

    public void grammarOutput() {
        Iterator var1 = this.VN.iterator();

        while(var1.hasNext()) {
            String value = (String)var1.next();
            boolean count = false;
            StringBuilder str = new StringBuilder();
            ArrayList<String> get = (ArrayList)this.productions.get(value);
            int i = 0;

            for(int getSize = get.size(); i < getSize; ++i) {
                String s = (String)get.get(i);
                List<String> list = Arrays.asList(s.trim().split("\\s+"));
                ArrayList<String> arrayList = new ArrayList(list);

                for(int j = 0; j < arrayList.size(); ++j) {
                    str.append((String)arrayList.get(j));
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
}
