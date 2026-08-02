package com.chrisp1985.springbootplay.model.mapper;

import com.chrisp1985.springbootplay.model.PlayerRequest;
import com.chrisp1985.springbootplay.model.entity.AuditLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuditMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "playerName", source = "name")
    @Mapping(target = "action", constant = "CREATED")
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    AuditLog toEntity(PlayerRequest request);
}
