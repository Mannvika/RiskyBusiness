import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useWebSocket } from '../WebSocketContext'; // Import the custom hook

function Home() {
  const [name, setName] = useState('');
  const [inputLobbyId, setInputLobbyId] = useState('');
  const navigate = useNavigate();
  const socketRef = useWebSocket(); // Use the custom hook to get the WebSocket reference

  const createLobby = async () => {
    try {
      const response = await fetch('http://localhost:8080/lobby/create', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ playerName: name }),
      });

      if (!response.ok) {
        throw new Error(`Failed to create lobby: ${response.status}`);
      }

      const data = await response.json();
      const lobbyId = data.lobbyId;

      console.log('Lobby created:', lobbyId);

      if (lobbyId) {
        const payload = { playerName: name };

        const joinResponse = await fetch(`http://localhost:8080/lobby/join/${lobbyId}`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload),
        });

        if (joinResponse.ok) {
          console.log('Successfully added to the lobby.');
          // Inform the server via WebSocket
          const socket = socketRef.current; // Access the WebSocket instance
          if (socket && socket.readyState === WebSocket.OPEN) {
            socket.send(
                JSON.stringify({ type: 'JOIN_LOBBY', lobbyId: lobbyId, playerName: name })
            );
          }
          navigate(`/lobby/${lobbyId}`);
        } else {
          console.error('Failed to join the lobby.');
        }
    }} catch (err) {
      console.error('Error:', err.message);
    }
  };

  const joinLobby = async () => {
    try {
      const lobbyIdStr = inputLobbyId.trim();

      if (!lobbyIdStr) {
        console.error('Please enter a valid lobby ID.');
        return;
      }

      if (!name) {
        console.error('Player name is required to join the lobby.');
        return;
      }

      const payload = { playerName: name };

      const response = await fetch(`http://localhost:8080/lobby/join/${lobbyIdStr}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      });

      if (response.ok) {
        console.log('Successfully added to the lobby.');
        // Inform the server via WebSocket
        const socket = socketRef.current; // Access the WebSocket instance
        if (socket && socket.readyState === WebSocket.OPEN) {
          socket.send(
              JSON.stringify({ type: 'JOIN_LOBBY', lobbyId: lobbyIdStr, playerName: name })
          );
        }
        navigate(`/lobby/${lobbyIdStr}`);
      } else {
        console.error('Failed to join the lobby.');
      }
    } catch (err) {
      console.error('Failed to join the lobby:', err);
    }
  };

  return (
      <div>
        <h1>Welcome to the Game</h1>
        <form>
          <label>
            Your Name:
            <input
                type="text"
                value={name}
                placeholder="Enter your name"
                onChange={(e) => setName(e.target.value)}
                required
            />
          </label>
        </form>

        <div style={{ marginTop: '20px' }}>
          <button onClick={createLobby}>Create Lobby</button>
        </div>

        <div style={{ marginTop: '20px' }}>
          <label>
            Lobby ID:
            <input
                type="text"
                value={inputLobbyId}
                placeholder="Enter lobby ID"
                onChange={(e) => setInputLobbyId(e.target.value)}
            />
          </label>
          <button onClick={joinLobby} style={{ marginLeft: '10px' }}>
            Join Lobby
          </button>
        </div>
      </div>
  );
}

export default Home;
