package com.jakubadamek.robotemil;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.slf4j.LoggerFactory;

public class FileUtil {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(FileUtil.class);
	/**
	 * Executes a command and shows its output
	 * @param commandLine
	 * @param workingDir
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public static void exec(String commandLine, File workingDir) throws IOException, InterruptedException {
		logger.info(commandLine);
        Process process = Runtime.getRuntime().exec(commandLine, null, workingDir);
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line;
        while ((line = inputReader.readLine()) != null) {
            logger.info(line);
        }
        while ((line = errorReader.readLine()) != null) {
            System.err.println(line);
        }
        if (process.waitFor() != 0) {
            System.err.println("exit value = " + process.exitValue());
        }
    }

    /**
     * @param resourceName
     * @param outputPath
     * @param classLoader
     * @return true if target path is equal with source
     */
    public static boolean copyOut(String resourceName, File outputPath, ClassLoader classLoader) {
        logger.info("Checking contents " + resourceName + " against " + outputPath);

        byte [] source = null;
        byte [] dest = null;

        try {
            InputStream streamSrc = classLoader.getResourceAsStream(resourceName);
            if (streamSrc == null)
                throw new IOException("Specified resource " + resourceName + " does not exist");

            source = loadFromStream(streamSrc);

            // We do not need to successfully read destination file. We overwrite if error.
            try {
                if (outputPath.exists()) {
                    dest = loadFromStream(new FileInputStream(outputPath));
                }
            } catch (IOException e) {
                logger.info("", e);
            }

            if (dest == null || !Arrays.equals(source, dest)) {
                saveToFile(outputPath, source);
                logger.info(resourceName + " copied out to " + outputPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static byte [] loadFromStream(InputStream stream) throws IOException {
        ByteArrayOutputStream temp = new ByteArrayOutputStream();
        byte [] buf = new byte[128];
        int readed = 0;
        while ((readed = stream.read(buf)) != -1)
            temp.write(buf, 0, readed);
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return temp.toByteArray();
    }

    private static void saveToFile(File f, byte[] content) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            fos.write(content);
        } finally {
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException ie) {
                ie.printStackTrace();
            }
        }
    }
}
