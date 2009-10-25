package com.jakubadamek.robotemil;

import java.io.File;
import java.io.IOException;

public class JavaServiceWrapper {

	private static App app;

	public static void setApp(App app2) {
		app = app2;
	}

	private static File wrapperDir() {
		return new File(new File(App.netxDir(), "java-service-wrapper"), "bin");
	}

	private static void batFile(String filename) {
		if(app != null) {
			app.showLog(filename);
		}
		try {
			FileUtil.exec("cmd /c " + filename, wrapperDir());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void init() {
		String jarName = "java-service-wrapper.jar";
		FileUtil.copyOut(jarName, new File(App.netxDir(), jarName), JavaServiceWrapper.class.getClassLoader());
		try {
			FileUtil.exec("jar -xf " + jarName, App.netxDir());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void start() {
		stop();
		init();
		batFile("InstallTestWrapper-NT.bat");
		batFile("StartTestWrapper-NT.bat");
		if(app != null) {
			app.showLog("TestWrapper started");
		}
	}

	public static void stop() {
		/* In this case, try to use current bat file to stop the service
		 * - otherwise probably it won't be possible to copy new files
		 */
		if(! new File(wrapperDir(), "StopTestWrapper-NT.bat").exists()) {
			init();
		}
		batFile("StopTestWrapper-NT.bat");
		batFile("UninstallTestWrapper-NT.bat");
		if(app != null) {
			app.showLog("TestWrapper stopped");
		}
	}
}
