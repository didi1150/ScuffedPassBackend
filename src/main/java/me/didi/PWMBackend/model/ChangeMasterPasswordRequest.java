package me.didi.PWMBackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChangeMasterPasswordRequest {
	private String currentMasterPassword, newMasterPassword;
}
