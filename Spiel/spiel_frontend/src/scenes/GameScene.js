import Phaser from 'phaser';

export default class GameScene extends Phaser.Scene {
    constructor() {
        super({ key: 'GameScene' });
        this.selectedWeapon = 'RIFLE_BULLET';
        this.isFiring       = false;
        this.fireEvent      = null;
    }

    init(data) {
        this.roomId           = data.roomId;
        this.playerId         = data.playerId;
        this.latestState      = null;
        this.playerSprites    = {};
        this.projectileSprites = {};
        this.keys             = null;
        this.initialZoom      = 1;
    }

    preload() {
        this.load.image('player',   '/assets/PNG/Hitman_1/hitman1_gun.png');
        this.load.tilemapTiledJSON('map1',   '/assets/map.json');
        this.load.image('tileset1', '/assets/Tilesheet/tilesheet_complete_2X.png');
        this.load.image('bomb', '/assets/PNG/explosion/bomb.png');
        this.load.image('sniper',  '/assets/PNG/projectile/sniper.png');
        this.load.image('shotgun_pellet',  '/assets/PNG/projectile/shotgun.png');
        this.load.image('rifle_bullet',  '/assets/PNG/projectile/rifle.png');

        for (let i = 0; i < 25; i++){
            this.load.image(`explosion${i}`, `/assets/PNG/explosion/explosion${i}.png`)
        }
    }

    create() {
        this.input.keyboard.on('keydown', evt => {
            let newWep = this.selectedWeapon;
            switch(evt.code) {
                case 'Digit1':
                    newWep = 'SNIPER';
                    break;
                case 'Digit2':
                    newWep = 'SHOTGUN_PELLET';
                    break;
                case 'Digit3':
                    newWep = 'RIFLE_BULLET';
                    break;
                case 'Digit4':
                    newWep = 'MINE';
                    break;
                    default: return;
            }
            this.selectedWeapon = newWep;
            console.log('Selected weapon:', newWep);
            // **Informiere den Server**
            this.socket.emit('changeWeapon', {
                roomId:         this.roomId,
                playerId:       this.playerId,
                projectileType: newWep
            });
            console.log('Selected weapon:', this.selectedWeapon);
        });


        // 1) Tilemap & Layers
        const map = this.make.tilemap({ key: 'map1' });
        const tileset = map.addTilesetImage('tilesheet_complete_2X', 'tileset1');
        this.groundLayer = map.createLayer('ground', tileset, 0, 0);
        this.grassLayer  = map.createLayer('grass',  tileset, 0, 0);
        this.grassLayer.setCollisionByExclusion([-1]);

        // 2) World- und Kamera-Bounds
        const width  = map.widthInPixels;
        const height = map.heightInPixels;
        this.physics.world.setBounds(0, 0, width, height);

        const cam = this.cameras.main;
        cam.setBounds(0, 0, width, height);

        // 3) Zoom so einstellen, dass die gesamte Map in dein 1280×720‐Fenster passt
        const fitZoom = Math.min(cam.width / width, cam.height / height);
        cam.setZoom(fitZoom);

        // 4) Kamera sofort auf die Kartenzentrum setzen
        cam.centerOn(width / 2, height / 2);

        this.ammoBarBg   = this.add.graphics().setScrollFactor(0);
        this.ammoBarFill = this.add.graphics().setScrollFactor(0);

        // Netzwerk-Update
        this.socket.on('stateUpdate', state => {
            this.latestState = state;
        });

        // WASD
        this.keys = this.input.keyboard.addKeys({
            up: 'W', down: 'S', left: 'A', right: 'D'
        });

        this.input.on('pointerdown', pointer => this.startFiring(pointer));
        this.input.on('pointerup',   ()      => this.stopFiring());
        this.input.on('pointerout',  ()      => this.stopFiring());

        this.anims.create({
            key: 'explode',
            frames: Array.from({ length: 25 }, (_, i) => ({ key: `explosion${i}` })),
            frameRate: 25,    // z.B. 25 fps = 1 Bild/ms
            repeat: 0,
            hideOnComplete: true
        });

        this.explosionGroup = this.add.group();
        this.prevProjectileIds = new Set();
        this.previousMinePositions = {};

        //projectile
        // this.input.on('pointerdown', (pointer) => {
        //     const sprite  = this.playerSprites[this.playerId];
        //     const meState = this.latestState?.players.find(p => p.playerId === this.playerId);
        //     if (!sprite || !meState || meState.ammo <= 0) return;
        //
        //     const direction = new Phaser.Math.Vector2(
        //         pointer.worldX - sprite.x,
        //         pointer.worldY - sprite.y
        //     ).normalize();
        //
        //     this.socket.emit('shootProjectile', {
        //         roomId: this.roomId,
        //         playerId: this.playerId,
        //         direction: { x: direction.x, y: direction.y },
        //         projectileType: this.selectedWeapon
        //     });
        // });

    }

