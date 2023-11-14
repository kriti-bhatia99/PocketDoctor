<?php

$phone = $_POST['phone'];
$type = $_POST['type'];
$prediction = $_POST['prediction'];

if ($type=='Heart Stroke')
{
	$probability = null;
}
else
{
	$probability = $_POST['probability'];
}

date_default_timezone_set('Asia/Kolkata');
$date = date('d-m-y h:i:s');

require 'db_connect.php';
$query = "insert into predictions values (null, '$phone', '$date', '$type', '$prediction', '$probability')";

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