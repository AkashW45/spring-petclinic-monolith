package org.springframework.samples.petclinic.owner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class VisitControllerTests {

    @Mock
    private OwnerRepository owners;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        VisitController controller = new VisitController(owners);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testInitNewVisitForm_HappyPath_ShouldReturnFormViewAndSetChronicIllnessTrue() throws Exception {
        Owner owner = new Owner();
        owner.setId(1);
        Pet pet = createPetWithChronicIllnesses(new ChronicIllness("Diabetes"));
        owner.addPet(pet);
        when(owners.findById(1)).thenReturn(Optional.of(owner));

        mockMvc.perform(get("/owners/1/pets/2/visits/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("pets/createOrUpdateVisitForm"))
                .andExpect(model().attributeExists("pet", "owner", "visit"))
                .andExpect(model().attribute("hasChronicIllness", true));
    }

    @Test
    void testInitNewVisitForm_PetWithoutChronicIllnesses_SetsHasChronicIllnessFalse() throws Exception {
        Owner owner = new Owner();
        owner.setId(1);
        Pet pet = new Pet();
        pet.setId(2);
        owner.addPet(pet);
        when(owners.findById(1)).thenReturn(Optional.of(owner));

        mockMvc.perform(get("/owners/1/pets/2/visits/new"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("hasChronicIllness", false));
    }

    @Test
    void testInitNewVisitForm_OwnerNotFound_ShouldThrowException() {
        when(owners.findById(anyInt())).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                mockMvc.perform(get("/owners/1/pets/2/visits/new"))
        ).hasCauseInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("Owner not found with id: 1");
    }

    @Test
    void testInitNewVisitForm_PetNotFound_ShouldThrowException() {
        Owner owner = new Owner();
        owner.setId(1);
        Pet pet = new Pet();
        pet.setId(99);
        owner.addPet(pet);
        when(owners.findById(1)).thenReturn(Optional.of(owner));

        assertThatThrownBy(() ->
                mockMvc.perform(get("/owners/1/pets/2/visits/new"))
        ).hasCauseInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("Pet with id 2 not found for owner with id 1");
    }

    @Test
    void testProcessNewVisitForm_ValidForm_ShouldRedirectAndSave() throws Exception {
        Owner owner = new Owner();
        owner.setId(1);
        Pet pet = new Pet();
        pet.setId(2);
        owner.addPet(pet);
        when(owners.findById(1)).thenReturn(Optional.of(owner));

        mockMvc.perform(post("/owners/1/pets/2/visits/new")
                .param("description", "Annual checkup"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/owners/1"))
                .andExpect(view().name("redirect:/owners/{ownerId}"));

        verify(owners).save(any(Owner.class));
    }

    @Test
    void testProcessNewVisitForm_ValidationErrors_ShouldReturnFormView() throws Exception {
        Owner owner = new Owner();
        owner.setId(1);
        Pet pet = new Pet();
        pet.setId(2);
        owner.addPet(pet);
        when(owners.findById(1)).thenReturn(Optional.of(owner));

        mockMvc.perform(post("/owners/1/pets/2/visits/new")
                .param("description", "")) // empty description triggers validation error
                .andExpect(status().isOk())
                .andExpect(view().name("pets/createOrUpdateVisitForm"));
    }

    private Pet createPetWithChronicIllnesses(ChronicIllness... illnesses) {
        Pet pet = new Pet();
        pet.setId(2);
        Set<ChronicIllness> illnessSet = new HashSet<>();
        for (ChronicIllness ci : illnesses) {
            illnessSet.add(ci);
        }
        pet.getChronicIllnesses().addAll(illnessSet);
        return pet;
    }
}