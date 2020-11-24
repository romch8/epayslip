package com.epayslip.interview.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.epayslip.interview.dao.ContactsRepository;
import com.epayslip.interview.vo.Contacts;

@Service
public class ContactsServiceImpl implements ContactsService{
	
	@Autowired
	private ContactsRepository contactsRepository;

	@Override
	public List<Contacts> getAllContacts() {
		return contactsRepository.findAll();
	}

	@Override
	public Contacts getContact(long id) {
		return contactsRepository.findById(id).get();
	}

	@Override
	public Contacts updateContact(Contacts contact) {
		return contactsRepository.save(contact);
	}

	@Override
	public boolean deleteContact(long id) {
		try {
			contactsRepository.deleteById(id);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public Contacts registerContact(Contacts contact) {
		Contacts savedContact = contactsRepository.save(contact);
		return savedContact;
	}

}
