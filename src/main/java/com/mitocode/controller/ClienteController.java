package com.mitocode.controller;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.mitocode.model.Cliente;
import com.mitocode.pagination.PageSupport;
import com.mitocode.service.IClienteService;
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
@RequestMapping("/clientes")
public class ClienteController {

  @Autowired
  private IClienteService service;

  @GetMapping
  public Mono<ResponseEntity<Flux<Cliente>>> listar() {
    Flux<Cliente> fxClientes = service.listar();

    return Mono.just(ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(fxClientes)
    );
  }

  @GetMapping("/{id}")
  public Mono<ResponseEntity<Cliente>> listarPorId(@PathVariable("id") String id) {
    return service.listarPorId(id) //Mono<Cliente> -> Mono<ResponseEntity<Cliente>>
        .map(p -> ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(p)
        )
        .defaultIfEmpty(ResponseEntity.notFound().build()); //Mono<ResponseEntity<Cliente>>
  }

  @PostMapping
  public Mono<ResponseEntity<Cliente>> registrar(@Valid @RequestBody Cliente Cliente,
      final ServerHttpRequest req) {
    //201 | localhost:8080/clientes/123yanss
    return service.registrar(Cliente)
        .map(p -> ResponseEntity
            .created(URI.create(req.getURI().toString().concat("/").concat(p.getId())))
            .contentType(MediaType.APPLICATION_JSON)
            .body(p)
        );
  }


  @PutMapping("/{id}")
  public Mono<ResponseEntity<Cliente>> modificar(@PathVariable("id") String id,
      @Valid @RequestBody Cliente Cliente) {

    Mono<Cliente> monoBody = Mono.just(Cliente);
    Mono<Cliente> monoBD = service.listarPorId(id);

    return monoBD
        .zipWith(monoBody, (bd, cl) -> {
          bd.setId(id);
          bd.setNombres(cl.getNombres());
          bd.setApellidos(cl.getApellidos());
          bd.setFechaNac(cl.getFechaNac());
          bd.setUrlFoto(cl.getUrlFoto());
          return bd;
        })
        .flatMap(service::modificar) //bd -> service.modificar(bd)
        .map(pl -> ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(pl))
        .defaultIfEmpty(new ResponseEntity<Cliente>(HttpStatus.NOT_FOUND));
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
  public Mono<EntityModel<Cliente>> listarHateoasPorId(@PathVariable("id") String id) {
    //localhost:8080/clientes/60779cc08e34kgjas0434
    Mono<Link> link1 = linkTo(methodOn(ClienteController.class).listarPorId(id)).withSelfRel()
        .toMono(); //De esta forma se construye el link
    //Se le pasa la referencia al método y se construye una referencia
    //al resultado
    Mono<Link> link2 = linkTo(methodOn(ClienteController.class).listarPorId(id)).withSelfRel()
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
  public Mono<ResponseEntity<PageSupport<Cliente>>> listarPageable(
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

  @PostMapping("/v1/subir/{id}")
  public Mono<ResponseEntity<Cliente>> subirV1(
      @PathVariable String id,
      @RequestPart FilePart file) //part para archivos multimedia
      throws IOException {
    Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
        "cloud_name", "ddkijjda2",
        "api_key", "147864941245156",
        "api_secret", "5d7k_ieYrt_gGdF6fgQbISB0x10"));
    File f = Files.createTempFile("temp", file.filename()).toFile();
    return file.transferTo(f)
        .then(service.listarPorId(id)
            .flatMap(c -> {
              Map response;
              try { //Como la librería cloudinary no soporta reactivo, se debe crear este bloque try catch para realizar el proceso.
                response = cloudinary.uploader().upload(f, ObjectUtils.asMap("resource_type",
                    "auto")); //Hay 4 tipos: image, video, raw y auto (automatico detecta el tipo automaticamente)
                JSONObject json = new JSONObject(
                    response); //Este json Object es propio de Cloudinary
                String url = json.getString("url");
                c.setUrlFoto(url);
              } catch (IOException e) {
                e.printStackTrace();
              }
              return service.modificar(c).then(Mono.just(ResponseEntity.ok().body(c)));
            })
            .defaultIfEmpty(ResponseEntity.notFound().build())
        );
  }

  @PostMapping("/v2/subir/{id}")
  public Mono<ResponseEntity<Cliente>> subirV2(
      @PathVariable String id,
      @RequestPart FilePart file) //part para archivos multimedia
   {
    Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
        "cloud_name", "ddkijjda2",
        "api_key", "147864941245156",
        "api_secret", "5d7k_ieYrt_gGdF6fgQbISB0x10"));

    return service.listarPorId(id)
        .flatMap(c -> {
          try {
            File f = Files.createTempFile("temp", file.filename()).toFile();
            file.transferTo(f).block(); //El block espera a que la transferencia esté lista
            Map response = cloudinary.uploader().upload(f, ObjectUtils.asMap("resource_type", "auto"));
            JSONObject json = new JSONObject(response);
            String url = json.getString("url");
            c.setUrlFoto(url);
            return service.modificar(c).thenReturn(ResponseEntity.ok().body(c));
          }catch (Exception e) {
              //throw new ArchivoException("Error al subir el archivo"); //- excepción personalziada.
          }
          return Mono.just(ResponseEntity.ok().body(c));
        })
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }


}
