package me.didi.PWMBackend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import me.didi.PWMBackend.model.table.Role;
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
		if (user != null) {
			List<Role> roles = new ArrayList<>(user.getRoles());
			String[] rolesNames = new String[roles.size()];
			for (int i = 0; i < roles.size(); i++) {
				rolesNames[i] = roles.get(i).getName();
			}
			return User.builder().id(user.getId()).email(user.getEmail()).password(user.getPassword())
					.roles(user.getRoles()).enabled(user.isEnabled()).build();
		}
		return null;
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
}