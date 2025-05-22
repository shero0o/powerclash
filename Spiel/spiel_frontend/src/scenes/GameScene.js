import Phaser from 'phaser';

export default class GameScene extends Phaser.Scene {
    constructor() {
        super({ key: 'GameScene' });

        this.isFiring          = false;
        this.fireEvent         = null;

        this.victoryText = null;
        // this.playAgainButton = null;
        this.hasWon = false;
        this.exitButtonGame = null;
        this.defeatShown = false;



    }

    init(data) {
        this.roomId            = data.roomId;
        this.playerId          = data.playerId;
        this.mapKey            = data.levelId || this.registry.get('levelId') || 'level1';
        const regWeapon = this.registry.get('weapon');
        this.selectedWeapon    = data.chosenWeapon || regWeapon || 'RIFLE_BULLET';
        this.selectedBrawler = this.registry.get('brawler') || 'sniper';
        this.playerName = this.registry.get('playerName') || 'Player';
        this.latestState       = null;
        this.playerSprites     = {};
        this.projectileSprites = {};
        this.initialZoom       = 0.7;
        this.maxHealth         = 100;
        this.playerCountText   = null;

    }

    preload() {
        // Player & projectiles
        this.load.image('rifle_bullet',   '/assets/PNG/projectile/rifle.png');
        this.load.image('sniper',         '/assets/PNG/projectile/sniper.png');
        this.load.image('shotgun_pellet', '/assets/PNG/projectile/shotgun.png');
        this.load.image('mine',           '/assets/PNG/explosion/bomb.png');

        // Brawler Sprites
        this.load.image('brawler_sniper', '/assets/PNG/Hitman_1/hitman1_gun.png');
        this.load.image('brawler_tank', '/assets/PNG/Robot_1/robot1_machine.png');
        this.load.image('brawler_mage', '/assets/PNG/Soldier_1/soldier1_silencer.png');
        this.load.image('brawler_healer', '/assets/PNG/Woman_Green/womanGreen_machine.png');

        for (let i = 0; i < 25; i++) {
            this.load.image(`explosion${i}`, `/assets/PNG/explosion/explosion${i}.png`);
        }

        // Map tileset & tilemap
        this.load.image('tileset', '/assets/Tilesheet/spritesheet_tiles.png');
        let mapFile;
        if (this.mapKey === 'level1') {
            mapFile = 'map1.tmj';
        } else {
            mapFile = 'map2.0.tmj'; // shared by level2 and level3
        }

        this.load.tilemapTiledJSON('map', `/assets/${mapFile}`);
    }

    create() {
        // Hintergrundfarbe
        this.cameras.main.setBackgroundColor('#222222');

        this.tileSize = 64;

        // Karte & Layer
        const map = this.make.tilemap({ key: 'map' });
        this.map = this.make.tilemap({ key: 'map' });
        const tileset = map.addTilesetImage('spritesheet_tiles','tileset',64,64);
        if (this.mapKey === 'level1') {
            map.createLayer('Boden', tileset, 0, 0);
            map.createLayer('Wand', tileset, 0, 0);
            this.obstacleLayer = map.createLayer('Wand', tileset, 0, 0);
        } else if (this.mapKey === 'level2'|| this.mapKey === 'level3'){
            map.createLayer('Boden', tileset, 0, 0);
            map.createLayer('Gebüsch, Giftzone, Energiezone', tileset, 0, 0);
            map.createLayer('Kisten', tileset, 0, 0);
            map.createLayer('Wand', tileset, 0, 0);
            this.obstacleLayer = map.createLayer('Wand', tileset, 0, 0);
        }


        // Physik-Welt & Kamera
        const width  = map.widthInPixels;
        const height = map.heightInPixels;
        this.physics.world.setBounds(0, 0, width, height);
        this.cameras.main.setBounds(0, 0, width, height);

        // Zoom auf ganze Karte
        const fitZoom = Math.min(this.cameras.main.width / width, this.cameras.main.height / height);
        this.cameras.main.setZoom(fitZoom);
        this.cameras.main.centerOn(width/2, height/2);
        this.input.topOnly = true;


        // UI: Ammo-Bar
        this.ammoBarBg   = this.add.graphics().setScrollFactor(0);
        this.ammoBarFill = this.add.graphics().setScrollFactor(0);

        // UI: Spielerzahl
        this.playerCountText = this.add.text(16, 16, '0/0 players', {
            fontFamily: 'Arial', fontSize: '24px', fontStyle: 'bold', color: '#ffffff', stroke: '#000000', strokeThickness: 3
        }).setScrollFactor(0);


        // Explosion-Animation
        this.anims.create({
            key: 'explode',
            frames: Array.from({ length: 25 }, (_, i) => ({ key: `explosion${i}` })),
            frameRate: 25,
            repeat: 0,
            hideOnComplete: true
        });
        this.explosionGroup = this.add.group();
        this.prevProjectileIds     = new Set();
        this.previousMinePositions = {};



        // Eingabe: WASD
        this.keys = this.input.keyboard.addKeys({ up: 'W', down: 'S', left: 'A', right: 'D' });
        // Eingabe: Schuss
        this.input.on('pointerdown', pointer => this.startFiring(pointer));
        this.input.on('pointerup',   ()      => this.stopFiring());
        this.input.on('pointerout',  ()      => this.stopFiring());

        // Socket-Update
        this.socket.on('stateUpdate', state => {
            this.latestState = state;

            // 🌿 Sichtbarkeits-Testausgabe
            console.log("📦 Spieler-Sichtbarkeit:");
            state.players.forEach(p => {
                console.log(`👤 ${p.playerId} – visible: ${p.visible}`);
            });
        });
        this.socket.emit('changeWeapon', {
            roomId: this.roomId,
            playerId: this.playerId,
            projectileType: this.selectedWeapon
        });

    }

