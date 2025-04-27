import Phaser from 'phaser';

export default class PreloadScene extends Phaser.Scene {
    constructor() { super({ key: 'PreloadScene' }); }

    preload() {
        this.load.tilemapTiledJSON('map', '/assets/MapLvL_1.json');
        this.load.image('TX Tileset Grass', '/assets/Tileset_Grass.png');
        this.load.image('TX Tileset Wall', '/assets/Tileset_Wall.png');
        this.load.image('TX Props',  '/assets/Tileset_Props.png');
    }

    create() {
        // nach Fertig: Wechsel zu Countdown
        this.scene.start('CountdownScene', this.sys.settings.data);
    }
}