    startFiring(pointer) {
        const meSprite = this.playerSprites[this.playerId];
        const meState  = this.latestState?.players.find(p => p.playerId === this.playerId);
        if (!meSprite || !meState || meState.ammo <= 0) return;

        if (this.selectedWeapon === 'RIFLE_BULLET') {
            if (this.isFiring) return;
            this.isFiring = true;

            this.fireEvent = this.time.addEvent({
                delay: 100,        // 10 shots/sec
                loop:  true,
                callback: () => {
                    const ammoLeft = this.latestState.players
                        .find(p => p.playerId === this.playerId).ammo;
                    if (ammoLeft <= 0) {
                        this.stopFiring();
                        return;
                    }
                    const dir = new Phaser.Math.Vector2(
                        pointer.worldX - meSprite.x,
                        pointer.worldY - meSprite.y
                    ).normalize();
                    this.socket.emit('shootProjectile', {
                        roomId:         this.roomId,
                        playerId:       this.playerId,
                        direction:      { x: dir.x, y: dir.y },
                        projectileType: 'RIFLE_BULLET'
                    });
                }
            });

        } else {
            // Alle anderen Waffen nur einmal pro Klick
            const dir = new Phaser.Math.Vector2(
                pointer.worldX - meSprite.x,
                pointer.worldY - meSprite.y
            ).normalize();
            this.socket.emit('shootProjectile', {
                roomId:         this.roomId,
                playerId:       this.playerId,
                direction:      { x: dir.x, y: dir.y },
                projectileType: this.selectedWeapon
            });
        }
    }

    stopFiring() {
        if (this.fireEvent) {
            this.fireEvent.remove();
            this.fireEvent = null;
        }
        this.isFiring = false;
    }


