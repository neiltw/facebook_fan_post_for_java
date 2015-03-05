package com.alchemy.facebookFanPost;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

public class fanNewsPageNext {

	
	public String nextpage(String data,int setTime){
		JSONObject dataPage= new JSONObject();
		try {
			dataPage = new JSONObject(data);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (true) {
			try {
				
				JSONObject commentPage = new JSONObject(readUrlPagneFan(dataPage.getString("commentsPageNext")));
				JSONObject fanPage = new JSONObject(readUrlPagneFan(dataPage.getString("fanFirstNextPage")));
				JSONObject jsGetFanData = new JSONObject();
				String successdate = null;
				try {
					
					dataPage.put("commentsPageNext", commentPage.getJSONObject("paging").getString("next"));
					dataPage.put("fanFirstNextPage", fanPage.getJSONObject("paging").getString("next"));
				} catch (Exception e) {
					// TODO: handle exception
					dataPage.put("commentsPageNext","");
					dataPage.put("fanFirstNextPage","");
					break;
				}
				try {
					//checkfbtime
					if (fanPage.getJSONArray("data").isNull(0) == false){
						JSONObject checkdate = new JSONObject();
						checkdate.put("check", fanPage.getJSONArray("data").get(0));
						successdate =checkdatetime(checkdate.getJSONObject("check").get("created_time").toString(),setTime);
//						System.out.println(successdate);
					}else if (fanPage.getJSONArray("data").isNull(0)) {
						break;
					}
					if (successdate=="false"){
						break;
					}	
					System.out.println("===2===getfandata========");
					//getfandata
					if (fanPage.getJSONArray("data").isNull(0) == false){
						jsGetFanData.put("fandata", fanPage.getJSONArray("data").get(0));
						JSONObject getlike = new JSONObject(fanPage.getJSONArray("data").get(0).toString());
						try {
							if (getlike.getJSONObject("likes").getJSONArray("data").isNull(0)==false){
								jsGetFanData.put("likes", getlike.getJSONObject("likes"));
//								System.out.println("===2===like========");
							}
						} catch (Exception e) {
							// TODO: handle exception
							jsGetFanData.put("likes","false");
						}
					}
					System.out.println("===2===getCommentsdata========");
					//getCommentsdata
					if (commentPage.getJSONArray("data").isNull(0) == false){
//						System.out.println("==2====jscomments_data========");
						jsGetFanData.put("comments", commentPage.getJSONArray("data").get(0));
					}else {
						jsGetFanData.put("comments","false");
//						System.out.println("===2===jscomments_data==null======");
					}
									
				} catch (Exception e) {
					// TODO: handle exception
//					System.out.println("fan data null");
				}
				System.out.println("===2===getfristlike========");
				//getfristlike
				int likeCountString = 0 ;
				try{
					if (jsGetFanData.getJSONObject("likes").getJSONArray("data").isNull(0) ==false){
						fanLikePage fanlike = new fanLikePage();
						likeCountString =Integer.parseInt(fanlike.getlikedata(jsGetFanData.toString()));
//						System.out.println("like data for jsGetFanData");
					}
				}catch (Exception e) {
					// TODO: handle exception
//					System.out.println("Not likes data for null");
				}
				System.out.println("===2===getfristcomments========");
				//getfristcomments
				int commentsCountString = 0 ;
				try{
					if (jsGetFanData.getJSONObject("comments") !=null){
						fanCommentspage fancomm = new fanCommentspage();
						commentsCountString = Integer.parseInt(fancomm.fanCommentsfrist(jsGetFanData.toString()));
					}
				}catch (Exception e) {
					// TODO: handle exception
//					System.out.println("Not comments data for null");
				}
				fanNewsPagePost fanpost = new fanNewsPagePost();
				fanpost.fanData(jsGetFanData.toString(), commentsCountString, likeCountString);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
				break;
			}
		}
		return "true";
	}
	

	
	public String readUrlPagneFan(String url){
		JSONObject getPage = null;
		try {
			URL fanNext = new URL(url);
			HttpURLConnection fanConn = (HttpURLConnection) fanNext.openConnection();
			InputStream checkFan;
			fanConn.setConnectTimeout(5000);
			fanConn.setReadTimeout(5000);
			if (fanConn.getResponseCode() >= 400) {
				checkFan = fanConn.getErrorStream();
//				return getPage.toString();
				System.out.println(checkFan);
				Thread.sleep(100);
				wait(5);
				readUrlPagneFan(url);
			} else {
			    BufferedReader reader = new BufferedReader(new InputStreamReader(fanConn.getInputStream(),"UTF-8"));
			    String line = "";
			    while((line = reader.readLine()) != null) {
			    	getPage = new JSONObject(line);
			    }
			    reader.close(); 
			}
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("===============fail==============");
			readUrlPagneFan(url);
		}
		
		return getPage.toString();
		
	}
	
	
	public  String checkdatetime(String fbtime,int setTime){
		try {
			java.util.Date date = null;
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, setTime);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date d1 = java.sql.Date.valueOf(sdf.format(cal.getTime()));
//			System.out.println(d1);
			String dateString = fbtime;
			date = sdf.parse(dateString);
			Date d2 = java.sql.Date.valueOf(sdf.format(date));
//			System.out.println(d2);
			if (d1.getTime()<=d2.getTime()){
				return "true";
			}else{
				return "false";
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "false";
		}
	}
	
	
	
	
	
	
	
}
