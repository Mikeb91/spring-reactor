package com.mitocode.model;

import java.time.LocalDate;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "clientes")
public class Cliente {
  @Id
  private String id;
  @NotNull
  @Size(min = 3)
  @Field(name = "nombres")
  private String nombres;
  @NotNull
  @Size(min = 3)
  @Field(name = "apellidos")
  private String appelidos;
  @NotNull
  @Field(name = "fechaNac")
  private LocalDate fechaNac;
  @Field(name = "urlFoto")
  private String urlFoto;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getNombres() {
    return nombres;
  }

  public void setNombres(String nombres) {
    this.nombres = nombres;
  }

  public String getAppelidos() {
    return appelidos;
  }

  public void setAppelidos(String appelidos) {
    this.appelidos = appelidos;
  }

  public LocalDate getFechaNac() {
    return fechaNac;
  }

  public void setFechaNac(LocalDate fechaNac) {
    this.fechaNac = fechaNac;
  }

  public String getUrlFoto() {
    return urlFoto;
  }

  public void setUrlFoto(String urlFoto) {
    this.urlFoto = urlFoto;
  }
}
