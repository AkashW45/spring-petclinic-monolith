package org.springframework.samples.petclinic.owner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OwnerTests {

    @Mock
    private Pet chronicPet;

    @Mock
    private Pet healthyPet;

    @Mock
    private Pet throwingPet;

    @InjectMocks
    private Owner owner;

    // --- Happy path ---

    @Test
    @DisplayName("getPetsWithChronicIllnesses returns only pets with chronic disease")
    void shouldReturnOnlyPetsWithChronicIllness() {
        owner.getPets().add(chronicPet);
        owner.getPets().add(healthyPet);
        when(chronicPet.hasChronicIllness()).thenReturn(true);
        when(healthyPet.hasChronicIllness()).thenReturn(false);

        List<Pet> result = owner.getPetsWithChronicIllnesses();

        assertEquals(1, result.size());
        assertTrue(result.contains(chronicPet));
        assertFalse(result.contains(healthyPet));
    }

    // --- Edge case: owner has no pets ---

    @Test
    @DisplayName("getPetsWithChronicIllnesses returns empty list when owner has no pets")
    void shouldReturnEmptyListWhenNoPets() {
        assertTrue(owner.getPetsWithChronicIllnesses().isEmpty());
    }

    // --- Edge case: all pets are healthy ---

    @Test
    @DisplayName("getPetsWithChronicIllnesses returns empty list when no pet has chronic illness")
    void shouldReturnEmptyListWhenAllPetsHealthy() {
        owner.getPets().add(healthyPet);
        owner.getPets().add(chronicPet);
        when(healthyPet.hasChronicIllness()).thenReturn(false);
        when(chronicPet.hasChronicIllness()).thenReturn(false);

        List<Pet> result = owner.getPetsWithChronicIllnesses();

        assertTrue(result.isEmpty());
    }

    // --- Edge case: null pet in list ---

    @Test
    @DisplayName("getPetsWithChronicIllnesses throws NullPointerException when pet list contains null")
    void shouldThrowNullPointerExceptionWhenPetListContainsNull() {
        owner.getPets().add(null);
        owner.getPets().add(chronicPet);
        when(chronicPet.hasChronicIllness()).thenReturn(true);

        assertThrows(NullPointerException.class,
                () -> owner.getPetsWithChronicIllnesses());
    }

    // --- Error path: hasChronicIllness() throws exception ---

    @Test
    @DisplayName("getPetsWithChronicIllnesses propagates exception from Pet.hasChronicIllness")
    void shouldPropagateExceptionFromHasChronicIllness() {
        owner.getPets().add(throwingPet);
        when(throwingPet.hasChronicIllness()).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class,
                () -> owner.getPetsWithChronicIllnesses());
    }
}