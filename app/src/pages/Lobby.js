import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useWebSocket, WS_MESSAGE_TYPES } from "../WebSocketContext";

function Lobby() {
    const { lobbyId } = useParams();
    const [error, setError] = useState("");
    const [lobbyDetails, setLobbyDetails] = useState("");
    const [isReady, setReady] = useState("Not Ready");
    const navigate = useNavigate();
    const { registerHandler, markReady, WS_MESSAGES_TYPES } = useWebSocket();

    useEffect(() => {
        const unregisterLobbyUpdate = registerHandler(WS_MESSAGE_TYPES.LOBBY_UPDATE,
            (message) => {
                if (message.lobbyId === lobbyId) {
                    setLobbyDetails(message.details);
                }
            }
        );

        const unregisterGameStart = registerHandler(WS_MESSAGE_TYPES.GAME_STARTING,
            () => navigate(`/game/${lobbyId}`)
        );

        return () => {
            unregisterLobbyUpdate();
            unregisterGameStart();
        };
    }, [lobbyId]);

    const handleReady = () => {
        markReady(lobbyId);
        setReady('True');
    };

    const handleCopy = () => {
        navigator.clipboard.writeText(lobbyId)
            .then(() => alert("Lobby ID copied to clipboard!"))
            .catch(() => alert("Failed to copy Lobby ID."));
    };

    return (
        <div>
            <h1>Lobby {lobbyId}</h1>
            <p>Lobby Status: {lobbyDetails.status || "Loading..."}</p>
            <button onClick={handleReady}>Ready</button>
            <p>Ready Status: {isReady}</p>
            <button onClick={handleCopy}>Copy Lobby ID</button>
        </div>
    );
}

export default Lobby;
