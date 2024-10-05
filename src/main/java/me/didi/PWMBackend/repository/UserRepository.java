package me.didi.PWMBackend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import me.didi.PWMBackend.model.table.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	@Modifying
	@Transactional
	@Query("DELETE FROM User u WHERE u.enabled = false")
	int deleteNonVerifiedUsers();

}
