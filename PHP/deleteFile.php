<?php

	isset($_POST['UID']) or die(json_encode(array('response' => -3)));
	isset($_POST['delfilename']) or die(json_encode(array('response' => -3)));
	$UID = $_POST['UID'];
	$delfilename = $_POST['delfilename'];

	$con = mysqli_connect('localhost','daisunhong_ad','4PPJQBDv','daisunhong_db')
	or die(json_encode(array('response' => -2)));

    mysqli_query($con,"DELETE FROM polyu_files where user_id='".$UID."' and file_name='".$delfilename."'")
	or die(json_encode(array('response' => -1)));

    print json_encode(array('response' => 1, 'affacted_row' => mysqli_affected_rows($con)));



	mysqli_close($con);

?>