package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.dto.TradeDto;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface TradeRepository extends CrudRepository {
    List<TradeDto> findAll();
    Optional<TradeDto> findByRank(int rank);
}
