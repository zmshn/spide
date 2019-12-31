package com.managenode;

import com.managenode.message.NodeMsg;
import com.managenode.message.NodeReply;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String args[]) throws IOException, InterruptedException {
        //SpiderSlaveNode node = new SpiderSlaveNode(1,"127.0.0.1");
        //node.write("https://me.csdn.net/follow/dataiyangu");
        //String msg = node.read();
        //System.out.println(msg);
        SlaveNodeManager manager = new SlaveNodeManager();
        manager.addIP("127.0.0.1");
        manager.addNode();
        NodeMsg msg = new NodeMsg(1,"https://me.csdn.net/follow/horses");
        NodeReply writeReply = manager.write(msg);
        System.out.println(writeReply.getMsgType().toString());
        List<NodeReply> receiveRely = new ArrayList<>();
        while(receiveRely.size() == 0){
            receiveRely = manager.getNodeReplyList();
            Thread.sleep(1000);
        }
        for (NodeReply reply : receiveRely){
            System.out.println(reply.getMsg());
        }
    }
}
