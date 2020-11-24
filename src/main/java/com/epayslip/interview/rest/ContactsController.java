package com.epayslip.interview.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.epayslip.interview.service.ContactsService;
import com.epayslip.interview.vo.Contacts;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
public class ContactsController {

	@Autowired
	private ContactsService contactsService;

	@RequestMapping(path = "/contacts", method = RequestMethod.GET)
	public ResponseEntity<?> getAllContact() {
		try {
			List<Contacts> contactList = contactsService.getAllContacts();
			return new ResponseEntity<List<Contacts>>(contactList, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(path = "/contact/{id}", method = RequestMethod.GET)
	public ResponseEntity<?> getContact(@PathVariable long id) {
		try {
			Contacts contact = contactsService.getContact(id);
			return new ResponseEntity<Contacts>(contact, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
	@RequestMapping(path = "/update-contact", method = RequestMethod.POST)
	public ResponseEntity<?> updateContact(@RequestBody Contacts contact) {
		try {
			Contacts updatedContact = contactsService.updateContact(contact);
			return new ResponseEntity<Contacts>(updatedContact, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
	@RequestMapping(path = "/delete-contact/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteContact(@PathVariable long id) {
		try {
			boolean isDeleted = contactsService.deleteContact(id);
			return new ResponseEntity<Boolean>(isDeleted, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(path = "/register-contact", method = RequestMethod.POST)
	public ResponseEntity<?> registerContact(@RequestBody Contacts contact) {
		try {
			Contacts registeredContact = contactsService.registerContact(contact);
			return new ResponseEntity<Contacts>(registeredContact, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
}
