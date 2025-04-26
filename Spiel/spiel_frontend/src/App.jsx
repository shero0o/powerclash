import React, { useState } from 'react';
import PhaserGame from './components/PhaserGame';
import './index.css';

export default function App() {
    const [roomId, setRoomId] = useState('');
    const [startJoin, setStartJoin] = useState(false);

    return (
        <div className="App flex flex-col items-center justify-center h-screen bg-gray-100">
            {!startJoin ? (
                <div className="space-y-4">
                    <input
                        type="text"
                        placeholder="Enter Room ID"
                        value={roomId}
                        onChange={e => setRoomId(e.target.value)}
                        className="px-4 py-2 border rounded w-64 focus:outline-none"
                    />
                    <button
                        disabled={!roomId}
                        className="px-6 py-3 text-lg font-semibold bg-blue-600 text-white rounded hover:bg-blue-700 transition disabled:opacity-50"
                        onClick={() => setStartJoin(true)}
                    >
                        Play
                    </button>
                </div>
            ) : (
                <PhaserGame roomId={roomId} onBack={() => setStartJoin(false)} />
            )}
        </div>
    );
}
