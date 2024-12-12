const express = require('express');
const { v4: uuidv4 } = require('uuid');

const app = express()
app.use(express.json())

bd = [{
    "id": 1,
    "title": "Harry Potter",
    "author": "J.K. Rowling",
    "genre": "Fantasy",
    "year": 1997
  }]

function small(name_par, id){
    for(let i = 0; i < bd.length; i++){
        if(bd[i][`${name_par}`] == id){
            return bd[i]
        }
    }
}

function searchs(name, author, id){
    if (id != null){
        return small("id", id)
    }
    else if (name != null){
        return small("name", name)
    }
    else if(author != null){
        return small("author", author)
    }
    return false
}

app.get('/books', function(request, response){
    const parm = request.query;

    if(parm.author != undefined || parm.title != undefined){
        if (parm.title != undefined){
            response.json(searchs(parm.title, null, null))
        }
        else if(parm.author != undefined){
            response.json(searchs(null, parm.author, null))
        }
        response.send(parm)
    }

    else{
        response.json(bd)
    }
})


app.get('/books/:id', function(request, response){
    const id = request.params['id']
    response.json(searchs(null, null, id))
})


app.post('/books', function(request, response){
    if (!request.body){return response.sendStatus(400)}

    const id = uuidv4();
    const title = request.body.title;
    const author = request.body.author;
    const genre = request.body.genre;
    const year = request.body.year;

    bd.push({'id': id, 'title': title, 'author': author, 'genre': genre, 'year': year})

    response.json({'id': id, 'title': title, 'author': author, 'genre': genre, 'year': year})
})


app.put('/books/:id', function(request, response){
    if (!request.body){return response.sendStatus(400)}

    const id = request.params['id'];
    const book = searchs(null, null, id);

    book.title = request.body.title;
    book.author = request.body.author;
    book.genre = request.body.genre;
    book.year = request.body.year;

    for(let i = 0; i < bd.length; i++){
        if(bd[i][`${id}`] == book.id){
            bd[i] = book
        }
    }

    response.json(book)
})


app.delete('/books/:id', function(request, response){
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



app.listen(4000, () => { console.log('Сервер работает') })