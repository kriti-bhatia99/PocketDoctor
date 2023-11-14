<?php

$u_phone = $_POST['phone'];
// $u_phone = "+1234";
$response; $u_id; $u_name; $u_gender; $u_age;
require 'db_connect.php';

$query = "select * from users where u_phone='$u_phone'";
$result = $mysqli -> query($query);

if ($result->num_rows > 0)
{
    $response = "FOUND";

    $row = mysqli_fetch_array($result);
    $u_id = $row['u_id']; 
    $u_name = $row['u_name']; 
    $u_gender = $row['u_gender']; 
    $u_age = $row['u_age'];
}
else
{
    $response = "NONE";
}

echo json_encode(array('response'=>$response, 'id'=>$u_id,
'name'=>$u_name, 'gender'=>$u_gender, 'age'=>$u_age));

?>