package com.niyiment.docrag.repository;

import com.niyiment.docrag.entity.ChunkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ChunkRepository extends JpaRepository<ChunkEntity, Integer> {

    List<ChunkEntity> findByDocumentId(Integer documentId);

}
