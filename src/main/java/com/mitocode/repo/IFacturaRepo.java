package com.mitocode.repo;

import com.mitocode.model.Factura;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface IFacturaRepo extends ReactiveMongoRepository<Factura, String> {

}
