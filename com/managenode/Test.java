package com.managenode;

public class Test {
    public static void main(String args[]){
        SpiderSlaveNode node = new SpiderSlaveNode(1,"127.0.0.1");
        node.write("https://me.csdn.net/follow/dataiyangu");
        //String msg = node.read();
        //System.out.println(msg);
    }
}
