import Phaser from 'phaser';

export default class CountdownScene extends Phaser.Scene {
    constructor() { super({ key: 'CountdownScene' }); }
    init(data) { this.initData = data; }
    create() {
        let count = 3;
        const txt = this.add.text(640, 360, `${count}`, { fontSize: '64px', color: '#fff' }).setOrigin(0.5);
        this.time.addEvent({
            delay: 1000, repeat: 2,
            callback: () => {
                count--;
                txt.setText(count > 0 ? `${count}` : 'Go!');
                if (count < 0) this.scene.start('GameScene', this.initData);
            }
        });
    }
}
