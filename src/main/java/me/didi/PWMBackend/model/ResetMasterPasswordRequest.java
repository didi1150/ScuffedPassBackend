package me.didi.PWMBackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetMasterPasswordRequest {
	private String iv, privateKeyMaster, privateKeyRecovery, password, salt, token;
}
