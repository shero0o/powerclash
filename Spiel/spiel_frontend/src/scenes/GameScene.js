import Phaser from 'phaser';

export default class GameScene extends Phaser.Scene {
    constructor() {
        super({ key: 'GameScene' });
        // Weapon & firing state
        this.selectedWeapon     = 'RIFLE_BULLET';
        this.isFiring           = false;
        this.fireEvent          = null;
        // UI & state
        this.initialZoom        = 0.7;
        this.maxHealth          = 100;
    }

    init(data) {
        this.roomId             = data.roomId;
        this.playerId           = data.playerId;
        this.mapKey             = data.levelId || 'map';
        this.latestState        = null;
        this.playerSprites      = {};
        this.projectileSprites  = {};
        this.keys               = null;
    }

    preload() {
        // Player, projectiles, tilesets, map
        this.load.image('player', '/assets/PNG/Hitman_1/hitman1_gun.png');
        this.load.image('sniper', '/assets/PNG/projectile/sniper.png');
        this.load.image('shotgun_pellet', '/assets/PNG/projectile/shotgun.png');
        this.load.image('rifle_bullet', '/assets/PNG/projectile/rifle.png');
        this.load.image('mine', '/assets/PNG/explosion/bomb.png');
        for (let i = 0; i < 25; i++) {
            this.load.image(`explosion${i}`, `/assets/PNG/explosion/explosion${i}.png`);
        }
        this.load.tilemapTiledJSON('map', `/assets/${this.mapKey}.tmj`);
        this.load.image('tileset', '/assets/Tilesheet/spritesheet_tiles.png');
    }

    create() {
        // Camera & world
        this.cameras.main.setBackgroundColor('#222222');
        const map = this.make.tilemap({ key: 'map' });
        const tiles = map.addTilesetImage('spritesheet_tiles', 'tileset', 64, 64);
        this.groundLayer = map.createLayer('Boden', tiles, 0, 0);
        this.wallLayer   = map.createLayer('Wand', tiles, 0, 0);
        this.groundLayer.setCollisionByExclusion([-1]);
        this.wallLayer.setCollisionByExclusion([-1]);

        const w = map.widthInPixels;
        const h = map.heightInPixels;
        this.physics.world.setBounds(0, 0, w, h);
        this.cameras.main.setBounds(0, 0, w, h);
        this.cameras.main.setZoom(this.initialZoom);

        // Input & UI
        this.keys = this.input.keyboard.addKeys({
            up: 'W', down: 'S', left: 'A', right: 'D'
        });
        this.playerCountText = this.add.text(16, 16, '0/0 players', {
            font: 'bold 24px Arial',
            fill: '#fff',
            stroke: '#000', strokeThickness: 3
        }).setScrollFactor(0);
        this.exitButton = this.add.text(16, 56, 'Exit', { fontSize:'18px', fill:'#f00' })
            .setScrollFactor(0).setInteractive().setVisible(false)
            .on('pointerdown', () => {
                this.socket.emit('leaveRoom', { roomId: this.roomId, playerId: this.playerId });
                this.scene.start('SplashScene');
            });

        // Ammo bar graphics
        this.ammoBarBg   = this.add.graphics().setScrollFactor(0);
        this.ammoBarFill = this.add.graphics().setScrollFactor(0);

        // Explosion animation
        this.anims.create({
            key: 'explode',
            frames: Array.from({ length: 25 }, (_, i) => ({ key: `explosion${i}` })),
            frameRate: 25,
            repeat: 0,
            hideOnComplete: true
        });
        this.explosionGroup = this.add.group();
        this.prevProjectileIds = new Set();
        this.previousMinePositions = {};

        // Networking
        this.socket.on('stateUpdate', state => this.latestState = state);
        this.input.keyboard.on('keydown', evt => this.changeWeapon(evt));
        this.input.on('pointerdown', ptr => this.startFiring(ptr));
        this.input.on('pointerup',  ()  => this.stopFiring());

        // Physics groups & collisions
        this.projectilesGroup = this.physics.add.group();
        this.physics.add.collider(this.projectilesGroup, this.wallLayer, (proj) => proj.destroy());
        // Will overlap with players when created
    }

    changeWeapon(evt) {
        const mapKey = { Digit1: 'SNIPER', Digit2: 'SHOTGUN_PELLET', Digit3: 'RIFLE_BULLET', Digit4: 'MINE' }[evt.code];
        if (!mapKey) return;
        this.selectedWeapon = mapKey;
        this.socket.emit('changeWeapon', { roomId: this.roomId, playerId: this.playerId, projectileType: mapKey });
    }

