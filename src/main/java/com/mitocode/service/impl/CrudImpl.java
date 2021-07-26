package com.mitocode.service.impl;

import com.mitocode.service.ICrud;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class CrudImpl<T,ID> implements ICrud<T, ID> {

  protected abstract ReactiveMongoRepository getRepo();

  @Override
  public Mono<T> registrar(T t) {
    return getRepo().save(t);
  }

  @Override
  public Mono<T> modificar(T t) {
    return getRepo().save(t);
  }

  @Override
  public Flux<T> listar() {
    return getRepo().findAll();
  }

  @Override
  public Mono<T> listarPorId(ID id) {
    return getRepo().findById(id);
  }

  @Override
  public Mono<Void> eliminar(ID id) {
    return getRepo().deleteById(id);
  }
}
