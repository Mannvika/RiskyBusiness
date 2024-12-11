import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';

function Lobby() {
  const navigate = useNavigate();
  const [error, setError] = useState('');
  const [response, setResponse] = useState('');

  const isFirstRender = useRef(true);

  useEffect(() => {
    if (isFirstRender.current) {
      isFirstRender.current = false;
  
      const url = `http://localhost:8080/api/gameID/${sessionStorage.getItem('gameID')}/lobby`;
  
      fetch(url)
        .then((res) => {
          if (!res.ok) throw new Error('Invalid Game ID');
          return res.text();
        })
        .then((data) => {
          sessionStorage.setItem('playerID', data);
          console.log(data);
        })
        .catch((err) => console.error(err.message));
    }
  }, []);
  

  const handleReady = () => {
    fetch(`http://localhost:8080/api/gameID/${sessionStorage.gameID}/ready`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ "playerID": sessionStorage.getItem("playerID") }),
    })
      .then((res) => {
        if (!res.ok) throw new Error('Invalid Game ID');
        return res.text();
      })
      .then((data) => {
        setResponse(data);
        console.log("Server Response: ", data);
      })
      .catch((err) => {
        console.error("Error: ", err.message);
        setResponse('Error');
      });
  };
  

  return (
    <div>
      <h1>Lobby</h1>

      {/* Conditionally render an error message */}
      {error && <p>Error: {error}</p>}
      
      <p>Player Name: "Test"</p>
      <p>Game ID: "Test"</p>

      {/* Ready button to start the game */}
      <button onClick={handleReady}>Ready</button>
    </div>
  );
}

export default Lobby;
