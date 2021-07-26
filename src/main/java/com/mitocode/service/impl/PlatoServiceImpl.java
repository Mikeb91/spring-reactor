package com.mitocode.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Service;

import com.mitocode.model.Plato;
import com.mitocode.repo.IPlatoRepo;
import com.mitocode.service.IPlatoService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PlatoServiceImpl extends CrudImpl<Plato, String> implements IPlatoService {

	@Autowired
	private IPlatoRepo repo;


	@Override
	protected ReactiveMongoRepository getRepo() {
		return repo;
	}
}
