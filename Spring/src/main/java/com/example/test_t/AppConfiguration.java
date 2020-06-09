package com.example.test_t;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.azure.data.cosmos.ConnectionMode;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.CosmosKeyCredential;
import com.microsoft.azure.spring.data.cosmosdb.config.AbstractCosmosConfiguration;
import com.microsoft.azure.spring.data.cosmosdb.config.CosmosDBConfig;
import com.microsoft.azure.spring.data.cosmosdb.core.ResponseDiagnostics;
import com.microsoft.azure.spring.data.cosmosdb.core.ResponseDiagnosticsProcessor;
import com.microsoft.azure.spring.data.cosmosdb.repository.config.EnableCosmosRepositories;

import io.reactivex.annotations.Nullable;

@Configuration
@EnableCosmosRepositories
public class AppConfiguration extends AbstractCosmosConfiguration {
	@Value("${azure.cosmosdb.uri}")
	private String uri;

	@Value("${azure.cosmosdb.key}")
	private String key;

	@Value("${azure.cosmosdb.database}")
	private String dbName;

	@Value("${azure.cosmosdb.populateQueryMetrics}")
	private boolean populateQueryMetrics;

	private CosmosKeyCredential cosmosKeyCredential;

    private static class ResponseDiagnosticsProcessorImplementation implements ResponseDiagnosticsProcessor {

        @Override
        public void processResponseDiagnostics(@Nullable ResponseDiagnostics responseDiagnostics) {
           // System.out.println("Response Diagnostics "+ responseDiagnostics);
        }
    }
    
	@Bean
	@Primary
	public CosmosDBConfig getConfig() {
		this.cosmosKeyCredential = new CosmosKeyCredential(key);
		ConnectionPolicy customizedConnectionPolicy = new ConnectionPolicy();
	    // Set the connection mode to Direct (TCP).
	    customizedConnectionPolicy.connectionMode(ConnectionMode.DIRECT);

	    // Set the maximum number of HTTP/TCP connections to 1000 per application.
	    customizedConnectionPolicy.maxPoolSize(10000);

	    // Set the request timeout to 10 seconds.
	    customizedConnectionPolicy.requestTimeoutInMillis(60000);

	    // Set the idle connection timeout to two minutes.
	    customizedConnectionPolicy.idleConnectionTimeoutInMillis(120000);
	    
		CosmosDBConfig cosmosdbConfig = CosmosDBConfig.builder(uri, this.cosmosKeyCredential,
				dbName)
				.connectionPolicy(customizedConnectionPolicy)
				.build();
		
		cosmosdbConfig.setPopulateQueryMetrics(populateQueryMetrics);
		cosmosdbConfig.setResponseDiagnosticsProcessor(new ResponseDiagnosticsProcessorImplementation());
		return cosmosdbConfig;
	}

}
