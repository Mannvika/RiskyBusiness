package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@CrossOrigin(origins = "*")
public class DemoController {
    private final ConcurrentHashMap<String, Boolean> validGameIDs = new ConcurrentHashMap<>();
    private int playerID = 0;
    private boolean[] playersReady = new boolean[2];

    // Generate a random gameID (for demonstration, only one is generated)
    public DemoController() {
        Random random = new Random();
        int randomNumber = random.nextInt(10000);
        String gameID = String.format("%04d", randomNumber);
        validGameIDs.put(gameID, true);
        System.out.println("Generated Game ID: " + gameID);
    }

    // Endpoint for Player 1
    @GetMapping("/api/gameID/{gameID}")
    public ResponseEntity<String> connectToGame(@PathVariable String gameID) {
        if (!validGameIDs.containsKey(gameID)) {
            return ResponseEntity.badRequest().body("Invalid Game ID");
        }
        return ResponseEntity.ok("Hello Player for Game ID: " + gameID);
    }

    @GetMapping("/api/gameID/{gameID}/lobby")
    public ResponseEntity<String> joinLobby(@PathVariable String gameID) {
        if (!validGameIDs.containsKey(gameID)) {
            return ResponseEntity.badRequest().body("Invalid Game ID");
        }
        return ResponseEntity.ok(String.valueOf(playerID++));
    }

    // Endpoint to submit a player message
    @PostMapping("/api/gameID/{gameID}/ready")
    public ResponseEntity<String> submitPlayerID(@PathVariable String gameID, @RequestBody PlayerRequest playerRequest) {
        if (!validGameIDs.containsKey(gameID)) {
            return ResponseEntity.badRequest().body("Invalid Game ID");
        }
        int playerID = Integer.parseInt(playerRequest.getPlayerID());
        playersReady[playerID] = true;
        System.out.println("Received ready for player: " + playerRequest.getPlayerID());
        if(playersReady[0] && playersReady[1])
        {
            return ResponseEntity.ok("start");
        }
        else
        {
            return ResponseEntity.ok("waiting");
        }
    }

    // DTO for Player Request
    public static class PlayerRequest {
        private String playerID;

        public String getPlayerID() {
            return playerID;
        }

        public void setPlayerID(String playerMessage) {
            this.playerID = playerMessage;
        }
    }
}
