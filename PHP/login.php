<?php
	isset($_POST['email']) or die(json_encode(array('response' => -3)));
	isset($_POST['password']) or die(json_encode(array('response' => -3)));
	$email = $_POST['email'];
	$password = $_POST['password'];	
	
	$con = mysqli_connect('localhost','daisunhong_ad','4PPJQBDv','daisunhong_db') 
	or die(json_encode(array('response' => -2)));
	
	$result = mysqli_query($con,"SELECT id,email,first_name,last_name FROM polyu_account where email='".$email."' and password='".$password."'") 
	or die(json_encode(array('response' => -1)));
	
	$rows = array();
	while($r = mysqli_fetch_assoc($result))
		$rows[] = $r;
	$obj = (object) array('response' => 1, 'result' => $rows, 'count' => mysqli_num_rows($result));	
	print json_encode($obj);	
	
	mysqli_close($con);
?>