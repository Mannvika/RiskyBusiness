import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import { WebSocketProvider } from './WebSocketContext';

const root = ReactDOM.createRoot(document.getElementById('root'));

root.render(
    <React.StrictMode>
        <WebSocketProvider> {/* Single WebSocket context for the app */}
            <App /> {/* Routing logic stays in App.js */}
        </WebSocketProvider>
    </React.StrictMode>
);
