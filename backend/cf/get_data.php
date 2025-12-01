<?php
$link=mysqli_connect('localhost','root','root','cloudfile');
$json_data=file_get_contents('php://input');
$post=json_decode($json_data,true);
$req='SELECT '.$post['pars'].' FROM accounts WHERE nickname=\''.$post['nick'].'\'';
$resp=mysqli_query($link,$req) or die(mysqli_error($link));
$array=mysqli_fetch_assoc($resp);
$pars_resp=array_keys($array);
$httpresp='{';
foreach($pars_resp as $par){
    if($par=='quota' || $par=='used') $httpresp.='"'.$par.'":'.$array[$par].', ';
    else $httpresp.='"'.$par.'":"'.$array[$par].'", ';
}
$httpresp=rtrim($httpresp,', ').'}';
echo $httpresp;
?>