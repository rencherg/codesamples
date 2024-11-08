const express = require('express');
const cookieParser = require('cookie-parser');
const app = express();
const uuid = require('uuid');
const bodyParser = require('body-parser');
app.use(bodyParser.json());
app.use(cookieParser());
const bcrypt = require('bcrypt');
const cors = require('cors');

const {processComment, addToUsers, addPost, addToWebsocket, getAllItems, updateUserAuthToken, addUserDb, addPostDb, addToWebsocketDb, updatePostData, getAllUsers} = require('./dao.js');
const getWeather = require('./weather.js')

const port = process.argv.length > 2 ? process.argv[2] : 4000;

app.use(express.static('public'));
app.set('trust proxy', true);

const { WebSocketServer } = require('ws');

// const whitelist = ['http://localhost:5173', 'http://192.168.1.100:5173', 'http://localhost:5173/login'];

// // CORS options
// const corsOptions = {
//   origin: function (origin, callback) {
//     if (whitelist.indexOf(origin) !== -1 || !origin) {
//       // Allow requests from whitelisted origins or if the origin is null (e.g., non-browser requests)
//       callback(null, true);
//     } else {
//       // Deny requests from other origins
//       callback(new Error('Not allowed by CORS'));
//     }
//   },
// };

app.use(cors());

// Enable CORS with custom options
// app.use(cors(corsOptions));

// Router for service endpoints
const apiRouter = express.Router();
app.use(`/api`, apiRouter);

// Error middleware
apiRouter.get('/error', (req, res) => {
  throw new Error('Error: Resource not found');
});

// testing
apiRouter.get('', (req, res) => {
  res.send('{\'message\': \'good\'}');
});

//getting backend data
apiRouter.get('/data', async (req, res) => {
  try {

    const siteData = await getAllItems()
    res.send(siteData);
  } catch (error) {
    console.error("Error:", error);
    res.status(500).send("Internal Server Error");
  }
})

//Model object body that we will use
// sampleLoginObject = {
//   "username": "username",
//   "password": "password"
// }
apiRouter.post('/login', async (req, res, next) => {
  userList = await getAllUsers()

  found = false
  let foundUserZip

  for (const user of userList) {
    const passwordComparison = await bcrypt.compare(req.body.password, user.password);
    
    if (req.body.username === user.username && passwordComparison) {
      found = true;
      foundUserZip = user.zipcode;
      break;
    }
  }

  if(found){

    const token = uuid.v4();

    updateUserAuthToken(req.body.username, token)

    const response = {
      "message": "success",
      "authToken": token,
      "userZip": foundUserZip,
      "username": req.body.username
    }

    setAuthUsernameCookies(res, token, req.body.username)

    res.send(response);
  }else{
    res.send({"message": "invalid credentials!"});
  }

});

apiRouter.post('/logout', async (req, res, next) => {

  const username = req.cookies['username'];

  userList = await getAllUsers()

  found = false

  userList.forEach(element => {
    if((element.username === username)){
      found = true
    }
  });

  if(found){

    updateUserAuthToken(username, '')

    const response = {
      "message": "success",
    }

    res.clearCookie('token');
    res.clearCookie('username');

    res.send(response);
  }else{
    res.send({"message": "invalid credentials"});
  }
});

//Model object body that we will use
// sampleAuthenticateObject = {
//   "username": "username",
//   "token": "token"
// }

//It will only work if the user is logged in
apiRouter.post('/authenticate', async (req, res, next) => {
  const token = req.cookies['token'];
  const username = req.cookies['username'];

  res.send(await authenticate(username, token));

});

async function authenticate(username, token){

  if(token === ''){
    res.send({"message": "invalid credentials"});
  }

  userList = await getAllUsers()

  found = false
  let foundUserZip

  userList.forEach(element => {
    if((username === element.username) && (token === element.authToken)){
      found = true
      foundUserZip = element.zipcode
    }
  });

  if(found){

    const response = {
      "message": "success",
      "username": username,
      "userZip": foundUserZip
    }

    return response
  }else{
    return({"message": "invalid credentials"});
  }
}

apiRouter.post('/register', async (req, res) => {

  let body = req.body

  userList = await getAllUsers()
  data = req.body

  duplicateUser=false

  userList.forEach(user => {
    if (data.username === user.username) {
      duplicateUser=true
    }
  });

  if(duplicateUser){
    res.send({"message": "User already exists"});
  }else{

    body.password = await bcrypt.hash(body.password, 10);

    await addUserDb(req.body)

    const token = uuid.v4();

    updateUserAuthToken(data.username, token)

    const response = {
      "message": "success",
      "authToken": token,
      "userZip": data.zipcode,
      "username": data.username
    }

    setAuthUsernameCookies(res, token, req.body.username)

    res.send(response);
  }
});

apiRouter.post('/post', async (req, res, next) => {

  const token = req.cookies['token'];
  const username = req.cookies['username'];

  authenticationResponse = await authenticate(username, token)

  if(authenticationResponse.message !== 'success'){
    res.send({"message": "Error: not logged in."});
  }else{
    await addPostDb(req.body)

    res.send({"message": "Post added successfully."});
  }
});

apiRouter.put('/comment', async (req, res) => {

  req.body.postData

  obj = await processComment(req.body.postData, req.body.commentText, req.body.currentPostID, req.body.parentID)
  updatePostData(obj)
  // addToWebsocketDb(req.body.commentText)
  sendMessageWebsocket(req.body.commentText)

  res.send({"message": "ok"});
});

apiRouter.get('/weather/:zip', async (req, res) => {

  const { zip } =  await req.params;

  let temp

  try{
    temp = await getWeather(zip)
  }catch(e){

    temp = {
      "error": "request invalid"
    }
  }
  
  res.send(temp);
})

const wss = new WebSocketServer({ noServer: true });

function setAuthUsernameCookies(res, authToken, username) {
  res.cookie('token', authToken, {
    secure: true,
    httpOnly: true,
    sameSite: 'strict',
  });

  res.cookie('username', username, {
    secure: true,
    httpOnly: true,
    sameSite: 'strict',
  });
}

app.use(function (err, req, res, next) {
  res.status(500).send({type: err.name, message: err.message});
});

// Listening to a network port
// const port = 8080;
server = app.listen(port, function () {
  console.log(`Listening on port ${port}`);
});

// Handle the protocol upgrade from HTTP to WebSocket
server.on('upgrade', (request, socket, head) => {
  wss.handleUpgrade(request, socket, head, function done(ws) {
    wss.emit('connection', ws, request);
  });
});

// Keep track of all the connections so we can forward messages
let connections = [];
let id = 0;

wss.on('connection', (ws) => {
  const connection = { id: ++id, alive: true, ws: ws };
  connections.push(connection);

  // Remove the closed connection so we don't try to forward anymore
  ws.on('close', () => {
    const pos = connections.findIndex((o, i) => o.id === connection.id);

    if (pos >= 0) {
      connections.splice(pos, 1);
    }
  });

  // Respond to pong messages by marking the connection alive
  ws.on('pong', () => {
    connection.alive = true;
  });
});

// Keep active connections alive
setInterval(() => {
  connections.forEach((c) => {
    // Kill any connection that didn't respond to the ping last time
    if (!c.alive) {
      c.ws.terminate();
    } else {
      c.alive = false;
      c.ws.ping();
    }
  });
}, 10000);

function sendMessageWebsocket(message){
  connections.forEach((connection)=>{
    connection.ws.send(message)
  })
}