import React, { useState, useEffect, useRef } from 'react';
import { io } from 'socket.io-client';
import './index.css';

// Helper: get or generate a unique playerId, persisted in localStorage
function getOrCreatePlayerId() {
    let id = localStorage.getItem('playerId');
    if (!id) {
        id = crypto?.randomUUID?.() || `player-${Date.now()}`;
        localStorage.setItem('playerId', id);
    }
    return id;
}

export default function App() {
    const [roomId, setRoomId] = useState(null);
    const [error, setError] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    const [playerId] = useState(getOrCreatePlayerId);
    const [messages, setMessages] = useState([]);
    const messageRef = useRef();
    const socketRef = useRef(null);

    const handlePlay = async () => {
        setError(null);
        setIsLoading(true);
        try {
            const res = await fetch('http://localhost:8080/api/rooms/join', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ playerId })
            });
            if (!res.ok) throw new Error(`Server responded with ${res.status}`);
            const { roomId: newRoomId } = await res.json();
            setRoomId(newRoomId);
        } catch (err) {
            setError(err.message || 'Unable to join room');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        if (!roomId) return;

        socketRef.current = io('http://localhost:8081', {
            query: { roomId, playerId },
        });

        socketRef.current.on('connect', () => {
            console.log('Connected to socket.io server');
        });

        socketRef.current.on('message', (message) => {
            setMessages(prev => [...prev, message]);
        });

        return () => {
            if (socketRef.current) {
                socketRef.current.disconnect();
            }
        };
    }, [roomId, playerId]);

    const handleSendMessage = () => {
        const text = messageRef.current.value.trim();
        if (text && socketRef.current) {
            socketRef.current.emit('message', { playerId, text });
            messageRef.current.value = '';
        }
    };

    if (error) {
        return (
            <div className="flex flex-col items-center space-y-4 p-4">
                <p className="text-red-600">Error: {error}</p>
                <button
                    onClick={handlePlay}
                    className="px-4 py-2 bg-gray-300 rounded hover:bg-gray-400"
                >
                    Retry
                </button>
            </div>
        );
    }

    if (!roomId) {
        return (
            <div className="flex items-center justify-center h-screen">
                <button
                    onClick={handlePlay}
                    disabled={isLoading}
                    className="px-6 py-3 text-lg font-semibold bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50 transition"
                >
                    {isLoading ? 'Joining...' : 'Play'}
                </button>
            </div>
        );
    }

    return (
        <div className="flex flex-col items-center justify-center h-screen space-y-4">
            <h1 className="text-2xl font-semibold">Your Room ID</h1>
            <p className="text-lg font-mono">{roomId}</p>
            <p className="text-sm text-gray-600">Player ID: {playerId}</p>
            <div className="flex space-x-2">
                <input
                    ref={messageRef}
                    type="text"
                    placeholder="Type a message"
                    className="border rounded p-2"
                />
                <button
                    onClick={handleSendMessage}
                    className="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600"
                >
                    Send
                </button>
            </div>
            <div className="border rounded w-full max-w-md h-64 overflow-y-auto p-2">
                {messages.map((msg, idx) => (
                    <div key={idx} className="mb-2">
                        <span className="font-semibold">{msg.sender || 'Server'}:</span> {msg.text}
                    </div>
                ))}
            </div>
            <button
                onClick={() => setRoomId(null)}
                className="px-4 py-2 bg-gray-300 rounded hover:bg-gray-400"
            >
                Back
            </button>
        </div>
    );
}
