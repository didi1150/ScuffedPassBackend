package me.didi.PWMBackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CryptoRequest {

	private String publicKey, privateKeyMaster, privateKeyRecovery, iv, symmetricKey;

}
