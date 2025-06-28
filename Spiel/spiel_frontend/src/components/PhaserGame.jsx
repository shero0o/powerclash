import React, { useEffect, useRef } from 'react';
import Phaser from 'phaser';
import io from 'socket.io-client';

import InventoryScene from "../scenes/InventoryScene.js";
import WaitingScene   from '../scenes/WaitingScene';
import GameScene      from '../scenes/GameScene';
import LobbyScene from "../scenes/LobbyScene.js";
import ShopScene from "../scenes/ShopScene.js";
import AccountScene from "../scenes/AccountScene.js";

export default function PhaserGame() {
    const containerRef = useRef(null);

    useEffect(() => {
        if (!containerRef.current) return;

        const playerId = localStorage.getItem('playerId');
        const roomId   = localStorage.getItem('roomId');

        const socket = io('http://localhost:8081', {

            autoConnect: false,
            query: { playerId, roomId }
        });
        console.log('Setting up socket connection...');

        const beforeUnloadHandler = () => {
            if (roomId && playerId) {
                socket.emit('leaveRoom', { roomId, playerId });
            }
            socket.disconnect();
        };

        window.addEventListener('beforeunload', beforeUnloadHandler);

        const scenes = [
            new LobbyScene(),
            new InventoryScene(),
            new WaitingScene(),
            new GameScene(),
            new ShopScene(),
            new AccountScene()
        ];
        scenes.forEach(s => s.socket = socket);

        const config = {
            type: Phaser.AUTO,
            parent: containerRef.current,
            width: window.innerWidth,
            height: window.innerHeight,
            scale: {
                mode: Phaser.Scale.FIT,
                autoCenter: Phaser.Scale.CENTER_BOTH,
            },
            backgroundColor: '#222222',
            physics: { default: 'arcade', arcade: { debug: false } },
            scene: scenes
        };

        const game = new Phaser.Game(config);

        return () => {
            window.removeEventListener('beforeunload', beforeUnloadHandler);
            socket.disconnect();
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
                background: '#6a0dad',
                overflow: 'hidden'
            }}
        />
    );
}
