// src/components/PhaserGame.jsx
import React, { useEffect, useRef } from 'react';
import Phaser from 'phaser';
import io from 'socket.io-client';

import SplashScene    from '../scenes/SplashScene';
import WaitingScene   from '../scenes/WaitingScene';
import PreloadScene   from '../scenes/PreloadScene';
import CountdownScene from '../scenes/CountdownScene';
import GameScene      from '../scenes/GameScene';

export default function PhaserGame() {
    const containerRef = useRef(null);

    useEffect(() => {
        if (!containerRef.current) return;

        // 1) Socket einmal erstellen
        const socket = io('http://localhost:8081');

        socket.on('connect', () => {
            console.log('✅ Verbunden mit Socket.IO, Client-ID:', socket.id);
        });
        socket.on('connect_error', (err) => {
            console.error('❌ Socket.IO-Verbindungsfehler:', err);
        });


        // 2) Szeneninstanzen erzeugen
        const splash    = new SplashScene();
        const waiting   = new WaitingScene();
        const preload   = new PreloadScene();
        const countdown = new CountdownScene();
        const gameScene = new GameScene();

        // 3) Socket in die Szenen injecten
        [splash, waiting, gameScene].forEach(scene => {
            scene.socket = socket;
        });

        // 4) Phaser-Game starten
        const game = new Phaser.Game({
            type: Phaser.AUTO,
            parent: containerRef.current,
            width: 1280,
            height: 720,
            physics: { default: 'arcade', arcade: { debug: false } },
            scene: [splash, waiting, preload, countdown, gameScene],
        });

        return () => game.destroy(true);
    }, []);

    return <div ref={containerRef} style={{ width: '100%', height: '100%' }} />;
}
