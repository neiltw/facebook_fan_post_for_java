package com.alchemy.facebookFanPost;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.elasticsearch.bootstrap.Elasticsearch;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class fanCommentspage {

	public String fanCommentsfrist(String data){
		int commentsCount = 0;
		try {
			int setPage = 0;
			elasticsearchConnect es_conn = new elasticsearchConnect();
			JSONObject getcomments = new JSONObject(data);
			JSONObject from = new JSONObject(getcomments.getJSONObject("fandata").getString("from"));
			String  postId = getcomments.getJSONObject("comments").getString("id"); //post Id
			for (int i = 0; i < getcomments.getJSONObject("comments").getJSONObject("comments").getJSONArray("data").length(); i++) {
				JSONObject outout = new JSONObject(getcomments.getJSONObject("comments").getJSONObject("comments").getJSONArray("data").get(i).toString());
				String created_time =readCreated_timeUrl(outout.getString("id"));
				JSONObject output2 = new JSONObject();	
				JSONObject subCount = new JSONObject();	
				try{
					if (outout.getJSONObject("comments").getJSONArray("data").isNull(0)==false){
						subCount = new JSONObject(commentsSubData(outout.getJSONObject("comments").getJSONArray("data").toString(),created_time,outout.getString("id"),outout.getJSONObject("comments").toString(),postId,from.toString(),es_conn));
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
					System.out.println("not commentsSub data in output");
					subCount.put("count", "0");
				}
				output2.put("username", outout.getJSONObject("from").getString("name"));
				output2.put("comment", outout.getString("message"));
				output2.put("like_count", outout.getString("like_count"));
				output2.put("from", from);
				output2.put("post_id", postId);
				output2.put("created_time", created_time);
				output2.put("comments_sub_count", subCount.getString("count"));
				output2.put("userid", outout.getJSONObject("from").getString("id"));
				output2.put("id", outout.getString("id"));
				es_conn.client.prepareIndex("facebook","comments",outout.getString("id")).setSource(output2.toString()).execute().actionGet();

				setPage++;
				commentsCount++;
			}
			if (setPage !=0){
				while (true) {
					try {
						if (getcomments.getJSONObject("comments").getJSONObject("comments").getJSONObject("paging").getString("next").isEmpty()==false) {
							String url = getcomments.getJSONObject("comments").getJSONObject("comments").getJSONObject("paging").getString("next").toString();
							JSONObject commentspage = new JSONObject(readUrlPagneComments(url));
							for (int i = 0; i < commentspage.getJSONArray("data").length(); i++) {
								JSONObject getdata = new JSONObject(commentspage.getJSONArray("data").get(i).toString());
								JSONObject subCount = new JSONObject();	
								String created_time =readCreated_timeUrl(getdata.getString("id"));
								try {
									if (getdata.getString("comments").isEmpty()== false) {
										subCount = new JSONObject(commentsSubData(getdata.getJSONObject("comments").getJSONArray("data").toString(),created_time,getdata.getString("id"),getdata.getJSONObject("comments").toString(),postId,from.toString(), es_conn ));
									}
								} catch (Exception e) {
									// TODO: handle exception
//									System.out.println("not commentsSub data in getdata");
									subCount.put("count", "0");
								}
								JSONObject output = new JSONObject();
								output.put("username", getdata.getJSONObject("from").getString("name"));
								output.put("comment", getdata.getString("message"));
								output.put("like_count", getdata.getString("like_count"));
								output.put("from", from);
								output.put("post_id", postId);
								output.put("created_time", created_time);
								output.put("comments_sub_count", subCount.getString("count"));
								output.put("userid", getdata.getJSONObject("from").getString("id"));
								output.put("id", getdata.getString("id"));
								es_conn.client.prepareIndex("facebook","comments",getdata.getString("id")).setSource(output.toString()).execute().actionGet();
								commentsCount++;
							}
							JSONObject next = new JSONObject();
							next.put("comments", commentspage);
							getcomments.put("comments", next);
						}
					} catch (Exception e) {
						// TODO: handle exception
//						System.out.println("comments not page ");
						break;
					}
				}	
			}
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			System.out.println("===========not comments data============");
			
		}
		String count = String.valueOf(commentsCount); 
		return count;
	}
	
	
	
	public String commentsSubData(String data,String time , String comment_id, String datapage,String postId, String from, elasticsearchConnect es_conn){
		JSONObject getSub = null;
		int count = 0;
		try {
			JSONObject fromjson = new JSONObject(from);
			JSONArray getar = new JSONArray(data.toString());
			JSONObject getPage = new JSONObject(datapage);
			for (int i = 0; i < getar.length(); i++) {
				getSub = new JSONObject(getar.get(i).toString());
				JSONObject output = new JSONObject();
				JSONObject parent = new JSONObject();
				parent.put("username", getSub.getJSONObject("parent").getJSONObject("from").getString("name"));
				parent.put("comment", getSub.getJSONObject("parent").getString("message"));
				parent.put("userid", getSub.getJSONObject("parent").getJSONObject("from").getString("id"));
				parent.put("post_id", postId);
				parent.put("like_count", getSub.getJSONObject("parent").getString("like_count"));
				parent.put("created_time", getSub.getJSONObject("parent").getString("created_time"));
				parent.put("id", getSub.getJSONObject("parent").getString("id"));

				output.put("username", getSub.getJSONObject("from").getString("name"));
				output.put("id", getSub.getString("id"));
				output.put("post_id", postId);//post id
				output.put("created_time", time);
				output.put("from", fromjson);
				output.put("comment", getSub.getString("message"));
				output.put("parent", parent);
				output.put("userid", getSub.getJSONObject("from").getString("id"));
				es_conn.client.prepareIndex("facebook","comments_sub",getSub.getString("id")).setSource(output.toString()).execute().actionGet();
				count ++;
			}				
			int commentssub = 0;
			URL commentsSubNext = null;
			
			while (true) {
				try{
					if (getPage.getJSONObject("paging").getString("next").isEmpty()== false){
						getPage = new JSONObject(readUrlPagneComments(getPage.getJSONObject("paging").getString("next")));
					}
						commentssub++;
				} catch (Exception e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
					System.out.println("=======Not comments Sub Page data=============");
					getSub.put("count", count);
					break;
				}
				//commentsSub Page input to es 
				if (commentssub !=0){
					JSONArray commentsSubPageDat = new JSONArray(getPage.getString("data").toString());
					for (int i = 0; i < commentsSubPageDat.length(); i++) {
						JSONObject output = new JSONObject();
						JSONObject getSubData = new JSONObject(commentsSubPageDat.get(i).toString());
						JSONObject parent = new JSONObject();
						parent.put("username", getSubData.getJSONObject("parent").getJSONObject("from").getString("name"));
						parent.put("comment", getSubData.getJSONObject("parent").getString("message"));
						parent.put("userid", getSubData.getJSONObject("parent").getJSONObject("from").getString("id"));
						parent.put("post_id", postId);
						parent.put("like_count", getSubData.getJSONObject("parent").getString("like_count"));
						parent.put("created_time", getSubData.getJSONObject("parent").getString("created_time"));
						parent.put("id", getSubData.getJSONObject("parent").getString("id"));

						output.put("username", getSubData.getJSONObject("from").getString("name"));
						output.put("id", getSubData.getString("id"));
						output.put("post_id", comment_id);
						output.put("created_time", time);
						output.put("comment", getSubData.getString("message"));
						output.put("parent", parent);
						output.put("userid", getSubData.getJSONObject("from").getString("id"));
						output.put("from", fromjson);
						es_conn.client.prepareIndex("facebook","comments_sub",getSubData.getString("id")).setSource(output.toString()).execute().actionGet();
						count++;
					}
					
				}
			}
			getSub.put("count", count);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return getSub.toString();
	}
	

	public String readUrlPagneComments(String url){
		JSONObject getPage = null;
		try {
			URL commentsNext = new URL(url);
			HttpURLConnection commentsConn = (HttpURLConnection) commentsNext.openConnection();
			InputStream checkFan;
			commentsConn.setConnectTimeout(5000);
			commentsConn.setReadTimeout(5000);
			if (commentsConn.getResponseCode() >= 400) {
				checkFan = commentsConn.getErrorStream();
				return getPage.toString();
			} else {
			    BufferedReader reader = new BufferedReader(new InputStreamReader(commentsConn.getInputStream(),"UTF-8"));
			    String line = "";
			    while((line = reader.readLine()) != null) {
			    	getPage = new JSONObject(line);
			    }
			    reader.close();
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
	
	

	
	public String readCreated_timeUrl (String url){
		URL fanurl;
		try {
			fanurl = new URL("https://graph.facebook.com/"+url);
			HttpURLConnection fanConn = (HttpURLConnection) fanurl.openConnection();
			InputStream checkFan;
			JSONObject jsfan = null;
			fanConn.setConnectTimeout(5000);
			fanConn.setReadTimeout(5000);
			if (fanConn.getResponseCode() >= 400) {
				checkFan = fanConn.getErrorStream();
			    wait(5);
			    System.out.println("==========wait======creadtime====================");
			    readCreated_timeUrl(url);
			} else {
			    BufferedReader reader = new BufferedReader(new InputStreamReader(fanConn.getInputStream(),"UTF-8"));
			    String line = "";
			    while((line = reader.readLine()) != null) {
			    	jsfan = new JSONObject(line);
			    }
			    reader.close();
			}
			String requestData = jsfan.getString("created_time");
			return requestData;
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return "read url false";

		
		
		
	}

		
	}
