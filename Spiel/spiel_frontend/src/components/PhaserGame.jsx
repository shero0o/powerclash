import React, { useEffect, useRef } from 'react';
import Phaser from 'phaser';
import GameScene from '../scenes/GameScene';
import useGameSocket from '../hooks/useGameSocket';

export default function PhaserGame({ roomId, onBack }) {
    const containerRef = useRef(null);
    const { sendMove, sendAttack, gameState, isJoined, joinError } = useGameSocket(roomId);

    // If join failed, display error and allow retry
    if (joinError) {
        return (
            <div className="flex flex-col items-center space-y-4">
                <p className="text-red-600">{joinError}</p>
                <button
                    className="px-4 py-2 bg-gray-300 rounded hover:bg-gray-400"
                    onClick={onBack}
                >
                    Back
                </button>
            </div>
        );
    }

    // If not joined yet, show connecting message
    if (!isJoined) {
        return (
            <div className="p-4 text-gray-700">
                Connecting to room "{roomId}"...
            </div>
        );
    }

    // Once joined, render Phaser
    const stateRef = useRef(gameState);
    useEffect(() => { stateRef.current = gameState; }, [gameState]);

    useEffect(() => {
        if (!containerRef.current) return;
        const scene = new GameScene();
        scene.sendMove = sendMove;
        scene.sendAttack = sendAttack;
        scene.getState = () => stateRef.current;

        const game = new Phaser.Game({
            type: Phaser.AUTO,
            parent: containerRef.current,
            width: 1280,
            height: 720,
            physics: { default: 'arcade', arcade: { debug: false } },
            scene: [scene]
        });

        return () => game.destroy(true);
    }, [sendMove, sendAttack]);

    return <div id="phaser-container" ref={containerRef} />;
}