    update() {
        if (!this.latestState) return;


        // 0) Erkenne, welche Mine-IDs neu verschwunden sind
        const currIds = new Set(this.latestState.projectiles
            .filter(p => p.projectileType === 'MINE')
            .map(p => p.id)
        );

        // Für jede Mine, die letztes Frame da war und jetzt nicht mehr:
        this.prevProjectileIds.forEach(id => {
            if (!currIds.has(id)) {
                // Letzte Position speichern in previousPositions Map
                const lastPos = this.previousMinePositions[id];
                if (lastPos) {
                    const expl = this.explosionGroup.create(
                        lastPos.x, lastPos.y, 'explosion0'
                    ).setOrigin(0.5);
                    expl.play('explode');
                    expl.on('animationcomplete', () => expl.destroy());
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



        // A) Bewegung senden
        const meState = this.latestState.players.find(p => p.playerId === this.playerId);
        if (meState) {
            let x = meState.pos?.x ?? meState.position?.x ?? meState.x ?? 0;
            let y = meState.pos?.y ?? meState.position?.y ?? meState.y ?? 0;
            const step = 10;
            if (this.keys.left.isDown)  x -= step;
            if (this.keys.right.isDown) x += step;
            if (this.keys.up.isDown)    y -= step;
            if (this.keys.down.isDown)  y += step;
            this.socket.emit('move', { roomId: this.roomId, playerId: this.playerId, x, y });
        }

        // B) Render & Kamera-Follow
        const ptr = this.input.activePointer;
        this.latestState.players.forEach(p => {
            const px = p.pos?.x ?? p.position?.x;
            const py = p.pos?.y ?? p.position?.y;
            if (px == null || py == null) return;

            let spr = this.playerSprites[p.playerId];
            if (!spr) {
                // Physics-Sprite, damit Collider & Follow funktionieren
                spr = this.physics.add.sprite(px, py, 'player')
                    .setOrigin(0.5, 0.5);
                this.playerSprites[p.playerId] = spr;

                if (p.playerId === this.playerId) {
                    // Kamera folgt exakt (kein Delay) und zentriert dich immer
                    this.cameras.main.startFollow(spr, false, 1, 1);
                    // setzt den weiterhin initialen (herausgezoomten) Zoom
                    this.cameras.main.setZoom(this.initialZoom);
                    // Kollision gegen Grass-Layer
                    this.physics.add.collider(spr, this.grassLayer);
                }
            }

            // Position und Drehung updaten
            spr.setPosition(px, py);
            if (p.playerId === this.playerId) {
                const angle = Phaser.Math.Angle.Between(px, py, ptr.worldX, ptr.worldY);
                spr.setRotation(angle);
            }
        });

        // C) Aufräumen
        Object.keys(this.playerSprites).forEach(id => {
            if (!this.latestState.players.find(p => p.playerId === id)) {
                this.playerSprites[id].destroy();
                delete this.playerSprites[id];
            }
        });


        // --- C) Projektile rendern und aufräumen ---
        // 1) alle aktuellen Projektile zeichnen/aktualisieren
        const aliveIds = new Set();
        this.latestState.projectiles?.forEach(p => {
            let key = 'projectile';
            switch(p.projectileType) {
                case 'SNIPER':          key = 'sniper';  break;
                case 'SHOTGUN_PELLET':  key = 'shotgun_pellet';  break;
                case 'RIFLE_BULLET':    key = 'rifle_bullet';  break;
                case "MINE":            key = "bomb"; break;
            }

            aliveIds.add(p.id);
            let spr = this.projectileSprites[p.id];
            if (!spr) {
                console.log('➕ creating projectile sprite', p.id);
                //spr = this.physics.add.sprite(p.position.x, p.position.y, 'projectile').setOrigin(0.5, 0.5);
                spr = this.physics.add.sprite(p.position.x, p.position.y, key).setOrigin(0.5, 0.5);
                if (p.projectileType === 'MINE') {
                    spr.setScale(0.3);    // skaliert die Bombe auf 30% der Originalgröße
                }
                this.projectileSprites[p.id] = spr;
            }
            else {
                spr.setPosition(p.position.x, p.position.y);
            }
        });

        // Projektile aufräumen
        Object.keys(this.projectileSprites).forEach(id => {
            if (!aliveIds.has(id)) {
                console.log('➖ destroying projectile sprite', id);
                this.projectileSprites[id].destroy();
                delete this.projectileSprites[id];
            }
        });

        // —– D) Ammo-Bar zeichnen —–
        const weapon = meState.currentWeapon;
        const ammo= meState?.ammo ?? 0;
        const maxAmmo  = weapon === 'RIFLE_BULLET' ? 15 : 3;
        const barX     = 10;
        const barY     = 10;
        const barW     = 100;
        const barH     = 10;

        this.ammoBarBg.clear();
        this.ammoBarBg.fillStyle(0x000000, 0.5);
        this.ammoBarBg.fillRect(barX, barY, barW, barH);

        this.ammoBarFill.clear();
        this.ammoBarFill.fillStyle(0xffffff, 1);
        const fillW = Math.floor((barW - 4) * (ammo / maxAmmo));
        this.ammoBarFill.fillRect(barX + 2, barY + 2, fillW, barH - 4);

    }
}
