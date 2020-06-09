package com.azure.async99percentile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfiguration 
{
	public String HostName;
    public String MasterKey;
    public String DbName;
    public String CollName;
	public int NumberOfOperations;
     
    	public AppConfiguration() throws IOException {
    	    InputStream inputStream=null;
    		try {
    			Properties prop = new Properties();
    			String propFileName = "application.properties";
     
    			inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
     
    			if (inputStream != null) {
    				prop.load(inputStream);
    			} else {
    				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
    			}
     
    			// get the property value and print it out
    			this.HostName = prop.getProperty("azure.cosmosdb.uri");
    			this.MasterKey = prop.getProperty("azure.cosmosdb.key");
    			this.DbName = prop.getProperty("azure.cosmosdb.database");
    			this.CollName=prop.getProperty("azure.cosmosdb.collection");
    			this.NumberOfOperations =Integer.parseInt(prop.getProperty("azure.cosmosdb.numberofoperations"));
    		}
    		 finally {
    			inputStream.close();
    		}
    	}
    }


