package com.azure.async99percentile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class AppMain {

	static AppConfiguration _objConfig;

	public static void main(String[] args) throws IOException, InterruptedException {
		_objConfig = new AppConfiguration();
		CosmosDBHelper objCosmos = new CosmosDBHelper(_objConfig.HostName, _objConfig.MasterKey, _objConfig.DbName,
				_objConfig.CollName);

		try {
			//Create collection, in case it exists it will throw the exception
				try {
					objCosmos.createCollection();
				} catch (Exception e) {
					// Not required to do anything.
				}
				

			int numberOfOperations = args.length > 0 ? Integer.parseInt(args[0]) : _objConfig.NumberOfOperations;

			for (int i = 0; i < numberOfOperations;) {
				long insertLatency = objCosmos.insertDocuments(i);
				
				long readLatency = objCosmos.readDocuments(i);
			
				System.out.println(i + "\t" + insertLatency + "\t" + readLatency);
				
				if (i < 100)
					i += 10;
				else
					i += 100;
				}
		
		} finally {
				System.exit(1);
		}
	

	}

}
