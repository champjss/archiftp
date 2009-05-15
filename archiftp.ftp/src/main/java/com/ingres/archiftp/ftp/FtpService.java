package com.ingres.archiftp.ftp;

import java.io.File;

public interface FtpService {
	void uploadFile(File file) throws RuntimeException;
}
