package me.didi.PWMBackend.model.authentication;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginRequest {

	private String email, password;

}