package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RsService{
  final RsEventRepository rsEventRepository;
  final UserRepository userRepository;
  final VoteRepository voteRepository;
  final TradeRepository tradeRepository;;

  public RsService(RsEventRepository rsEventRepository, UserRepository userRepository, VoteRepository voteRepository, TradeRepository tradeRepository) {
    this.rsEventRepository = rsEventRepository;
    this.userRepository = userRepository;
    this.voteRepository = voteRepository;
    this.tradeRepository = tradeRepository;
  }


  public ResponseEntity<List<RsEvent>> getListByOrder(Integer start, Integer end){

    List<RsEventDto> rsEventDtoList = rsEventRepository.findAll();
    Collections.sort(rsEventDtoList);
      for (int i = rsEventDtoList.size()-1; i >= 0; i--) {
          int p = rsEventDtoList.get(i).getTradeRank();
          if(p != Integer.MAX_VALUE && p > i+1){
              int j = i+0;
              while(rsEventDtoList.get(j).getTradeRank() > j+1){
                Collections.swap(rsEventDtoList, j, j+1);
                j++;
              }
          }
      }

    List<RsEvent> rsEventSorted = rsEventDtoList.stream()
          .map(rsEventDto ->
                  RsEvent.builder()
                          .eventName(rsEventDto.getEventName())
                          .keyword(rsEventDto.getKeyword())
                          .userId(rsEventDto.getUser().getId())
                          .voteNum(rsEventDto.getVoteNum())
                          .tradeRank(rsEventDto.getTradeRank())
                          .build()
          )
          .collect(Collectors.toList());
    if (start == null || end == null) {
      return ResponseEntity.ok(rsEventSorted);
    }
    return ResponseEntity.ok(rsEventSorted.subList(start - 1, end));
  }

  public void vote(Vote vote, int rsEventId) {
    Optional<RsEventDto> rsEventDto = rsEventRepository.findById(rsEventId);
    Optional<UserDto> userDto = userRepository.findById(vote.getUserId());
    if (!rsEventDto.isPresent()
        || !userDto.isPresent()
        || vote.getVoteNum() > userDto.get().getVoteNum()) {
      throw new RuntimeException();
    }
    VoteDto voteDto =
        VoteDto.builder()
            .localDateTime(vote.getTime())
            .num(vote.getVoteNum())
            .rsEvent(rsEventDto.get())
            .user(userDto.get())
            .build();
    voteRepository.save(voteDto);
    UserDto user = userDto.get();
    user.setVoteNum(user.getVoteNum() - vote.getVoteNum());
    userRepository.save(user);
    RsEventDto rsEvent = rsEventDto.get();
    rsEvent.setVoteNum(rsEvent.getVoteNum() + vote.getVoteNum());
    rsEventRepository.save(rsEvent);
  }

  @Transactional
  public ResponseEntity buy(Trade trade, int id) {
    Optional<TradeDto> tradeDto = tradeRepository.findByRank(trade.getRank());
    if(tradeDto.isPresent() && tradeDto.get().getAmount() > trade.getAmount()){
      return ResponseEntity.badRequest().build();
    }else{
      TradeDto tradeDtoToSave = TradeDto.builder().amount(trade.getAmount())
              .rank(trade.getRank()).build();
      tradeDto.ifPresent(dto -> rsEventRepository.deleteByTradeRank(dto.getRank()));
      tradeDto.ifPresent(dto -> tradeRepository.deleteByRank(dto.getRank()));

      Optional<RsEventDto> rsEventDtoTraded = rsEventRepository.findById(id);
      if(rsEventDtoTraded.isPresent()) {
        rsEventDtoTraded.get().setTradeRank(trade.getRank());
        rsEventRepository.save(rsEventDtoTraded.get());
      }
      tradeRepository.save(tradeDtoToSave);
      return ResponseEntity.ok().build();
    }
  }
}
