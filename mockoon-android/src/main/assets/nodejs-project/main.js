const mockoon = require('@mockoon/commons-server');
const jsonFile = require('jsonfile');

try {
    const environment = jsonFile.readFileSync(process.argv[2]);
    environment.data.forEach(it => {
        const env = it.item;
        console.log('Starting: ' + env.name + ' on port: ' + env.port + '');
        const server = new mockoon.MockoonServer(env);
        server.start();
    });
} catch (error) {
    console.error(error);
}


