package org.springframework.samples.petclinic.owner;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VisitController.class)
class VisitControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OwnerRepository owners;

    private Owner owner;
    private Pet pet;

    @BeforeEach
    void setUp() {
        owner = new Owner();
        owner.setId(1);
        pet = new Pet();
        pet.setId(2);
        owner.addPet(pet);
    }

    @Test
    void getVisitForm_ValidOwnerAndPet_ReturnsFormWithModel() throws Exception {
        when(owners.findById(1)).thenReturn(Optional.of(owner));

        mockMvc.perform(get("/owners/1/pets/2/visits/new"))
               .andExpect(status().isOk())
               .andExpect(view().name("pets/createOrUpdateVisitForm"))
               .andExpect(model().attributeExists("owner"))
               .andExpect(model().attributeExists("pet"))
               .andExpect(model().attributeExists("visit"))
               .andExpect(model().attribute("hasChronicIllness", false)); // pet has no illnesses by default
    }

    @Test
    void getVisitForm_PetHasChronicIllness_ModelAttributeTrue() throws Exception {
        pet.setChronicIllnesses(Collections.singletonList(new ChronicIllness())); // assume a valid illness
        when(owners.findById(1)).thenReturn(Optional.of(owner));

        mockMvc.perform(get("/owners/1/pets/2/visits/new"))
               .andExpect(status().isOk())
               .andExpect(model().attribute("hasChronicIllness", true));
    }

    @Test
    void getVisitForm_PetHasNoChronicIllness_ModelAttributeFalse() throws Exception {
        pet.setChronicIllnesses(Collections.emptyList());
        when(owners.findById(1)).thenReturn(Optional.of(owner));

        mockMvc.perform(get("/owners/1/pets/2/visits/new"))
               .andExpect(status().isOk())
               .andExpect(model().attribute("hasChronicIllness", false));
    }

    @Test
    void getVisitForm_OwnerNotFound_ThrowsException() throws Exception {
        when(owners.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/owners/99/pets/2/visits/new"))
               .andExpect(status().is5xxServerError())
               .andExpect(result -> {
                   Exception ex = result.getResolvedException();
                   assert ex instanceof IllegalArgumentException;
                   assert ex.getMessage().contains("Owner not found with id: 99");
               });
    }

    @Test
    void getVisitForm_PetNotFound_ThrowsException() throws Exception {
        when(owners.findById(1)).thenReturn(Optional.of(owner));
        // pet with id 99 does not exist in owner's pet list
        owner.getPet(99); // owner.getPet returns null for unknown id

        mockMvc.perform(get("/owners/1/pets/99/visits/new"))
               .andExpect(status().is5xxServerError())
               .andExpect(result -> {
                   Exception ex = result.getResolvedException();
                   assert ex instanceof IllegalArgumentException;
                   assert ex.getMessage().contains("Pet with id 99 not found");
               });
    }
}