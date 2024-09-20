package me.didi.PWMBackend.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import me.didi.PWMBackend.service.CookingService;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "api/auth/account/user")
public class UserController {

	private final CookingService cookingService;

	@GetMapping
	public ResponseEntity<String> getUserName(HttpServletRequest request, Authentication authentication) {
		
		return authentication.getName() == null ? new ResponseEntity<String>(null, null, 403)
				: ResponseEntity.ok(authentication.getName());
	}

	@GetMapping("/salt")
	public ResponseEntity<String> getSalt(Authentication authentication) {
		return authentication.getName() == null ? new ResponseEntity<String>(null, null, 403)
				: ResponseEntity.ok(cookingService.retrieveSalt(authentication.getName()));
	}
}
