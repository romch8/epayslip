package com.epayslip.interview.service;

import java.util.List;

import com.epayslip.interview.vo.Contacts;

public interface ContactsService {

	List<Contacts> getAllContacts();

	Contacts getContact(long id);

	Contacts updateContact(Contacts contact);

	boolean deleteContact(long id);

	Contacts registerContact(Contacts contact);

}
