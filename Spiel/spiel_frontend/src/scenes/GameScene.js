// src/scenes/GameScene.js
// (based on turn8file1, with health-bar + collision fixes included)
import Phaser from 'phaser';

export default class GameScene extends Phaser.Scene {
    constructor() { super({ key: 'GameScene' }); }

    init(data) {
        this.roomId      = data.roomId;
        this.playerId    = data.playerId;
        this.mapKey = data.levelId;
        this.latestState = null;
        this.playerSprites = {};
        this.bulletSprites = {};
        this.initialZoom = 0.7;
        this.maxHealth   = 100;
        this.playerCountText = null;

    }


    preload() {
        this.load.image('player', '/assets/PNG/Hitman_1/hitman1_gun.png');
        this.load.tilemapTiledJSON('map', '/assets/mapAli.tmj');
        //this.load.tilemapTiledJSON('map', `/assets/${this.mapKey}.tmj`);
        this.load.image('tileset', '/assets/Tilesheet/spritesheet_tiles.png');
    }

    create() {
        this.cameras.main.setBackgroundColor('#222222');
        const map = this.make.tilemap({ key: 'map' });
        const tileset = map.addTilesetImage('spritesheet_tiles','tileset',64,64);
        map.createLayer('Boden', tileset, 0,0);
        map.createLayer('Wand',  tileset, 0,0);

        this.keys = this.input.keyboard.addKeys({
            up:'W', down:'S', left:'A', right:'D'
        });
        this.playerCountText = this.add.text(0, 0, '0/0 players', {
            fontFamily: 'Arial',
            fontSize: 32, // Jetzt als Zahl
            fontStyle: 'bold',
            color: '#ffffff',
            stroke: '#000000',
            strokeThickness: 4,
            align: 'left'
        })
            .setScrollFactor(0)
            .setOrigin(0, 0)
            .setDepth(10);




        this.exitButton = this.add
            .text(16,16,'Exit',{ fontSize:'18px', fill:'#ff0000' })
            .setScrollFactor(0)
            .setInteractive()
            .setVisible(false)
            .on('pointerdown', () => {
                this.socket.emit('leaveRoom', {
                    roomId:   this.roomId,
                    playerId: this.playerId
                });
                this.socket.disconnect();
                this.scene.start('SplashScene');
            });

        this.input.on('pointerdown', pointer => {
            const me = this.latestState?.players.find(p => p.playerId===this.playerId);
            if (!me || me.currentHealth<=0) return;
            const world = this.cameras.main.getWorldPoint(pointer.x,pointer.y);
            const angle = Phaser.Math.Angle.Between(
                me.position.x, me.position.y, world.x, world.y
            );
            this.socket.emit('attack', {
                roomId:   this.roomId,
                playerId: this.playerId,
                angle
            });
        });

        this.socket.on('stateUpdate', state => {
            this.latestState = state;
        });
    }

    update() {
        if (!this.latestState) return;
        const connected = this.latestState.players.filter(p => p.currentHealth > 0).length;
        const total = 2;
        this.playerCountText.setText(`${connected}/${total} players`);



        // remove dead players & their bars
        Object.entries(this.playerSprites).forEach(([id,spr]) => {
            const p = this.latestState.players.find(x => x.playerId===id);
            if (p && p.currentHealth <= 0) {
                spr.healthBar.destroy();
                spr.destroy();
                delete this.playerSprites[id];
            }
        });

        const cam = this.cameras.main;
        const me = this.latestState.players.find(p=>p.playerId===this.playerId);
        if (me && me.currentHealth <= 0) this.exitButton.setVisible(true);

        // draw players
        this.latestState.players.forEach(p => {
            if (p.currentHealth <= 0) return;
            let spr = this.playerSprites[p.playerId];
            if (!spr) {
                spr = this.physics.add.sprite(p.position.x,p.position.y,'player');
                spr.setOrigin(0.5);
                spr.healthBar = this.add.graphics();
                this.playerSprites[p.playerId] = spr;
                if (p.playerId===this.playerId) {
                    cam.startFollow(spr);
                    cam.setZoom(this.initialZoom);
                }
            }

            spr.setPosition(p.position.x,p.position.y);
            spr.setRotation(p.position.angle);
            spr.setVisible(p.visible);

            // draw HP bar
            const barW=40, barH=6;
            const pct = Phaser.Math.Clamp(p.currentHealth/this.maxHealth,0,1);
            spr.healthBar
                .clear()
                .fillStyle(0x000000)
                .fillRect(
                    p.position.x-barW/2-1,
                    p.position.y-spr.height/2-barH-9,
                    barW+2,barH+2
                )
                .fillStyle(0x00ff00)
                .fillRect(
                    p.position.x-barW/2,
                    p.position.y-spr.height/2-barH-8,
                    barW*pct,barH
                );

            // local player movement
            if (p.playerId===this.playerId) {
                const ptr = this.input.activePointer;
                const worldPt = cam.getWorldPoint(ptr.x,ptr.y);
                const angle = Phaser.Math.Angle.Between(
                    p.position.x,p.position.y,worldPt.x,worldPt.y
                );
                const dirX = (this.keys.left.isDown?-1:0)+(this.keys.right.isDown?1:0);
                const dirY = (this.keys.up.isDown?-1:0)+(this.keys.down.isDown?1:0);
                this.socket.emit('move', {
                    roomId:   this.roomId,
                    playerId: this.playerId,
                    dirX, dirY, angle
                });
            }
        });

        // bullets
        const seen = new Set();
        (this.latestState.events||[]).forEach(evt => {
            if (evt.type==='ATTACK') {
                const b = evt.data;
                seen.add(b.bulletId);
                let circ = this.bulletSprites[b.bulletId];
                if (!circ) circ = this.add.circle(b.x,b.y,4).setDepth(1);
                circ.setPosition(b.x,b.y);
                this.bulletSprites[b.bulletId] = circ;
            }
        });
        Object.keys(this.bulletSprites).forEach(id => {
            if (!seen.has(id)) {
                this.bulletSprites[id].destroy();
                delete this.bulletSprites[id];
            }
        });
    }
}