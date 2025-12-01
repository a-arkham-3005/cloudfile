<?php
function generateToken($length = 32) {
    $characters = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
    $shuffledCharacters = str_shuffle($characters);
    return substr($shuffledCharacters, 0, $length);
}
$link=mysqli_connect('localhost','root','root','cloudfile');
$json_data=file_get_contents('php://input');
$post=json_decode($json_data,true);
$req="SELECT * FROM accounts WHERE nickname='".$post['nick']."'";
$resp=mysqli_query($link,$req) or die(mysqli_error($link));
$user=mysqli_fetch_assoc($resp);
if(!empty($user)){
    $pass=$user['password'];
    if(password_verify($post['pass'],$pass)){
        $token=generateToken();
        $req="INSERT INTO tokens SET account_id=".$user['id'].", token='$token'";
        mysqli_query($link,$req) or die(mysqli_error($link));
        echo $token;
    } else {
        echo 'incorrect_data';
    }
} else {
    echo 'incorrect_data';
}
?>