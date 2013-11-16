<?php 

require_once 'config.php';

function listDir($dir) {
	$retval = array();
	if ($handle = opendir($dir)) {
		while (false !== ($entry = readdir($handle))) {
			if ($entry != "." && $entry != "..") {
				$retval[] = $entry;
			}
		}
		closedir($handle);
	}
	return $retval;
}

function cleanup($today) {
	global $log;
	global $mainDir;
	$log->LogInfo("Cleaning all directories older than " . ($today - 2));
	foreach(listDir($mainDir) as $subdir) {
		if($subdir < ($today - 2)) {			
			$log->LogInfo("About to delete dir " . $subdir . " with all files");
			$subdir2 = joinDir($mainDir, $subdir);
			foreach(listDir($subdir2) as $file) {
				unlink(joinDir($subdir2, $file));
			}
			rmdir($subdir2);
			$log->LogInfo("Deleted dir " . $subdir . " with all files");
		}
	}
}	

function postPrices() {
	global $log;
	global $mainDir;
	
	$crc = $_POST["crc"];
	$prices = $_POST["prices"];
	$today = $_POST["today"];

	$log->LogInfo("store.php called with POST crc=".$crc."&today=".$today."&date=".$_POST["date"]);

	$myFile = myFile($_POST);
    $path = myPath($_POST, $myFile);
	$subDir = joinDir($mainDir, $today);
	if(! is_dir($subDir)) {
		$log->LogInfo("About to create dir " . $subDir);
		$retval = mkdir($subDir);
		$log->LogInfo("Mkdir returned " . $retval);
	}
	
	$crc2 = md5($prices);
	if($crc == $crc2) {
		$log->LogInfo("CRC is OK. POST filename " . $path);	

		$fh = fopen($path, 'w') or die("can't open file");
		fwrite($fh, $prices);
		fclose($fh);
		
		echo "Saved";
	} else {
		echo "ERROR";
		$log->LogInfo("Incorrect CRC for " . $path. " " . $crc . " <> " . $crc2);	
	}
	
	cleanup($today);
}

postPrices();
?> 
