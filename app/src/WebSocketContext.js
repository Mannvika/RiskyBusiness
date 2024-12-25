import React, { createContext, useContext, useEffect, useRef } from "react";

export const WS_MESSAGE_TYPES = {
    JOIN_LOBBY: 'JOIN_LOBBY',
    CREATE_LOBBY: 'CREATE_LOBBY',
    READY: 'READY',
    ROUND_READY: 'ROUND_READY',
    LOBBY_UPDATE: 'LOBBY_UPDATE',
    GAME_STARTING: 'GAME_STARTING',
    LOBBY_CREATED: 'LOBBY_CREATED',
    LOBBY_JOINED: 'LOBBY_JOINED',
    PICK_CARD: 'PICK_CARD',
    CHOOSE_CARD: 'CHOOSE_CARD',
    ERROR: 'ERROR'
};

const WebSocketContext = createContext(null);

export const WebSocketProvider = ({ children }) => {
    const socketRef = useRef(null);
    const handlersRef = useRef(new Map());

    const registerHandler = (messageType, handler) => {
        if (!handlersRef.current.has(messageType)) {
            handlersRef.current.set(messageType, new Set());
        }
        handlersRef.current.get(messageType).add(handler);
        return () => handlersRef.current.get(messageType).delete(handler);
    };

    const sendMessage = (type, data = {}) => {
        if (socketRef.current?.readyState === WebSocket.OPEN) {
            // Remove any undefined or null values and ensure proper structure
            const cleanData = Object.entries(data).reduce((acc, [key, value]) => {
                if (value != null) {
                    acc[key] = value;
                }
                return acc;
            }, {});

            const message = JSON.stringify({
                type,
                ...cleanData,
                timestamp: new Date().toISOString()
            });

            return socketRef.current.debugSend(message);
        }
        console.error('WebSocket not connected');
        return false;
    };

    useEffect(() => {
        if (!socketRef.current) {
            const socket = new WebSocket("ws://localhost:8080/game");

            socket.onopen = (event) => {
                console.log("WebSocket Connected:", {
                    timestamp: new Date().toISOString(),
                    readyState: socket.readyState,
                    binaryType: socket.binaryType,
                    url: socket.url,
                    protocol: socket.protocol,
                    event: event
                });
            };

            socket.onmessage = (event) => {
                const timestamp = new Date().toISOString();
                try {
                    const data = JSON.parse(event.data);
                    console.log("Message Received:", {
                        timestamp,
                        data,
                        readyState: socket.readyState,
                        bufferedAmount: socket.bufferedAmount
                    });

                    // Dispatch to registered handlers
                    const handlers = handlersRef.current.get(data.type);
                    if (handlers) {
                        handlers.forEach(handler => handler(data));
                    }
                } catch (e) {
                    console.error("Message Parse Error:", {
                        timestamp,
                        error: e,
                        rawData: event.data
                    });
                }
            };

            socket.onerror = (error) => {
                console.error("WebSocket Error:", {
                    timestamp: new Date().toISOString(),
                    error,
                    readyState: socket.readyState
                });
            };

            socket.onclose = (event) => {
                console.log("WebSocket Closed:", {
                    timestamp: new Date().toISOString(),
                    code: event.code,
                    reason: event.reason,
                    wasClean: event.wasClean
                });
            };

            socket.debugSend = function(data) {
                const timestamp = new Date().toISOString();
                console.log("Attempting to send message:", {
                    timestamp,
                    data,
                    readyState: this.readyState,
                    bufferedAmount: this.bufferedAmount
                });

                try {
                    this.send(data);
                    console.log("Message sent successfully:", {
                        timestamp: new Date().toISOString(),
                        bufferedAmount: this.bufferedAmount
                    });
                    return true;
                } catch (error) {
                    console.error("Send failed:", {
                        timestamp: new Date().toISOString(),
                        error,
                        data
                    });
                    return false;
                }
            };

            socketRef.current = socket;
        }

        return () => {
            if (socketRef.current?.readyState === WebSocket.OPEN) {
                socketRef.current.close();
                socketRef.current = null;
            }
        };
    }, []);

    const contextValue = {
        socket: socketRef,
        registerHandler,
        sendMessage,
        // Common message senders
        createLobby: (playerName) =>
            sendMessage(WS_MESSAGE_TYPES.CREATE_LOBBY, { playerName }),
        joinLobby: (lobbyId, playerName) =>
            sendMessage(WS_MESSAGE_TYPES.JOIN_LOBBY, { lobbyId, playerName }),
        markReady: (lobbyId) =>
            sendMessage(WS_MESSAGE_TYPES.READY, { lobbyId }),
        markRoundReady: (lobbyId) =>
            sendMessage(WS_MESSAGE_TYPES.ROUND_READY, { lobbyId }),
        chooseCard: (lobbyId, cardIndex) =>
            sendMessage(WS_MESSAGE_TYPES.CHOOSE_CARD, { lobbyId, cardIndex })
    };

    return (
        <WebSocketContext.Provider value={contextValue}>
            {children}
        </WebSocketContext.Provider>
    );
};

export const useWebSocket = () => {
    const context = useContext(WebSocketContext);
    if (!context) {
        throw new Error("useWebSocket must be used within a WebSocketProvider");
    }
    return context;
};