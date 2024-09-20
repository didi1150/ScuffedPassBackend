package me.didi.PWMBackend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import me.didi.PWMBackend.model.table.Password;
import me.didi.PWMBackend.model.table.User;
import me.didi.PWMBackend.repository.PasswordRepository;
import me.didi.PWMBackend.repository.UserRepository;

@Service
public class PasswordService {

	private PasswordRepository passwordRepository;
	private UserRepository userRepository;

	public PasswordService(PasswordRepository passwordRepository, UserRepository userRepository) {
		this.passwordRepository = passwordRepository;
		this.userRepository = userRepository;
	}

	public Password updatePassword(Long userID, Long passwordID, String data, String iv) {
		Password pw = passwordRepository.findById(passwordID).get();
		pw.setIv(iv);
		pw.setPassword(data);

		return passwordRepository.save(pw);
	}

	public List<Password> findAllByUserID(Long id) {
		return passwordRepository.findByUserId(id);
	}

	public Password createPassword(Long userID, Password password) {
		User user = userRepository.findById(userID).orElseThrow(() -> new RuntimeException("User not found"));
		password.setUser(user);
		return passwordRepository.save(password);
	}

	public void deleteByID(Long id) {
		passwordRepository.deleteById(id);
	}

}
