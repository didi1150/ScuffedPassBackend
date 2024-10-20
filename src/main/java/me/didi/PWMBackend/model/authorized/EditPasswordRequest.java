package me.didi.PWMBackend.model.authorized;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EditPasswordRequest {

	private String password, iv, id, website, email;

}
