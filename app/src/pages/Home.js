import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useWebSocket, WS_MESSAGE_TYPES } from '../WebSocketContext';

function Home() {
  const [name, setName] = useState('');
  const [inputLobbyId, setInputLobbyId] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const { createLobby, joinLobby, registerHandler } = useWebSocket();

  useEffect(() => {
    // Handle lobby creation response
    const unregisterLobbyCreated = registerHandler(WS_MESSAGE_TYPES.LOBBY_CREATED,
        (data) => {
          console.log('Lobby created:', data.lobbyId);
          joinLobby(data.lobbyId, name)
        }
    );

    // Handle lobby join response
    const unregisterLobbyJoined = registerHandler(WS_MESSAGE_TYPES.LOBBY_JOINED,
        (data) => {
          console.log('Successfully joined lobby:', data.lobbyId);
          navigate(`/lobby/${data.lobbyId}`);
        }
    );

    // Handle errors
    const unregisterError = registerHandler(WS_MESSAGE_TYPES.ERROR,
        (data) => {
          setError(data.message);
          console.error('Server error:', data.message);
        }
    );

    return () => {
      unregisterLobbyCreated();
      unregisterLobbyJoined();
      unregisterError();
    };
  }, [navigate]);

  const handleCreateLobby = () => {
    if (!name) {
      setError('Please enter your name');
      return;
    }
    setError('');
    createLobby(name);
  };

  const handleJoinLobby = () => {
    const lobbyIdStr = inputLobbyId.trim();

    if (!lobbyIdStr) {
      setError('Please enter a valid lobby ID');
      return;
    }

    if (!name) {
      setError('Please enter your name');
      return;
    }

    setError('');
    joinLobby(lobbyIdStr, name);
  };

  return (
      <div>
        <h1>Welcome to the Game</h1>
        {error && <p style={{ color: 'red' }}>{error}</p>}

        <form onSubmit={e => e.preventDefault()}>
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
          <button onClick={handleCreateLobby}>Create Lobby</button>
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
          <button onClick={handleJoinLobby} style={{ marginLeft: '10px' }}>
            Join Lobby
          </button>
        </div>
      </div>
  );
}

export default Home;