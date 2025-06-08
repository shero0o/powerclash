package at.fhv.shop_catalogue.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "gadget")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class Gadget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private String description;
}
