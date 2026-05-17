package org.springframework.samples.petclinic.owner;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test class for {@link VisitController}
 *
 * @author Spring PetClinic
 */
@WebMvcTest(VisitController.class)
class VisitControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OwnerRepository owners;

    // ---------------------------------------------------------------
    // Happy path tests
    // ---------------------------------------------------------------

    @Test
    void initNewVisitForm_ValidOwnerAndPet_ReturnsFormViewAndModelAttributes() throws Exception {
        int ownerId = 1;
        int petId = 2;
        Owner owner = new Owner();
        Pet pet = new Pet();
        pet.setId(petId);
        when(pet.getChronicIllnesses()).thenReturn(Collections.emptyList());
        owner.addPet(pet);
        when(owners.findById(ownerId)).thenReturn(Optional.of(owner));

        mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits/new", ownerId, petId))
                .andExpect(status().isOk())
                .andExpect(view().name("pets/createOrUpdateVisitForm"))
                .andExpect(model().attributeExists("owner", "pet", "visit", "hasChronicIllness"))
                .andExpect(model().attribute("pet", hasProperty("id", is(petId))))
                .andExpect(model().attribute("hasChronicIllness", is(false)))
                .andExpect(model().attribute("visit", notNullValue()));
    }

    @Test
    void initNewVisitForm_PetWithChronicIllness_HasChronicIllnessTrue() throws Exception {
        int ownerId = 1;
        int petId = 3;
        Owner owner = new Owner();
        Pet pet = new Pet();
        pet.setId(petId);
        // Simulate pet having a chronic illness (just a non-empty list)
        when(pet.getChronicIllnesses()).thenReturn(Collections.singletonList(new Object()));
        owner.addPet(pet);
        when(owners.findById(ownerId)).thenReturn(Optional.of(owner));

        mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits/new", ownerId, petId))
                .andExpect(status().isOk())
                .andExpect(view().name("pets/createOrUpdateVisitForm"))
                .andExpect(model().attribute("hasChronicIllness", is(true)));
    }

    @Test
    void processNewVisitForm_ValidVisit_SavesAndRedirects() throws Exception {
        int ownerId = 1;
        int petId = 2;
        Owner owner = new Owner();
        Pet pet = new Pet();
        pet.setId(petId);
        when(pet.getChronicIllnesses()).thenReturn(Collections.emptyList());
        owner.addPet(pet);
        when(owners.findById(ownerId)).thenReturn(Optional.of(owner));
        when(owners.save(any(Owner.class))).thenReturn(owner);

        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", ownerId, petId)
                .param("description", "regular checkup")
                .param("date", "2020-10-10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/owners/" + ownerId))
                .andExpect(flash().attribute("message", "Your visit has been booked"));

        verify(owners).save(owner);
        // verify that addVisit was called on the owner
        // note: due to the way owner is loaded and then the model used,
        // it's the same owner instance
        verify(owner).addVisit(petId, any(Visit.class));
    }

    // ---------------------------------------------------------------
    // Error path tests
    // ---------------------------------------------------------------

    @Test
    void initNewVisitForm_OwnerNotFound_ThrowsException() throws Exception {
        int ownerId = 99;
        int petId = 2;
        when(owners.findById(ownerId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits/new", ownerId, petId))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> result.getResolvedException().getClass().equals(IllegalArgumentException.class));
    }

    @Test
    void initNewVisitForm_PetNotFound_ThrowsException() throws Exception {
        int ownerId = 1;
        int petId = 999;
        Owner owner = new Owner();
        when(owners.findById(ownerId)).thenReturn(Optional.of(owner));

        mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits/new", ownerId, petId))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> result.getResolvedException().getClass().equals(IllegalArgumentException.class));
    }

    @Test
    void processNewVisitForm_ValidationErrors_RedisplayForm() throws Exception {
        int ownerId = 1;
        int petId = 2;
        Owner owner = new Owner();
        Pet pet = new Pet();
        pet.setId(petId);
        when(pet.getChronicIllnesses()).thenReturn(Collections.emptyList());
        owner.addPet(pet);
        when(owners.findById(ownerId)).thenReturn(Optional.of(owner));

        // Submit with invalid data (empty description triggers validation error assumed)
        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", ownerId, petId)
                .param("description", "")
                .param("date", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("pets/createOrUpdateVisitForm"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasErrors("visit"));

        verify(owners, never()).save(any(Owner.class));
        verify(owner, never()).addVisit(anyInt(), any(Visit.class));
    }

}