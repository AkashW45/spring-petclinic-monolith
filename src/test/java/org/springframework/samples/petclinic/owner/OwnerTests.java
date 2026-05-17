package org.springframework.samples.petclinic.owner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OwnerTests {

    @Mock
    private Pet pet1, pet2, pet3;

    @Test
    void testGetPetsWithChronicIllnessesMixed() {
        // Arrange
        when(pet1.isNew()).thenReturn(true);
        when(pet2.isNew()).thenReturn(true);
        when(pet3.isNew()).thenReturn(true);
        when(pet1.hasChronicIllness()).thenReturn(true);
        when(pet2.hasChronicIllness()).thenReturn(false);
        when(pet3.hasChronicIllness()).thenReturn(true);

        Owner owner = new Owner();
        owner.addPet(pet1);
        owner.addPet(pet2);
        owner.addPet(pet3);

        // Act
        List<Pet> result = owner.getPetsWithChronicIllnesses();

        // Assert
        assertThat(result).containsExactlyInAnyOrder(pet1, pet3);
    }

    @Test
    void testGetPetsWithChronicIllnessesNoPets() {
        // Arrange
        Owner owner = new Owner();

        // Act
        List<Pet> result = owner.getPetsWithChronicIllnesses();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void testGetPetsWithChronicIllnessesAllChronic() {
        // Arrange
        when(pet1.isNew()).thenReturn(true);
        when(pet2.isNew()).thenReturn(true);
        when(pet1.hasChronicIllness()).thenReturn(true);
        when(pet2.hasChronicIllness()).thenReturn(true);

        Owner owner = new Owner();
        owner.addPet(pet1);
        owner.addPet(pet2);

        // Act
        List<Pet> result = owner.getPetsWithChronicIllnesses();

        // Assert
        assertThat(result).containsExactlyInAnyOrder(pet1, pet2);
    }

    @Test
    void testGetPetsWithChronicIllnessesNoneChronic() {
        // Arrange
        when(pet1.isNew()).thenReturn(true);
        when(pet2.isNew()).thenReturn(true);
        when(pet1.hasChronicIllness()).thenReturn(false);
        when(pet2.hasChronicIllness()).thenReturn(false);

        Owner owner = new Owner();
        owner.addPet(pet1);
        owner.addPet(pet2);

        // Act
        List<Pet> result = owner.getPetsWithChronicIllnesses();

        // Assert
        assertThat(result).isEmpty();
    }
}