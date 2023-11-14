<?php

$mysqli = new mysqli('localhost', 'root','','pocket_doctor');

if ($mysqli -> connect_errno) 
{
    die('connection_error');
}

?>