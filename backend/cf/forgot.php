<?php
$link=mysqli_connect('localhost','root','root','cloudfile');
$json_data=file_get_contents('php://input');
$post=json_decode($json_data,true);
$req="SELECT * FROM accounts WHERE nickname='".$post['nick']."' AND check_answer='".$post['answer']."'";
$resp=mysqli_query($link,$req) or die(mysqli_error($link));
$user=mysqli_fetch_assoc($resp);
if(!empty($user)){ // пользователь подтверждён, можно сбрасывать пароль
    die('OK');
} else { // неверный логин или ответ на вопрос
    die('incorrect_data');
}
?>