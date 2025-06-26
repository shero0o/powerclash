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
        this.zone        = null;
        this.graphicsZone      = null;
        this.textZoneTimer     = null;
        this.boostActive       = false;
        this.boostEndTime      = 0;
        this.boostMultiplier   = 2;
        this.gadgetMaxUses     = 3;
        this.matchOverEmitted = false;

    }

    init(data) {
        this.roomId            = data.roomId;
        this.playerId          = data.playerId;
        this.mapKey = this.registry.get('levelId');

        this.selectedWeapon = data.chosenWeapon || (() => {
            switch (this.registry.get('weapon')) {
                case 2: return 'SNIPER';
                case 3: return 'SHOTGUN_PELLET';
                case 4: return 'MINE';
                default: return 'RIFLE_BULLET';
            }
        })() || 'RIFLE_BULLET';
        this.gadgetType = (() => {
            switch (this.registry.get('gadget')) {
                case 1: return 'DAMAGE_BOOST';
                case 2: return 'SPEED_BOOST';
                default: return 'HEALTH_BOOST';
            }
        })() || 'HEALTH_BOOST';
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
        this.load.image('hitman1', '/assets/PNG/Hitman_1/hitman1_gun.png');
        this.load.image('robot1',   '/assets/PNG/Robot_1/robot1_machine.png');
        this.load.image('soldier1',   '/assets/PNG/Soldier_1/soldier1_silencer.png');
        this.load.image('womanGreen', '/assets/PNG/Woman_Green/womanGreen_machine.png');
        this.load.image('npc', '/assets/PNG/Survivor1/survivor1_hold.png');

        this.load.svg("exitButtonSvg", "assets/svg/btn-exit.svg", { width: 190, height: 90 })

        this.load.svg("controller", "assets/svg/controller.svg", { width: 350, height: 150 })
        this.load.svg("brawler-stats", "assets/svg/brawler-stats.svg", { width: 250, height: 125 })

        this.load.svg("victory", "assets/svg/victory.svg", { width: 600, height: 300 })
        this.load.svg("defeat", "assets/svg/defeat.svg", { width: 600, height: 300 })

        this.load.svg('icon_coin', '/assets/svg/coin-icon.svg', { width: 250, height: 200 });

        this.load.svg("healthGadget", "assets/svg/healthGadget.svg", { width: 400, height: 200 });
        this.load.svg("damageGadget", "assets/svg/damageGadget.svg", { width: 400, height: 200 });
        this.load.svg("speedGadget", "assets/svg/speedGadget.svg", { width: 400, height: 200 });

        for (let i = 1; i <= 25; i++) {
            const frame = i.toString().padStart(4, '0');  // "0001", "0002", …, "0010", …, "0025"
            this.load.image(frame, `/assets/PNG/explosion/${frame}.png`);
        }

        // Map tileset & tilemap
        this.load.image('tileset', '/assets/Tilesheet/spritesheet_tiles.png');
        let mapFile;
        console.log("MAP KEY", this.mapKey);
        if (this.mapKey === 'level1') {
            mapFile = 'map1.tmj';
        } else {
            mapFile = 'map2.tmj'; // shared by level2 and level3
        }
        console.log("MAP FILE", mapFile);

        this.load.tilemapTiledJSON('map', `/assets/${mapFile}`);
    }

    create() {
        const vw = this.scale.width;   // Breite des Viewports in Pixeln
        const vh = this.scale.height;  // Höhe des Viewports in Pixeln
        // fest am Bildschirm haften


        // ─── 1) matchOver-Listener einmalig registrieren ───
        this.socket.on('matchOver', () => {
            const me = this.latestState.players.find(p => p.playerId === this.playerId);
            if (!me || me.currentHealth <= 0) {
                const alive = this.latestState.players.filter(p => p.currentHealth > 0).length;
                const place = alive + 1;
                const base  = me?.coinCount ?? 0;
                const bonus = place === 1 ? 10 : place === 2 ? 5 : place === 3 ? 0 : -10;
                this.showDefeatScreen(place, base, bonus, base + bonus);
                this.defeatShown = true;
            }
        });

        this.exitButtonSvg = this.add.image(
            this.cameras.main.width / 2,
            this.cameras.main.height / 2 + 70,
            'exitButtonSvg'
        )
            .setOrigin(0.5)
            .setScrollFactor(0)
            .setInteractive({ useHandCursor: true })
            .setVisible(false)   // zunächst wegstecken
            .on('pointerdown', () => {
                this.socket.emit('leaveRoom', { playerId: this.playerId });
                this.socket.disconnect();
                this.scene.start('LobbyScene');
            });

        // Hintergrundfarbe
        this.cameras.main.setBackgroundColor('#222222');

        this.tileSize = 64;

        // Karte & Layer
        const map = this.make.tilemap({ key: 'map' });
        this.map = this.make.tilemap({ key: 'map' });
        const tileset = map.addTilesetImage('spritesheet_tiles','tileset',64,64);
        // 1) Boden immer zuerst
            map.createLayer('Boden', tileset, 0, 0);

            // 2) Zusätzliche Overlays nur für Level 2 & 3
            if (this.mapKey === 'level2' || this.mapKey === 'level3') {
                map.createLayer('Gebüsch, Giftzone, Energiezone', tileset, 0, 0);
                this.crateLayer = map.createLayer('Kisten', tileset, 0, 0);
            }

            // 3) Mauern ganz zum Schluss
                this.obstacleLayer = map.createLayer('Wand', tileset, 0, 0);

        const controllerIcon = this.add.image(
            vw / 130,    // horizontal zentriert
            vh + 10,  // 50px oberhalb der unteren Bildschirmkante
            'controller'
        )
            .setOrigin(0.5)
            .setScale(0.8)
            .setScrollFactor(0).setAlpha(0.5)       // 0.0 = komplett durchsichtig, 1.0 = voll sichtbar
            .setDepth(1000);

        const offset = 80;
        this.add.text(
            controllerIcon.x,
            controllerIcon.y - offset,
            'W',
            {
                fontSize: '32px',
                fontFamily: 'Arial',
                color: '#ffffff',
                stroke: '#000000',
                strokeThickness: 4
            }
        )
            .setOrigin(0.5)
            .setScrollFactor(0)
            .setDepth(1000);

        // "S" unterhalb
        this.add.text(
            controllerIcon.x,
            controllerIcon.y + offset,
            'S',
            {
                fontSize: '32px',
                fontFamily: 'Arial',
                color: '#ffffff',
                stroke: '#000000',
                strokeThickness: 4
            }
        )
            .setOrigin(0.5)
            .setScrollFactor(0)
            .setDepth(1000);

        // "A" links
        this.add.text(
            controllerIcon.x - offset,
            controllerIcon.y,
            'A',
            {
                fontSize: '32px',
                fontFamily: 'Arial',
                color: '#ffffff',
                stroke: '#000000',
                strokeThickness: 4
            }
        )
            .setOrigin(0.5)
            .setScrollFactor(0)
            .setDepth(1000);

        // "D" rechts
        this.add.text(
            controllerIcon.x + offset,
            controllerIcon.y,
            'D',
            {
                fontSize: '32px',
                fontFamily: 'Arial',
                color: '#ffffff',
                stroke: '#000000',
                strokeThickness: 4
            }
        )
            .setOrigin(0.5)
            .setScrollFactor(0)
            .setDepth(1000);

        this.healthIcon = this.add.image(
            vw / 2,
            vh + 10,
            'brawler-stats'
        )
            .setOrigin(0.5)
            .setScale(0.8)
            .setScrollFactor(0)
            .setDepth(1000);

        this.healthValueText = this.add.text(
            vw / 2 + 44,
            this.healthIcon.y + 25,
            '0', // Startwert; wird in update() überschrieben
            {
                fontSize: '28px',
                fontFamily: 'Arial Black',
                color: '#ffffff',       // weiße Schrift
                stroke: '#000000',      // schwarzer Rand
                strokeThickness: 4,
                align: 'center'
            }
        )
            .setOrigin(0.5)
            .setScrollFactor(0)
            .setDepth(1000);




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



        const coinX = vw - 200;
        const coinY = 16;

        // 1) Erstelle das Coin‐Icon
        this.coinIcon = this.add.image(coinX, coinY, 'icon_coin')
            .setOrigin(0, 0)      // links‐oben am Icon
            .setScrollFactor(0)   // bleibt fest am HUD
            .setDisplaySize(60, 60) // skaliere auf 32×32px (oder so, wie es optisch passt)
            .setDepth(1000);


        this.coinText = this.add.text(
            coinX + 90,  // 36px nach rechts, damit Text hinter dem 32px‐Icon beginnt + 4px Abstand
            coinY + 25,   // 8px nach unten, sodass Text mittig zum Icon sitzt
            '0',         // Startwert; wird in update() überschrieben
            {
                fontSize: '28px',
                fontFamily: 'Arial Black',
                color: '#ffff00',       // Gelb
                stroke: '#000000',      // Schwarzer Rand
                strokeThickness: 3,
                align: 'left'
            }
        )
            .setOrigin(0.5)   // linke Mitte des Texts
            .setScrollFactor(0)
            .setDepth(1000);

        // Explosion-Animation
        this.anims.create({
            key: 'explode',
            frames: Array.from({ length: 25 }, (_, idx) => {
                // idx läuft von 0–24, also +1 und auf 4 Stellen auffüllen
                const frame = (idx + 1).toString().padStart(4, '0');
                return { key: frame };
            }),
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
        const gadgetKey = this.registry.get('gadgetKey') || localStorage.getItem('gadgetKey') || 'Q';
        this.gadgetKey = gadgetKey;

        this.input.keyboard.on(`keydown-${gadgetKey}`, () => {
            const now = this.time.now;
            if (this.gadgetMaxUses <= 0 || now < this.cooldownExpireTime) return;
            this.useGadget();
        });

        // Socket-Update
        this.socket.on('stateUpdate', state => {
            this.npcs = state.npcs || [];
            this.latestState = state;
            this.zone   = state.zone || null;
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


        //Gadget:
        //    In Lobby hast du offenbar mit Werten wie 'DAMAGE_BOOST' o. Ä. gearbeitet.
        //    Wir definieren nun eine Hilfsfunktion, die daraus den passenden Texture-Key macht:

        const getGadgetKey = (gadgetTypeString) => {
            switch (gadgetTypeString) {
                case 'DAMAGE_BOOST': return 'damageGadget';
                case 'SPEED_BOOST':  return 'speedGadget';
                case 'HEALTH_BOOST': return 'healthGadget';
                default:             return 'healthGadget';
            }
        };

        // 2) Wähle den korrekten Texture-Key
        const currentGadgetKey = getGadgetKey(this.gadgetType);

        // 3) Gadget-Icon an gewünschter HUD-Position platzieren (z. B. links oben neben den Coins)
        //    Wir legen es z. B. knapp unterhalb des Coin-Icons ab (y=90). Passe bei Bedarf an!
        this.gadgetIcon = this.add.image(
            vw / 2 + 800,   // 100px links neben Münz-HUD (oder wo du es haben willst)
            vh + 10,    // zentriert zur HUD-Leiste
            currentGadgetKey
        )
            .setOrigin(0.5)
            .setScrollFactor(0)
            .setDisplaySize(160, 80)  // skaliere so, dass es zum HUD passt
            .setDepth(1000);

        // ─── (optional) Wenn du dennoch einen Gadget-Text (z.B. für Cooldown) brauchst,
        //       kannst du ihn hier anlegen und später in update() aktualisieren:
        this.gadgetText = this.add.text(
            this.gadgetIcon.x + 0,   // z. B. direkt unter dem Icon
            this.gadgetIcon.y + 55,
            '',
            {
                fontFamily: 'Arial',
                fontSize: '18px',
                color: '#ffffff',
                stroke: '#000000',
                strokeThickness: 3,
                align: 'center'
            }
        )
            .setOrigin(0.5)
            .setScrollFactor(0)
            .setDepth(1000);

        // Gadget-Anzeige initialisieren
        // this.gadgetType = this.registry.get('gadget');   // Typ aus JoinRequest
        // this.cooldownExpireTime = 0; // Timestamp in ms, wann Cooldown endet
        // this.gadgetText = this.add.text(16, 80, '', {
        //     fontFamily: 'Arial', fontSize: '20px', color: '#ffff00', stroke: '#000', strokeThickness: 3
        // }).setScrollFactor(0);

    }

    startFiring(pointer) {
        const meSprite = this.playerSprites[this.playerId];
        const meState  = this.latestState?.players.find(p => p.playerId === this.playerId);
        if (!meSprite || !meState || meState.ammo <= 0) return;

        const emitShot = () => {
            const worldPoint = this.cameras.main.getWorldPoint(pointer.x, pointer.y);
            const dir = new Phaser.Math.Vector2(worldPoint.x - meSprite.x, worldPoint.y - meSprite.y).normalize();
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
            case 'robot':   return 'robot1';
            case 'soldier':   return 'soldier1';
            case 'woman': return 'womanGreen';
            default:       return 'hitman1';
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

        if (this.zone) {
            const {center, radius, timeMsRemaining} = this.zone;

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
        if (me && this.healthValueText) {
            this.healthValueText.setText(`${me.currentHealth} / ${me.maxHealth}`);

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
                        .setText(`0 Uses: ${this.gadgetType}`)
                        .setStyle({ fill: '#ff0000', fontSize: '20px', stroke: '#000', strokeThickness: 3 });
                }
                else{
                    // Bereit
                    this.gadgetText
                        .setText(this.gadgetKey)
                        .setStyle({ fill: '#00ff00', fontSize: '26px', stroke: '#000', strokeThickness: 3 });
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

            if (!spr && npc.currentHealth > 0 && (npc.visible || me)) {
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
            if (spr) {
                spr.setPosition(npc.position.x, npc.position.y)
                    .setRotation(npc.position.angle);
            }

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
            if (label) {
                label.setPosition(
                    npc.position.x,
                    npc.position.y - spr.displayHeight/2 - bh - 16
                );
            }

            // 5) Hide if dead
            const visible = npc.currentHealth > 0;
            if (spr)   spr.setVisible(visible);
            if (bar)   bar.setVisible(visible);
            if (label) label.setVisible(visible);
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
            if (spr) {
                this.physics.add.collider(spr, this.obstacleLayer);
            }

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
        if (me && me.currentHealth <= 0) this.exitButtonSvg.setVisible(true);

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
                this.coinText.setText(`${me.coinCount}`);
            }
        }

        const alivePlayers = this.latestState.players.filter(p => p.currentHealth > 0).length;


        // 2) Wenn man selbst gerade gestorben ist
        // if (!this.hasWon && me && me.currentHealth <= 0 && !this.defeatShown) {
        //     // Anzahl der Lebenden (nach dem Tod) = alivePlayers (denn me.currentHealth <= 0 zählt nicht mit)
        //     // Platz des Verstorbenen = alivePlayers + 1
        //     const place = alivePlayers + 1;
        //
        //     const baseCoins = me.coinCount ?? 0;
        //
        //     // Bonus-/Malus-Berechnung nach Platz
        //     let bonus = 0;
        //     switch (place) {
        //         case 1:
        //             bonus = 10;
        //             break;
        //         case 2:
        //             bonus = 5;
        //             break;
        //         case 3:
        //             bonus = 0;
        //             break;
        //         case 4:
        //             bonus = -10;
        //             break;
        //         default:
        //             bonus = 0; // Für Platz > 4 kein zusätzlicher Effekt
        //     }
        //     const totalCoins = baseCoins + bonus;
        //
        //     this.showDefeatScreen(place, baseCoins, bonus, totalCoins);
        //     this.defeatShown = true;
        // }

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

        // … dein bestehender Code bis zum Schluss …

        // 1) matchOver EINMALIG senden
        if (!this.matchOverEmitted && alivePlayers === 1) {
            this.socket.emit('matchOver', { roomId: this.roomId });
            this.matchOverEmitted = true;
        }

        // 2) Nur Gewinner zeigt Victory
        if (!this.hasWon && alivePlayers === 1 && me?.currentHealth > 0) {
            const base = me.coinCount ?? 0, bonus = 10;
            this.showVictoryScreen(1, base, bonus, base + bonus);
            this.hasWon = true;
        }

        // 3) Nur der Gestorbene zeigt Defeat
        if (!this.defeatShown && me && me.currentHealth <= 0) {
            const place = alivePlayers + 1, base = me.coinCount ?? 0;
            const bonus = place === 2 ? 5 : place === 3 ? 0 : place === 4 ? -10 : 0;
            this.showDefeatScreen(place, base, bonus, base + bonus);
            this.defeatShown = true;
        }

        // 4) Bewegungs-Emit IMMER senden (auch wenn jemand gestorben ist)
        const dirX = (this.keys.right.isDown ? 1 : 0) - (this.keys.left.isDown ? 1 : 0);
        const dirY = (this.keys.down.isDown  ? 1 : 0) - (this.keys.up.isDown   ? 1 : 0);
        const world = this.cameras.main.getWorldPoint(this.input.activePointer.x, this.input.activePointer.y);
        const angle = Phaser.Math.Angle.Between(me.position.x, me.position.y, world.x, world.y);
        this.socket.emit('move', { roomId: this.roomId, playerId: this.playerId, dirX, dirY, angle });

        // Ende von update()




    }


    showVictoryScreen(place, baseCoins, bonus, totalCoins) {
        const {width, height} = this.scale;

        this.add.rectangle(width / 2, height / 2, width - 830, height - 240, 0x000000)
            .setOrigin(0.5)
            .setScrollFactor(0)
            .setAlpha(0.8).setDepth(1000);


        this.victoryText = this.add.text(
            width / 2, height / 2 - 80, `You placed ${place}!`, {
                fontSize: '52px', fontFamily: 'Arial',
                color: '#00ff00', stroke: '#000000',
                strokeThickness: 6
            }
        ).setOrigin(0.5).setScrollFactor(0).setDepth(1001);


        this.add.image(width/2 - 50, height / 2 -200, "victory").setOrigin(0.5).setScrollFactor(0).setDepth(1001);

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
            .setScrollFactor(0).setDepth(1001);

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
            .setScrollFactor(0).setDepth(1001);

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
            .setScrollFactor(0).setDepth(1001);


        const exitBtn = this.add.image(
            width / 2,
            height / 2 + 180,
            'exitButtonSvg'
        )
            .setOrigin(0.5)
            .setScrollFactor(0)
            .setInteractive({ useHandCursor: true }).setDepth(1001);

        exitBtn.on('pointerover', () => {
            exitBtn.setScale(1.1);
        });
        exitBtn.on('pointerout', () => {
            exitBtn.setScale(1.0).setDepth(1001);
        });

        exitBtn.on('pointerdown', async () => {
            try {
                const res = await fetch(
                    `http://localhost:8092/api/wallet/coins/add?playerId=${this.playerId}&amount=${totalCoins}`,
                    { method: 'POST' }
                );
                if (!res.ok) {
                    const err = await res.text();
                    console.error('Error while adding coins: ', err);
                    return;
                }
            } catch (e) {
                console.error('Wallet-API not reachable:', e);
                return;
            }

            this.socket.emit('leaveRoom', { playerId: this.playerId });

            this.socket.disconnect();
            this.scene.start('LobbyScene');
        });


    }

    showDefeatScreen(place, baseCoins, bonus, totalCoins) {
        const {width, height} = this.scale;

        this.add.rectangle(width / 2, height / 2, width - 830, height - 240, 0x000000)
            .setOrigin(0.5)
            .setScrollFactor(0).setAlpha(0.8).setDepth(1000);

        this.victoryText = this.add.text(
            width / 2, height / 2 - 80,  `You placed: ${place}!`, {
                fontSize: '52px', fontFamily: 'Arial',
                color: '#ff0000', stroke: '#000000',
                strokeThickness: 6
            }
        ).setOrigin(0.5).setScrollFactor(0).setDepth(1001);


        this.add.image(width/2 - 50, height / 2 -200, "defeat").setOrigin(0.5).setScrollFactor(0).setDepth(1001);

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
            .setScrollFactor(0).setDepth(1001);

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
            .setScrollFactor(0).setDepth(1001);

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
            .setScrollFactor(0).setDepth(1001);

        const exitBtn = this.add.image(
            width / 2,
            height / 2 + 180,
            'exitButtonSvg'
        )
            .setOrigin(0.5)
            .setScrollFactor(0)
            .setInteractive({ useHandCursor: true }).setDepth(1001);

        exitBtn.on('pointerover', () => {
            exitBtn.setScale(1.1).setDepth(1001);
        });
        exitBtn.on('pointerout', () => {
            exitBtn.setScale(1.0).setDepth(1001);
        });

        exitBtn.on('pointerdown', async() => {
            try {
                const res = await fetch(
                    `http://localhost:8092/api/wallet/coins/add?playerId=${this.playerId}&amount=${totalCoins}`,
                    { method: 'POST' }
                );
                if (!res.ok) {
                    const err = await res.text();
                    console.error('Error while adding coins: ', err);
                    return;
                }
            } catch (e) {
                console.error('Wallet-API not reachable:', e);
                return;
            }

            this.socket.emit('leaveRoom', { playerId: this.playerId });

            this.socket.disconnect();
            this.scene.start('LobbyScene');
        });

    }
}