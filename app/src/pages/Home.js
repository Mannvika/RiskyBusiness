import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

function Home() {
  const [name, setName] = useState('');
  const [gameID, setGameID] = useState('');
  const [message, setMessage] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const url = `http://localhost:8080/api/gameID/${gameID}`;
      const response = await fetch(url);

      if (!response.ok) {
        throw new Error('Invalid Game ID');
      }

      const data = await response.text();
      console.log('Server Response:', data);

      // Save the player's name and Game ID in sessionStorage or React state
      sessionStorage.setItem('playerName', name);
      sessionStorage.setItem('gameID', gameID);

      // Navigate to the Lobby Page
      navigate('/lobby');
    } catch (err) {
      setMessage(err.message);
    }
  };

  return (
    <div>
      <h1>Welcome to the Game</h1>
      <form onSubmit={handleSubmit}>
        <label>
          Name:
          <input type="text" value={name} placeholder="Enter your name" onChange={(e) => setName(e.target.value)} required/>
        </label>
        <br />
        <label>
          Game ID:
          <input type="text" placeholder="Enter Game ID" value={gameID} onChange={(e) => setGameID(e.target.value)} required/>
        </label>
        <br />
        <button type="submit">Join Lobby</button>
        {message && <p style={{ color: 'red' }}>{message}</p>}
      </form>
    </div>
  );
}

export default Home;
