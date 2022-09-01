package nextstep.subway.domain;

import nextstep.subway.applicaion.dto.PathResponse;
import nextstep.subway.exception.NotFoundLineException;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.WeightedMultigraph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class PathSearch {

    private WeightedMultigraph<Station, DefaultWeightedEdge> graph;
    private List<Line> lines = new ArrayList<>();


    public PathSearch() {
        graph = new WeightedMultigraph(DefaultWeightedEdge.class);
    }

    public PathSearch(List<Line> lines) {
        graph = new WeightedMultigraph(DefaultWeightedEdge.class);
        addPaths(lines);
    }


    /**
     * 지하철 경로 찾기에 필요한 노선, 역, 구간을 등록한다
     * @param lines 노선목록
     */
    public void addPaths(List<Line> lines) {
        if (lines.isEmpty()) {
            throw new NotFoundLineException();
        }

        for (Line line : lines) {
            this.lines.add(line);
            addStations(line.getStations());
            addSections(line.getSections());
        }
    }

    private void addStations(List<Station> stations) {
        stations.forEach(station -> addStation(station));
    }

    private void addStation(Station station) {
        if (!graph.containsVertex(station)) {
            graph.addVertex(station);
        }
    }

    private void addSections(Sections sections) {
        for (Section section : sections.getSections()) {
            Station upStation = section.getUpStation();
            Station downStation = section.getDownStation();
            graph.setEdgeWeight(graph.addEdge(upStation, downStation), section.getDistance());
        }
    }

    /**
     * 출발역에서 도착역까지 최단경로의 거리를 반환한다.
     * @param departure 출발역
     * @param destination 도착역
     */
    public Double getShortestPathDistance(Station departure, Station destination) {
        stationsValidationCheck(departure, destination);

        List<GraphPath> paths = new KShortestPaths(graph, 100).getPaths(departure, destination);
        return paths.stream()
                        .min(Comparator.comparingDouble(GraphPath::getWeight))
                        .get()
                        .getWeight();
    }

    /**
     * 출발역에서 도착역까지 최단경로의 역 목록을 반환한다.
     * @param departure 출발역
     * @param destination 도착역
     */
    public List<Station> getShortestPath(Station departure, Station destination) {
        stationsValidationCheck(departure, destination);

        return new DijkstraShortestPath(graph).getPath(departure, destination).getVertexList();
    }

    public PathResponse findShortestPath(Station departure, Station destination) {
        return new PathResponse(getShortestPath(departure, destination), getShortestPathDistance(departure, destination));
    }

    private void stationsValidationCheck(Station departure, Station destination) {
        if (departure.equals(destination)) {
            throw new IllegalArgumentException("출발역과 도착역이 같을 수 없습니다.");
        }

        Set<Station> stationsRegistered = graph.vertexSet();
        if (!stationsRegistered.containsAll(List.of(departure, destination))) {
            throw new IllegalArgumentException("경로 찾기에 존재하지 않는 역이 포함되어 있습니다. 출발역과 도착역을 확인해주세요");
        }

        if (!checkStationsLinked(departure, destination)) {
            throw new IllegalArgumentException("출발역과 도착역이 연결되어 있지 않습니다.");
        }
    }

    private boolean checkStationsLinked(Station departure, Station destination) {
        Line departureLine = getLineByStation(departure);
        Line destinationLine = getLineByStation(destination);

        return departureLine.getStations().stream()
                .anyMatch(station -> destinationLine.getStations().contains(station));
    }

    private Line getLineByStation(Station station) {
        return lines.stream()
                .filter(line -> line.getStations().contains(station))
                .findFirst().orElseThrow(IllegalArgumentException::new);
    }

}
