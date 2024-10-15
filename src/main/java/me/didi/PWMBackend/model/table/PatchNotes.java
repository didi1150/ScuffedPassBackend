package me.didi.PWMBackend.model.table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import me.didi.PWMBackend.converters.StringListConverter;

@Data
@Entity
public class PatchNotes {

	@Id
	@GeneratedValue
	private Long id;

	private String version;

	@Column(name = "pushed_at")
	private LocalDateTime timestamp;

	@Convert(converter = StringListConverter.class)
	@Column(name = "notes", nullable = false)
	private List<String> notes;

	public PatchNotes(String version) {
		this.version = version;
		this.timestamp = LocalDateTime.now();
		this.notes = new ArrayList<String>();
	}

}
