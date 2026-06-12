package ir.fum.siliconvalley.model.board;

import ir.fum.siliconvalley.exception.InvalidPlacementException;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class Board implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int size;
    private final Sector[][] sectors;
    private final Map<VertexPosition, Vertex> vertices = new LinkedHashMap<>();
    private final Map<EdgePosition, Edge> edges = new LinkedHashMap<>();
    private SectorPosition auditorPosition;

    public Board(int size, Collection<Sector> sectorCollection) {
        if (size < 2) {
            throw new IllegalArgumentException("Board size must be at least 2");
        }
        this.size = size;
        this.sectors = new Sector[size][size];
        for (Sector sector : sectorCollection) {
            SectorPosition position = sector.getPosition();
            validateSectorPosition(position);
            if (sectors[position.row()][position.column()] != null) {
                throw new IllegalArgumentException("Duplicate sector position: " + position);
            }
            sectors[position.row()][position.column()] = sector;
        }
        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                if (sectors[row][column] == null) {
                    throw new IllegalArgumentException("Missing sector at " + row + "," + column);
                }
            }
        }
        initializeVerticesAndEdges();
    }

    private void initializeVerticesAndEdges() {
        for (int row = 0; row <= size; row++) {
            for (int column = 0; column <= size; column++) {
                VertexPosition position = new VertexPosition(row, column);
                vertices.put(position, new Vertex(position));
            }
        }
        for (int row = 0; row <= size; row++) {
            for (int column = 0; column < size; column++) {
                addEdge(new VertexPosition(row, column), new VertexPosition(row, column + 1));
            }
        }
        for (int row = 0; row < size; row++) {
            for (int column = 0; column <= size; column++) {
                addEdge(new VertexPosition(row, column), new VertexPosition(row + 1, column));
            }
        }
    }

    private void addEdge(VertexPosition first, VertexPosition second) {
        EdgePosition edgePosition = new EdgePosition(first, second);
        edges.put(edgePosition, new Edge(edgePosition));
    }

    public int getSize() {
        return size;
    }

    public Sector getSector(SectorPosition position) {
        validateSectorPosition(position);
        return sectors[position.row()][position.column()];
    }

    public Vertex getVertex(VertexPosition position) {
        Vertex vertex = vertices.get(position);
        if (vertex == null) {
            throw new IllegalArgumentException("Unknown vertex: " + position);
        }
        return vertex;
    }

    public Edge getEdge(EdgePosition position) {
        Edge edge = edges.get(position);
        if (edge == null) {
            throw new IllegalArgumentException("Unknown edge: " + position);
        }
        return edge;
    }

    public Collection<Sector> getSectors() {
        List<Sector> result = new ArrayList<>();
        for (Sector[] row : sectors) {
            Collections.addAll(result, row);
        }
        return Collections.unmodifiableList(result);
    }

    public Collection<Vertex> getVertices() {
        return Collections.unmodifiableCollection(vertices.values());
    }

    public Collection<Edge> getEdges() {
        return Collections.unmodifiableCollection(edges.values());
    }

    public List<Sector> getAdjacentSectors(VertexPosition vertexPosition) {
        getVertex(vertexPosition);
        List<Sector> result = new ArrayList<>(4);
        addSectorIfPresent(result, vertexPosition.row() - 1, vertexPosition.column() - 1);
        addSectorIfPresent(result, vertexPosition.row() - 1, vertexPosition.column());
        addSectorIfPresent(result, vertexPosition.row(), vertexPosition.column() - 1);
        addSectorIfPresent(result, vertexPosition.row(), vertexPosition.column());
        return Collections.unmodifiableList(result);
    }

    public List<Vertex> getAdjacentVertices(SectorPosition sectorPosition) {
        validateSectorPosition(sectorPosition);
        int row = sectorPosition.row();
        int column = sectorPosition.column();
        return List.of(
                getVertex(new VertexPosition(row, column)),
                getVertex(new VertexPosition(row, column + 1)),
                getVertex(new VertexPosition(row + 1, column)),
                getVertex(new VertexPosition(row + 1, column + 1))
        );
    }

    public List<Edge> getIncidentEdges(VertexPosition vertexPosition) {
        getVertex(vertexPosition);
        return edges.values().stream()
                .filter(edge -> edge.getPosition().touches(vertexPosition))
                .toList();
    }

    public Optional<SectorPosition> getAuditorPosition() {
        return Optional.ofNullable(auditorPosition);
    }

    public void setAuditorPosition(SectorPosition position) {
        Objects.requireNonNull(position, "position");
        Sector next = getSector(position);
        if (auditorPosition != null) {
            getSector(auditorPosition).setAudited(false);
        }
        auditorPosition = position;
        next.setAudited(true);
    }

    public void placeCompany(UUID structureId, VertexPosition position) throws InvalidPlacementException {
        Objects.requireNonNull(structureId, "structureId");
        Vertex vertex = getVertex(position);
        if (vertex.getCompanyStructureId().isPresent()) {
            throw new InvalidPlacementException("Vertex is already occupied: " + position);
        }
        vertex.occupy(structureId);
    }

    public void replaceCompany(UUID expectedCurrentId, UUID replacementId, VertexPosition position)
            throws InvalidPlacementException {
        Vertex vertex = getVertex(position);
        if (vertex.getCompanyStructureId().isEmpty()
                || !vertex.getCompanyStructureId().orElseThrow().equals(expectedCurrentId)) {
            throw new InvalidPlacementException("Expected MVP was not found at vertex: " + position);
        }
        vertex.occupy(replacementId);
    }

    public void placePartnership(UUID structureId, EdgePosition position) throws InvalidPlacementException {
        Objects.requireNonNull(structureId, "structureId");
        Edge edge = getEdge(position);
        if (edge.getPartnershipId().isPresent()) {
            throw new InvalidPlacementException("Edge is already occupied: " + position);
        }
        edge.occupy(structureId);
    }

    private void addSectorIfPresent(List<Sector> result, int row, int column) {
        if (row >= 0 && row < size && column >= 0 && column < size) {
            result.add(sectors[row][column]);
        }
    }

    private void validateSectorPosition(SectorPosition position) {
        Objects.requireNonNull(position, "position");
        if (position.row() >= size || position.column() >= size) {
            throw new IllegalArgumentException("Unknown sector: " + position);
        }
    }
}
