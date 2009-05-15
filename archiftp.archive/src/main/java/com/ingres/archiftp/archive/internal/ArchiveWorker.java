package com.ingres.archiftp.archive.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ArchiveWorker {

	private ArchiveProperties properties;
	private LogServiceWrapper logService;

	public ArchiveWorker(ArchiveProperties properties, LogServiceWrapper logService) {
		this.properties = properties;
		this.logService = logService;
	}

	public void moveFileToArchive(File file) {
		try {
			checkIsFileCanMove(file);
			checkIsArchiveAvailable();
			renameFile(file);
		} catch (Exception e) {
			String message = String.format(
					"Cannot move file '%s' to archive.", file.getAbsolutePath(), 
					this.properties.getArchivePath());
			throw new RuntimeException(message, e);
		}
	}

	private void renameFile(File file) {
		File destination = getDestinationFilenameInArchive(file);
		file.renameTo(destination);
	}

	private File getDestinationFilenameInArchive(File file) {
		String targetPath = getSubDirectoryNameByCurrentDate();
		String fileName = getStringOfAbsolutePath(targetPath, file.getName());
		return new File(fileName);
	}
	
	private String getSubDirectoryNameByCurrentDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		String targetPathStr = dateFormat.format(new Date());
		String targetPathAbsoluteStr = getStringOfAbsolutePath(
				this.properties.getArchivePath(), targetPathStr);
		
		File targetPath = new File(targetPathAbsoluteStr);
		if (!targetPath.exists()) {
			boolean mkdirResult = targetPath.mkdir();
			if (!mkdirResult) {
				String message = String.format(
						"Create subdirectory '%s' failed.", targetPathAbsoluteStr);
				logService.debug(message);
				throw new RuntimeException(message);
			}
			logService.debug(String.format("Subdirectory '%s' is created.", targetPathAbsoluteStr));
		}
		
		return targetPath.getAbsolutePath();
	}
	
	private String getStringOfAbsolutePath(String directory, String file) {
		return String.format("%s%s%s", directory, File.separator, file);
	}

	private void checkIsFileCanMove(File file) throws FileNotFoundException {
		if (!file.exists()) {
			String message = String.format("File '%s' is not found.",
					file.getAbsolutePath());
			logService.debug(message);
			throw new FileNotFoundException(message);
		}

		File parentDir = file.getParentFile();
		if (!parentDir.canWrite()) {
			String message = String.format(
					"Not have permission to write into directory '%s'.", 
					parentDir.getAbsolutePath());
			logService.debug(message);
			throw new RuntimeException(message);
		}
	}

	private void checkIsArchiveAvailable() throws FileNotFoundException {
		File archive = new File(this.properties.getArchivePath());
		if (!archive.exists()) {
			String message = String.format(
					"Archive path '%s' not found.", archive.getAbsolutePath());
			logService.debug(message);
			throw new FileNotFoundException(message);
		}
	}
}
