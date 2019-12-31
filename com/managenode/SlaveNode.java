package com.managenode;

import java.io.IOException;

public abstract class SlaveNode {
    protected String IP = null;
    protected int num;

    public SlaveNode(int num, String IP){
        this.IP = IP;
        this.num = num;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }
    public String getIP() {
        return IP;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getNum() {
        return num;
    }

    public abstract boolean write(String msg);

 //   public abstract String read();

}
