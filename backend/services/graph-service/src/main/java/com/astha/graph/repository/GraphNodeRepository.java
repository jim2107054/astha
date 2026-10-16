package com.astha.graph.repository;

import com.astha.graph.domain.GraphNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GraphNodeRepository extends JpaRepository<GraphNode, String> {
}
