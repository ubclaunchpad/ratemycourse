var fs = require('fs');

fs.readFile('message.json', "utf8", function(err, data){
  if(err){ throw err;}
  var content = data;
  processData(content);
})

function processData(content){
  var res = content.split("\n");
  var course = {};
  var allCourses = [];
  for(var i = 0; i < res.length; i++){
    if(i%5 == 0 && i != 0){
      allCourses.push(course);
      course = {};
      continue;
    }
    if(i%5 == 2){
      var pair = res[i].split(":");
      var vals = pair[1].trim().substring(1, pair[1].length-3);
      var fields = vals.split(" ");
      course["faculty"] = fields[0];
      course["code"] = fields[1];
      course["section"] = fields[2];
    }else{
      var pair = res[i].split(":");
      if(pair.length < 2) continue;
      var key = pair[0].trim().substring(1, pair[0].length-5);
      var val = pair[1].trim();
      if(val.charAt(val.length-1) == ','){
        val = val.substring(1, val.length-2);
      }else{
        val = val.substring(1, val.length-1);
      }
      course[key] = val;
    }
  }
  console.log(allCourses);
  console.log(allCourses.length);
}
