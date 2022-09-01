package nextstep.subway.applicaion;

import nextstep.subway.applicaion.dto.PathResponse;
import nextstep.subway.domain.Line;
import nextstep.subway.domain.LineRepository;
import nextstep.subway.domain.PathSearch;
import nextstep.subway.domain.Station;
import nextstep.subway.exception.NotFoundLineException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PathService {
    private LineRepository lineRepository;
    private StationService stationService;

    public PathService(LineRepository lineRepository, StationService stationService) {
        this.lineRepository = lineRepository;
        this.stationService = stationService;
    }

    /**
     * 출발역과 도착역까지의 최단경로 정보를 조회한다.
     * @param source 출발역 id
     * @param target 도착역 id
     */
    public PathResponse showPaths(Long source, Long target) {

        Station departure = stationService.findById(source);
        Station destination = stationService.findById(target);

        PathSearch pathSearch = new PathSearch(lineRepository.findAll());

        return pathSearch.findShortestPath(departure, destination);
    }
}
