package com.example.war;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
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
import java.io.PrintStream;
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Save=(Button)findViewById(R.id.Save);
        Save.setClickable(false);
        Stop=(Button)findViewById(R.id.Stop);
        Stop.setClickable(false);
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
                Toast.makeText(getApplicationContext(),"Start to Listen",Toast.LENGTH_LONG).show();
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
                                                      Stop.setClickable(true);
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

        Stop.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                try {
                    Client_Socket.close();
                    Server_Socket.close();
                    State.setChecked(false);
                    Save.setClickable(true);
                    CButton.setClickable(true);
                }catch (Exception e)
                {
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });

        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String filename=FileName.getText().toString();
                if(filename.isEmpty())
                {
                    Toast.makeText(getApplicationContext(),"Enter a valid File Name",Toast.LENGTH_LONG).show();
                    return;
                }
                Intent exploer=new Intent(Intent.ACTION_GET_CONTENT);
                exploer.addCategory(Intent.CATEGORY_OPENABLE);
                startActivity(exploer);
                File datafile=new File(exploer.getCategories().toString(),filename+".txt");
                PrintStream fout=null;
                try {
                    fout=new PrintStream(datafile);
                    for(int i=0;i<DataArray.size();i++)
                    {
                        fout.println(Integer.toString(i)+"s  "+DataArray.get(i).toString()+"Hz");
                    }
                    fout.close();
                    Toast.makeText(getApplicationContext(),"File saved!",Toast.LENGTH_LONG).show();

                    DataArray.clear();
                    DataNow=0;
                    linedataset.clear();
                    linedata.notifyDataChanged();
                    Chart.notifyDataSetChanged();
                    ChartPresent.notifyDataSetChanged();
                    Chart.invalidate();
                    ChartPresent.invalidate();

                }
                catch (Exception e)
                {
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}