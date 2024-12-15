import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { useWebSocket } from "../WebSocketContext";

function Lobby() {
  const { lobbyId } = useParams();
  const [error, setError] = useState("");
  const socketRef = useWebSocket();
  const socket = socketRef.current;
  const [lobbyDetails, setLobbyDetails] = useState("");
  const [isReady, setReady] = useState("Not Ready");

  const fetchLobbyDetails = async () => {
    try {
      console.log(`Fetching lobby details for lobbyId: ${lobbyId}`);
      const response = await fetch(`http://localhost:8080/lobby/${lobbyId}`);

      if (!response.ok) {
        throw new Error(`Lobby with ID ${lobbyId} not found`);
      }

      const data = await response.json();
      setLobbyDetails(data);
      console.log("Lobby details fetched:", data);
    } catch (err) {
      console.error(err);
      setError("Error fetching lobby details.");
    }
  };

  useEffect(() => {
    fetchLobbyDetails();

    if (socket) {
      if (socket.readyState === WebSocket.OPEN) {
        console.log("WebSocket is open, sending JOIN_LOBBY...");
        socket.send(JSON.stringify({ type: "JOIN_LOBBY", lobbyId }));
      } else {
        console.warn("WebSocket is not ready yet.");
      }

      // Listen for updates from the WebSocket
      const handleSocketMessage = (event) => {
        try {
          const message = JSON.parse(event.data);
          console.log("Received WebSocket message:", message);

          if (message.type === "LOBBY_UPDATE" && message.lobbyId === lobbyId) {
            setLobbyDetails(message.details);
          }
        } catch (e) {
          console.error("Error parsing WebSocket message:", e);
        }
      };

      socket.addEventListener("message", handleSocketMessage);

      // Cleanup the event listener on unmount
      return () => {
        socket.removeEventListener("message", handleSocketMessage);
      };
    }
  }, [lobbyId, socket]);

  if (error) return <p>{error}</p>;

  const ready = () => {
    if (!socket) {
      console.error('WebSocket instance not found in context');
      return;
    }

    console.log('WebSocket ready state:', socket.readyState);

    if (socket.readyState === WebSocket.OPEN) {
      socket.send(JSON.stringify({ type: 'READY', lobbyId: lobbyId }));
      setReady('True');
    } else {
      console.error('WebSocket is not open');
    }
  }

  return (
      <div>
        <h1>Lobby {lobbyId}</h1>
        <p>Lobby Status: {lobbyDetails.status || "Loading..."}</p>
        <button onClick={ready}>Ready</button>
        <p>Ready Status: {isReady}</p>
      </div>
  );
}

export default Lobby;
