package edu.iu.uits.lms.etextmanager.repository;

/*-
 * #%L
 * etext-manager
 * %%
 * Copyright (C) 2024 Indiana University
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Indiana University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

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
