<?php
$link=mysqli_connect('localhost','root','root','cloudfile');
$json_data=file_get_contents('php://input');
$post=json_decode($json_data,true);
$passwd=password_hash($post['pass'],PASSWORD_DEFAULT);
$req="UPDATE accounts SET password='$passwd' WHERE nickname='".$post['nick']."'";
mysqli_query($link,$req) or die(mysqli_error($link));
$req="SELECT * FROM accounts WHERE nickname='".$post['nick']."'";
$resp=mysqli_query($link,$req) or die(mysqli_error($link));
$user=mysqli_fetch_assoc($resp);
$id=$user['id'];
$req="DELETE FROM tokens WHERE account_id=$id";
mysqli_query($link,$req) or die(mysqli_error($link));
echo "OK";
?>