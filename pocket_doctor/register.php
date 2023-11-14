<?php

$phone = $_POST['phone'];
$name = $_POST['name'];
$age = $_POST['age'];
$gender = $_POST['gender'];

require 'db_connect.php';
$query = "insert into users values (null, '$name', '$phone', '$gender', $age)";

$result=$mysqli->query($query);

if($result)
{
	$response='OK';
}
else
{
	$response='ERROR';
}

$query = "select u_id from users where u_phone='$phone'";
$result=$mysqli->query($query);

if ($result->num_rows > 0)
{
    $row = mysqli_fetch_array($result);
    $u_id = $row['u_id']; 
}

echo json_encode(array('response' => $response, 'u_id' => $u_id));

$mysqli->close();

?>