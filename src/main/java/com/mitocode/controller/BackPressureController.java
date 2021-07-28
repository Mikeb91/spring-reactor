package com.mitocode.controller;

import java.time.Duration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/backpressure")
public class BackPressureController {

  @GetMapping("/buffer")
  public Flux<Integer> testContrapresion() {
    return Flux.range(1, 100)
        .log()
       // .limitRate(10) //Cada cuánto se realiza petición, forma en la que se drenan los elementos del flujo. (por defecto, primero 32 luego 24).
                       //en este caso se configura en 10, primer request pide 10 y luego cuando vuelva a tener 75% libre vuelve a pedir, poreso
                        //la siguiente petición es 8 (7.5 se aproxima a 8)
        //.limitRate(10, 0) //Aquí pide cuando se drene el 100%, ya q no tiene porcentaje de residuo para volver a solicitar
        .limitRate(10,8) //param2 -> cantidad a pedir en el request
        .delayElements(Duration.ofMillis(1));
  }

}
