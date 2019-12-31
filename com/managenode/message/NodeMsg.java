package com.managenode.message;

public class NodeMsg {
    private String msg;
    private int nodeNum;

    public NodeMsg(int nodeNum, String msg){
        this.msg = msg;
        this.nodeNum = nodeNum;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setNodeNum(int nodeNum) {
        this.nodeNum = nodeNum;
    }

    public String getMsg() {
        return msg;
    }

    public int getNodeNum() {
        return nodeNum;
    }

}
