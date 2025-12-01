<?php
$link=mysqli_connect('localhost','root','root','cloudfile');
$json_data=file_get_contents('php://input');
$post=json_decode($json_data,true);
$req="SELECT * FROM tokens WHERE token='".$post['token']."'";
$resp=mysqli_query($link,$req) or die(mysqli_error($link));
$user=mysqli_fetch_assoc($resp);
if(!empty($user)){
    $req="SELECT * FROM accounts WHERE id=".$user['account_id'];
    $resp=mysqli_query($link,$req) or die(mysqli_error($link));
    $userdata=mysqli_fetch_assoc($resp);
    if(!empty($userdata)){
        if($userdata['ftp_password']=='') die('registration_not_yet_approved');
        $httpresp='{"nick":"'.$userdata['nickname'].'","pass":"'.$userdata['ftp_password'].'"}';
        echo $httpresp;
    } else {
        echo 'user_removed';
        $req="DELETE FROM tokens WHERE account_id=".$user['account_id'];
        mysqli_query($link,$req) or die(mysqli_error($link));
    }
} else {
    echo 'token_expired';
}
?>