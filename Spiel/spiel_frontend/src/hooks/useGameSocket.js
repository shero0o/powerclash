import { useEffect, useState, useRef, useCallback } from 'react';

export default function useGameSocket(roomId) {
    const wsRef = useRef(null);
    const reconnectRef = useRef(null);
    const [gameState, setGameState] = useState({ players: [], projectiles: [] });
    const [isJoined, setIsJoined] = useState(false);
    const [joinError, setJoinError] = useState(null);

    const connect = useCallback(() => {
        setJoinError(null);
        const wsUrl = import.meta.env.DEV
            ? 'ws://localhost:8080/ws/game'
            : `${window.location.protocol.replace(/^http/, 'ws')}://${window.location.host}/ws/game`;

        const ws = new WebSocket(wsUrl);
        ws.onopen = () => {
            console.log('✅ WebSocket connected to', wsUrl);
            // send join request
            ws.send(JSON.stringify({ type: 'JOIN', roomId }));
            console.log('➡️ Sent JOIN for room', roomId);
        };
        ws.onmessage = e => {
            try {
                const msg = JSON.parse(e.data);
                switch (msg.type) {
                    case 'JOIN_ACK':
                        setIsJoined(true);
                        console.log('✅ Joined room', roomId);
                        break;
                    case 'JOIN_ERROR':
                        setJoinError(msg.error || 'Failed to join room');
                        ws.close();
                        break;
                    case 'STATE_UPDATE':
                        setGameState({ players: msg.players || [], projectiles: msg.projectiles || [] });
                        break;
                    default:
                        console.warn('Unhandled WS message', msg);
                }
            } catch (err) {
                console.error('Invalid JSON', err);
            }
        };
        ws.onclose = () => {
            if (!isJoined) {
                setJoinError(`Unable to join room ${roomId}`);
            } else {
                console.log('❌ Disconnected, retrying in 1s');
                reconnectRef.current = setTimeout(connect, 1000);
            }
        };
        ws.onerror = err => console.error('🚨 WebSocket error', err);

        wsRef.current = ws;
    }, [roomId, isJoined]);

    useEffect(() => {
        if (!roomId) return;
        connect();
        return () => {
            wsRef.current?.close();
            clearTimeout(reconnectRef.current);
        };
    }, [connect, roomId]);

    const sendMove = useCallback((dx, dy) => {
        if (wsRef.current?.readyState === WebSocket.OPEN) {
            wsRef.current.send(JSON.stringify({ type: 'MOVE', playerId: 'self', payload: { dx, dy } }));
        }
    }, []);

    const sendAttack = useCallback((tx, ty) => {
        if (wsRef.current?.readyState === WebSocket.OPEN) {
            wsRef.current.send(JSON.stringify({ type: 'ATTACK', playerId: 'self', payload: { targetX: tx, targetY: ty } }));
        }
    }, []);

    return { sendMove, sendAttack, gameState, isJoined, joinError };
}

