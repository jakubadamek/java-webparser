<?php 

require_once 'KLogger.php';

$log = new KLogger ( "log.txt" , KLogger::DEBUG );
$mainDir = "prices";

function myFile($params) {
	global $mainDir;

	$date = $params["date"];
	$los = $params["los"];
	$web = $params["web"];
	$today = $params["today"];
	
	return joinDir($mainDir, joinDir($today, $date . "-" . $los . "-" . $web . ".json"));
}
	

function getPrices() {
	global $log;
	$log->LogInfo("store.php called with GET");

	$myFile = myFile($_GET);
	$log->LogInfo("GET filename " . $myFile);	
	
	header('Content-type: text/plain');
	if (file_exists($myFile)) {
		readfile($myFile);
	}
}

function joinDir($dir, $file) {
	$lastChar = substr($dir, strlen($dir) - 1, 1);
	if($lastChar != '/' and $lastChar != '\\') {
		return $dir . DIRECTORY_SEPARATOR . $file;
	} else {
		return $dir . $file;
	}
}

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
	$log->LogInfo("Cleaning all directories older than " . $today);
	foreach(listDir($mainDir) as $subdir) {
		if($subdir < $today) {			
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
	$log->LogInfo("store.php called with POST");
	
	$crc = $_POST["crc"];
	$prices = $_POST["prices"];
	$today = $_POST["today"];

	$myFile = myFile($_POST);
	$subDir = joinDir($mainDir, $today);
	if(! is_dir($subDir)) {
		$log->LogInfo("About to create dir " . $subDir);
		$retval = mkdir($subDir);
		$log->LogInfo("Mkdir returned " . $retval);
	}
	
	$crc2 = md5($prices);
	if($crc == $crc2) {
		$log->LogInfo("CRC is OK. POST filename " . $myFile);	

		$fh = fopen($myFile, 'w') or die("can't open file");
		fwrite($fh, $prices);
		fclose($fh);
		
		echo "Saved";
	} else {
		echo "ERROR";
		$log->LogInfo("Incorrect CRC for " . $myFile. " " . $crc . " <> " . $crc2);	
	}
	
	cleanup($today);
}

if($_SERVER['REQUEST_METHOD'] == 'GET') {
	getPrices();
} else {
	postPrices();
}	
?> 
