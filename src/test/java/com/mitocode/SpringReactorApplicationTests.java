package com.mitocode;

import com.mitocode.model.Plato;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SpringReactorApplicationTests {

  @Autowired
  private WebTestClient cliente;
  @Test
  void contextLoads() {
    cliente.get()
        .uri("/platos")
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBodyList(Plato.class)
        .hasSize(4);
  }

  //Técnica de clase equivalente
  @Test
  void registrarTest() {
    Plato plato = new Plato();
    plato.setNombre("pachamanca");
    plato.setPrecio(20.0);

    cliente.post()
        .uri("/platos")
        .body(Mono.just(plato), Plato.class)
        .exchange()
        .expectStatus().isCreated()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.nombre").isNotEmpty()
        .jsonPath("$.precio").isNumber();
  }

  @Test
  void modificarTest() {
    Plato plato = new Plato();
    plato.setId("60fa2ce25eb4c66cc8825c67");
    plato.setNombre("arroz pollo");
    plato.setPrecio(25.0);
    plato.setEstado(true);

    cliente.put()
        .uri("/platos/" + plato.getId())
        .body(Mono.just(plato), Plato.class)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.nombre").isNotEmpty()
        .jsonPath("$.precio").isNumber();
  }

  @Test
  void eliminarTest() {
    Plato plato = new Plato();
    plato.setId("60fa2ce25eb4c66cc8825c67");

    cliente.delete()
        .uri("/platos/" + plato.getId())
        .exchange()
        .expectStatus().isNoContent();
  }

}
