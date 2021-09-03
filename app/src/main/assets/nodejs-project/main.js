const mockoon = require('@mockoon/commons-server');

const environment = JSON.parse(process.argv[2]);

console.log('Starting: ' + environment.name + ' on port: ' + environment.port + '');
const server = new mockoon.MockoonServer(environment);
server.start();

