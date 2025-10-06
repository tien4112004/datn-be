package com.datn.datnbe.document.repository;

import com.datn.datnbe.document.entity.Presentation;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PresentationRepository extends MongoRepository<Presentation, String> {

    @Query("{ 'title': { $regex: ?0, $options: 'i' }, 'deleted_at': null }")
    Page<Presentation> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @Query("{ 'deleted_at': null }")
    Page<Presentation> findAll(Pageable pageable);

    @Query(value = "{ 'title': { $regex: ?0, $options: '' }, 'deleted_at': null }", sort = "{'title':  1}")
    java.util.List<Presentation> findByTitlePattern(String titlePattern);

    @Query(value = "{ 'title': { $regex: ?0, $options: '' }, 'deleted_at': null }", sort = "{ 'title': -1 }")
    java.util.List<Presentation> findByTitlePatternOrderByTitleDesc(String titlePattern);

    @Query("{ '_id': ?0, 'deleted_at': null }")
    Optional<Presentation> findById(ObjectId id);

    @Query("{ '_id': ?0, 'deleted_at': null }")
    @Update(pipeline = {"""
            { $set: {
                slides: { $map: {
                    input: "$slides", as: "s",
                    in: { $cond: [
                        { $eq: [ "$$s.id", ?1 ] },
                        { $mergeObjects: [
                            "$$s",
                            { elements: { $map: {
                                input: "$$s.elements", as: "e",
                                in: { $cond: [
                                    { $and: [
                                        { $eq: [ "$$e.id", ?2 ] },
                                        { $eq: [ { $toLower: "$$e.type" }, "image" ] }
                                    ] },
                                    { $mergeObjects: [
                                        "$$e",
                                        { extraFields: { $mergeObjects: [
                                            { $ifNull: [ "$$e.extraFields", {} ] },
                                            { src: ?3 }
                                        ] } }
                                    ] },
                                    "$$e"
                                ] }
                            } } }
                        ] },
                        "$$s"
                    ] }
                } }
            } }
            """})

    long insertImageToPresentation(ObjectId presentationId, String slideId, String elementId, String imageUrl);
    
    @Query(value = "{ 'title': ?0, 'deleted_at': null }", exists = true)
    boolean existsByTitle(String title);
}