    startFiring(pointer) {
        const meSprite = this.playerSprites[this.playerId];
        const meState  = this.latestState?.players.find(p => p.playerId === this.playerId);
        if (!meSprite || !meState || meState.ammo <= 0) return;

        const emitShot = () => {
            const dir = new Phaser.Math.Vector2(pointer.worldX - meSprite.x, pointer.worldY - meSprite.y).normalize();
            this.socket.emit('shootProjectile', {
                roomId: this.roomId,
                playerId: this.playerId,
                direction: { x: dir.x, y: dir.y },
                projectileType: this.selectedWeapon
            });
        };

        if (this.selectedWeapon === 'RIFLE_BULLET') {
            if (this.isFiring) return;
            this.isFiring = true;
            this.fireEvent = this.time.addEvent({ delay: 100, loop: true, callback: () => {
                    if (this.latestState.players.find(p => p.playerId === this.playerId).ammo <= 0) return this.stopFiring();
                    emitShot();
                }});
        } else {
            emitShot();
        }
    }

    stopFiring() {
        if (this.fireEvent) {
            this.fireEvent.remove();
            this.fireEvent = null;
        }
        this.isFiring = false;
    }

    getBrawlerSpriteName(brawlerId) {
        switch (brawlerId) {
            case 'tank': return 'brawler_tank';
            case 'mage': return 'brawler_mage';
            case 'healer': return 'brawler_healer';
            default: return 'brawler_sniper';
        }
    }

