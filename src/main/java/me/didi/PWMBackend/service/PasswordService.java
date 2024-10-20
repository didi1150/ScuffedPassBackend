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

	public Password updatePassword(Long userID, Long passwordID, String data, String iv, String website, String email) {
		Password pw = passwordRepository.findById(passwordID).get();
		if (!iv.isEmpty())
			pw.setIv(iv);
		if (!data.isEmpty())
			pw.setPassword(data);
		if (!website.isEmpty())
			pw.setWebsiteURL(website);
		if (!email.isEmpty())
			pw.setEmail(email);
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

	public void deleteAllByUserId(Long id) {
		passwordRepository.deleteAllByUserId(id);
	}

	public boolean isMasterPasswordCorrect(String email, String password) {
		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

		String encodedPassword = user.getPassword();
		String salt = cookingService.retrieveSalt(email);
		System.out.println(encodedPassword);

		if (!passwordEncoder.matches(salt + password, encodedPassword)) {
			System.out.println("Password doesn't match");
			return false;
		}
		return true;
	}

}
