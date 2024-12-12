import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";

function Lobby() {
  const { lobbyId } = useParams();
  const [lobbyDetails, setLobbyDetails] = useState(null);
  const [error, setError] = useState("");

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
  }, [lobbyId]);

  if (error) return <p>{error}</p>;

  return (
      <div>
        <h1>Lobby {lobbyId}</h1>
        {lobbyDetails ? (
            <div>
              <p>Lobby Status: {lobbyDetails.status}</p>
            </div>
        ) : (
            <p>Loading...</p>
        )}
      </div>
  );
}

export default Lobby;
