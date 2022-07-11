package com.example.udptcp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    EditText user,pass;
    CheckBox record;
    socketinit sock;
    ImageButton denglv;
    SharedPreferences Data;
    SharedPreferences.Editor editor;
    private String urlbuf;
    private String wenti;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        getData();
        denglv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(record.isChecked())
                {
                    editor.putString("user",user.getText().toString());
                    editor.putString("password",pass.getText().toString());
                    editor.putBoolean("check",record.isChecked());
                    editor.commit();
                }
                else
                {
                    editor.clear();
                    editor.commit();
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getUrl();
                    }
                }).start();

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(wenti.length()>12)
                {
                    Intent intent=new Intent(MainActivity.this,minterface.class);
                    Bundle bundle=new Bundle();
                    bundle.putString("times",wenti);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                else if(wenti.equals("用户不存在"))
                {
                    Toast.makeText(MainActivity.this,"用户不存在",Toast.LENGTH_SHORT).show();
                }
                else if(wenti.equals("密码错误"))
                {
                    Toast.makeText(MainActivity.this,"密码错误",Toast.LENGTH_SHORT).show();
                }
                // Intent intent=new Intent(MainActivity.this,minterface.class);
                //startActivity(intent);
            }
        });
    }

    protected void init()
    {
        user=findViewById(R.id.user);
        pass=findViewById(R.id.pass);
        record=findViewById(R.id.record);
        denglv=findViewById(R.id.denglv);
        Data=getSharedPreferences("key", Activity.MODE_PRIVATE);
        editor=Data.edit();


    }

    protected void getData()
    {
        if(Data.getString("user","").equals("")&&Data.getString("password","").equals(""))
        {

        }
        else
        {
            user.setText(Data.getString("user",""));
            pass.setText(Data.getString("password",""));
            record.setChecked(Data.getBoolean("check",false));
        }
    }

    private void getUrl()
    {
        urlbuf=String.format("http://121.40.209.60:1081/check?user=%s&&pass=%s",Data.getString("user",""),Data.getString("password",""));
        try {
            URL url = new URL(urlbuf);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            //设置请求方式 GET / POST 一定要大小
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.setDoOutput(false);
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

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("http","失败");
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
}