package com.pessoal.galeria_ney.domain;

import com.pessoal.galeria_ney.infra.exception.RegraDeNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ObraTest {

    @Test
    @DisplayName("CT02 - Deve impedir a adição de mais de 3 tags em uma obra")
    void deveImpedirMaisDeTresTags() {
        Obra obra = new Obra();
        obra.getTags().add("Pintura");
        obra.getTags().add("Óleo");
        obra.getTags().add("Clássico");

        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class, () -> {
            obra.adicionarTag("Museu");
        });

        assertEquals("A obra já possui o limite máximo de 3 tags", ex.getMessage());
    }
}