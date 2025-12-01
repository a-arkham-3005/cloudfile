<?php
$link=mysqli_connect('localhost','root','root','cloudfile');
$json_data=file_get_contents('php://input');
$post=json_decode($json_data,true);
$req='UPDATE accounts SET ';
$tmp=array_keys($post);
foreach($tmp as $par){
    if($par!='nick'){
        if($par=='quota'||$par=='used') $req.=$par."=".$post[$par].", ";
        elseif($par!='new_nick') $req.=$par."='".$post[$par]."', ";
		else {
			$checkreq="SELECT * FROM accounts WHERE nickname='".$post[$par]."'";
			$resp=mysqli_query($link,$checkreq) or die(mysqli_error($link));
			$array=mysqli_fetch_assoc($resp);
			if(empty($array)) $req.="nickname='".$post[$par]."', ";
			else die("nickname_taken");
		}
    }
}
$req=rtrim($req,', ')." WHERE nickname='".$post['nick']."'";
mysqli_query($link,$req) or die(mysqli_error($link));
echo "OK";
?>