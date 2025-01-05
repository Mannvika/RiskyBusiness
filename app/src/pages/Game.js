import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { useWebSocket, WS_MESSAGE_TYPES } from "../WebSocketContext";
import lobby from "./Lobby";

function Game() {
  const { lobbyId } = useParams();
  const { socket, registerHandler, markRoundReady, chooseCard, chooseInvestment, activateCard } = useWebSocket();

  const [cards, setCards] = useState([]);
  const [selectedCard, setSelectedCard] = useState(null);
  const [hand, setHand] = useState([]);

  const [investments, setInvestments] = useState([]);
  const [selectedInvestment, setSelectedInvestment] = useState(null);
  const [bankedInvestments, setBankedInvestments] = useState([]);
  const [roundActionMessages, setRoundActionMessages] = useState([]); // To store all round action messages

  const [onHandCash, setOnHandCash] = useState(0);
  const [bankedCash, setBankedCash] = useState(0);
  const [investmentMultiplier, setInvestmentMultiplier] = useState(0.0);
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
        setSelectedCard(null); // Allow selecting a new card
        setCards(message.cards || []); // Update the cards list
      }
    };

    const handlePickInvestmentMessage = (message) => {
      if (message.type === WS_MESSAGE_TYPES.PICK_INVESTMENT && message.lobbyId === lobbyId) {
        setSelectedInvestment(null); // Allow selecting a new investment
        setInvestments(message.investments || []); // Update the investments list
      }
    };

    const handleRoundEnd = (message) => {
      if (message.type === WS_MESSAGE_TYPES.ROUND_END && message.lobbyId === lobbyId) {
        setOnHandCash(message.onHandCash || 0);
        setBankedCash(message.bankedCash || 0);
        setHand(message.hand || []);
        setBankedInvestments(message.investments || []);
        setInvestmentMultiplier(message.investmentMultiplier || []);

        setRoundActionMessages((prev) => [
          ...prev,
          message.roundActions ? message.roundActions : "No actions this round",
        ]);

        console.log(message.roundActions);
      }
    };

    if (socket.current) {
      socket.current.addEventListener("open", handleOpen);
      socket.current.addEventListener("close", handleClose);

      const unregisterHandlePickCard = registerHandler(WS_MESSAGE_TYPES.PICK_CARD, handlePickCardMessage);
      const unregisterHandlePickInvestment = registerHandler(WS_MESSAGE_TYPES.PICK_INVESTMENT, handlePickInvestmentMessage);
      const unregisterHandleRoundEnd = registerHandler(WS_MESSAGE_TYPES.ROUND_END, handleRoundEnd);

      return () => {
        socket.current?.removeEventListener("open", handleOpen);
        socket.current?.removeEventListener("close", handleClose);
        unregisterHandlePickCard();
        unregisterHandlePickInvestment();
        unregisterHandleRoundEnd();
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
    if (selectedCard) return; // Prevent further selection if a card is already selected
    setSelectedCard(card);
    chooseCard(lobbyId, index);
  };

  const handleInvestmentSelect = (investment, index) => {
    if (selectedInvestment) return; // Prevent further selection if an investment is already selected
    setSelectedInvestment(investment);
    chooseInvestment(lobbyId, index);
  };

  const handleUseCard = (index) => {
    setHand((prevHand) => {
      const updatedHand = [...prevHand];
      updatedHand.splice(index, 1); // Remove the card at the specified index
      return updatedHand;
    });

    activateCard(lobbyId, index);
  };


  return (
      <div>
        <div>
          <h2>Game</h2>
          <h3>Hand</h3>
          {hand.length === 0 ? (
              <p>Your hand is empty.</p>
          ) : (
              hand.map((card, index) => (
                  <button
                      key={index}
                      onClick={() => handleUseCard(index)}
                  >
                    {card}
                  </button>
              ))
          )}

          <h3>Banked Investments</h3>
          {bankedInvestments.length === 0 ? (
              <p>You have no investments yet.</p>
          ) : (
              bankedInvestments.map((investment, index) => <div key={index}>{investment}</div>)
          )}

          <h3>Choose an Investment</h3>
          {selectedInvestment ? (
              <p>You selected: {selectedInvestment}</p>
          ) : investments.length === 0 ? (
              <p>No investments available.</p>
          ) : (
              investments.map((investment, index) => (
                  <button
                      key={index}
                      onClick={() => handleInvestmentSelect(investment, index)}
                      disabled={!!selectedInvestment}
                  >
                    {investment}
                  </button>
              ))
          )}

          <h3>Choose a Card</h3>
          {selectedCard ? (
              <p>You selected: {selectedCard}</p>
          ) : cards.length === 0 ? (
              <p>No cards received yet.</p>
          ) : (
              cards.map((card, index) => (
                  <button
                      key={index}
                      onClick={() => handleCardSelect(card, index)}
                      disabled={!!selectedCard}
                  >
                    {card}
                  </button>
              ))
          )}
          <h3>Round Action Messages</h3>
          {roundActionMessages.length === 0 ? (
              <p>No actions recorded yet.</p>
          ) : (
              <ul>
                {roundActionMessages.map((action, index) => (
                    <li key={index}>
                      <strong>Round {index + 1}:</strong> {action}
                    </li>
                ))}
              </ul>
          )}
        </div>
        <div>
          <h2>Player Information</h2>
          <p>On Hand Cash: ${onHandCash}</p>
          <p>Banked Cash: ${bankedCash}</p>
          <p>Investment Multiplier: {investmentMultiplier}</p>
        </div>
        <button onClick={endTurn}>End Turn</button>
      </div>
  );
}

export default Game;
