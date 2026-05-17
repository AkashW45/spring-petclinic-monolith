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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PetController.class)
class PetControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OwnerRepository owners;

    @MockBean
    private PetTypeRepository types;

    @MockBean
    private ChronicIllnessRepository chronicIllnessRepository;

    @Test
    void assignChronicIllness_shouldSaveAndRedirect() throws Exception {
        int ownerId = 1;
        int petId = 1;
        Owner owner = mock(Owner.class);
        Pet pet = new Pet();
        pet.setId(petId);
        pet.setName("Buddy");

        when(types.findPetTypes()).thenReturn(Collections.emptyList());
        when(owners.findById(ownerId)).thenReturn(Optional.of(owner));
        when(owner.getPet(petId)).thenReturn(pet);

        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/chronic-illnesses", ownerId, petId)
                .param("illnessName", "Diabetes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/owners/" + ownerId + "/pets/" + petId))
                .andExpect(flash().attribute("message", "Chronic illness assigned successfully"));

        verify(chronicIllnessRepository).save(any(ChronicIllness.class));
    }

    @Test
    void assignChronicIllness_ownerNotFound() throws Exception {
        int ownerId = 2;
        int petId = 1;

        when(types.findPetTypes()).thenReturn(Collections.emptyList());
        when(owners.findById(ownerId)).thenReturn(Optional.empty());

        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/chronic-illnesses", ownerId, petId)
                .param("illnessName", "Asthma"))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> result.getResolvedException()
                        .getClass().equals(IllegalArgumentException.class))
                .andExpect(result -> result.getResolvedException().getMessage()
                        .contains("Owner not found with id: " + ownerId));
    }

    @Test
    void assignChronicIllness_petNotFound() throws Exception {
        int ownerId = 1;
        int petId = 99;
        Owner owner = mock(Owner.class);

        when(types.findPetTypes()).thenReturn(Collections.emptyList());
        when(owners.findById(ownerId)).thenReturn(Optional.of(owner));
        when(owner.getPet(petId)).thenReturn(null);

        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/chronic-illnesses", ownerId, petId)
                .param("illnessName", "Allergy"))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> result.getResolvedException()
                        .getClass().equals(IllegalArgumentException.class))
                .andExpect(result -> result.getResolvedException().getMessage()
                        .contains("Pet not found with id: " + petId));
    }

    @Test
    void assignChronicIllness_missingIllnessName() throws Exception {
        int ownerId = 1;
        int petId = 1;
        Owner owner = mock(Owner.class);

        when(types.findPetTypes()).thenReturn(Collections.emptyList());
        when(owners.findById(ownerId)).thenReturn(Optional.of(owner));
        // pet existence doesn't matter because request param validation happens first

        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/chronic-illnesses", ownerId, petId))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void assignChronicIllness_emptyIllnessName() throws Exception {
        int ownerId = 1;
        int petId = 1;
        Owner owner = mock(Owner.class);
        Pet pet = new Pet();
        pet.setId(petId);
        pet.setName("Buddy");

        when(types.findPetTypes()).thenReturn(Collections.emptyList());
        when(owners.findById(ownerId)).thenReturn(Optional.of(owner));
        when(owner.getPet(petId)).thenReturn(pet);

        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/chronic-illnesses", ownerId, petId)
                .param("illnessName", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/owners/" + ownerId + "/pets/" + petId))
                .andExpect(flash().attribute("message", "Chronic illness assigned successfully"));

        verify(chronicIllnessRepository).save(any(ChronicIllness.class));
    }

}