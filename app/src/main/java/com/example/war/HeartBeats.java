package com.example.war;

import android.app.Application;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.Socket;


public class HeartBeats  implements Runnable{

    final long HeartBeatsTime=5000; //5s per trans
    private Socket ClientSocket;
    private OutputStreamWriter OW;
    public HeartBeats(Socket clientsocket)
    {
        ClientSocket=clientsocket;
        try {
            OW=new OutputStreamWriter(clientsocket.getOutputStream(),"UTF-8");
        }
        catch (Exception e)
        {
        }
    }
    @Override
    public void run() {
        while(true) {
            try {
                OW.write("#");
                wait(HeartBeatsTime);
            }
            catch (Exception e)
            {
                try
                {
                    ClientSocket.close();
                }
                catch(Exception ex) {
                }
                return;
            }
        }

    }
}
