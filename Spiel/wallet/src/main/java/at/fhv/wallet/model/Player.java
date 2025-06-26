package at.fhv.wallet.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "player")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    @Id
    @Column(name = "player_id")
    private UUID id;

    private Integer coins;

    @OneToOne(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private Selected selected;

    @ManyToMany
    @JoinTable(
            name = "player_brawler",
            joinColumns = @JoinColumn(name = "player_id"),
            inverseJoinColumns = @JoinColumn(name = "brawler_id")
    )
    private List<Brawler> brawlers = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "player_gadget",
            joinColumns = @JoinColumn(name = "player_id"),
            inverseJoinColumns = @JoinColumn(name = "gadget_id")
    )
    private List<Gadget> gadgets = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "player_level",
            joinColumns = @JoinColumn(name = "player_id"),
            inverseJoinColumns = @JoinColumn(name = "level_id")
    )
    private List<Level> levels = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "player_weapon",
            joinColumns = @JoinColumn(name = "player_id"),
            inverseJoinColumns = @JoinColumn(name = "weapon_id")
    )
    private List<Weapon> weapons = new ArrayList<>();
}
