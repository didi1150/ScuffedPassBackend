package me.didi.PWMBackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddPasswordRequest {

	private String website, email, password, iv;
	
}
