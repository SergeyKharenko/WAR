package com.example.war;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;


public class MainActivity extends AppCompatActivity {

    private Button CButton;
    private Switch State;
    private TextView Port,CMDLine;

    private int port;

    private ServerSocket Server_Socket;
    private Socket Client_Socket;

    public int DataNow;
    public Vector DataArray;

    /*private boolean IpCheck(TextView ipcontainer)
    {
        String cache=ipcontainer.getText().toString();
        int ip1=0,ip2=0,ip3=0,ip4=0;
        int count=0;
        for(int i=0;i<cache.length();i++)
        {
            if(cache.charAt(i)=='.')
            {
                count++;
            }
            else {
                switch (count) {
                    case 0: {
                        ip1 *= 10;
                        ip1 += (int)cache.charAt(i) -(int) '0';
                        break;
                    }
                    case 1: {
                        ip2 *= 10;
                        ip2 += (int)cache.charAt(i) - (int)'0';
                        break;
                    }
                    case 2: {
                        ip3 *= 10;
                        ip3 += (int)cache.charAt(i) - (int)'0';
                        break;
                    }
                    case 3: {
                        ip4 *= 10;
                        ip4 += (int)cache.charAt(i) -(int) '0';
                        break;
                    }
                    default:
                        return false;
                }
            }
        }
        if(count!=3)
            return false;
        if(ip1>255||ip2>255||ip3>255||ip4>255)
            return false;
        ipaddr=cache;
        return true;
    }
    */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CButton=(Button)findViewById(R.id.Confirm);
        Port=(TextView)findViewById(R.id.Port);
        State=(Switch)findViewById(R.id.ConnectionState);
        CMDLine=(TextView)findViewById(R.id.CMDLine);

        DataNow=0;

        CButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String cache=Port.getText().toString();
                if(cache.isEmpty())
                {
                    Toast.makeText(getApplicationContext(),"Enter a Port",Toast.LENGTH_SHORT).show();
                    return;
                }
                port=Integer.parseInt(cache);
                try {
                    Thread TCPServer=new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                runOnUiThread(new Runnable() {
                                                  @Override
                                                  public void run() {
                                                      CButton.setClickable(false);
                                                  }
                                              });

                                Server_Socket=new ServerSocket(port);
                                Client_Socket=null;

                                Client_Socket=Server_Socket.accept();
                                Client_Socket.setPerformancePreferences(0,1,0);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        State.setChecked(true);
                                    }
                                });
                                BufferedReader Bin=new BufferedReader(new InputStreamReader(Client_Socket.getInputStream()));
                                while (true) {
                                    final String Cache = Bin.readLine();
                                    /*if (cache.isEmpty()) {
                                        Bin.close();
                                        Client_Socket.close();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                CButton.setClickable(true);
                                                State.setChecked(false);
                                            }
                                        });
                                        return;
                                    }*/
                                    if(Cache!=null) {
                                        if (!Cache.isEmpty()) {
                                            DataNow = Integer.parseInt(Cache);
                                            //DataArray.addElement(DataNow);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    CMDLine.append(Cache);
                                                    CMDLine.append("\n");
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                            catch(final Exception ex)
                            {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        CButton.setClickable(true);
                                        State.setChecked(false);
                                        Toast.makeText(getApplicationContext(),ex.getMessage(),Toast.LENGTH_LONG).show();
                                    }
                                });
                                return;
                            }
                        }
                    });

                    TCPServer.start();
                }
                catch (Exception e)
                {
                    String temp=e.getMessage();
                    System.out.println(temp);
                    return;
                }

            }
        });
    }
}