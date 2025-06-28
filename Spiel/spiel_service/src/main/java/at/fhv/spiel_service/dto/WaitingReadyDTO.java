package at.fhv.spiel_service.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WaitingReadyDTO {
    private String playerId;
    private String roomId;
    private String brawlerId;
    private String levelId;
    private String playerName;
}
