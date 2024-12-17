import React, { createContext, useContext, useEffect, useRef } from "react";

const WebSocketContext = createContext(null);

export const WebSocketProvider = ({ children }) => {
    const socketRef = useRef(null);

    useEffect(() => {
        // Prevent reinitialization if WebSocket already exists
        if (!socketRef.current) {
            const socket = new WebSocket("ws://localhost:8080/game");

            socket.onopen = () => {
                console.log("WebSocket connected");
            };

            socket.onmessage = (event) => {
                const data = JSON.parse(event.data);
                console.log("Message from server:", data);
            };

            socket.onerror = (error) => {
                console.error("WebSocket error:", error);
            };

            socket.onclose = () => {
                console.log("WebSocket disconnected");
            };

            // Store the socket reference
            socketRef.current = socket;
        }

        return () => {
            // Only close WebSocket if it's open
            if (socketRef.current?.readyState === WebSocket.OPEN) {
                socketRef.current.close();
                socketRef.current = null; // Clear the reference
            }
        };
    }, []);

    return (
        <WebSocketContext.Provider value={socketRef}>
            {children}
        </WebSocketContext.Provider>
    );
};

// Custom hook to use WebSocket
export const useWebSocket = () => {
    const context = useContext(WebSocketContext);
    if (!context) {
        throw new Error("useWebSocket must be used within a WebSocketProvider");
    }
    return context;
};
