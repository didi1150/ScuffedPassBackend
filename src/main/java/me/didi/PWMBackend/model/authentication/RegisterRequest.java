package me.didi.PWMBackend.model.authentication;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterRequest {
	private String email, password, salt;
}
