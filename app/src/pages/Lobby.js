import React, { useEffect, useState, useContext } from "react";
import { useParams } from "react-router-dom";
import { useWebSocket } from "../WebSocketContext";

function Lobby() {
  const { lobbyId } = useParams();
  const [lobbyDetails, setLobbyDetails] = useState(null);
  const [error, setError] = useState("");
  const { socket } = useWebSocket();

  const fetchLobbyDetails = async () => {
    try {
      const response = await fetch(`http://localhost:8080/lobby/${lobbyId}`);

      if (!response.ok) {
        throw new Error(`Lobby with ID ${lobbyId} not found`);
      }

      const data = await response.json();
      setLobbyDetails(data);
    } catch (err) {
      console.error(err);
      setError("Error fetching lobby details.");
    }
  };

  useEffect(() => {
    fetchLobbyDetails();

    // Send a WebSocket message to join the lobby
    if (socket && socket.readyState === WebSocket.OPEN) {
      socket.send(
          JSON.stringify({ type: "JOIN_LOBBY", lobbyId })
      );
    }

    // Listen for updates from the WebSocket
    const handleSocketMessage = (event) => {
      const message = JSON.parse(event.data);
      if (message.type === "LOBBY_UPDATE" && message.lobbyId === lobbyId) {
        setLobbyDetails(message.details);
      }
    };

    if (socket) {
      socket.addEventListener("message", handleSocketMessage);
    }

    // Cleanup the event listener on unmount
    return () => {
      if (socket) {
        socket.removeEventListener("message", handleSocketMessage);
      }
    };
  }, [lobbyId, socket]);

  if (error) return <p>{error}</p>;

  return (
      <div>
        <h1>Lobby {lobbyId}</h1>
        {lobbyDetails ? (
            <div>
              <p>Lobby Status: {lobbyDetails.status}</p>
              {lobbyDetails.players && (
                  <ul>
                    <h2>Players:</h2>
                    {lobbyDetails.players.map((player, index) => (
                        <li key={index}>{player.name}</li>
                    ))}
                  </ul>
              )}
            </div>
        ) : (
            <p>Loading...</p>
        )}
      </div>
  );
}

export default Lobby;
