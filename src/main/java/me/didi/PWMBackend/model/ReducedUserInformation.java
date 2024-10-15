package me.didi.PWMBackend.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReducedUserInformation {

	private Long id;
	private String email;
	private boolean enabled;
	private boolean locked;
	private LocalDateTime createdAt;

}
