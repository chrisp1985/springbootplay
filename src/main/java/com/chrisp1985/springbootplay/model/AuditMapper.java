package com.chrisp1985.springbootplay.model;

import com.chrisp1985.springbootplay.model.entity.AuditLog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditMapper {

    PlayerRequest toRequest(AuditLog dto);
    AuditLog toEntity(PlayerRequest request);
}
