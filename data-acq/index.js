var express = require('express');
var fs = require('fs');
var request = require('request');
var cheerio = require('cheerio');
var firebase = require('firebase');

var app = express();
var allLinks = [];
var visitedLink = {};
var visitedCourse = {};
var allCourses = [];
var index = 0;

var config = {
  apiKey: "AIzaSyDvkcVSUHsvf0rPW529sVBUbcOgLy2mOs0",
  authDomain: "dataimport-c2359.firebaseapp.com",
  databaseURL: "https://dataimport-c2359.firebaseio.com",
  projectId: "dataimport-c2359",
  storageBucket: "dataimport-c2359.appspot.com",
  messagingSenderId: "138846803809"
};
firebase.initializeApp(config);

// Get a reference to the database service
var database = firebase.database();

app.get('/scrape', function(req, res){
  console.log("hello");
  collectAllLinks(crawlLinks);
});

app.listen('8081');
console.log('Magic happens on port 8081');

function collectAllLinks(crawlLinks){
  var url = 'https://courses.students.ubc.ca/cs/main?pname=subjarea&tname=subjareas&req=0';
  request(url, function(error, response, html){
    if(!error){
      var $ = cheerio.load(html);
      var courses;
      $('.table-striped').filter(function(){
        var data = $(this);
        courses = data.children().last().children();
        for(var courseIndex in courses){
          if(courses.hasOwnProperty(courseIndex)){
            var course = courses.eq(courseIndex);
            var courselink = course.children().eq(0).children().eq(0).attr('href');
            if(!courselink){
              continue;
            }
            courselink = 'https://courses.students.ubc.ca' + courselink;
            allLinks.push(courselink);
          }
        }
      });
      crawlLinks();
    }
  });
}

function crawlLinks(){
  console.log("index is " + index);
  if(index >= allLinks.length){
    console.log("the end");
    writeToFirebase();
    return;
  }
  var link = allLinks[index];
  crawlLinksHelper(link, crawlLinks);
}

function crawlLinksHelper(link, crawlLinks){
  index++;

  request(link, function(error, response, html){
    if(!error){
      var $ = cheerio.load(html);
      $('.table-striped').filter(function(){
        var data = $(this);
        courses = data.children().last().children();
        for(var courseIndex in courses){
          if(courses.hasOwnProperty(courseIndex)){
            var json = {
              courseCode: "",
              courseTitle: ""
            };
            var course = courses.eq(courseIndex);
            var courseCode = course.children().eq(0).children().eq(0).text();
            if(!courseCode){
              continue;
            }
            if(visitedCourse[courseCode]){
              continue;
            }
            visitedCourse[courseCode] = true;
            json.courseCode = courseCode;
            console.log("courseCode is " + courseCode);
            var courseTitle = course.children().eq(1).text();
            json.courseTitle = courseTitle;
            console.log("courseTitle is " + courseTitle);
            allCourses.push(json);
          }
        }
        crawlLinks();
      });
    }
  });
}

function writeToFirebase(){
  allCourses.forEach((courseJSON) => {
    var courseCode = courseJSON.courseCode;
    var courseTitle = courseJSON.courseTitle;
    var components = courseCode.split(" ");
    if (components.length != 2) {
      return;
    }
    var courseDepartment = components[0].trim();
    var courseNumber = components[1].trim();
    var courseYear = "Year " + courseNumber.charAt(0);

    var departmentRef = firebase.database().ref('NewCourses/' + courseDepartment);
    var yearRef = departmentRef.child(courseYear);

    yearRef.child(courseDepartment + courseNumber).set({
      Description: courseTitle,
    });
  });
}

function printLinks(){
  allCourses.forEach((course)=>{
    console.log(JSON.stringify(course));
  });
}
exports = module.exports = app;
