package com.example.war;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;


public class MainActivity extends AppCompatActivity {

    private Button CButton;
    private Switch State;
    private TextView Port,CMDLine,PresentData;
    private LineChart Chart;

    private int port;

    private ServerSocket Server_Socket;
    private Socket Client_Socket;

    public int DataNow;
    public Vector DataArray;
    private LineDataSet linedataset;
    private LineData linedata;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        CButton=(Button)findViewById(R.id.Confirm);
        Port=(TextView)findViewById(R.id.Port);
        State=(Switch)findViewById(R.id.ConnectionState);
        CMDLine=(TextView)findViewById(R.id.CMDLine);
        CMDLine.setKeyListener(null);
        PresentData=(TextView)findViewById(R.id.PresentData);
        PresentData.setKeyListener(null);
        Chart=(LineChart)findViewById(R.id.Chart);

        DataNow=0;
        DataArray=new Vector();

        Chart.setNoDataText("Data Real Time");
        Chart.setTouchEnabled(true);
        Chart.setBackgroundColor(Color.WHITE);
        Chart.setScaleEnabled(true);
        Chart.setDrawGridBackground(true);
        Chart.setVisibleXRangeMaximum(20f);

        linedataset=new LineDataSet(null,"Frequency");
        linedataset.setColor(Color.CYAN);
        linedataset.setCircleColor(Color.GREEN);
        //linedataset.setCircleSize();
        linedataset.setLineWidth(3f);
        linedataset.setDrawValues(true);
        linedataset.setAxisDependency(YAxis.AxisDependency.LEFT);

        linedata=new LineData(linedataset);
        linedata.setValueTextColor(Color.BLUE);
        Chart.setData(linedata);
        Legend Flegend=Chart.getLegend();
        Flegend.setForm(Legend.LegendForm.CIRCLE);
        Flegend.setTextColor(Color.BLUE);

        XAxis xl = Chart.getXAxis();
        xl.setTextColor(Color.BLUE);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setAxisMinimum(0f);

        YAxis yl = Chart.getAxisLeft();
        yl.setTextColor(Color.BLUE);
        yl.setDrawGridLines(true);
        yl.setAxisMinimum(0);

        YAxis rightAxis = Chart.getAxisRight();
        rightAxis.setEnabled(false);


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
                                BufferedReader Bin=new BufferedReader(new InputStreamReader(Client_Socket.getInputStream(),"UTF-8"));
                                while (true) {
                                    final String Cache = Bin.readLine();
                                    if(Cache!=null) {
                                        if (!Cache.isEmpty()) {
                                            DataNow = Integer.parseInt(Cache);
                                            //if(DataArray==null)
                                            {
                                                //DataArray.add(new Entry(0,DataNow));
                                                //linedataset.addEntry(new Entry(0,DataNow));
                                            }
                                            //else {
                                                //DataArray.add(new Entry(DataArray.size(), DataNow));
                                            //}
                                            //linedataset.addEntry(new Entry(linedataset.getXMax(),DataNow));
                                            DataArray.addElement(DataNow);
                                            linedataset.addEntry(new Entry(linedataset.getEntryCount(),DataNow));
                                            linedata.notifyDataChanged();
                                            Chart.notifyDataSetChanged();



                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        PresentData.setText(Cache);
                                                        CMDLine.append(Cache);
                                                        CMDLine.append("\n");

                                                        int count=linedataset.getEntryCount();
                                                        if(count>20)
                                                            Chart.moveViewToX(count);
                                                        else
                                                            Chart.invalidate();
                                                        //Toast.makeText(getApplicationContext(),Integer.toString(linedataset.getEntryCount()-20),Toast.LENGTH_LONG).show();
                                                        //Chart.invalidate();
                                                        //Chart.notifyDataSetChanged();
                                                        //Chart.invalidate();
                                                    }
                                                    catch(Exception E)
                                                    {
                                                        Toast.makeText(getApplicationContext(),E.getMessage(),Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                        }
                                    }

                                }
                            }
                            catch(final Exception ex)
                            {
                                try {
                                    Client_Socket.close();
                                    Server_Socket.close();
                                }
                                catch (Exception e)
                                {
                                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                                }

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