package com.mitocode.repo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.mitocode.model.Plato;

public interface IPlatoRepo extends ReactiveMongoRepository<Plato, String> {

}
