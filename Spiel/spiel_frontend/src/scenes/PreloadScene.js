import Phaser from 'phaser';

export default class PreloadScene extends Phaser.Scene {
    constructor() { super({ key: 'PreloadScene' }); }

    preload() {
        this.load.tilemapTiledJSON('map', '/assets/map.json');
        this.load.image('tileset', '/assets/Tilesheet/Tileset_Grass.png');
        this.load.image('TX Tileset Wall', '/assets/Tileset_Wall.png');
        this.load.image('TX Props',  '/assets/Tileset_Props.png');

    }

    create() {
        // nach Fertig: Wechsel zu Countdown
        this.scene.start('GameScene', this.sys.settings.data);
    }
}
