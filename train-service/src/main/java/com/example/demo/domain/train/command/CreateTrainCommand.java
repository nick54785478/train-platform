package com.example.demo.domain.train.command;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateTrainCommand {

	private Integer trainNo; // 火車代號

	private String trainKind;

	private List<CreateStopCommand> stops;

}
