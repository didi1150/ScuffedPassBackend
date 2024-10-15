package me.didi.PWMBackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import me.didi.PWMBackend.model.table.PatchNotes;

public interface PatchNotesRepository extends JpaRepository<PatchNotes, Long>{

	@Query("select p from PatchNotes p where p.id = (select max(id) from PatchNotes)")
	PatchNotes getLatestPatchNotes();
	
}
