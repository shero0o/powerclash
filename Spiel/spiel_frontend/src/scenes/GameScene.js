import Phaser from 'phaser';

export default class GameScene extends Phaser.Scene {
    gadgetType;
    cooldownExpireTime;
    gadgetText;

    constructor() {
        super({ key: 'GameScene' });

        this.isFiring          = false;
        this.fireEvent         = null;
        this.victoryText      = null;
        this.hasWon           = false;
        this.exitButtonGame   = null;
        this.defeatShown      = false;

        // zone overlay
        this.zoneState        = null;
        this.graphicsZone      = null;
        this.textZoneTimer     = null;
        this.boostActive       = false;
        this.boostEndTime      = 0;
        this.boostMultiplier   = 2;
        this.gadgetMaxUses     = 3;
    }

    init(data) {
        this.roomId            = data.roomId;
        this.playerId          = data.playerId;
        this.mapKey            = data.levelId || this.registry.get('levelId') || 'level1';
        this.selectedWeapon    = data.chosenWeapon || this.registry.get('weapon') || 'RIFLE_BULLET';
        this.selectedBrawler   = this.registry.get('brawler') || 'sniper';
        this.playerName        = this.registry.get('playerName') || 'Player';

        this.latestState       = null;
        this.playerSprites     = {};
        this.crateSprites      = {};
        this.projectileSprites = {};
        this.initialZoom       = 0.7;
        this.playerCountText   = null;
        this.npcSprites = {};
        this.npcBars    = {};
        this.npcLabels  = {};
        this.lastCoinCount = 0;
    }

    preload() {
        // Player & projectiles
        this.load.image('player',         '/assets/PNG/Hitman_1/hitman1_gun.png');
        this.load.image('rifle_bullet',   '/assets/PNG/projectile/rifle.png');
        this.load.image('sniper',         '/assets/PNG/projectile/sniper.png');
        this.load.image('shotgun_pellet', '/assets/PNG/projectile/shotgun.png');
        this.load.image('mine',           '/assets/PNG/explosion/bomb.png');

        // brawlers
        this.load.image('brawler_sniper', '/assets/PNG/Hitman_1/hitman1_gun.png');
        this.load.image('brawler_tank',   '/assets/PNG/Robot_1/robot1_machine.png');
        this.load.image('brawler_mage',   '/assets/PNG/Soldier_1/soldier1_silencer.png');
        this.load.image('brawler_healer', '/assets/PNG/Woman_Green/womanGreen_machine.png');
        this.load.image('npc', '/assets/PNG/Zombie/zoimbie1_hold.png');

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
            this.obstacleLayer = this.map.createLayer('Wand', tileset, 0, 0);        } else if (this.mapKey === 'level2'|| this.mapKey === 'level3'){
            map.createLayer('Boden', tileset, 0, 0);
            map.createLayer('Gebüsch, Giftzone, Energiezone', tileset, 0, 0);
            this.crateLayer = map.createLayer('Kisten', tileset, 0, 0);
            this.obstacleLayer = this.map.createLayer('Wand', tileset, 0, 0);        }


        // Physik-Welt & Kamera
        const width  = map.widthInPixels;
        const height = map.heightInPixels;
        this.physics.world.setBounds(0, 0, width, height);
        this.cameras.main.setBounds(0, 0, width, height);

        // zoom to fit
        const fitZoom = Math.min(
            this.cameras.main.width  / width,
            this.cameras.main.height / height
        );
        this.cameras.main.setZoom(fitZoom)
            .centerOn(width/2, height/2);
        this.input.topOnly = true;

        // ─── zone overlay setup ──────────────────────────────
        // AFTER: no scrollFactor → it's in world‐space
        this.graphicsZone = this.add.graphics();

        this.textZoneTimer   = this.add.text(15, 120, '', {
            fontFamily: 'Arial', fontSize: '24px',
            color: '#ffffff', stroke: '#000000',
            strokeThickness: 3
        }).setScrollFactor(0);

