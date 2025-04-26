package at.fhv.spiel_backend.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Gadget {
    String id;
    EffectType effectType; // enum
    int effectValue;
    float cooldownSeconds;

    public enum EffectType { HEAL, DAMAGE, INVISIBILITY }
}

