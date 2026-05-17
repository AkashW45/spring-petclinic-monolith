/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(VisitController.class)
class VisitControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private OwnerRepository owners;

	@Test
	void testWhenPetHasChronicIllnessesModelContainsTrue() throws Exception {
		int ownerId = 1;
		int petId = 10;

		Owner owner = new Owner();
		Pet pet = new Pet();
		List<ChronicIllness> chronicIllnesses = new ArrayList<>();
		chronicIllnesses.add(new ChronicIllness());
		pet.setChronicIllnesses(chronicIllnesses);
		owner.addPet(pet);
		given(owners.findById(ownerId)).willReturn(Optional.of(owner));

		mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits/new", ownerId, petId))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdateVisitForm"))
			.andExpect(model().attributeExists("pet"))
			.andExpect(model().attributeExists("owner"))
			.andExpect(model().attribute("hasChronicIllness", true));
	}

	@Test
	void testWhenPetHasNoChronicIllnessesModelContainsFalse() throws Exception {
		int ownerId = 1;
		int petId = 10;

		Owner owner = new Owner();
		Pet pet = new Pet();
		pet.setChronicIllnesses(new ArrayList<>());
		owner.addPet(pet);
		given(owners.findById(ownerId)).willReturn(Optional.of(owner));

		mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits/new", ownerId, petId))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdateVisitForm"))
			.andExpect(model().attribute("hasChronicIllness", false));
	}

	@Test
	void testWhenOwnerNotFoundReturnsError() throws Exception {
		int ownerId = 99;
		int petId = 1;

		given(owners.findById(ownerId)).willReturn(Optional.empty());

		mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits/new", ownerId, petId))
			.andExpect(status().is5xxServerError());
	}

	@Test
	void testWhenPetNotFoundReturnsError() throws Exception {
		int ownerId = 1;
		int petId = 99;

		Owner owner = new Owner();
		given(owners.findById(ownerId)).willReturn(Optional.of(owner));
		// owner has no pet with id 99, getPet() returns null

		mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits/new", ownerId, petId))
			.andExpect(status().is5xxServerError());
	}

}