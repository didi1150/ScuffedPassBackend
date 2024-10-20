package me.didi.PWMBackend.service;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import me.didi.PWMBackend.model.CryptoRequest;
import me.didi.PWMBackend.model.table.User;
import me.didi.PWMBackend.repository.UserRepository;

@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService {

	private final UserRepository userRepository;

	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	public User saveUser(User user) {
		return userRepository.save(user);
	}

	public User findByID(Long id) {
		return userRepository.findById(id).isPresent() ? userRepository.findById(id).get() : null;
	}

	public void deleteUser(Long id) {
		userRepository.deleteById(id);
	}

	public User updateUser(User user) {
		return userRepository.save(user);
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = findByEmail(username);
		return user;
	}

	public User findByEmail(String email) {
		return userRepository.findByEmail(email).get();
	}

	public int deleteDisabledUsers() {
		return userRepository.deleteNonVerifiedUsers();
	}

	public void enableUserEmail(String email) {
		User user = findByEmail(email);
		user.setEnabled(true);
		saveUser(user);
	}

	public void addCryptoDetails(CryptoRequest cryptoRequest, String email) {
		User user = findByEmail(email);
		if (user == null) {
			throw new UsernameNotFoundException("User not found: " + email);
		} else {
			if (user.getEncryptionKey() == null) {
				user.setPublicKey(cryptoRequest.getPublicKey());
				user.setIv(cryptoRequest.getIv());
				user.setPrivateKeyMaster(cryptoRequest.getPrivateKeyMaster());
				user.setPrivateKeyRecovery(cryptoRequest.getPrivateKeyRecovery());
				user.setEncryptionKey(cryptoRequest.getSymmetricKey());
				user.setFirstLogin(false);
				saveUser(user);
			}

		}
	}
}