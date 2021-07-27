package com.mitocode.handler;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import com.mitocode.model.Cliente;
import com.mitocode.service.IClienteService;
import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class ClienteHandler {

  @Autowired
  private IClienteService service;

  public Mono<ServerResponse> listar(ServerRequest req) {
    return ServerResponse
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(service.listar(), Cliente.class);
  }

  public Mono<ServerResponse> listarPorId(ServerRequest req) {
    String id = req.pathVariable("id");
    return service.listarPorId(id)
        .flatMap(p -> ServerResponse
            .ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(fromValue(p)) //FromValue - BodyInserters.fromValue static import
        )
        .switchIfEmpty(ServerResponse.notFound().build()); //Diferente forma de manejar empty en lugar de DefaultIfEmpty
  }

  public Mono<ServerResponse> registrar(ServerRequest req) {
    Mono<Cliente> monoCliente = req.bodyToMono(
        Cliente.class); //Forma de recuperar el body directamente en instancia de Mono<Cliente>
    return monoCliente
        .flatMap(p -> service.registrar(p))
        .flatMap(p -> ServerResponse.created(URI.create(req.uri().toString().concat(p.getId())))
            .contentType(MediaType.APPLICATION_JSON)
            .body(fromValue(p))
        );
  }

  public Mono<ServerResponse> modificar(ServerRequest req) {
    Mono<Cliente> monoCliente = req.bodyToMono(Cliente.class);
    Mono<Cliente> monoBD = service.listarPorId(req.pathVariable("id"));

    return monoBD
        .zipWith(monoCliente, (bd, cl) -> {
          bd.setId(req.pathVariable("id"));
          bd.setNombres(cl.getNombres());
          bd.setApellidos(cl.getApellidos());
          bd.setFechaNac(cl.getFechaNac());
          bd.setUrlFoto(cl.getUrlFoto());
          return bd;
        })
        .flatMap(service::modificar)
        .flatMap(p -> ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(fromValue(p))
        )
        .switchIfEmpty(ServerResponse.notFound().build());
  }

  public Mono<ServerResponse> eliminar (ServerRequest req) {
    String id = req.pathVariable("id");
    return service.listarPorId(id)
        .flatMap(p-> service.eliminar(p.getId())
          .then(ServerResponse.noContent().build())
        ).switchIfEmpty(ServerResponse.notFound().build());
  }

}
