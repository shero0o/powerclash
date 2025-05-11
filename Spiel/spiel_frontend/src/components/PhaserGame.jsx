// src/components/PhaserGame.jsx
import React, { useEffect, useRef } from 'react';
import Phaser from 'phaser';
import io from 'socket.io-client';

import SplashScene    from '../scenes/SplashScene';    // originally turn8file3
import WaitingScene   from '../scenes/WaitingScene';   // originally turn8file4
import GameScene      from '../scenes/GameScene';      // originally turn8file1

export default function PhaserGame() {
    const containerRef = useRef(null);

    useEffect(() => {
        if (!containerRef.current) return;

        // 1) single socket for all scenes
        const socket = io('http://localhost:8081');

        const scenes = [
            new SplashScene(),
            new WaitingScene(),
            new GameScene()
        ];
        // inject socket into each scene instance
        scenes.forEach(s => s.socket = socket);

        const config = {
            type: Phaser.AUTO,
            parent: containerRef.current,
            width: 1280,
            height: 720,
            scale: {
                mode: Phaser.Scale.FIT,               // keep 16:9 aspect
                autoCenter: Phaser.Scale.CENTER_BOTH, // center in container
            },
            backgroundColor: '#222222',             // dark BG behind scenes
            physics: { default: 'arcade', arcade: { debug: false } },
            scene: scenes
        };

        const game = new Phaser.Game(config);

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
                width: '100%',
                height: '100%',
                background: '#6a0dad', // purple behind the canvas
                overflow: 'hidden'
            }}
        />
    );
}
