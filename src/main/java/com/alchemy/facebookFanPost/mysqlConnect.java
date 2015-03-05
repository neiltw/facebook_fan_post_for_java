package com.alchemy.facebookFanPost;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;




public class mysqlConnect {

	public Connection con = null;
	public Statement stmt = null;
	public ResultSet rs = null;
	public PreparedStatement pst = null;
	private String url = "jdbc:mysql://localhost:3306/shojo?useUnicode=true&characterEncoding=utf-8";
	private String user= "root";
	private String password = "root@iii";
	
	
	public mysqlConnect() {
		// TODO Auto-generated constructor stub
		try {
			Class.forName("com.mysql.jdbc.Driver");
		    con = DriverManager.getConnection(this.url,this.user,this.password);
		    } catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("SQLException :"+e.toString()); 
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("DriverClassNotFound :"+e.toString()); 
			e.printStackTrace();
		}
		
	}
	
	
	public  ResultSet SelectTable(){
		String sql = "SELECT access_token FROM shojo.fbuser";
		try {
			stmt = con.createStatement();
			rs= stmt.executeQuery(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("SQLException :"+e.toString()); 
			e.printStackTrace();
		}
		return rs;
		
	}


	public Connection getCon() {
		return con;
	}


	public void setCon(Connection con) {
		this.con = con;
	}


	public Statement getStmt() {
		return stmt;
	}


	public void setStmt(Statement stmt) {
		this.stmt = stmt;
	}


	public ResultSet getRs() {
		return rs;
	}


	public void setRs(ResultSet rs) {
		this.rs = rs;
	}


	public PreparedStatement getPst() {
		return pst;
	}


	public void setPst(PreparedStatement pst) {
		this.pst = pst;
	}


	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}


	public String getUser() {
		return user;
	}


	public void setUser(String user) {
		this.user = user;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}



	
}
