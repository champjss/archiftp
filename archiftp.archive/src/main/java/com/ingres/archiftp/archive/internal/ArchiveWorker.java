package com.ingres.archiftp.archive.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ArchiveWorker {

	private ArchiveProperties properties;

	public ArchiveWorker(ArchiveProperties properties) {
		this.properties = properties;
	}

	public void moveFileToArchive(File file) {
		try {
			checkIsFileCanMove(file);
			checkIsArchiveAvailable();
			renameFile(file);
		} catch (Throwable e) {
			throw new RuntimeException(String.format(
					"Cannot move file %s to archive path %s.", file.getAbsolutePath(), 
					this.properties.getArchivePath()), e);
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
			targetPath.mkdir();
		}
		
		return targetPath.getAbsolutePath();
	}
	
	private String getStringOfAbsolutePath(String directory, String file) {
		return String.format("%s%s%s", directory, File.separator, file);
	}

	private void checkIsFileCanMove(File file) throws FileNotFoundException {
		if (!file.exists()) {
			throw new FileNotFoundException(String.format("File %s not found.",
					file.getAbsolutePath()));
		}

		File parentDir = file.getParentFile();
		if (!parentDir.canWrite()) {
			throw new RuntimeException(String.format(
					"Not have permission to write into directory %s", 
					parentDir.getAbsolutePath()));
		}
	}

	private void checkIsArchiveAvailable() throws FileNotFoundException {
		File archive = new File(this.properties.getArchivePath());
		if (!archive.exists()) {
			throw new FileNotFoundException(String.format(
					"Archive path %s not found.", archive.getAbsolutePath()));
		}
	}
}
