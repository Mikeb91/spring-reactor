package com.mitocode.repo;

import com.mitocode.model.Factura;
import java.time.LocalDate;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface IFacturaRepo extends ReactiveMongoRepository<Factura, String> {

  //En forma tradicional se usa @Query("") -> incluso con bd NOSQL
  //Revisar los query en Robo3T

  @Query("{'cliente': {_id : ?0}}")
  Flux<Factura> obtenerFacturasPorId(String idCliente);

  @Query("{'creadoEn': { $gte: ?0, $lt: ?1}}")
  Flux<Factura> obtenerFacturasPorFecha(LocalDate fechaInicio, LocalDate fechaFin);


}
