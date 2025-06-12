package at.fhv.wallet.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "weapon")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class Weapon {
    @Id
    @Column(name = "weapon_id")
    private Long id;
    private String weaponType;
    private Integer damage;
    private Integer projectileSpeed;
    private Integer range;
    private String weaponCooldown;
    private Integer magazineSize;

}
