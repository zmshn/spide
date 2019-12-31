package com.managenode;

import com.managenode.message.MsgType;
import com.managenode.message.NodeMsg;
import com.managenode.message.NodeReply;
import com.managenode.message.TaskMsg;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SlaveNodeManager {
    public static final int READ_PORT = 10005;
    public static final int WRITE_PORT = 10006;
    public static final int TEST_PORT = 10008;
    public static final int SLAVE_NODE_TEST_PORT = 10007;
    private long timeout;
    private ServerSocket  serverSocket = new ServerSocket(READ_PORT);
    private ServerSocket testSocket = new ServerSocket(TEST_PORT);
    private static List<TaskMsg> tasks = new ArrayList<>();
    private static List<String> acked = new ArrayList<>();
    private List<String> IPs = new ArrayList<>();
    private List<String> freeIPs = new ArrayList<>();
    private List<SlaveNode> slaveNodes = new ArrayList<>();
    private List<NodeReply> nodeReplyList = new ArrayList<>();
    private int useingNodeNum = 0;
    private int ipNum = 0;

    public SlaveNodeManager() throws IOException {
        new Thread(()->{
            receiveTest();
        }).start();

        new Thread(()->{
            read();
        }).start();

        new Thread(()->{
            timeoutFind();
        }).start();
    }

    public List<NodeReply> getNodeReplyList() {
        List<NodeReply> results = nodeReplyList;
        nodeReplyList = new ArrayList<>();
        return results;
    }

    public int getIpNum() {
        return ipNum;
    }

    public int getUseingNodeNum() {
        return useingNodeNum;
    }

    public List<String> getIPs() {
        return IPs;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public long getTimeout() {
        return timeout;
    }

    private boolean testIP(String ip){
        try {
            Socket sck = new Socket(ip,SLAVE_NODE_TEST_PORT);
            PrintWriter writer = new PrintWriter(sck.getOutputStream());
            writer.write(ip);
            writer.close();
            sck.close();
            Thread.sleep(1000);
            if (acked.contains(ip)){
                acked.remove(ip);
                return true;
            }else{
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void receiveTest(){
        for (;;) {
            Socket socket = null;
            try {
                socket = testSocket.accept();
                Scanner scan = new Scanner(socket.getInputStream());
                String msg = scan.nextLine();
                if (isIP(msg)){
                    acked.add(msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally{
                if(socket != null){
                    try{
                        socket.close();
                    }catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    public static boolean isIP(String ip){
        String pattern = "((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(ip);
        if (m.matches()){
            return true;
        }else{
            return false;
        }
    }

    public boolean addIP(String ip){
        if (isIP(ip) && testIP(ip) && !IPs.contains(ip)){
            IPs.add(ip);
            freeIPs.add(ip);
            ipNum++;
            return true;
        }else{
            return false;
        }
    }

    public boolean deleteIP(String ip){
        if (!IPs.contains(ip)){
            return false;
        }
        if (freeIPs.contains(ip)){
            IPs.remove(ip);
            freeIPs.remove(ip);
            ipNum --;
            return true;
        }
        SlaveNode node = null;
        for (int i = 0; i < slaveNodes.size(); i ++){
            if (slaveNodes.get(i).getIP().equals(ip)){
                node = slaveNodes.get(i);
            }
        }
        if (freeIPs.size() == 0){
            return false;
        }
        node.setIP(freeIPs.get(0));
        IPs.remove(ip);
        ipNum--;
        return true;
    }

    public boolean addNode(){
        if (useingNodeNum >= ipNum){
            return false;
        }
        SlaveNode node = new SpiderSlaveNode(useingNodeNum + 1, freeIPs.get(0));
        useingNodeNum ++;
        freeIPs.remove(0);
        slaveNodes.add(node);
        return true;
    }

    public boolean addNodes(int num){
        if (ipNum - useingNodeNum < num){
            return false;
        }
        for (int i = 0; i < num; i++){
            addNode();
        }
        return true;
    }

    public boolean reduceNode(){
        if (useingNodeNum < 1){
            return false;
        }
        SlaveNode node = slaveNodes.get(slaveNodes.size() - 1);
        freeIPs.add(node.getIP());
        slaveNodes.remove(node);
        useingNodeNum --;
        return true;
    }

    public NodeReply write(NodeMsg msg){
        NodeReply reply = new NodeReply();
        if (msg.getNodeNum() > useingNodeNum || msg.getNodeNum() < 1){
            reply.setMsgType(MsgType.FALSE);
            reply.setNodeNum(msg.getNodeNum());
            reply.setMsg(String.format("Node[NodeNum:%d] does not exist", msg.getNodeNum()));
            return reply;
        }
        SlaveNode node = slaveNodes.get(msg.getNodeNum() - 1);
        if (node.write(msg.getMsg())){
            TaskMsg taskMsg = new TaskMsg();
            taskMsg.setMsg(msg);
            taskMsg.setTime(System.currentTimeMillis());
            tasks.add(taskMsg);
            reply.setMsgType(MsgType.SUCCESS);
            reply.setMsg("Message have been send to slave node");
            reply.setNodeNum(msg.getNodeNum());
            return reply;
        }else{
            boolean isSuccess = false;
            for (int i = 0;i < 3; i++){
                if (testIP(node.IP)){
                    isSuccess = true;
                    break;
                }
            }
            if (!isSuccess){
                String oldIP = node.getIP();
                if (changeNodeIP(node)){
                    reply = write(msg);
                    if (reply.getMsgType() == MsgType.SUCCESS){
                        reply.setMsgType(MsgType.WARING);
                        reply.setMsg("These IP can't be used:" + "[" + oldIP + "]");
                    }
                    return reply;
                }else{
                    String errorMsg = "These IP can't be used:";
                    for (String ip : freeIPs) {
                        errorMsg = errorMsg + "[" + ip + "]";
                    }
                    errorMsg = errorMsg + "[" + node.getIP() + "]";
                    reply.setMsgType(MsgType.FALSE);
                    reply.setNodeNum(msg.getNodeNum());
                    reply.setMsg(errorMsg);
                    return reply;
                }
            }else{
                return write(msg);
            }
        }
    }

    public boolean changeNodeIP(SlaveNode node){
        if (freeIPs.size() == 0){
            return false;
        }
        for (int i = 0; i < freeIPs.size(); i ++){
            String ip = freeIPs.get(i);
            if (testIP(ip)){
                String oldIP = node.getIP();
                node.setIP(ip);
                freeIPs.add(oldIP);
                return true;
            }
        }
        return false;
    }

    public void read(){
        for (;;){
            String msg = receive();
            JSONArray array = JSONArray.fromObject(msg);
            JSONObject object = (JSONObject) array.get(0);
            String originURL = (String) object.get("originURL");
            array.remove(0);
            NodeMsg nodeMsg = null;
            for (int i = 0; i < tasks.size(); i ++){
                NodeMsg task = tasks.get(i).getMsg();
                if (task.getMsg().equals(originURL)){
                    nodeMsg = task;
                    tasks.remove(task);
                    break;
                }
            }
            NodeReply reply = new NodeReply();
            reply.setMsgType(MsgType.SUCCESS);
            reply.setNodeNum(nodeMsg.getNodeNum());
            reply.setMsg(array.toString());
            nodeReplyList.add(reply);
        }
    }

    public String receive(){
        Socket socket = null;
        String msg = null;
        try {
            socket = serverSocket.accept();
            Scanner scan = new Scanner(socket.getInputStream());
            msg = scan.nextLine();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if(socket != null){
                try{
                    socket.close();
                }catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return msg;
    }

    public void timeoutFind(){
        for(;;){
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (tasks.size() != 0){
                long currentTime = System.currentTimeMillis();
                for (TaskMsg taskMsg : tasks){
                    SlaveNode node = slaveNodes.get(taskMsg.getMsg().getNodeNum() - 1);
                    if (currentTime - taskMsg.getTime() > timeout){
                        boolean isSuccess = false;
                        for (int i = 0;i < 3; i++){
                            if (testIP(node.IP)){
                                isSuccess = true;
                                break;
                            }
                        }
                        if (!isSuccess){
                            tasks.remove(taskMsg);
                            NodeReply reply = write(taskMsg.getMsg());
                            if (reply.getMsgType() != MsgType.SUCCESS){
                                nodeReplyList.add(reply);
                            }
                        }
                    }
                }
            }
        }
    }

}
