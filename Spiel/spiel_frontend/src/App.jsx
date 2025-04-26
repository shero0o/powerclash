import React, { useState } from 'react';
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

    // Sobald wir eine roomId haben, zeigen wir sie nur an
    return (
        <div className="flex flex-col items-center justify-center h-screen space-y-4">
            <h1 className="text-2xl font-semibold">Your Room ID</h1>
            <p className="text-lg font-mono">{roomId}</p>
            <p className="text-sm text-gray-600">Player ID: {playerId}</p>
            <button
                onClick={() => setRoomId(null)}
                className="px-4 py-2 bg-gray-300 rounded hover:bg-gray-400"
            >
                Back
            </button>
        </div>
    );
}