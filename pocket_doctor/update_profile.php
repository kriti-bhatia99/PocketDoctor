<?php

$id = $_POST['id'];
$phone = $_POST['phone'];
$name = $_POST['name'];
$age = $_POST['age'];
$gender = $_POST['gender'];

require 'db_connect.php';
$query = "update users set u_name='$name', u_phone='$phone', u_age=$age, u_gender='$gender' where u_id=$id";

$result=$mysqli->query($query);

if($result)
{
	$response='OK';
}
else
{
	$response='ERROR';
}

echo json_encode(array('response' => $response));

$mysqli->close();

?>