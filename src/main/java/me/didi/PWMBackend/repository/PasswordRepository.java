package me.didi.PWMBackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import me.didi.PWMBackend.model.table.Password;

@Repository
public interface PasswordRepository extends JpaRepository<Password, Long> {

	List<Password> findByUserId(Long userID);

	@Query("SELECT websiteURL FROM Password p where p.user.id =:userId")
	List<String> findWebsites(@Param("userId") Long userID);

}
