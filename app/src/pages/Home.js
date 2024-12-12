import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

function Home() {
  const [lobbyId, setLobbyId] = useState('');
  const [name, setName] = useState('');
  const [inputLobbyId, setInputLobbyId] = useState('');
  const navigate = useNavigate();
  const socket = new WebSocket("ws://localhost:8080/game");

  socket.onopen = () => {
    console.log("Connected to game server");
  };

  socket.onmessage = (event) => {
    const data = JSON.parse(event.data);
    console.log(data);
  };

  const createLobby = async () => {
    try {
      const response = await fetch("http://localhost:8080/lobby/create", { method: "POST" });
      const data = await response.json();
      console.log("Lobby created:", data.lobbyId);
      navigate(`/lobby/${data.lobbyId}`); // Navigate to the new lobby
    } catch (error) {
      console.error("Error creating lobby:", error);
    }
  };


  const joinLobby = async () => {
    try {
      const lobbyIdStr = inputLobbyId.trim();

      if (!lobbyIdStr) {
        console.error("Please enter a valid lobby ID.");
        return;
      }

      if (!name) {
        console.error("Player name is required to join the lobby.");
        return;
      }

      const payload = { playerName: name };

      const response = await fetch(`http://localhost:8080/lobby/join/${lobbyIdStr}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      });

      if (response.ok) {
        console.log("Successfully added to the lobby.");
        navigate(`/lobby/${lobbyIdStr}`);
      } else {
        console.error("Failed to join the lobby.");
      }
    } catch (err) {
      console.error("Failed to join the lobby:", err);
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
          <button onClick={joinLobby} style={{ marginLeft: '10px' }}>Join Lobby</button>
        </div>
      </div>
  );
}

export default Home;
