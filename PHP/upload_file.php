<?php
    $uid = $_GET['uid'];
    $ran_num 	= rand(0, 999999);
	$file_ext = get_extension($_FILES['uploadedfile']['name']);
	$file_name = preg_replace("/\\.[^.\\s]{3,4}$/", "", $_FILES['uploadedfile']['name']) ;
    $new_file_name = md5($file_name).'-'.$ran_num.'.'.$file_ext;


    $target_path  = "../user_files/";
    $target_path = $target_path . basename( $new_file_name);
    if(move_uploaded_file($_FILES['uploadedfile']['tmp_name'], $target_path)) {
       //echo "The file ".  basename( $_FILES['uploadedfile']['name']). " has been uploaded";
       //echo 0;


               $con = mysqli_connect('localhost','daisunhong_ad','4PPJQBDv','daisunhong_db') or die(json_encode(array('response' => -2)));
               echo $sql = "INSERT INTO polyu_files (user_id,parent_folder,level,file_name,file_suffix,storage_path,create_date,last_update,version_no) VALUES ($uid,'root',0,'$file_name','$file_ext','$new_file_name', NOW(), NOW(), 1)";
               mysqli_query($con, $sql) or die(mysqli_error($con));



    }  else{
       //echo "There was an error uploading the file, please try again!" . $_FILES['uploadedfile']['error'];
       echo 1;
    }

    function insertDB () {
        //mysqli_query($con,"INSERT INTO polyu_files (user_id,parent_folder,file_name,file_suffix,storage_path,create_date,last_update,version_no)".
        		//" VALUES ('".$fstName."', '".$lstName."','".$email."','".$password."',NOW())")
        	//or die(json_encode(array('response' => -1)));

    }


    function get_extension($file_name){
    	$ext = explode('.', $file_name);
    	$ext = array_pop($ext);
    	return strtolower($ext);
    }

?>