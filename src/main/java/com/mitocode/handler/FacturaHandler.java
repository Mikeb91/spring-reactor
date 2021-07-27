package com.mitocode.handler;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import com.mitocode.model.Factura;
import com.mitocode.service.IFacturaService;
import com.mitocode.validators.RequestValidators;
import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class FacturaHandler {

  @Autowired
  private IFacturaService service;
  @Autowired
  private RequestValidators validadorGeneral;

  public Mono<ServerResponse> listar(ServerRequest req) {
    return ServerResponse
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(service.listar(), Factura.class);
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
    Mono<Factura> monoFactura = req.bodyToMono(
        Factura.class); //Forma de recuperar el body directamente en instancia de Mono<Factura>
    return monoFactura
        .flatMap(validadorGeneral::validate)
        .flatMap(p -> service.registrar(p))
        .flatMap(p -> ServerResponse.created(URI.create(req.uri().toString().concat(p.getId())))
            .contentType(MediaType.APPLICATION_JSON)
            .body(fromValue(p))
        );
  }

  public Mono<ServerResponse> modificar(ServerRequest req) {
    Mono<Factura> monoFactura = req.bodyToMono(Factura.class);
    Mono<Factura> monoBD = service.listarPorId(req.pathVariable("id"));

    return monoBD
        .zipWith(monoFactura, (bd, f) -> {
          bd.setId(req.pathVariable("id"));
          bd.setCliente(f.getCliente());
          bd.setDescripcion(f.getDescripcion());
          bd.setObservacion(f.getObservacion());
          bd.setItems(f.getItems());
          return bd;
        })
        .flatMap(validadorGeneral::validate)
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