    update() {
        if (!this.latestState) return;

        if (this.hasWon || this.defeatShown) {
            return;
        }

        // Explosion für verschwundene Mines
        const currIds = new Set(this.latestState.projectiles
            .filter(p => p.projectileType === 'MINE')
            .map(p => p.id));
        this.prevProjectileIds.forEach(id => {
            if (!currIds.has(id)) {
                const pos = this.previousMinePositions[id];
                if (pos) {
                    const expl = this.explosionGroup.create(pos.x, pos.y, 'explosion1').setOrigin(0.5);
                    expl.play('explode');
                }
            }
        });
        this.previousMinePositions = {};
        this.latestState.projectiles.forEach(p => {
            if (p.projectileType === 'MINE') {
                this.previousMinePositions[p.id] = { x: p.position.x, y: p.position.y };
            }
        });
        this.prevProjectileIds = currIds;

        const me = this.latestState.players.find(p => p.playerId === this.playerId);

        // Bewegung senden
        if (me) {
            const dirX = (this.keys.left.isDown ? -1 : 0) + (this.keys.right.isDown ? 1 : 0);
            const dirY = (this.keys.up.isDown   ? -1 : 0) + (this.keys.down.isDown  ? 1 : 0);
            const world = this.cameras.main.getWorldPoint(this.input.activePointer.x, this.input.activePointer.y);
            const angle = Phaser.Math.Angle.Between(me.position.x, me.position.y, world.x, world.y);
            this.socket.emit('move', { roomId: this.roomId, playerId: this.playerId, dirX, dirY, angle });
        }

        // Spieler rendern & Health Bar
        const connected = this.latestState.players.filter(p => p.currentHealth > 0).length;
        const total = this.latestState.players.length;
        this.playerCountText.setText(`${connected}/${total} players`);

        const cam = this.cameras.main;
        this.latestState.players.forEach(p => {
            const isMe = p.playerId === this.playerId;
            let spr = this.playerSprites[p.playerId];
            if (!spr && p.currentHealth > 0 && (p.visible || isMe)) {
                const spriteKey = this.getBrawlerSpriteName(p.brawlerId || 'sniper');
                spr = this.physics.add.sprite(p.position.x, p.position.y, spriteKey).setOrigin(0.5);
                spr.healthBar = this.add.graphics();

                const isOwnPlayer = p.playerId === this.playerId;
                const nameColor = isOwnPlayer ? '#ffffff' : '#ff0000';

                spr.label = this.add.text(0, 0, p.playerName || 'Player', {
                    fontSize: '20px',
                    fontFamily: 'Arial',
                    color: nameColor,
                    stroke: '#000000',
                    strokeThickness: 4,
                    fontStyle: 'bold'
                }).setOrigin(0.5).setDepth(10);


                this.playerSprites[p.playerId] = spr;

                spr.outline = this.add.sprite(p.position.x, p.position.y, spriteKey)
                    .setOrigin(0.5)
                    .setTint(0x00ffff)     // zyanfarben
                    .setAlpha(0.4)         // halbtransparent
                    .setDepth(10)          // liegt über anderem Zeug
                    .setVisible(false);    // zunächst nicht sichtbar

                spr.healOutline = this.add.sprite(p.position.x, p.position.y, spriteKey)
                    .setOrigin(0.5)
                    .setTint(0x00ff00)
                    .setAlpha(0.4)
                    .setDepth(11)
                    .setVisible(false);

                spr.poisonOutline = this.add.sprite(p.position.x, p.position.y, spriteKey)
                    .setOrigin(0.5)
                    .setTint(0xff0000)
                    .setAlpha(0.4)
                    .setDepth(11)
                    .setVisible(false);



                if (isMe) {
                    spr.label = this.add.text(0, 0, this.playerName, {
                        fontSize: '20px',
                        fontFamily: 'Arial',
                        color: '#ffffff',
                        stroke: '#000000',
                        strokeThickness: 4,
                        fontStyle: "bold"
                    }).setOrigin(0.5).setDepth(10);
                    cam.startFollow(spr);
                    cam.setZoom(this.initialZoom);
                }

                this.physics.add.collider(spr, this.obstacleLayer);
            }
            if (spr) {
                if (p.currentHealth <= 0) {
                    spr.healthBar.destroy();
                    spr.outline.destroy();
                    spr.healOutline?.destroy();
                    spr.poisonOutline?.destroy();
                    spr.destroy();
                    delete this.playerSprites[p.playerId];
                } else {
                    spr.setPosition(p.position.x, p.position.y);
                    spr.setRotation(p.position.angle);
                    spr.setVisible(p.visible);

                    let gid = -1;
                    if (isMe && this.map) {
                        const tileX = Math.floor(p.position.x / this.tileSize);
                        const tileY = Math.floor(p.position.y / this.tileSize);
                        const tile = this.map.getTileAt(tileX, tileY, true, 'Gebüsch, Giftzone, Energiezone');
                        gid = tile?.index ?? -1;
                    }

                    const showOutline        = isMe && !p.visible;
                    const showHealOutline    = isMe && [19, 20].includes(gid);
                    const showPoisonOutline  = isMe && gid === 186;

                    spr.outline.setVisible(showOutline);
                    spr.healOutline.setVisible(showHealOutline);
                    spr.poisonOutline.setVisible(showPoisonOutline);

                    [spr.outline, spr.healOutline, spr.poisonOutline].forEach(o => {
                        o.setPosition(p.position.x, p.position.y);
                        o.setRotation(p.position.angle);
                    });


                    spr.healthBar.clear();

                    const shouldShowHealthBar = p.visible || isMe;

                    const barW = 40, barH = 6;
                    const pct = Phaser.Math.Clamp(p.currentHealth / this.maxHealth, 0, 1);
                    if (shouldShowHealthBar) {
                        spr.healthBar.clear()
                            .fillStyle(0x000000)
                            .fillRect(p.position.x - barW / 2 - 1, p.position.y - spr.height / 2 - barH - 9, barW + 2, barH + 2)
                            .fillStyle(0x00ff00)
                            .fillRect(p.position.x - barW / 2, p.position.y - spr.height / 2 - barH - 8, barW * pct, barH);
                    }
                }

                if (spr.label) {
                    spr.label.setPosition(p.position.x, p.position.y - 50);
                    spr.label.setVisible(p.visible);
                }
            }


        });

        // Projektile rendern
        const alive = new Set();
        this.latestState.projectiles.forEach(p => {
            const key = p.projectileType === 'SNIPER' ? 'sniper'
                : p.projectileType === 'SHOTGUN_PELLET' ? 'shotgun_pellet'
                    : p.projectileType === 'RIFLE_BULLET' ? 'rifle_bullet'
                        : 'mine';
            alive.add(p.id);
            let spr = this.projectileSprites[p.id];
            if (!spr) {
                spr = this.add.sprite(p.position.x, p.position.y, key).setOrigin(0.5);
                if (p.projectileType === 'MINE') spr.setScale(0.3);
                this.projectileSprites[p.id] = spr;
            } else {
                spr.setPosition(p.position.x, p.position.y);
            }
        });
        // Entfernen
        Object.keys(this.projectileSprites).forEach(id => {
            if (!alive.has(id)) {
                this.projectileSprites[id].destroy();
                delete this.projectileSprites[id];
            }
        });

        // Ammo-Bar
        if (me) {
            const weapon = me.currentWeapon;
            const ammo= me?.ammo ?? 0;
            const max    = weapon === 'RIFLE_BULLET' ? 15
                : weapon === 'SHOTGUN_PELLET' ? 3
                    : weapon === 'SNIPER' ? 1
                        : 1;
            const barX = 10, barY = 10, barW = 100, barH = 10;
            this.ammoBarBg.clear().fillStyle(0x000000, 0.5).fillRect(barX, barY, barW, barH);
            this.ammoBarFill.clear().fillStyle(0xffffff, 1)
                .fillRect(barX + 2, barY + 2, Math.floor((barW - 4) * (ammo / max)), barH - 4);
        }

        if (!this.hasWon && this.latestState.players.filter(p => p.currentHealth > 0).length === 1) {
            const me = this.latestState.players.find(p => p.playerId === this.playerId);
            if (me && me.currentHealth > 0) {
                this.showVictoryScreen();
                this.hasWon = true;
            }
        }

        // Wenn Spieler tot ist, aber Spiel nicht gewonnen => YOU DIED
        if (!this.hasWon) {
            const me = this.latestState.players.find(p => p.playerId === this.playerId);
            if (me && me.currentHealth <= 0 && !this.defeatShown) {
                this.showDefeatScreen();
                this.defeatShown = true;
            }
        }



    }

