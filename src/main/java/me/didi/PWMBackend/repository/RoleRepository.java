package me.didi.PWMBackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import me.didi.PWMBackend.model.table.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
	Role findByName(String name);
}
