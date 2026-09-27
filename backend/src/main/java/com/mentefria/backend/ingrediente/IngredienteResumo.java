package com.mentefria.backend.ingrediente;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IngredienteResumo(int id, String nome, @JsonProperty("unidade_medida") String unidadeMedida) {}