    createButton(x, y, text, onClick) {
        const btn = this.add.text(x, y, text, {
            fontSize: '32px',
            fontFamily: 'Arial',
            color: '#ffffff',
            backgroundColor: '#333333',
            padding: { x: 20, y: 10 },
            align: 'center'
        })
            .setOrigin(0.5)
            .setInteractive()
            .setScrollFactor(0);

        btn.on('pointerover', () => btn.setStyle({ backgroundColor: '#555555' }));
        btn.on('pointerout',  () => btn.setStyle({ backgroundColor: '#333333' }));
        btn.on('pointerdown', onClick);


        btn.on('pointerdown', () => {
            console.log(`Button '${text}' clicked`);
            onClick();
        });


        return btn;
    }

    showVictoryScreen() {
        const { width, height } = this.scale;

        this.victoryText = this.add.text(width / 2, height / 2 - 80, 'YOU WON!', {
            fontSize: '52px',
            fontFamily: 'Arial',
            color: '#00ff00',
            stroke: '#000000',
            strokeThickness: 6
        }).setOrigin(0.5).setScrollFactor(0);

        // this.playAgainButton = this.createButton(width / 2, height / 2, 'Play Again', () => {
        //     this.socket.emit('leaveRoom', { roomId: this.roomId, playerId: this.playerId });
        //     this.socket.disconnect();
        //     window.location.reload();
        //     this.scene.start('SelectionScene');
        // });

        this.exitButtonGame = this.createButton(width / 2, height / 2 + 70, 'Exit', () => {
            this.socket.emit('leaveRoom', { roomId: this.roomId, playerId: this.playerId });
            this.socket.disconnect();
            window.location.reload();
        });
    }

    showDefeatScreen() {
        const { width, height } = this.scale;

        this.victoryText = this.add.text(width / 2, height / 2 - 80, 'YOU DIED', {
            fontSize: '52px',
            fontFamily: 'Arial',
            color: '#ff0000',
            stroke: '#000000',
            strokeThickness: 6
        }).setOrigin(0.5).setScrollFactor(0);

        this.exitButtonGame = this.createButton(width / 2, height / 2 + 70, 'Exit', () => {
            this.socket.emit('leaveRoom', { roomId: this.roomId, playerId: this.playerId });
            this.socket.disconnect();
            window.location.reload();
        });
    }



}