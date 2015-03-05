package com.alchemy.facebookFanPost;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;


public class fanNewsPagePost {


	public String fanNewsPagePostGetMysqldb (String fanid,int setTime)  {
		mysqlConnect dbConnect = new mysqlConnect();
		ResultSet dbdata = dbConnect.SelectTable();
		String firstdata = "";
	    try {
			while(dbdata.next()) 
			  { 
//				String token = "CAAKL9ZAGJXRUBAORztkxqpqGtQN83GcciEN9PZAYJqQzqBMG2YF6kS9HFcZCKYZBx5xq6TsEGhExO0AQjKvbbCJBsyLq7tdFDCNp1ZCvDK1xZBY59HVA8CMBlHZBwZBumn3M0hnfAkEqX56a6hDdiJXMSxLoy89zaXZC0zuwDSHlMgoxrwnqmu8bX";

			    String check= checkToken(dbdata.getString("access_token"));
//			    String check= checkToken(token);
			    System.out.println("token:"+check); 
			    
			    if (check =="success"){
			    	// run facebook code
			    	firstdata = Crawlerfbfan(dbdata.getString("access_token"),fanid,setTime);
			    	 System.out.println("firstdata:"+firstdata); 
			    	 elasticsearchConnect es_conn = new elasticsearchConnect();
			    	 es_conn.client.close();
			    	if (firstdata=="fan_data_true"){
			    		System.out.println("es_complete"); 
			    		break;
			    	}
			    	if (firstdata =="true"){
			    		System.out.println("next token"); 			    		
			    	}
			    	if (firstdata =="datefalse"){
			    		System.out.println("Time Error");
			    		return firstdata;
			    	}
			    }
			  }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
		return firstdata ; 
	    

	}
	
	public String  checkToken(String token) throws IOException{
			URL url = new URL("https://graph.facebook.com/me?access_token="+token);
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			InputStream is;
			//判斷url是否錯誤
			if (httpConn.getResponseCode() >= 400) {
			    is = httpConn.getErrorStream();
			    String tokenCheck ="fail"; 
//			    System.out.println("====="+is);
			    return tokenCheck;
			} else {
			    is = httpConn.getInputStream();
//			    System.out.println(is);
			    BufferedReader reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream(),"UTF-8"));
			    String tokenCheck ="success"; 
			    reader.close();
			    return tokenCheck;
			}	
		}
	
	public String Crawlerfbfan(String token, String fanid,int setTime) throws IOException, JSONException{
			//getFanData
			URL fanurl = new URL("https://graph.facebook.com/"+fanid+"/posts?access_token="+token+"&limit=1");
			HttpURLConnection fanConn = (HttpURLConnection) fanurl.openConnection();
			InputStream checkFan;
			JSONObject jsfan = null;
			
			if (fanConn.getResponseCode() >= 400) {
				checkFan = fanConn.getErrorStream();
			    String tokenCheck ="false_fan"; 
			    
			    return tokenCheck;
			} else {
			    BufferedReader reader = new BufferedReader(new InputStreamReader(fanConn.getInputStream(),"UTF-8"));
			    String line = "";
			    while((line = reader.readLine()) != null) {
			    	jsfan = new JSONObject(line);
			    }
			    reader.close();
			}	
			//getCommentsPage
			URL commentsurl = new URL("https://graph.facebook.com/"+fanid+"/posts?access_token="+token+"&fields=comments{comments{from,id,message,parent,user_likes,like_count,message_tags},id,from,message,parent,like_count,message_tags,user_likes}&limit=1");
			HttpURLConnection commentsConn = (HttpURLConnection) commentsurl.openConnection();
			InputStream checkComments;
			JSONObject jsComments = null;
			if (commentsConn.getResponseCode() >= 400) {
				checkComments = fanConn.getErrorStream();
			    String tokenCheck ="false_comments"; 
			    return tokenCheck;
			} else {
			    BufferedReader reader = new BufferedReader(new InputStreamReader(commentsConn.getInputStream(),"UTF-8"));
			    String line = "";
			    while((line = reader.readLine()) != null) {
			    	jsComments = new JSONObject(line);
			    }
			    reader.close();
			}
			String data = Dismantling(jsfan,jsComments,setTime);
			return data;

		}
		

	public String Dismantling(JSONObject jsfan,JSONObject jsComments,int setTime) throws JSONException{
		JSONObject jsGetFanData = new JSONObject();
		String successdate = null;
		//checkfbtime
		if (jsfan.getJSONArray("data").isNull(0) == false){
			JSONObject checkdate = new JSONObject();
			checkdate.put("check", jsfan.getJSONArray("data").get(0));
			successdate =checkdatetime(checkdate.getJSONObject("check").get("created_time").toString(),setTime);
		}else if (jsfan.getJSONArray("data").isNull(0)) {
			return "fanfalse data";
		}
		if (successdate=="false"){
			return "datefalse";
		}
		try {
			System.out.println("======getfandata========");
			//getfandata
			if (jsfan.getJSONArray("data").isNull(0) == false){
				jsGetFanData.put("fandata", jsfan.getJSONArray("data").get(0));
				JSONObject getlike = new JSONObject(jsfan.getJSONArray("data").get(0).toString());
				if (getlike.getJSONObject("likes").getJSONArray("data").isNull(0)==false){
					jsGetFanData.put("likes", getlike.getJSONObject("likes"));
//					System.out.println("======like========");
				}
			}else{
				jsGetFanData.put("likes","false");
			}
			try{
				if (jsfan.getJSONObject("paging").get("next") !=null) {
//					System.out.println("====paging===");
					jsGetFanData.put("fanFirstNextPage", jsfan.getJSONObject("paging").get("next") );
				}
			} catch (Exception e) {
				// TODO: handle exception
				jsGetFanData.put("fanFirstNextPage","false");
//				System.out.println("Not paging data for jsfan");
			}
			System.out.println("======getCommentsdata======");
			//getCommentsdata
			if (jsComments.getJSONArray("data").isNull(0) == false){
//				System.out.println("======jscomments_data========");
				jsGetFanData.put("comments", jsComments.getJSONArray("data").get(0));
			}else {
				jsGetFanData.put("comments","false");
//				System.out.println("======jscomments_data==null======");
			}
			try{
				if (jsComments.getJSONObject("paging").get("next") != null){
//					System.out.println("======jscomments_ paging========");
					jsGetFanData.put("commentsPageNext", jsComments.getJSONObject("paging").get("next"));
				}
			} catch (Exception e) {
				// TODO: handle exception
				jsGetFanData.put("commentsPageNext", "false");
//				System.out.println("======jscomments_ paging==null======");
			}	
		} catch (Exception e) {
			// TODO: handle exception
//			e.printStackTrace();
//			System.out.println("Not paging data for jsfan");
		}

		System.out.println("======getfristlike========");

		//getfristlike
		int likeCountString = 0 ;
		try{
			System.out.println(jsGetFanData.getJSONObject("likes").getJSONArray("data").isNull(0));
			if (jsGetFanData.getJSONObject("likes").getJSONArray("data").isNull(0) ==false){
				fanLikePage fanlike = new fanLikePage();
				likeCountString =Integer.parseInt(fanlike.getlikedata(jsGetFanData.toString()));
//				System.out.println("like data for jsGetFanData");
			}
		}catch (Exception e) {
			// TODO: handle exception
//			System.out.println("Not likes data for null");
		}
		//getfristcomments
		int commentsCountString = 0 ;
		try{
			if (jsGetFanData.getJSONObject("comments") !=null){
				fanCommentspage fancomm = new fanCommentspage();
				commentsCountString = Integer.parseInt(fancomm.fanCommentsfrist(jsGetFanData.toString()));
//				System.out.println("comments data for jsGetFanData");
			}
		}catch (Exception e) {
			// TODO: handle exception
//			System.out.println("Not comments data for null");
		}
		String inputfan =fanData(jsGetFanData.toString(),commentsCountString,likeCountString);
		fanNewsPageNext nextPage = new fanNewsPageNext();
		nextPage.nextpage(jsGetFanData.toString(),setTime);
		
		return inputfan;		
	}
	
	
	
	
	
	
	public String fanData(String data,int commentscount,int likecount){
		try {
			elasticsearchConnect es_conn = new elasticsearchConnect();
			JSONObject fandata = new JSONObject(data);
			JSONObject output = new JSONObject();
			String  posId = fandata.getJSONObject("fandata").getString("id");
//			System.out.println(posId);
			output.put("created_time", fandata.getJSONObject("fandata").getString("created_time"));
			
			try {
				if (fandata.getJSONObject("fandata").getString("description").isEmpty()==false) {
					output.put("description", fandata.getJSONObject("fandata").getString("description"));
				} 
			} catch (Exception e) {
				// TODO: handle exception
				output.put("description","");
			}

			output.put("post_id", posId);
			output.put("like_count", likecount);
			try {
				if (fandata.getJSONObject("fandata").getString("link").isEmpty()==false) {
					output.put("link", fandata.getJSONObject("fandata").getString("link"));
				} 
			} catch (Exception e) {
				// TODO: handle exception
				output.put("link","");
			}
			
			output.put("from", fandata.getJSONObject("fandata").getJSONObject("from"));
			try {
				if (fandata.getJSONObject("fandata").getString("name").isEmpty()==false) {
					output.put("name", fandata.getJSONObject("fandata").getString("name"));
				}
			} catch (Exception e) {
				// TODO: handle exception
				output.put("name", "");
			}
			try {
				if (fandata.getJSONObject("fandata").getJSONObject("shares").getString("count").isEmpty()==false) {
					output.put("shares", fandata.getJSONObject("fandata").getJSONObject("shares").getString("count"));
				}
			} catch (Exception e) {
				// TODO: handle exception
				output.put("shares", "0");
			}
			
			try {
				if (fandata.getJSONObject("fandata").getString("message").isEmpty()==false) {
					output.put("message",fandata.getJSONObject("fandata").getString("message"));
				}
			} catch (Exception e) {
				// TODO: handle exception
				output.put("message", "");
			}
			
			try {
				if (fandata.getJSONObject("fandata").getString("updated_time").isEmpty()==false) {
					output.put("updated_time", fandata.getJSONObject("fandata").getString("updated_time"));
				}
			} catch (Exception e) {
				// TODO: handle exception
				output.put("updated_time", "");
			}
			
			output.put("comment_count", commentscount);
			
			try {
				if (fandata.getJSONObject("fandata").getString("type").isEmpty()==false) {
					output.put("type", fandata.getJSONObject("fandata").getString("type"));
				}
			} catch (Exception e) {
				// TODO: handle exception
				output.put("type", "");
			}
			
			try {
				if (fandata.getJSONObject("fandata").getString("status_type").isEmpty()==false) {
					output.put("status_type", fandata.getJSONObject("fandata").getString("status_type"));
				}
			} catch (Exception e) {
				// TODO: handle exception
				output.put("status_type", "");
			}
			
			es_conn.client.prepareIndex("facebook","post",posId).setSource(output.toString()).execute().actionGet();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "fandatafalse";
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return "fan_data_true";
	}
	
	
	
	
	public  String checkdatetime(String fbtime,int setTime){
		try {
			java.util.Date date = null;
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, setTime);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date d1 = java.sql.Date.valueOf(sdf.format(cal.getTime()));
			System.out.println("Crawler_end_Time:"+d1);
			String dateString = fbtime;
			date = sdf.parse(dateString);
			Date d2 = java.sql.Date.valueOf(sdf.format(date));
			System.out.println("facebook_data_Time:"+d2);
			if (d1.getTime()<d2.getTime()){
//				System.out.println("====");
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
