package me.didi.PWMBackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EditPasswordRequest {

	private String password, iv, id, website, email;

}