        // ammo UI
        this.ammoBarBg   = this.add.graphics().setScrollFactor(0);
        this.ammoBarFill = this.add.graphics().setScrollFactor(0);

        // UI: Gadget-Cooldown
        this.cooldownBarBg   = this.add.graphics().setScrollFactor(0);
        this.cooldownBarFill = this.add.graphics().setScrollFactor(0);


        // UI: Spielerzahl
        this.playerCountText = this.add.text(16, 16, '0/0 players', {
            fontFamily: 'Arial', fontSize: '24px',
            fontStyle: 'bold', color: '#ffffff',
            stroke: '#000000', strokeThickness: 3
        }).setScrollFactor(0);

        this.coinFloatingTexts = this.add.group();



        this.coinText = this.add.text(this.cameras.main.width - 160, 16, 'Coins: 0', {
            fontFamily: 'Arial',
            fontSize: '24px',
            fontStyle: 'bold',
            color: '#ffff00',
            stroke: '#000000',
            strokeThickness: 3
        })
            .setOrigin(0, 0)
            .setScrollFactor(0)
            .setDepth(1000);

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
        this.keys = this.input.keyboard.addKeys({
            up: Phaser.Input.Keyboard.KeyCodes.W,
            down: Phaser.Input.Keyboard.KeyCodes.S,
            left: Phaser.Input.Keyboard.KeyCodes.A,
            right: Phaser.Input.Keyboard.KeyCodes.D
        });

        console.log("KEY OBJECTS", this.keys);


        // Eingabe: Schuss
        this.input.on('pointerdown', pointer => this.startFiring(pointer));
        this.input.on('pointerup',   ()      => this.stopFiring());
        this.input.on('pointerout',  ()      => this.stopFiring());

// Eingabe: Q -> Gadget
        this.input.keyboard.on('keydown-Q', () => {
            const now = this.time.now;
            if (this.gadgetMaxUses <= 0) {
                console.log('Kein Gadget-Einsatz mehr möglich');
                return;
            }
            if (now < this.cooldownExpireTime) {
                console.log('Gadget im Cooldown');
                return;
            }
            console.log('keydown-Q ausgelöst → useGadget() wird aufgerufen');
            this.useGadget();
        });
        // Socket-Update
        this.socket.on('stateUpdate', state => {
            this.npcs = state.npcs || [];
            this.latestState = state;
            this.zoneState   = state.zoneState || null;
            const myGadget = state.gadgets.find(g => g.playerId === this.playerId);
            if (myGadget) {
                this.gadgetMaxUses      = myGadget.remainingUses;
                this.cooldownExpireTime = this.time.now + myGadget.timeRemaining;
            }

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

        // Gadget-Anzeige initialisieren
        this.gadgetType = this.registry.get('gadget');   // Typ aus JoinRequest
        this.cooldownExpireTime = 0; // Timestamp in ms, wann Cooldown endet
        this.gadgetText = this.add.text(16, 80, '', {
            fontFamily: 'Arial', fontSize: '20px', color: '#ffff00', stroke: '#000', strokeThickness: 3
        }).setScrollFactor(0);

        this.exitButtonGame = this.createButton(
            this.cameras.main.width / 2,
            this.cameras.main.height / 2 + 70,
            'Exit',
            () => {
                this.socket.emit('leaveRoom', { roomId: this.roomId, playerId: this.playerId });
                this.socket.disconnect();
                window.location.reload();
            }
        )
            .setScrollFactor(0)
            .setVisible(false);
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
            case 'tank':   return 'brawler_tank';
            case 'mage':   return 'brawler_mage';
            case 'healer': return 'brawler_healer';
            default:       return 'brawler_sniper';
        }
    }

    showFloatingCoinGain(amount, x, y) {
        const text = this.add.text(x, y - 40, `+${amount}`, {
            font: 'bold 24px Arial',
            fill: '#ffff00',
            stroke: '#000000',
            strokeThickness: 3
        }).setOrigin(0.5);

        this.coinFloatingTexts.add(text);

        this.tweens.add({
            targets: text,
            y: y - 80,
            alpha: 0,
            duration: 800,
            ease: 'power1',
            onComplete: () => text.destroy()
        });
    }

