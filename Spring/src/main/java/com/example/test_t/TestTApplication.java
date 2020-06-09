package com.example.test_t;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.azure.data.cosmos.PartitionKey;
import com.fasterxml.uuid.Generators;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class TestTApplication implements CommandLineRunner {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestTApplication.class);
	public List<Long> writeLatency;
	public List<Long> readLatency;

	@Autowired
	private CustomerRepositry repository;

	public static void main(String[] args) {
		SpringApplication.run(TestTApplication.class, args);
	}

	public void run(String... args) throws Exception {

		cleanup();

		int numberOfOperations = 10000;// Integer.parseInt(args[0]);

		for (int i = 0; i < numberOfOperations;) {

			try {
				long insertLatency = insertDocuments(i);
			
				long readLatency = readDocuments(i);
			
				System.out.println(insertLatency + "\t" + readLatency + "\t" + i);

				if (i < 100)
					i += 10;
				else
					i += 100;
			} catch (Exception ex) {
				System.out.println(ex.getStackTrace());
				System.exit(1);
			} finally {
			}
		}

		System.exit(1);

	}

	public static Long Percentile(List<Long> latencies, double Percentile) {

		int Index = (int) Math.ceil(((double) Percentile / (double) 100) * (double) latencies.size());
		return latencies.get(Index - 1);
	}

	List<String> lstDocID = new ArrayList<String>();

	public long insertDocuments(int numberOfDocToInsert) {
		int count = 1;
		long startTime = 0;
		long totalLatency = 0;
		List<Mono<Customer>> lstMono = new ArrayList<Mono<Customer>>();

		CountDownLatch latch = new CountDownLatch(numberOfDocToInsert);

		for (int i = 0; i < numberOfDocToInsert; i++) {
			if ((count > 2 || numberOfDocToInsert > 2) && startTime == 0) {
				startTime = System.currentTimeMillis();
			}
			String id = Generators.randomBasedGenerator().generate().toString();
			lstDocID.add(id);
			Customer objCustomer = new Customer(id, "AB" + i, "Calderon1", "4567 Main St Buffalo, NY 98052");
			Mono<Customer> saveCustomerMono = repository.save(objCustomer);

			saveCustomerMono.publishOn(Schedulers.elastic()).subscribe(resourceResponse -> {
			}, Throwable::printStackTrace, latch::countDown);

			lstMono.add(saveCustomerMono);
			count++;
		}

		try {
			Flux.merge(lstMono);
			latch.await();
			totalLatency = System.currentTimeMillis() - startTime;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.exit(1);
			e.printStackTrace();
		}
		return totalLatency;
	}

	public long readDocuments(int numberOfDocToRead) {
		int count = 1;
		long startTime = 0;
		long totalLatency = 0;

		List<Mono<Customer>> lstMono = new ArrayList<Mono<Customer>>();

		CountDownLatch latch = new CountDownLatch(numberOfDocToRead);

		for (int i = 0; i < numberOfDocToRead; i++) {
			if ((count > 2 || numberOfDocToRead > 2) && startTime == 0) {
				startTime = System.currentTimeMillis();
			}
			String id=lstDocID.get(i);
			Mono<Customer> objCosmosItem = repository.findById(id,
					new PartitionKey(id));

			objCosmosItem.publishOn(Schedulers.elastic()).subscribe(resourceResponse -> {
			}, Throwable::printStackTrace, latch::countDown);

			lstMono.add(objCosmosItem);
			count++;
		}

		try {
			Flux.merge(lstMono);
			latch.await();
			totalLatency = System.currentTimeMillis() - startTime;
		} catch (InterruptedException e) {
			System.out.println("here i am");
			System.exit(1);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lstDocID.clear();
		return totalLatency;
	}

	@PostConstruct
	public void setup() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);

		LOGGER.info("Clear the database");

		this.repository.deleteAll().subscribeOn(Schedulers.elastic()).subscribe(x -> {
		}, Throwable::printStackTrace, latch::countDown);

		latch.await();
	}

	@PreDestroy
	public void cleanup() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);

		LOGGER.info("Cleaning up customers");
		// cleanup with unblocking way.
		this.repository.deleteAll().subscribeOn(Schedulers.elastic()).subscribe(x -> {
		}, Throwable::printStackTrace, latch::countDown);

		latch.await();
	}
}
