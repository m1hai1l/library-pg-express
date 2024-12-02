const http = require("http");
const fs = require("fs");
const path = require("path")
  

/*
let data = {"null": "123"}

fs.writeFile('as.json', JSON.stringify(data), function(error){
    if(error){  // если ошибка
        return console.log(error);
    }
    console.log("Файл успешно записан");
});



fs.readFile(path.resolve(__dirname, 'as.json'), function(err, data){
    if (err){
        return console.log(err)
    }
    console.log(data.toString())
})
*/

http.createServer(async (request, response) => {
        
    if(request.url == "/user"){
           
          let body = "";   // буфер для получаемых данных
         // получаем данные из запроса в буфер
          for await (const chunk of request) {
            body += chunk;
          }
        // для параметра name
        let userName = "";
        // для параметра age
        let userAge = 0;
        // разбиваем запрос на параметры по символу &
        const params = body.split("&");
        // проходим по всем параметрам и определяем их значения
        let obj = {}
        for(param of params){
            // разбиваем каждый параметр на имя и значение
            const [paramName, paramValue] = param.split("=");
            if(paramName === "username") userName = paramValue; obj["username"]=userName;
            if(paramName === "userage") userAge = paramValue; obj["userage"]=userAge;
        }
        fs.writeFile(path.resolve(__dirname, 'as.json'), JSON.stringify(obj), function(err){
            if(err){
                return console.log(err)
            }
        })
        response.end(`Your name: ${userName}  Your Age: ${userAge}`);
        
    }
    else{
        fs.readFile("form.html", (_, data) => response.end(data));
    }
}).listen(3000, ()=>console.log("Сервер запущен по адресу http://localhost:3000"));