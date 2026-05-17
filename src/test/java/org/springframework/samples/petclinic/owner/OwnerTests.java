package org.springframework.samples.petclinic.owner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OwnerTests {

	@Mock
	private Pet petWithChronicIllness;
	@Mock
	private Pet petWithoutChronicIllness;

	@Test
	@DisplayName("getPetsWithChronicIllnesses returns only pets with chronic illnesses")
	void shouldReturnPetsWithChronicIllnesses() {
		Owner owner = new Owner();
		when(petWithChronicIllness.hasChronicIllness()).thenReturn(true);
		when(petWithoutChronicIllness.hasChronicIllness()).thenReturn(false);
		owner.addPet(petWithChronicIllness);
		owner.addPet(petWithoutChronicIllness);

		List<Pet> result = owner.getPetsWithChronicIllnesses();

		assertThat(result).containsExactly(petWithChronicIllness);
	}

	@Test
	@DisplayName("getPetsWithChronicIllnesses returns empty list when no pets have chronic illness")
	void shouldReturnEmptyListWhenNoChronicIllnesses() {
		Owner owner = new Owner();
		when(petWithoutChronicIllness.hasChronicIllness()).thenReturn(false);
		owner.addPet(petWithoutChronicIllness);

		List<Pet> result = owner.getPetsWithChronicIllnesses();

		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("getPetsWithChronicIllnesses returns all pets when all have chronic illness")
	void shouldReturnAllPetsWhenAllHaveChronicIllness() {
		Owner owner = new Owner();
		when(petWithChronicIllness.hasChronicIllness()).thenReturn(true);
		when(petWithoutChronicIllness.hasChronicIllness()).thenReturn(true);
		owner.addPet(petWithChronicIllness);
		owner.addPet(petWithoutChronicIllness);

		List<Pet> result = owner.getPetsWithChronicIllnesses();

		assertThat(result).containsExactlyInAnyOrder(petWithChronicIllness, petWithoutChronicIllness);
	}

	@Test
	@DisplayName("addVisit throws IllegalArgumentException when petId is null")
	void shouldThrowExceptionOnNullPetId() {
		Owner owner = new Owner();

		assertThrows(IllegalArgumentException.class, () -> owner.addVisit(null, new Visit()));
	}
}