import React, {useEffect} from 'react';

import {useNavigate, useParams} from "react-router-dom";
import {useWebSocket} from "../WebSocketContext";

function Game() {

  const {lobbyId} = useParams();
  const socketRef = useWebSocket();
  const socket = socketRef.current;

  useEffect(() => {
    const handleSocketMessage = (event) => {
      try {
        const message = JSON.parse(event.data);
        console.log("Received WebSocket message:", message);

        if (message.type === "message" && message.lobbyId === lobbyId) {
          console.log("Lobby Message:", message.message);
        }
      } catch (e) {
        console.error("Error parsing WebSocket message:", e, "Received data:", event.data);
      }
    };
      socket.addEventListener("message", handleSocketMessage);

      // Cleanup the event listener on unmount
      return () => {
        socket.removeEventListener("message", handleSocketMessage);
      };
  }, [lobbyId, socket]);

  return (
    <div>
      <button>Test</button>
    </div>
  );
}

export default Game;
