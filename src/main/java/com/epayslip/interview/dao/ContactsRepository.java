package com.epayslip.interview.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.epayslip.interview.vo.Contacts;

@Repository
public interface ContactsRepository extends JpaRepository<Contacts, Long>{

}
