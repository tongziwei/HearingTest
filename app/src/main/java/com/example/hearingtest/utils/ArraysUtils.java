package com.example.hearingtest.utils;

import java.util.ArrayList;
import java.util.List;

public class ArraysUtils {
    public static int sum(int[] intArray){
        int total =0;
        for(int item :intArray){
            total+= item;
        }
        return total;
    }

    public static double sum(List<Double> doubleList){
        double total =0;
        for(double item:doubleList){
            total+=item;
        }
        return total;
    }

    public static int avg(int[] intArray){
        if(intArray.length==0){
            return 0;
        }
        int total = sum(intArray);
        return total/intArray.length;
    }

    public static double avg(List<Double> doubleList){
        if(doubleList.isEmpty()){
            return 0;
        }
        double total = sum(doubleList);
        return total/doubleList.size();
    }

    /**
     * 对后num个数取平均
     * @param doubleList
     * @param num
     * @return
     */
    public static double avg(List<Double> doubleList,int num){
        if(doubleList.isEmpty()){
            return 0;
        }
        if(doubleList.size()<num){
            double total = sum(doubleList);
            return total/doubleList.size();
        }else{
            //长度足够，选后半部分
            List<Double> dous = new ArrayList<>();
            int index = doubleList.size() - num;
            for(int i=0;i<num;i++){
                dous.add(doubleList.get(index+i));
            }
            double total = sum(dous);
            return total/dous.size();
        }
    }

    /**
     * 截取数组
     */
    public static double[] sub(List<Double> doubleList,int num){
        double[] dous = new double[num];
        if(doubleList.size()<num){
            //长度不够
            for(int i=0;i<doubleList.size();i++){
                dous[i]=doubleList.get(i);
            }
            for(int i=doubleList.size();i<num;i++){
                dous[i]=0;
            }

        }else{
            //长度足够，选后半部分
            int index = doubleList.size() - num;
            for(int i=0;i<num;i++){
                dous[i]= doubleList.get(index+i);
            }

        }
        return dous;
    }


}
