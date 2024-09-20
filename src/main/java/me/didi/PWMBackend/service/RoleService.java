package me.didi.PWMBackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import me.didi.PWMBackend.model.table.Role;
import me.didi.PWMBackend.repository.RoleRepository;

@Service
public class RoleService {

	@Autowired
	private RoleRepository repository;

	public Role findById(Long id) {
		return repository.getReferenceById(id);
	}

	public Role findByName(String name) {
		return repository.findByName(name);
	}

	public void saveRole(String name) {
		Role role = new Role();
		role.setName(name);
		repository.save(role);
	}

}