    useGadget() {
        this.socket.emit('useGadget', {
            roomId:   this.roomId,
            playerId: this.playerId
        }, (response) => {
            if (response === 'ok') {
                // Cooldown lokal starten (10 000 ms ab jetzt)
                this.cooldownExpireTime = this.time.now + 10_000;
                this.boostActive = true;
                this.boostEndTime = this.time.now + 2_000;
                console.log(`Gadget ${this.gadgetType} used: cooldown started`);
            } else {
                console.warn('Gadget use failed:', response);
            }
        });
    }

    update() {
        if (!this.latestState) return;

            this.graphicsZone.clear();

            if (this.zoneState) {
                const {center, radius, timeMsRemaining} = this.zoneState;

                // Draw a green circle outline at the true world coords
                this.graphicsZone
                    .lineStyle(4, 0x00aa00)          // 4px thick, green
                    .strokeCircle(center.x, center.y, radius);

                // Update the timer text (screen‐locked)
                const secs = Math.ceil(timeMsRemaining / 1000);
                const mm = String(Math.floor(secs / 60)).padStart(2, '0');
                const ss = String(secs % 60).padStart(2, '0');
                this.textZoneTimer.setText(`Zone closes in ${mm}:${ss}`);
            } else {
                this.textZoneTimer.setText('');
            }


            // if game won/lost, stop here
            if (this.hasWon || this.defeatShown) return;

            // ─── handle mines/explosions ────────────────────────
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
                    this.previousMinePositions[p.id] = {x: p.position.x, y: p.position.y};
                }
            });
            this.prevProjectileIds = currIds;

        // Bewegung senden
        const me = this.latestState.players.find(p => p.playerId === this.playerId);
        if (me) {
            // Boost-Ablauf prüfen
            if (this.boostActive && this.time.now > this.boostEndTime) {
                this.boostActive = false;
            }
            const baseX = (this.keys.left.isDown ? -1 : 0) + (this.keys.right.isDown ? 1 : 0);
            const baseY = (this.keys.up.isDown   ? -1 : 0) + (this.keys.down.isDown  ? 1 : 0);
            const speedFactor = this.boostActive ? this.boostMultiplier : 1;
            const dirX = baseX * speedFactor;
            const dirY = baseY * speedFactor;
            const world = this.cameras.main.getWorldPoint(this.input.activePointer.x, this.input.activePointer.y);
            const angle = Phaser.Math.Angle.Between(me.position.x, me.position.y, world.x, world.y);
            this.socket.emit('move', { roomId: this.roomId, playerId: this.playerId, dirX, dirY, angle });

            const remMs = Math.max(0, this.cooldownExpireTime - this.time.now);

            if (remMs > 0) {
                // Noch im Cooldown
                const cdSec = `${Math.ceil(remMs / 1000)}s`;
                this.gadgetText
                    .setText(`Cooldown: ${cdSec}`)
                    .setStyle({ fill: '#ff0000', fontSize: '20px', stroke: '#000', strokeThickness: 3 });
            } else {
                if (this.gadgetMaxUses <= 0){
                    // nicht Bereit
                    this.gadgetText
                        .setText(`No Gadget uses left: ${this.gadgetType}`)
                        .setStyle({ fill: '#ff0000', fontSize: '20px', stroke: '#000', strokeThickness: 3 });
                }
                else{
                    // Bereit
                    this.gadgetText
                        .setText(`Gadget Ready: ${this.gadgetType}`)
                        .setStyle({ fill: '#00ff00', fontSize: '20px', stroke: '#000', strokeThickness: 3 });
                }
            }



        const cdBarX = 10, cdBarY = 65, cdBarW = 100, cdBarH = 10;
        const now    = this.time.now;
        const remCd  = Math.max(0, this.cooldownExpireTime - now);
        const ratio  = remCd > 0 ? (remCd / 10_000) : 1;

        // Hintergrund
        this.cooldownBarBg
            .clear()
            .fillStyle(0x000000, 0.5)
            .fillRect(cdBarX, cdBarY, cdBarW, cdBarH);

        // Füllung
        this.cooldownBarFill
            .clear()
            .fillStyle(0xffffff, 1)
            .fillRect(
                cdBarX + 2,
                cdBarY + 2,
                Math.floor((cdBarW - 4) * ratio),
                cdBarH - 4
            );
        }

            // ── render NPCs ─────────────────────────────────────────
            const aliveNpcIds = new Set();

            this.npcs.forEach(npc => {
                aliveNpcIds.add(npc.id);

                // 1) Ensure we have three GameObjects for this NPC:
                let spr = this.npcSprites[npc.id];
                let bar = this.npcBars[npc.id];
                let label = this.npcLabels[npc.id];

                if (!spr) {
                    // a) main sprite
                    spr = this.physics.add.sprite(npc.position.x, npc.position.y, 'npc')
                        .setOrigin(0.5);
                    this.physics.add.collider(spr, this.obstacleLayer);

                    // b) health-bar graphic
                    bar = this.add.graphics().setDepth(11);

                    // c) name label
                    label = this.add.text(0, 0, 'NPC', {
                        fontSize: '16px', fontFamily: 'Arial',
                        color: '#ffffff', stroke: '#000000',
                        strokeThickness: 2
                    }).setOrigin(0.5).setDepth(12);

                    this.npcSprites[npc.id] = spr;
                    this.npcBars[npc.id] = bar;
                    this.npcLabels[npc.id] = label;
                }

                // 2) Update position & rotation
                spr.setPosition(npc.position.x, npc.position.y)
                    .setRotation(npc.position.angle);

                // 3) Redraw health bar above the sprite
                const maxHp = 50; // or pull from npc.maxHealth if you have it
                const pct = Phaser.Math.Clamp(npc.currentHealth / maxHp, 0, 1);
                const bw = 40, bh = 6;
                bar.clear()
                    .fillStyle(0x000000)
                    .fillRect(
                        npc.position.x - bw / 2 - 1,
                        npc.position.y - spr.displayHeight / 2 - bh - 8,
                        bw + 2, bh + 2
                    )
                    .fillStyle(0x00ff00)
                    .fillRect(
                        npc.position.x - bw / 2,
                        npc.position.y - spr.displayHeight / 2 - bh - 7,
                        bw * pct, bh
                    );

                // 4) Position the “NPC” label just above the bar
                label.setPosition(
                    npc.position.x,
                    npc.position.y - spr.displayHeight / 2 - bh - 16
                );

                // 5) Hide if dead
                const visible = npc.currentHealth > 0;
                spr.setVisible(visible);
                bar.setVisible(visible);
                label.setVisible(visible);
            });

