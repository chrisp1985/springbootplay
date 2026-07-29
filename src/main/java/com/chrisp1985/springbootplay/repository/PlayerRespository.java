package com.chrisp1985.springbootplay.repository;

import com.chrisp1985.springbootplay.model.entity.FullPlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRespository  extends JpaRepository<FullPlayer, String> {

}
