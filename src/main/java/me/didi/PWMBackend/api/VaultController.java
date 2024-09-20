package me.didi.PWMBackend.api;

import java.util.List;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import me.didi.PWMBackend.model.AddPasswordRequest;
import me.didi.PWMBackend.model.EditPasswordRequest;
import me.didi.PWMBackend.model.table.Password;
import me.didi.PWMBackend.model.table.User;
import me.didi.PWMBackend.service.PasswordService;
import me.didi.PWMBackend.service.UserService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/vault")
public class VaultController {

	private final PasswordService passwordService;
	private final UserService userService;

	@GetMapping
	public ResponseEntity<List<Password>> getVaultData(Authentication authentication) {
		User user = (User) userService.loadUserByUsername(authentication.getName());
		Long id = user.getId();

		return new ResponseEntity<List<Password>>(passwordService.findAllByUserID(id), HttpStatusCode.valueOf(200));
	}

	@PostMapping
	public ResponseEntity<Password> addPasswordEntry(@RequestBody AddPasswordRequest addPasswordRequest,
			Authentication authentication) {
		User user = (User) userService.loadUserByUsername(authentication.getName());
		Long id = user.getId();

		Password pw = passwordService.createPassword(id,
				Password.builder().email(addPasswordRequest.getEmail()).iv(addPasswordRequest.getIv())
						.password(addPasswordRequest.getPassword()).user(user)
						.websiteURL(addPasswordRequest.getWebsite()).build());
		return new ResponseEntity<Password>(pw, HttpStatusCode.valueOf(200));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deletePasswordEntry(@PathVariable("id") String id, Authentication authentication) {
		if (authentication == null)
			return new ResponseEntity<String>(HttpStatusCode.valueOf(403));
		passwordService.deleteByID(Long.valueOf(id));
		return ResponseEntity.ok("Deletion successful");
	}

	@PatchMapping
	public ResponseEntity<String> editPasswordEntry(@RequestBody EditPasswordRequest request,
			Authentication authentication) {
		if (authentication == null)
			return new ResponseEntity<String>(HttpStatusCode.valueOf(403));
		User user = (User) userService.loadUserByUsername(authentication.getName());
		Long id = user.getId();
		passwordService.updatePassword(id, Long.valueOf(request.getId()), request.getPassword(), request.getIv());
		return ResponseEntity.ok("Edit successful");
	}

}
