<?php
	isset($_POST['fstName']) or die(json_encode(array('response' => -3)));
	isset($_POST['lstName']) or die(json_encode(array('response' => -3)));
	isset($_POST['email']) or die(json_encode(array('response' => -3)));
	isset($_POST['password']) or die(json_encode(array('response' => -3)));
	
	$fstName = $_POST['fstName'];
	$lstName = $_POST['lstName'];
	$email = $_POST['email'];
	$password = $_POST['password'];	
	$con = mysqli_connect('localhost','daisunhong_ad','4PPJQBDv','daisunhong_db') 
	or die(json_encode(array('response' => -2)));
	
	$result = mysqli_query($con,"SELECT * FROM polyu_account where email='".$email."'") 
	or die(json_encode(array('response' => -1)));
	
	mysqli_num_rows($result)>0 
	and die(json_encode(array('response' => 1, 'affacted_row' => 0)));

	mysqli_query($con,"INSERT INTO polyu_account (first_name,last_name,email,password,register_date)".
		" VALUES ('".$fstName."', '".$lstName."','".$email."','".$password."',NOW())") 
	or die(json_encode(array('response' => -1)));
	
	print json_encode(array('response' => 1, 'affacted_row' => mysqli_affected_rows($con)));

	mysqli_close($con);
?>