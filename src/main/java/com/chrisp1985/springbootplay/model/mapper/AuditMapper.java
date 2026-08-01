package com.chrisp1985.springbootplay.model.mapper;

import com.chrisp1985.springbootplay.model.PlayerRequest;
import com.chrisp1985.springbootplay.model.entity.AuditLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuditMapper {

    PlayerRequest toRequest(AuditLog dto);

    @Mapping(target = "id", ignore = true)
    AuditLog toEntity(PlayerRequest request);
}
