package org.springframework.samples.petclinic.owner;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void testAssignChronicIllnessSuccess() throws Exception {
        int ownerId = 1;
        int petId = 10;
        String illnessName = "Diabetes";

        Owner owner = new Owner();
        owner.setId(ownerId);
        Pet pet = new Pet();
        pet.setId(petId);
        owner.addPet(pet);
        when(owners.findById(ownerId)).thenReturn(Optional.of(owner));
        // types.findPetTypes() may be called due to @ModelAttribute; return empty to avoid NPE
        when(types.findPetTypes()).thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders
                .post("/owners/{ownerId}/pets/{petId}/chronic-illnesses", ownerId, petId)
                .param("illnessName", illnessName))
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/owners/" + ownerId + "/pets/" + petId))
                .andExpect(MockMvcResultMatchers.flash().attribute("message", "Chronic illness assigned successfully"));

        verify(chronicIllnessRepository).save(any(ChronicIllness.class));
    }

    @Test
    void testAssignChronicIllnessOwnerNotFound() throws Exception {
        int ownerId = 1;
        int petId = 10;
        String illnessName = "Diabetes";

        when(owners.findById(ownerId)).thenReturn(Optional.empty());
        // types.findPetTypes() might be called before the handler method; prevent NPE
        when(types.findPetTypes()).thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders
                .post("/owners/{ownerId}/pets/{petId}/chronic-illnesses", ownerId, petId)
                .param("illnessName", illnessName))
                .andExpect(status().is5xxServerError()); // illegal argument leads to 500
    }

    @Test
    void testAssignChronicIllnessPetNotFound() throws Exception {
        int ownerId = 1;
        int petId = 10;
        String illnessName = "Diabetes";

        Owner owner = new Owner();
        owner.setId(ownerId);
        // do not add any pet, so getPet returns null
        when(owners.findById(ownerId)).thenReturn(Optional.of(owner));
        when(types.findPetTypes()).thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders
                .post("/owners/{ownerId}/pets/{petId}/chronic-illnesses", ownerId, petId)
                .param("illnessName", illnessName))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testAssignChronicIllnessMissingIllnessName() throws Exception {
        int ownerId = 1;
        int petId = 10;

        // Not providing the required request parameter 'illnessName'
        mockMvc.perform(MockMvcRequestBuilders
                .post("/owners/{ownerId}/pets/{petId}/chronic-illnesses", ownerId, petId))
                .andExpect(status().isBadRequest());
    }
}