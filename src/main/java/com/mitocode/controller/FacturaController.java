package com.mitocode.controller;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.mitocode.dto.FiltroDTO;
import com.mitocode.model.Factura;
import com.mitocode.pagination.PageSupport;
import com.mitocode.service.IFacturaService;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.Map;
import javax.validation.Valid;
import org.cloudinary.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/facturas")
public class FacturaController {

  @Autowired
  private IFacturaService service;

  @GetMapping
  public Mono<ResponseEntity<Flux<Factura>>> listar() {
    Flux<Factura> fxFacturas = service.listar();

    return Mono.just(ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(fxFacturas)
    );
  }

  @GetMapping("/{id}")
  public Mono<ResponseEntity<Factura>> listarPorId(@PathVariable("id") String id) {
    return service.listarPorId(id) //Mono<Factura> -> Mono<ResponseEntity<Factura>>
        .map(p -> ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(p)
        )
        .defaultIfEmpty(ResponseEntity.notFound().build()); //Mono<ResponseEntity<Factura>>
  }

  @PostMapping
  public Mono<ResponseEntity<Factura>> registrar(@Valid @RequestBody Factura Factura,
      final ServerHttpRequest req) {
    //201 | localhost:8080/Facturas/123yanss
    return service.registrar(Factura)
        .map(p -> ResponseEntity
            .created(URI.create(req.getURI().toString().concat("/").concat(p.getId())))
            .contentType(MediaType.APPLICATION_JSON)
            .body(p)
        );
  }


  @PutMapping("/{id}")
  public Mono<ResponseEntity<Factura>> modificar(@PathVariable("id") String id,
      @Valid @RequestBody Factura Factura) {

    Mono<Factura> monoBody = Mono.just(Factura);
    Mono<Factura> monoBD = service.listarPorId(id);

    return monoBD
        .zipWith(monoBody, (bd, f) -> {
          bd.setId(id);
          bd.setCliente(f.getCliente());
          bd.setDescripcion(f.getDescripcion());
          bd.setObservacion(f.getObservacion());
          bd.setItems(f.getItems());
          return bd;
        })
        .flatMap(service::modificar) //bd -> service.modificar(bd)
        .map(pl -> ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(pl))
        .defaultIfEmpty(new ResponseEntity<Factura>(HttpStatus.NOT_FOUND));
  }

  @DeleteMapping("/{id}")
  public Mono<ResponseEntity<Void>> eliminar(@PathVariable("id") String id) {
    return service.listarPorId(id)
        .flatMap(p -> {
          return service.eliminar(p.getId()) //Mono<Void>
              .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
        })
        .defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
  }

  @GetMapping("/hateoas/{id}")
  public Mono<EntityModel<Factura>> listarHateoasPorId(@PathVariable("id") String id) {
    //localhost:8080/Facturas/60779cc08e34kgjas0434
    Mono<Link> link1 = linkTo(methodOn(FacturaController.class).listarPorId(id)).withSelfRel()
        .toMono(); //De esta forma se construye el link
    //Se le pasa la referencia al método y se construye una referencia
    //al resultado
    Mono<Link> link2 = linkTo(methodOn(FacturaController.class).listarPorId(id)).withSelfRel()
        .toMono(); //Qué pasaría si se manejan 2 links (se crea un link 2 para ejemplo)

    //Práctica intermedia:
//		return service.listarPorId(id)
//				.flatMap(p -> {
//					return link1.map(lk -> EntityModel.of(p, lk));
//				}); //No es tan recomendada puesto que puede llegar a volverse un callback hell

    //Práctica Más apropiada:
    return service.listarPorId(id)
        .zipWith(link1, (p, lk) -> EntityModel.of(p, lk));

    //Si es más de un link:
//		return link1.zipWith(link2)
//				.map(function((left, right) -> Links.of(left, right)))
//				.zipWith(service.listarPorId(id), (lk, p) -> EntityModel.of(p, lk));
  }

  @GetMapping("/pageable")
  public Mono<ResponseEntity<PageSupport<Factura>>> listarPageable(
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "10") int size
  ) {
    Pageable pageRequest = PageRequest.of(page, size);
    return service.listarPage(pageRequest)
        .map(p -> ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(p)
        )
        .defaultIfEmpty(ResponseEntity.noContent().build());
  }

  @PostMapping("/buscar")
  public Mono<ResponseEntity<Flux<Factura>>> buscar(@RequestBody FiltroDTO filtro){
    Flux<Factura> fxFacturas =  service.obtenerFacturasPorFiltro(filtro);
    return Mono.just(ResponseEntity.ok()
    .contentType(MediaType.APPLICATION_JSON)
    .body(fxFacturas));
  }

  @GetMapping("/generarReporte/{id}")
  public Mono<ResponseEntity<byte[]>> generarReporte(@PathVariable("id") String id){

    Mono<byte[]> monoReporte = service.generarReporte(id);

    return monoReporte
        .map(bytes -> ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM) //Est tipo de datos representa arreglo de bytes en este caso PDF
            .body(bytes)
        ).defaultIfEmpty(new ResponseEntity<byte[]>(HttpStatus.NO_CONTENT));
  }

}
