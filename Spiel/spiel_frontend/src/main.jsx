// import { v4 as uuidv4 } from 'uuid'
//
// // Polyfill für crypto.randomUUID
// if (typeof crypto.randomUUID !== 'function') {
//   crypto.randomUUID = () => uuidv4();
// }

import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import PhaserGame from "./components/PhaserGame.jsx";

ReactDOM.createRoot(document.getElementById('root')).render(
    <React.StrictMode>
        <PhaserGame />
    </React.StrictMode>
);
