package com.chrisp1985.springbootplay.model.mapper;

import com.chrisp1985.springbootplay.model.PlayerRequest;
import com.chrisp1985.springbootplay.model.entity.FullPlayer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlayerMapper {

    @Mapping(target = "id", ignore = true)
    FullPlayer toEntity(PlayerRequest dto);
    PlayerRequest toDto(FullPlayer entity);
}
