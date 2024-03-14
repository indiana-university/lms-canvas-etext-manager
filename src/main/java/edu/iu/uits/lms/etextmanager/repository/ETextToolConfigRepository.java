package edu.iu.uits.lms.etextmanager.repository;

import edu.iu.uits.lms.etextmanager.model.ETextToolConfig;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

@Component
@RepositoryRestResource(path = "etext_tool_config",
        itemResourceDescription = @Description("asdf asdf"),
        collectionResourceDescription = @Description("qwerty qwerty"))
@Tag(name = "ETextToolConfigRepository", description = "Operations involving the ETextToolConfig table")
@CrossOrigin(origins = {"${lms.swagger.cors.origin}"})
public interface ETextToolConfigRepository extends PagingAndSortingRepository<ETextToolConfig, Long> {

    ETextToolConfig findByToolName(@Param("toolName") String toolName);

}