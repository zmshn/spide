package com.managenode;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class SpiderSlaveNode extends SlaveNode {
    public static final int READ_PORT = 10005;
    public static final int WRITE_PORT = 10006;

    public SpiderSlaveNode(int num, String IP) {
        super(num, IP);
    }

    @Override
    public boolean write(String msg) {
        Socket sck = null;
        try {
            sck = new Socket(IP,WRITE_PORT);
            PrintWriter writer = new PrintWriter(sck.getOutputStream());
            writer.write(msg);
            writer.close();
            sck.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

/*    @Override
    public String read() {
        Socket socket = null;
        String msg = null;
        try {
            ServerSocket  serverSocket = new ServerSocket(READ_PORT);
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
    }*/
}
