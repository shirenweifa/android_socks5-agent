package com.example.udptcp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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

public class tcpface extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tcpface, container, false);
    }
    Button TcpClick;
    socketinit sock;
    SharedPreferences Data;
    SharedPreferences.Editor editor;

    String user,password;

    String lTcpip;          //目标用户ip
    short lTcpport;         //目标用户端口

    String Tcpip_agent;     //代理服务器
    int    Tcpport_agent;   //代理端口

    String wenti;
    String nei;
    boolean to=false;
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
        TcpClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TcpClick.getText().equals("代理"))
                {
                    to=false;
                    renzhen();
                }
                else if(TcpClick.getText().equals("成功"))
                {
                    sock.Tclose();
                    handler.sendEmptyMessage(6);
                }
            }
        });
    }

    private void init() {
        TcpClick=getActivity().findViewById(R.id.TcpClick);
        Data= getActivity().getSharedPreferences("key", Activity.MODE_PRIVATE);
        editor=Data.edit();
        user=Data.getString("user","");
        password=Data.getString("password","");

        //初始化udp    udpip:目标ip udpport:目标端口
        lTcpip="39.103.165.21";
        lTcpport=8080;

        sock=new socketinit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(sock.stf!=null)
        {
            sock.Tclose();
            to=false;
            Toast.makeText(getActivity(),"Tcp代理结束",Toast.LENGTH_SHORT).show();
        }
    }

    private void renzhen()
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
                        Thread.sleep(1000);
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
                        Thread.sleep(1000);
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
                        客户端会用通过认证的这个TCP连接发送请求，信令格式如下：
                         +----+-----+-------+------+----------+----------+
                         |VER | CMD |  RSV  | ATYP | DST.ADDR | DST.PORT |
                         +----+-----+-------+------+----------+----------+
                         | 1  |  1  | X'00' |  1   | Variable |    2     |
                         +----+-----+-------+------+----------+----------+

                        client的本地地址即端口,这个很重要，要填客户端想发送/接收Tcp包的本地端口
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
                        int ipv4=getIPV4(lTcpip);
                        buf[0]=0x05;
                        buf[1]=0x01;        //此为Tcp代理
                        buf[2]=0x00;
                        buf[3]=0x01;
                        buf[4]=(byte) (ipv4>>24);
                        buf[5]=(byte) (ipv4>>16);
                        buf[6]=(byte) (ipv4>>8);
                        buf[7]=(byte) (ipv4&0x000000ff);
                        buf[8]=(byte) (lTcpport>>8);
                        buf[9]=(byte) (lTcpport&0x00ff);
                        if(sock.Tsendbyte(buf)==0)
                        {
                            wenti="Tsend:发送Tcp代理失败";
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
                            wenti="温馨提示：成功代理Tcp";
                            nei="\nbuf值："+byte2hex(buf);
                            handler.sendEmptyMessage(0);
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            to=true;
                            //sock.Tclose();
                            Tcp_agent(buf);              //开始进行Tcp代理
                        }
                        else
                        {
                            wenti="温馨提示：未能成功代理Tcp";
                            nei="\nbuf值："+byte2hex(buf);
                            handler.sendEmptyMessage(0);
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            handler.sendEmptyMessage(5);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            sock.Tclose();
                            handler.sendEmptyMessage(6);
                            return;
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
                        return;
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
                    return;
                }
            }
        }).start();
    }

    private void Tcp_agent(byte[] buf)
    {
        Tcpip_agent=String.format("%d.%d.%d.%d",hex_udec(buf[4]),hex_udec(buf[5]),hex_udec(buf[6]),hex_udec(buf[7]));
        Tcpport_agent=(hex_udec(buf[8])<<8)+(hex_udec(buf[9]));
        wenti="代理ip："+Tcpip_agent;
        nei="\n"+String.format("代理端口：%d",Tcpport_agent);
        handler.sendEmptyMessage(0);
        new Thread(new Runnable() {
            @Override
            public void run() {
                /*
                if(sock.Tcpinit("121.40.209.60",Tcpport_agent)==0)
                {
                    Log.d("Tcp_agent","Tcpclick:失败");
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
                handler.sendEmptyMessage(4);
                Log.d("Tcp_agent","Tcpclick:成功");
                while(to)
                {
                    //String buffer="nihaoya";
                    byte[] buffer=new byte[256];
                    buffer[0]=0x55;
                    buffer[1]=0x77;
                    buffer[2]=0x78;
                    sock.Tsendbyte(buffer);
                    sock.Trecv();
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                 */
                handler.sendEmptyMessage(4);
                while(to) {
                    sock.Tsendstr("123456");
                    wenti="Tcp代理发送信息:";
                    nei="\n"+"123456";
                    handler.sendEmptyMessage(0);
                    wenti="Tcp代理接收信息:";
                    nei="\n"+sock.Trecv();
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

    private byte[] memcpys(byte[] buf,String puf,int qi,int size)
    {
        byte[] p=puf.getBytes();
        for(int t=qi;t<qi+size;t++)
        {
            buf[t]=p[t-qi];
        }
        return buf;
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
                    TcpClick.setText("成功");
                    break;
                case 5:
                    TcpClick.setText("失败");
                    break;
                case 6:
                    TcpClick.setText("代理");
                    break;
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

}
