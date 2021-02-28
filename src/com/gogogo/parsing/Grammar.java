package com.gogogo.parsing;

import java.io.File;
import java.io.FileNotFoundException;
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
    ArrayList<String> VT=new ArrayList<>();//终结符
    ArrayList<String> VN=new ArrayList<>();//非终结符
    String S=new String();//开始符号
    HashMap<String,ArrayList<String>> FIRST=new HashMap<>();//First集
    HashMap<String,ArrayList<String>> FOLLOW=new HashMap<>();//Follow集
    ArrayList<Production> productions = new ArrayList<>();//产生式集

    public void setProductions(String path){
        try {
            File file =new File(path);
            RandomAccessFile randomAccessFile = new RandomAccessFile(file,"r");
            String line;
            String left;
            while ((line=randomAccessFile.readLine())!=null){
                left =line.split("->")[0].trim();
                String temp = line.split("->")[1].trim();
                List<String> result1 = Arrays.asList(temp.split("\\|"));
                ArrayList<String> tempWithoutOr = new ArrayList<>(result1);
                ArrayList<ArrayList<String>> right = new ArrayList<>();
                for(String str:tempWithoutOr){
                    List<String> result2 = Arrays.asList(str.split(" "));
                    ArrayList<String> result3 = new ArrayList<>(result2);
                    right.add(result3);
                }
                Production production = new Production(left,right);
                productions.add(production);
            }
            randomAccessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void show(){
        for (Production production: productions){
            System.out.print(production.left+" : ");
            for (int i=0;i<production.right.size();i++){
                System.out.print(production.right.get(i));
            }
            System.out.println();
        }
        System.out.println("非终结符集 : "+VT.toString());
        System.out.println("终结符集 : "+VN.toString());
    }

    //读取非终结符号集
    public void setVN(String filepath){
        try {
            //读取文件，然后一行一行地进行读取
            File file = new File(filepath);
            RandomAccessFile randomfile = new RandomAccessFile(file, "r");

            String line;
            String left;
            while((line = randomfile.readLine()) != null) {
                left = line.split("->")[0].trim();
                if (!this.VN.contains(left)) {
                    this.VN.add(left);
                }
            }
            randomfile.close();
        } catch (Exception var5) {
            var5.printStackTrace();
        }
    }

    //读取终结符
    public void setVT() {
        ArrayList<ArrayList<String>> rights;
        for(int i =0;i<productions.size();i++){
            //rights代表的是产生式右边的所有产生式
            rights = productions.get(i).getRight();

            for(int j =0;j<rights.size();j++){
                //right代表的是右边其中一条的产生式
                ArrayList<String> right= rights.get(j);
                for(int k = 0;k<right.size();k++){
                    if(VN.contains(right.get(k))||right.get(k).equals("$")){
                        continue;
                    }
                    else{
                        if(!VT.contains(right.get(k))){
                            VT.add(right.get(k));
                        }
                    }
                }
            }
        }
    }
}
