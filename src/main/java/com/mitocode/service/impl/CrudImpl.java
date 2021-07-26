package com.mitocode.service.impl;

import com.mitocode.pagination.PageSupport;
import com.mitocode.service.ICrud;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class CrudImpl<T, ID> implements ICrud<T, ID> {

  protected abstract ReactiveMongoRepository<T, ID> getRepo();

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

   public Mono<PageSupport<T>> listarPage(Pageable page){
    return getRepo().findAll() //Flux<T>
        .collectList() //Mono<List<T>>
        .map(list -> new PageSupport<>(
                list
                    .stream()
                    .skip(page.getPageNumber() * page.getPageSize())
                    .limit(page.getPageSize())
                    .collect(Collectors.toList()),
                page.getPageNumber(), page.getPageSize(), list.size()
            )
        );
  }

}
