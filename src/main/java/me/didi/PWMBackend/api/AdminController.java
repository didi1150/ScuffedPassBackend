package me.didi.PWMBackend.api;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import me.didi.PWMBackend.model.ReducedUserInformation;
import me.didi.PWMBackend.service.AdminService;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

	private final AdminService adminService;

	@GetMapping("/users")
	public List<ReducedUserInformation> getUsers(Authentication authentication) {
		return adminService.getUserInformation();
	}

	@DeleteMapping("/delete")
	public void deleteUser(@RequestParam Long id) {
		adminService.deleteUser(id);
	}

}
