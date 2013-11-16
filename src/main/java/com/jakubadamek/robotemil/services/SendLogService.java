package com.jakubadamek.robotemil.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.io.IOUtils;

public class SendLogService {
	private static final String log = "1384414283.in";
	
	public static void main(String... args) throws IOException {
		new SendLogService().demarshallLog();
	}
	
	public void demarshallLog() throws IOException {
		GZIPInputStream gzipInputStream = 
				new GZIPInputStream(
						new Base64InputStream(
								new FileInputStream(new File(log))));
		FileOutputStream fos = new FileOutputStream(log + ".txt");
		try {
			IOUtils.copy(gzipInputStream, fos);		
		} finally {
			gzipInputStream.close();
			fos.close();
		}
	}
}