    startFiring(pointer) {
        const meState = this.latestState?.players.find(p => p.playerId === this.playerId);
        const meSprite = this.playerSprites[this.playerId];
        if (!meState || !meSprite || meState.ammo <= 0) return;
        const shoot = () => {
            const dir = new Phaser.Math.Vector2(
                pointer.worldX - meSprite.x, pointer.worldY - meSprite.y
            ).normalize();
            this.socket.emit('shootProjectile', {
                roomId: this.roomId, playerId: this.playerId,
                direction: { x: dir.x, y: dir.y }, projectileType: this.selectedWeapon
            });
        };
        if (this.selectedWeapon === 'RIFLE_BULLET' && !this.isFiring) {
            this.isFiring = true;
            this.fireEvent = this.time.addEvent({ delay:100, loop:true, callback:()=>{
                    if (this.latestState.players.find(p=>p.playerId===this.playerId).ammo<=0) {
                        this.stopFiring(); return; }
                    shoot();
                }});
        } else if (this.selectedWeapon !== 'RIFLE_BULLET') {
            shoot();
        }
    }

    stopFiring() {
        if (this.fireEvent) this.fireEvent.remove();
        this.isFiring = false;
    }

    update() {
        if (!this.latestState) return;
        // Update player count
        const alive = this.latestState.players.filter(p=>p.currentHealth>0).length;
        this.playerCountText.setText(`${alive}/${this.latestState.players.length} players`);
        // Show exit if dead
        const me = this.latestState.players.find(p=>p.playerId===this.playerId);
        if (me?.currentHealth<=0) this.exitButton.setVisible(true);

        // Handle mine explosions
        const currIds = new Set(this.latestState.projectiles.filter(p=>p.projectileType==='MINE').map(p=>p.id));
        this.prevProjectileIds.forEach(id => {
            if (!currIds.has(id) && this.previousMinePositions[id]) {
                const pos = this.previousMinePositions[id];
                const e = this.explosionGroup.create(pos.x,pos.y,'explosion0').setOrigin(0.5);
                e.play('explode');
            }
        });
        this.previousMinePositions = {};
        this.latestState.projectiles.forEach(p=>{ if(p.projectileType==='MINE') this.previousMinePositions[p.id] = p.position; });
        this.prevProjectileIds = currIds;

        // Send movement
        if (me) {
            const dirX = (this.keys.left.isDown?-1:0)+(this.keys.right.isDown?1:0);
            const dirY = (this.keys.up.isDown?-1:0)+(this.keys.down.isDown?1:0);
            const ptr = this.input.activePointer;
            const world = this.cameras.main.getWorldPoint(ptr.x,ptr.y);
            const angle = Phaser.Math.Angle.Between(me.position.x,me.position.y,world.x,world.y);
            this.socket.emit('move', { roomId:this.roomId, playerId:this.playerId, dirX, dirY, angle });
        }

        // Render players
        this.latestState.players.forEach(p => {
            if (p.currentHealth<=0) return;
            let spr = this.playerSprites[p.playerId];
            if (!spr) {
                spr = this.physics.add.sprite(p.position.x,p.position.y,'player').setOrigin(0.5);
                spr.healthBar = this.add.graphics();
                this.playerSprites[p.playerId] = spr;
                this.physics.add.overlap(this.projectilesGroup, spr, (proj, hit)=>{
                    proj.destroy();
                });
                if (p.playerId===this.playerId) {
                    this.cameras.main.startFollow(spr);
                }
            }
            spr.setPosition(p.position.x,p.position.y).setRotation(p.position.angle).setVisible(p.visible);
            // Health bar
            const pct = Phaser.Math.Clamp(p.currentHealth/this.maxHealth,0,1);
            spr.healthBar.clear()
                .fillStyle(0x000000).fillRect(p.position.x-21,p.position.y-spr.height/2-16,42,8)
                .fillStyle(0x00ff00).fillRect(p.position.x-20,p.position.y-spr.height/2-15,40*pct,6);
        });

        // Render projectiles
        const seen = new Set();
        this.latestState.projectiles.forEach(p => {
            seen.add(p.id);
            let spr = this.projectileSprites[p.id];
            if (!spr) {
                spr = this.physics.add.sprite(p.position.x,p.position.y,
                    { SNIPER:'sniper', SHOTGUN_PELLET:'shotgun_pellet', RIFLE_BULLET:'rifle_bullet', MINE:'mine' }[p.projectileType]
                ).setOrigin(0.5).setScale(p.projectileType==='MINE'?0.3:1);
                this.projectileSprites[p.id] = spr;
                this.projectilesGroup.add(spr);
            } else spr.setPosition(p.position.x,p.position.y);
        });
        Object.keys(this.projectileSprites).forEach(id => {
            if (!seen.has(id)) {
                this.projectileSprites[id].destroy(); delete this.projectileSprites[id];
            }
        });

        // Draw ammo bar
        const weapon = me?.currentWeapon;
        const ammo   = me?.ammo ?? 0;
        const max    = weapon==='RIFLE_BULLET'?15:(weapon==='SNIPER'||weapon==='MINE'?1:3);
        this.ammoBarBg.clear().fillStyle(0x000000,0.5).fillRect(10,10,100,10);
        this.ammoBarFill.clear().fillStyle(0xffffff).fillRect(12,12, (100-4)*(ammo/max), 6);
    }
}