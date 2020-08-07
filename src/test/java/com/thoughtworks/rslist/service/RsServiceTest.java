package com.thoughtworks.rslist.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class RsServiceTest {
  RsService rsService;

  @Mock RsEventRepository rsEventRepository;
  @Mock UserRepository userRepository;
  @Mock VoteRepository voteRepository;
  @Mock TradeRepository tradeRepository;
  LocalDateTime localDateTime;
  Vote vote;

  @BeforeEach
  void setUp() {
    initMocks(this);
    rsService = new RsService(rsEventRepository, userRepository, voteRepository, tradeRepository);
    localDateTime = LocalDateTime.now();
    vote = Vote.builder().voteNum(2).rsEventId(1).time(localDateTime).userId(1).build();
  }

  @Test
  void shouldVoteSuccess() {
    // given

    UserDto userDto =
        UserDto.builder()
            .voteNum(5)
            .phone("18888888888")
            .gender("female")
            .email("a@b.com")
            .age(19)
            .userName("xiaoli")
            .id(2)
            .build();
    RsEventDto rsEventDto =
        RsEventDto.builder()
            .eventName("event name")
            .id(1)
            .keyword("keyword")
            .voteNum(2)
            .user(userDto)
            .build();

    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
    when(userRepository.findById(anyInt())).thenReturn(Optional.of(userDto));
    // when
    rsService.vote(vote, 1);
    // then
    verify(voteRepository)
        .save(
            VoteDto.builder()
                .num(2)
                .localDateTime(localDateTime)
                .user(userDto)
                .rsEvent(rsEventDto)
                .build());
    verify(userRepository).save(userDto);
    verify(rsEventRepository).save(rsEventDto);
  }

  @Test
  void shouldThrowExceptionWhenUserNotExist() {
    // given
    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.empty());
    when(userRepository.findById(anyInt())).thenReturn(Optional.empty());
    //when&then
    assertThrows(
        RuntimeException.class,
        () -> {
          rsService.vote(vote, 1);
        });
  }

  @Test
  void shouldAddTradeWhenRankIsNotTraded(){
    // given
    Trade trade = Trade.builder().amount(1).rank(1).build();
    TradeDto tradeDto = TradeDto.builder().amount(1).rank(1).build();
    when(tradeRepository.findByRank(trade.getRank())).thenReturn(Optional.ofNullable(null));
    // when
    rsService.buy(trade, anyInt());
    // then
    verify(tradeRepository).save(tradeDto);
  }

  @Test
  void shouldAddTradeWhenRankIsTradedAndAmountIsEnough(){
    Trade tradeToBuy = Trade.builder().amount(15).rank(1).build();
    TradeDto tradeDtoToBuy = TradeDto.builder().amount(15).rank(1).build();
    TradeDto tradeDtoExists = TradeDto.builder().amount(10).rank(1).build();
    when(tradeRepository.findByRank(tradeToBuy.getRank())).thenReturn(Optional.ofNullable(tradeDtoExists));

    rsService.buy(tradeToBuy, anyInt());

    verify(tradeRepository).save(tradeDtoToBuy);
  }

  @Test
  void shouldUpdateRankOfRsEventAndDeleteOldRsEventWhenAmountIsEnough(){
    Trade tradeToBuy = Trade.builder().amount(15).rank(1).build();
    TradeDto tradeDtoExists = TradeDto.builder().amount(10).rank(1).build();
    RsEventDto rsEventDtoExists = RsEventDto.builder().eventName("1").keyword("1").tradeRank(1).build();
    RsEventDto rsEventDtoToTrade = RsEventDto.builder().eventName("2").keyword("2").build();
    RsEventDto rsEventDtoTraded = RsEventDto.builder().eventName("2").keyword("2").tradeRank(1).build();

    when(tradeRepository.findByRank(tradeToBuy.getRank())).thenReturn(Optional.of(tradeDtoExists));
    when(rsEventRepository.findByTradeRank(1)).thenReturn(Optional.of(rsEventDtoExists));
    when(rsEventRepository.findById(rsEventDtoToTrade.getId())).thenReturn(Optional.of(rsEventDtoToTrade));

    rsService.buy(tradeToBuy, rsEventDtoToTrade.getId());
    verify(tradeRepository).deleteByRank(1);
    verify(rsEventRepository).deleteByTradeRank(rsEventDtoExists.getTradeRank());
    verify(rsEventRepository).save(rsEventDtoTraded);
  }
}
