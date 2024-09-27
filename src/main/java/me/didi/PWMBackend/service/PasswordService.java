package me.didi.PWMBackend.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import me.didi.PWMBackend.model.table.Password;
import me.didi.PWMBackend.model.table.User;
import me.didi.PWMBackend.repository.PasswordRepository;
import me.didi.PWMBackend.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class PasswordService {

	private final PasswordRepository passwordRepository;
	private final UserRepository userRepository;
	private final CookingService cookingService;
	private final PasswordEncoder passwordEncoder;

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

	public boolean isMasterPasswordCorrect(String email, String password) {
		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

		String salt = cookingService.retrieveSalt(user.getEmail());
		String encodedPassword = user.getPassword();
		if (!passwordEncoder.matches(salt + password, encodedPassword)) {
			return false;
		}
		return true;
	}

}
