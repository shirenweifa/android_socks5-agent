package com.example.udptcp;

import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class socketinit {
    public  Socket stf;                                      //Tcp套接字
    private InputStream Trecvs;                              //接收流
    private DataOutputStream Tsends;                         //发送流

    public  DatagramSocket suf;                              //Udp套接字
    private DatagramPacket Urecvs;                           //Udp接收流
    private DatagramPacket Usends;                           //Udp发送流
    private InetAddress cilentAddress;                       //随时获取代理服务器ip

    private int clientPort;                                  //随时获取端口ip

    /*
    * Tcpinit              初始化Tcp
    * Trecv                接收服务器信息
    * Tsendbyte|Tsendstr   发送信息给服务器
    * Tclose               结束Tcp套接字
    * */
    public int Tcpinit(String Tip,int port)
    {
        try {
            //连接服务器
            stf=new Socket(Tip,port);
            Trecvs=stf.getInputStream();
            Tsends=new DataOutputStream(stf.getOutputStream());
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public byte[] Trecv()
    {
        final byte[] buffer = new byte[1024];
        try {
            Trecvs.read(buffer);
            return buffer;
        } catch (IOException e) {
            e.printStackTrace();
            return buffer;
        }
    }

    public int Tsendbyte(byte[] f)
    {
        try {
            Tsends.write(f);
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public int Tsendstr(String f)
    {
        try {
            Tsends.writeBytes(f);
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public void Tclose()
    {
        try {
            if(stf!=null) {
                stf.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    * Udpinit              初始化Udp
    * Urecv                接收服务器信息
    * Usendbyte|Usendstr   发送信息给服务器
    * Uclose               结束Tcp套接字
    * */
    public int Udpinit(String ip_agent,int Uport,String bip,int bport)
    {
        try {
            if (suf != null) {
                suf.close();
                suf = null;
            }
            InetAddress ipAddress = InetAddress.getByName(ip_agent);
            suf = new DatagramSocket(bport);
            suf.setReuseAddress(true);
            suf.connect(ipAddress,Uport);
            //suf.bind(new InetSocketAddress(bip,bport));
            return 1;
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public String Urecv()
    {
        byte []Mrecvbuf=new byte[1024];
        Urecvs=new DatagramPacket(Mrecvbuf,Mrecvbuf.length);
        try {
            suf.receive(Urecvs);
            cilentAddress=Urecvs.getAddress();
            clientPort=Urecvs.getPort();
            String RcvMsg = new String(Urecvs.getData(),Urecvs.getOffset(),Urecvs.getLength());
            return RcvMsg;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
    public int Usendbyte(byte[] f,String ip_agent,int Uport)
    {
        try {
            Usends = new DatagramPacket(f,f.length,new InetSocketAddress(ip_agent,Uport));
            Usends.setData(f);
            Usends.setLength(f.length);
           // Usends.setPort(Uport);
            //Usends.setAddress(InetAddress.getByName(ip_agent));
            suf.send(Usends);
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public int Usendstr(String f,String ip_agent,int Uport)
    {
        try {
            Usends = new DatagramPacket(f.getBytes(),f.getBytes().length,new InetSocketAddress(ip_agent,Uport));
            suf.send(Usends);
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public void Uclose()
    {
        suf.close();
    }
}
