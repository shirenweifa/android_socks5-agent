package com.example.udptcp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class udpface extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.udpface, container, false);
    }
    Button UdpClick;
    socketinit sock;
    SharedPreferences Data;
    SharedPreferences.Editor editor;

    String user,password;

    String udpip;           //本地用户ip
    short udpport;          //本地用户端口

    String udpip_agent;     //代理服务器
    int    udpport_agent;   //代理端口

    String muip;
    short  muport;

    String wenti;
    String nei;

    boolean to=false;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
        UdpClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(UdpClick.getText().equals("代理"))
                {
                    renzhen();
                    //to=true;
                    //ceshi();
                }
                else if(UdpClick.getText().equals("成功"))
                {
                    TUclose();
                    handler.sendEmptyMessage(6);
                }
            }
        });
    }
    /*
    protected void ceshi()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(sock.Udpinit(udpip_agent,udpport_agent,udpip,udpport)==0)
                {
                    handler.sendEmptyMessage(3);
                    sock.Uclose();
                    handler.sendEmptyMessage(5);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.sendEmptyMessage(6);
                    return;
                }
                handler.sendEmptyMessage(4);
                while(to)
                {
                    byte[] buffer=new byte[30];       //内容
                    int ipv4=getIPV4(muip);
                    buffer[0]=0x00;
                    buffer[1]=0x00;
                    buffer[2]=0x00;
                    buffer[3]=0x01;
                    buffer[4]=(byte) (ipv4>>24);
                    buffer[5]=(byte) (ipv4>>16);
                    buffer[6]=(byte) (ipv4>>8);
                    buffer[7]=(byte) (ipv4&0x000000ff);
                    buffer[8]=(byte) (muport>>8);
                    buffer[9]=(byte) (muport&0x00ff);
                    String data="hello,world";
                    buffer=memcpys(buffer,data,10,data.length());
                    wenti="buffer";
                    nei="\nbuffer值："+byte2hex(buffer);
                    handler.sendEmptyMessage(0);

                    if(sock.Usendbyte(buffer,muip,muport)==0)
                    {
                        handler.sendEmptyMessage(7);
                        Log.d("Usend：","发送失败"+muip+":"+String.valueOf(muport));
                    }
                    else
                    {
                        Log.d("Usend：","发送成功"+muip+":"+String.valueOf(muport));
                    }

                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //wenti=sock.Urecv();
                    //Toast.makeText(getActivity(),wenti,Toast.LENGTH_SHORT).show();

                }
            }
        }).start();
    }*/

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(sock.suf!=null||sock.stf!=null)
        {
            TUclose();
        }
    }

    private void init() {
        UdpClick=getActivity().findViewById(R.id.UdpClick);
        Data= getActivity().getSharedPreferences("key", Activity.MODE_PRIVATE);
        editor=Data.edit();
        user=Data.getString("user","");
        password=Data.getString("password","");

        //初始化udp    udpip:本地ip udpport:本地端口
        udpip="0.0.0.0";
        udpport=25500;
        //目标ip及端口
        muip="39.103.165.21";
        muport=7355;
        //初始化
        sock=new socketinit();
    }

    private void TUclose()
    {
        to=false;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sock.Tclose();
        sock.Uclose();
        Toast.makeText(getActivity(),"Udp代理结束",Toast.LENGTH_SHORT).show();
    }

    //进行socks5代理
    private int renzhen()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(sock.Tcpinit("121.40.209.60",1080)==0)
                {
                    handler.sendEmptyMessage(2);
                    sock.Tclose();
                    handler.sendEmptyMessage(5);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.sendEmptyMessage(6);
                    return;
                }
                /*
                +------+-------------------+------------+
            　　|VER   | Number of METHODS | METHODS    |
            　　+------+-------------------+------------+
            　　| 0x05 | 0x02 (有两个方法)  | 0x00 | 0x02|
            　　+------+-------------------+------------+
                */
                byte[] buf=new byte[]{0x05,0x02,0x00,0x02};
                if(sock.Tsendbyte(buf)==0)
                {
                    wenti="Tsend:发送协商认证方式失败";
                    nei="\nbuf值："+byte2hex(buf);
                    handler.sendEmptyMessage(0);
                    sock.Tclose();
                    handler.sendEmptyMessage(5);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.sendEmptyMessage(6);
                    return;
                }
                buf=sock.Trecv();
                if(buf[1]==0x02)
                {
                    /*
                       +----+------+----------+------+----------+
                       |VER | ULEN |  UNAME   | PLEN |  PASSWD  |
                       +----+------+----------+------+----------+
                       | 1  |  1   | 1 to 255 |  1   | 1 to 255 |
                       +----+------+----------+------+----------+
                    */
                    buf[0]=0x01;
                    buf[1]=(byte) (user.length());
                    buf=memcpys(buf,user,2,user.length());
                    buf[2+user.length()]=(byte)(password.length());
                    buf=memcpys(buf,password,3+user.length(),password.length());
                    if(sock.Tsendbyte(buf)==0)
                    {
                        wenti="Tsend:发送身份验证信息给服务器失败";
                        nei="\nbuf值："+byte2hex(buf);
                        handler.sendEmptyMessage(0);
                        sock.Tclose();
                        handler.sendEmptyMessage(5);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        handler.sendEmptyMessage(6);
                        return;
                    }
                    buf=sock.Trecv();
                    //验证成功
                    if(buf[0]==0x01&&buf[1]==0x00)
                    {
                        /*
                        客户端会用通过认证的这个TCP连接发送UDP穿透请求，信令格式如下：
                         +----+-----+-------+------+----------+----------+
                         |VER | CMD |  RSV  | ATYP | DST.ADDR | DST.PORT |
                         +----+-----+-------+------+----------+----------+
                         | 1  |  1  | X'00' |  1   | Variable |    2     |
                         +----+-----+-------+------+----------+----------+

                        client的本地地址即端口,这个很重要，要填客户端想发送/接收UDP包的本地端口
                        Where:
                        o  VER    protocol version: X'05'
                        o  CMD
                            o  CONNECT X'01'
                            o  BIND X'02'
                            o  UDP ASSOCIATE X'03'
                        o  RSV    RESERVED
                        o  ATYP   address type of following address
                            o  IP V4 address: 		X'01'
                            o  DOMAINNAME:		 X'03'
                            o  IP V6 address: 	X'04'
                        o  DST.ADDR       desired destination address
                        o  DST.PORT desired destination port in network octet
                        order client的本地地址即端口
                        */
                        buf=new byte[10];
                        int ipv4=getIPV4(udpip);
                        buf[0]=0x05;
                        buf[1]=0x03;        //此为Udp代理
                        buf[2]=0x00;
                        buf[3]=0x01;
                        buf[4]=(byte) (ipv4>>24);
                        buf[5]=(byte) (ipv4>>16);
                        buf[6]=(byte) (ipv4>>8);
                        buf[7]=(byte) (ipv4&0x000000ff);
                        buf[8]=(byte) (udpport>>8);
                        buf[9]=(byte) (udpport&0x00ff);
                        if(sock.Tsendbyte(buf)==0)
                        {
                            wenti="Tsend:发送UDP穿透请求失败";
                            nei="\nbuf值："+byte2hex(buf);
                            handler.sendEmptyMessage(0);
                            sock.Tclose();
                            handler.sendEmptyMessage(5);
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            handler.sendEmptyMessage(6);
                            return;
                        }
                        buf=sock.Trecv();
                        /*
                             +----+-----+-------+------+----------+----------+
                             |VER | REP |  RSV  | ATYP | BND.ADDR | BND.PORT |
                             +----+-----+-------+------+----------+----------+
                             | 1  |  1  | X'00' |  1   | Variable |    2     |
                             +----+-----+-------+------+----------+----------+

                             Where:

                             o  VER    protocol version: X'05'
                             o  REP    Reply field:
                                 o  X'00' succeeded
                                 o  X'01' general SOCKS server failure
                                 o  X'02' connection not allowed by ruleset
                                 o  X'03' Network unreachable
                                 o  X'04' Host unreachable
                                 o  X'05' Connection refused
                                 o  X'06' TTL expired
                                 o  X'07' Command not supported
                                 o  X'08' Address type not supported
                                 o  X'09' to X'FF' unassigned
                             o  RSV    RESERVED
                             o  ATYP   address type of following address
                             o  IP V4 address: 		X'01'
                             o  DOMAINNAME:		    X'03'
                             o  IP V6 address: 	    X'04'
                             o  BND.ADDR   此UDP穿透通道对应的代理服务器地址。
                             o  BND.PORT    此UDP穿透通道对应的代理服务器端口。
                             至此，UDP穿透通道已经被建起来了，客户端只要按标准格式将UDP包发往上述地址端口，UDP包就会被代理服务器转发出去。
                         */
                        // 接收回应
                        if(buf[0]==0x05&&buf[1]==0x00)
                        {
                            wenti="温馨提示：成功穿透UDP";
                            nei="\nbuf值："+byte2hex(buf);
                            handler.sendEmptyMessage(0);
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            //sock.Tclose();
                            to=true;
                            udp_agent(buf);    //开始进行udp代理
                        }
                        else
                        {
                            wenti="温馨提示：未能成功穿透UDP";
                            nei="\nbuf值："+byte2hex(buf);
                            handler.sendEmptyMessage(0);
                            sock.Tclose();
                            handler.sendEmptyMessage(5);
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            handler.sendEmptyMessage(6);
                            to=false;
                        }
                    }
                    else
                    {
                        wenti="身份验证失败，请检查账号是否过期";
                        nei="\nbuf值："+byte2hex(buf);
                        handler.sendEmptyMessage(0);
                        sock.Tclose();
                        handler.sendEmptyMessage(5);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        handler.sendEmptyMessage(6);
                        to=false;
                    }
                }
                else
                {
                    wenti="buf[1]:不等于0x02";
                    nei="\nbuf值："+byte2hex(buf);
                    handler.sendEmptyMessage(0);
                    sock.Tclose();
                    handler.sendEmptyMessage(5);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.sendEmptyMessage(6);
                    to=false;
                }
            }
        }).start();
        return 0;
    }

    private void udp_agent(byte[] buf)
    {
        udpip_agent=String.format("%d.%d.%d.%d",hex_udec(buf[4]),hex_udec(buf[5]),hex_udec(buf[6]),hex_udec(buf[7]));
        udpport_agent=(hex_udec(buf[8])<<8)+(hex_udec(buf[9]));
        wenti="代理ip："+udpip_agent;
        nei="\n"+String.format("代理端口：%d",udpport_agent);
        handler.sendEmptyMessage(0);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if(sock.Udpinit(udpip_agent,udpport_agent,udpip,udpport)==0)
                {
                    handler.sendEmptyMessage(3);
                    sock.Uclose();
                    handler.sendEmptyMessage(5);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.sendEmptyMessage(6);
                    return;
                }
                handler.sendEmptyMessage(4);
//                System.setProperty("proxyType", "4");
//                System.setProperty("proxyPort", String.valueOf(udpport_agent));
//                System.setProperty("proxyHost", "121.40.209.60");
//                System.setProperty("proxySet", "true");
                String cmd=String.format("settings put global socks_proxy 121.40.209.60:%d",udpport_agent);
                shellExec(cmd);
                while(to)
                {
                    byte[] buffer=new byte[30];       //内容
                    int ipv4=getIPV4(muip);
                    buffer[0]=0x00;
                    buffer[1]=0x00;
                    buffer[2]=0x00;
                    buffer[3]=0x01;
                    buffer[4]=(byte) (ipv4>>24);
                    buffer[5]=(byte) (ipv4>>16);
                    buffer[6]=(byte) (ipv4>>8);
                    buffer[7]=(byte) (ipv4&0x000000ff);
                    buffer[8]=(byte) (muport>>8);
                    buffer[9]=(byte) (muport&0x00ff);
                    String data="hello,world";
                    buffer=memcpys(buffer,data,10,data.length());
                    //wenti="UDP代理发送信息:";
                    //nei="\nbuffer值："+byte2hex(buffer);
                    //handler.sendEmptyMessage(0);

                    //"121.40.209.60"：代理服务器此为公网-如局域网的话就使用udpip_agent  udpport_agent：代理端口
                    /*
                    if(sock.Usendbyte(buffer,"121.40.209.60",udpport_agent)==0)
                    {
                        handler.sendEmptyMessage(7);
                        Log.d("Usend：","发送失败");
                    }
                    else
                    {
                        Log.d("Usend：","发送成功"+udpip_agent+":"+String.valueOf(udpport_agent));
                    }*/
                    wenti="UDP代理接收信息:";
                    nei="\n"+sock.Urecv();
                    handler.sendEmptyMessage(0);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();
    }

    private int hex_udec(byte buf)
    {
        int code=0;
        code=((buf&0b11110000)>>4);
        code=code*16+(buf&0b00001111);
        return code;
    }

    private byte[] memcpys(byte[] buf,String puf,int qi,int size)
    {
        byte[] p=puf.getBytes();
        for(int t=qi;t<qi+size;t++)
        {
            buf[t]=p[t-qi];
        }
        return buf;
    }

    public static final String byte2hex(byte b[]) {
        if (b == null) {
            throw new IllegalArgumentException(
                    "Argument b ( byte array ) is null! ");
        }
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xff);
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs.toUpperCase();
    }

    private final Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 0:
                    Toast.makeText(getActivity(), wenti+nei, Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Toast.makeText(getActivity(), wenti+nei, Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(getActivity(),"Tcp初始化失败", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(getActivity(),"Udp初始化失败", Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    UdpClick.setText("成功");
                    break;
                case 5:
                    UdpClick.setText("失败");
                    break;
                case 6:
                    UdpClick.setText("代理");
                    break;
                case 7:
                    Toast.makeText(getActivity(),"发送失败",Toast.LENGTH_SHORT).show();
                default :

                    break;
            }
        }

    };

    public static int getIPV4(String s){
        int error = 0;
        if(s==null || s.length()==0 || !s.contains(".")){
            return error;
        }
        String[] subs = s.split("\\.");
        //不是4段
        if(subs.length!=4){
            return error;
        }
        int result = 0;
        for(int i=subs.length-1;i>=0;i--){
            int sub = Integer.parseInt(subs[i].replaceAll(" ",""));
            if(sub>255){
                return error;
            }
            if(i == 2){
                sub = sub<<8;
            }else if(i ==1){
                sub = sub<<16;
            }else if(i == 0){
                sub = sub << 24;
            }
            result +=sub;
        }

        return result;

    }

    public static StringBuffer shellExec(String cmd) {
        Runtime mRuntime = Runtime.getRuntime(); //执行命令的方法
        try {
            //Process中封装了返回的结果和执行错误的结果
            Process mProcess = mRuntime.exec(cmd); //加入参数
            //使用BufferReader缓冲各个字符，实现高效读取
            //InputStreamReader将执行命令后得到的字节流数据转化为字符流
            //mProcess.getInputStream()获取命令执行后的的字节流结果
            BufferedReader mReader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
            //实例化一个字符缓冲区
            StringBuffer mRespBuff = new StringBuffer();
            //实例化并初始化一个大小为1024的字符缓冲区，char类型
            char[] buff = new char[1024];
            int ch = 0;
            //read()方法读取内容到buff缓冲区中，大小为buff的大小，返回一个整型值，即内容的长度
            //如果长度不为null
            while ((ch = mReader.read(buff)) != -1) {
                //就将缓冲区buff的内容填进字符缓冲区
                mRespBuff.append(buff, 0, ch);
            }
            //结束缓冲
            mReader.close();
            Log.i("shell", "shellExec: " + mRespBuff);
            //弹出结果
            Log.i("shell", "执行命令: " + cmd + "执行成功");
            return mRespBuff;

        } catch (IOException e) {
            // 异常处理
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

}
