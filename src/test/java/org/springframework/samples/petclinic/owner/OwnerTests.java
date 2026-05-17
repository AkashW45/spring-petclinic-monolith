package org.springframework.samples.petclinic.owner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OwnerTests {

	@Test
	void getPetsWithChronicIllnessesHappyPath() {
		Owner owner = new Owner();
		Pet healthy = mock(Pet.class);
		Pet sick1 = mock(Pet.class);
		Pet sick2 = mock(Pet.class);

		when(sick1.hasChronicIllness()).thenReturn(true);
		when(sick2.hasChronicIllness()).thenReturn(true);
		when(healthy.hasChronicIllness()).thenReturn(false);

		owner.getPets().add(healthy);
		owner.getPets().add(sick1);
		owner.getPets().add(sick2);

		List<Pet> result = owner.getPetsWithChronicIllnesses();

		assertEquals(2, result.size());
		assertTrue(result.contains(sick1));
		assertTrue(result.contains(sick2));
		assertFalse(result.contains(healthy));
	}

	@Test
	void getPetsWithChronicIllnessesWhenNoPets() {
		Owner owner = new Owner();
		List<Pet> result = owner.getPetsWithChronicIllnesses();
		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	@Test
	void getPetsWithChronicIllnessesWhenNoChronicIllnesses() {
		Owner owner = new Owner();
		Pet healthy1 = mock(Pet.class);
		Pet healthy2 = mock(Pet.class);

		when(healthy1.hasChronicIllness()).thenReturn(false);
		when(healthy2.hasChronicIllness()).thenReturn(false);

		owner.getPets().add(healthy1);
		owner.getPets().add(healthy2);

		List<Pet> result = owner.getPetsWithChronicIllnesses();

		assertTrue(result.isEmpty());
	}

	@Test
	void getPetsWithChronicIllnessesWhenAllHaveChronicIllnesses() {
		Owner owner = new Owner();
		Pet sick1 = mock(Pet.class);
		Pet sick2 = mock(Pet.class);

		when(sick1.hasChronicIllness()).thenReturn(true);
		when(sick2.hasChronicIllness()).thenReturn(true);

		owner.getPets().add(sick1);
		owner.getPets().add(sick2);

		List<Pet> result = owner.getPetsWithChronicIllnesses();

		assertEquals(2, result.size());
		assertTrue(result.contains(sick1));
		assertTrue(result.contains(sick2));
	}

	@Test
	void getPetsWithChronicIllnessesWhenHasChronicIllnessThrowsException() {
		Owner owner = new Owner();
		Pet faulty = mock(Pet.class);
		when(faulty.hasChronicIllness()).thenThrow(new RuntimeException("Database error"));

		owner.getPets().add(faulty);

		assertThrows(RuntimeException.class, () -> owner.getPetsWithChronicIllnesses());
	}
}