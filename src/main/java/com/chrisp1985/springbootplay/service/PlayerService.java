package com.chrisp1985.springbootplay.service;

import com.chrisp1985.springbootplay.model.Player;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {

    public String returnPlayerName(Player player) {
        return player.name();
    }
}
