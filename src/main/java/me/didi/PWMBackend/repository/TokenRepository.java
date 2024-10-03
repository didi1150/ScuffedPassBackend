package me.didi.PWMBackend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import me.didi.PWMBackend.model.table.Token;

public interface TokenRepository extends JpaRepository<Token, Long> {
	@Query(value = "select t from tokens t inner join User u on t.user.id = u.id where u.id = :id and (t.expired = false or t.revoked = false)")
	List<Token> findAllValidTokenByUser(Long id);

	Optional<Token> findByToken(String token);

	@Modifying
	@Transactional
	@Query("DELETE FROM tokens t WHERE t.revoked = true AND t.expired = true")
	int deleteRevokedAndExpiredTokens();
}
