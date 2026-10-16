package com.astha.graph.repository;

import com.astha.graph.domain.GraphEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GraphEdgeRepository extends JpaRepository<GraphEdge, String> {
    List<GraphEdge> findBySourceNodeId(String sourceNodeId);
    List<GraphEdge> findByTargetNodeId(String targetNodeId);
    List<GraphEdge> findBySourceNodeIdOrTargetNodeId(String sourceNodeId, String targetNodeId);
}
