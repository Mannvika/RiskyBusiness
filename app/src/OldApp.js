import React, { useState } from 'react';

function App() {
  const [gameID, setGameID] = useState('');
  const [playerID, setPlayerID] = useState('1');
  const [message, setMessage] = useState('');
  const [playerMessage, setPlayerMessage] = useState('');
  const [response, setResponse] = useState('');

  const fetchMessage = () => {
    const url = `http://localhost:8080/api/gameID/${gameID}/playerID/${playerID}`;
    fetch(url)
      .then((res) => {
        if (!res.ok) throw new Error('Invalid Game ID');
        return res.text();
      })
      .then((data) => setMessage(data))
      .catch((err) => setMessage(err.message));
  };

  const sendMessage = () => {
    fetch(`http://localhost:8080/api/gameID/${gameID}/submit`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ playerMessage }),
    })
      .then((res) => {
        if (!res.ok) throw new Error('Invalid Game ID');
        return res.text();
      })
      .then((data) => setResponse(data))
      .catch((err) => setResponse(err.message));
  };

  return (
    <div>
      <h1>Enter Game ID</h1>
      <input
        type="text"
        value={gameID}
        onChange={(e) => setGameID(e.target.value)}
        placeholder="Game ID"
      />
      <h2>Player Actions</h2>
      <select value={playerID} onChange={(e) => setPlayerID(e.target.value)}>
        <option value="1">Player 1</option>
        <option value="2">Player 2</option>
      </select>
      <button onClick={fetchMessage}>Fetch Message</button>
      <p>Message: {message}</p>

      <h2>Send Message</h2>
      <input
        type="text"
        value={playerMessage}
        onChange={(e) => setPlayerMessage(e.target.value)}
        placeholder="Enter your message"
      />
      <button onClick={sendMessage}>Send Message</button>
      <p>Response: {response}</p>
    </div>
  );
}

export default App;
