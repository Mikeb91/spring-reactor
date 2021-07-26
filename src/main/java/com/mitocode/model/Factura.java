package com.mitocode.model;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "facturas")
public class Factura {
  @Id
  private String id;
  @Field(name = "descripcion")
  private String descripcion;
  @Field(name = "observacion")
  private String observacion;
  //Como enlazar con otra colección la información:
  @Field(name = "cliente")
  private Cliente cliente; //Incluye directamente la información de la clase. Por esta razón, esta forma es mejor.
  //private Integer idCliente; //Referencia al valor que enlaza al otro documento - Requiere doble consulta
  @Field(name = "items")
  private List<FacturaItem> items;

  private LocalDateTime creadoEn = LocalDateTime.now();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getObservacion() {
    return observacion;
  }

  public void setObservacion(String observacion) {
    this.observacion = observacion;
  }

  public Cliente getCliente() {
    return cliente;
  }

  public void setCliente(Cliente cliente) {
    this.cliente = cliente;
  }

  public List<FacturaItem> getItems() {
    return items;
  }

  public void setItems(List<FacturaItem> items) {
    this.items = items;
  }

  public LocalDateTime getCreadoEn() {
    return creadoEn;
  }

  public void setCreadoEn(LocalDateTime creadoEn) {
    this.creadoEn = creadoEn;
  }
}
