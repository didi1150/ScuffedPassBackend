package me.didi.PWMBackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CryptoResponse {
	private String encryptionKey, privateKeyMaster, iv, salt;
}
