package com.example.test_t;


import com.microsoft.azure.spring.data.cosmosdb.repository.ReactiveCosmosRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface CustomerRepositry extends ReactiveCosmosRepository<Customer, String> {
    Flux<Customer> findByFirstName(String firstName);
}
