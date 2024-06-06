package edu.iu.uits.lms.etextmanager.repository;

import edu.iu.uits.lms.etextmanager.model.ETextResult;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public interface ETextResultRepository extends PagingAndSortingRepository<ETextResult, Long> {

    @Query(nativeQuery = true, value = """
        select ETEXT_RESULT_ID, ETEXT_RESULTS_BATCH_ID, TOOL, FILENAME, TOOL_ID, SIS_COURSE_ID, CANVAS_COURSE_ID, DEPLOYMENT_ID, MESSAGE, ARCHIVED, STATUS, INPUT_NEW_NAME, INPUT_PRESSBOOK_TITLE, INPUT_PRESSBOOK_LINK
        from (
            select ETEXT_RESULT_ID, ETEXT_RESULTS_BATCH_ID, TOOL, FILENAME, TOOL_ID, SIS_COURSE_ID, CANVAS_COURSE_ID, DEPLOYMENT_ID, MESSAGE, ARCHIVED, STATUS, INPUT_NEW_NAME, INPUT_PRESSBOOK_TITLE, INPUT_PRESSBOOK_LINK, row_number()
            over (partition by sis_course_id, tool order by etext_results_batch_id desc) f1
            from etext_results
            where sis_course_id is not null)
        where f1 = 1
            and archived = false
            and status = :status
        """)
    List<ETextResult> findActiveResultsByStatus(@Param("status") String status);

    @Modifying
    @Transactional(transactionManager = "etextmanagerTransactionMgr")
    @Query("update ETextResult set archived = :archived where id in (:idList)")
    int updateResults(@Param("idList") List<Long> idList, @Param("archived") boolean archived);
}
