var express = require('express');
var fs = require('fs');
var request = require('request');
var cheerio = require('cheerio');
var app     = express();

app.get('/scrape', function(req, res){

    // The URL we will scrape from - in our example Anchorman 2.

    url = 'https://courses.students.ubc.ca/cs/main?pname=subjarea&tname=subjareas&req=0';

    // The structure of our request call
    // The first parameter is our URL
    // The callback function takes 3 parameters, an error, response status code and the html

    request(url, function(error, response, html){

        // First we'll check to make sure no errors occurred when making the request

        if(!error){
            var $ = cheerio.load(html);

            var departments;

            // We'll use the unique header class as a starting point.

            $('.table-striped').filter(function(){

                // Let's store the data we filter into a variable so we can easily see what's going on.

                var data = $(this);

                // In examining the DOM we notice that the title rests within the first child element of the header tag.
                // Utilizing jQuery we can easily navigate and get the text by writing the following code:

                departments = data.children().last().children();

                var links = [];

                for (var depIndex in departments) {
                    if (departments.hasOwnProperty(depIndex)) {
                        var department = departments.eq(depIndex);
                        var code = department.children().eq(0).children().eq(0).text();
                        var link = department.children().eq(0).children().eq(0).attr('href');
                        links.push(link);
                        console.log(code + ": " + link);
                    }
                }


                // To write to the system we will use the built in 'fs' library.
                // In this example we will pass 3 parameters to the writeFile function
                // Parameter 1 :  output.json - this is what the created filename will be called
                // Parameter 2 :  JSON.stringify(json, null, 4) - the data to write, here we do an extra step by calling JSON.stringify to make our JSON easier to read
                // Parameter 3 :  callback function - a callback function to let us know the status of our function

                fs.writeFile('output.json', JSON.stringify(links, null, 4), function(err){

                    console.log('File successfully written! - Check your project directory for the output.json file');

                });

                // Finally, we'll just send out a message to the browser reminding you that this app does not have a UI.
                res.send('Check your console!');
            })
        }
    })
});

app.listen('8081');

console.log('Magic happens on port 8081');

exports = module.exports = app;