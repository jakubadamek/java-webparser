<?php

require_once 'config.php';

function postLog() {
	global $log;
	global $mainDir;
	
	$content = $_POST["content"];
	$today = $_POST["today"];
	
	if($log && $today) {	
		$subDir = joinDir($mainDir, $today);
		if(! is_dir($subDir)) {
			$log->LogInfo("About to create dir " . $subDir);
			$retval = mkdir($subDir);
			$log->LogInfo("Mkdir returned " . $retval);
		}
		$filename = joinDir($subDir, time().".log");

		$log->LogInfo("storeLog.php called with POST today=".$today."; filename=".$filename);

		$fh = fopen($filename, 'w') or die("can't open file");
		fwrite($fh, $content);
		fclose($fh);
		
		echo "Saved";	
	}
}

postLog();
?>