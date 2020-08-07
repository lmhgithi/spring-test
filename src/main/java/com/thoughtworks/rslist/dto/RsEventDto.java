package com.thoughtworks.rslist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rs_event")
public class RsEventDto {
  @Id @GeneratedValue private int id;
  private String eventName;
  private String keyword;
  private int voteNum;
  @Builder.Default
  private int tradeRank = Integer.MAX_VALUE;
  @Builder.Default
  private int rank = Integer.MAX_VALUE;
  @ManyToOne private UserDto user;
}
