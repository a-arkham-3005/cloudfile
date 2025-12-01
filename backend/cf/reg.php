<?php
$link=mysqli_connect('localhost','root','root','cloudfile');
$json_data=file_get_contents('php://input');
$post=json_decode($json_data,true);
$req="SELECT * FROM accounts WHERE nickname='".$post['nick']."'";
$resp=mysqli_query($link,$req) or die(mysqli_error($link));
$user=mysqli_fetch_assoc($resp);
if(!empty($user)){
    die('nickname_taken');
} else {
    $passwd=password_hash($post['pass'],PASSWORD_DEFAULT);
    $req="INSERT INTO accounts SET fullname='".$post['fullname']."', nickname='".$post['nick']."', check_answer='".$post['answer']."', password='$passwd'";
    mysqli_query($link,$req) or die(mysqli_error($link));
    die('OK');
}
?>