const express = require('express');
const app = express();
const path = require('path');
const http = require('http');

const port = process.env.NODE_PORT || 8081;

app.use(express.static(__dirname));
app.get("/", (req, res) => res.sendFile(path.join(__dirname + "/insights/index.html"))); 

const server = http.createServer(app);
server.listen(port, () => console.log(`App running on: http://localhost:${port}`));

