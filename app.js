const express = require('express');
const { Pool } = require('pg');
const { exec } = require('child_process');
const path = require('path');

const pool = new Pool({
    user: 'postgres', 
    host: 'localhost', 
    database: 'myexpress-bd', 
    password: 'lira', 
    port: 5433
});

const password = 'lira';
const host = 'localhost';
const user = 'postgres';
const port = 5433;

const databaseName = 'myexpress-bd'; 
const dumpFilePath = path.join(__dirname, 'sql.dump');


async function checkAndRestoreDatabase() {

  const checkCommand = `psql -h ${host} -p ${port} -U ${user} -d ${databaseName} -c "SELECT 1 FROM library"`;

  exec(checkCommand, { env: { PGPASSWORD: password } }, (_, stdout) => {
    if (stdout.includes('1')) {
      console.log(`База данных "${databaseName}" уже существует.`);
    } else {
      console.log(`База данных "${databaseName}" не существует. Создаю и восстанавливаю из дампа...`);
      createAndRestoreDatabase();
    }
  });
}


function createAndRestoreDatabase() {

  const createCommand = `psql -h ${host} -p ${port} -U ${user} -d ${databaseName} -f ${dumpFilePath}`;

  exec(createCommand, { env: { PGPASSWORD: password } }, (error) => {
    if (error) {
      console.error('Ошибка при создании базы данных:', error.message);
      return;
    }
    console.log(`База данных "${databaseName}" успешно создана.`);
  });
}

checkAndRestoreDatabase();

const javaClientPath = path.join(__dirname, 'LibraryClient.java'); 

const app = express()
app.use(express.json())

const passwordSuperUser = 1234;
let FLAG = false;

app.use(function(request, response, next){
    const pas = request.query.password;
    if (pas == passwordSuperUser){
        FLAG = true;
    }
    next()
})

app.get('/books', async function(request, response){
    const parm = request.query;
    const sql_author = "SELECT * FROM library WHERE author=$1";
    const sql_title = "SELECT * FROM library WHERE title=$1";
    const sql_all = "SELECT * FROM library";

    if(parm.author != undefined || parm.title != undefined){
        if (parm.title != undefined){
            const result = await pool.query(sql_title, [parm.title]);
            response.json(result.rows)
        }
        else if(parm.author != undefined){
            const result = await pool.query(sql_author, [parm.author]);
            response.json(result.rows)
        }
    }

    else{
        const result = await pool.query(sql_all);
        response.json(result.rows)
    }
})


app.get('/books/:id', async function(request, response){
    const sql_id = "SELECT * FROM library WHERE id=$1"
    const id = [request.params['id']]

    const result = await pool.query(sql_id, id);
    response.json(result.rows)
})


app.post('/books', async function(request, response){
    if (!FLAG){ return response.sendStatus(403) }
    if (!request.body){return response.sendStatus(400)}

    const sql_post = `INSERT INTO library(title, author, genre, year) VALUES ($1, $2, $3, $4) RETURNING *`;
    const data = [request.body.title, request.body.author, request.body.genre, request.body.year]

    const result = await pool.query(sql_post, data)
    response.json(result.rows)
})


app.put('/books/:id', async function(request, response){
    if (!FLAG){ return response.sendStatus(403) }
    if (!request.body){return response.sendStatus(400)}

    const id = request.params['id'];
    const sql = `UPDATE library SET title=$1, author=$2, genre=$3, year=$4 WHERE id=$5`;
    const data = [request.body.title, request.body.author, request.body.genre, request.body.year, id]

    const result = await pool.query(sql, data);


    if(result.rowCount){
        response.json({'data': 'Успешно'})
    }
    else{
        response.json({'data': "Ошибка"})
    }
})


app.delete('/books/:id', async function(request, response){
    if (!FLAG){ return response.sendStatus(403) }
    const sql_delete = "DELETE FROM library WHERE id=$1";
    const id = [request.params['id']];

    const result = await pool.query(sql_delete, id)

    if(result.rowCount){
        response.json({'data': 'Успешно'})
    }
    else{
        response.json({'data': 'Ошибка'})
    }
})

app.listen(4000, () => { exec(`java "${javaClientPath}"`, (error, stdout, stderr) => {
    if (error) {
        console.error(`Ошибка запуска Java клиента: ${error.message}`);
        return;
    }
    if (stderr) {
        console.error(`Ошибка Java клиента: ${stderr}`);
        return;
    }
    console.log(`Java клиент успешно запущен:\n${stdout}`);
    }); 
})
