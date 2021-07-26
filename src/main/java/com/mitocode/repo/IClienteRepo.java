package com.mitocode.repo;

import com.mitocode.model.Cliente;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface IClienteRepo extends ReactiveMongoRepository<Cliente, String> {

}
