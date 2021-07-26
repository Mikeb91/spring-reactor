package com.mitocode.service.impl;

import com.mitocode.model.Cliente;
import com.mitocode.repo.IClienteRepo;
import com.mitocode.service.IClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ClienteServiceImpl extends CrudImpl<Cliente, String> implements IClienteService {

	@Autowired
	private IClienteRepo repo;


	@Override
	protected ReactiveMongoRepository getRepo() {
		return repo;
	}
}
