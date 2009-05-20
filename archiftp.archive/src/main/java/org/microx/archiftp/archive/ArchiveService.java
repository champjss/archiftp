package org.microx.archiftp.archive;

import java.io.File;
import java.io.FileNotFoundException;

public interface ArchiveService {
	void moveFileToArchive(File file) throws FileNotFoundException, PermissionDeniedException;
}
