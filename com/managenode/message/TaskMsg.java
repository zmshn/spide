package com.managenode.message;

public class TaskMsg {
    private NodeMsg msg;
    private Long time;

    public void setMsg(NodeMsg msg) {
        this.msg = msg;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public NodeMsg getMsg() {
        return msg;
    }
}
