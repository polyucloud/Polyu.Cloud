<?php

	isset($_POST['UID']) or die(json_encode(array('response' => -3)));
	isset($_POST['delfilename']) or die(json_encode(array('response' => -3)));
	$UID = $_POST['UID'];
	$delfilename = $_POST['delfilename'];

	$con = mysqli_connect('localhost','daisunhong_ad','4PPJQBDv','daisunhong_db')
	or die(json_encode(array('response' => -2)));

    $result = mysqli_query($con,"DELETE FROM polyu_files where user_id='".$UID."' and file_name='".$delfilename."'")
	or die(json_encode(array('response' => -1)));

    $rows = array();
	while($r = mysqli_fetch_assoc($result))
		$rows[] = $r;
	$obj = (object) array('response' => 1, 'result' => $rows, 'count' => mysqli_num_rows($result));
	print json_encode($obj);


	mysqli_close($con);

?>