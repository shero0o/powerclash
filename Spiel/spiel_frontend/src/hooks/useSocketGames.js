import { useEffect, useState, useRef } from 'react';
import io from 'socket.io-client';

export default function useSocketStatus(roomId, playerId) {
    const socketRef = useRef(null);
    const [isConnected, setIsConnected] = useState(false);

    useEffect(() => {
        if (!roomId) return;

        const socket = io('http://localhost:8081', {
            transports: ['polling', 'websocket'],
            query: { roomId, playerId }
        });
        socketRef.current = socket;

        socket.on('connect', () => setIsConnected(true));
        socket.on('disconnect', () => setIsConnected(false));

        return () => {
            socket.disconnect();
        };
    }, [roomId, playerId]);

    return { socket: socketRef.current, isConnected };
}
