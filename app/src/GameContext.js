import React, { createContext, useState, useContext } from 'react';

const GameContext = createContext();

export const GameProvider = ({ children }) => {
  const [gameData, setGameData] = useState({
    playerName: '',
    gameID: '',
    playerReady: false,
  });

  return (
    <GameContext.Provider value={{ gameData, setGameData }}>
      {children}
    </GameContext.Provider>
  );
};

export const useGameContext = () => useContext(GameContext);