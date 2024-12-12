const express = require('express');
const { v4: uuidv4 } = require('uuid');

const app = express()

app.use(express.json())

const bd = [{ "id": 1, "name": "Alice", "email": "alice@example.com", "age": 25 }];


app.get('/user', function(_, response){
    response.json(bd)
});

app.get('/user/:id', function(request, response){
    const id = request.params['id'];
    let flag = 0
    for (let i = 0; i < bd.length; i++){
        if (bd[i]["id"] == id){
            response.json(bd[id-1]);
            flag++
        }
    }
    if(!flag){
        response.json({ error: "User not found" }).sendStatus(404)
    }
})


app.post('/user', function(request, response){
    if(!request.body) return response.sendStatus(400);
    const user = request.body
    
    user.id = uuidv4();

    bd.push(user)
    response.json(user)
})




app.put('/user/:id', function(request, response){
    if(!request.body) return response.sendStatus(400);


    const id = Number(request.params['id']);
    let ind

    for (let i = 0; i < bd.length; i++){
        if (bd[i]["id"] == id){
            ind = i
        }
    }
    if(ind == undefined){
        return response.json({ error: "User not found" }).sendStatus(404)
    }


    const name = request.body.name;
    const email = request.body.email;
    const age = request.body.age;

    bd[ind] = {"id": id, "name": name, "email": email, "age": age}

    response.json({"id": id, "name": name, "email": email, "age": age})
})





app.delete('/user/:id', function(request, response){
    const id = request.params['id'];

    let flag = 0

    for (let i = 0; i < bd.length; i++){
        if (bd[i]["id"] == id){
            flag++;
            bd.splice(i, 1);
            response.json({content: "Успешно"})
        }
    }
    if(!flag){
        return response.json({ error: "User not found" }).sendStatus(404)
    }
})



app.listen(4000, () => { console.log('Сервер запущен') });