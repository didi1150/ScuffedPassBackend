package me.didi.PWMBackend.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import me.didi.PWMBackend.model.table.PatchNotes;
import me.didi.PWMBackend.repository.PatchNotesRepository;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/version")
public class VersionController {

	private final PatchNotesRepository patchNotesRepository;

	@GetMapping
	public String getAppVersion() {
		return patchNotesRepository.getLatestPatchNotes().getVersion();
	}

	@GetMapping("/details")
	public PatchNotes getPatchNotes() {
		return patchNotesRepository.getLatestPatchNotes();
	}

}