// 6) Cleanup any despawned NPCs
            Object.keys(this.npcSprites).forEach(id => {
                if (!aliveNpcIds.has(id)) {
                    this.npcSprites[id].destroy();
                    this.npcBars[id].destroy();
                    this.npcLabels[id].destroy();
                    delete this.npcSprites[id];
                    delete this.npcBars[id];
                    delete this.npcLabels[id];
                }
            });


// Cleanup despawned NPCs
            Object.keys(this.npcSprites).forEach(id => {
                if (!aliveNpcIds.has(id)) {
                    this.npcSprites[id].destroy();
                    this.npcBars[id].destroy();
                    this.npcLabels[id].destroy();
                    delete this.npcSprites[id];
                    delete this.npcBars[id];
                    delete this.npcLabels[id];
                }
            });


            // cleanup
            Object.keys(this.npcSprites).forEach(id => {
                if (!aliveNpcIds.has(id)) {
                    this.npcSprites[id].destroy();
                    this.npcBars[id].destroy();
                    this.npcLabels[id].destroy();
                    delete this.npcSprites[id];
                    delete this.npcBars[id];
                    delete this.npcLabels[id];
                }
            });


            // ─── render players & health ───────────────────────
            const connected = this.latestState.players
                .filter(p => p.currentHealth > 0).length;
            const total = this.latestState.players.length;
            this.playerCountText.setText(`${connected}/${total} players`);

            this.latestState.players.forEach(p => {
                const isMe = p.playerId === this.playerId;
                let spr = this.playerSprites[p.playerId];

                // 🟢 Spawn: nur wenn noch kein Sprite vorhanden
                if (!spr && p.currentHealth > 0 && (p.visible || isMe)) {
                    const key = this.getBrawlerSpriteName(p.brawlerId || 'sniper');
                    spr = this.physics.add.sprite(p.position.x, p.position.y, key)
                        .setOrigin(0.5);

                    // 🟡 OUTLINE-SPRITES ERZEUGEN
                    spr.outline = this.add.sprite(p.position.x, p.position.y, key)
                        .setOrigin(0.5).setTint(0x00ffff).setAlpha(0.4)
                        .setDepth(10).setVisible(false);

                    spr.healOutline = this.add.sprite(p.position.x, p.position.y, key)
                        .setOrigin(0.5).setTint(0x00ff00).setAlpha(0.4)
                        .setDepth(11).setVisible(false);

                    spr.poisonOutline = this.add.sprite(p.position.x, p.position.y, key)
                        .setOrigin(0.5).setTint(0xff0000).setAlpha(0.4)
                        .setDepth(11).setVisible(false);


                    spr.healthBar = this.add.graphics();
                    spr.label = this.add.text(0, 0, p.playerName || 'Player', {
                        fontSize: '20px', fontFamily: 'Arial',
                        color: isMe ? '#ffffff' : '#ff0000',
                        stroke: '#000000', strokeThickness: 4,
                        fontStyle: 'bold'
                    }).setOrigin(0.5).setDepth(10);

                    this.playerSprites[p.playerId] = spr;
                }
                // Kollision gegen Wände
                this.physics.add.collider(spr, this.obstacleLayer);

            if (spr) {
                if (p.currentHealth <= 0) {
                    spr.healthBar.destroy();
                    spr.label?.destroy();
                    spr.outline?.destroy();
                    spr.healOutline?.destroy();
                    spr.poisonOutline?.destroy();
                    spr.destroy();
                    delete this.playerSprites[p.playerId];
                } else {
                    spr.setPosition(p.position.x, p.position.y);
                    spr.setRotation(p.position.angle);
                    spr.setVisible(p.visible);


                    // Health Bar zeichnen
                    const barW       = 40;
                    const barH       = 6;
                    const pct        = Phaser.Math.Clamp(p.currentHealth / p.maxHealth, 0, 1);

// Segment-Größe in HP und Abstand in px lassen sich hier anpassen:
                    const segmentHP  = 20;     // jedes Segment entspricht 20 HP
                    const gap        = 1;      // Abstand zwischen Segmenten in px

// Anzahl der Segmente ergibt sich aus der Max-HP des Brawlers:
                    const segments   = Math.ceil(p.maxHealth / segmentHP);
                    const segW       = barW / segments;
                    const fillPixels = barW * pct;

                    spr.healthBar.clear();

// 1) Äußerer Rahmen (2px schwarz)
                    const bgX = p.position.x - barW/2 - 1;
                    const bgY = p.position.y - spr.height/2 - barH - 9;
                    spr.healthBar
                        .lineStyle(2, 0x000000)
                        .strokeRect(bgX, bgY, barW + 2, barH + 2);

// 2) Schwarzer Hintergrund innen
                    spr.healthBar
                        .fillStyle(0x000000)
                        .fillRect(bgX + 1, bgY + 1, barW, barH);

// 3) Grüne Segmente entsprechend gefüllter HP
                    spr.healthBar.fillStyle(0x00ff00);
                    for (let i = 0; i < segments; i++) {
                        const x      = bgX + 1 + i * segW;
                        const remain = fillPixels - i * segW;
                        if (remain <= 0) break;
                        const w      = Math.min(segW - gap, remain);
                        spr.healthBar.fillRect(x, bgY + 1, w, barH);
                    }
                    spr.healthBar.setVisible(p.visible);
                    if (spr.label) {
                        spr.label.setPosition(p.position.x, p.position.y - 50);
                        spr.label.setVisible(p.visible);
                    }

                    let gid = -1;
                    if (isMe && this.map) {
                        const tileX = Math.floor(p.position.x / this.tileSize);
                        const tileY = Math.floor(p.position.y / this.tileSize);
                        const tile = this.map.getTileAt(tileX, tileY, true, 'Gebüsch, Giftzone, Energiezone');
                        gid = tile?.index ?? -1;
                    }

                    const showOutline = isMe && !p.visible;
                    const showHealOutline = isMe && [19, 20].includes(gid);
                    const showPoisonOutline = isMe && gid === 186;

                    spr.outline?.setVisible(showOutline);
                    spr.healOutline?.setVisible(showHealOutline);
                    spr.poisonOutline?.setVisible(showPoisonOutline);

                    [spr.outline, spr.healOutline, spr.poisonOutline].forEach(o => {
                        o?.setPosition(p.position.x, p.position.y);
                        o?.setRotation(p.position.angle);
                    });
                }
            }
        });
        // Exit anzeigen, wenn tot
        if (me && me.currentHealth <= 0) this.exitButtonGame.setVisible(true);

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
            const ammo = me?.ammo ?? 0;
            const max = weapon === 'RIFLE_BULLET' ? 15
                : weapon === 'SHOTGUN_PELLET' ? 3
                    : weapon === 'SNIPER' ? 1
                        : 1;
            const barX = 10, barY = 10, barW = 100, barH = 10;
            this.ammoBarBg.clear().fillStyle(0x000000, 0.5).fillRect(barX, barY, barW, barH);
            this.ammoBarFill.clear().fillStyle(0xffffff, 1)
                .fillRect(barX + 2, barY + 2, Math.floor((barW - 4) * (ammo / max)), barH - 4);
        }

        if (me && this.coinText) {
            const newCount = me.coinCount ?? 0;
            if (newCount > this.lastCoinCount) {
                const gain = newCount - this.lastCoinCount;
                this.showFloatingCoinGain(gain, me.position.x, me.position.y);

            }
            this.lastCoinCount = newCount;
        }


        // ========== KISTEN VERARBEITEN ==========
        if (this.latestState.crates) {
            const currentCrateIds = new Set();

            this.latestState.crates.forEach(crate => {
                currentCrateIds.add(crate.crateId);
                let spr = this.crateSprites[crate.crateId];

                if (!spr) {
                    spr = this.add.sprite(crate.x * this.tileSize + 32, crate.y * this.tileSize + 32, 'crateTexture').setOrigin(0.5);
                    spr.healthBar = this.add.graphics();
                    this.crateSprites[crate.crateId] = spr;
                }

                spr.setPosition(crate.x * this.tileSize + 32, crate.y * this.tileSize + 32);

                // Healthbar anzeigen
                spr.healthBar.clear();
                if (crate.crateHp < 100) {
                    const pct = Phaser.Math.Clamp(crate.crateHp / 100, 0, 1);
                    const barW = 40, barH = 6;
                    spr.healthBar
                        .fillStyle(0x000000)
                        .fillRect(spr.x - barW / 2 - 1, spr.y - 40, barW + 2, barH + 2)
                        .fillStyle(0xff0000)
                        .fillRect(spr.x - barW / 2, spr.y - 39, barW * pct, barH);
                }
            });

            // Entferne verschwundene Crates
            Object.keys(this.crateSprites).forEach(crateId => {
                if (!currentCrateIds.has(crateId)) {
                    const spr = this.crateSprites[crateId];
                    spr.healthBar?.destroy();
                    spr.destroy();
                    delete this.crateSprites[crateId];

                    // 🟩 Ersetze das Tile an Position mit GID 401 (Gras)
                    const tileX = Math.floor(spr.x / this.tileSize);
                    const tileY = Math.floor(spr.y / this.tileSize);
                    this.crateLayer.putTileAt(401, tileX, tileY);

                }
            });
        }

        if (this.latestState && this.latestState.players) {
            const me = this.latestState.players.find(p => p.playerId === this.playerId);
            if (me && this.coinText) {
                this.coinText.setText(`Coins: ${me.coinCount}`);
            }
        }


        const alivePlayers = this.latestState.players.filter(p => p.currentHealth > 0).length;

        // 1) Wenn nur noch ein Spieler übrig ist (Sieg-Bedingung)
        if (!this.hasWon && alivePlayers === 1) {
            const meAlive = this.latestState.players.find(p => p.playerId === this.playerId);
            if (meAlive && meAlive.currentHealth > 0) {
                const place = 1; // Sieger ist automatisch Platz 1
                const baseCoins = meAlive.coinCount ?? 0;
                // Bonus-Regeln: Platz 1 +10, Platz 2 +5, Platz 3 +0, Platz 4 -10
                const bonus = 10; // Platz 1 immer +10
                const totalCoins = baseCoins + bonus;
                this.showVictoryScreen(place, baseCoins, bonus, totalCoins);
                this.hasWon = true;
            }
            const newCount = me.coinCount ?? 0;
            this.lastCoinCount = newCount;
            this.coinText.setText(`${newCount}`);
        }

        // 2) Wenn man selbst gerade gestorben ist
        if (!this.hasWon && me && me.currentHealth <= 0 && !this.defeatShown) {
            // Anzahl der Lebenden (nach dem Tod) = alivePlayers (denn me.currentHealth <= 0 zählt nicht mit)
            // Platz des Verstorbenen = alivePlayers + 1
            const place = alivePlayers + 1;

            const baseCoins = me.coinCount ?? 0;

            // Bonus-/Malus-Berechnung nach Platz
            let bonus = 0;
            switch (place) {
                case 1:
                    bonus = 10;
                    break;
                case 2:
                    bonus = 5;
                    break;
                case 3:
                    bonus = 0;
                    break;
                case 4:
                    bonus = -10;
                    break;
                default:
                    bonus = 0; // Für Platz > 4 kein zusätzlicher Effekt
            }
            const totalCoins = baseCoins + bonus;

            this.showDefeatScreen(place, baseCoins, bonus, totalCoins);
            this.defeatShown = true;
        }

        console.log("KEYS:", {
            W: this.keys.up.isDown,
            A: this.keys.left.isDown,
            S: this.keys.down.isDown,
            D: this.keys.right.isDown
        });

        if (me && this.playerSprites[this.playerId]) {
            const meSprite = this.playerSprites[this.playerId];

            if (this.cameras.main._follow !== meSprite) {
                this.cameras.main.startFollow(meSprite);
                this.cameras.main.setZoom(this.initialZoom);
            }
        }




    }

    createButton(x, y, text, onClick) {
        const btn = this.add.text(x, y, text, {
            fontSize: '32px', fontFamily: 'Arial',
            color: '#ffffff', backgroundColor: '#333333',
            padding: {x: 20, y: 10}, align: 'center'
        })
            .setOrigin(0.5)
            .setInteractive()
            .setScrollFactor(0)
            .on('pointerover', () => btn.setStyle({backgroundColor: '#555555'}))
            .on('pointerout', () => btn.setStyle({backgroundColor: '#333333'}))
            .on('pointerdown', onClick);

        return btn;
    }

    showVictoryScreen(place, baseCoins, bonus, totalCoins) {
        const {width, height} = this.scale;

        this.add.rectangle(width / 2, height / 2, width, height, 0x000000)
            .setOrigin(0.5)
            .setScrollFactor(0);


        this.victoryText = this.add.text(
            width / 2, height / 2 - 80, `You placed ${place}!`, {
                fontSize: '52px', fontFamily: 'Arial',
                color: '#00ff00', stroke: '#000000',
                strokeThickness: 6
            }
        ).setOrigin(0.5).setScrollFactor(0);


        this.add.text(
            width / 2,
            height / 2 - 30,
            `In game won coins: ${baseCoins}`,
            {
                fontSize: '32px',
                fontFamily: 'Arial',
                color: '#ffffff',
                stroke: '#000000',
                strokeThickness: 4
            }
        )
            .setOrigin(0.5)
            .setScrollFactor(0);

        // 3) Bonus/Malus
        const bonusText = (bonus >= 0) ? `Bonus: +${bonus}` : `Malus: ${bonus}`;
        this.add.text(
            width / 2,
            height / 2 + 20,
            bonusText,
            {
                fontSize: '32px',
                fontFamily: 'Arial',
                color: bonus >= 0 ? '#00ff00' : '#ff0000',
                stroke: '#000000',
                strokeThickness: 4
            }
        )
            .setOrigin(0.5)
            .setScrollFactor(0);

        // 4) Gesamt-Coins
        this.add.text(
            width / 2,
            height / 2 + 70,
            `Gesamt-Coins: ${totalCoins}`,
            {
                fontSize: '32px',
                fontFamily: 'Arial',
                color: '#ffd700', // Gold-Farbton
                stroke: '#000000',
                strokeThickness: 4
            }
        )
            .setOrigin(0.5)
            .setScrollFactor(0);

        this.exitButtonGame = this.createButton(
            width / 2, height / 2 + 150, 'Exit', () => {
                this.socket.emit('leaveRoom', {
                    roomId: this.roomId, playerId: this.playerId
                });
                this.socket.disconnect();
                window.location.reload();
            }
        );
    }

    showDefeatScreen(place, baseCoins, bonus, totalCoins) {
        const {width, height} = this.scale;

        this.add.rectangle(width / 2, height / 2, width, height, 0x000000)
            .setOrigin(0.5)
            .setScrollFactor(0);

        this.victoryText = this.add.text(
            width / 2, height / 2 - 80,  `You placed: ${place}!`, {
                fontSize: '52px', fontFamily: 'Arial',
                color: '#ff0000', stroke: '#000000',
                strokeThickness: 6
            }
        ).setOrigin(0.5).setScrollFactor(0);


        // 2) Coins im Spiel
        this.add.text(
            width / 2,
            height / 2 - 30,
            `In game won coins: ${baseCoins}`,
            {
                fontSize: '32px',
                fontFamily: 'Arial',
                color: '#ffffff',
                stroke: '#000000',
                strokeThickness: 4
            }
        )
            .setOrigin(0.5)
            .setScrollFactor(0);

        // 3) Bonus/Malus
        const bonusText = (bonus >= 0) ? `Bonus: +${bonus}` : `Malus: ${bonus}`;
        this.add.text(
            width / 2,
            height / 2 + 20,
            bonusText,
            {
                fontSize: '32px',
                fontFamily: 'Arial',
                color: bonus >= 0 ? '#00ff00' : '#ff0000',
                stroke: '#000000',
                strokeThickness: 4
            }
        )
            .setOrigin(0.5)
            .setScrollFactor(0);

        // 4) Gesamt-Coins
        this.add.text(
            width / 2,
            height / 2 + 70,
            `Gesamt-Coins: ${totalCoins}`,
            {
                fontSize: '32px',
                fontFamily: 'Arial',
                color: '#ffd700',
                stroke: '#000000',
                strokeThickness: 4
            }
        )
            .setOrigin(0.5)
            .setScrollFactor(0);


        this.exitButtonGame = this.createButton(
            width / 2, height / 2 + 150, 'Exit', () => {
                this.socket.emit('leaveRoom', {
                    roomId: this.roomId, playerId: this.playerId
                });
                this.socket.disconnect();
                window.location.reload();
            }
        );
    }
}