package com.mitocode.service.impl;

import com.mitocode.model.Cliente;
import com.mitocode.model.Factura;
import com.mitocode.repo.IClienteRepo;
import com.mitocode.repo.IFacturaRepo;
import com.mitocode.service.IClienteService;
import com.mitocode.service.IFacturaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Service;

@Service
public class FacturaServiceImpl extends CrudImpl<Factura, String> implements IFacturaService {

	@Autowired
	private IFacturaRepo repo;


	@Override
	protected ReactiveMongoRepository getRepo() {
		return repo;
	}
}
