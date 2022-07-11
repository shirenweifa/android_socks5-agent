package com.example.udptcp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class minterface extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    socketinit sock;
    private DrawerLayout mDrawerLayout;
    private ImageButton btn;
    private Button shuaxin,chongzhi;
    private String gethttp;
    private TextView date,times;
    private String wenti;
    private String urlbuf;
    SharedPreferences Data;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mface);
        Frement(savedInstanceState);
        init();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDrawerLayout();
            }
        });
        ce_gongneng();
    }

    private void init()
    {
        mDrawerLayout = findViewById(R.id.dl_left);
        btn=findViewById(R.id.ci);
        shuaxin=mDrawerLayout.findViewById(R.id.shuaxin);
        chongzhi=mDrawerLayout.findViewById(R.id.chongzhi);
        date=mDrawerLayout.findViewById(R.id.date);
        times=mDrawerLayout.findViewById(R.id.times);
        wenti=new String();
        sock=new socketinit();

        Data=getSharedPreferences("key", Activity.MODE_PRIVATE);
        editor=Data.edit();

        String time=getIntent().getStringExtra("times");
        date.setText(time.substring(0,11));
        times.setText(time.substring(12));
    }
    //侧推栏效果
    private void showDrawerLayout() {
        if (!mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.openDrawer(GravityCompat.START);

        } else {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void ce_gongneng()
    {

        shuaxin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String data=String.format("http://121.43.42.206:1081/check?user=%s&&pass=%s",Data.getString("user",""),Data.getString("password",""));
                        getUrl(data,true);
                    }
                }).start();
            }
        });
        chongzhi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String data=String.format("http://121.40.209.60:1081/reset?user=%s&&pass=%s",Data.getString("user",""),Data.getString("password",""));
                        getUrl(data,false);
                    }
                }).start();
            }
        });
    }

    private void getUrl(String data,boolean t)
    {
        urlbuf=data;
        try {
            URL url = new URL(urlbuf);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            //设置请求方式 GET / POST 一定要大小
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.setDoOutput(false);
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code" + responseCode);
            }
            wenti = getStringByStream(connection.getInputStream());
            if (wenti == null) {
                Log.d("Fail", "失败了");
            }else{
                Log.d("succuss", "成功了 ");
            }
            if(t) {
                handler.sendEmptyMessage(4);
                handler.sendEmptyMessage(5);
            }
            else
            {
                handler.sendEmptyMessage(6);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if(t)
            {
                handler.sendEmptyMessage(7);
            }
            else
            {
                handler.sendEmptyMessage(8);
            }
        }
    }

    private String getStringByStream(InputStream inputStream){
        Reader reader;
        try {
            reader=new InputStreamReader(inputStream,"UTF-8");
            char[] rawBuffer=new char[512];
            StringBuffer buffer=new StringBuffer();
            int length;
            while ((length=reader.read(rawBuffer))!=-1){
                buffer.append(rawBuffer,0,length);
            }
            return buffer.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    protected void Frement(Bundle savedInstanceState)
    {
        bottomNavigationView=findViewById(R.id.bottomNav);
        if(savedInstanceState==null)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,new udpface()).commit();
        }
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment=null;
                switch (item.getItemId())
                {
                    case R.id.udp:
                        fragment=new udpface();
                        break;
                    case R.id.tcp:
                        fragment=new tcpface();
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,fragment).commit();
                return true;
            }
        });
    }


    private final Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 0:
                    Toast.makeText(minterface.this, wenti, Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Toast.makeText(minterface.this, wenti, Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(minterface.this,"Tcp初始化失败", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(minterface.this,"Udp初始化失败", Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    date.setText(wenti.substring(0,11));
                    times.setText(wenti.substring(12));
                    break;
                case 5:
                    Toast.makeText(minterface.this,"刷新成功",Toast.LENGTH_SHORT).show();
                    break;
                case 6:
                    Toast.makeText(minterface.this,"重置成功",Toast.LENGTH_SHORT).show();
                    break;
                case 7:
                    Toast.makeText(minterface.this,"刷新失败",Toast.LENGTH_SHORT).show();
                    break;
                case 8:
                    Toast.makeText(minterface.this,"重置失败",Toast.LENGTH_SHORT).show();
                    break;
                default :

                    break;
            }
        }

    };
}