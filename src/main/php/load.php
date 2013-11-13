<?php 

require_once 'config.php';

function createZip($params) {
	global $log;
	global $mainDir;
	
	$zip = new ZipArchive();
	$zipFilename = joinDir(joinDir($mainDir, $params["today"]), "prices" . time() . ".zip");
	$log->LogInfo("About to create ZIP " . $zipFilename . " for params today=" . $params["today"]." dateLosWebs=" . $params["dateLosWebs"]);
	if ($zip->open($zipFilename, ZipArchive::CREATE)!==TRUE) {
    	exit("cannot open <$filename>\n");
	}
	foreach(explode(";", $params["dateLosWebs"]) as $dateLosWeb) {
		list($date, $los, $web) = explode(",", $dateLosWeb);
		 $filename = myFile3($date, $los, $web);
		 $path = myPath($params, $filename);
		 if (file_exists($path)) {
			$zip->addFile($path, $filename);
			$log->LogInfo("Added " . $path . " to zip as " . $filename);
		 } else {
			$log->LogInfo("Not found " . $path);
		 }
	}
	$zip->close();
	$log->LogInfo("Closed ZIP " . $zipFilename);
	return $zipFilename;
}	

function getPrices() {
	global $log;
	$log->LogInfo("store.php called with GET");

	$myFile = createZip($_GET);
	$log->LogInfo("GET filename " . $myFile);	
	
	header("Content-type: application/zip");
	header("Content-disposition: attachment; filename=prices.zip");  	
	
	if (file_exists($myFile)) {
		readfile($myFile);
	}
}

getPrices();
?> 
