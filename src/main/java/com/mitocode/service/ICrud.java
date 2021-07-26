package com.mitocode.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ICrud<T, ID> {

  Mono<T> registrar(T t);
  Mono<T> modificar(T t);
  Flux<T> listar();
  Mono<T> listarPorId(ID id);
  Mono<Void> eliminar(ID id);

}
