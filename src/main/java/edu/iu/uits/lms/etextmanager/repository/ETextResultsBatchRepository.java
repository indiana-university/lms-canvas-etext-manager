package edu.iu.uits.lms.etextmanager.repository;

import edu.iu.uits.lms.etextmanager.model.ETextResultsBatch;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

@Component
@RepositoryRestResource(path = "etext_results_batch",
        itemResourceDescription = @Description("asdf asdf"),
        collectionResourceDescription = @Description("qwerty qwerty"))
@Tag(name = "ETextResultsBatchRepository", description = "Operations involving the ETextResultsBatch table")
@CrossOrigin(origins = {"${lms.swagger.cors.origin}"})
public interface ETextResultsBatchRepository extends PagingAndSortingRepository<ETextResultsBatch, Long> {

}