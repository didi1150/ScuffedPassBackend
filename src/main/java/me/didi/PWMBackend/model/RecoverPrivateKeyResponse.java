package me.didi.PWMBackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class RecoverPrivateKeyResponse {

	private String privateKeyRecovery, iv, salt;

}
