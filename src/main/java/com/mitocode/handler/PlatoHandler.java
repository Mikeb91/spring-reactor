package com.mitocode.handler;

import com.mitocode.dto.ValidationDTO;
import com.mitocode.model.Plato;
import com.mitocode.service.IPlatoService;
import com.mitocode.validators.RequestValidators;
import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class PlatoHandler {

  @Autowired
  private IPlatoService service;
//  @Autowired
//  private Validator validador; //Dependencia para validación manual
  @Autowired
  private RequestValidators validadorGeneral;

  public Mono<ServerResponse> listar(ServerRequest req) {
    return ServerResponse
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(service.listar(), Plato.class);
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
    Mono<Plato> monoPlato = req.bodyToMono(
        Plato.class); //Forma de recuperar el body directamente en instancia de Mono<Plato>

//    return monoPlato
//        .flatMap(p -> {
//          Errors errores = new BeanPropertyBindingResult(p, Plato.class.getName());   //Forma manual de generar validaciones
//          validador.validate(p, errores);
//          if(errores.hasErrors()){
//            return Flux.fromIterable(errores.getFieldErrors())
//            .map(error -> new ValidationDTO(error.getField(), error.getDefaultMessage()))
//                .collectList()
//                .flatMap(listaErrores -> {
//                  return ServerResponse.badRequest()
//                      .contentType(MediaType.APPLICATION_JSON)
//                      .body(fromValue(listaErrores));
//                });
//          }else{
//            return service.registrar(p)
//                .flatMap(pdb -> ServerResponse
//                .created(URI.create(req.uri().toString().concat(p.getId())))
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(fromValue(pdb))
//                );
//          }
//        });

    return monoPlato
        .flatMap(validadorGeneral::validate) //Forma de generar validaciones por intercepción ver clase genérica RequestValidators
        .flatMap(p -> service.registrar(p))
        .flatMap(p -> ServerResponse.created(URI.create(req.uri().toString().concat(p.getId())))
            .contentType(MediaType.APPLICATION_JSON)
            .body(fromValue(p))
        );
  }

  public Mono<ServerResponse> modificar(ServerRequest req) {
    Mono<Plato> monoPlato = req.bodyToMono(Plato.class);
    Mono<Plato> monoBD = service.listarPorId(req.pathVariable("id"));

    return monoBD
        .zipWith(monoPlato, (bd, p) -> {
          bd.setId(p.getId());
          bd.setNombre(p.getNombre());
          bd.setEstado(p.getEstado());
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
