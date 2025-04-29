// src/components/PhaserGame.jsx
import React, { useEffect, useRef } from 'react';
import Phaser from 'phaser';
import io from 'socket.io-client';

import SplashScene from '../scenes/SplashScene';
import WaitingScene from '../scenes/WaitingScene';
import PreloadScene from '../scenes/PreloadScene';
import CountdownScene from '../scenes/CountdownScene';
import GameScene from '../scenes/GameScene';

export default function PhaserGame() {
    const containerRef = useRef(null);

    useEffect(() => {
        if (!containerRef.current) return;

        const socket = io('http://localhost:8081');
        const scenes = [
            new SplashScene(),
            new WaitingScene(),
            new PreloadScene(),
            new CountdownScene(),
            new GameScene()
        ];
        scenes.forEach(scene => scene.socket = socket);

        const game = new Phaser.Game({
            type: Phaser.AUTO,
            parent: containerRef.current,
            width: 1280,
            height: 720,
            physics: { default: 'arcade', arcade: { debug: false } },
            scene: scenes,
        });

        return () => {
            socket.close();
            game.destroy(true);
        };
    }, []);

    return (
        <div
            id="phaser-container"
            ref={containerRef}
            style={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                width: '100vw',
                height: '100vh',
                overflow: 'hidden',      // prevent scrollbars
                background: '#6a0dad',   // your purple bg
            }}
        />
    );
}
