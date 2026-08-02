package com.chrisp1985.springbootplay.model.mapper;

import com.chrisp1985.springbootplay.model.PlayerRequest;
import com.chrisp1985.springbootplay.model.entity.FullPlayer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlayerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "aiSummary", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    FullPlayer toEntity(PlayerRequest dto);
    PlayerRequest toDto(FullPlayer entity);
}
