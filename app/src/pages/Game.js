import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { useWebSocket, WS_MESSAGE_TYPES } from "../WebSocketContext";

function Game() {
  const { lobbyId } = useParams();
  const { socket, registerHandler, markRoundReady, chooseCard } = useWebSocket();
  const [cards, setCards] = useState([]);
  const [selectedCard, setSelectedCard] = useState(null);
  const [debugInfo, setDebugInfo] = useState({
    readyAttempts: 0,
    lastAttemptTime: null,
    socketState: "initializing"
  });

  useEffect(() => {
    const handleOpen = () => {
      setDebugInfo((prev) => ({ ...prev, socketState: "connected" }));
    };

    const handleClose = () => {
      setDebugInfo((prev) => ({ ...prev, socketState: "disconnected" }));
    };

    const handlePickCardMessage = (message) => {
      if (message.type === WS_MESSAGE_TYPES.PICK_CARD && message.lobbyId === lobbyId) {
        setSelectedCard(null);
        console.log("Received pick_card", message);

        // Extract card names from the message
        const newCards = Object.entries(message)
            .filter(([key]) => key.startsWith("card")) // Filter keys that start with "card"
            .map(([, value]) => value); // Map to the card values

        setCards(newCards);
      }
    };

    if (socket.current) {
      socket.current.addEventListener("open", handleOpen);
      socket.current.addEventListener("close", handleClose);

      // Register the message handler for "PICK_CARD"
      const unregisterHandler = registerHandler(WS_MESSAGE_TYPES.PICK_CARD, handlePickCardMessage);

      return () => {
        socket.current?.removeEventListener("open", handleOpen);
        socket.current?.removeEventListener("close", handleClose);
        unregisterHandler();
      };
    }
  }, [socket, lobbyId, registerHandler]);

  const endTurn = () => {
    setDebugInfo((prev) => ({
      ...prev,
      readyAttempts: prev.readyAttempts + 1,
      lastAttemptTime: new Date().toISOString()
    }));

    markRoundReady(lobbyId);
  };

  const handleCardSelect = (card, index) => {
    setSelectedCard(card); // Update the selected card
    chooseCard(lobbyId, index); // Notify the server of the selected card
  };

  return (
      <div>
        <div>
          <h2>Game</h2>
          {selectedCard ? (
              <p>You selected: {selectedCard}</p>
          ) : cards.length === 0 ? (
              <p>No cards received yet.</p>
          ) : (
              cards.map((card, index) => (
                  <button key={index} onClick={() => handleCardSelect(card, index)}>
                    {card}
                  </button>
              ))
          )}
        </div>
        <div>
          <h2>Debug Information</h2>
          <pre>
          {JSON.stringify(
              {
                socketState: debugInfo.socketState,
                readyAttempts: debugInfo.readyAttempts,
                lastAttempt: debugInfo.lastAttemptTime
              },
              null,
              2
          )}
        </pre>
        </div>
        <button onClick={endTurn}>End Turn</button>
      </div>
  );
}

export default Game;
