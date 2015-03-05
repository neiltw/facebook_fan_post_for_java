package com.alchemy.facebookFanPost;

import java.io.FileInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;


public class elasticsearchConnect {

	static Properties propsES = new Properties();
	

	Settings settings  ; 
	
	Client client ; 
	
	public  elasticsearchConnect() throws FileNotFoundException, IOException {
		
		propsES.load(new FileInputStream(System.getProperty("user.dir")+"/resources/elasticsearchAccountNumber.properties"));
	
		 this.settings= ImmutableSettings.settingsBuilder()
				 .put("cluster.name",propsES.getProperty("clustername"))
				 .put("username",propsES.getProperty("user"))
				 .put("password",propsES.getProperty("password"))
				 .put("client.transport.ping_timeout", "30s")
				 .put("node.client", true)
				 .put("client.transport.nodes_sampler_interval","5s")
				 .put("client.transport.sniff",false).build();
		 this.client= new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(propsES.getProperty("host"),Integer.parseInt(propsES.getProperty("apiport"))));
	
		
	}

	public void closeClient(){
		client.close();
	}
	
	public Settings getSettings() {
		return settings;
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public static Properties getPropsES() {
		return propsES;
	}

	public static void setPropsES(Properties propsES) {
		elasticsearchConnect.propsES = propsES;
	}


	
	
}
