package com.alchemy.facebookFanPost;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class fanLikePage {


	public String  getlikedata (String data){
		int likeCount = 0;
		try {
			elasticsearchConnect es_conn = new elasticsearchConnect();
			JSONObject likedata = new JSONObject(data);
			JSONObject obj =null;
			
			JSONObject from = new JSONObject(likedata.getJSONObject("fandata").getString("from"));
			String  postId = likedata.getJSONObject("fandata").getString("id"); //post Id
			for (int i = 0; i < likedata.getJSONObject("likes").getJSONArray("data").length(); i++) {
				obj=new JSONObject(likedata.getJSONObject("likes").getJSONArray("data").get(i).toString());
				JSONObject output =new JSONObject();
				output.put("username", obj.getString("name"));
				output.put("userid", obj.getString("id"));
				output.put("from", from);
				output.put("post_id", postId);
				String likeid = postId+"_"+obj.getString("id");
				es_conn.client.prepareIndex("facebook","like",likeid).setSource(output.toString()).execute().actionGet();
				likeCount++;
			}
			while (true) {
				try {
					if (likedata.getJSONObject("likes").getJSONObject("paging").getString("next").isEmpty()==false){
						String url =likedata.getJSONObject("likes").getJSONObject("paging").getString("next");
						JSONObject likePage = new JSONObject(readUrlPagneLike(url));
						likedata.put("likes", likePage);
						for (int i = 0; i < likedata.getJSONObject("likes").getJSONArray("data").length(); i++) {
							obj=new JSONObject(likedata.getJSONObject("likes").getJSONArray("data").get(i).toString());
							JSONObject output =new JSONObject();
							output.put("username", obj.getString("name"));
							output.put("userid", obj.getString("id"));
							output.put("from", from);
							output.put("post_id", postId);
							String likeid = postId+"_"+obj.getString("id");
							es_conn.client.prepareIndex("facebook","like",likeid).setSource(output.toString()).execute().actionGet();
							likeCount++;
						}
					}
				} catch (Exception e) {
					// TODO: handle exception
//					System.out.println("not like data ");
					break;
				}	
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String count = String.valueOf(likeCount);
		return count;
		
	}

	
	
	public String readUrlPagneLike(String url){
		JSONObject getPage = null;
		try {
			URL likeNext = new URL(url);
			HttpURLConnection likeConn = (HttpURLConnection) likeNext.openConnection();
			InputStream checkFan;
			likeConn.setConnectTimeout(5000);
			likeConn.setReadTimeout(5000);
			if (likeConn.getResponseCode() >= 400) {
				checkFan = likeConn.getErrorStream();
				return getPage.toString();
			} else {
			    BufferedReader reader = new BufferedReader(new InputStreamReader(likeConn.getInputStream(),"UTF-8"));
			    String line = "";
			    while((line = reader.readLine()) != null) {
			    	getPage = new JSONObject(line);
			    }
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return getPage.toString();
	}
	
	
	
	
	
	

	
}
