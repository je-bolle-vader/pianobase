const express = require('express');
const path = require('path');
const { spawn } = require('child_process');

const app = express();
const port = 3000;

// Serve static files in de "public" map
app.use(express.static(path.join(__dirname, 'public')));

// Eventueel kan je de index.html rechtstreeks serveren als dat nodig is:
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'index.html'));
});

// Voeg dit toe vóór app.listen(...)
app.get('/api/pianos', (req, res) => {
    // Dummy data; vervang dit met jouw echte data indien beschikbaar
    const pianos = [
      { serialNumber: 'SN001', model: 'U1', merk: 'Yamaha', bouwjaar: 1998 },
      { serialNumber: 'SN002', model: 'Model D', merk: 'Steinway', bouwjaar: 2005 }
    ];
    res.json(pianos);
});

app.listen(port, () => {
    console.log(`Server is listening on http://localhost:${port}`);
  
    // Start de Java applicatie (Main.java) met stdio inheritance zodat de Scanner input werkt
    const javaProcess = spawn('java', [
      '-cp',
      `.;C:\\Users\\ingma\\Desktop\\pianobase\\sqlite-jdbc-3.49.1.0.jar`,
      'Main'
    ], { stdio: 'inherit' });
  
    javaProcess.on('close', code => {
      console.log(`Java process exited with code ${code}`);
    });
});