/*
 * Copyright 2012-2019 the original author or authors.
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

import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.samples.petclinic.visit.Visit;
import org.springframework.samples.petclinic.visit.VisitRepository;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Map;

@Controller
class VisitController {

	private final VisitRepository visits;

	private final PetRepository pets;

	private final VetRepository vets;

	private final OwnerRepository owners;

	public VisitController(VisitRepository visits, PetRepository pets, VetRepository vets, OwnerRepository clinicService) {
		this.visits = visits;
		this.pets = pets;
		this.vets = vets;
		this.owners = clinicService;
	}

	/**
	 * Called before each and every @RequestMapping annotated method. 2 goals: - Make sure
	 * we always have fresh data - Since we do not use the session scope, make sure that
	 * Pet object always has an id (Even though id is not part of the form fields)
	 *
	 * @param petId
	 * @return Pet
	 */
	@ModelAttribute("visit")
	public Visit loadPetWithVisit(@PathVariable("petId") int petId, Map<String, Object> model) {
		Pet pet = this.pets.findById(petId);
		pet.setVisitsInternal(this.visits.findByPetId(petId));
		model.put("pet", pet);
		Visit visit = new Visit();
		pet.addVisit(visit);
		return visit;
	}

	// Spring MVC calls method loadPetWithVisit(...) before initNewVisitForm is called
	@GetMapping("/owners/{ownerId}/pets/{petId}/visits/new")
	public String initNewVisitForm(@PathVariable("petId") int petId, @PathVariable("ownerId") int ownerId, Map<String, Object> model) {
		Pet pet = this.pets.findById(petId);
		model.put("pet", pet);
		Owner owner = this.owners.findById(ownerId);
		model.put("owner", owner);
		return "pets/createOrUpdateVisitForm";
	}

	// Spring MVC calls method loadPetWithVisit(...) before processNewVisitForm is called
	@PostMapping("/owners/{ownerId}/pets/{petId}/visits/new")
	public String processNewVisitForm(@Valid Visit visit, BindingResult result) {

		if (result.hasErrors()) {
			return "pets/createOrUpdateVisitForm";
		} else {
			visit.setStatus(1);
			this.visits.save(visit);
			return "redirect:/owners/{ownerId}";
		}
	}

	@ModelAttribute("vets")
	public Collection<Vet> vetTypes() {
		return this.vets.findVetTypes();
	}

// Added functional for change visits
	@PostMapping("/owners/{ownerId}/pets/{petId}/visits/{visitId}/edit")
	public String processEditVisitForm(@Valid Visit visit, @PathVariable("visitId") int visitId,
									   @RequestParam(value = "action") String action, BindingResult result) {

		if (result.hasErrors()) {
			return "pets/createOrUpdateVisitForm";
		} else {
			visit.setId(visitId);    //Need to edit, but not to create new copy of visit

			if (action.equals("edit")) {
				visit.setStatus(1);
			} else {
				visit.setStatus(0);
			}
			this.visits.save(visit);
			return "redirect:/owners/{ownerId}";
		}
	}

	// Added functional for change visits
	@GetMapping("/owners/{ownerId}/pets/{petId}/visits/{visitId}/edit")
	public String initEditVisitForm(@PathVariable("visitId") int visitId,
									@PathVariable("ownerId") int ownerId, Map<String, Object> model) {

		Owner owner = this.owners.findById(ownerId);
		model.put("owner", owner);
		Visit visit = this.visits.findById(visitId);
		model.put("visit", visit);
		return "pets/createOrUpdateVisitForm";
	}

	// Added functional for change visits
	@GetMapping("/owners/{ownerId}/pets/{petId}/visits/{visitId}/return")
	public String initReturnVisitForm(@PathVariable("visitId") int visitId) {

		Visit visit = this.visits.findById(visitId);
		visit.setStatus(1);
		this.visits.save(visit);
		return "redirect:/owners/{ownerId}";
	}
}
