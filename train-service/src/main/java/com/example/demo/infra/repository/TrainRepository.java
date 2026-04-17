package com.example.demo.infra.repository;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.domain.train.aggregate.Train;

@Repository
public interface TrainRepository extends JpaRepository<Train, String> {

	Train findByNumber(Integer number);

	List<Train> findByNumberIn(List<Integer> numberList);

	List<Train> findByUuidIn(List<String> uuidList);

	List<Train> findAll(Specification<Train> specification);

	@Query(value = QUERY, nativeQuery = true)
	List<Train> findByCondition(@Param("trainNo") Integer trainNo, @Param("trainKind") String trainKind,
			@Param("arriveTime") String arriveTime, @Param("fromStop") String fromStop, @Param("toStop") String toStop);

	String QUERY = """
			SELECT
					DISTINCT    t.*
					FROM
					    TRAIN t
					    INNER JOIN TRAIN_STOP from_stop ON t.UUID = from_stop.TRAIN_UUID
					    INNER JOIN TRAIN_STOP to_stop ON t.UUID = to_stop.TRAIN_UUID
			WHERE
			   (
			       (
			           :trainNo = ''
			           OR :trainNo IS NULL
			       )
			       OR t.TRAIN_NO = :trainNo
			   )
			   AND (
			       (
			           :trainKind = ''
			           OR :trainKind IS NULL
			      )
			       OR t.TRAIN_KIND = :trainKind
			   )
			   AND (from_stop.`TIME` > :arriveTime)
			   AND (
			       (
			           (:fromStop IS NULL OR :fromStop = '')
			           OR ( from_stop.`NAME` IS NULL OR :fromStop = '')
			       )
			       OR (
			           from_stop.NAME = :fromStop
			           AND to_stop.NAME = :toStop
			           AND from_stop.SEQ < to_stop.SEQ
			       )
			   );


					""";
}
