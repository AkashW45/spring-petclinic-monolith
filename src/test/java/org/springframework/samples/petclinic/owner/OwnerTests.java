package org.springframework.samples.petclinic.owner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OwnerTests {

    @Test
    void getPetsWithChronicIllnesses_returnsOnlyPetsWithChronicIllness() {
        Owner owner = new Owner();
        Pet healthyPet = mock(Pet.class);
        when(healthyPet.isNew()).thenReturn(true);
        Pet chronicPet = mock(Pet.class);
        when(chronicPet.isNew()).thenReturn(true);
        when(chronicPet.hasChronicIllness()).thenReturn(true);
        owner.addPet(healthyPet);
        owner.addPet(chronicPet);

        List<Pet> result = owner.getPetsWithChronicIllnesses();

        assertThat(result).containsExactly(chronicPet);
    }

    @Test
    void getPetsWithChronicIllnesses_withNoPets_returnsEmptyList() {
        Owner owner = new Owner();

        List<Pet> result = owner.getPetsWithChronicIllnesses();

        assertThat(result).isEmpty();
    }

    @Test
    void getPetsWithChronicIllnesses_whenAllPetsHaveChronicIllness_returnsAll() {
        Owner owner = new Owner();
        Pet pet1 = mock(Pet.class);
        when(pet1.isNew()).thenReturn(true);
        when(pet1.hasChronicIllness()).thenReturn(true);
        Pet pet2 = mock(Pet.class);
        when(pet2.isNew()).thenReturn(true);
        when(pet2.hasChronicIllness()).thenReturn(true);
        owner.addPet(pet1);
        owner.addPet(pet2);

        List<Pet> result = owner.getPetsWithChronicIllnesses();

        assertThat(result).containsExactly(pet1, pet2);
    }

    @Test
    void getPetsWithChronicIllnesses_whenNoPetsHaveChronicIllness_returnsEmptyList() {
        Owner owner = new Owner();
        Pet pet1 = mock(Pet.class);
        when(pet1.isNew()).thenReturn(true);
        Pet pet2 = mock(Pet.class);
        when(pet2.isNew()).thenReturn(true);
        owner.addPet(pet1);
        owner.addPet(pet2);

        List<Pet> result = owner.getPetsWithChronicIllnesses();

        assertThat(result).isEmpty();
    }

    @Test
    void getPetsWithChronicIllnesses_withNullPet_throwsNullPointerException() {
        Owner owner = new Owner();
        List<Pet> petsWithNull = new ArrayList<>();
        petsWithNull.add(null);
        ReflectionTestUtils.setField(owner, "pets", petsWithNull);

        assertThatThrownBy(() -> owner.getPetsWithChronicIllnesses())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void getPetsWithChronicIllnesses_whenHasChronicIllnessThrowsException_propagatesException() {
        Owner owner = new Owner();
        Pet pet = mock(Pet.class);
        when(pet.isNew()).thenReturn(true);
        when(pet.hasChronicIllness()).thenThrow(new RuntimeException("Test exception"));
        owner.addPet(pet);

        assertThatThrownBy(() -> owner.getPetsWithChronicIllnesses())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Test exception");
    }
}