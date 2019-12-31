package com.managenode.message;

public class NodeReply {
    private MsgType msgType;
    private String msg;
    private int nodeNum;

    public NodeReply(int nodeNum, MsgType msgType, String msg){
        this.msg = msg;
        this.msgType = msgType;
        this.nodeNum = nodeNum;
    }

    public NodeReply(){
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setMsgType(MsgType msgType) {
        this.msgType = msgType;
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

    public MsgType getMsgType() {
        return msgType;
    }
}
