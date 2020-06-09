package com.azure.async99percentile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.uuid.Generators;
import com.azure.cosmos.models.CosmosAsyncItemResponse;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.ThroughputProperties;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class CosmosDBHelper {

	static CosmosAsyncClient client = null;
	CosmosAsyncContainer container = null;
	String _dbName = "";
	String _collName = "";
	// Document _docs;
	public List<Long> writeLatency;
	public List<Long> readLatency;
	CosmosAsyncDatabase objDatabase = null;

	public CosmosDBHelper(String Host, String MasterKey, String dbName, String collName) throws InterruptedException {
		_dbName = dbName;
		_collName = collName;
		_dbName = dbName;
		_collName = collName;
		CosmosClientBuilder cb = new CosmosClientBuilder();

		if (client == null)
			client = cb.endpoint(Host)// (Host, MasterKey, dbName, collName).Builder()
					.directMode().key(MasterKey).buildAsyncClient();

		objDatabase = client.getDatabase(_dbName);
		container = objDatabase.getContainer(collName);

	}

	public void createCollection() {
		CountDownLatch latch = new CountDownLatch(1);
				
		PartitionKeyDefinition pkd = new PartitionKeyDefinition();
		List<String> partitionKeyPaths = new ArrayList<String>();
		partitionKeyPaths.add("/id");
		pkd.setPaths(partitionKeyPaths);

		IndexingPolicy idxPolicy = new IndexingPolicy();
		idxPolicy.setAutomatic(false);
		idxPolicy.setIndexingMode(IndexingMode.NONE);

		// Create a container with a partition key and provision 1000 RU/s throughput.
		CosmosContainerProperties objContainerProperties = new CosmosContainerProperties(_collName, pkd);
		objContainerProperties.setIndexingPolicy(idxPolicy);

		CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();

		options.setConsistencyLevel(ConsistencyLevel.EVENTUAL);

		
		ThroughputProperties autoscaleThroughputProperties = ThroughputProperties.createAutoscaledThroughput(100000);
		var objColl = objDatabase.createContainer(objContainerProperties, autoscaleThroughputProperties, options);

		objColl.publishOn(Schedulers.elastic()).subscribe(r -> {
			container = r.getContainer();
		}, Throwable::printStackTrace, latch::countDown);

		try {
			latch.await();

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	List<String> lstDocID = new ArrayList<String>();

	//helper method to insert the documents
	public long insertDocuments(int numberOfDocToInsert) {
		int count = 1;
		long startTime = 0;
		long totalLatency = 0;
		List<Mono<CosmosAsyncItemResponse<CustomerModel>>> lstMono = new ArrayList<Mono<CosmosAsyncItemResponse<CustomerModel>>>();

		CountDownLatch latch = new CountDownLatch(numberOfDocToInsert);

		for (int i = 0; i < numberOfDocToInsert; i++) {
			if ((count > 2 || numberOfDocToInsert > 2) && startTime == 0) {
				startTime = System.currentTimeMillis();
			}
			CustomerModel doc = new CustomerModel();
			doc.id = Generators.randomBasedGenerator().generate().toString();
			lstDocID.add(doc.id);
			doc.name = "AB" + i;
			CosmosItemRequestOptions ro = new CosmosItemRequestOptions();

			Mono<CosmosAsyncItemResponse<CustomerModel>> objMono = container.createItem(doc, ro);
			objMono.publishOn(Schedulers.elastic()).subscribe(resourceResponse -> {
			}, Throwable::printStackTrace, latch::countDown);

			lstMono.add(objMono);
			count++;
		}

		try {
			Flux.merge(lstMono);
			latch.await();
			totalLatency = System.currentTimeMillis() - startTime;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return totalLatency;
	}

	public long readDocuments(int numberOfDocToRead) {
		int count = 1;
		long startTime = 0;
		long totalLatency = 0;
		List<Mono<CosmosAsyncItemResponse<CustomerModel>>> lstMono = new ArrayList<Mono<CosmosAsyncItemResponse<CustomerModel>>>();

		CountDownLatch latch = new CountDownLatch(numberOfDocToRead);

		for (int i = 0; i < numberOfDocToRead; i++) {
			if ((count > 2 || numberOfDocToRead > 2) && startTime == 0) {
				startTime = System.currentTimeMillis();
			}
			String id=lstDocID.get(i);
			var objCosmosItem = container.readItem(id, new PartitionKey(id),
					CustomerModel.class);

			objCosmosItem.publishOn(Schedulers.elastic())
			.subscribe(resourceResponse -> {
			}, Throwable::printStackTrace, latch::countDown);

			lstMono.add(objCosmosItem);
			count++;
		}

		try {
			Flux.merge(lstMono);
			latch.await();
			totalLatency = System.currentTimeMillis() - startTime;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lstDocID.clear();
		return totalLatency;
	}

	public void deleteCollection() {

		CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
		// options.offerThroughput=10000;
		options.setConsistencyLevel(ConsistencyLevel.EVENTUAL);

		CountDownLatch latch = new CountDownLatch(1);

		var objColl = container.delete();

		objColl.publishOn(Schedulers.elastic()).subscribe(r -> {
			container = r.getContainer();
		}, Throwable::printStackTrace, latch::countDown);

		try {
			latch.await();

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}