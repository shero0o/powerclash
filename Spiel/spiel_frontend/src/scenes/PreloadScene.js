import Phaser from 'phaser';

export default class PreloadScene extends Phaser.Scene {
    constructor() { super({ key: 'PreloadScene' }); }

    preload() {
        // Beispiel: lade Bild und Audio
        //this.load.image('player', '/assets/player.png');
        //this.load.image('projectile', '/assets/proj.png');
        //this.load.audio('bgm', '/assets/bgm.mp3');
        // Ladebalken, falls gewünscht…
    }

    create() {
        // nach Fertig: Wechsel zu Countdown
        this.scene.start('CountdownScene', this.sys.settings.data);
    }
}
