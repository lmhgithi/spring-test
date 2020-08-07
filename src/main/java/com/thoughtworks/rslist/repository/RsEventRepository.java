package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.dto.RsEventDto;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface RsEventRepository extends CrudRepository<RsEventDto, Integer> {
  List<RsEventDto> findAll();

  @Transactional
  void deleteAllByUserId(int userId);

  Optional<RsEventDto> findByTradeRank(int rank);

  void deleteByTradeRank(int TradeRank);
  @Transactional
  Optional<RsEventDto> findById(int id);
}
