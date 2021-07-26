package com.mitocode.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.time.LocalDate;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
@JsonInclude(Include.NON_NULL) //Anotación para evitar que se muestre el null en consultas a DB en campos que no existen.
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
  private String apellidos;
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

  public String getApellidos() {
    return apellidos;
  }

  public void setApellidos(String apellidos) {
    this.apellidos = apellidos;
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
