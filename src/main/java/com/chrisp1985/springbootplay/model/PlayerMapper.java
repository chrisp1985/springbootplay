package com.chrisp1985.springbootplay.model;

import com.chrisp1985.springbootplay.model.entity.FullPlayer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlayerMapper {

    FullPlayer toRequest(PlayerRequest dto);
    PlayerRequest toEntity(FullPlayer request);
}
