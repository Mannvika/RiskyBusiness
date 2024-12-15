import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import Home from './pages/Home';
import Lobby from './pages/Lobby';
import Game from './pages/Game';
import {WebSocketProvider} from "./WebSocketContext";

function App() {
  return (
      <WebSocketProvider>
          <Router>
              <Routes>
                  <Route path="/" element={<Home />} />
                  <Route path="/lobby/:lobbyId" element={<Lobby />} />
                  <Route path="/game" element={<Game />} />
              </Routes>
          </Router>
      </WebSocketProvider>
  );
}

export default App;
