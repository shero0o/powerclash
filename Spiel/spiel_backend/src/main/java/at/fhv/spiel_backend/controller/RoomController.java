package at.fhv.spiel_backend.controller;


import at.fhv.spiel_backend.DTO.JoinRequestDTO;
import at.fhv.spiel_backend.DTO.JoinResponseDTO;
import at.fhv.spiel_backend.server.room.IRoomManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "http://localhost:5173")
public class RoomController {
    private final IRoomManager roomManager;

    public RoomController(IRoomManager roomManager){
        this.roomManager = roomManager;
    }

    @PostMapping("/join")
    public ResponseEntity<JoinResponseDTO> joinRoom(@RequestBody JoinRequestDTO joinRequestDTO){
        String playerId  = joinRequestDTO.getPlayerId();
        String brawlerId = joinRequestDTO.getBrawlerId();
        String levelId   = joinRequestDTO.getLevelId();

        String roomId = roomManager.assignToRoom(playerId, brawlerId, levelId);
        JoinResponseDTO joinResponseDTO = new JoinResponseDTO(roomId);
        return ResponseEntity.ok(joinResponseDTO);
    }


}
