import Phaser from 'phaser';

export default class GameScene extends Phaser.Scene {
    constructor() {
        super({ key: 'GameScene' });
    }

    init(data) {
        this.roomId = data.roomId;
        this.playerId = data.playerId;
        this.latestState = null;
        this.playerSprites = {};
        this.projectileSprites = {};
        this.keys = null;
    }

    create() {
        // Karte erzeugen und Layer bauen
        const map = this.make.tilemap({ key: 'map' });
        console.log('Map Layers:', map.layers.map(l => l.name));
        //const tsStruct = map.addTilesetImage('TX Struct', 'tiles_struct');
        const tsGrass = map.addTilesetImage('TX Tileset Grass', 'TX Tileset Grass');
        const tsWall  = map.addTilesetImage('TX Tileset Wall',  'TX Tileset Wall');
        const tsProps = map.addTilesetImage('TX Props', 'TX Props');

        // wenn Probs das gleiche Tileset nutzt
        map.createLayer('Ground Tiles', [ tsGrass ], 0, 0);
        map.createLayer('Probs Tiles',  [ tsProps ], 0, 0);
        map.createLayer('Wall Tiles',   [ tsWall, tsProps  ], 0, 0);

        //map.createLayer('Struct Tiles', [ tsStruct  ], 0, 0);

        // Kollision aktivieren, falls ihr in Tiled „collides=true“ gesetzt habt
        // layer.setCollisionByProperty({ collides: true });

        // Optional: Kamerabereich auf Karten-Größe beschränken
         const width = map.widthInPixels;
         const height = map.heightInPixels;
         this.cameras.main.setBounds(0, 0, width, height);
         this.physics.world.setBounds(0, 0, width, height);

        // Spawnposition (aus Objekt-Layer „Objects“, Objekt-Typ „Spawn“)
        //     const spawn = map.findObject(
        //         'Objects',
        //         obj => obj.type === 'Spawn'
        //     );
        //     this.player = this.physics.add.sprite(spawn.x, spawn.y, 'tiles', 0);
        //
             // Collider zwischen Player und Layer
        //     this.physics.add.collider(this.player, layer);

             // Kamera folgt dem Player
          //   this.cameras.main.startFollow(this.player);

        const worldWidth  = map.widthInPixels;
        const worldHeight = map.heightInPixels;
        this.cameras.main.setBounds(0, 0, worldWidth, worldHeight);
        this.physics.world.setBounds(0, 0, worldWidth, worldHeight);

        const cam = this.cameras.main;
        const zoomX = cam.width  / worldWidth;
        const zoomY = cam.height / worldHeight;
        const zoom  = Math.min(zoomX, zoomY);

        cam.setZoom(zoom);
        cam.centerOn(worldWidth/2, worldHeight/2);

        console.log('[GameScene] create() loaded, roomId =', this.roomId);

        // Keyboard input registration for debugging
        this.input.keyboard.on('keydown', (event) => {
            console.log('[GameScene] keydown event:', event.key);
        });
        this.input.keyboard.on('keyup', (event) => {
            console.log('[GameScene] keyup event:', event.key);
        });

        // Pointer input for click moves
        this.input.on('pointerdown', (pointer) => {
            const payload = {
                roomId: this.roomId,
                playerId: this.playerId,
                x: pointer.worldX,
                y: pointer.worldY
            };
            console.log('[GameScene] pointerdown emit move:', payload);
            this.socket.emit('move', payload);
        });

        // Listen for state updates
        this.socket.on('stateUpdate', (state) => {
            console.log('🎮 stateUpdate received with payload:', state);
            this.latestState = state;
        });

        // Define WASD keys for movement
        this.keys = this.input.keyboard.addKeys({ up: 'W', down: 'S', left: 'A', right: 'D' });
    }

    update() {
        if (!this.latestState || !Array.isArray(this.latestState.players)) return;

        // Key-based movement
        const me = this.latestState.players.find(p => p.playerId === this.playerId);
        if (me) {
            let moved = false;
            let newX = (me.pos?.x ?? me.position?.x ?? me.x) || 0;
            let newY = (me.pos?.y ?? me.position?.y ?? me.y) || 0;
            const step = 10;

            if (this.keys.left.isDown)  { newX -= step; moved = true; }
            if (this.keys.right.isDown) { newX += step; moved = true; }
            if (this.keys.up.isDown)    { newY -= step; moved = true; }
            if (this.keys.down.isDown)  { newY += step; moved = true; }

            if (moved) {
                const payload = { roomId: this.roomId, playerId: this.playerId, x: newX, y: newY };
                console.log('[GameScene] key movement emit move:', payload);
                this.socket.emit('move', payload);
            }
        }

        // Render player sprites
        this.latestState.players.forEach(p => {
            const pos = { x: (p.pos?.x ?? p.position?.x), y: (p.pos?.y ?? p.position?.y) };
            if (pos.x == null || pos.y == null) {
                console.warn('Missing position in player state:', p);
                return;
            }
            let spr = this.playerSprites[p.playerId];
            if (!spr) {
                spr = this.add.circle(pos.x, pos.y, 20, 0x00ff00);
                this.playerSprites[p.playerId] = spr;
            }
            spr.setPosition(pos.x, pos.y);
        });

        // Render projectile sprites
        this.latestState.projectiles?.forEach(pr => {
            const pos = { x: (pr.pos?.x ?? pr.position?.x), y: (pr.pos?.y ?? pr.position?.y) };
            if (pos.x == null || pos.y == null) {
                console.warn('Missing position in projectile state:', pr);
                return;
            }
            let spr = this.projectileSprites[pr.id];
            if (!spr) {
                spr = this.add.circle(pos.x, pos.y, 5, 0xff0000);
                this.projectileSprites[pr.id] = spr;
            }
            spr.setPosition(pos.x, pos.y);
        });

        // Clean up sprites for removed entities
        Object.keys(this.playerSprites).forEach(id => {
            if (!this.latestState.players.find(p => p.playerId === id)) {
                this.playerSprites[id].destroy(); delete this.playerSprites[id];
            }
        });
        Object.keys(this.projectileSprites).forEach(id => {
            if (!this.latestState.projectiles.find(pr => pr.id === id)) {
                this.projectileSprites[id].destroy(); delete this.projectileSprites[id];
            }
        });
    }
}
