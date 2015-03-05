package com.alchemy.facebookFanPost;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import com.alchemy.facebookFanPost.elasticsearchConnect;


public class runfan {

	public static void main(String[] args)  {
//		String fanidString = "189105304512715";
		//FAN Id
		String fanidString = "232633627068";
//		String fanidString = args[0];
		
		//Crawler FB fan Data
		int setTime = -5;
//		int setTime = -Integer.valueOf(args[1]);
		
		// sleep 
//		int sleeptime = Integer.valueOf(args[2]);
		int sleeptime = 30;
		
		fanNewsPagePost fbNews = new fanNewsPagePost();
		while (true) {
			String run =fbNews.fanNewsPagePostGetMysqldb(fanidString,setTime);
			if (run == "datefalse"){
				break;
			}
			try {
				System.out.println("sleep............");
				//stop 30 Minute
				Thread.sleep(1000*sleeptime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

}
