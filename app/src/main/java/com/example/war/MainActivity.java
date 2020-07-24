package com.example.war;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TableLayout;
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
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;


public class MainActivity extends AppCompatActivity {

    private Button CButton,Stop,Save;
    private Switch State;
    private TextView Port,CMDLine,PresentData,FileName;
    private LineChart Chart,ChartPresent;

    private int port;

    private ServerSocket Server_Socket;
    private Socket Client_Socket;

    public int DataNow;
    public Vector DataArray;
    private LineDataSet linedataset;
    private LineData linedata;
    private String filename;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Save=(Button)findViewById(R.id.Save);
        Save.setEnabled(false);
        Stop=(Button)findViewById(R.id.Stop);
        Stop.setEnabled(false);
        CButton=(Button)findViewById(R.id.Confirm);
        Port=(TextView)findViewById(R.id.Port);
        State=(Switch)findViewById(R.id.ConnectionState);
        CMDLine=(TextView)findViewById(R.id.CMDLine);
        CMDLine.setKeyListener(null);
        FileName=(TextView)findViewById(R.id.FileName);
        PresentData=(TextView)findViewById(R.id.PresentData);
        PresentData.setKeyListener(null);
        Chart=(LineChart)findViewById(R.id.Chart);
        ChartPresent=(LineChart)findViewById(R.id.ChartPresent);

        DataNow=0;
        DataArray=new Vector();

        Chart.setNoDataText("Data Real Time");
        Chart.setTouchEnabled(true);
        Chart.setBackgroundColor(Color.WHITE);
        Chart.setScaleEnabled(true);
        Chart.setDrawGridBackground(true);
        Description des1=new Description();
        des1.setText("Global");
        Chart.setDescription(des1);

        ChartPresent.setNoDataText("Data Real Time");
        ChartPresent.setTouchEnabled(false);
        ChartPresent.setBackgroundColor(Color.WHITE);
        ChartPresent.setScaleEnabled(false);
        Description des2=new Description();
        des2.setText("Runtime");
        ChartPresent.setDescription(des2);
        ChartPresent.setDrawGridBackground(true);
        ChartPresent.setVisibleXRangeMaximum(20);

        linedataset=new LineDataSet(null,"Frequency");
        linedataset.setColor(Color.rgb(255,0,255));
        linedataset.setCircleColor(Color.RED);
        linedataset.setLineWidth(1f);
        linedataset.setDrawValues(true);
        linedataset.setAxisDependency(YAxis.AxisDependency.LEFT);

        linedata=new LineData(linedataset);
        linedata.setValueTextColor(Color.BLUE);
        Chart.setData(linedata);
        ChartPresent.setData(linedata);

        Legend Flegend=Chart.getLegend();
        Flegend.setForm(Legend.LegendForm.CIRCLE);
        Flegend.setTextColor(Color.BLUE);

        Flegend=ChartPresent.getLegend();
        Flegend.setForm(Legend.LegendForm.CIRCLE);
        Flegend.setTextColor(Color.BLUE);

        XAxis xl = Chart.getXAxis();
        xl.setTextColor(Color.BLUE);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setAxisMinimum(0f);

        YAxis yl = Chart.getAxisLeft();
        yl.setValueFormatter(new IndexAxisValueFormatter());
        yl.setTextColor(Color.BLUE);
        yl.setDrawGridLines(true);
        yl.setAxisMinimum(0);

        YAxis rightAxis = Chart.getAxisRight();
        rightAxis.setEnabled(false);

        xl = ChartPresent.getXAxis();
        xl.setTextColor(Color.BLUE);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setAxisMinimum(0f);

        yl = ChartPresent.getAxisLeft();
        yl.setTextColor(Color.BLUE);
        yl.setDrawGridLines(true);
        yl.setAxisMinimum(0);

        rightAxis = ChartPresent.getAxisRight();
        rightAxis.setEnabled(false);

        CButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"Start to Listen",Toast.LENGTH_SHORT).show();
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
                                                      CButton.setEnabled(false);
                                                      Stop.setEnabled(true);
                                                      Save.setEnabled(false);
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
                                            DataArray.addElement(DataNow);
                                            linedataset.addEntry(new Entry(linedataset.getEntryCount(),DataNow));
                                            linedata.notifyDataChanged();
                                            Chart.notifyDataSetChanged();
                                            ChartPresent.notifyDataSetChanged();

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        PresentData.setText(Cache);
                                                        CMDLine.append(Cache);
                                                        CMDLine.append("\n");

                                                        int count=linedataset.getEntryCount();
                                                        Chart.invalidate();
                                                        if(count>20)
                                                        {
                                                            ChartPresent.setVisibleXRangeMaximum(20);
                                                            ChartPresent.moveViewToX(count-20);
                                                        }
                                                        else
                                                            ChartPresent.invalidate();
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
                                        CButton.setEnabled(true);
                                        Save.setEnabled(true);
                                        State.setEnabled(false);
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

        Stop.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                try {
                    Client_Socket.close();
                    Server_Socket.close();
                    State.setChecked(false);
                    Save.setEnabled(true);
                    CButton.setEnabled(true);
                    Stop.setEnabled(false);

                    DataArray.clear();
                    DataNow=0;
                    linedataset.clear();
                    linedata.notifyDataChanged();
                    Chart.notifyDataSetChanged();
                    ChartPresent.notifyDataSetChanged();
                    Chart.invalidate();
                    ChartPresent.moveViewToX(0);
                    ChartPresent.invalidate();

                }catch (Exception e)
                {
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });

        Save.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onClick(View view) {
                filename=FileName.getText().toString();

                if(filename.isEmpty())
                {
                    Toast.makeText(getApplicationContext(),"Enter a valid File Name",Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    String GlobalFileName=filename+".csv";
                    File file=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),GlobalFileName);
                    if(!file.exists())
                        file.createNewFile();
                    else
                    {
                        Toast.makeText(getApplicationContext(),"File Exists!",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    FileOutputStream fout=new FileOutputStream(file);
                    OutputStreamWriter ow= new OutputStreamWriter(fout,"UTF-8");
                    ow.write("Time /s,Frequency /Hz");
                    for(int i=0;i<DataArray.size();i++)
                    {
                        ow.write(Integer.toString((i))+","+DataArray.get(i).toString()+"\n");
                    }
                    ow.close();
                    Toast.makeText(getApplicationContext(),"File saved in "+Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/"+GlobalFileName,Toast.LENGTH_LONG).show();

                }
                catch (Exception e)
                {
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}