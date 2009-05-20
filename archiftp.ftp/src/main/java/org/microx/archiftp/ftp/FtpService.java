package org.microx.archiftp.ftp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface FtpService {
	void uploadFile(File file) throws IOException, FileNotFoundException;
}
