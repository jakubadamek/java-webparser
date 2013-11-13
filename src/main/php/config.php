<?php 

require_once 'KLogger.php';

$log = new KLogger ( "log.txt" , KLogger::DEBUG );
$mainDir = "./prices";

function myFile($params) {
	return myFile3($params["date"], $params["los"], $params["web"]); 	
}

function myFile3($date, $los, $web) {
	return $date . "-" . $los . "-" . $web . ".json";
}
	
function myPath($params, $filename) {
	global $mainDir;
	$today = $params["today"];
	return joinDir($mainDir, joinDir ($today, $filename));
}

function joinDir($dir, $file) {
	$lastChar = substr($dir, strlen($dir) - 1, 1);
	if($lastChar != '/' and $lastChar != '\\') {
		return $dir . DIRECTORY_SEPARATOR . $file;
	} else {
		return $dir . $file;
	}
}

?>