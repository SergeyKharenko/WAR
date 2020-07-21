package com.example.war;

public class PriThread implements Runnable{
	
	public String ThreadName;
	public PriThread(String threadname) {
		ThreadName=threadname;
	}
	
	
	public void run()
	{
		for(int i=0;i<5;i++)
		{
			System.out.println(ThreadName+":"+Integer.toString(i));
		}
	}
	
}
