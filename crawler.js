var express = require('express');
var fs = require('fs');
var request = require('request');
var cheerio = require('cheerio');
var app     = express();

app.get('/scrape', function(req, res){

    url = 'https://courses.students.ubc.ca/cs/main?pname=subjarea&tname=subjareas&req=0';

    request(url, function(error, response, html){
        if(!error){
            var $ = cheerio.load(html);

            var departments;

            $('#mainTable').filter(function(){
                var data = $(this);
                departments = data.children().last().children();
                for (var depIndex in departments) {
                    if (departments.hasOwnProperty(depIndex)) {
                        var department = departments.eq(depIndex);
                        var department_code = department.children().eq(0).children().eq(0).text();
                        var link = department.children().eq(0).children().eq(0).attr('href');
                        link = 'https://courses.students.ubc.ca' + link;
                        request(link, function(error, response, html) {
                            if(!error) {
                                var $ = cheerio.load(html);
                                var courses;
                                $('.table-striped').filter(function() {
                                    var data = $(this);
                                    courses = data.children().last().children();
                                    for(var courseIndex in courses) {
                                        if(courses.hasOwnProperty(courseIndex)) {
                                            var course = courses.eq(courseIndex);
                                            var course_code = course.children().eq(0).children().eq(0).text().trim();
                                            var course_link = course.children().eq(0).children().eq(0).attr('href');
                                            course_link = 'https://courses.students.ubc.ca' + course_link;                                          
                                            request(course_link, function(error, response, html) {
                                                if(!error) {
                                                    var $ = cheerio.load(html);
                                                    var sections;
                                                    $('.table-striped').filter(function() {
                                                        var data = $(this);
                                                        sections = data.children().last().children();
                                                        for(var secIndex in sections) {
                                                            if(sections.hasOwnProperty(secIndex)) {
                                                                var section = sections.eq(secIndex);
                                                                var course_page = section.children().eq(1).children().eq(0);                                                    
                                                                if(course_page.attr('href')) {
                                                                    var sec_code = section.children().eq(1).children().eq(0).text().trim();
                                                                    var page_link = section.children().eq(1).children().eq(0).attr('href');
                                                                    page_link = 'https://courses.students.ubc.ca' + page_link;
                                                                    request(page_link, function(error, response, html) {                                                   
                                                                        if(!error) {
                                                                            var $ = cheerio.load(html);
                                                                            var json = { prof : "", section_code : "", title: "", description: ""};
                                                                            
                                                                            $('tr:has(td:contains("Instructor:  "))').filter(function() {
                                                                                var data = $(this);
                                                                                var prof = data.children().last().text();
                                                                                json.prof = prof;
                                                                                json.section_code = sec_code;                                                                                                                   
                                                                            })
                                                                            $('.content').filter(function() {
                                                                                var data = $(this);
                                                                                var title = data.children().eq(2).text();
                                                                                json.title = title;
                                                                                var description = data.children().eq(3).text();
                                                                                json.description = description;
                                                                            })
                                                                            fs.appendFile('message.json', JSON.stringify(json, null, 4), function (err) {
                                                                                    if (err) throw err;
                                                                                    console.log('Saved!');
                                                                            });
                                                                            
                                                                        }
                                                                    })
                                                                }
                                                                
                                                            }
                                                        }
                                                    })
                                                }
                                            })

                                        }
                                    }
                                })
                            }
                        })
                    }
                }

                // fs.writeFile('output.json', JSON.stringify(links, null, 4), function(err){

                //     console.log('File successfully written! - Check your project directory for the output.json file');

                // });

                // Finally, we'll just send out a message to the browser reminding you that this app does not have a UI.
                res.send('Check your console!');
            })
        }
    })
});

app.listen('8081');

console.log('Magic happens on port 8081');

exports = module.exports = app;