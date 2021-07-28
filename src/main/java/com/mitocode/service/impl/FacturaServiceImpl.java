package com.mitocode.service.impl;

import com.mitocode.dto.FiltroDTO;
import com.mitocode.model.Cliente;
import com.mitocode.model.Factura;
import com.mitocode.repo.IClienteRepo;
import com.mitocode.repo.IFacturaRepo;
import com.mitocode.repo.IPlatoRepo;
import com.mitocode.service.IClienteService;
import com.mitocode.service.IFacturaService;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class FacturaServiceImpl extends CrudImpl<Factura, String> implements IFacturaService {

  @Autowired
  private IFacturaRepo repo;
  @Autowired
  private IClienteRepo clienteRepo;
  @Autowired
  private IPlatoRepo platoRepo;


  @Override
  protected ReactiveMongoRepository getRepo() {
    return repo;
  }

  @Override
  public Flux<Factura> obtenerFacturasPorFiltro(FiltroDTO filtro) {
    String criterio = filtro.getIdCliente() != null ? "C" : "O";
    if (criterio.equalsIgnoreCase("C")) {
      return repo.obtenerFacturasPorId(filtro.getIdCliente());
    } else {
      return repo.obtenerFacturasPorFecha(filtro.getFechaInicio(), filtro.getFechaFin());
    }
  }

  @Override
  public Mono<byte[]> generarReporte(String idFactura) {
    return repo.findById(idFactura) //Mono<Factura>
        //Obteniendo Cliente
        .flatMap(f -> {
          return Mono.just(f)
              .zipWith(clienteRepo.findById(f.getCliente().getId()), (fa, cl) -> {
                fa.setCliente(cl);
                return fa;
              });
        })
        //Obteniendo cada Plato
        .flatMap(f -> {
          return Flux.fromIterable(f.getItems()).flatMap(it -> {
            return platoRepo.findById(it.getPlato().getId())
                .map(p -> {
                  it.setPlato(p);
                  return it;
                });
          }).collectList().flatMap(list -> {
            //Seteando la nueva lista a factura
            f.setItems(list);
            return Mono.just(f);
          });
        })
        .map(f -> {
          InputStream stream;
          try {
            Map<String, Object> parametros = new HashMap<String, Object>();
            parametros.put("txt_cliente", f.getCliente().getNombres() + " " + f.getCliente().getApellidos());

            stream = getClass().getResourceAsStream("/facturas.jrxml");
            JasperReport report = JasperCompileManager.compileReport(stream);
            JasperPrint print = JasperFillManager
                .fillReport(report, parametros, new JRBeanCollectionDataSource(f.getItems()));
            return JasperExportManager.exportReportToPdf(print);
          } catch (Exception e) {
            e.printStackTrace();
          }
          return new byte[0];
        });

  }
}
