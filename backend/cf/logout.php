<?php
$link=mysqli_connect('localhost','root','root','cloudfile');
$json_data=file_get_contents('php://input');
$post=json_decode($json_data,true);
$req="SELECT * FROM tokens WHERE token='".$post['token']."'";
$resp=mysqli_query($link,$req) or die(mysqli_error($link));
$user=mysqli_fetch_assoc($resp);
if(!empty($user)){
    $req="DELETE FROM tokens WHERE token='".$post['token']."'";
    mysqli_query($link,$req) or die(mysqli_error($link));
	echo 'OK';
} else {
    echo 'token_expired';
}
?>