import React, { useEffect, useState } from 'react';
import { useParams } from "react-router-dom";
import { useWebSocket } from "../WebSocketContext";

function Game() {
  const { lobbyId } = useParams();
  const { socket, markRoundReady } = useWebSocket();
  const [debugInfo, setDebugInfo] = useState({
    readyAttempts: 0,
    lastAttemptTime: null,
    socketState: "initializing"
  });

  useEffect(() => {
    const handleOpen = () => {
      setDebugInfo(prev => ({...prev, socketState: "connected"}));
    };

    const handleClose = () => {
      setDebugInfo(prev => ({...prev, socketState: "disconnected"}));
    };

    if (socket.current) {
      socket.current.addEventListener('open', handleOpen);
      socket.current.addEventListener('close', handleClose);

      return () => {
        socket.current?.removeEventListener('open', handleOpen);
        socket.current?.removeEventListener('close', handleClose);
      };
    }
  }, [socket]);

  const endTurn = () => {
    setDebugInfo(prev => ({
      ...prev,
      readyAttempts: prev.readyAttempts + 1,
      lastAttemptTime: new Date().toISOString()
    }));

    markRoundReady(lobbyId);
  };

  return (
      <div>
        <div>
          <h2>Debug Information</h2>
          <pre>
                    {JSON.stringify({
                      socketState: debugInfo.socketState,
                      readyAttempts: debugInfo.readyAttempts,
                      lastAttempt: debugInfo.lastAttemptTime,
                      socketReadyState: socket?.readyState
                    }, null, 2)}
                </pre>
        </div>
        <button onClick={endTurn}>Test</button>
      </div>
  );
}

export default Game;
