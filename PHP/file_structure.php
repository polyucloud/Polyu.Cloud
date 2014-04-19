<?php
	isset($_POST['id']) or die(json_encode(array('response' => -3)));
	isset($_POST['level']) or die(json_encode(array('response' => -3)));
	isset($_POST['depth']) or die(json_encode(array('response' => -3)));
	isset($_POST['parent_folder']) or die(json_encode(array('response' => -3)));
	
	$id = $_POST['id'];
	$level = $_POST['level'];
	$depth = $_POST['depth'];
	$parent_folder = $_POST['parent_folder'];
	
	$con = mysqli_connect('localhost','daisunhong_ad','4PPJQBDv','daisunhong_db') 
	or die(json_encode(array('response' => -2)));	
	
	function ret($id, $con, &$node, $level, $parent)
	{
		$count = 0;
		$result = mysqli_query($con,
		"SELECT folder_name FROM polyu_folder ".
		"where user_id=".$id." and level=".$level." and parent_folder='".$parent."'") 
		or die(json_encode(array('response' => -1)));		
		while($r = mysqli_fetch_assoc($result))
		{
			$node[$count] = array('name'=>$r['folder_name'],'type'=>'d','child'=>array());
			ret($id,$con,$node[$count++]['child'],$level+1,$r['folder_name']);
		}
		$result = mysqli_query($con,
		"SELECT file_name,file_suffix,storage_path,level FROM polyu_files ".
		"where user_id=".$id." and level=".$level." and parent_folder='".$parent."'") 
		or die(json_encode(array('response' => -1)));	
		while($r = mysqli_fetch_assoc($result))
			$node[$count++] = array('name'=>$r['file_name'].$r['file_suffix'],'path'=>$r['storage_path'],'type'=>'f');
	}

	$obj = array();
	$obj['response'] = 1;
	$obj['result'][0] = array('name'=>$parent_folder,'type'=>'d','child'=>array());
	ret($id, $con, $obj['result'][0]['child'], $level, $parent_folder);

	print json_encode($obj);	
	mysqli_close($con);	
?>