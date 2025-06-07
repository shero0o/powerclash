package at.fhv.wallet.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "weapon")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class Weapon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String weaponType;

    @Column(nullable = false)
    private Integer damage;

    @Column(nullable = false)
    private Integer projectileSpeed;

    @Column(nullable = false)
    private Integer range;

    @Column(nullable = false)
    private String weaponCooldown;

    @Column(nullable = false)
    private Integer magazineSize;

}
